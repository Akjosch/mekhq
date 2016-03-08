package mekhq.campaign.universe.planetaryclass;

import mekhq.campaign.universe.PlanetaryClass;
import mekhq.campaign.universe.Planet.PlanetaryType;

/**  Giant planet composed mostly of water, methane and ammonia; typically very cold. */
public class IceGiant extends PlanetaryClass {
	public IceGiant() {
		super(PlanetaryType.ICE_GIANT, "ICE_GIANT", "Ice giant");
	}
}
