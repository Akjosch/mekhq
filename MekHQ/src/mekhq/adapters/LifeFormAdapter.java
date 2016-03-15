package mekhq.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import mekhq.campaign.universe.LifeForm;

public class LifeFormAdapter extends XmlAdapter<String, LifeForm> {
    @Override
    public LifeForm unmarshal(String v) throws Exception {
        return LifeForm.parseLifeForm(v);
    }

    @Override
    public String marshal(LifeForm v) throws Exception {
        return v.toString();
    }
}
