package mekhq.campaign.universe;

import java.util.Date;
import java.util.Locale;

/** This class represent one of the two official jump points around a system */
public class JumpPoint extends ConstantPoint {
	public JumpPoint(Star star, boolean nadir) {
		super(star, nadir);
	}
	
	@Override
	public double getDistance() {
		return getStar().getDistanceToJumpPoint();
	}
	
	@Override
	public boolean canJumpTo(SpaceLocation other) {
		// We can only jump to other JumpPoints, but not to ourselves, and only when they are in range
		// Other jump points within the same system are fine though.
		if( other instanceof JumpPoint && !this.equals(other) ) {
			return getStar().getDistanceTo(other.getStar()) <= 30.0;
		}
		// Same system, different jump points?
		return isNadir() != ((JumpPoint)other).isNadir();
	}
	
	@Override
	public boolean isJumpPoint() {
		return true;
	}
	
	@Override
	public double getRechargeTime() {
		// Standard recharge time
		return 141 + 10 * getStar().getSpectralClass() + getStar().getSubtype();
	}

	@Override
	public String getName() {
		return "[JumpPoint,star=" + getStar().getId() + ",nadir=" + (isNadir() ? "true" : "false") + "]";
	}
	
	@Override
	public String getDesc(Date when) {
		return String.format(Locale.ROOT, "At %s jump point of %s", isNadir() ? "nadir" : "zenith", getStar().getName(when));
	}
	
	public static JumpPoint fromOptions(String[] opts) {
		Star star = null;
		boolean nadir = true;
		for( String opt : opts ) {
			if( opt.startsWith("star=") ) {
				star = Planets.getInstance().getStarById(opt.substring(5));
			}
			if( opt.startsWith("nadir=") ) {
				nadir = Boolean.parseBoolean(opt.substring(6));
			}
		}
		return new JumpPoint(star, nadir);
	}
}
