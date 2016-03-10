package mekhq.campaign.universe;

import java.util.Locale;

/** A point on the surface of a planet */
public class PlanetarySurfacePoint extends OrbitalPoint {
	protected String planetId;
	protected double longitude;
	protected double latitude;
	protected double altitude;
	protected double x;
	protected double y;
	protected double z;
	
	protected PlanetarySurfacePoint(Planet planet, double longitude, double latitude, double altitude) {
		super(planet.getStar(), planet.getOrbitSemimajorAxisKm());
		planetId = planet.getId();
		if( latitude > 90.0 || latitude < -90.0 ) {
			throw new IllegalArgumentException("Latitude has to be between +90.0 (north pole) and -90.0 (south pole), inclusive");
		}
		this.latitude = latitude;
		if( longitude > 180.0 ) {
			longitude -= Math.ceil(longitude / 360.0) * 360.0;
		}
		if( longitude < -180.0 ) {
			longitude -= Math.floor(longitude / 360.0) * 360.0;
		}
		this.longitude = longitude;
		this.altitude = altitude; // TODO: Planet radius check
		
		// Position in flat 3D coordinates, for display and distance calculations. TODO: Flattening
		double r = null != planet.getRadius() ? planet.getRadius() : 0.0;
		r += altitude;
		x = r * Math.cos(Math.toRadians(longitude)) * Math.sin(Math.toRadians(latitude));
		y = r * Math.sin(Math.toRadians(longitude)) * Math.sin(Math.toRadians(latitude));
		z = r * Math.cos(Math.toRadians(latitude));
	}
	
	@Override
	public String getName() {
		return String.format(Locale.ROOT, "[PlanetarySurfacePoint,planet=%s,long=%f,lat=%f,alt=%f]",
				planetId, longitude, latitude, altitude);
	}

	public static PlanetarySurfacePoint fromOptions(String[] opts) {
		Planet planet = null;
		double longitude = 0.0;
		double latitude = 0.0;
		double altitude = 0.0;
		for( String opt : opts ) {
			if( opt.startsWith("planet=") ) {
				planet = Planets.getInstance().getPlanetById(opt.substring(7));
			}
			if( opt.startsWith("long=") ) {
				longitude = Double.parseDouble(opt.substring(5));
			}
			if( opt.startsWith("lat=") ) {
				latitude = Double.parseDouble(opt.substring(4));
			}
			if( opt.startsWith("alt=") ) {
				altitude = Double.parseDouble(opt.substring(4));
			}
		}
		return new PlanetarySurfacePoint(planet, longitude, latitude, altitude);
	}
}
