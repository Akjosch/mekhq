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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import megamek.common.EquipmentType;
import mekhq.MekHQ;
import mekhq.campaign.Campaign;
import mekhq.campaign.parts.Part;

/**
 *
 * @author Jay Lawson <jaylawson39 at yahoo.com>
 */
public class Faction {
	// TODO: Add a possibility to create custom factions in game (and have their XML representation printed)
	public static ConcurrentMap<String, Faction> factions;
	public static String[] choosableFactionCodes = {"MERC","CC","DC","FS","FWL","LA","FC","ROS","CS","WOB","FRR","SIC","MOC","MH","OA","TC","CDS","CGB","CHH","CJF","CNC","CSJ","CSV","CW","TH","RWR"};
	
	// Special factions
	public static Faction UNDISCOVERED;
	public static Faction UNEXPLORED;
	public static Faction PIRATE;
	public static Faction REBELS;

	private String shortname;
	private String fullname;
	private String[] altNames;
	private Color color;
	private String nameGenerator;
	private boolean clan;
	private boolean periphery;
	private String[] startingPlanet;
	private int[] eraMods;

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
		startingPlanet = new String[Era.E_NUM];
		Arrays.fill(startingPlanet, "Terra");
		altNames = new String[Era.E_NUM];
		Arrays.fill(altNames, "");
		eraMods = new int[Era.E_NUM];
	}

	public String getShortName() {
		return shortname;
	}

	public String getFullName(int era) {
		String alt = "";
		if(era >= 0 && altNames.length > era) {
			alt = altNames[era];
		}
		if(alt.trim().length() == 0) {
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
		if(era >= 0 && startingPlanet.length > era) {
			return startingPlanet[era];
		} else if(startingPlanet.length > 0) {
			return startingPlanet[startingPlanet.length-1];
		}
		return "Terra";
	}

	public int getEraMod(int era) {
		if(era >= 0 && eraMods.length > era) {
			return eraMods[era];
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

    public static Faction getFactionFromXML(Node wn) throws DOMException, ParseException {
		Faction retVal = new Faction();
		NodeList nl = wn.getChildNodes();

		for (int x=0; x<nl.getLength(); x++) {
			Node wn2 = nl.item(x);
			if (wn2.getNodeName().equalsIgnoreCase("shortname")) {
				retVal.shortname = wn2.getTextContent();
			} else if (wn2.getNodeName().equalsIgnoreCase("fullname")) {
				retVal.fullname = wn2.getTextContent();
			} else if (wn2.getNodeName().equalsIgnoreCase("nameGenerator")) {
				retVal.nameGenerator = wn2.getTextContent();
			} else if (wn2.getNodeName().equalsIgnoreCase("clan")) {
				if (wn2.getTextContent().equalsIgnoreCase("true"))
					retVal.clan = true;
				else
					retVal.clan = false;
			} else if (wn2.getNodeName().equalsIgnoreCase("periphery")) {
				if (wn2.getTextContent().equalsIgnoreCase("true"))
					retVal.periphery = true;
				else
					retVal.periphery = false;
			} else if (wn2.getNodeName().equalsIgnoreCase("startingPlanet")) {
				retVal.startingPlanet = wn2.getTextContent().split(",", -2);
			} else if (wn2.getNodeName().equalsIgnoreCase("altNames")) {
				retVal.altNames = wn2.getTextContent().split(",", -2);
			} else if (wn2.getNodeName().equalsIgnoreCase("eraMods")) {
				String[] values = wn2.getTextContent().split(",", -2);
				for(int i = 0; i < values.length; i++) {
					retVal.eraMods[i] = Integer.parseInt(values[i]);
				}
			} else if (wn2.getNodeName().equalsIgnoreCase("colorRGB")) {
				String[] values = wn2.getTextContent().split(",");
				if(values.length == 3) {
					int colorRed = Integer.parseInt(values[0]);
					int colorGreen = Integer.parseInt(values[1]);
					int colorBlue = Integer.parseInt(values[2]);
					retVal.color = new Color(colorRed, colorGreen, colorBlue);
				}
			}
		}

		if(retVal.altNames.length < Era.E_NUM) {
			MekHQ.logMessage(retVal.fullname + " faction did not have a long enough altNames vector");
		}
		if(retVal.eraMods.length < Era.E_NUM) {
			MekHQ.logMessage(retVal.fullname + " faction did not have a long enough eraMods vector");
		}
		if(retVal.startingPlanet.length < Era.E_NUM) {
			MekHQ.logMessage(retVal.fullname + " faction did not have a long enough startingPlanet vector");
		}

		return retVal;
	}

    public static void generateFactions() throws DOMException, ParseException {
		MekHQ.logMessage("Starting load of faction data from XML...");
		// Initialize variables.
		factions = new ConcurrentHashMap<String, Faction>();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		Document xmlDoc = null;


		try {
			FileInputStream fis = new FileInputStream(MekHQ.getPreference(MekHQ.DATA_DIR) + "/universe/factions.xml");
			// Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			// Parse using builder to get DOM representation of the XML file
			xmlDoc = db.parse(fis);
		} catch (Exception ex) {
			MekHQ.logError(ex);
		}

		Element factionEle = xmlDoc.getDocumentElement();
		NodeList nl = factionEle.getChildNodes();

		// Get rid of empty text nodes and adjacent text nodes...
		// Stupid weird parsing of XML.  At least this cleans it up.
		factionEle.normalize();

		// Okay, lets iterate through the children, eh?
		for (int x = 0; x < nl.getLength(); x++) {
			Node wn = nl.item(x);

			if (wn.getParentNode() != factionEle)
				continue;

			int xc = wn.getNodeType();

			if (xc == Node.ELEMENT_NODE) {
				// This is what we really care about.
				// All the meat of our document is in this node type, at this
				// level.
				// Okay, so what element is it?
				String xn = wn.getNodeName();

				if (xn.equalsIgnoreCase("faction")) {
					Faction f = getFactionFromXML(wn);
					factions.put(f.getShortName(), f);
				} else if (xn.equalsIgnoreCase("choosableFactionCodes")) {
					choosableFactionCodes = wn.getTextContent().split(",");
				}
			}
		}
		
		// Populate default factions
		UNDISCOVERED = getFaction("UND");
		UNEXPLORED = getFaction("NONE");
		PIRATE = getFaction("PIR");
		REBELS = getFaction("REB");
		
		MekHQ.logMessage("Loaded a total of " + factions.keySet().size() + " factions");
	}
}
