/*
 * java
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
 * 
 * @author: Dylan Myers <ralgith@gmail.com>
 */
package mekhq.campaign.personnel;

import java.io.PrintWriter;
import java.util.UUID;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import mekhq.MekHQ;
import mekhq.MekHqXmlUtil;
import mekhq.Utilities;
import mekhq.campaign.mod.am.BodyLocation;

// Injury class based on Jayof9s' <jayof9s@gmail.com> Advanced Medical documents
public class Injury {
    private String fluff;
    private int days;
    private int originalDays;
    /** 0 = past injury, for scars, 1 = default, max depends on type */
    private int severity;
    private BodyLocation location;
    private InjuryType type;
    private boolean permanent;
    private boolean workedOn;
    private boolean extended;
    protected UUID id;
     
     // Base constructor, in reality should never be used
     private Injury() {
         this(0, "", BodyLocation.GENERIC, InjuryType.BAD_HEALTH, 1, false, false);
    }
    
     // Normal constructor for a new injury that has not been treated by a doctor & does not have extended time
    public Injury(int time, String text, BodyLocation loc, InjuryType type, int num, boolean perm) {
        this(time, text, loc, type, num, perm, false);
    }

    // Constructor if this injury has been treated by a doctor, but without extended time
    public Injury(int time, String text, BodyLocation loc, InjuryType type, int num, boolean perm, boolean workedOn) {
        this(time, text, loc, type, num, perm, workedOn, false);
    }
    
    // Constructor for when this injury has extended time, full options includng worked on by a doctor
    public Injury(int time, String text, BodyLocation loc, InjuryType type, int num, boolean perm, boolean workedOn, boolean extended) {
        setTime(time);
        originalDays = time;
        setFluff(text);
        location = loc;
        setType(type);
        setSeverity(num);
        setPermanent(perm);
        setWorkedOn(workedOn);
        setExtended(extended);
        id = UUID.randomUUID();
    }
    
    // UUID Control Methods
    public UUID getUUID() {
        return id;
    }
    
    public void setUUID(UUID uuid) {
        id = uuid;
    }
    // End UUID Control Methods
    
    // Time Control Methods
    public int getTime() {
        return days;
    }
    
    public void setTime(int time) {
        days = time;
    }
    
    public int getOriginalTime() {
        return originalDays;
    }
    // End Time Control Methods
    
    // Details Methods (Fluff, Location on Body, how many hits did it take, etc...)
    public String getFluff() {
        return fluff;
    }
    
    public void setFluff(String text) {
        fluff = text;
    }
    
    public BodyLocation getLocation() {
        return location;
    }
    
    public int getSeverity() {
        return severity;
    }
    
    public void setSeverity(int num) {
        final int minSeverity = isPermanent() ? 1 : 0;
        final int maxSeverity = type.getMaxSeverity();
        if(num < minSeverity) {
            num = minSeverity;
        } else if(num > maxSeverity) {
            num = maxSeverity;
        }
        severity = num;
    }
    
    public boolean isPermanent() {
        return permanent || type.isPermanent();
    }
    
    public void setPermanent(boolean perm) {
        permanent = perm;
    }
    
    public boolean getExtended() {
        return extended;
    }
    
    public void setExtended(boolean ext) {
        extended = ext;
    }
    
    public boolean getWorkedOn() {
        return workedOn;
    }
    
    public void setWorkedOn(boolean wo) {
        workedOn = wo;
    }
    
    public InjuryType getType() {
        return type;
    }
    
    public void setType(InjuryType type) {
        this.type = type;
    }
    // End Details Methods
    
    // Returns the full long name of this injury including location and type as applicable
    public String getName() {
        return type.getName(location, severity);
    }
    
