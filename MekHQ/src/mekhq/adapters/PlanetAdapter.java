package mekhq.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import mekhq.campaign.universe.Planet;
import mekhq.campaign.universe.PlanetXMLData;

public class PlanetAdapter extends XmlAdapter<PlanetXMLData, Planet> {
	@Override
	public Planet unmarshal(PlanetXMLData v) throws Exception {
		return Planet.getPlanetFromXMLData(v);
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
		result.className = v.getClassName();
		result.events = v.getEvents();
		result.factions = v.getFactions(null);
		result.gravity = v.getGravity();
		result.hpg = v.getHPG(null);
		result.landMasses = v.getLandMasses();
		result.lifeForm = v.getLifeForm(null);
		result.orbitSemimajorAxis = v.getOrbitSemimajorAxis();
		result.orbitEccentricity = v.getOrbitEccentricity();
		result.orbitInclination = v.getOrbitInclination();
		result.percentWater = v.getPercentWater(null);
		result.pressure = v.getPressure(null);
		result.pressureAtm = v.getPressureAtm(null);
		result.atmMass = v.getAtmMass(null);
		result.atmosphere = v.getAtmosphere(null);
		result.albedo = v.getAlbedo(null);
		result.greenhouseEffect = v.getGreenhouseEffect(null);
		result.habitability = v.getHabitability(null);
		result.pois = v.getPois(null);
		result.satellites = v.getSatellites();
		result.socioIndustrial = v.getSocioIndustrial(null);
		result.sysPos = v.getSystemPosition();
		result.temperature = v.getTemperature(null);
		result.volcanicActivity = v.getVolcanicActivity();
		result.tectonicActivity = v.getTectonicActivity();
		result.dayLength = v.getDayLength();
		return result;
	}

}
