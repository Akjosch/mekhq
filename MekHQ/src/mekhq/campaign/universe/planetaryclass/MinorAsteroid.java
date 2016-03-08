package mekhq.campaign.universe.planetaryclass;

import mekhq.campaign.universe.PlanetaryClass;
import mekhq.campaign.universe.Planet.PlanetaryType;

/** A bunch of rocks loosely held together in a weird shape */
public class MinorAsteroid extends PlanetaryClass {
	public MinorAsteroid() {
		super(PlanetaryType.SMALL_ASTEROID, "ASTEROID", "Asteroid");
	}
}
