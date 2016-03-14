package mekhq.campaign.universe;

import java.util.Arrays;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import mekhq.Utilities;
import mekhq.campaign.universe.Star.SpectralDefinition;

/** Static method only helper class for stars */
public final class StarUtil {
	//taken from Dropships and Jumpships sourcebook, pg. 17. L- and T-classes estimated
	private static final double[] DISTANCE_TO_JUMP_POINT = new double[]{
		Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
		Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
		347840509855.0, 282065439915.0, 229404075188.0, 187117766777.0, 153063985045.0, // Class B
		125563499718.0, 103287722257.0,  85198295036.0,  70467069133.0,  58438309136.0,
		48590182199.0, 40506291619.0, 33853487850.0, 28364525294.0, 23824470101.0, // Class A
		20060019532.0, 16931086050.0, 14324152109.0, 12147004515.0, 10324556364.0,
		8795520975.0, 7509758447.0, 6426154651.0, 5510915132.0, 4736208289.0, // Class F
		4079054583.0, 3520442982.0, 3044611112.0, 2638462416.0, 2291092549.0,
		1993403717.0, 1737789950.0, 1517879732.0, 1328325100.0, 1164628460.0, // Class G
		1023000099.0,  900240718.0,  793644393.0,  700918272.0,  620115976.0,
		549582283.0, 487907078.0, 433886958.0, 386493164.0, 344844735.0, // Class K
		308186014.0, 275867748.0, 247331200.0, 222094749.0, 199742590.0,
		179915179.0, 162301133.0, 146630374.0, 132668292.0, 120210786.0, // Class M
		109080037.0,  99120895.0,  90197803.0,  82192147.0,  75000000.0,
		64303323.0, 58164544.0, 52741556.0, 48276182.0, 45054062.0, // Class L
		40668992.0, 37794523.0, 33581315.0, 32442633.0, 31262503.0,
		30036042.0, 29403633.0, 28757320.0, 28494691.0, 28229618.0, // Class T
		27962033.0, 27691862.0, 27419029.0, 27143454.0, 26865052.0
	};
	
	private static final int[] HOT_SPECTRAL_TYPE = new int[]{
		Star.SPECTRAL_B, Star.SPECTRAL_B, Star.SPECTRAL_A, Star.SPECTRAL_A, Star.SPECTRAL_A,
		Star.SPECTRAL_F, Star.SPECTRAL_F, Star.SPECTRAL_F, Star.SPECTRAL_F, Star.SPECTRAL_F, Star.SPECTRAL_F};
	
	private static final int[] LIFEFRIENDLY_SPECTRAL_TYPE = new int[]{
		Star.SPECTRAL_M, Star.SPECTRAL_M, Star.SPECTRAL_M, Star.SPECTRAL_K, Star.SPECTRAL_K,
		Star.SPECTRAL_G, Star.SPECTRAL_G, Star.SPECTRAL_F, Star.SPECTRAL_F, Star.SPECTRAL_F, Star.SPECTRAL_F};
	
	private static final double[] MAX_LIFE_ZONE = new double[]{
		Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
		Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
		38242858157.0, 27996060437.0, 19446165689.0, 14055635525.0, 9618642836.0, // Class B
		 6845813319.0,  5287484468.0,  4040050000.0,  2919693648.0, 2192651135.0,
		1650159810.0, 1409868505.0, 1261665328.0, 1080550276.0, 968144204.0, // Class A
		 828744231.0,  781060241.0,  702062818.0,  663604476.0, 597953886.0,
		566377913.0, 514811189.0, 490291699.0, 446744826.0, 426385389.0, // Class F
		389234826.0, 355605301.0, 325346923.0, 312035911.0, 286380918.0,
		263609029.0, 242869224.0, 199629657.0, 186594212.0, 175399766.0, // Class G
		167822075.0, 158854972.0, 150108291.0, 142701962.0, 135419349.0,
		128218049.0, 105827610.0, 89287631.0, 76400524.0, 65978008.0, // Class K
		 58795714.0,  53743641.0, 49297586.0, 46062946.0, 42690748.0,
		39244426.0, 33187574.0, 27680951.0, 21613496.0, 18377700.0, // Class M
		15048294.0, 11772898.0,  8929569.0,  6594932.0,  4638276.0,
		4572412.0, 4079942.0, 3621114.0, 3194956.0, 2800492.0, // Class L
		2436749.0, 2102753.0, 1797530.0, 1520107.0, 1269509.0,
		1062395.0, 876476.0, 710946.0, 565001.0, 437836.0, // Class T
		 328645.0, 277705.0, 231795.0, 190715.0, 154262.0
	};
	
