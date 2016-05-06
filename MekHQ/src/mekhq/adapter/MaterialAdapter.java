package mekhq.adapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import mekhq.campaign.material.Material;
import mekhq.campaign.material.Materials;

public class MaterialAdapter extends XmlAdapter<String, Material> {
    @Override
    public Material unmarshal(String v) throws Exception {
        return (null == v) ? null : Materials.getMaterial(v);
    }

    @Override
    public String marshal(Material v) throws Exception {
        return (null == v) ? null : v.getId();
    }

}
