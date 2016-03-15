package mekhq.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class BooleanValueAdapter extends XmlAdapter<String, Boolean> {
    @Override
    public Boolean unmarshal(String v) throws Exception {
        return v.equalsIgnoreCase("true") //$NON-NLS-1$
                || v.equalsIgnoreCase("yes") //$NON-NLS-1$
                || v.equals("1"); //$NON-NLS-1$
    }

    @Override
    public String marshal(Boolean v) throws Exception {
        return v.toString();
    }

}