	private static final double[] MIN_LIFE_ZONE = new double[]{
		Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
		Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
		18836034615.0, 13789104394.0, 9577962205.0, 6922924960.0, 4737540501.0, // Class B
		 3371818500.0,  2604283395.0, 1989875373.0, 1438058066.0, 1079962499.0,
		 812765280.0, 694412846.0, 621417251.0, 532211330.0, 476847145.0, // Class A
		 408187457.0, 384701313.0, 345792134.0, 326849966.0, 294514601.0,
		 278962256.0, 253563720.0, 241486956.0, 220038497.0, 210010714.0, // Class F
		 191712676.0, 175148880.0, 160245499.0, 153689329.0, 141053288.0,
		 129837283.0, 119622155.0, 98151248.0, 91688535.0, 86213444.0, // Class G
		  82535447.0,  77425112.0, 74433863.0, 70141642.0, 66581180.0,
		 63003696.0, 51915431.0, 43693947.0, 37332074.0, 32571422.0, // Class K
		 28624229.0, 26182800.0, 24000141.0, 22440922.0, 21060769.0,
		 19622213.0, 16407340.0, 13437355.0, 10606623.0, 8957198.0, // Class M
		  7346411.0,  5735514.0,  4373667.0,  3208345.0, 2319138.0,
		 2055095.0, 1833752.0, 1627530.0, 1435990.0, 1258696.0, // Class L
		 1095210.0,  945094.0,  807910.0,  683220.0,  570588.0,
		 477499.0, 393937.0, 319539.0, 253943.0, 196788.0, // Class T
		 147711.0, 124816.0, 104182.0,  85718.0,  69334.0	
	};
	
	// Slightly modified IO Beta table
	private static final int[] REALISTIC_SPECTRAL_TYPE = new int[]{
			Star.SPECTRAL_F, Star.SPECTRAL_M, Star.SPECTRAL_G, Star.SPECTRAL_K, Star.SPECTRAL_M,
			Star.SPECTRAL_M, Star.SPECTRAL_M, Star.SPECTRAL_M, Star.SPECTRAL_M, Star.SPECTRAL_L, -1};
	
	private static final int[] RECHARGE_HOURS_CLASS_L = new int[]{
			512, 616, 717, 901, 1142, 1462, 1767, 2325, 3617, 5038};

	private static final int[] RECHARGE_HOURS_CLASS_T = new int[]{
			7973, 13371, 21315, 35876, 70424, 134352, 215620, 32188, 569703, 892922};
	
	private static final Set<String> VALID_WHITE_DWARF_SUBCLASSES = new TreeSet<String>();

	static {
		VALID_WHITE_DWARF_SUBCLASSES.addAll(Arrays.asList("", "A", "B", "O", "Q", "Z",
					"AB", "AO", "AQ", "AZ", "BO", "BQ", "BZ", "QZ",
					"ABO", "ABQ", "ABZ", "AOQ", "AOZ", "AQZ", "BOQ", "BOZ", "BQZ", "OQZ",
					"ABOQ", "ABOZ", "ABQZ", "AOQZ", "BOQZ",
					"ABOQZ", "C", "X"));
	}

	public static String generateSpectralType(Random rnd, boolean lifeFriendly) {
		int spectralType;
		if( lifeFriendly ) {
			spectralType = LIFEFRIENDLY_SPECTRAL_TYPE[rnd.nextInt(6) + rnd.nextInt(6)];
		} else {
			spectralType = REALISTIC_SPECTRAL_TYPE[rnd.nextInt(6) + rnd.nextInt(6)];
			if( -1 == spectralType ) {
				spectralType = HOT_SPECTRAL_TYPE[rnd.nextInt(6) + rnd.nextInt(6)];
			}
		}
		// Slightly weighted towards the higher numbers
		int subType = (int)Math.floor(Utilities.lerp(0.0, 10.0, Math.pow(rnd.nextDouble(), 0.8)));
		return getSpectralType(spectralType, subType * 1.0, Star.LUM_V);
	}

	public static double getDistanceToJumpPoint(int spectralTypeNumber) {
		if((spectralTypeNumber >= 0) && (spectralTypeNumber < DISTANCE_TO_JUMP_POINT.length)) {
			return DISTANCE_TO_JUMP_POINT[spectralTypeNumber];
		}
		return 0.0;
	}

	/**
	 * Distance to jump point given a spectral class and subtype measured in kilometers
	 */
	public static double getDistanceToJumpPoint(int spectral, double subtype) {
		int spectralTypeNumber = spectral * 10 + (int)subtype;
		double remainder = subtype - (int)subtype;
		return Utilities.lerp(getDistanceToJumpPoint(spectralTypeNumber), getDistanceToJumpPoint(spectralTypeNumber), remainder);
	}

