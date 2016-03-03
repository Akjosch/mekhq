package mekhq.campaign.universe;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import mekhq.adapters.BooleanValueAdapter;
import mekhq.adapters.SpectralClassAdapter;

/**
 * A pure data class holding the Java representation of the star inside the configuration list.
 * This is useful as a single point for reading the data even as the underlying format
 * and data types change and evolve.
 * <p>
 * Currently, "star" and "star system" are treated as synonymous, which is fine for the vast majority
 * of inhabited BT worlds. This might need to change at some point.
 */
@XmlRootElement(name = "star")
public class StarXMLData {
    public String name;
	public String shortName;
	public String id;
	@XmlElement(name = "xcood")
	public Double xCoord;
	@XmlElement(name = "ycood")
    public Double yCoord;
	@XmlElement(name = "event")
    public List<Star.StellarEvent> events;
	@XmlJavaTypeAdapter(BooleanValueAdapter.class)
    public Boolean nadirCharge;
	@XmlJavaTypeAdapter(BooleanValueAdapter.class)
    public Boolean zenithCharge;
	@XmlJavaTypeAdapter(SpectralClassAdapter.class)
    public Integer spectralClass;
    public Double subtype;
    public String luminosity;
    public String spectralType;
    public String desc;
    // Fluff data, for now
    public Double mass; // mass in solar masses (1.98855e30 kg)
    public Double lum; // luminosity in solar luminosity (3.846e26 W) 
    public Double temperature; // effective temperature in K
    public Double radius; // radius in solar radii (695700 km)
    @XmlElement(name="planets")
    public Integer numPlanets; // amount of planets
    @XmlElement(name="minorPlanets")
    public Integer numMinorPlanets; // amount of minor planets
    
}
