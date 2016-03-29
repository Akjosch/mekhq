package mekhq.campaign.universe.planetaryclass;

/**
 * High-water content planets with temperatures between 260 K and 350 K;
 * typically good cloud cover and greenhouse effect.
 */
public class OceanPlanet extends TerrestrialPlanet {
	public OceanPlanet() {
		super("OCEAN", "Ocean planet");
	}
	
	@Override
	public int generateWaterCoverage(Double rndValue) {
		return 90 + (int)Math.floor(11 * rndValue);
	}
}
