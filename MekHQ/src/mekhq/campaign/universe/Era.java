/*
 * Era.java
 * 
 * Copyright (c) 2009 Jay Lawson <jaylawson39 at yahoo.com>. All rights reserved.
 * 
 * This file is part of MekHQ.
 * 
 * MekHQ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MekHQ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MekHQ.  If not, see <http://www.gnu.org/licenses/>.
 */

package mekhq.campaign.universe;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.transform.stream.StreamSource;

import mekhq.MekHQ;

/**
 *
 * @author Jay Lawson <jaylawson39 at yahoo.com>
 */
@XmlRootElement(name="era")
@XmlAccessorType(XmlAccessType.FIELD)
public class Era {
    private final static Object LOADING_LOCK = new Object[0];
    
    private static Map<String, Era> eraRegistry;
    private static Map<Integer, Era> numericEras;
    private static Era defaultEra;
    
    private static boolean initialized = false;
    private static boolean initializing = false;
    
    @XmlElement(required=true)
    private final String id;
    private int numeric;
    @XmlElement(defaultValue="Unknown")
    private String name = "Unknown";
    /** Starting year */
    @XmlElement(required=true)
    private int start;
    /** Ending year */
    @XmlElement(required=true)
    private int end;
    @XmlElement(defaultValue="ALL")
    private String parent = "ALL";
    @XmlElement(defaultValue="ALL")
    private FactionGroup factionGroup = FactionGroup.ALL;
    /** Corresponding tech manual availability */
    @XmlElement(defaultValue="-1")
    private int availability = -1;
    
    @XmlTransient
    private Set<Era> children;

    public static final int E_AOW   = 0;
    public static final int E_RW    = 1;
    public static final int E_SL    = 2;
    public static final int E_1SW   = 3;
    public static final int E_2SW   = 4;
    public static final int E_3SW   = 5;
    public static final int E_4SW   = 6;
    public static final int E_CLAN  = 7;
    public static final int E_JIHAD = 8;
    public static final int E_NUM   = 9;
    
    // for JAXB only
    private Era() {
        this("", -1);
    }

    private Era(String id) {
        this(id, -1);
    }
    
    private Era(String id, int numeric) {
        this.id = Objects.requireNonNull(id);
        this.numeric = numeric;
        this.children = new HashSet<Era>();
    }
    
    public String getId() {
        return id;
    }
    
    public int getNumeric() {
        return numeric;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = Objects.requireNonNull(name);
    }
    
    public int getStart() {
        return start;
    }
    
    public int getEnd() {
        return end;
    }
    
    public boolean isYearInEra(int year) {
        return (start <= year) && (year <= end);
    }
    
    public Era getParentEra() {
        if( null == parent ) {
            return defaultEra;
        }
        Era result = Era.valueOf(parent);
        return (null != result) ? result : defaultEra;
    }
    
    /** @return true if this era or one of its parents has the specified id */
    public boolean is(String eraId) {
    	if( null == eraId ) {
    		return false;
    	}
    	if( id.equals(eraId) ) {
    		return true;
    	}
    	return (null != parent) && getParentEra().is(eraId);
    }
    
    public FactionGroup getFactionGroup() {
        return factionGroup;
    }
    
    public Set<Era> getChildren() {
        return new HashSet<Era>(children);
    }
    
    /**
     * @return the "leaf" era for the given year and faction group, or null if this era
     *         doesn't cover the specified year or faction group.
     */
    public Era getActualEra(int year, FactionGroup fg) {
        if( null == fg ) {
            fg = FactionGroup.ALL;
        }
        if( !isYearInEra(year) || !fg.is(factionGroup) ) {
            return null;
        }
        for( Era child : children ) {
            if( child.isYearInEra(year) && fg.is(child.factionGroup) ) {
                return child.getActualEra(year, fg);
            }
        }
        return this;
    }
    
    public int getAvailability() {
        return availability;
    }
    
    public static Era getEra(int year) {
        return getEra(year, FactionGroup.ALL);
    }

    public static Era getEra(int year, FactionGroup fg) {
        return defaultEra.getActualEra(year, fg);
    }
    
    public static String getEraNameFromYear(int year) {
        return getEraNameFromYear(year, FactionGroup.ALL);
    }
 
