package mekhq.campaign.universe;

public enum LifeForm {
	NONE("None"), MICROBE("Microbes"), PLANT("Plants"), INSECT("Insects"), FISH("Fish"),
	AMPH("Amphibians"), REPTILE("Reptiles"), BIRD("Birds"), MAMMAL("Mammals");
	
	// For old life form data
	public static LifeForm parseLifeForm(String val) {
		switch(val) {
			case "0": return NONE;
			case "1": return MICROBE;
			case "2": return PLANT;
			case "3": return FISH;
			case "4": return AMPH;
			case "5": return REPTILE;
			case "6": return BIRD;
			case "7": return MAMMAL;
			case "8": return INSECT;
			default: return LifeForm.valueOf(val);
		}
	}
	
	public final String name;
	
	private LifeForm(String name) {
		this.name = name;
	}
}
