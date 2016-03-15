package mekhq.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class HPGRatingAdapter extends XmlAdapter<String, Integer> {
    @Override
    public Integer unmarshal(String v) throws Exception {
        return SocioIndustrialDataAdapter.convertRatingToCode(v);
    }

    @Override
    public String marshal(Integer v) throws Exception {
        return SocioIndustrialDataAdapter.convertCodeToRating(v);
    }
}
