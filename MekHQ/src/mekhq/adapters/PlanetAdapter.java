package mekhq.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import mekhq.campaign.universe.Planet;
import mekhq.campaign.universe.PlanetXMLData;

public class PlanetAdapter extends XmlAdapter<PlanetXMLData, Planet> {

	/**
	 * Use Planets.updatePlanets() to add new data to the global registry from XML source
	 * or Planet.getPlanetFromXMLData() and Star.getStarFromXMLData() to get a new instance
	 * from an already unmarshalled PlanetXMLData instance.
	 */
	@Override
	public Planet unmarshal(PlanetXMLData v) throws Exception {
		throw new IllegalArgumentException("No automatic loading of Planet classes");
	}

	@Override
	public PlanetXMLData marshal(Planet v) throws Exception {
		PlanetXMLData result = new PlanetXMLData();
		result.id = v.getId();
		result.name = v.getName(null);
		result.shortName = v.getShortName(null);
		result.starId = v.getStarId();
		result.climate = v.getClimate(null);
		result.desc = v.getDescription();
		result.events = v.getEvents();
		result.factions = v.getFactions(null);
		result.gravity = v.getGravity();
		result.hpg = v.getHPG(null);
		result.landMasses = v.getLandMasses();
		result.lifeForm = v.getLifeForm(null);
		result.orbitSemimajorAxis = v.getOrbitSemimajorAxis();
		result.percentWater = v.getPercentWater(null);
		result.pressure = v.getPressure();
		result.satellites = v.getSatellites();
		result.socioIndustrial = v.getSocioIndustrial(null);
		result.sysPos = v.getSystemPosition();
		result.temperature = v.getTemperature(null);
		return result;
	}

}