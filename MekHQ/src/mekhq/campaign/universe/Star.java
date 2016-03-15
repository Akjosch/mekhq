package mekhq.campaign.universe;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import mekhq.Utilities;
import mekhq.adapters.BooleanValueAdapter;
import mekhq.adapters.DateAdapter;
import mekhq.adapters.SpectralClassAdapter;

@XmlRootElement(name="star")
@XmlAccessorType(XmlAccessType.FIELD)
public class Star implements Serializable {
    private static final long serialVersionUID = -5089854102647097334L;
    
    // Star classification data and methods
    
    public static final int SPECTRAL_O = 0;
    public static final int SPECTRAL_B = 1;
    public static final int SPECTRAL_A = 2;
    public static final int SPECTRAL_F = 3;
    public static final int SPECTRAL_G = 4;
    public static final int SPECTRAL_K = 5;
    public static final int SPECTRAL_M = 6;
    public static final int SPECTRAL_L = 7;
    public static final int SPECTRAL_T = 8;
    public static final int SPECTRAL_Y = 9;
    // Spectral class "D" (white dwarfs) are determined by their luminosity "VII" - the number is here for sorting
    public static final int SPECTRAL_D = 99;
    // TODO: Wolf-Rayet stars ("W"), carbon stars ("C"), S-type stars ("S"), 
    
    public static final String LUM_0           = "0"; //$NON-NLS-1$
    public static final String LUM_IA          = "Ia"; //$NON-NLS-1$
    public static final String LUM_IAB         = "Iab"; //$NON-NLS-1$
    public static final String LUM_IB          = "Ib"; //$NON-NLS-1$
    // Generic class, consisting of Ia, Iab and Ib
    public static final String LUM_I           = "I"; //$NON-NLS-1$
    public static final String LUM_II_EVOLVED  = "I/II"; //$NON-NLS-1$
    public static final String LUM_II          = "II"; //$NON-NLS-1$
    public static final String LUM_III_EVOLVED = "II/III"; //$NON-NLS-1$
    public static final String LUM_III         = "III"; //$NON-NLS-1$
    public static final String LUM_IV_EVOLVED  = "III/IV"; //$NON-NLS-1$
    public static final String LUM_IV          = "IV"; //$NON-NLS-1$
    public static final String LUM_V_EVOLVED   = "IV/V"; //$NON-NLS-1$
    public static final String LUM_V           = "V"; //$NON-NLS-1$
    // typically used as a prefix "sd", not as a suffix
    public static final String LUM_VI          = "VI";  //$NON-NLS-1$
    // typically used as a prefix "esd", not as a suffix
    public static final String LUM_VI_PLUS     = "VI+"; //$NON-NLS-1$
    // always used as class designation "D", never as a suffix
    public static final String LUM_VII         = "VII"; //$NON-NLS-1$
    
    /**
     * Create a Star object from the data gathered in a &lt;planet&gt; element (obsolete old style)
     */
    public static Star getStarFromXMLData(PlanetXMLData data) {
        Star result = new Star();
        result.name = data.name;
        result.shortName = data.shortName;
        result.id = Utilities.nonNull(data.id, data.name);
        result.x = data.xCoord;
        result.y = data.yCoord;
        result.spectralClass = data.spectralClass;
        result.subtype = data.subtype;
        result.luminosity = data.luminosity;
        result.spectralType = StarUtil.getSpectralType(data.spectralClass, data.subtype, data.luminosity);
        result.nadirCharge = Utilities.nonNull(data.nadirCharge, Boolean.FALSE);
        result.zenithCharge = Utilities.nonNull(data.zenithCharge, Boolean.FALSE);
        if( null == result.spectralType ) {
            result.setSpectralType(StarUtil.generateSpectralType(new Random(result.id.hashCode() + 133773), true));
        }
        return result;
    }

    // Base data
    private String id;
    private String name;
    private String shortName;
    @XmlElement(name = "xcood")
    private Double x;
    @XmlElement(name = "ycood")
    private Double y;

