package mekhq.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import mekhq.campaign.universe.PlanetXMLData;
import mekhq.campaign.universe.Star;

public class ObsoleteStarAdapter extends XmlAdapter<PlanetXMLData, Star> {
    @Override
    public Star unmarshal(PlanetXMLData v) throws Exception {
        if( null == v.xCoord || null == v.yCoord ) {
            // New type planet or incomplete star; ignore
            return null;
        }
        return Star.getStarFromXMLData(v);
    }

    @Override
    public PlanetXMLData marshal(Star v) throws Exception {
        throw new IllegalArgumentException("Obsolete star definitions are read-only"); //$NON-NLS-1$
    }
}
