package mekhq.campaign.universe;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Star implements Serializable {
	public static final int SPECTRAL_O = 0;
	public static final int SPECTRAL_B = 1;
	public static final int SPECTRAL_A = 2;
	public static final int SPECTRAL_F = 3;
	public static final int SPECTRAL_G = 4;
	public static final int SPECTRAL_K = 5;
	public static final int SPECTRAL_M = 6;
	
	public static final String LUM_0   = "0";
	public static final String LUM_IA  = "Ia";
	public static final String LUM_IB  = "Ib";
	public static final String LUM_II  = "II";
	public static final String LUM_III = "III";
	public static final String LUM_IV  = "IV";
	public static final String LUM_V   = "V";
	public static final String LUM_VI  = "VI";
	public static final String LUM_VII = "VII";

	private double x = 0;
	private double y = 0;

	private String id = null;
	private String name = null;
	
	//star type
	private int spectralClass = SPECTRAL_G;
	private int subtype = 2;
	private String luminosity = LUM_V;
	
	// Amount of planets.
	private int numPlanets = 0;
	// planets - list of planets in a given orbit; can (and often is) partially empty
	// This list is by the planet's ID, not instance, to help the GC and not create circular references
	private List<String> planets = new ArrayList<String>();
	
	private boolean nadirCharge = false;
	private boolean zenithCharge = false;
	
	public double getX() {
		return x;
	}
	
	public double getY() {
		return y;
	}

	public void setX(double x) {
		this.x = x;
	}
	
	public void setY(double y) {
		this.y = y;
	}

	public int getSpectralClass() {
	    return spectralClass;
	}

	public void setSpectralClass(int spectralClass) {
	    this.spectralClass = spectralClass;
	}

	public int getSubtype() {
	    return subtype;
	}

	public void setSubtype(int subtype) {
	    this.subtype = subtype;
	}

	public String getLuminosity() {
		return luminosity;
	}
	
	public void setLuminosity(String luminosity) {
		this.luminosity = luminosity;
	}

	public int getNumPlanets() {
		return numPlanets;
	}

	/** Make sure our planets list is big enough */
	private void preparePlanetsList(int size) {
		while( size > planets.size() ) {
			planets.add(null);
		}
	}
	
	public void setNumPlanets(int numPlanets) {
		if( numPlanets > planets.size() ) {
			this.numPlanets = numPlanets;
			preparePlanetsList(numPlanets);
		}
	}
	
	public String getPlanetId(int orbit) {
		if( orbit <= 0 || orbit > planets.size() ) {
			return null;
		}
		return planets.get(orbit - 1);
	}
	
	public Planet getPlanet(int orbit) {
		String planetID = getPlanetId(orbit);
		return Planets.getInstance().getPlanetById(planetID);
	}
	
	public void setPlanet(int orbit, Planet planet) {
		if( orbit <= 0 ) {
			return;
		}
		preparePlanetsList(orbit);
		if( null != planet ) {
			// Put the planet or moon where it belongs
			planets.set(orbit - 1, planet.getId());
		} else {
			// planet == null -> Remove planet or moon if there
			planets.set(orbit - 1, null);
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
	
	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isNadirCharge() {
		return nadirCharge;
	}

	public void setNadirCharge(boolean nadirCharge) {
		this.nadirCharge = nadirCharge;
	}

	public boolean isZenithCharge() {
		return zenithCharge;
	}

	public void setZenithCharge(boolean zenithCharge) {
		this.zenithCharge = zenithCharge;
	}

	public String getStarType() {
		if( luminosity == LUM_VI ) {
			// subdwarves
			return "sd" + getSpectralClassName(spectralClass) + subtype;
		} else if( luminosity == LUM_VII ) {
			// white dwarves
			// very approximately (for the proper designation we'd need the star's temperature)
			return "D" + spectralClass + "." + subtype;
		} else {
			return getSpectralClassName(spectralClass) + subtype + luminosity;
		}
	}
	
	/** @return the distance to another star in light years (0 if both are in the same system) */
	public double getDistanceTo(Star other) {
		return Math.sqrt(Math.pow(x - other.x, 2) + Math.pow(y - other.y, 2));
	}

	/** @return the distance from the star to its jump point in km */
	public float getDistanceToJumpPoint() {
		return getDistanceToJumpPoint(spectralClass, subtype);
	}

	public String getRechargeStations() {
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

	public int getRechargeTime() {
		if(zenithCharge || nadirCharge) {
			return Math.min(176, 141 + 10*spectralClass + subtype);
		} else {
			return 141 + 10*spectralClass + subtype;
		}
	}
	
	/** @return the rough middle of the habitable zone around this star, in km */
	public double getAverageLifeZone() {
		// TODO Calculate from luminosity and the like. For now, using the table in IO.
		return (getMinLifeZone(spectralClass, subtype) + getMaxLifeZone(spectralClass, subtype)) / 2;
	}	

	/**
	 * Copy data (but not the planets, space stations or id) from another star.
	 */
	public void copyFrom(Star other) {
		if( null != other ) {
			name = other.name;
			x = other.x;
			y = other.y;
			spectralClass = other.spectralClass;
			subtype = other.subtype;
			luminosity = other.luminosity;
		}
	}

	/**
	 * Distance to jump point given a spectral class and subtype
	 * measured in kilometers
	 * @param spectral
	 * @param subtype
	 * @return
	 */
	public static float getDistanceToJumpPoint(int spectral, int subtype) {
	
		//taken from Dropships and Jumpships sourcebook, pg. 17
		switch(spectral) {
		case SPECTRAL_M:
			if(subtype == 0) {
				return 179915179f;
			}
			else if(subtype == 1) {
				return 162301133f;
			}
			else if(subtype == 2) {
				return 146630374f;
			}
			else if(subtype == 3) {
				return 132668292f;
			}
			else if(subtype == 4) {
				return 120210786f;
			}
			else if(subtype == 5) {
				return 109080037f;
			}
			else if(subtype == 6) {
				return 99120895f;
			}
			else if(subtype == 7) {
				return 90197803f;
			}
			else if(subtype == 8) {
				return 82192147f;
			}
			else if(subtype > 8) {
				return 75000000f;
			}
		case SPECTRAL_K:
			if(subtype == 0) {
				return 549582283f;
			}
			else if(subtype == 1) {
				return 487907078f;
			}
			else if(subtype == 2) {
				return 433886958f;
			}
			else if(subtype == 3) {
				return 386493164f;
			}
			else if(subtype == 4) {
				return 344844735f;
			}
			else if(subtype == 5) {
				return 308186014f;
			}
			else if(subtype == 6) {
				return 275867748f;
			}
			else if(subtype == 7) {
				return 247331200f;
			}
			else if(subtype == 8) {
				return 222094749f;
			}
			else if(subtype > 8) {
				return 199742590f;
			}
		case SPECTRAL_G:
			if(subtype == 0) {
				return 1993403717f;
			}
			else if(subtype == 1) {
				return 1737789950f;
			}
			else if(subtype == 2) {
				return 1517879732f;
			}
			else if(subtype == 3) {
				return 1328325100f;
			}
			else if(subtype == 4) {
				return 1164628460f;
			}
			else if(subtype == 5) {
				return 1023000099f;
			}
			else if(subtype == 6) {
				return 900240718f;
			}
			else if(subtype == 7) {
				return 793644393f;
			}
			else if(subtype == 8) {
				return 700918272f;
			}
			else if(subtype > 8) {
				return 620115976f;
			}
		case SPECTRAL_F:
			if(subtype == 0) {
				return 8795520975f;
			}
			else if(subtype == 1) {
				return 7509758447f;
			}
			else if(subtype == 2) {
				return 6426154651f;
			}
			else if(subtype == 3) {
				return 5510915132f;
			}
			else if(subtype == 4) {
				return 4736208289f;
			}
			else if(subtype == 5) {
				return 4079054583f;
			}
			else if(subtype == 6) {
				return 3520442982f;
			}
			else if(subtype == 7) {
				return 3044611112f;
			}
			else if(subtype == 8) {
				return 2638462416f;
			}
			else if(subtype > 8) {
				return 2291092549f;
			}
		case SPECTRAL_A:
			if(subtype == 0) {
				return 48590182199f;
			}
			else if(subtype == 1) {
				return 40506291619f;
			}
			else if(subtype == 2) {
				return 33853487850f;
			}
			else if(subtype == 3) {
				return 28364525294f;
			}
			else if(subtype == 4) {
				return 23824470101f;
			}
			else if(subtype == 5) {
				return 20060019532f;
			}
			else if(subtype == 6) {
				return 16931086050f;
			}
			else if(subtype == 7) {
				return 14324152109f;
			}
			else if(subtype == 8) {
				return 12147004515f;
			}
			else if(subtype > 8) {
				return 10324556364f;
			}
		case SPECTRAL_B:
			if(subtype == 0) {
				return 347840509855f;
			}
			else if(subtype == 1) {
				return 282065439915f;
			}
			else if(subtype == 2) {
				return 229404075188f;
			}
			else if(subtype == 3) {
				return 187117766777f;
			}
			else if(subtype == 4) {
				return 153063985045f;
			}
			else if(subtype == 5) {
				return 12556160986f;
			}
			else if(subtype == 6) {
				return 103287722257f;
			}
			else if(subtype == 7) {
				return 85198295036f;
			}
			else if(subtype == 8) {
				return 70467069133f;
			}
			else if(subtype > 8) {
				return 58438309136f;
			}
		default:
			return 0;
		}
	
	
	}

	public static int getSpectralClassFrom(String spectral) {
		if(spectral.trim().equalsIgnoreCase("B")) {
			return SPECTRAL_B;
		}
		else if(spectral.trim().equalsIgnoreCase("A")) {
			return SPECTRAL_A;
		}
		else if(spectral.trim().equalsIgnoreCase("F")) {
			return SPECTRAL_F;
		}
		else if(spectral.trim().equalsIgnoreCase("G")) {
			return SPECTRAL_G;
		}
		else if(spectral.trim().equalsIgnoreCase("M")) {
			return SPECTRAL_M;
		}
		else if(spectral.trim().equalsIgnoreCase("K")) {
			return SPECTRAL_K;
		}
		else {
			return SPECTRAL_O;
		}
	}

	public static String getSpectralClassName(int spectral) {
		switch(spectral) {
		case SPECTRAL_O:
			return "O";
		case SPECTRAL_B:
			return "B";
		case SPECTRAL_A:
			return "A";
		case SPECTRAL_F:
			return "F";
		case SPECTRAL_G:
			return "G";
		case SPECTRAL_K:
			return "K";
		case SPECTRAL_M:
			return "M";
		default:
			return "?";
		}
	}

	public static double getMinLifeZone(int spectral, int subtype) {
		switch(spectral * 10 + subtype) {
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
	
	public static double getMaxLifeZone(int spectral, int subtype) {
		switch(spectral * 10 + subtype) {
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

}
