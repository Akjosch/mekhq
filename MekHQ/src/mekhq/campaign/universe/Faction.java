/*
 * Faction.java
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

import java.awt.Color;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.DOMException;

import megamek.common.EquipmentType;
import mekhq.FileParser;
import mekhq.MekHQ;
import mekhq.Utilities;
import mekhq.adapters.BooleanValueAdapter;
import mekhq.adapters.ColorAdapter;
import mekhq.adapters.IntegerListAdapter;
import mekhq.adapters.StringListAdapter;
import mekhq.campaign.Campaign;
import mekhq.campaign.parts.Part;

/**
 *
 * @author Jay Lawson <jaylawson39 at yahoo.com>
 */
@XmlRootElement(name="faction")
@XmlAccessorType(XmlAccessType.FIELD)
public class Faction {
	private final static Object LOADING_LOCK = new Object[0];
	
	// TODO: Add a possibility to create custom factions in game (and have their XML representation printed)
	public static ConcurrentMap<String, Faction> factions;
	public static List<String> choosableFactionCodes = Arrays.asList("MERC","CC","DC","FS","FWL","LA","FC","ROS","CS","WOB","FRR","SIC","MOC","MH","OA","TC","CDS","CGB","CHH","CJF","CNC","CSJ","CSV","CW","TH","RWR");
	
	// Special factions
	public static Faction UNDISCOVERED;
	public static Faction UNEXPLORED;
	public static Faction PIRATE;
	public static Faction REBELS;

	// Marshaller / unmarshaller instances
	private static Marshaller marshaller;
	private static Unmarshaller unmarshaller;
	static {
		try {
			JAXBContext context = JAXBContext.newInstance(Faction.class, FactionData.class);
			marshaller = context.createMarshaller();
			marshaller.setProperty("jaxb.fragment", Boolean.TRUE);
			unmarshaller = context.createUnmarshaller();
		} catch(JAXBException e) {
			MekHQ.logError(e);
		}
	}
	
	@XmlElement(required=true)
	private String shortname;
	@XmlElement(required=true)
	private String fullname;
	@XmlJavaTypeAdapter(StringListAdapter.class)
	private List<String> altNames;
	@XmlElement(name="colorRGB", defaultValue="192,192,192")
	@XmlJavaTypeAdapter(ColorAdapter.class)
	private Color color;
	@XmlElement(defaultValue="General")
	private String nameGenerator;
	@XmlElement(defaultValue="false")
	@XmlJavaTypeAdapter(BooleanValueAdapter.class)
	private Boolean clan;
	@XmlElement(defaultValue="false")
	@XmlJavaTypeAdapter(BooleanValueAdapter.class)
	private Boolean periphery;
	@XmlJavaTypeAdapter(StringListAdapter.class)
	private List<String> startingPlanet;
	@XmlJavaTypeAdapter(IntegerListAdapter.class)
	private List<Integer> eraMods;
	private Integer startYear;
	private Integer endYear;
	
	/** Is it an auto-generated faction? */
	@XmlTransient
	private boolean generated = false;
	/** Is this faction modified compared to the version saved? */
	@XmlTransient
	private boolean modified = false;

	public Faction() {
		this("???", "Unknown");
	}

	public Faction(String sname, String fname) {
		shortname = sname;
		fullname = fname;
		nameGenerator = "General";
		clan = false;
		periphery = false;
		color = Color.LIGHT_GRAY;
		startingPlanet = new ArrayList<String>(Era.E_NUM);
		startingPlanet.addAll(Collections.nCopies(Era.E_NUM, "Terra"));
		altNames = new ArrayList<String>(Era.E_NUM);
		altNames.addAll(Collections.nCopies(Era.E_NUM, ""));
		eraMods = new ArrayList<Integer>(Era.E_NUM);
		eraMods.addAll(Collections.nCopies(Era.E_NUM, 0));
	}

	public String getShortName() {
		return shortname;
	}

	public String getFullName(int era) {
		String alt = "";
		if(era >= 0 && altNames.size() > era) {
			alt = altNames.get(era);
		}
		if(null == alt || alt.trim().length() == 0) {
			return fullname;
		} else {
			return alt;
		}
	}