    // Spectral type
    private String spectralType;
    @XmlJavaTypeAdapter(SpectralClassAdapter.class)
    private Integer spectralClass;
    private Double subtype;
    private String luminosity;
    
    // Physical characteristics
    /** Mass in solar masses (1.98855e30 kg) */
    private Double mass;
    /** Luminosity in solar luminosity (3.846e26 W)  */
    private Double lum;
    /** Effective temperature in K */
    private Double temperature;
    /** Radius in solar radii (695700 km) */
    private Double radius;

    // Planets and other natural bodies in orbit
    /** Amount of planets */
    @XmlElement(name="planets")
    private Integer numPlanets;
    /** Amount of minor planets, asteroids and the like */
    @XmlElement(name="minorPlanets")
    private Integer numMinorPlanets;
    /**
     * List of planets in a given orbit; can (and often is) partially empty.
     * This list is by the planet's ID, not instance, to help the GC and not create circular references.
     */
    @XmlTransient
    private List<String> planetOrbits = new ArrayList<String>();
    /** All the planets orbiting around this star, even if they have no orbit set. */
    @XmlTransient
    private Set<String> planets = new HashSet<String>();
    @XmlElement(name="defaultPlanet")
    private String defaultPlanetId;
    
    // Human influence
    @XmlJavaTypeAdapter(BooleanValueAdapter.class)
    private Boolean nadirCharge;
    @XmlJavaTypeAdapter(BooleanValueAdapter.class)
    private Boolean zenithCharge;
    
    /**
     * a hash to keep track of dynamic stellar system changes
     * <p>
     * sorted map of [date of change: change information]
     */
    @XmlTransient
    private TreeMap<Date, StellarEvent> events;

    // Fluff
    private String desc;
    
    /** Mark this star to have a procedurally generated spectral class, based on its ID. This has no effect if the class is specified. */
    @XmlJavaTypeAdapter(BooleanValueAdapter.class)
    public Boolean generateType;
    @XmlElement(name = "event")
    private List<StellarEvent> eventList;
    
    // Constants (which are most of them for a star)
    
    public String getId() {
        return id;
    }
    
    public Double getX() {
        return x;
    }
    
    public Double getY() {
        return y;
    }

    public String getSpectralType() {
        return spectralType;
    }
    
    /** @return normalized spectral type, for display */
    public String getSpectralTypeNormalized() {
        return null != spectralType ? StarUtil.getSpectralType(spectralClass, subtype, luminosity) : "?"; //$NON-NLS-1$
    }
    
    public Integer getSpectralClass() {
        return spectralClass;
    }

    public Double getSubtype() {
        return subtype;
    }

    public String getLuminosity() {
        return luminosity;
    }
    
    public Double getMass() {
        return mass;
    }
    
    public Double getMassKg() {
        return null != mass ? mass.doubleValue() * Utilities.SOLAR_MASS : 0.0;
    }

    public Double getLum() {
        return lum;
    }
    
    public Double getLumW() {
        return null != mass ? mass.doubleValue() * Utilities.SOLAR_LUM : 0.0;
    }

    public Double getTemperature() {
        return temperature;
    }

    public Double getRadius() {
        return radius;
    }

    public Double getRadiusKm() {
        return null != radius ? radius.doubleValue() * Utilities.SOLAR_RADIUS : 0.0;
    }
    
    public String getDescription() {
        return desc;
    }
    
    public int getNumPlanets() {
        return Math.max(null != numPlanets ? numPlanets.intValue() : 0, planetOrbits.size());
    }

    public int getNumMinorPlanets() {
        return null != numMinorPlanets ? numMinorPlanets.intValue() : 0;
    }

    public String getDefaultPlanetId() {
        return defaultPlanetId;
    }

    /** @return the default (default: first defined) planet around this star */
    public Planet getDefaultPlanet() {
        ensureDefaultPlanetExists(null);
        return null != defaultPlanetId ? Planets.getInstance().getPlanetById(defaultPlanetId) : null;
    }
    
    // Date-dependant data
    
