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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import megamek.common.EquipmentType;
import megamek.common.PlanetaryConditions;
import mekhq.Utilities;
import mekhq.adapters.ClimateAdapter;
import mekhq.adapters.DateAdapter;
import mekhq.adapters.FactionDataAdapter;
import mekhq.adapters.HPGRatingAdapter;
import mekhq.adapters.LifeFormAdapter;
import mekhq.adapters.SocioIndustrialDataAdapter;
import mekhq.campaign.universe.PlanetXMLData.FactionChange;


/**
 * This is the start of a planet object that will keep lots of information about
 * planets that can be displayed on the interstellar map.
 *
 *
 * @author Jay Lawson <jaylawson39 at yahoo.com>
 */
public class Planet implements Serializable {
	private static final long serialVersionUID = -8699502165157515099L;

	
	/**
	 * This is the base faction which the program will fall back on if
	 * no better faction is found in the faction history given the date
	 * <p>
	 * map of [faction code: weight]
	 */
	private Map<String, Integer> factionCodes;
	private ArrayList<String> garrisonUnits;
	
	private String id;
	private String name;
	private String shortName;
	private String starId;
	private Integer sysPos;

	private Integer pressure;
	private Double gravity;
	//fluff
	private LifeForm lifeForm;
	private Climate climate;
	private Integer percentWater;
	private Integer temperature;
	private Integer hpg;
	private String desc;
	
	// Orbital data
	/** Semimajor axis (average distance to parent star), in AU */
	private Double orbitSemimajorAxis = 0.0;

	//socioindustrial levels
	private Planet.SocioIndustrialData socioIndustrial;

	//keep some string information in arraylists
	private List<String> satellites;
	private List<String> landMasses;

	/**
	 * a hash to keep track of dynamic planet changes
	 * <p>
	 * sorted map of [date of change: change information]
	 */
	TreeMap<Date, PlanetaryEvent> events;

	//a hash to keep track of dynamic garrison changes
	TreeMap<Date,ArrayList<String>> garrisonHistory;

	public Planet() {
		this.factionCodes = new HashMap<String, Integer>();
		this.garrisonUnits = new ArrayList<String>();

		this.satellites = new ArrayList<String>();
		this.landMasses = new ArrayList<String>();
	}

	// Constant (for the scope of the game) data
	
	public String getStarId() {
		return starId;
	}
	
	public void setStarId(String starId) {
		this.starId = starId;
	}
	
	public Star getStar() {
		return Planets.getInstance().getStarById(starId);
	}
	
	public Integer getSystemPosition() {
		return sysPos;
	}

	public String getId() {
		return id;
	}

	public Double getGravity() {
		return gravity;
	}

	public Integer getPressure() {
		return pressure;
	}

	public String getPressureName() {
		return PlanetaryConditions.getAtmosphereDisplayableName(pressure);
	}

	public Double getOrbitSemimajorAxis() {
		return orbitSemimajorAxis;
	}

	public List<String> getSatellites() {
		return null != satellites ? new ArrayList<String>(satellites) : null;
	}

	public List<String> getLandMasses() {
		return null != landMasses ? new ArrayList<String>(landMasses) : null;
	}

	// Date-dependant data
	
	private PlanetaryEvent getOrCreateEvent(Date when) {
		if( null == when ) {
			return null;
		}
		if( null == events ) {
			events = new TreeMap<Date, PlanetaryEvent>();
		}
		PlanetaryEvent event = events.get(when);
		if( null == event ) {
			event = new PlanetaryEvent();
			event.date = when;
			events.put(when, event);
		}
		return event;
	}
	
	public List<PlanetaryEvent> getEvents() {
		if( null == events ) {
			return null;
		}
		return new ArrayList<PlanetaryEvent>(events.values());
	}
	
	/** @return events for this year. Never returns <i>null</i>. */
	@SuppressWarnings("deprecation")
	public List<PlanetaryEvent> getEvents(int year) {
		if( null == events ) {
			return Collections.<PlanetaryEvent>emptyList();
		}
		List<PlanetaryEvent> result = new ArrayList<PlanetaryEvent>();
		for( Date date : events.navigableKeySet() ) {
			if( date.getYear() + 1900 > year ) {
				break;
			}
			if( date.getYear() + 1900 == year ) {
				result.add(events.get(date));
			}
		}
		return result;
	}