	public static double getMaxLifeZone(int spectralTypeNumber) {
		if((spectralTypeNumber >= 0) && (spectralTypeNumber < MAX_LIFE_ZONE.length)) {
			return MAX_LIFE_ZONE[spectralTypeNumber];
		}
		return 0.0;
	}

	public static double getMaxLifeZone(int spectral, double subtype) {
		int spectralTypeNumber = spectral * 10 + (int)subtype;
		double remainder = subtype - (int)subtype;
		return Utilities.lerp(getMaxLifeZone(spectralTypeNumber), getMaxLifeZone(spectralTypeNumber), remainder);
	}

	public static double getMinLifeZone(int spectralTypeNumber) {
		if((spectralTypeNumber >= 0) && (spectralTypeNumber < MIN_LIFE_ZONE.length)) {
			return MIN_LIFE_ZONE[spectralTypeNumber];
		}
		return 0.0;
	}

	public static double getMinLifeZone(int spectral, double subtype) {
		int spectralTypeNumber = spectral * 10 + (int)subtype;
		double remainder = subtype - (int)subtype;
		return Utilities.lerp(getMinLifeZone(spectralTypeNumber), getMinLifeZone(spectralTypeNumber), remainder);
	}

	public static int getSolarRechargeTime(int spectralClass, double subtype) {
		if( spectralClass == Star.SPECTRAL_T ) {
			// months!
			return RECHARGE_HOURS_CLASS_T[(int)subtype];
		} else if( spectralClass == Star.SPECTRAL_L ) {
			// weeks!
			return RECHARGE_HOURS_CLASS_L[(int)subtype];
		} else {
			return 141 + 10*spectralClass + (int)subtype;
		}
	}

	public static int getSpectralClassFrom(String spectral) {
		switch(spectral.trim().toUpperCase(Locale.ROOT)) {
			case "O": return Star.SPECTRAL_O;
			case "B": return Star.SPECTRAL_B;
			case "A": return Star.SPECTRAL_A;
			case "F": return Star.SPECTRAL_F;
			case "G": return Star.SPECTRAL_G;
			case "K": return Star.SPECTRAL_K;
			case "M": return Star.SPECTRAL_M;
			case "L": return Star.SPECTRAL_L;
			case "T": return Star.SPECTRAL_T;
			case "Y": return Star.SPECTRAL_Y;
			default: return -1;
		}
	}

	public static String getSpectralClassName(int spectral) {
		switch(spectral) {
			case Star.SPECTRAL_O: return "O";
			case Star.SPECTRAL_B: return "B";
			case Star.SPECTRAL_A: return "A";
			case Star.SPECTRAL_F: return "F";
			case Star.SPECTRAL_G: return "G";
			case Star.SPECTRAL_K: return "K";
			case Star.SPECTRAL_M: return "M";
			case Star.SPECTRAL_L: return "L";
			case Star.SPECTRAL_T: return "T";
			case Star.SPECTRAL_Y: return "Y";
			default: return "?";
		}
	}

	/** @return canonical name for the given combination of spectral class, subtype and luminosity */
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
		