    private StellarEvent getOrCreateEvent(Date when) {
        if( null == when ) {
            return null;
        }
        if( null == events ) {
            events = new TreeMap<Date, StellarEvent>();
        }
        StellarEvent event = events.get(when);
        if( null == event ) {
            event = new StellarEvent();
            event.date = when;
            events.put(when, event);
        }
        return event;
    }
    
    public List<StellarEvent> getEvents() {
        if( null == events ) {
            return null;
        }
        return new ArrayList<StellarEvent>(events.values());
    }

    protected <T> T getEventData(Date when, T defaultValue, EventGetter<T> getter) {
        if( null == when || null == events || null == getter ) {
            return defaultValue;
        }
        T result = defaultValue;
        for( Date date : events.navigableKeySet() ) {
            if( date.after(when) ) {
                break;
            }
            result = Utilities.nonNull(getter.get(events.get(date)), result);
        }
        return result;
    }

    /** @return events for this year. Never returns <i>null</i>. */
    @SuppressWarnings("deprecation")
    public List<StellarEvent> getEvents(int year) {
        if( null == events ) {
            return Collections.<StellarEvent>emptyList();
        }
        List<StellarEvent> result = new ArrayList<StellarEvent>();
        for( Date date : events.navigableKeySet() ) {
            if( date.getYear() + 1900 > year ) {
                break;
            }
            if( date.getYear() + 1900 == year ) {
                result.add(events.get(date));
            }
        }
        return result;
    }
    
    public String getName(Date when) {
        return getEventData(when, name, new EventGetter<String>() {
            @Override public String get(StellarEvent e) { return e.name; }
        });
    }

    public String getShortName(Date when) {
        return getEventData(when, shortName, new EventGetter<String>() {
            @Override public String get(StellarEvent e) { return e.shortName; }
        });
    }
    
    /** @return short name if set, else full name, else "unnamed" */
    public String getPrintableName(Date when) {
        String result = getShortName(when);
        if( null == result ) {
            result = getName(when);
        }
        return null != result ? result : "unnamed";
    }
    
    public Boolean isNadirCharge(Date when) {
        return getEventData(when, nadirCharge, new EventGetter<Boolean>() {
            @Override public Boolean get(StellarEvent e) { return e.nadirCharge; }
        });
    }

    public boolean isZenithCharge(Date when) {
        return getEventData(when, zenithCharge, new EventGetter<Boolean>() {
            @Override public Boolean get(StellarEvent e) { return e.zenithCharge; }
        });
    }

    public String getRechargeStationsText(Date when) {
        Boolean nadir = isNadirCharge(when);
        Boolean zenith = isZenithCharge(when);
        if(null != nadir && null != zenith && nadir.booleanValue() && zenith.booleanValue()) {
            return "Zenith, Nadir";
        } else if(null != zenith && zenith.booleanValue()) {
            return "Zenith";
        } else if(null != nadir && nadir.booleanValue()) {
            return "Nadir";
        } else {
            return "None";
        }
    }

    /** @return the factions in system; check individual planets for details */
    public Set<Faction> getFactionSet(Date when) {
        Set<Faction> factions = new HashSet<Faction>();
        for( Planet planet : getPlanets() ) {
            Set<Faction> planetaryFactions = planet.getFactionSet(when);
            if( null != planetaryFactions ) {
                factions.addAll(planet.getFactionSet(when));
            }
        }
        return factions;
    }

    public Map<String, Integer> getFactions(Date when) {
        Map<String, Integer> factions = new HashMap<String, Integer>();
        for( Planet planet : getPlanets() ) {
            for( Map.Entry<String, Integer> planetaryFactionEntry : planet.getFactions(when).entrySet() ) {
                Integer currentValue = factions.get(planetaryFactionEntry.getKey());
                if( null == currentValue ) {
                    currentValue = 0;
                }
                currentValue += planetaryFactionEntry.getValue();
                factions.put(planetaryFactionEntry.getKey(), currentValue);
            }
        }
        return factions;
    }

