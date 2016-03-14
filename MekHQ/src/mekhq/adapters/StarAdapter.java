package mekhq.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import mekhq.campaign.universe.Star;
import mekhq.campaign.universe.StarXMLData;

public class StarAdapter extends XmlAdapter<StarXMLData, Star> {
	@Override
	public Star unmarshal(StarXMLData v) throws Exception {
		return Star.getStarFromXMLData(v);
	}

	@Override
	public StarXMLData marshal(Star v) throws Exception {
		StarXMLData result = new StarXMLData();
		result.id = v.getId();
		result.name = v.getName(null);
		result.shortName = v.getShortName(null);
		result.defaultPlanetId = v.getDefaultPlanetId();
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
