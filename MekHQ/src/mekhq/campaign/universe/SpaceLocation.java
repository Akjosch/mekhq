package mekhq.campaign.universe;

/** A class representing some kind of location in space */
public abstract class SpaceLocation {
	private Star star;
	
	public static SpaceLocation byName(String name) {
		// TODO: Make this a generic registry lookup type
		if( null == name || name.length() <= 2 || !name.startsWith("[") || !name.endsWith("]") ) {
			return null;
		}
		// Skip the brackets
		name = name.substring(1, name.length() - 1);
		String type = name.replaceAll(",.*$", "");
		String[] options = name.replaceAll("^[A-Za-z0-9]*,", "").split(",");
		switch( type ) {
			case "OrbitalPoint": return OrbitalPoint.fromOptions(options);
			case "JumpPoint": return JumpPoint.fromOptions(options);
			case "RechargeStation": return RechargeStationPoint.fromOptions(options);
			default: return null;
		}
	}
	
	protected SpaceLocation(Star star) {
		this.star = star;
	}
	
	public Star getStar() {
		return star;
	}
	
	public boolean inSameSystemAs(SpaceLocation other) {
		// If the star is set to null, we aren't in the same system as anyone else by definition
		return star == other.star && null != star;
	}
	
	/**
	 * A function calculating the travel time to another point in space, in hours, and assuming "normal"
	 * travel modes (1g acceleration for interplanetary travel, 1.5g for takeoff to low orbit and so on).
	 * <p>
	 * For jumps, the travel time is assumed to be about 1 hour mostly used for hyperspace
	 * calculations (for the precise determinations, see SO p.88).
	 * <p>
	 * If there's no way to travel between the two space locations directly, the function
	 * should return Double.POSITIVE_INFINITY
	 */
	public abstract double getTravelTimeTo(SpaceLocation other);
	
	/**
	 * A function returning "true" if you can jump from this space location to the other one.
	 */
	public abstract boolean canJumpTo(SpaceLocation other);
	
	/**
	 * A function returning how much time it takes to recharge a normal K-F jump drive from
	 * empty to full assuming all relevant components work fully, in hours.
	 * <p>
	 * This should return Double.POSITIVE_INFINITY if recharging is not possible here (typically anywhere
	 * outside jump points and recharge stations).
	 */
	public abstract double getRechargeTime();

	/**
	 * @return a machine-readable name allowing to reconstruct this location later on
	 */
	public abstract String getName();
	
	/**
	 * @return human-readable i18n string reference
	 */
	public String getDesc() {
		return "Lost in space";
	}
}