	public String getName(Date when) {
		if( null == when || null == events ) {
			return name;
		}
		String result = name;
		for( Date date : events.navigableKeySet() ) {
			if( date.after(when) ) {
				break;
			}
			if( null != events.get(date).name ) {
				result = events.get(date).name;
			}
		}
		return result;
	}

	public String getShortName(Date when) {
		if( null == when || null == events ) {
			return shortName;
		}
		String result = shortName;
		for( Date date : events.navigableKeySet() ) {
			if( date.after(when) ) {
				break;
			}
			if( null != events.get(date).shortName ) {
				result = events.get(date).shortName;
			}
		}
		return result;
	}

	public SocioIndustrialData getSocioIndustrial(Date when) {
		if( null == when || null == events ) {
			return null != socioIndustrial ? socioIndustrial : SocioIndustrialData.NONE;
		}
		SocioIndustrialData result = null != socioIndustrial ? socioIndustrial : SocioIndustrialData.NONE;
		for( Date date : events.navigableKeySet() ) {
			if( date.after(when) ) {
				break;
			}
			if( null != events.get(date).socioIndustrial ) {
				result = events.get(date).socioIndustrial;
			}
		}
		return result;
	}

	public String getSocioIndustrialLevel(Date when) {
		return getSocioIndustrial(when).toString();
	}

	public Integer getHPG(Date when) {
		if( null == when || null == events ) {
			return null != hpg ? hpg : EquipmentType.RATING_X;
		}
		Integer result = null != hpg ? hpg : EquipmentType.RATING_X;
		for( Date date : events.navigableKeySet() ) {
			if( date.after(when) ) {
				break;
			}
			if( null != events.get(date).hpg ) {
				result = events.get(date).hpg;
			}
		}
		return result;
	}


	public String getHPGClass(Date when) {
		return EquipmentType.getRatingName(getHPG(when));
	}

	public LifeForm getLifeForm(Date when) {
		if( null == when || null == events ) {
			return null != lifeForm ? lifeForm : LifeForm.NONE;
		}
		LifeForm result = null != lifeForm ? lifeForm : LifeForm.NONE;
		for( Date date : events.navigableKeySet() ) {
			if( date.after(when) ) {
				break;
			}
			if( null != events.get(date).lifeForm ) {
				result = events.get(date).lifeForm;
			}
		}
		return result;
	}

	public String getLifeFormName(Date when) {
		return getLifeForm(when).name;
	}

	public Climate getClimate(Date when) {
		if( null == when || null == events ) {
			return climate;
		}
		Climate result = climate;
		for( Date date : events.navigableKeySet() ) {
			if( date.after(when) ) {
				break;
			}
			if( null != events.get(date).climate ) {
				result = events.get(date).climate;
			}
		}
		return result;
	}


	public String getClimateName(Date when) {
		Climate c = getClimate(when);
		return null != c ? c.climateName : null;
	}

	public Integer getPercentWater(Date when) {
		if( null == when || null == events ) {
			return percentWater;
		}
		Integer result = percentWater;
		for( Date date : events.navigableKeySet() ) {
			if( date.after(when) ) {
				break;
			}
			if( null != events.get(date).percentWater ) {
				result = events.get(date).percentWater;
			}
		}
		return result;
	}

	public Integer getTemperature(Date when) {
		if( null == when || null == events ) {
			return temperature;
		}
		Integer result = temperature;
		for( Date date : events.navigableKeySet() ) {
			if( date.after(when) ) {
				break;
			}
			if( null != events.get(date).temperature ) {
				result = events.get(date).temperature;
			}
		}
		return result;
	}

	public List<String> getGarrisonUnits() {
		return garrisonUnits;
	}

	public Map<String, Integer> getFactions(Date when) {
		if( null == factionCodes ) {
			return null;
		}
		Map<String, Integer> result = factionCodes;
		for( Date date : events.navigableKeySet() ) {
			if( date.after(when) ) {
				break;
			}
			if( null != events.get(date).faction ) {
				result = events.get(date).faction;
			}
		}
		return result;
	}

	public Set<String> getBaseFactionCodes() {
		return Collections.unmodifiableSet(factionCodes.keySet());
	}