    public static String getEraNameFromYear(int year, FactionGroup fg) {
        return getEra(year, fg).getName();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public boolean equals(Object obj) {
        if( this == obj ) {
            return true;
        }
        if( null == obj || getClass() != obj.getClass() ) {
            return false;
        }
        final Era other = (Era)obj;
        return Objects.equals(id, other.id);
    }

    public static Era valueOf(String id) {
        initializeEras();
        return eraRegistry.get(id);
    }
    
    public static Era valueOf(int numeric) {
        initializeEras();
        return numericEras.get(numeric);
    }
    
    private static boolean registerEra(Era era) {
        // Invariant: ID is unique
        if( eraRegistry.containsKey(era.id) ) {
            MekHQ.logError(String.format("Era registry: %s's ID \"%s\" is already registered",
                    era.name, era.id));
            return false;
        }
        // Invariant: Start before the end
        if( era.start > era.end ) {
            MekHQ.logError(String.format("Era registry: %s has start year %d after end yeard %d",
                    era.name, era.start, era.end));
            return false;
        }
        // Invariant: All eras but the default need to have a valid parent
        Era parent = valueOf(era.parent);
        if( null == parent ) {
            MekHQ.logError(String.format("Era registry: %s's parent with ID \"%s\" is not registered",
                    era.name, era.parent));
            return false;
        }
        // Invariant: parent's year must include this era's year
        if( !parent.isYearInEra(era.start) || !parent.isYearInEra(era.end) ) {
            MekHQ.logError(String.format("Era registry: %s's years [%d-%d] don't fit into its parents [%d-%d]",
                    era.name, era.start, era.end, parent.start, parent.end));
            return false;
        }
        // Invariant: A parent's faction group needs to be the same or a child faction group
        if( !era.factionGroup.is(parent.factionGroup) ) {
            MekHQ.logError(String.format("Era registry: %s's parent has an incompatible faction group: %s is not %s",
                    era.name, era.factionGroup, parent.factionGroup));
            return false;
        }
        // Invariant: A parent has to have no children which cover the same years and faction groups
        for( Era child : parent.children ) {
            if( (child.start <= era.end) && (era.start <= child.end) ) {
                // Range overlap, check for faction groups
                if( era.factionGroup.is(child.factionGroup) ) {
                    MekHQ.logError(String.format("Era registry: %s's year range and factions overlap with the parent's child %s",
                            era.name, child.name));
                    return false;
                }
            }
        }
        eraRegistry.put(era.id, era);
        if( -1 != era.numeric ) {
            numericEras.put(era.numeric, era);
        }
        parent.children.add(era);
        return true;
    }
    
    public static void initializeEras() {
        synchronized (LOADING_LOCK) {
            if( initialized || initializing ) {
                return;
            }
            MekHQ.logMessage("Starting load of era data from XML...");
            initializing = true;
            try(FileInputStream fis = new FileInputStream("data/universe/eras.xml")) {
                JAXBContext context = JAXBContext.newInstance(Era.class, EraList.class, FactionGroup.class);
                Unmarshaller unmarshaller = context.createUnmarshaller();
                EraList list = unmarshaller.unmarshal(new StreamSource(fis), EraList.class).getValue();
                if( null != list.eras ) {
                    for( Era era : list.eras ) {
                        registerEra(era);
                    }
                }
            } catch (Exception ex) {
                MekHQ.logError(ex);
            } finally {
                initialized = true;
            }

        }
        MekHQ.logMessage("Loaded a total of " + eraRegistry.keySet().size() + " eras");
    }
    
    static {
        // Default era setup
        eraRegistry = new HashMap<String, Era>();
        numericEras = new HashMap<Integer, Era>();
        defaultEra = new Era("ALL");
        defaultEra.name = "All of civilization";
        defaultEra.parent = null;
        defaultEra.start = 0;
        defaultEra.end = 9999;
        eraRegistry.put(defaultEra.id, defaultEra);
    }
    
    @XmlRootElement(name="eras")
    @XmlAccessorType(XmlAccessType.FIELD)
    private static class EraList {
        @XmlElement(name="era")
        private List<Era> eras;
    }
}
