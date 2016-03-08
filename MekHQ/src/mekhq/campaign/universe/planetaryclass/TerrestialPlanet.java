package mekhq.campaign.universe.planetaryclass;

import mekhq.campaign.universe.PlanetaryClass;
import mekhq.campaign.universe.Planet.PlanetaryType;

public abstract class TerrestialPlanet extends PlanetaryClass {
	protected TerrestialPlanet(String id, String name) {
		super(PlanetaryType.TERRESTIAL, id, name);
	}
}
