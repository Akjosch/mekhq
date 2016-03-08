package mekhq.adapters;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class IntegerListAdapter extends XmlAdapter<String, List<Integer>> {
	@Override
	public List<Integer> unmarshal(String v) throws Exception {
		String[] values = v.split(",");
		List<Integer> result = new ArrayList<Integer>(values.length);
		for(String val : values) {
			result.add(Integer.parseInt(val));
		}
		return result;
	}

	@Override
	public String marshal(List<Integer> v) throws Exception {
		StringBuilder sb = new StringBuilder();
		boolean firstElement = true;
		for( Integer item : v ) {
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
