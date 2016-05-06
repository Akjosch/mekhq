package mekhq.adapter;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import mekhq.Utilities;
import mekhq.campaign.material.MaterialUsage;

public class MaterialUsageSetAdapter extends XmlAdapter<String, EnumSet<MaterialUsage>>{
    @Override
    public EnumSet<MaterialUsage> unmarshal(String v) throws Exception {
        EnumSet<MaterialUsage> result = null;
        if((null != v) && !v.isEmpty()) {
            result = EnumSet.noneOf(MaterialUsage.class);
            for(String val : v.split(",", -1)) {
                try {
                    result.add(MaterialUsage.valueOf(val));
                } catch(IllegalArgumentException iaex) {
                    // Just ignore "wrong" data
                }
            }
        }
        return result;
    }

    @Override
    public String marshal(EnumSet<MaterialUsage> v) throws Exception {
        if(null == v) {
            return null;
        }
        List<String> values = new ArrayList<>(v.size());
        for(MaterialUsage matUsage : v) {
            values.add(matUsage.name());
        }
        return Utilities.combineString(values, ",");
    }

}
