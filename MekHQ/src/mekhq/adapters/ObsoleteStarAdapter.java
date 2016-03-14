package mekhq.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import mekhq.campaign.universe.PlanetXMLData;
import mekhq.campaign.universe.Star;

public class ObsoleteStarAdapter extends XmlAdapter<PlanetXMLData, Star> {

	@Override
	public Star unmarshal(PlanetXMLData v) throws Exception {
		return Star.getStarFromXMLData(v);
	}

	@Override
	public PlanetXMLData marshal(Star v) throws Exception {
		throw new IllegalArgumentException("Loading obsolete star definition is read-only");
	}

}
