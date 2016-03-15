package mekhq.adapters;

import java.awt.Color;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class ColorAdapter extends XmlAdapter<String, Color> {
    @Override
    public Color unmarshal(String v) throws Exception {
        String[] values = v.split(","); //$NON-NLS-1$
        if(values.length == 3) {
            int colorRed = Integer.parseInt(values[0]);
            int colorGreen = Integer.parseInt(values[1]);
            int colorBlue = Integer.parseInt(values[2]);
            return new Color(colorRed, colorGreen, colorBlue);
        }
        return null;
    }

    @Override
    public String marshal(Color v) throws Exception {
        return String.format("%d,%d,%d", v.getRed(), v.getGreen(), v.getBlue()); //$NON-NLS-1$
    }

}
