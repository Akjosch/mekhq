package mekhq.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class BooleanValueAdapter extends XmlAdapter<String, Boolean> {
	@Override
	public Boolean unmarshal(String v) throws Exception {
		return v.equalsIgnoreCase("true") || v.equalsIgnoreCase("yes") || v.equals("1");
	}

	@Override
	public String marshal(Boolean v) throws Exception {
		return v.toString();
	}

}
