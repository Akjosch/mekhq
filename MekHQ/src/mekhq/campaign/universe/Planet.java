/*
 * Planet.java
 *
 * Copyright (c) 2011 Jay Lawson <jaylawson39 at yahoo.com>. All rights reserved.
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

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;

import javax.swing.JOptionPane;

import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import megamek.common.Compute;
import megamek.common.EquipmentType;
import megamek.common.PlanetaryConditions;


/**
 * This is the start of a planet object that will keep lots of information about
 * planets that can be displayed on the interstellar map.
 *
 *
 * @author Jay Lawson <jaylawson39 at yahoo.com>
 */
public class Planet implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -8699502165157515099L;

	private static final int TYPE_EMPTY			= 0;
	private static final int TYPE_ASTEROID		= 1;
	private static final int TYPE_DWARF			= 2;
	private static final int TYPE_TERRESTRIAL	= 3;
	private static final int TYPE_GIANT 		= 4;
	private static final int TYPE_GAS_GIANT		= 5;
	private static final int TYPE_ICE_GIANT		= 6;

	private static final int LIFE_NONE    = 0;
	private static final int LIFE_MICROBE = 1;
	private static final int LIFE_PLANT   = 2;
	private static final int LIFE_FISH    = 3;
	private static final int LIFE_AMPH    = 4;
	private static final int LIFE_REPTILE = 5;
	private static final int LIFE_BIRD    = 6;
	private static final int LIFE_MAMMAL  = 7;
	private static final int LIFE_INSECT  = 8;

	private static final int CLIMATE_ARCTIC   = 0;
	private static final int CLIMATE_BOREAL   = 1;
	private static final int CLIMATE_COOLTEM  = 2;
	private static final int CLIMATE_WARMTEM  = 3;
	private static final int CLIMATE_ARID     = 4;
	private static final int CLIMATE_TROPICAL = 5;

	private Star star = new Star();
	
	/**
	 * This is the base faction which the program will fall back on if
	 * no better faction is found in the faction history given the date
	 */
	private ArrayList<String> factionCodes;
	private ArrayList<String> garrisonUnits;
	
	private String id;
	private String name;
	private String shortName;

	private int sysPos;

	private int pressure;
	private double gravity;
	//fluff
	private int lifeForm;
	private int climate;
	private int percentWater;
	private int temperature;
	private int hpg;
	private String desc;
	
	// Orbital data
	private double orbitSemimajorAxis = 0.0;

	//socioindustrial levels
	private int tech;
	private int industry;
	private int rawMaterials;
	private int output;
	private int agriculture;

	//keep some string information in arraylists
	private ArrayList<String> satellites;
	private ArrayList<String> landMasses;

	//a hash to keep track of dynamic faction changes
	TreeMap<Date,ArrayList<String>> factionHistory;

	//a hash to keep track of dynamic garrison changes
	TreeMap<Date,ArrayList<String>> garrisonHistory;

	public Planet() {
		this.factionCodes = new ArrayList<String>();
		this.factionCodes.add("CS");
		this.garrisonUnits = new ArrayList<String>();
		this.id = null;
		this.name = "Terra";
		this.shortName = "Terra";

		this.sysPos = 1;

		this.pressure = PlanetaryConditions.ATMO_STANDARD;
		this.gravity = 1.0;

		this.lifeForm = LIFE_NONE;
		this.climate = CLIMATE_WARMTEM;
		this.percentWater = 70;
		this.temperature = 20;
		this.desc = "Nothing here yet. Who wants to volunteer to enter planet data?";

		this.tech = EquipmentType.RATING_C;
		this.industry = EquipmentType.RATING_C;
		this.rawMaterials = EquipmentType.RATING_C;
		this.output = EquipmentType.RATING_C;
		this.agriculture = EquipmentType.RATING_C;

		this.satellites = new ArrayList<String>();
		this.landMasses = new ArrayList<String>();

		this.hpg = EquipmentType.RATING_B;

		this.factionHistory = new TreeMap<Date,ArrayList<String>>();
	}

	public Star getStar() {
		return star;
	}

	/**
	 * @deprecated Use {@link mekhq.campaign.universe.Star#getSpectralClass()} instead
	 */
	public int getSpectralClass() {
		return star.getSpectralClass();
	}

    /**
	 * @deprecated Use {@link mekhq.campaign.universe.Star#setSpectralClass(mekhq.campaign.universe.Planet,int)} instead
	 */
	public void setSpectralClass(int spectralClass) {
		star.setSpectralClass(spectralClass);
	}

    /**
	 * @deprecated Use {@link mekhq.campaign.universe.Star#getSubtype()} instead
	 */
	public int getSubtype() {
		return star.getSubtype();
	}

    /**
	 * @deprecated Use {@link mekhq.campaign.universe.Star#setSubtype(mekhq.campaign.universe.Planet,int)} instead
	 */
	public void setSubtype(int subtype) {
		star.setSubtype(subtype);
	}

    public static String getLifeFormName(int life) {
		switch(life) {
		case LIFE_NONE:
			return "None";
		case LIFE_MICROBE:
			return "Microbes";
		case LIFE_PLANT:
			return "Plants";
		case LIFE_FISH:
			return "Fish";
		case LIFE_AMPH:
			return "Amphibians";
		case LIFE_REPTILE:
			return "Reptiles";
		case LIFE_BIRD:
			return "Birds";
		case LIFE_MAMMAL:
			return "Mammals";
		case LIFE_INSECT:
			return "Insects";
		default:
			return "Unknown";
		}
	}

	public static String getClimateName(int cl) {
		switch(cl) {
		case CLIMATE_ARCTIC:
			return "Arctic";
		case CLIMATE_BOREAL:
			return "Boreal";
		case CLIMATE_COOLTEM:
			return "Cool-Temperate";
		case CLIMATE_WARMTEM:
			return "Warm-Temperate";
		case CLIMATE_ARID:
			return "Arid";
		case CLIMATE_TROPICAL:
			return "Tropical";
		default:
			return "Unknown";
		}
	}

	public String getSocioIndustrialLevel() {
		return EquipmentType.getRatingName(tech) + "-" + EquipmentType.getRatingName(industry) + "-" + EquipmentType.getRatingName(rawMaterials) + "-" + EquipmentType.getRatingName(output) + "-" + EquipmentType.getRatingName(agriculture);
	}

	public String getHPGClass() {
		return EquipmentType.getRatingName(hpg);
	}

	@Deprecated
	public double getX() {
		return star.getX();
	}

	@Deprecated
	public double getY() {
		return star.getY();
	}

	public ArrayList<String> getGarrisonUnits() {
		return garrisonUnits;
	}

	public ArrayList<String> getBaseFactionCodes() {
		return factionCodes;
	}

	public ArrayList<Faction> getBaseFactions() {
		return getFactionsFrom(factionCodes);
	}

	private static ArrayList<Faction> getFactionsFrom(ArrayList<String> codes) {
		ArrayList<Faction> factions = new ArrayList<Faction>();
		for(String code : codes) {
			factions.add(Faction.getFaction(code));
		}
		return factions;
	}

	public int getSystemPosition() {
		return sysPos;
	}

	public ArrayList<Faction> getCurrentFactions(Date date) {
		ArrayList<String> currentFactionCode = getBaseFactionCodes();
		for(Date event : factionHistory.keySet()) {
			if(event.after(date)) {
				break;
			} else {
				currentFactionCode = factionHistory.get(event);
			}
		}
		return getFactionsFrom(currentFactionCode);
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getShortName() {
		return shortName;
	}

	public String getShortDesc(Date date) {
		return getShortName() + " (" + getFactionDesc(date) + ")";
	}

	public String getFactionDesc(Date date) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(date);
		String desc = "";
		Iterator<Faction> factions = getCurrentFactions(date).iterator();
		while(factions.hasNext()) {
			Faction f = factions.next();
			desc += f.getFullName(Era.getEra(cal.get(Calendar.YEAR)));
			if(factions.hasNext()) {
				desc += "/";
			}
		}
		return desc;
	}

	/**
	 * Used when there are planets with duplicate names
	 * @param n
	 */
	public void resetName(String n) {
		this.shortName = name;
		this.name = n;
	}


	public double getGravity() {
		return gravity;
	}

	public int getPressure() {
		return pressure;
	}

	public String getPressureName() {
		return PlanetaryConditions.getAtmosphereDisplayableName(pressure);
	}

	public String getLifeFormName() {
		return getLifeFormName(lifeForm);
	}

	public String getClimateName() {
		return getClimateName(climate);
	}

	public int getPercentWater() {
		return percentWater;
	}

	public int getTemperature() {
		return temperature;
	}

	/** @return a point representing a not exactly defined point on the surface of this planet */
	public SpaceLocation getPointOnSurface() {
		return new OrbitalPoint(getStar(), orbitSemimajorAxis);
	}
	
	/**
	 * @deprecated Use {@link mekhq.campaign.universe.Star#getStarType(mekhq.campaign.universe.Planet)} instead
	 */
	public String getStarType() {
		return star.getStarType();
	}

	public String getSatelliteDescription() {
		if(satellites.isEmpty()) {
			return "0";
		}
		String toReturn = satellites.size() + " (";
		for(int i = 0; i < satellites.size(); i++) {
			toReturn += satellites.get(i);
			if(i < (satellites.size() - 1)) {
				toReturn += ", ";
			} else {
				toReturn += ")";
			}
		}
		return toReturn;
	}

	public String getLandMassDescription() {
		String toReturn = "";
		for(int i = 0; i < landMasses.size(); i++) {
			toReturn += landMasses.get(i);
			if(i < (landMasses.size() - 1)) {
				toReturn += ", ";
			}
		}
		return toReturn;
	}

	/**
	 * @deprecated Use {@link mekhq.campaign.universe.Star#getRechargeStations()} instead
	 */
	public String getRechargeStations() {
		return star.getRechargeStations();
	}

	/**
	 * @deprecated Use {@link mekhq.campaign.universe.Star#getRechargeTime(mekhq.campaign.universe.Planet)} instead
	 */
	public int getRechargeTime() {
		return star.getRechargeTime();
	}

	/** @return the average travel time from low orbit to the jump point at 1g, in Terran days */
	public double getTimeToJumpPoint(double acceleration) {
		//based on the formula in StratOps
		return Math.sqrt((getDistanceToJumpPoint()*1000)/(9.8*acceleration))/43200;
	}

	/** @return the average distance to the system's jump point in km */
	public float getDistanceToJumpPoint() {
		// TODO: Orbital distance
		return star.getDistanceToJumpPoint();
	}

	/** @return the distance to another planet in light years (0 if both are in the same system) */
	public double getDistanceTo(Planet anotherPlanet) {
		return Math.sqrt(Math.pow(getX() - anotherPlanet.getX(), 2) + Math.pow(getY() - anotherPlanet.getY(), 2));
	}

	public String getDescription() {
		return desc;
	}

	public static int convertRatingToCode(String rating) {
		if(rating.equalsIgnoreCase("A")) {
			return EquipmentType.RATING_A;
		}
		else if(rating.equalsIgnoreCase("B")) {
			return EquipmentType.RATING_B;
		}
		else if(rating.equalsIgnoreCase("C")) {
			return EquipmentType.RATING_C;
		}
		else if(rating.equalsIgnoreCase("D")) {
			return EquipmentType.RATING_D;
		}
		else if(rating.equalsIgnoreCase("E")) {
			return EquipmentType.RATING_E;
		}
		else if(rating.equalsIgnoreCase("F")) {
			return EquipmentType.RATING_F;
		}
		return EquipmentType.RATING_C;
	}

	public static Planet getPlanetFromXML(Node wn) throws DOMException, ParseException {
		Planet retVal = new Planet();
		NodeList nl = wn.getChildNodes();

		for (int x=0; x<nl.getLength(); x++) {
			Node wn2 = nl.item(x);
			if (wn2.getNodeName().equalsIgnoreCase("name")) {
				retVal.name = wn2.getTextContent();
				retVal.shortName = retVal.name;
				if( null == retVal.id ) {
					retVal.id = retVal.name;
				}
				retVal.star.setName(retVal.name);
			} else if (wn2.getNodeName().equalsIgnoreCase("id")) {
				retVal.id = wn2.getTextContent();
			} else if (wn2.getNodeName().equalsIgnoreCase("xcood")) {
				retVal.star.setX(Double.parseDouble(wn2.getTextContent()));
			} else if (wn2.getNodeName().equalsIgnoreCase("ycood")) {
				retVal.star.setY(Double.parseDouble(wn2.getTextContent()));
			} else if (wn2.getNodeName().equalsIgnoreCase("faction")) {
				try {
					retVal.factionCodes = processFactionCodes(wn2.getTextContent());
				} catch (NoSuchFieldException e) {
					JOptionPane.showMessageDialog(null,
						    "Invalid faction code detected for planet "+retVal.getName(),
						    "Invalid Faction Code",
						    JOptionPane.ERROR_MESSAGE);
				}
			} else if (wn2.getNodeName().equalsIgnoreCase("pressure")) {
				retVal.pressure = Integer.parseInt(wn2.getTextContent());
			} else if (wn2.getNodeName().equalsIgnoreCase("gravity")) {
				retVal.gravity = Double.parseDouble(wn2.getTextContent());
			} else if (wn2.getNodeName().equalsIgnoreCase("sysPos")) {
				retVal.sysPos = Integer.parseInt(wn2.getTextContent());
			} else if (wn2.getNodeName().equalsIgnoreCase("nadirCharge")) {
				if (wn2.getTextContent().equalsIgnoreCase("true"))
					retVal.star.setNadirCharge(true);
				else
					retVal.star.setNadirCharge(false);
			} else if (wn2.getNodeName().equalsIgnoreCase("zenithCharge")) {
				if (wn2.getTextContent().equalsIgnoreCase("true"))
					retVal.star.setZenithCharge(true);
				else
					retVal.star.setZenithCharge(false);
			} else if (wn2.getNodeName().equalsIgnoreCase("lifeForm")) {
				retVal.lifeForm = Integer.parseInt(wn2.getTextContent());
			} else if (wn2.getNodeName().equalsIgnoreCase("climate")) {
				retVal.climate = Integer.parseInt(wn2.getTextContent());
			} else if (wn2.getNodeName().equalsIgnoreCase("percentWater")) {
				retVal.percentWater = Integer.parseInt(wn2.getTextContent());
			} else if (wn2.getNodeName().equalsIgnoreCase("temperature")) {
				retVal.temperature = Integer.parseInt(wn2.getTextContent());
			} else if (wn2.getNodeName().equalsIgnoreCase("spectralClass")) {
				retVal.star.setSpectralClass(Star.getSpectralClassFrom(wn2.getTextContent()));
			} else if (wn2.getNodeName().equalsIgnoreCase("subtype")) {
				retVal.star.setSubtype(Integer.parseInt(wn2.getTextContent()));
			} else if (wn2.getNodeName().equalsIgnoreCase("luminosity")) {
				retVal.star.setLuminosity(wn2.getTextContent());
			} else if (wn2.getNodeName().equalsIgnoreCase("factionChange")) {
				processFactionChange(retVal, wn2);
			} else if (wn2.getNodeName().equalsIgnoreCase("satellite")) {
				retVal.satellites.add(wn2.getTextContent());
			} else if (wn2.getNodeName().equalsIgnoreCase("landMass")) {
				retVal.landMasses.add(wn2.getTextContent());
			} else if (wn2.getNodeName().equalsIgnoreCase("hpg")) {
				retVal.hpg = convertRatingToCode(wn2.getTextContent());
			} else if (wn2.getNodeName().equalsIgnoreCase("socioIndustrial")) {
				String[] socio = wn2.getTextContent().split("-");
				if(socio.length >= 5) {
					retVal.tech = convertRatingToCode(wn2.getTextContent().split("-")[0]);
					retVal.industry = convertRatingToCode(wn2.getTextContent().split("-")[1]);
					retVal.rawMaterials = convertRatingToCode(wn2.getTextContent().split("-")[2]);
					retVal.output = convertRatingToCode(wn2.getTextContent().split("-")[3]);
					retVal.agriculture = convertRatingToCode(wn2.getTextContent().split("-")[4]);
				}
			} else if (wn2.getNodeName().equalsIgnoreCase("desc")) {
				retVal.desc = wn2.getTextContent();
			} else if (wn2.getNodeName().equalsIgnoreCase("orbitradius")) {
				retVal.orbitSemimajorAxis = Double.parseDouble(wn2.getTextContent());
			}
		}
		retVal.star.setPlanet(retVal.sysPos, retVal);
		if( retVal.orbitSemimajorAxis <= 0 ) {
			// Set a default value in the middle of the habitable zone for the star
			retVal.orbitSemimajorAxis = retVal.star.getAverageLifeZone();
		}
		return retVal;
	}

	private static void processFactionChange(Planet retVal, Node wni) throws DOMException, ParseException {
		NodeList nl = wni.getChildNodes();

		Date date = null;
		ArrayList<String> factions = new ArrayList<String>();
		// Okay, lets iterate through the children, eh?
		for (int x = 0; x < nl.getLength(); x++) {
			Node wn = nl.item(x);
			int xc = wn.getNodeType();

			// If it's not an element, again, we're ignoring it.
			if (xc == Node.ELEMENT_NODE) {
				String xn = wn.getNodeName();

				if (xn.equalsIgnoreCase("date")) {
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
					date = df.parse(wn.getTextContent().trim());
				} else if (xn.equalsIgnoreCase("faction")) {
					try {
						factions = processFactionCodes(wn.getTextContent().trim());
					} catch (NoSuchFieldException e) {
						JOptionPane.showMessageDialog(null,
							    "Invalid faction code detected for planet "+retVal.getName(),
							    "Invalid Faction Code",
							    JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		}
		if(null != date && factions.size() > 0) {
			retVal.factionHistory.put(date, factions);
		}
	}

	@Override
	public boolean equals(Object object) {
		if(object instanceof Planet) {
			Planet planet = (Planet)object;
			if(planet.getName().equalsIgnoreCase(name)
					&& planet.getX() == getX()
					&& planet.getY() == getY()
					&& planet.getSystemPosition() == sysPos) {
				return true;
			}
		}
		return false;
	}

	private static ArrayList<String> processFactionCodes(String codeList) throws NoSuchFieldException {
		ArrayList<String> factions = new ArrayList<String>();
		String[] codes = codeList.split(",");
		for(String code : codes) {
			if(null == Faction.getFaction(code)) {
				throw new NoSuchFieldException();
			}
			factions.add(code);
		}
		return factions;
	}

	public static int generateStarType() {
		switch (Compute.d6(2)) {
			case 2:
				return Star.SPECTRAL_F;
			case 3:
				return Star.SPECTRAL_M;
			case 4:
				return Star.SPECTRAL_G;
			case 5:
				return Star.SPECTRAL_K;
			case 6:
			case 7:
			case 8:
			case 9:
			case 10:
			case 11:
				return Star.SPECTRAL_M;
			case 12:
				switch (Compute.d6(2)) {
					case 2:
					case 3:
						return Star.SPECTRAL_B;
					case 4:
					case 5:
					case 6:
					case 7:
					case 8:
					case 9:
					case 10:
						return Star.SPECTRAL_A;
					case 11:
						return Star.SPECTRAL_B;
					case 12:
						return Star.SPECTRAL_F;
					default:
						return Star.SPECTRAL_A;
				}
			default:
				return Star.SPECTRAL_M;
		}
	}

	public static int generateSubtype() {
		switch (Compute.d6()) {
			case 1:
				return 1;
			case 2:
				return 2;
			case 3:
				return 4;
			case 4:
				return 6;
			case 5:
				return 8;
			case 6:
				return 0;
			default:
				return 1;
		}
	}

	public static int calculateNumberOfSlots() {
		return Compute.d6(2) + 3;
	}

	public static HashMap<String, Integer> generateSlotType(boolean outOfZone) {
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		int roll = Compute.d6(2);
		if (outOfZone) {
			roll += 2;
		}

		switch (roll) {
			case 2:
			case 3:
				map.put("type", TYPE_EMPTY);
				map.put("base_dm", 0);
				map.put("dm_mod", 0);
				map.put("density", 0);
				map.put("day_length", 0);
				break;
			case 4:
				map.put("type", TYPE_ASTEROID);
				map.put("base_dm", 0);
				map.put("dm_mod", 0);
				map.put("density", 0);
				map.put("day_length", 0);
				break;
			case 5:
				map.put("type", TYPE_DWARF);
				map.put("base_dm", 400);
				map.put("dm_mod", (100 * Compute.d6(3)));
				map.put("density", Compute.d6());
				map.put("day_length", (Compute.d6(3) + 12));
				break;
			case 6:
			case 7:
				map.put("type", TYPE_TERRESTRIAL);
				map.put("base_dm", 2500);
				map.put("dm_mod", (1000 * Compute.d6(2)));
				map.put("density", (int)Math.pow(2.5 + Compute.d6(), 0.75));
				map.put("day_length", (Compute.d6(3) + 12));
				break;
			case 8:
				map.put("type", TYPE_GIANT);
				map.put("base_dm", 12500);
				map.put("dm_mod", (1000 * Compute.d6(2)));
				map.put("density", Compute.d6()+2);
				map.put("day_length", (Compute.d6(4)));
				break;
			case 9:
			case 10:
				map.put("type", TYPE_GAS_GIANT);
				map.put("base_dm", 50000);
				map.put("dm_mod", (10000 * Compute.d6(2)));
				map.put("density", (int) (Compute.d6(2) / 10 + 0.5));
				map.put("day_length", (Compute.d6(4)));
				break;
			case 11:
			case 12:
				map.put("type", TYPE_ICE_GIANT);
				map.put("base_dm", 25000);
				map.put("dm_mod", (5000 * Compute.d6()));
				map.put("density", (int) (Compute.d6(2) / 10 + 1));
				map.put("day_length", (Compute.d6(4)));
				break;
			default:
				map.put("type", TYPE_EMPTY);
				map.put("base_dm", 0);
				map.put("dm_mod", 0);
				map.put("density", 0);
				map.put("day_length", 0);
				break;
		}

		return map;
	}

	public static HashMap<String, Integer> genMoons(int type) {
	    HashMap<String, Integer> map = new HashMap<String, Integer>();

	    // Fill the map with default 0s
        map.put("giant", 0);
        map.put("large", 0);
        map.put("medium", 0);
        map.put("small", 0);
        map.put("rings", 0);

        int roll = Compute.d6(1);
	    switch (type) {
	        case TYPE_DWARF:
	            switch (roll) {
	                case 1:
	                case 2:
                        map.put("medium", Math.max(0, Compute.d6()-5));
                        map.put("small", Math.max(0, Compute.d6()-2));
	                    break;
	                case 3:
	                case 4:
                        map.put("small", Math.max(0, Compute.d6()-2));
	                    break;
	            }
	            break;
	        case TYPE_TERRESTRIAL:
                switch (roll) {
                    case 1:
                    case 2:
                        map.put("large", Math.max(0, Compute.d6()-5));
                        break;
                    case 3:
                    case 4:
                        map.put("medium", Math.max(0, Compute.d6()-3));
                        map.put("small", Math.max(0, Compute.d6()-3));
                        break;
                    case 5:
                    case 6:
                        map.put("small", Math.max(0, Compute.d6(2)-4));
                        map.put("rings", Math.max(0, Compute.d6()-5));
                        break;
                }
                break;
	        case TYPE_GIANT:
                switch (roll) {
                    case 1:
                    case 2:
                        map.put("giant", Math.max(0, Compute.d6()-5));
                        break;
                    case 3:
                    case 4:
                        map.put("large", Math.max(0, Compute.d6()-4));
                        map.put("medium", Math.max(0, Compute.d6()-3));
                        map.put("small", Math.max(0, Compute.d6()-2));
                        break;
                    case 5:
                    case 6:
                        map.put("medium", Math.max(0, Compute.d6()-3));
                        map.put("small", Math.max(0, Compute.d6(2)));
                        map.put("rings", Math.max(0, Compute.d6()-4));
                        break;
                }
                break;
	        case TYPE_GAS_GIANT:
                switch (roll) {
                    case 1:
                    case 2:
                        map.put("giant", Math.max(0, Compute.d6()-4));
                        map.put("large", Math.max(0, Compute.d6()-1));
                        map.put("medium", Math.max(0, Compute.d6()-2));
                        map.put("small", Math.max(0, Compute.d6(5)));
                        map.put("rings", Math.max(0, Compute.d6()-3));
                        break;
                    case 3:
                    case 4:
                        map.put("large", Math.max(0, Compute.d6()-3));
                        map.put("medium", Math.max(0, Compute.d6()-2));
                        map.put("small", Math.max(0, Compute.d6(5)));
                        map.put("rings", Math.max(0, Compute.d6()-2));
                        break;
                    case 5:
                    case 6:
                        map.put("large", Math.max(0, Compute.d6()-4));
                        map.put("medium", Math.max(0, Compute.d6()-3));
                        map.put("small", Math.max(0, Compute.d6(5)));
                        map.put("rings", Math.max(0, Compute.d6()-2));
                        break;
                }
                break;
	        case TYPE_ICE_GIANT:
                switch (roll) {
                    case 1:
                    case 2:
                        map.put("giant", Math.max(0, Compute.d6()-4));
                        map.put("large", Math.max(0, Compute.d6()-3));
                        map.put("small", Math.max(0, Compute.d6(2)));
                        break;
                    case 3:
                    case 4:
                        map.put("large", Math.max(0, Compute.d6()-3));
                        map.put("medium", Math.max(0, Compute.d6()-2));
                        map.put("small", Math.max(0, Compute.d6(2)));
                        map.put("rings", Math.max(0, Compute.d6()-3));
                        break;
                    case 5:
                    case 6:
                        map.put("large", Math.max(0, Compute.d6()-4));
                        map.put("medium", Math.max(0, Compute.d6()-3));
                        map.put("small", Math.max(0, Compute.d6(2)));
                        map.put("rings", Math.max(0, Compute.d6()-3));
                        break;
                }
                break;
	        default:
	            break;
	    }
	    return map;
	}


}