package mekhq.campaign.universe;

import java.util.Locale;

/** Recharge station */
public class RechargeStationPoint extends ConstantPoint {
	protected RechargeStationPoint(Star star, boolean nadir) {
		super(star, nadir);
	}

	@Override
	public double getDistance() {
		// Recharge stations are "slightly inward" in respect to the actual jump points (SO p.138),
		// so we estimate that it's some 5 thousand km inwards (40 minutes transit time at 1g).
		return getStar().getDistanceToJumpPoint() - 5000;
	}
	
	@Override
	public boolean canJumpTo(SpaceLocation other) {
		// No, recharge stations are too close to the star, travel to a jump point
		return false;
	}

	@Override
	public double rechargeTime() {
		// Constant time
		return 176;
	}


	@Override
	public String getName() {
		return "[RechargeStation,star=" + getStar().getId() + ",nadir=" + (isNadir() ? "true" : "false") + "]";
	}
	
	@Override
	public String getDesc() {
		return String.format(Locale.ROOT, "At %s recharge station of %s", isNadir() ? "nadir" : "zenith", getStar().getName());
	}
	
	public static RechargeStationPoint fromOptions(String[] opts) {
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
		return new RechargeStationPoint(star, nadir);
	}
}
