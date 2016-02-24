package mekhq.campaign.universe;

/**
 * A class representing a point at a constant distance above/below the eclipptic - jump points
 * and recharge stations for now
 */
public abstract class ConstantPoint extends SpaceLocation {
	private boolean nadir;

	public ConstantPoint(Star star, boolean nadir) {
		super(star);
		if( null == star ) {
			throw new IllegalArgumentException("Star may not be null.");
		}
		this.nadir = nadir;
	}

	public boolean isNadir() {
		return nadir;
	}
	
	public boolean isZenith() {
		return !nadir;
	}

	/** @return average distance to the star, in km */
	public abstract double getDistance();
	
	@Override
	public double travelTimeTo(SpaceLocation other) {
		if( this == other ) {
			return 0;
		}
		if( !inSameSystemAs(other) ) {
			return Double.POSITIVE_INFINITY;
		}
		if( other instanceof OrbitalPoint ) {
			double distance = Math.sqrt(Math.pow(((OrbitalPoint)other).getDistance(), 2)
					+ Math.pow(getDistance(), 2));
			return Math.sqrt(distance * 1000 / 9.8) / 1800;
		}
		if( other instanceof ConstantPoint ) {
			// Another constant point inside the same system - see if it is on the other side or this one
			double distance;
			if( isNadir() == ((ConstantPoint)other).isNadir() ) {
				// Same side
				distance = Math.abs(getDistance() - ((ConstantPoint)other).getDistance());
			} else {
				// Different side
				distance = getDistance() + ((ConstantPoint)other).getDistance();
			}
			return Math.sqrt(distance * 1000 / 9.8) / 1800;
		}
		// Whatever it is, we have no idea how to get there on a direct route. Try something else.
		return Double.POSITIVE_INFINITY;
	}
}