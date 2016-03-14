package mekhq.campaign.universe;

import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.w3c.dom.DOMException;

import mekhq.MekHQ;
import mekhq.adapters.BooleanValueAdapter;
import mekhq.adapters.SpectralClassAdapter;

/**
 * A pure data class holding the Java representation of the planet inside the configuration list.
 * This is only used to get old stellar data out of that list.
 */
@XmlRootElement(name = "planet")
public class PlanetXMLData {
    public String name;
	public String shortName;
	public String id;
	@XmlElement(name = "xcood")
	public Double xCoord;
	@XmlElement(name = "ycood")
    public Double yCoord;
	@XmlJavaTypeAdapter(BooleanValueAdapter.class)
    public Boolean nadirCharge;
	@XmlJavaTypeAdapter(BooleanValueAdapter.class)
    public Boolean zenithCharge;
	@XmlJavaTypeAdapter(SpectralClassAdapter.class)
    public Integer spectralClass;
    public Double subtype;
    public String luminosity;
    public String desc;

    /** For testing only 
     * @throws InterruptedException */
	public static void main(String[] args) throws JAXBException, DOMException, ParseException, FileNotFoundException, InterruptedException {
		MekHQ.getInstance().readPreferences();
		Faction.generateFactions();
		@SuppressWarnings("unused")
		Map<String, Star> starsLoaded = Planets.getInstance().getStars();
		while( !Planets.getInstance().isInitialized() ) {
			Thread.sleep(50);
		}
		
		Planets.getInstance().writeStar(System.out, Planets.getInstance().getStarById("Thurrock"));
	}
}
