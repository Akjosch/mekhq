package mekhq.campaign.universe.planetaryclass;

/**
 * Special - tidally flexed small planet, see Io.
 * These need special calculations to appear, and can only appear as moons of gas giants.
 */
public class SulfurPlanetoid extends Planetoid {
	public SulfurPlanetoid() {
		super("SULFUR_PLANETOID", "Tidally-flexed planetoid");
	}
}
