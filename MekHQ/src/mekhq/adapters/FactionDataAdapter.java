package mekhq.adapters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class FactionDataAdapter extends XmlAdapter<String, Map<String, Integer>> {
	@Override
	public Map<String, Integer> unmarshal(String v) throws Exception {
		Map<String, Integer> factions = new HashMap<String, Integer>();
		String[] codes = v.split(",");
		for(String code : codes) {
			String[] subCodes = code.split(":");
			int weight = 100;
			if( subCodes.length > 1 ) {
				try {
					weight = Integer.parseInt(subCodes[1]);
				} catch( NumberFormatException nfex ) {
					// Ignore
				}
			}
			factions.put(subCodes[0], weight);
		}
		return factions;
	}

	@Override
	public String marshal(Map<String, Integer> v) throws Exception {
		StringBuilder sb = new StringBuilder();
		List<String> dataEntries = new ArrayList<String>();
		for( Map.Entry<String, Integer> data : v.entrySet() ) {
			sb.setLength(0);
			sb.append(data.getKey());
			if( data.getValue() != 100 ) {
				sb.append(":").append(data.getValue());
			}
			dataEntries.add(sb.toString());
		}
		sb.setLength(0);
		boolean firstElement = true;
		for( String item : dataEntries ) {
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