	public Set<Faction> getBaseFactions() {
		return getFactionsFrom(factionCodes.keySet());
	}

	private static Set<Faction> getFactionsFrom(Set<String> codes) {
		Set<Faction> factions = new HashSet<Faction>(codes.size());
		for(String code : codes) {
			factions.add(Faction.getFaction(code));
		}
		return factions;
	}

	public Set<Faction> getCurrentFactions(Date when) {
		Map<String, Integer> currentFactions = getFactions(when);
		return null != currentFactions ? getFactionsFrom(currentFactions.keySet()) : null;
	}

	public String getShortDesc(Date when) {
		return getShortName(when) + " (" + getFactionDesc(when) + ")";
	}

	public String getFactionDesc(Date when) {
		@SuppressWarnings("deprecation")
		int era = Era.getEra(when.getYear() + 1900);
		Set<Faction> factions = getCurrentFactions(when);
		if( null == factions ) {
			return "-";
		}
		List<String> factionNames = new ArrayList<String>(factions.size());
		for( Faction f : factions ) {
			factionNames.add(f.getFullName(era));
		}
		Collections.sort(factionNames);
		return Utilities.combineString(factionNames, "/");
	}

	/** @return a point representing a not exactly defined point on the surface of this planet */
	public SpaceLocation getPointOnSurface() {
		return new OrbitalPoint(getStar(), orbitSemimajorAxis * Utilities.AU);
	}
	
