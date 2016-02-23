package mekhq.campaign.universe;

import java.io.Serializable;

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

	private String id;
	private String name;
	
	//star type
	private int spectralClass = SPECTRAL_G;
	private int subtype = 2;
	private String luminosity = LUM_V;
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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

}
