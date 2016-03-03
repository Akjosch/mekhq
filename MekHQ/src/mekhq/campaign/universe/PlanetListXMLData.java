package mekhq.campaign.universe;

import java.util.List;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "planets")
public final class PlanetListXMLData {
	@XmlAnyElement(lax=true)
	public List<Object> objects;
}