    public String getFactionDesc(Date when) {
        @SuppressWarnings("deprecation")
        int era = Era.getEra(when.getYear() + 1900);
        Set<Faction> factions = getFactionSet(when);
        List<String> factionNames = new ArrayList<String>(factions.size());
        for( Faction f : factions ) {
            factionNames.add(f.getFullName(era));
        }
        Collections.sort(factionNames);
        return Utilities.combineString(factionNames, "/"); //$NON-NLS-1$
    }
    
    // Planet data
    
    /** Make sure our planets list is big enough */
    private void preparePlanetsList(int size) {
        while( size > planetOrbits.size() ) {
            planetOrbits.add(null);
        }
    }
    
    /** Make sure the default planet ID is set to something, unless we have no planets */
    private void ensureDefaultPlanetExists(String suggestedPlanetId) {
        if( null == defaultPlanetId ) {
            if( null != suggestedPlanetId ) {
                defaultPlanetId = suggestedPlanetId;
            } else {
                defaultPlanetId = Utilities.getRandomItem(planets);
            }
        }
    }
    
    /** @return planetary ID in the given orbit */
    public String getPlanetId(int orbit) {
        if( orbit <= 0 || orbit > planetOrbits.size() ) {
            return null;
        }
        return planetOrbits.get(orbit - 1);
    }
    
    /** @return the planet in the given orbit */
    public Planet getPlanet(int orbit) {
        String planetID = getPlanetId(orbit);
        return Planets.getInstance().getPlanetById(planetID);
    }
    
    /** Adds a planet without setting its orbit */
    public void addPlanet(Planet planet) {
        if( null != planet ) {
            planets.add(planet.getId());
            Integer planetPos = planet.getSystemPosition();
            if( null != planetPos && planetPos.intValue() > 0 ) {
                preparePlanetsList(planetPos.intValue());
                planetOrbits.set(planetPos.intValue() - 1, planet.getId());
            }
            ensureDefaultPlanetExists(planet.getId());
        }
    }
    
    public void removePlanet(Planet planet) {
        if( null != planet ) {
            removePlanet(planet.getId());
        }
    }
    
    public void removePlanet(String planetId) {
        if( null != planetId ) {
            planets.remove(planetId);
            for( int orbit = 0; orbit < planetOrbits.size(); ++ orbit ) {
                if( planetId.equals(planetOrbits.get(orbit)) ) {
                    planetOrbits.set(orbit, null);
                }
            }
            if( defaultPlanetId.equals(planetId) ) {
                defaultPlanetId = null;
                ensureDefaultPlanetExists(null);
            }
        }
    }

    public boolean hasPlanet(String id) {
        return planets.contains(id);
    }
    
    public void setPlanet(int orbit, Planet planet) {
        if( orbit <= 0 ) {
            return;
        }
        preparePlanetsList(orbit);
        if( null != planet ) {
            // Put the planet or moon where it belongs
            planetOrbits.set(orbit - 1, planet.getId());
            planets.add(planet.getId());
            ensureDefaultPlanetExists(planet.getId());
        } else {
            // planet == null -> Remove planet or moon if there
            planets.remove(planetOrbits.get(orbit - 1));
            planetOrbits.set(orbit - 1, null);
            ensureDefaultPlanetExists(null);
        }
    }
    
    public Set<Planet> getPlanets() {
        Set<Planet> result = new HashSet<Planet>(planets.size());
        for( String planetName : planets ) {
            Planet planet = Planets.getInstance().getPlanetById(planetName);
            if( null != planet ) {
                result.add(planet);
            }
        }
        return result;
    }
    
    public Set<String> getPlanetIds() {
        return new HashSet<String>(planets);
    }
    
    
    /** Includes a parser for spectral type strings */
    protected void setSpectralType(String type) {
        SpectralDefinition scDef = StarUtil.parseSpectralType(type);
        
        if( null == scDef ) {
            return;
        }
        
        spectralType = scDef.spectralType;
        spectralClass = scDef.spectralClass;
        subtype = scDef.subtype;
        luminosity = scDef.luminosity;
    }
    
