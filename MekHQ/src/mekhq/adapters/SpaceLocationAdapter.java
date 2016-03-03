package mekhq.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import mekhq.campaign.universe.SpaceLocation;

public class SpaceLocationAdapter extends XmlAdapter<String, SpaceLocation> {
	@Override public SpaceLocation unmarshal(String v) throws Exception {
		return SpaceLocation.byName(v);
	}

	@Override public String marshal(SpaceLocation v) throws Exception {
		return v.getName();
	}

}
