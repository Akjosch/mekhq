package mekhq.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import mekhq.campaign.universe.Climate;

public class ClimateAdapter extends XmlAdapter<String, Climate> {
	@Override
	public Climate unmarshal(String v) throws Exception {
		return Climate.parseClimate(v);
	}

	@Override
	public String marshal(Climate v) throws Exception {
		return v.toString();
	}
}
