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
import mekhq.adapters.StringListAdapter;
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
    public Integer pressure;
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
    @XmlJavaTypeAdapter(StringListAdapter.class)
	@XmlElement(name = "satellite")
    public List<String> satellites;
    @XmlJavaTypeAdapter(StringListAdapter.class)
	@XmlElement(name = "landMass")
    public List<String> landMasses;
    @XmlJavaTypeAdapter(HPGRatingAdapter.class)
    public Integer hpg;
    public String desc;
	@XmlElement(name = "orbitRadius")
	public Double orbitSemimajorAxis;
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
		Star aquagea = Planets.getInstance().getStarById("Aquagea");
		Planet oldAquagea = Planets.getInstance().getPlanetById("Aquagea IV");

		// Try to output it back
		Planets.getInstance().writeStar(System.out, aquagea, true);
		
		// Try out jump paths
		JumpPath jp = new JumpPath();
		jp.addLocation(oldAquagea);
		jp.addLocation(Planets.getInstance().getPlanetById("Aquagea V"));
		jp.addLocation(aquagea.getPreferredJumpPoint());
		jp.addLocation(Planets.getInstance().getStarById("Thurrock"));
		
		System.out.println("");
		System.out.println("Amount of jumps: " + jp.getJumps());
		System.out.println("Travel time: " + jp.getTotalTime(0));
		System.out.println("Recharging time: " + jp.getTotalRechargeTime());
		System.out.flush();
		
		jp.writeToXml(new PrintWriter(System.out), 0);
		
		System.out.println("");
		System.out.flush();
		
		for( JumpPath.Edge edge : jp.getEdges() ) {
			System.out.println(edge.getDesc(null));
		}
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
