package mekhq.campaign.universe;

public enum Climate {
	// Temperature ranges from Interstellar Operations Beta
	ARTIC(150, 267, "Arctic"), BOREAL(268, 277, "Boreal"), TEMPERATE(278, 287, "Temperate"),
	WARM(288, 297, "Warm"), TROPICAL(298, 307, "Tropical"), SUPERTROPICAL(308, 317, "Supertropical"),
	HELL(318, 500, "Hellish");
	
	// For old climate data
	public static Climate parseClimate(String val) {
		switch(val) {
			case "0": return ARTIC;
			case "1": return BOREAL;
			case "2": case "3": return TEMPERATE;
			case "4": return WARM;
			case "5": return TROPICAL;
			default: return Climate.valueOf(val);
		}
	}
	
	public final int minTemp;
	public final int maxTemp;
	public final String climateName;
	
	private Climate(int minTemp, int maxTemp, String climateName) {
		this.minTemp = minTemp;
		this.maxTemp = maxTemp;
		this.climateName = climateName;
	}
}
