package mekhq.campaign.universe.planetaryclass;

import mekhq.campaign.universe.Planet.PlanetaryType;
import mekhq.campaign.universe.PlanetaryClass;

public class Planetoid extends PlanetaryClass {
	protected Planetoid(String id, String name) {
		super(PlanetaryType.MEDIUM_ASTEROID, id, name);
	}

}