    /** @return the distance to another star in light years (0 if both are in the same system) */
    public double getDistanceTo(Star other) {
        return Math.sqrt(Math.pow(x - other.x, 2) + Math.pow(y - other.y, 2));
    }

    /** @return the distance from the star to its jump point in km */
    public double getDistanceToJumpPoint() {
        if( null == spectralClass || null == subtype ) {
            return StarUtil.getDistanceToJumpPoint(42);
        }
        return StarUtil.getDistanceToJumpPoint(spectralClass, subtype);
    }

    /** Recharge time in hours (assuming the usage of the fastest charing method available) */
    public int getRechargeTime() {
        if(zenithCharge || nadirCharge) {
            return Math.min(176, 141 + 10*spectralClass + subtype.intValue());
        } else {
            return getSolarRechargeTime();
        }
    }
    
    /** Recharge time in hours using solar radiation alone (at jump point and 100% efficiency) */
    public int getSolarRechargeTime() {
        if( null == spectralClass || null == subtype ) {
            return 183;
        }
        return StarUtil.getSolarRechargeTime(spectralClass, subtype);
    }
    
    /** @return the rough middle of the habitable zone around this star, in km */
    public double getAverageLifeZone() {
        // TODO Calculate from luminosity and the like. For now, using the table in IO Beta.
        if( null == spectralClass || null == subtype ) {
            return (StarUtil.getMinLifeZone(42) + StarUtil.getMaxLifeZone(42)) / 2;
        }
        return (StarUtil.getMinLifeZone(spectralClass, subtype) + StarUtil.getMaxLifeZone(spectralClass, subtype)) / 2;
    }    

    @SuppressWarnings("unused")
    private void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
        if( null == id ) {
            id = name;
        }
        
        // Spectral classification: use spectralType if available, else the separate values
        if( null != spectralType ) {
            setSpectralType(spectralType);
        } else {
            spectralType = StarUtil.getSpectralType(spectralClass, subtype, luminosity);
        }
        nadirCharge = Utilities.nonNull(nadirCharge, false);
        zenithCharge = Utilities.nonNull(zenithCharge, false);
        preparePlanetsList(null != numPlanets ? numPlanets.intValue() : 0);
        
        // Fill up events
        events = new TreeMap<Date, StellarEvent>();
        if( null != eventList ) {
            for( StellarEvent event : eventList ) {
                if( null != event && null != event.date ) {
                    events.put(event.date, event);
                }
            }
            eventList.clear();
        }
        eventList = null;

