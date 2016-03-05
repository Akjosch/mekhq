package mekhq.campaign.universe;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import mekhq.MekHQ;
import mekhq.Utilities;
import mekhq.adapters.BooleanValueAdapter;
import mekhq.adapters.DateAdapter;

public class Star implements Serializable {
	private static final long serialVersionUID = -5089854102647097334L;
	
	public static final int SPECTRAL_O = 0;
	public static final int SPECTRAL_B = 1;
	public static final int SPECTRAL_A = 2;
	public static final int SPECTRAL_F = 3;
	public static final int SPECTRAL_G = 4;
	public static final int SPECTRAL_K = 5;
	public static final int SPECTRAL_M = 6;
	public static final int SPECTRAL_L = 7;
	public static final int SPECTRAL_T = 8;
	// Spectral class "D" (white dwarfs) are handled internally as spectral class "O", luminosity "VII"
	// TODO: Wolf-Rayet stars ("W"), protostars ("Y"), carbon stars ("C"), S-type stars ("S"), 
	
	public static final String LUM_0           = "0";
	public static final String LUM_IA          = "Ia";
	public static final String LUM_IAB         = "Iab";
	public static final String LUM_IB          = "Ib";
	public static final String LUM_I           = "I"; // Generic class, consisting of Ia, Iab and Ib
	public static final String LUM_II_EVOLVED  = "I/II";
	public static final String LUM_II          = "II";
	public static final String LUM_III_EVOLVED = "II/III";
	public static final String LUM_III         = "III";
	public static final String LUM_IV_EVOLVED  = "III/IV";
	public static final String LUM_IV          = "IV";
	public static final String LUM_V_EVOLVED   = "IV/V";
	public static final String LUM_V           = "V";
	public static final String LUM_VI          = "VI"; // typically used as a prefix "sd", not as a suffix
	public static final String LUM_VI_PLUS     = "VI+"; // typically used as a prefix "esd", not as a suffix
	public static final String LUM_VII         = "VII"; // always used as class designation "D", never as a suffix
	
	private Double x;
	private Double y;

	private String id;
	private String name;
	private String shortName;
	
	//star type
	private Integer spectralClass;
	private Double subtype;
	private String luminosity;
	private String spectralType;
	
	// Amount of planets.
	private Integer numPlanets;
	// Amount of minor planets, asteroids and the like
	private Integer numMinorPlanets;
	// planets - list of planets in a given orbit; can (and often is) partially empty
	// This list is by the planet's ID, not instance, to help the GC and not create circular references
	private List<String> planetOrbits = new ArrayList<String>();
	// planets - all the planets orbiting around this star, even if they have no orbit set
	private Set<String> planets = new HashSet<String>();
	private String defaultPlanetId;
	
	private Boolean nadirCharge;
	private Boolean zenithCharge;
	
	/**
	 * a hash to keep track of dynamic stellar system changes
	 * <p>
	 * sorted map of [date of change: change information]
	 */
	private TreeMap<Date, StellarEvent> events;

	// Fluff
	private Double mass; // mass in solar masses (1.98855e30 kg)
	private Double lum; // luminosity in solar luminosity (3.846e26 W) 
	private Double temperature; // effective temperature in K
	private Double radius; // radius in solar radii (695700 km)

	private String desc;
	
	// Constants
	
	public String getId() {
		return id;
	}
	
	public Double getX() {
		return x;
	}
	
	public Double getY() {
		return y;
	}

	public Integer getSpectralClass() {
	    return spectralClass;
	}

	public Double getSubtype() {
	    return subtype;
	}

	public String getLuminosity() {
		return luminosity;
	}
	
	public String getDescription() {
		return desc;
	}
	
	public int getNumPlanets() {
		return Math.max(null != numPlanets ? numPlanets.intValue() : 0, planetOrbits.size());
	}

	public int getNumMinorPlanets() {
		return null != numMinorPlanets ? numMinorPlanets.intValue() : 0;
	}

	public Double getMass() {
		return mass;
	}
	
	public Double getMassKg() {
		return null != mass ? mass.doubleValue() * Utilities.SOLAR_MASS : 0.0;
	}

	public Double getLum() {
		return lum;
	}
	
	public Double getLumW() {
		return null != mass ? mass.doubleValue() * Utilities.SOLAR_LUM : 0.0;
	}

	public Double getTemperature() {
		return temperature;
	}

	public Double getRadius() {
		return radius;
	}

	public Double getRadiusKm() {
		return null != radius ? radius.doubleValue() * Utilities.SOLAR_RADIUS : 0.0;
	}
	
	// Date-dependant data
	
	private StellarEvent getOrCreateEvent(Date when) {
		if( null == when ) {
			return null;
		}
		if( null == events ) {
			events = new TreeMap<Date, StellarEvent>();
		}
		StellarEvent event = events.get(when);
		if( null == event ) {
			event = new StellarEvent();
			event.date = when;
			events.put(when, event);
		}
		return event;
	}
	