	/** @return all the available in-system space locations */
	public Set<SpaceLocation> getAllLocations() {
		return Collections.singleton(getPointOnSurface());
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

	/** @return the average travel time from low orbit to the jump point at 1g, in Terran days */
	public double getTimeToJumpPoint(double acceleration) {
		//based on the formula in StratOps
		return Math.sqrt((getDistanceToJumpPoint()*1000)/(9.8*acceleration))/43200;
	}

	/** @return the average distance to the system's jump point in km */
	public double getDistanceToJumpPoint() {
		return Math.sqrt(Math.pow(orbitSemimajorAxis * Utilities.AU, 2) + Math.pow(getStar().getDistanceToJumpPoint(), 2));
	}

	/** @return the distance to another planet in light years (0 if both are in the same system) */
	public double getDistanceTo(Planet anotherPlanet) {
		return Math.sqrt(Math.pow(getStar().getX() - anotherPlanet.getStar().getX(), 2)
				+ Math.pow(getStar().getY() - anotherPlanet.getStar().getY(), 2));
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

	/**
	 * Copy all but id from the other planet. Update event list. Events with the
	 * same date as others already in the list get overwritten, others added.
	 * To effectively delete an event, simply create a new one with <i>just</i> the date.
	 */
	public void copyDataFrom(Planet other) {
		if( null != other ) {
			name = null != other.name ? other.name : name;
			shortName = null != other.shortName ? other.shortName : shortName;
			climate = null != other.climate ? other.climate : climate;
			desc = null != other.desc ? other.desc : desc;
			factionCodes = null != other.factionCodes ? other.factionCodes : factionCodes;
			gravity = null != other.gravity ? other.gravity : gravity;
			hpg = null != other.hpg ? other.hpg : hpg;
			landMasses = null != other.landMasses ? other.landMasses : landMasses;
			lifeForm = null != other.lifeForm ? other.lifeForm : lifeForm;
			orbitSemimajorAxis = null != other.orbitSemimajorAxis ? other.orbitSemimajorAxis : orbitSemimajorAxis;
			percentWater = null != other.percentWater ? other.percentWater : percentWater;
			pressure = null != other.pressure ? other.pressure : pressure;
			satellites = null != other.satellites ? other.satellites : satellites;
			sysPos = null != other.sysPos ? other.sysPos : sysPos;
			temperature = null != other.temperature ? other.temperature : temperature;
			socioIndustrial = null != other.socioIndustrial ? other.socioIndustrial : socioIndustrial;
			// Merge (not replace!) events
			if( null != other.events ) {
				for( PlanetaryEvent event : other.getEvents() ) {
					if( null != event && null != event.date ) {
						PlanetaryEvent myEvent = getOrCreateEvent(event.date);
						myEvent.climate = null != event.climate ? event.climate : myEvent.climate;
						myEvent.faction = null != event.faction ? event.faction : myEvent.faction;
						myEvent.hpg = null != event.hpg ? event.hpg : myEvent.hpg;
						myEvent.lifeForm = null != event.lifeForm ? event.lifeForm : myEvent.lifeForm;
						myEvent.message = null != event.message ? event.message : myEvent.message;
						myEvent.name = null != event.name ? event.name : myEvent.name;
						myEvent.percentWater = null != event.percentWater ? event.percentWater : myEvent.percentWater;
						myEvent.shortName = null != event.shortName ? event.shortName : myEvent.shortName;
						myEvent.socioIndustrial = null != event.socioIndustrial ? event.socioIndustrial : myEvent.socioIndustrial;
						myEvent.temperature = null != event.temperature ? event.temperature : myEvent.temperature;
					}
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	public static Planet getPlanetFromXMLData(PlanetXMLData data) {
		Planet result = new Planet();
		result.name = data.name;
		result.shortName = data.shortName;
		result.id = null != data.id ? data.id : data.name;
		result.starId = data.starId;
		result.climate = data.climate;
		result.desc = data.desc;
		result.factionCodes = data.factions;
		result.gravity = data.gravity;
		result.hpg = data.hpg;
		result.landMasses = data.landMasses;
		result.lifeForm = data.lifeForm;
		result.orbitSemimajorAxis = data.orbitSemimajorAxis;
		result.percentWater = data.percentWater;
		result.pressure = data.pressure;
		result.satellites = data.satellites;
		result.sysPos = data.sysPos;
		result.temperature = data.temperature;
		result.socioIndustrial = data.socioIndustrial;
		if( null != data.events ) {
			result.events = new TreeMap<Date, PlanetaryEvent>();
			for( PlanetaryEvent event : data.events ) {
				if( null != event && null != event.date ) {
					result.events.put(event.date, event);
				}
			}
		}
		// Merge faction change events into the event data
		if( null != data.factionChanges ) {
			for( FactionChange change : data.factionChanges ) {
				if( null != change && null != change.date ) {
					PlanetaryEvent event = result.getOrCreateEvent(change.date);
					event.faction = change.faction;
				}
			}
		}
		return result;
	}
	
	@Override
	public int hashCode() {
		return 37 + ((id == null) ? 0 : id.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if( this == obj ) {
			return true;
		}
		if( obj instanceof Planet ) {
			Planet other = (Planet)obj;
			if( null == id ) {
				return null == other.id;
			}
			return id.equals(other.id);
		}
		return false;
	}

	/* Unused anyway ...
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
	*/
	
	public static final class SocioIndustrialData {
		public static final SocioIndustrialData NONE = new SocioIndustrialData();
		static {
			NONE.tech = EquipmentType.RATING_X;
			NONE.industry = EquipmentType.RATING_X;
			NONE.rawMaterials = EquipmentType.RATING_X;
			NONE.output = EquipmentType.RATING_X;
			NONE.agriculture = EquipmentType.RATING_X;
		}
		
		public int tech;
		public int industry;
		public int rawMaterials;
		public int output;
		public int agriculture;
		
		@Override
		public String toString() {
			 return EquipmentType.getRatingName(tech)
				+ "-" + EquipmentType.getRatingName(industry)
				+ "-" + EquipmentType.getRatingName(rawMaterials)
				+ "-" + EquipmentType.getRatingName(output) + "-"
				+ EquipmentType.getRatingName(agriculture);		}
	}

	/** A class representing some event, possibly changing planetary information */
	public static final class PlanetaryEvent {
		@XmlJavaTypeAdapter(DateAdapter.class)
		public Date date;
		public String message;
	    public String name;
		public String shortName;
		@XmlJavaTypeAdapter(FactionDataAdapter.class)
	    public Map<String, Integer> faction;
	    @XmlJavaTypeAdapter(LifeFormAdapter.class)
	    public LifeForm lifeForm;
	    @XmlJavaTypeAdapter(ClimateAdapter.class)
	    public Climate climate;
	    public Integer percentWater;
	    public Integer temperature;
	    @XmlJavaTypeAdapter(SocioIndustrialDataAdapter.class)
	    public SocioIndustrialData socioIndustrial;
	    @XmlJavaTypeAdapter(HPGRatingAdapter.class)
	    public Integer hpg;
	}
}