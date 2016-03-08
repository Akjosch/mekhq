package mekhq.campaign.universe.planetaryclass;

import mekhq.campaign.universe.Planet.PlanetaryType;
import mekhq.campaign.universe.PlanetaryClass;

public class GasGiant extends PlanetaryClass {
	protected GasGiant(String id, String name) {
		super(PlanetaryType.GAS_GIANT, id, name);
	}

}
