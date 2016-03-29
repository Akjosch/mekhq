package mekhq.campaign.universe.planetaryclass;

import mekhq.campaign.universe.PlanetaryClass;
import mekhq.campaign.universe.Planet.PlanetaryType;

public abstract class TerrestrialPlanet extends PlanetaryClass {
	protected TerrestrialPlanet(String id, String name) {
		super(PlanetaryType.TERRESTRIAL, id, name);
	}
}
