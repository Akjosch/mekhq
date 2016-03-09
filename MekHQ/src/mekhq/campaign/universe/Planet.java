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

import javax.xml.bind.annotation.XmlElement;
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
    /** Pressure in Earth standard */
	private Double pressureAtm;
    /** Atmospheric mass compared to Earth's 28.9645 kg/mol */
	private Double atmMass;
    /** Atmospheric description */
	private String atmosphere;
	private Double albedo;
	private Double greenhouseEffect;
    @XmlElement(name = "volcamisn")
    private Integer volcanicActivity;
    @XmlElement(name = "tectonics")
    private Integer tectonicActivity;
    private Integer habitability;
	private Double dayLength;
	private Integer hpg;
	private String desc;
	
	// Orbital data
	/** Semimajor axis (average distance to parent star), in AU */
	private Double orbitSemimajorAxis = 0.0;
	private Double orbitEccentricity;
	private Double orbitInclination;

	//socioindustrial levels
	private Planet.SocioIndustrialData socioIndustrial;

	//keep some string information in lists
	private List<String> satellites;
	private List<String> landMasses;

	private List<PointOfInterest> pois;
	
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
	
	public String getSystemPositionText() {
		return null != sysPos ? sysPos.toString() : "?";
	}

	public String getId() {
		return id;
	}

	public Double getGravity() {
		return gravity;
	}
	
	public String getGravityText() {
		return null != gravity ? gravity.toString() + "g" : "fit for humans";
	}

	public Double getOrbitSemimajorAxis() {
		return orbitSemimajorAxis;
	}
	
	/** @return orbital semimajor axis in km; in the middle of the star's life zone if not set */
	public double getOrbitSemimajorAxisKm() {
		return null != orbitSemimajorAxis ? orbitSemimajorAxis * Utilities.AU : getStar().getAverageLifeZone();
	}

	public List<String> getSatellites() {
		return null != satellites ? new ArrayList<String>(satellites) : null;
	}

	public List<String> getLandMasses() {
		return null != landMasses ? new ArrayList<String>(landMasses) : null;
	}

	public Integer getVolcanicActivity() {
		return volcanicActivity;
	}

	public Integer getTectonicActivity() {
		return tectonicActivity;
	}

	public Double getDayLength() {
		return dayLength;
	}

	public Double getOrbitEccentricity() {
		return orbitEccentricity;
	}

	public Double getOrbitInclination() {
		return orbitInclination;
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
	
	protected <T> T getEventData(Date when, T defaultValue, EventGetter<T> getter) {
		if( null == when || null == events || null == getter ) {
			return defaultValue;
		}
		T result = defaultValue;
		for( Date date : events.navigableKeySet() ) {
			if( date.after(when) ) {
				break;
			}
			result = Utilities.nonNull(getter.get(events.get(date)), result);
		}
		return result;
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
		return getEventData(when, name, new EventGetter<String>() {
			@Override public String get(PlanetaryEvent e) { return e.name; }
		});
	}

	public String getShortName(Date when) {
		return getEventData(when, shortName, new EventGetter<String>() {
			@Override public String get(PlanetaryEvent e) { return e.shortName; }
		});
	}

	public SocioIndustrialData getSocioIndustrial(Date when) {
		return getEventData(when, socioIndustrial, new EventGetter<SocioIndustrialData>() {
			@Override public SocioIndustrialData get(PlanetaryEvent e) { return e.socioIndustrial; }
		});
	}

	public String getSocioIndustrialText(Date when) {
		SocioIndustrialData sid = getSocioIndustrial(when);
		return null != sid ? sid.toString() : "";
	}

	public Integer getHPG(Date when) {
		return getEventData(when, hpg, new EventGetter<Integer>() {
			@Override public Integer get(PlanetaryEvent e) { return e.hpg; }
		});
	}


	public String getHPGClass(Date when) {
		Integer currentHPG = getHPG(when);
		return null != currentHPG ? EquipmentType.getRatingName(currentHPG) : "";
	}

	public LifeForm getLifeForm(Date when) {
		return getEventData(when, null != lifeForm ? lifeForm : LifeForm.NONE, new EventGetter<LifeForm>() {
			@Override public LifeForm get(PlanetaryEvent e) { return e.lifeForm; }
		});
	}

	public String getLifeFormName(Date when) {
		return getLifeForm(when).name;
	}

	public Climate getClimate(Date when) {
		return getEventData(when, climate, new EventGetter<Climate>() {
			@Override public Climate get(PlanetaryEvent e) { return e.climate; }
		});
	}

	public String getClimateName(Date when) {
		Climate c = getClimate(when);
		return null != c ? c.climateName : null;
	}

	public Integer getPercentWater(Date when) {
		return getEventData(when, percentWater, new EventGetter<Integer>() {
			@Override public Integer get(PlanetaryEvent e) { return e.percentWater; }
		});
	}

	public Integer getTemperature(Date when) {
		return getEventData(when, temperature, new EventGetter<Integer>() {
			@Override public Integer get(PlanetaryEvent e) { return e.temperature; }
		});
	}

	public List<String> getGarrisonUnits() {
		return garrisonUnits;
	}

	public Integer getPressure(Date when) {
		return getEventData(when, pressure, new EventGetter<Integer>() {
			@Override public Integer get(PlanetaryEvent e) { return e.pressure; }
		});
	}
	
	public String getPressureName(Date when) {
		Integer currentPressure = getPressure(when);
		return null != currentPressure ? PlanetaryConditions.getAtmosphereDisplayableName(currentPressure) : "unknown";
	}

	public Double getPressureAtm(Date when) {
		return getEventData(when, pressureAtm, new EventGetter<Double>() {
			@Override public Double get(PlanetaryEvent e) { return e.pressureAtm; }
		});
	}

	public Double getAtmMass(Date when) {
		return getEventData(when, atmMass, new EventGetter<Double>() {
			@Override public Double get(PlanetaryEvent e) { return e.atmMass; }
		});
	}

	public String getAtmosphere(Date when) {
		return getEventData(when, atmosphere, new EventGetter<String>() {
			@Override public String get(PlanetaryEvent e) { return e.atmosphere; }
		});
	}

	public Double getAlbedo(Date when) {
		return getEventData(when, albedo, new EventGetter<Double>() {
			@Override public Double get(PlanetaryEvent e) { return e.albedo; }
		});
	}

	public Double getGreenhouseEffect(Date when) {
		return getEventData(when, greenhouseEffect, new EventGetter<Double>() {
			@Override public Double get(PlanetaryEvent e) { return e.greenhouseEffect; }
		});
	}

	public Integer getHabitability(Date when) {
		return getEventData(when, habitability, new EventGetter<Integer>() {
			@Override public Integer get(PlanetaryEvent e) { return e.habitability; }
		});
	}

	public List<PointOfInterest> getPois(Date when) {
		return null != pois ? new ArrayList<PointOfInterest>(pois) : null; // TODO: include in events
	}
	
	/** @return ap of factions and their influences at a given date */
	public Map<String, Integer> getFactions(Date when) {
		return getEventData(when, factionCodes, new EventGetter< Map<String, Integer>>() {
			@Override public  Map<String, Integer> get(PlanetaryEvent e) { return e.faction; }
		});
	}

	private static Set<Faction> getFactionsFrom(Set<String> codes) {
		Set<Faction> factions = new HashSet<Faction>(codes.size());
		for(String code : codes) {
			factions.add(Faction.getFaction(code));
		}
		return factions;
	}

	/** @return set of factions at a given date */
	public Set<Faction> getFactionSet(Date when) {
		Map<String, Integer> currentFactions = getFactions(when);
		return null != currentFactions ? getFactionsFrom(currentFactions.keySet()) : null;
	}

	public String getShortDesc(Date when) {
		return getShortName(when) + " (" + getFactionDesc(when) + ")";
	}

	public String getFactionDesc(Date when) {
		@SuppressWarnings("deprecation")
		int era = Era.getEra(when.getYear() + 1900);
		Set<Faction> factions = getFactionSet(when);
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
		return new OrbitalPoint(getStar(), getOrbitSemimajorAxisKm());
	}
	
	/** @return all the available in-system space locations */
	public Set<SpaceLocation> getAllLocations() {
		return Collections.singleton(getPointOnSurface());
	}

	public String getSatelliteDescription() {
		if(null == satellites || satellites.isEmpty()) {
			return "0";
		}
		return satellites.size() + " (" + Utilities.combineString(satellites, ", ") + ")";
	}

	public String getLandMassDescription() {
		return null != landMasses ? Utilities.combineString(landMasses, ", ") : "";
	}

	/** @return the average travel time from low orbit to the jump point at 1g, in Terran days */
	public double getTimeToJumpPoint(double acceleration) {
		//based on the formula in StratOps
		return Math.sqrt((getDistanceToJumpPoint()*1000)/(9.8*acceleration))/43200;
	}

	/** @return the average distance to the system's jump point in km */
	public double getDistanceToJumpPoint() {
		return Math.sqrt(Math.pow(getOrbitSemimajorAxisKm(), 2) + Math.pow(getStar().getDistanceToJumpPoint(), 2));
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

	private void addPointOfInterest(PointOfInterest poi) {
		if( null == poi ) {
			return;
		}
		if( null == pois ) {
			pois = new ArrayList<PointOfInterest>();
		}
		pois.add(poi);
	}
	
	/**
	 * Copy all but id from the other planet. Update event list. Events with the
	 * same date as others already in the list get overwritten, others added.
	 * To effectively delete an event, simply create a new one with <i>just</i> the date.
	 */
	public void copyDataFrom(Planet other) {
		if( null != other ) {
			// We don't change the ID or StarID
			name = Utilities.nonNull(other.name, name);
			shortName = Utilities.nonNull(other.shortName, shortName);
			climate = Utilities.nonNull(other.climate, climate);
			desc = Utilities.nonNull(other.desc, desc);
			factionCodes = Utilities.nonNull(other.factionCodes, factionCodes);
			gravity = Utilities.nonNull(other.gravity, gravity);
			hpg = Utilities.nonNull(other.hpg, hpg);
			landMasses = Utilities.nonNull(other.landMasses, landMasses);
			lifeForm = Utilities.nonNull(other.lifeForm, lifeForm);
			orbitSemimajorAxis = Utilities.nonNull(other.orbitSemimajorAxis, orbitSemimajorAxis);
			orbitEccentricity = Utilities.nonNull(other.orbitEccentricity, orbitEccentricity);
			orbitInclination = Utilities.nonNull(other.orbitInclination, orbitInclination);
			percentWater = Utilities.nonNull(other.percentWater, percentWater);
			pressure = Utilities.nonNull(other.pressure, pressure);
			pressureAtm = Utilities.nonNull(other.pressureAtm, pressureAtm);
			pressureAtm = Utilities.nonNull(other.pressureAtm, pressureAtm);
			atmMass = Utilities.nonNull(other.atmMass, atmMass);
			atmosphere = Utilities.nonNull(other.atmosphere, atmosphere);
			albedo = Utilities.nonNull(other.albedo, albedo);
			greenhouseEffect = Utilities.nonNull(other.greenhouseEffect, greenhouseEffect);
			volcanicActivity = Utilities.nonNull(other.volcanicActivity, volcanicActivity);
			tectonicActivity = Utilities.nonNull(other.tectonicActivity, tectonicActivity);
			habitability = Utilities.nonNull(other.habitability, habitability);
			dayLength = Utilities.nonNull(other.dayLength, dayLength);
			satellites = Utilities.nonNull(other.satellites, satellites);
			sysPos = Utilities.nonNull(other.sysPos, sysPos);
			temperature = Utilities.nonNull(other.temperature, temperature);
			socioIndustrial = Utilities.nonNull(other.socioIndustrial, socioIndustrial);
			// Merge (not replace!) events
			if( null != other.events ) {
				for( PlanetaryEvent event : other.getEvents() ) {
					if( null != event && null != event.date ) {
						PlanetaryEvent myEvent = getOrCreateEvent(event.date);
						myEvent.climate = Utilities.nonNull(event.climate, myEvent.climate);
						myEvent.faction = Utilities.nonNull(event.faction, myEvent.faction);
						myEvent.hpg = Utilities.nonNull(event.hpg, myEvent.hpg);
						myEvent.lifeForm = Utilities.nonNull(event.lifeForm, myEvent.lifeForm);
						myEvent.message = Utilities.nonNull(event.message, myEvent.message);
						myEvent.name = Utilities.nonNull(event.name, myEvent.name);
						myEvent.percentWater = Utilities.nonNull(event.percentWater, myEvent.percentWater);
						myEvent.shortName = Utilities.nonNull(event.shortName, myEvent.shortName);
						myEvent.socioIndustrial = Utilities.nonNull(event.socioIndustrial, myEvent.socioIndustrial);
						myEvent.temperature = Utilities.nonNull(event.temperature, myEvent.temperature);
						myEvent.pressure = Utilities.nonNull(event.pressure, myEvent.pressure);
						myEvent.pressureAtm = Utilities.nonNull(event.pressureAtm, myEvent.pressureAtm);
						myEvent.atmMass = Utilities.nonNull(event.atmMass, myEvent.atmMass);
						myEvent.atmosphere = Utilities.nonNull(event.atmosphere, myEvent.atmosphere);
						myEvent.albedo = Utilities.nonNull(event.albedo, myEvent.albedo);
						myEvent.greenhouseEffect = Utilities.nonNull(event.greenhouseEffect, myEvent.greenhouseEffect);
						myEvent.habitability = Utilities.nonNull(event.habitability, myEvent.habitability);
					}
				}
			}
			// Merge points of interest
			if( null != other.pois ) {
				for( PointOfInterest poi : other.pois ) {
					addPointOfInterest(poi);
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	public static Planet getPlanetFromXMLData(PlanetXMLData data) {
		Planet result = new Planet();
		result.id = null != data.id ? data.id : data.name;
		result.name = data.name;
		result.shortName = data.shortName;
		result.starId = data.starId;
		result.climate = data.climate;
		result.desc = data.desc;
		result.factionCodes = data.factions;
		result.gravity = data.gravity;
		result.hpg = data.hpg;
		result.landMasses = data.landMasses;
		result.lifeForm = data.lifeForm;
		result.orbitSemimajorAxis = data.orbitSemimajorAxis;
		result.orbitEccentricity = data.orbitEccentricity;
		result.orbitInclination = data.orbitInclination;
		result.percentWater = data.percentWater;
		result.pressure = data.pressure;
		result.pressureAtm = data.pressureAtm;
		result.atmMass = data.atmMass;
		result.atmosphere = data.atmosphere;
		result.albedo = data.albedo;
		result.greenhouseEffect = data.greenhouseEffect;
		result.volcanicActivity = data.volcanicActivity;
		result.tectonicActivity = data.tectonicActivity;
		result.habitability = data.habitability;
		result.dayLength = data.dayLength;
		result.satellites = data.satellites;
		result.sysPos = data.sysPos;
		result.temperature = data.temperature;
		result.socioIndustrial = data.socioIndustrial;
		result.pois = data.pois;
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
	    public Integer pressure;
	    public Double pressureAtm;
	    public Double atmMass;
		public String atmosphere;
		public Double albedo;
		public Double greenhouseEffect;
		public Integer habitability;
	}
	
	/**
	 * Some point of interest on the planetary surface or very close to it (city, factory, garrison).
	 * <p>
	 * The hex position is according to the planetary map in IO Beta. If both it and lat/long values
	 * are provided, the hex value is overwritten.
	 * If one of lat or long is provided, the other has to be as well.
	 */
	public static final class PointOfInterest {
		public String id;
		public String name;
		public String type;
		public String hex;
		@XmlElement(name="lat")
		public Double latitude;
		@XmlElement(name="long")
		public Double longitude;
		/** Height above or below zero surface in km, defaults to 0. For example, Mt. Everest would have 8.848. */
		@XmlElement(name="alt")
		public Double altitude;
		public String desc;
	}
	
	// @FunctionalInterface in Java 8, or just use Function<PlanetaryEvent, T>
	private static interface EventGetter<T> {
		T get(PlanetaryEvent e);
	}
	
	/** BT planet types */
	public static enum PlanetaryType {
		SMALL_ASTEROID, MEDIUM_ASTEROID, DWARF_TERRESTIAL, TERRESTIAL, GIANT_TERRESTIAL, GAS_GIANT, ICE_GIANT;
	}
}