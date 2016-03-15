package mekhq.adapters;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class StringListAdapter extends XmlAdapter<String, List<String>> {
    private static final String SEPARATOR = ","; //$NON-NLS-1$
    
    @Override
    public List<String> unmarshal(String v) throws Exception {
        String[] values = v.split(SEPARATOR);
        List<String> result = new ArrayList<String>(values.length);
        for(String val : values) {
            result.add(val.trim());
        }
        return result;
    }

    @Override
    public String marshal(List<String> v) throws Exception {
        StringBuilder sb = new StringBuilder();
        boolean firstElement = true;
        for(String item : v) {
            if(firstElement) {
                firstElement = false;
            } else {
                sb.append(SEPARATOR);
            }
            sb.append(item.trim());
        }
        return sb.toString();
    }
}
