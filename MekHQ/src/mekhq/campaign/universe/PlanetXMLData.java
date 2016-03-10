package mekhq.campaign.universe;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.w3c.dom.DOMException;

import mekhq.MekHQ;
import mekhq.adapters.BooleanValueAdapter;
import mekhq.adapters.ClimateAdapter;
import mekhq.adapters.DateAdapter;
import mekhq.adapters.FactionDataAdapter;
import mekhq.adapters.HPGRatingAdapter;
import mekhq.adapters.LifeFormAdapter;
import mekhq.adapters.SocioIndustrialDataAdapter;
import mekhq.adapters.SpectralClassAdapter;
import mekhq.campaign.JumpPath;

/**
 * A pure data class holding the Java representation of the planet inside the configuration list.
 * This is useful as a single point for reading the data even as the underlying format
 * and data types change and evolve.
 */
@XmlRootElement(name = "planet")
public class PlanetXMLData {
    public String name;
	public String shortName;
	public String id;
	public String starId;
	/** @deprecated Should belong to the star */
	@XmlElement(name = "xcood")
	public Double xCoord;
	/** @deprecated Should belong to the star */
	@XmlElement(name = "ycood")
    public Double yCoord;
	@XmlJavaTypeAdapter(FactionDataAdapter.class)
	@XmlElement(name = "faction")
    public Map<String, Integer> factions;
	/** @deprecated Use "event", which can have any number of changes to the planetary data */
	@XmlElement(name = "factionChange")
    public List<FactionChange> factionChanges;
	@XmlElement(name = "event")
    public List<Planet.PlanetaryEvent> events;
	/** Pressure description ID */
    public Integer pressure;
    /** Pressure in Earth standard */
    public Double pressureAtm;
    /** Atmospheric mass compared to Earth's 28.9645 kg/mol */
    public Double atmMass;
    /** Atmospheric description */
    public String atmosphere;
    public Double albedo;
    public Double greenhouseEffect;
    @XmlElement(name = "volcamisn")
    public Integer volcanicActivity;
    @XmlElement(name = "tectonics")
    public Integer tectonicActivity;
    public Integer habitability;
    
    // Human data
    /** Order of magnitude of the population - 1 */
    @XmlElement(name = "pop")
    public Integer populationRating;
    public String government;
    public Integer controlRating;
    
    public Double gravity;
	/** @deprecated Should belong to the star */
	@XmlJavaTypeAdapter(BooleanValueAdapter.class)
    public Boolean nadirCharge;
	/** @deprecated Should belong to the star */
	@XmlJavaTypeAdapter(BooleanValueAdapter.class)
    public Boolean zenithCharge;
    @XmlJavaTypeAdapter(LifeFormAdapter.class)
    public LifeForm lifeForm;
    @XmlJavaTypeAdapter(ClimateAdapter.class)
    public Climate climate;
    public Integer percentWater;
    public Integer temperature;
    public Double dayLength;
	/** @deprecated Should belong to the star */
	@XmlJavaTypeAdapter(SpectralClassAdapter.class)
    public Integer spectralClass;
	/** @deprecated Should belong to the star */
    public Double subtype;
	/** @deprecated Should belong to the star */
    public String luminosity;
    public Integer sysPos;
    @XmlJavaTypeAdapter(SocioIndustrialDataAdapter.class)
    public Planet.SocioIndustrialData socioIndustrial;
	@XmlElement(name = "satellite")
    public List<String> satellites;
	@XmlElement(name = "landMass")
    public List<String> landMasses;
    @XmlJavaTypeAdapter(HPGRatingAdapter.class)
    public Integer hpg;
    public String desc;
    
    /** Points of interest - cities, garrisons, factories */
    @XmlElement(name="poi")
    public List<Planet.PointOfInterest> pois;
    
    // Orbital parameters
	@XmlElement(name = "orbitRadius")
	public Double orbitSemimajorAxis;
	public Double orbitEccentricity;
	public Double orbitInclination;
	
	@XmlElement(name = "class")
	public String className;
	
	// All kinds of flags
	
	/** Mark this planet as not to be included/deleted. Requires a valid id (or name if no id supplied). */
	@XmlJavaTypeAdapter(BooleanValueAdapter.class)
	public Boolean delete;

    /** For testing only 
     * @throws InterruptedException */
	public static void main(String[] args) throws JAXBException, DOMException, ParseException, FileNotFoundException, InterruptedException {
		MekHQ.getInstance().readPreferences();
		Faction.generateFactions();
		@SuppressWarnings("unused")
		Map<String, Star> stars = Planets.getInstance().getStars();
		while( !Planets.getInstance().isInitialized() ) {
			Thread.sleep(50);
		}

		Star start = Planets.getInstance().getStarById("Aquagea");
		Planet startPlanet = Planets.getInstance().getPlanetById("Aquagea V");
		Star end = Planets.getInstance().getStarById("Thurrock");
		JumpPath path = new JumpPath();
		path.addLocation(startPlanet);
		path.addLocation(start.getPreferredJumpPoint());
		path.addLocation(end.getPreferredJumpPoint());
		
		path.writeToXml(new PrintWriter(System.out), 0);
	}

    public static final class FactionChange {
    	@XmlJavaTypeAdapter(DateAdapter.class)
    	public Date date;
    	@XmlJavaTypeAdapter(FactionDataAdapter.class)
    	public Map<String, Integer> faction;
    	
    	@Override
    	public String toString() {
    		StringBuilder sb = new StringBuilder();
    		sb.append("{");
   			sb.append("date=").append(date).append(",");
   			sb.append("faction=").append(faction).append("}");
   			return sb.toString();
    	}
    }
}