	public Color getColor() {
		return color;
	}

	public boolean isClan() {
		return clan;
	}

	public boolean isPeriphery() {
		return periphery;
	}
	
	public boolean isGenerated() {
		return generated;
	}
	
	public boolean isModified() {
		return modified;
	}
	
	/** @return true if this faction represents a lack of civilization (though not necessarily lack of people) */
	public boolean isEmpty() {
		return shortname.equals("ABN") // Abandoned
				|| shortname.equals("UND") // Undiscovered
				|| shortname.equals("NONE") // Unexplored
				|| shortname.equals("???"); // Default short name for a not initialized faction
	}

	public String getNameGenerator() {
		return nameGenerator;
	}

	public String getStartingPlanet(int era) {
		if(era >= 0 && startingPlanet.size() > era) {
			return startingPlanet.get(era);
		} else if(startingPlanet.size() > 0) {
			return startingPlanet.get(startingPlanet.size() - 1);
		}
		return "Terra";
	}

	public int getEraMod(int era) {
		if(era >= 0 && eraMods.size() > era) {
			return eraMods.get(era);
		}
		return 0;
	}

	public int getTechMod(Part part, Campaign campaign) {
		int currentYear = campaign.getCalendar().get(Calendar.YEAR);

		//TODO: This seems hacky - we shouldn't hardcode in universe details
		//like this
        int factionMod = 0;
        if (part.getTechBase() == Part.T_CLAN && !isClan()) {
            // Availability of clan tech for IS
            if (currentYear<3050)
                // Impossible to buy before clan invasion
                factionMod = 12;
            else if (currentYear<=3052)
                // Between begining of clan invasiuon and tukayyid, very very hard to buy
                factionMod = 5;
            else if (currentYear<=3060)
                // Between tukayyid and great refusal, very hard to buy
                factionMod = 4;
            else
                // After great refusal, hard to buy
                factionMod = 3;
        }
        if (part.getTechBase() == Part.T_IS && isPeriphery()) {
            // Availability of high tech rating equipment in low tech areas (periphery)
            switch (part.getTechRating()) {
                case(EquipmentType.RATING_E) :
                	factionMod += 1;
                    break;
                case(EquipmentType.RATING_F) :
                	factionMod += 2;
                    break;
            }
        }

        return factionMod;
	}

	@Override
	public int hashCode() {
		return 31 + ((shortname == null) ? 0 : shortname.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if( this == obj ) {
			return true;
		}
		if( obj == null ) {
			return false;
		}
		if( getClass() != obj.getClass() ) {
			return false;
		}
		Faction other = (Faction) obj;
		if( shortname == null ) {
			if( other.shortname != null ) {
				return false;
			}
		} else if( !shortname.equals(other.shortname) ) {
			return false;
		}
		return true;
	}
	
	/**
	 * This method is called after all the properties (except IDREF) are unmarshalled for this object, 
	 * but before this object is set to the parent object.
	 */
	@SuppressWarnings("unused")
	private void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
		// Generic warnings about possibly outdated data
		if(altNames.size() < Era.E_NUM) {
			MekHQ.logMessage(fullname + " faction did not have a long enough altNames vector");
		}
		if(eraMods.size() < Era.E_NUM) {
			MekHQ.logMessage(fullname + " faction did not have a long enough eraMods vector");
		}
		if(startingPlanet.size() < Era.E_NUM) {
			MekHQ.logMessage(fullname + " faction did not have a long enough startingPlanet vector");
		}
		
		altNames.addAll(Collections.nCopies(Math.max(0, Era.E_NUM - altNames.size()), ""));
		eraMods.addAll(Collections.nCopies(Math.max(0, Era.E_NUM - eraMods.size()), 0));
		String planet = startingPlanet.isEmpty() ? "Terra" : startingPlanet.get(startingPlanet.size() - 1);
		startingPlanet.addAll(Collections.nCopies(Math.max(0, Era.E_NUM - startingPlanet.size()), planet));
	}