        // Generator part, if requested
        if( null != generateType && generateType && null == spectralType ) {
            setSpectralType(StarUtil.generateSpectralType(new Random(id.hashCode() + 133773), true));
        }
    }
    
    @SuppressWarnings("unused")
    private boolean beforeMarshal(Marshaller marshaller) {
        // Fill up our event list from the internal data type
        if( null != events ) {
            eventList = new ArrayList<StellarEvent>(events.values());
        }
        return true;
    }

    /**
     * Copy data (but not the id) from another star.
     * <p>
     * Planet lists aren't changed; they need to be updated on the planet's side.
     */
    public void copyDataFrom(Star other) {
        if( null != other ) {
            name = Utilities.nonNull(other.name, name);
            shortName = Utilities.nonNull(other.shortName, shortName);
            x = Utilities.nonNull(other.x, x);
            y = Utilities.nonNull(other.y, y);
            spectralType = Utilities.nonNull(other.spectralType, spectralType);
            spectralClass =Utilities.nonNull(other.spectralClass, spectralClass);
            subtype = Utilities.nonNull(other.subtype, subtype);
            luminosity = Utilities.nonNull(other.luminosity, luminosity);
            mass = Utilities.nonNull(other.mass, mass);
            lum = Utilities.nonNull(other.lum, lum);
            temperature = Utilities.nonNull(other.temperature, temperature);
            radius = Utilities.nonNull(other.radius, radius);
            numPlanets = Utilities.nonNull(other.numPlanets, numPlanets);
            numMinorPlanets = Utilities.nonNull(other.numMinorPlanets, numMinorPlanets);
            defaultPlanetId = Utilities.nonNull(other.defaultPlanetId, defaultPlanetId);
            nadirCharge = Utilities.nonNull(other.nadirCharge, nadirCharge);
            zenithCharge = Utilities.nonNull(other.zenithCharge, zenithCharge);
            preparePlanetsList(null != numPlanets ? numPlanets.intValue() : 0);
            if( null != other.events ) {
                for( StellarEvent event : other.getEvents() ) {
                    if( null != event && null != event.date ) {
                        StellarEvent myEvent = getOrCreateEvent(event.date);
                        myEvent.message = Utilities.nonNull(event.message, myEvent.message);
                        myEvent.name = Utilities.nonNull(event.name, myEvent.name);
                        myEvent.shortName = Utilities.nonNull(event.shortName, myEvent.shortName);
                        myEvent.nadirCharge = Utilities.nonNull(event.nadirCharge, myEvent.nadirCharge);
                        myEvent.zenithCharge = Utilities.nonNull(event.zenithCharge, myEvent.zenithCharge);
                    }
                }
            }
        }
    }

    @Override
    public int hashCode() {
        return 31 + ((id == null) ? 0 : id.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if( this == obj ) {
            return true;
        }
        if( obj instanceof Star ) {
            Star other = (Star)obj;
            if( null == id ) {
                return null == other.id;
            }
            return id.equals(other.id);
        }
        return false;
    }

    /** @return a space location corresponding to one of the jump points */
    public SpaceLocation getJumpPoint(boolean nadir) {
        return new JumpPoint(this, nadir);
    }
    
    /**
     * @return a space location corresponding to the preferred jump point (one with a space
     *         station attached to it), else a random one.
     */
    public SpaceLocation getPreferredJumpPoint() {
        if( nadirCharge && !zenithCharge ) {
            return getJumpPoint(true);
        }
        if( !nadirCharge && zenithCharge ) {
            return getJumpPoint(false);
        }
        return getJumpPoint(Math.random() < 0.5);        
    }
    
    /** @return all the available in-system space locations */
    public Set<SpaceLocation> getAllLocations() {
        Set<SpaceLocation> result = new HashSet<SpaceLocation>();
        result.add(getJumpPoint(true));
        result.add(getJumpPoint(false));
        for( Planet planet : getPlanets() ) {
            result.addAll(planet.getAllLocations());
        }
        if( nadirCharge ) {
            result.add(new RechargeStationPoint(this, true));
        }
        if( zenithCharge ) {
            result.add(new RechargeStationPoint(this, false));
        }
        return result;
    }
    
    public void setDefaultPlanetId(String id) {
        if( null != id ) {
            defaultPlanetId = id;
        }
    }
    
    public void setDefaultPlanet(Planet planet) {
        if( null != planet && planet.getStarId().equals(id) ) {
            defaultPlanetId = planet.getId();
        }
    }

    // Classes
    
    /** A class representing some event, possibly changing stellar information */
    public static final class StellarEvent {
        @XmlJavaTypeAdapter(DateAdapter.class)
        public Date date;
        public String message;
        public String name;
        public String shortName;
        @XmlJavaTypeAdapter(BooleanValueAdapter.class)
        public Boolean nadirCharge;
        @XmlJavaTypeAdapter(BooleanValueAdapter.class)
        public Boolean zenithCharge;
    }
    
    /** Data class to hold parsed spectral definitions */
    public static final class SpectralDefinition {
        public String spectralType;
        public int spectralClass;
        public double subtype;
        public String luminosity;
        
        public SpectralDefinition(String spectralType, int spectralClass, double subtype, String luminosity) {
            this.spectralType = Objects.requireNonNull(spectralType);
            this.spectralClass = spectralClass;
            this.subtype = subtype;
            this.luminosity = Objects.requireNonNull(luminosity);
        }
    }
    
    // @FunctionalInterface in Java 8
    private static interface EventGetter<T> {
        T get(StellarEvent e);
    }
}
