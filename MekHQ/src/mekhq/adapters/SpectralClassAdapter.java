package mekhq.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import mekhq.campaign.universe.Star;

public class SpectralClassAdapter extends XmlAdapter<String, Integer> {
	@Override
	public Integer unmarshal(String v) throws Exception {
		return Star.getSpectralClassFrom(v);
	}

	@Override
	public String marshal(Integer v) throws Exception {
		return Star.getSpectralClassName(v); 
	}
}