		if( luminosity == Star.LUM_VI ) {
			// subdwarfs
			return "sd" + getSpectralClassName(spectralClass) + String.format(subtypeFormat, subtypeValue / 100.0);
		} else if( luminosity == Star.LUM_VI_PLUS ) {
			// extreme subdwarfs
			return "esd" + getSpectralClassName(spectralClass) + String.format(subtypeFormat, subtypeValue / 100.0);
		} else if( luminosity == Star.LUM_VII ) {
			// white dwarfs
			return String.format(Locale.ROOT, "D" + subtypeFormat, subtypeValue / 100.0);
		} else {
			// main class
			return String.format(Locale.ROOT, "%s" + subtypeFormat + "%s", getSpectralClassName(spectralClass), subtypeValue / 100.0, (null != luminosity ? luminosity : Star.LUM_V));
		}
	}

	/** Parser for spectral type strings */
	public static SpectralDefinition parseSpectralType(String type) {
		if( null == type ) {
			return null;
		}
		
		// We make sure to not rewrite the subtype, in case we need whatever special part is behind it
		String parsedSpectralType = type;
		Integer parsedSpectralClass = null;
		Double parsedSubtype = null;
		String parsedLuminosity = null;
		
		// Subdwarf prefix parsing
		if( type.length() > 2 && type.startsWith("sd") ) {
			// subdwarf
			parsedLuminosity = Star.LUM_VI;
			type = type.substring(2);
		}
		else if( type.length() > 3 && type.startsWith("esd") ) {
			// extreme subdwarf
			parsedLuminosity = Star.LUM_VI_PLUS;
			type = type.substring(3);
		}
		
		if( type.length() < 1 ) {
			// We can't parse an empty string
			return null;
		}
		String mainClass = type.substring(0, 1);
		
		if( mainClass.equals("D") && type.length() > 1 && null == parsedLuminosity /* prevent "sdD..." */ ) {
			// white dwarf
			parsedLuminosity = Star.LUM_VII;
			String whiteDwarfVariant = type.substring(1).replaceAll("([A-Z]*).*?$", "$1");
			if( !VALID_WHITE_DWARF_SUBCLASSES.contains(whiteDwarfVariant) ) {
				// Don't just make up D-class variants, that's silly ...
				return null;
			}
			String subTypeString = type.substring(1 + whiteDwarfVariant.length()).replaceAll("^([0-9\\.]*).*?$", "$1");
			try {
				parsedSubtype = Double.parseDouble(subTypeString);
			} catch( NumberFormatException nfex ) {
				return null;
			}
			// We're done here, white dwarfs have a special spectral class
			parsedSpectralClass = Star.SPECTRAL_D;
		} else if( getSpectralClassFrom(mainClass) >= 0 ) {
			parsedSpectralClass = getSpectralClassFrom(mainClass);
			String subTypeString = type.length() > 1 ? type.substring(1).replaceAll("^([0-9\\.]*).*?$", "$1") : "5" /* default */;
			try {
				parsedSubtype = Double.parseDouble(subTypeString);
			} catch( NumberFormatException nfex ) {
				return null;
			}
			if( type.length() > 1 + subTypeString.length() && null == parsedLuminosity ) {
				// We might have a luminosity, try to parse it
				parsedLuminosity = validateLuminosity(type.substring(1 + subTypeString.length()));
				if( parsedLuminosity.equals(Star.LUM_VII) ) {
					// That's not how white dwarfs work
					return null;
				}
			}
		}
		
		if( null != parsedSpectralClass && null != parsedSubtype && null != parsedLuminosity ) {
			return new SpectralDefinition(parsedSpectralType, parsedSpectralClass, parsedSubtype, parsedLuminosity);
		} else {
			return null;
		}
	}

	/**
	 * @param lc string which starts with some luminosity description
	 * @return the canonical luminosity string based on how this string starts, or <i>null</i> if it doesn't look like luminosity
	 */
	protected static String validateLuminosity(String lc) {
		// The order of entries here is important
		if( lc.startsWith("I/II") ) { return Star.LUM_II_EVOLVED; }
		if( lc.startsWith("I-II") ) { return Star.LUM_II_EVOLVED; }
		if( lc.startsWith("Ib/II") ) { return Star.LUM_II_EVOLVED; }
		if( lc.startsWith("Ib-II") ) { return Star.LUM_II_EVOLVED; }
		if( lc.startsWith("II/III") ) { return Star.LUM_III_EVOLVED; }
		if( lc.startsWith("II-III") ) { return Star.LUM_III_EVOLVED; }
		if( lc.startsWith("III/IV") ) { return Star.LUM_IV_EVOLVED; }
		if( lc.startsWith("III-IV") ) { return Star.LUM_IV_EVOLVED; }
		if( lc.startsWith("IV/V") ) { return Star.LUM_V_EVOLVED; }
		if( lc.startsWith("IV-V") ) { return Star.LUM_V_EVOLVED; }
		if( lc.startsWith("III") ) { return Star.LUM_III; }
		if( lc.startsWith("II") ) { return Star.LUM_II; }
		if( lc.startsWith("IV") ) { return Star.LUM_IV; }
		if( lc.startsWith("Ia-0") ) { return Star.LUM_0; } // Alias
		if( lc.startsWith("Ia0") ) { return Star.LUM_0; } // Alias
		if( lc.startsWith("Ia+") ) { return Star.LUM_0; } // Alias
		if( lc.startsWith("Iab") ) { return Star.LUM_IAB; }
		if( lc.startsWith("Ia") ) { return Star.LUM_IA; }
		if( lc.startsWith("Ib") ) { return Star.LUM_IB; }
		if( lc.startsWith("I") ) { return Star.LUM_I; } // includes Ia, Iab and Ib
		if( lc.startsWith("O") ) { return Star.LUM_0; }
		if( lc.startsWith("VII") ) { return Star.LUM_VII; }
		if( lc.startsWith("VI+") ) { return Star.LUM_VI_PLUS; }
		if( lc.startsWith("VI") ) { return Star.LUM_VI; }
		if( lc.startsWith("V") ) { return Star.LUM_V; }
		return null;
	}
	
	private StarUtil() {}
}