	public void writeToXML(OutputStream out) {
		try {
			marshaller.marshal(this, out);
		} catch(JAXBException e) {
			MekHQ.logError(e);
			throw new RuntimeException(e);
		}
	}
	
	public static Faction generateLocalFaction(Planet planet) {
		return generateLocalFaction(planet, "Local faction (" + planet.getName(null) + ")");
	}
	
	public static Faction generateLocalFaction(Planet planet, String name) {
		if( null == planet || null == name ) {
			throw new NullPointerException();
		}
		Faction result = new Faction();
		result.fullname = name;
		result.startingPlanet.clear();
		result.startingPlanet.addAll(Collections.nCopies(Era.E_NUM, planet.getId()));
		result.periphery = true;
		result.generated = true;
		result.modified = true;
		
		String baseShortName = "_LOCAL_" + planet.getId().replaceAll("\\s+", "_").toUpperCase(Locale.ROOT);
		result.shortname = baseShortName;
		synchronized(LOADING_LOCK) {
			int i = 2;
			while( factions.containsKey(result.shortname) ) {
				result.shortname = baseShortName + "_" + i;
				++ i;
			}
			factions.put(result.shortname, result);
		}
		
		return result;
	}
	
	/** @return a copy of the short faction names */
	public static List<String> getFactionList() {
		List<String> flist = new ArrayList<String>();
		for(String sname : factions.keySet()) {
			flist.add(sname);
		}
		return flist;
	}

	public static Faction getFaction(String sname) {
		return null != factions ? factions.get(sname) : null;
	}

	public static Faction getFactionFromFullName(String fname, int year) {
		return getFactionFromFullNameAndEra(fname, Era.getEra(year));
	}

	public static Faction getFactionFromFullNameAndEra(String fname, int era) {
		Faction faction = null;
		for (Faction f : factions.values()) {
			if (f.getFullName(era).equals(fname)) {
				faction = f;
				break;
			}
		}
		return faction;
	}

	public static Faction getFactionFromXML(InputStream source) {
		try {
			return unmarshaller.unmarshal(new StreamSource(source), Faction.class).getValue();
		} catch(JAXBException e) {
			MekHQ.logError(e);
		}
		return null;
	}
	
	private static void updateFactions(InputStream source) {
		try {
			FactionData list = unmarshaller.unmarshal(new StreamSource(source), FactionData.class).getValue();
			if( null != list.factions ) {
				for( Faction faction : list.factions ) {
					factions.put(faction.shortname, faction);
				}
			}
			choosableFactionCodes = Utilities.nonNull(list.choosableFactionCodes, choosableFactionCodes);
		} catch(JAXBException e) {
			MekHQ.logError(e);
		}
	}

    public static void generateFactions() throws DOMException, ParseException {
		MekHQ.logMessage("Starting load of faction data from XML...");
		synchronized (LOADING_LOCK) {
			// Step 1: Initialize variables.
			if( null == factions ) {
				factions = new ConcurrentHashMap<String, Faction>();
			}
			factions.clear();
			
			// Step 2: Read the default file
			try {
				FileInputStream fis = new FileInputStream(MekHQ.getPreference(MekHQ.DATA_DIR) + "/universe/factions.xml");
				updateFactions(fis);
				fis.close();
			} catch (Exception ex) {
				MekHQ.logError(ex);
			}

			// Step 3: Load all the xml files within the "factions" subdirectory, if it exists
			Utilities.parseXMLFiles(MekHQ.getPreference(MekHQ.DATA_DIR) + "/universe/factions",
					new FileParser() {
						@Override
						public void parse(InputStream is) {
							updateFactions(is);
						}
					});
		
			// Populate default factions
			UNDISCOVERED = getFaction("UND");
			UNEXPLORED = getFaction("NONE");
			PIRATE = getFaction("PIR");
			REBELS = getFaction("REB");
		}
		MekHQ.logMessage("Loaded a total of " + factions.keySet().size() + " factions");
	}
    
    @XmlRootElement(name="factions")
    private static class FactionData {
    	@XmlElement(name="faction")
    	public List<Faction> factions;
    	@XmlJavaTypeAdapter(StringListAdapter.class)
    	public List<String> choosableFactionCodes;
    }
}
