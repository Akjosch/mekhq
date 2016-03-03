package mekhq.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import mekhq.campaign.universe.Star;
import mekhq.campaign.universe.StarXMLData;

public class StarAdapter extends XmlAdapter<StarXMLData, Star> {

	/**
	 * Use Planets.updatePlanets() to add new data to the global registry from XML source
	 * or Star.getStarFromXMLData() to get a new instance
	 * from an already unmarshalled StarXMLData instance.
	 */
	@Override
	public Star unmarshal(StarXMLData v) throws Exception {
		throw new IllegalArgumentException("No automatic loading of Star classes");
	}

	@Override
	public StarXMLData marshal(Star v) throws Exception {
		StarXMLData result = new StarXMLData();
		result.id = v.getId();
		result.name = v.getName(null);
		result.shortName = v.getShortName(null);
		result.xCoord = v.getX();
		result.yCoord = v.getY();
		result.desc = v.getDescription();
		result.spectralType = v.getSpectralType();
		result.nadirCharge = v.isNadirCharge(null);
		result.zenithCharge = v.isZenithCharge(null);
		result.events = v.getEvents();
		// fluff
		result.numPlanets = v.getNumPlanets();
		result.numMinorPlanets = v.getNumMinorPlanets();
		result.mass = v.getMass();
		result.lum = v.getLum();
		result.temperature = v.getTemperature();
		result.radius = v.getRadius();
		
		return result;
	}

}