    // Save to campaign file as XML
    // Also used by the personnel exporter
    public void writeToXml(PrintWriter pw1, int indent) {
        pw1.println(MekHqXmlUtil.indentStr(indent) + "<injury>");
        pw1.println(MekHqXmlUtil.indentStr(indent+1)
                +"<fluff>"
                +MekHqXmlUtil.escape(fluff)
                +"</fluff>");
        pw1.println(MekHqXmlUtil.indentStr(indent+1)
                +"<days>"
                +days
                +"</days>");
        pw1.println(MekHqXmlUtil.indentStr(indent+1)
                +"<originalDays>"
                +originalDays
                +"</originalDays>");
        pw1.println(MekHqXmlUtil.indentStr(indent+1)
                +"<hits>"
                +severity
                +"</hits>");
        pw1.println(MekHqXmlUtil.indentStr(indent+1)
                +"<location>"
                +location
                +"</location>");
        pw1.println(MekHqXmlUtil.indentStr(indent+1)
                +"<type>"
                +type
                +"</type>");
        pw1.println(MekHqXmlUtil.indentStr(indent+1)
                +"<permanent>"
                +permanent
                +"</permanent>");
        pw1.println(MekHqXmlUtil.indentStr(indent+1)
                +"<extended>"
                +extended
                +"</extended>");
        pw1.println(MekHqXmlUtil.indentStr(indent+1)
                +"<workedOn>"
                +workedOn
                +"</workedOn>");
        pw1.println(MekHqXmlUtil.indentStr(indent+1)
                +"<InjuryUUID>"
                +id.toString()
                +"</InjuryUUID>");
        pw1.println(MekHqXmlUtil.indentStr(indent) + "</injury>");
    }
    
    // Load from campaign file XML
    // Also used by the personnel exporter
    public static Injury generateInstanceFromXML(Node wn) {
        Injury retVal = new Injury();
        
        try {    
            // Okay, now load fields!
            NodeList nl = wn.getChildNodes();
            
            for (int x=0; x<nl.getLength(); x++) {
                Node wn2 = nl.item(x);
                
                if (wn2.getNodeName().equalsIgnoreCase("fluff")) {
                    retVal.fluff = wn2.getTextContent();
                } else if (wn2.getNodeName().equalsIgnoreCase("days")) {
                    retVal.days = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("originalDays")) {
                    retVal.originalDays = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("hits")) {
                    retVal.severity = Integer.parseInt(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("location")) {
                    retVal.location = BodyLocation.of(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("type")) {
                    retVal.type = InjuryType.byKey(wn2.getTextContent().trim());
                } else if (wn2.getNodeName().equalsIgnoreCase("permanent")) {
                    if (wn2.getTextContent().equalsIgnoreCase("true")) {
                        retVal.permanent = true;
                    } else {
                        retVal.permanent = false;
                    }
                } else if (wn2.getNodeName().equalsIgnoreCase("extended")) {
                    if (wn2.getTextContent().equalsIgnoreCase("true")) {
                        retVal.extended = true;
                    } else {
                        retVal.extended = false;
                    }
                } else if (wn2.getNodeName().equalsIgnoreCase("workedOn")) {
                    if (wn2.getTextContent().equalsIgnoreCase("true")) {
                        retVal.workedOn = true;
                    } else {
                        retVal.workedOn = false;
                    }
                } else if (wn2.getNodeName().equalsIgnoreCase("InjuryUUID")) {
                    retVal.id = UUID.fromString(wn2.getTextContent());
                }
            }
            if (retVal.id == null) { // We didn't have an ID, so let's generate one!
                retVal.id = UUID.randomUUID();
            }
        } catch (Exception ex) {
            // Doh!
            MekHQ.logError(ex);
        }
        
        return retVal;
    }
    
    // Return the location name for the injury by passing location to the static overload
    public String getLocationName() {
        return Utilities.capitalize(location.readableName);
    }
    
    // Return the name for this type of injury by passing the type to the static overload
    public String getTypeKey() {
        return type.getKey();
    }

    public int getTypeId() {
        return type.getId();
    }
}