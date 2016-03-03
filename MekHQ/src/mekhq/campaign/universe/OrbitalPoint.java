package mekhq.campaign.universe;

import java.util.Date;
import java.util.Locale;

import mekhq.Utilities;

/** A point (roughly) on the ecliptic */
public class OrbitalPoint extends SpaceLocation {
	private double distance = 0;
	
	protected OrbitalPoint(Star star, double distance) {
		super(star);
		if( null == star ) {
			throw new IllegalArgumentException("Star may not be null.");
		}
		this.distance = distance;
	}

	/** semimajor axis of this orbit in km */
	public double getDistance() {
		return distance;
	}
	
	@Override
	public double getTravelTimeTo(SpaceLocation other, double acceleration) {
		if( this.equals(other) ) {
			return 0;
		}
		if( other instanceof OrbitalPoint ) {
			// Simplification: The average distance between two random points in orbits is
			// equal to the semimajor axis of the bigger one.
			// TODO: Actually determine orbital position, some time in the future.
			// OrbitalPosition subclasses which represent points of interest on the surface
			// of a planet will need to have their own calculation.
			double orbitalDistance = Math.max(distance, ((OrbitalPoint)other).getDistance());
			return Math.sqrt(orbitalDistance * 1000 / 9.8 / acceleration) / 1800;
		}
		if( other instanceof ConstantPoint ) {
			// Travel times are symmetrical, no need to replicate the code
			return other.getTravelTimeTo(this);
		}
		return Double.POSITIVE_INFINITY;
	}

	@Override
	public boolean canJumpTo(SpaceLocation other) {
		// You can't typically jump from here (Lagrange points and
		// anything sufficiently far out of any gravitational pull will need to override this).
		return false;
	}

	@Override
	public boolean isOnSurface() {
		// TODO: Only return true out of a surface point (subclass)
		return true;
	}
	
	@Override
	public double getRechargeTime() {
		// No recharge at orbital positions (for now)
		return Double.POSITIVE_INFINITY;
	}

	@Override
	public String getName() {
		return String.format(Locale.ROOT, "[OrbitalPoint,star=%s,distance=%f]", getStar().getId(), distance);
	}

	@Override
	public String getDesc(Date date) {
		return String.format(Locale.ROOT, "orbit around %s, average distance %.3f AU", getStar().getName(date), distance / Utilities.AU);
	}
	
	@Override
	public String getShortDesc(Date date) {
		return String.format(Locale.ROOT, "%.3f AU orbit around %s", distance / Utilities.AU, getStar().getPrintableName(date));
	}
	
	@Override public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		long temp;
		temp = Double.doubleToLongBits(distance);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if( this == obj ) {
			return true;
		}
		if( !super.equals(obj) ) {
			return false;
		}
		if( getClass() != obj.getClass() ) {
			return false;
		}
		OrbitalPoint other = (OrbitalPoint) obj;
		if( Double.doubleToLongBits(distance) != Double.doubleToLongBits(other.distance) ) {
			return false;
		}
		return true;
	}

	public static OrbitalPoint fromOptions(String[] opts) {
		Star star = null;
		double distance = 0;
		for( String opt : opts ) {
			if( opt.startsWith("star=") ) {
				star = Planets.getInstance().getStarById(opt.substring(5));
			}
			if( opt.startsWith("distance=") ) {
				distance = Double.parseDouble(opt.substring(9));
			}
		}
		return new OrbitalPoint(star, distance);
	}
}
