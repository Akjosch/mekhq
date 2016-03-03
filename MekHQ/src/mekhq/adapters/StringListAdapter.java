package mekhq.adapters;

import java.util.Arrays;
import java.util.List;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class StringListAdapter extends XmlAdapter<String, List<String>> {
	@Override
	public List<String> unmarshal(String v) throws Exception {
		return Arrays.<String>asList(v.split(","));
	}

	@Override
	public String marshal(List<String> v) throws Exception {
		StringBuilder sb = new StringBuilder();
		boolean firstElement = true;
		for( String item : v ) {
			if( firstElement ) {
				firstElement = false;
			} else {
				sb.append(",");
			}
			sb.append(item);
		}
		return sb.toString();
	}
}