	public List<StellarEvent> getEvents() {
		if( null == events ) {
			return null;
		}
		return new ArrayList<StellarEvent>(events.values());
	}

	/** @return events for this year. Never returns <i>null</i>. */
	@SuppressWarnings("deprecation")
	public List<StellarEvent> getEvents(int year) {
		if( null == events ) {
			return Collections.<StellarEvent>emptyList();
		}
		List<StellarEvent> result = new ArrayList<StellarEvent>();
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

	public void setName(String name, Date when) {
		if( null == when ) {
			this.name = name;
			return;
		}
		StellarEvent event = getOrCreateEvent(when);
		event.name = name;
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
	
	/** @return short name if set, else full name, else "unnamed" */
	public String getPrintableName(Date when) {
		String result = getShortName(when);
		if( null == result ) {
			result = getName(when);
		}
		return null != result ? result : "unnamed";
	}
	
	public void setShortName(String shortName, Date when) {
		if( null == when ) {
			this.shortName = shortName;
			return;
		}
		StellarEvent event = getOrCreateEvent(when);
		event.shortName = shortName;
	}
	
	public Boolean isNadirCharge(Date when) {
		if( null == when || null == events ) {
			return nadirCharge;
		}
		Boolean result = nadirCharge;
		for( Date date : events.navigableKeySet() ) {
			if( date.after(when) ) {
				break;
			}
			if( null != events.get(date).nadirCharge ) {
				result = events.get(date).nadirCharge;
			}
		}
		return result;
	}

	public void setNadirCharge(boolean nadirCharge, Date when) {
		if( null == when ) {
			this.nadirCharge = nadirCharge;
			return;
		}
		StellarEvent event = getOrCreateEvent(when);
		event.nadirCharge = nadirCharge;
	}

	public boolean isZenithCharge(Date when) {
		if( null == when || null == events ) {
			return zenithCharge;
		}
		Boolean result = zenithCharge;
		for( Date date : events.navigableKeySet() ) {
			if( date.after(when) ) {
				break;
			}
			if( null != events.get(date).zenithCharge ) {
				result = events.get(date).zenithCharge;
			}
		}
		return result;
	}

	public void setZenithCharge(boolean zenithCharge, Date when) {
		if( null == when ) {
			this.zenithCharge = zenithCharge;
			return;
		}
		StellarEvent event = getOrCreateEvent(when);
		event.zenithCharge = zenithCharge;
	}

	public String getRechargeStations(Date when) {
		if(zenithCharge && nadirCharge) {
			return "Zenith, Nadir";
		} else if(zenithCharge) {
			return "Zenith";
		} else if(nadirCharge) {
			return "Nadir";
		} else {
			return "None";
		}
	}

	/** @return the factions in system; check individual planets for details */
	public Set<Faction> getCurrentFactions(Date when) {
		Set<Faction> factions = new HashSet<Faction>();
		for( Planet planet : getPlanets() ) {
			Set<Faction> planetaryFactions = planet.getCurrentFactions(when);
			if( null != planetaryFactions ) {
				factions.addAll(planet.getCurrentFactions(when));
			}
		}
		return factions;
	}

	public Map<String, Integer> getFactions(Date when) {
		Map<String, Integer> factions = new HashMap<String, Integer>();
		for( Planet planet : getPlanets() ) {
			for( Map.Entry<String, Integer> planetaryFactionEntry : planet.getFactions(when).entrySet() ) {
				Integer currentValue = factions.get(planetaryFactionEntry.getKey());
				if( null == currentValue ) {
					currentValue = 0;
				}
				currentValue += planetaryFactionEntry.getValue();
				factions.put(planetaryFactionEntry.getKey(), currentValue);
			}
		}
		return factions;
	}

	public String getFactionDesc(Date when) {
		@SuppressWarnings("deprecation")
		int era = Era.getEra(when.getYear() + 1900);
		Set<Faction> factions = getCurrentFactions(when);
		List<String> factionNames = new ArrayList<String>(factions.size());
		for( Faction f : factions ) {
			factionNames.add(f.getFullName(era));
		}
		Collections.sort(factionNames);
		return Utilities.combineString(factionNames, "/");
	}
	
	// Planet data
	
	/** Make sure our planets list is big enough */
	private void preparePlanetsList(int size) {
		while( size > planetOrbits.size() ) {
			planetOrbits.add(null);
		}
	}
	
	/** Make sure the default planet ID is set to something, unless we have no planets */
	private void ensureDefaultPlanetExists(String suggestedPlanetId) {
		if( null == defaultPlanetId ) {
			if( null != suggestedPlanetId ) {
				defaultPlanetId = suggestedPlanetId;
			} else {
				defaultPlanetId = Utilities.getRandomItem(planets);
			}
		}
	}
	
	/** @return planetary ID in the given orbit */
	public String getPlanetId(int orbit) {
		if( orbit <= 0 || orbit > planetOrbits.size() ) {
			return null;
		}
		return planetOrbits.get(orbit - 1);
	}
	
	/** @return the planet in the given orbit */
	public Planet getPlanet(int orbit) {
		String planetID = getPlanetId(orbit);
		return Planets.getInstance().getPlanetById(planetID);
	}
	
	/** Adds a planet without setting its orbit */
	public void addPlanet(Planet planet) {
		if( null != planet ) {
			planets.add(planet.getId());
			Integer planetPos = planet.getSystemPosition();
			if( null != planetPos && planetPos.intValue() > 0 ) {
				preparePlanetsList(planetPos.intValue());
				planetOrbits.set(planetPos.intValue() - 1, planet.getId());
			}
			ensureDefaultPlanetExists(planet.getId());
		}
	}
	
	public void removePlanet(Planet planet) {
		if( null != planet ) {
			removePlanet(planet.getId());
		}
	}
	
	public void removePlanet(String planetId) {
		if( null != planetId ) {
			planets.remove(planetId);
			for( int orbit = 0; orbit < planetOrbits.size(); ++ orbit ) {
				if( planetId.equals(planetOrbits.get(orbit)) ) {
					planetOrbits.set(orbit, null);
				}
			}
			if( defaultPlanetId.equals(planetId) ) {
				defaultPlanetId = null;
				ensureDefaultPlanetExists(null);
			}
		}
	}

	public boolean hasPlanet(String id) {
		return planets.contains(id);
	}
	
	public void setPlanet(int orbit, Planet planet) {
		if( orbit <= 0 ) {
			return;
		}
		preparePlanetsList(orbit);
		if( null != planet ) {
			// Put the planet or moon where it belongs
			planetOrbits.set(orbit - 1, planet.getId());
			planets.add(planet.getId());
			ensureDefaultPlanetExists(planet.getId());
		} else {
			// planet == null -> Remove planet or moon if there
			planets.remove(planetOrbits.get(orbit - 1));
			planetOrbits.set(orbit - 1, null);
			ensureDefaultPlanetExists(null);
		}
	}
	
	public Set<Planet> getPlanets() {
		Set<Planet> result = new HashSet<Planet>(planets.size());
		for( String planetName : planets ) {
			Planet planet = Planets.getInstance().getPlanetById(planetName);
			if( null != planet ) {
				result.add(planet);
			}
		}
		return result;
	}
	
	public Set<String> getPlanetIds() {
		return new HashSet<String>(planets);
	}
	
	// Star classification
	
	public String getSpectralType() {
		return spectralType;
	}
	
	/** @return normalized spectral type, for display */
	public String getSpectralTypeNormalized() {
		return null != spectralType ? getSpectralType(spectralClass, subtype, luminosity) : "?";
	}
	
	protected String validateLuminosity(String lc) {
		// The order of entries here is important
		if( lc.startsWith("I/II") ) { return LUM_II_EVOLVED; }
		if( lc.startsWith("I-II") ) { return LUM_II_EVOLVED; }
		if( lc.startsWith("Ib/II") ) { return LUM_II_EVOLVED; }
		if( lc.startsWith("Ib-II") ) { return LUM_II_EVOLVED; }
		if( lc.startsWith("II/III") ) { return LUM_III_EVOLVED; }
		if( lc.startsWith("II-III") ) { return LUM_III_EVOLVED; }
		if( lc.startsWith("III/IV") ) { return LUM_IV_EVOLVED; }
		if( lc.startsWith("III-IV") ) { return LUM_IV_EVOLVED; }
		if( lc.startsWith("IV/V") ) { return LUM_V_EVOLVED; }
		if( lc.startsWith("IV-V") ) { return LUM_V_EVOLVED; }
		if( lc.startsWith("III") ) { return LUM_III; }
		if( lc.startsWith("II") ) { return LUM_II; }
		if( lc.startsWith("IV") ) { return LUM_IV; }
		if( lc.startsWith("Ia-0") ) { return LUM_0; } // Alias
		if( lc.startsWith("Ia0") ) { return LUM_0; } // Alias
		if( lc.startsWith("Ia+") ) { return LUM_0; } // Alias
		if( lc.startsWith("Iab") ) { return LUM_IAB; }
		if( lc.startsWith("Ia") ) { return LUM_IA; }
		if( lc.startsWith("Ib") ) { return LUM_IB; }
		if( lc.startsWith("I") ) { return LUM_I; } // includes Ia, Iab and Ib
		if( lc.startsWith("O") ) { return LUM_0; }
		if( lc.startsWith("VII") ) { return LUM_VII; }
		if( lc.startsWith("VI+") ) { return LUM_VI_PLUS; }
		if( lc.startsWith("VI") ) { return LUM_VI; }
		if( lc.startsWith("V") ) { return LUM_V; }
		return null;
	}
	
	/** Includes a parser for spectral type strings */
	protected void setSpectralType(String type) {
		if( null == type ) {
			return;
		}
		
		// We make sure to not rewrite the subtype, in case we need whatever special part is behind it
		String parsedSpectralType = type;
		Integer parsedSpectralClass = null;
		Double parsedSubtype = null;
		String parsedLuminosity = null;
		
		// Subdwarf prefix parsing
		if( type.length() > 2 && type.startsWith("sd") ) {
			// subdwarf
			parsedLuminosity = LUM_VI;
			type = type.substring(2);
		}
		else if( type.length() > 3 && type.startsWith("esd") ) {
			// extreme subdwarf
			parsedLuminosity = LUM_VI_PLUS;
			type = type.substring(3);
		}
		
		if( type.length() < 1 ) {
			// We can't parse an empty string
			return;
		}
		String mainClass = type.substring(0, 1);
		
		if( mainClass.equals("D") && type.length() > 1 && null == parsedLuminosity /* prevent "sdD..." */ ) {
			// white dwarf
			parsedLuminosity = LUM_VII;
			String subTypeString = type.substring(1).replaceAll("^([0-9\\.]*).*?$", "$1");
			try {
				parsedSubtype = Double.parseDouble(subTypeString);
			} catch( NumberFormatException nfex ) {
				return;
			}
			// We're done here, white dwarfs have a spectral class of 0 (and ignore it)
			parsedSpectralClass = 0;
		} else if( getSpectralClassFrom(mainClass) >= 0 ) {
			parsedSpectralClass = getSpectralClassFrom(mainClass);
			String subTypeString = type.length() > 1 ? type.substring(1).replaceAll("^([0-9\\.]*).*?$", "$1") : "5" /* default */;
			try {
				parsedSubtype = Double.parseDouble(subTypeString);
			} catch( NumberFormatException nfex ) {
				return;
			}
			if( type.length() > 1 + subTypeString.length() && null == parsedLuminosity ) {
				// We might have a luminosity, try to parse it
				parsedLuminosity = validateLuminosity(type.substring(1 + subTypeString.length()));
				if( parsedLuminosity.equals(LUM_VII) ) {
					// That's not how white dwarfs work
					return;
				}
			}
		}
		// See if we have all
		if( null != parsedSpectralClass && null != parsedSubtype && null != parsedLuminosity ) {
			spectralType = parsedSpectralType;
			spectralClass = parsedSpectralClass;
			subtype = parsedSubtype;
			luminosity = parsedLuminosity;
		}
	}
	
	public static String getSpectralType(Integer spectralClass, Double subtype, String luminosity) {
		if( null == spectralClass || null == subtype ) {
			return null;
		}
		
		// Formatting subtype value up to two decimal points, if needed
		int subtypeValue = (int)Math.round(subtype * 100);
		if( subtypeValue < 0 ) { subtypeValue = 0; }
		if( subtypeValue > 999 ) { subtypeValue = 999; }
		
		String subtypeFormat = "%.2f";
		if( subtypeValue % 100 == 0 ) { subtypeFormat = "%.0f"; }
		else if( subtypeValue % 10 == 0 ) { subtypeFormat = "%.1f"; }
		
		if( luminosity == LUM_VI ) {
			// subdwarfs
			return "sd" + getSpectralClassName(spectralClass) + String.format(subtypeFormat, subtypeValue / 100.0);
		} else if( luminosity == LUM_VI_PLUS ) {
			// extreme subdwarfs
			return "esd" + getSpectralClassName(spectralClass) + String.format(subtypeFormat, subtypeValue / 100.0);
		} else if( luminosity == LUM_VII ) {
			// white dwarfs
			return String.format(Locale.ROOT, "D" + subtypeFormat, subtypeValue / 100.0);
		} else {
			// main class
			return String.format(Locale.ROOT, "%s" + subtypeFormat + "%s", getSpectralClassName(spectralClass), subtypeValue / 100.0, (null != luminosity ? luminosity : LUM_V));
		}
	}
	
	/** @return the distance to another star in light years (0 if both are in the same system) */
	public double getDistanceTo(Star other) {
		return Math.sqrt(Math.pow(x - other.x, 2) + Math.pow(y - other.y, 2));
	}

	/** @return the distance from the star to its jump point in km */
	public double getDistanceToJumpPoint() {
		if( null == spectralClass || null == subtype ) {
			return getDistanceToJumpPoint(42);
		}
		return getDistanceToJumpPoint(spectralClass, subtype);
	}

	/** Recharge time in hours (assuming the usage of the fastest charing method available) */
	public int getRechargeTime() {
		if(zenithCharge || nadirCharge) {
			return Math.min(176, 141 + 10*spectralClass + subtype.intValue());
		} else {
			return getSolarRechargeTime();
		}
	}
	
	private final int[] rechargeHoursT = new int[]{
			7973, 13371, 21315, 35876, 70424, 134352, 215620, 32188, 569703, 892922};
	private final int[] rechargeHoursL = new int[]{
			512, 616, 717, 901, 1142, 1462, 1767, 2325, 3617, 5038};
	
	/** Recharge time in hours using solar radiation alone (at jump point and 100% efficiency) */
	public int getSolarRechargeTime() {
		if( null == spectralClass || null == subtype ) {
			return 183;
		}
		if( spectralClass == SPECTRAL_T ) {
			// months!
			return rechargeHoursT[subtype.intValue()];
		} else if( spectralClass == SPECTRAL_L ) {
			// weeks!
			return rechargeHoursL[subtype.intValue()];
		} else {
			return 141 + 10*spectralClass + subtype.intValue();
		}
	}
	
	/** @return the rough middle of the habitable zone around this star, in km */
	public double getAverageLifeZone() {
		// TODO Calculate from luminosity and the like. For now, using the table in IO Beta.
		if( null == spectralClass || null == subtype ) {
			return (getMinLifeZone(42) + getMaxLifeZone(42)) / 2;
		}
		return (getMinLifeZone(spectralClass, subtype) + getMaxLifeZone(spectralClass, subtype)) / 2;
	}	

	/**
	 * Copy data (but not the id) from another star.
	 * TODO: Planets.
	 */
	public void copyDataFrom(Star other) {
		if( null != other ) {
			name = null != other.name ? other.name : name;
			shortName = null != other.shortName ? other.shortName : shortName;
			x = null != other.x ? other.x : x;
			y = null != other.y ? other.y : y;
			spectralClass = null != other.spectralClass ? other.spectralClass : spectralClass;
			subtype = null != other.subtype ? other.subtype : subtype;
			luminosity = null != other.luminosity ? other.luminosity : luminosity;
			nadirCharge = null != other.nadirCharge ? other.nadirCharge : nadirCharge;
			zenithCharge = null != other.zenithCharge ? other.zenithCharge : zenithCharge;
			// Spectral classification: Use spectralType where available, and the others only where it's null
			spectralType = null != other.spectralType ? other.spectralType : spectralType;
			numPlanets = null != other.numPlanets ? other.numPlanets : numPlanets;
			numMinorPlanets = null != other.numMinorPlanets ? other.numMinorPlanets : numMinorPlanets;
			mass = null != other.mass ? other.mass : mass;
			lum = null != other.lum ? other.lum : lum;
			temperature = null != other.temperature ? other.temperature : temperature;
			radius = null != other.radius ? other.radius : radius;
			defaultPlanetId = null != other.defaultPlanetId ? other.defaultPlanetId : defaultPlanetId;
			preparePlanetsList(null != numPlanets ? numPlanets.intValue() : 0);
		}
	}

	/**
	 * Distance to jump point given a spectral class and subtype
	 * measured in kilometers
	 * @param spectral
	 * @param subtype
	 * @return
	 */
	public static double getDistanceToJumpPoint(int spectral, double subtype) {
		int spectralTypeNumber = spectral * 10 + (int)subtype;
		double remainder = subtype - (int)subtype;
		return Utilities.lerp(getDistanceToJumpPoint(spectralTypeNumber), getDistanceToJumpPoint(spectralTypeNumber), remainder);
	}
	
	public static double getDistanceToJumpPoint(int spectralTypeNumber) {
		//taken from Dropships and Jumpships sourcebook, pg. 17. L- and T-classes estimated
		switch(spectralTypeNumber) {
			case 89: return 26865052.0;
			case 88: return 27143454.0;
			case 87: return 27419029.0;
			case 86: return 27691862.0;
			case 85: return 27962033.0;
			case 84: return 28229618.0;
			case 83: return 28494691.0;
			case 82: return 28757320.0;
			case 81: return 29403633.0;
			case 80: return 30036042.0;
			case 79: return 31262503.0;
			case 78: return 32442633.0;
			case 77: return 33581315.0;
			case 76: return 37794523.0;
			case 75: return 40668992.0;
			case 74: return 45054062.0;
			case 73: return 48276182.0;
			case 72: return 52741556.0;
			case 71: return 58164544.0;
			case 70: return 64303323.0;
			case 69: return 75000000.0;
			case 68: return 82192147.0;
			case 67: return 90197803.0;
			case 66: return 99120895.0;
			case 65: return 109080037.0;
			case 64: return 120210786.0;
			case 63: return 132668292.0;
			case 62: return 146630374.0;
			case 61: return 162301133.0;
			case 60: return 179915179.0;
			case 59: return 199742590.0;
			case 58: return 222094749.0;
			case 57: return 247331200.0;
			case 56: return 275867748.0;
			case 55: return 308186014.0;
			case 54: return 344844735.0;
			case 53: return 386493164.0;
			case 52: return 433886958.0;
			case 51: return 487907078.0;
			case 50: return 549582283.0;
			case 49: return 620115976.0;
			case 48: return 700918272.0;
			case 47: return 793644393.0;
			case 46: return 900240718.0;
			case 45: return 1023000099.0;
			case 44: return 1164628460.0;
			case 43: return 1328325100.0;
			case 42: return 1517879732.0;
			case 41: return 1737789950.0;
			case 40: return 1993403717.0;
			case 39: return 2291092549.0;
			case 38: return 2638462416.0;
			case 37: return 3044611112.0;
			case 36: return 3520442982.0;
			case 35: return 4079054583.0;
			case 34: return 4736208289.0;
			case 33: return 5510915132.0;
			case 32: return 6426154651.0;
			case 31: return 7509758447.0;
			case 30: return 8795520975.0;
			case 29: return 10324556364.0;
			case 28: return 12147004515.0;
			case 27: return 14324152109.0;
			case 26: return 16931086050.0;
			case 25: return 20060019532.0;
			case 24: return 23824470101.0;
			case 23: return 28364525294.0;
			case 22: return 33853487850.0;
			case 21: return 40506291619.0;
			case 20: return 48590182199.0;
			case 19: return 58438309136.0;
			case 18: return 70467069133.0;
			case 17: return 85198295036.0;
			case 16: return 103287722257.0;
			case 15: return 125563499718.0;
			case 14: return 153063985045.0;
			case 13: return 187117766777.0;
			case 12: return 229404075188.0;
			case 11: return 282065439915.0;
			case 10: return 347840509855.0;
			default: return 0.0;
		}
	}

	public static int getSpectralClassFrom(String spectral) {
		switch(spectral.trim().toUpperCase(Locale.ROOT)) {
			case "O": return SPECTRAL_O;
			case "B": return SPECTRAL_B;
			case "A": return SPECTRAL_A;
			case "F": return SPECTRAL_F;
			case "G": return SPECTRAL_G;
			case "K": return SPECTRAL_K;
			case "M": return SPECTRAL_M;
			case "L": return SPECTRAL_L;
			case "T": return SPECTRAL_T;
			default: return -1;
		}
	}

	public static String getSpectralClassName(int spectral) {
		switch(spectral) {
			case SPECTRAL_O: return "O";
			case SPECTRAL_B: return "B";
			case SPECTRAL_A: return "A";
			case SPECTRAL_F: return "F";
			case SPECTRAL_G: return "G";
			case SPECTRAL_K: return "K";
			case SPECTRAL_M: return "M";
			case SPECTRAL_L: return "L";
			case SPECTRAL_T: return "T";
			default: return "?";
		}
	}

	public static double getMinLifeZone(int spectral, double subtype) {
		int spectralTypeNumber = spectral * 10 + (int)subtype;
		double remainder = subtype - (int)subtype;
		return Utilities.lerp(getMinLifeZone(spectralTypeNumber), getMinLifeZone(spectralTypeNumber), remainder);
	}
	
	private static double getMinLifeZone(int spectralTypeNumber) {
		switch(spectralTypeNumber) {
			case 89: return 69334.0;
			case 88: return 85718.0;
			case 87: return 104182.0;
			case 86: return 124816.0;
			case 85: return 147711.0;
			case 84: return 196788.0;
			case 83: return 253943.0;
			case 82: return 319539.0;
			case 81: return 393937.0;
			case 80: return 477499.0;
			case 79: return 570588.0;
			case 78: return 683220.0;
			case 77: return 807910.0;
			case 76: return 945094.0;
			case 75: return 1095210.0;
			case 74: return 1258696.0;
			case 73: return 1435990.0;
			case 72: return 1627530.0;
			case 71: return 1833752.0;
			case 70: return 2055095.0;
			case 69: return 2319138.0;
			case 68: return 3208345.0;
			case 67: return 4373667.0;
			case 66: return 5735514.0;
			case 65: return 7346411.0;
			case 64: return 8957198.0;
			case 63: return 10606623.0;
			case 62: return 13437355.0;
			case 61: return 16407340.0;
			case 60: return 19622213.0;
			case 59: return 21060769.0;
			case 58: return 22440922.0;
			case 57: return 24000141.0;
			case 56: return 26182800.0;
			case 55: return 28624229.0;
			case 54: return 32571422.0;
			case 53: return 37332074.0;
			case 52: return 43693947.0;
			case 51: return 51915431.0;
			case 50: return 63003696.0;
			case 49: return 66581180.0;
			case 48: return 70141642.0;
			case 47: return 74433863.0;
			case 46: return 77425112.0;
			case 45: return 82535447.0;
			case 44: return 86213444.0;
			case 43: return 91688535.0;
			case 42: return 98151248.0;
			case 41: return 119622155.0;
			case 40: return 129837283.0;
			case 39: return 141053288.0;
			case 38: return 153689329.0;
			case 37: return 160245499.0;
			case 36: return 175148880.0;
			case 35: return 191712676.0;
			case 34: return 210010714.0;
			case 33: return 220038497.0;
			case 32: return 241486956.0;
			case 31: return 253563720.0;
			case 30: return 278962256.0;
			case 29: return 294514601.0;
			case 28: return 326849966.0;
			case 27: return 345792134.0;
			case 26: return 384701313.0;
			case 25: return 408187457.0;
			case 24: return 476847145.0;
			case 23: return 532211330.0;
			case 22: return 621417251.0;
			case 21: return 694412846.0;
			case 20: return 812765280.0;
			case 19: return 1079962499.0;
			case 18: return 1438058066.0;
			case 17: return 1989875373.0;
			case 16: return 2604283395.0;
			case 15: return 3371818500.0;
			case 14: return 4737540501.0;
			case 13: return 6922924960.0;
			case 12: return 9577962205.0;
			case 11: return 13789104394.0;
			case 10: return 18836034615.0;
			default: return 0;
		}
	}
	
	public static double getMaxLifeZone(int spectral, double subtype) {
		int spectralTypeNumber = spectral * 10 + (int)subtype;
		double remainder = subtype - (int)subtype;
		return Utilities.lerp(getMaxLifeZone(spectralTypeNumber), getMaxLifeZone(spectralTypeNumber), remainder);
	}
	
	public static double getMaxLifeZone(int spectralTypeNumber) {
		switch(spectralTypeNumber) {
			case 89: return 154262.0;
			case 88: return 190715.0;
			case 87: return 231795.0;
			case 86: return 277705.0;
			case 85: return 328645.0;
			case 84: return 437836.0;
			case 83: return 565001.0;
			case 82: return 710946.0;
			case 81: return 876476.0;
			case 80: return 1062395.0;
			case 79: return 1269509.0;
			case 78: return 1520107.0;
			case 77: return 1797530.0;
			case 76: return 2102753.0;
			case 75: return 2436749.0;
			case 74: return 2800492.0;
			case 73: return 3194956.0;
			case 72: return 3621114.0;
			case 71: return 4079942.0;
			case 70: return 4572412.0;
			case 69: return 4638276.0;
			case 68: return 6594932.0;
			case 67: return 8929569.0;
			case 66: return 11772898.0;
			case 65: return 15048294.0;
			case 64: return 18377700.0;
			case 63: return 21613496.0;
			case 62: return 27680951.0;
			case 61: return 33187574.0;
			case 60: return 39244426.0;
			case 59: return 42690748.0;
			case 58: return 46062946.0;
			case 57: return 49297586.0;
			case 56: return 53743641.0;
			case 55: return 58795714.0;
			case 54: return 65978008.0;
			case 53: return 76400524.0;
			case 52: return 89287631.0;
			case 51: return 105827610.0;
			case 50: return 128218049.0;
			case 49: return 135419349.0;
			case 48: return 142701962.0;
			case 47: return 150108291.0;
			case 46: return 158854972.0;
			case 45: return 167822075.0;
			case 44: return 175399766.0;
			case 43: return 186594212.0;
			case 42: return 199629657.0;
			case 41: return 242869224.0;
			case 40: return 263609029.0;
			case 39: return 286380918.0;
			case 38: return 312035911.0;
			case 37: return 325346923.0;
			case 36: return 355605301.0;
			case 35: return 389234826.0;
			case 34: return 426385389.0;
			case 33: return 446744826.0;
			case 32: return 490291699.0;
			case 31: return 514811189.0;
			case 30: return 566377913.0;
			case 29: return 597953886.0;
			case 28: return 663604476.0;
			case 27: return 702062818.0;
			case 26: return 781060241.0;
			case 25: return 828744231.0;
			case 24: return 968144204.0;
			case 23: return 1080550276.0;
			case 22: return 1261665328.0;
			case 21: return 1409868505.0;
			case 20: return 1650159810.0;
			case 19: return 2192651135.0;
			case 18: return 2919693648.0;
			case 17: return 4040050000.0;
			case 16: return 5287484468.0;
			case 15: return 6845813319.0;
			case 14: return 9618642836.0;
			case 13: return 14055635525.0;
			case 12: return 19446165689.0;
			case 11: return 27996060437.0;
			case 10: return 38242858157.0;
			default: return 0;
		}
	}
	
	@Override
	public int hashCode() {
		return 31 + ((id == null) ? 0 : id.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if( this == obj ) {
			return true;
		}
		if( obj instanceof Star ) {
			Star other = (Star)obj;
			if( null == id ) {
				return null == other.id;
			}
			return id.equals(other.id);
		}
		return false;
	}

	/** @return a space location corresponding to one of the jump points */
	public SpaceLocation getJumpPoint(boolean nadir) {
		return new JumpPoint(this, nadir);
	}
	
	/**
	 * @return a space location corresponding to the preferred jump point (one with a space
	 *         station attached to it), else a random one.
	 */
	public SpaceLocation getPreferredJumpPoint() {
		if( nadirCharge && !zenithCharge ) {
			return getJumpPoint(true);
		}
		if( !nadirCharge && zenithCharge ) {
			return getJumpPoint(false);
		}
		return getJumpPoint(Math.random() < 0.5);		
	}
	
	/** @return all the available in-system space locations */
	public Set<SpaceLocation> getAllLocations() {
		Set<SpaceLocation> result = new HashSet<SpaceLocation>();
		result.add(getJumpPoint(true));
		result.add(getJumpPoint(false));
		for( Planet planet : getPlanets() ) {
			result.addAll(planet.getAllLocations());
		}
		if( nadirCharge ) {
			result.add(new RechargeStationPoint(this, true));
		}
		if( zenithCharge ) {
			result.add(new RechargeStationPoint(this, false));
		}
		return result;
	}
	
	public String getDefaultPlanetId() {
		return defaultPlanetId;
	}

	public void setDefaultPlanetId(String id) {
		if( null != id ) {
			defaultPlanetId = id;
		}
	}
	
	/** @return the default (default: first defined) planet around this star */
	public Planet getDefaultPlanet() {
		ensureDefaultPlanetExists(null);
		return Planets.getInstance().getPlanetById(defaultPlanetId);
	}
	
	public void setDefaultPlanet(Planet planet) {
		if( null != planet && planet.getStarId().equals(id) ) {
			defaultPlanetId = planet.getId();
		}
	}
	
	/**
	 * Create a Star object from the data gathered in a &lt;star&gt; element
	 */
	public static Star getStarFromXMLData(StarXMLData data) {
		Star result = new Star();
		result.name = data.name;
		result.shortName = data.shortName;
		result.id = null != data.id ? data.id : data.name;
		result.defaultPlanetId = data.defaultPlanetId;
		result.x = data.xCoord;
		result.y = data.yCoord;
		// Spectral classification: use spectralType if available, else the separate values
		if( null != data.spectralType ) {
			result.setSpectralType(data.spectralType);
		} else {
			result.spectralClass = data.spectralClass;
			result.subtype = data.subtype;
			result.luminosity = data.luminosity;
			result.spectralType = getSpectralType(data.spectralClass, data.subtype, data.luminosity);
		}
		result.nadirCharge = null != data.nadirCharge ? data.nadirCharge.booleanValue() : false;
		result.zenithCharge = null != data.zenithCharge ? data.zenithCharge.booleanValue() : false;
		result.numPlanets = data.numPlanets;
		result.numMinorPlanets = data.numMinorPlanets;
		result.preparePlanetsList(null != result.numPlanets ? result.numPlanets.intValue() : 0);
		result.mass = data.mass;
		result.lum = data.lum;
		result.temperature = data.temperature;
		result.radius = data.radius;
		return result;
	}

	/**
	 * Create a Star object from the data gathered in a &lt;planet&gt; element (old style)
	 */
	@SuppressWarnings("deprecation")
	public static Star getStarFromXMLData(PlanetXMLData data) {
		Star result = new Star();
		result.name = data.name;
		result.shortName = data.shortName;
		result.id = null != data.id ? data.id : data.name;
		result.x = data.xCoord;
		result.y = data.yCoord;
		result.spectralClass = data.spectralClass;
		result.subtype = data.subtype;
		result.luminosity = data.luminosity;
		result.spectralType = getSpectralType(data.spectralClass, data.subtype, data.luminosity);
		result.nadirCharge = null != data.nadirCharge ? data.nadirCharge.booleanValue() : false;
		result.zenithCharge = null != data.zenithCharge ? data.zenithCharge.booleanValue() : false;
		return result;
	}

	/** A class representing some event, possibly changing stellar information */
	public static final class StellarEvent {
		@XmlJavaTypeAdapter(DateAdapter.class)
		public Date date;
		public String message;
	    public String name;
		public String shortName;
		@XmlJavaTypeAdapter(BooleanValueAdapter.class)
	    public Boolean nadirCharge;
		@XmlJavaTypeAdapter(BooleanValueAdapter.class)
	    public Boolean zenithCharge;
	}
}
