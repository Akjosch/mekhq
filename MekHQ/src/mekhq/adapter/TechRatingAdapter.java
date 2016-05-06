package mekhq.adapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import megamek.common.EquipmentType;

public class TechRatingAdapter extends XmlAdapter<String, Integer> {
    @Override
    public Integer unmarshal(String v) throws Exception {
        if(null == v) {
            return Integer.valueOf(EquipmentType.RATING_C);
        }
        return SocioIndustrialDataAdapter.convertRatingToCode(v);
    }

    @Override
    public String marshal(Integer v) throws Exception {
        if(null == v) {
            return null;
        }
        return SocioIndustrialDataAdapter.convertCodeToRating(v);
    }

}
