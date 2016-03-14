package mekhq.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import mekhq.campaign.universe.StarUtil;

public class SpectralClassAdapter extends XmlAdapter<String, Integer> {
	@Override
	public Integer unmarshal(String v) throws Exception {
		return StarUtil.getSpectralClassFrom(v);
	}

	@Override
	public String marshal(Integer v) throws Exception {
		return StarUtil.getSpectralClassName(v); 
	}
}
