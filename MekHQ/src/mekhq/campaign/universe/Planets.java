package mekhq.campaign.universe;

import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.DOMException;

import mekhq.FileParser;
import mekhq.MekHQ;
import mekhq.Utilities;
import mekhq.adapters.ObsoleteStarAdapter;

public class Planets {
	private final static Object LOADING_LOCK = new Object[0];
	
	private boolean initialized = false;
	private boolean initializing = false;
	private static Planets planets;
	private static ConcurrentMap<String, Planet> planetList = new ConcurrentHashMap<String, Planet>();
	private static ConcurrentMap<String, Star> starList = new ConcurrentHashMap<String, Star>();
	/**
	 * organizes systems into a grid of 30lyx30ly squares so we can find
	 * nearby systems without iterating through the entire planet list
	 */
 	private static HashMap<Integer, Map<Integer, Set<Star>>> starGrid;
 	
	// Marshaller / unmarshaller instances
	private static Marshaller marshaller;
	private static Unmarshaller unmarshaller;
	static {
		try {
			JAXBContext context = JAXBContext.newInstance(
					LocalPlanetList.class, LocalStarList.class, Planet.class, Star.class);
			marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			unmarshaller = context.createUnmarshaller();
			// For debugging only!
			// unmarshaller.setEventHandler(new javax.xml.bind.helpers.DefaultValidationEventHandler());
		} catch(JAXBException e) {
			MekHQ.logError(e);
		}
	}
 	
    private Thread loader;

    private Planets() {
        planetList = new ConcurrentHashMap<String, Planet>();
        starList = new ConcurrentHashMap<String, Star>();
		starGrid = new HashMap<Integer, Map<Integer, Set<Star>>>();
   }

    private static Set<Star> getStarGrid(int x, int y) {
    	if( !starGrid.containsKey(x) ) {
    		return null;
    	}
    	return starGrid.get(x).get(y);
    }
    
    public static List<Star> getNearbyStars(Planet p, int distance) {
    	return getNearbyStars(p.getStar(), distance);
    }
    
    public static List<Star> getNearbyStars(final Star star, int distance) {
    	List<Star> neighbors = new ArrayList<Star>();
    	int gridRadius = (int)Math.ceil(distance / 30.0);
		int gridX = (int)(star.getX() / 30.0);
		int gridY = (int)(star.getY() / 30.0);
		for (int x = gridX - gridRadius; x <= gridX + gridRadius; x++) {
			for (int y = gridY - gridRadius; y <= gridY + gridRadius; y++) {
				Set<Star> grid = getStarGrid(x, y);
				if( null != grid ) {
					for( Star s : grid ) {
						if( star.getDistanceTo(s) <= distance ) {
							neighbors.add(s);
						}
					}
				}
			}
		}
		Collections.sort(neighbors, new Comparator<Star>() {
			@Override
			public int compare(Star o1, Star o2) {
				return Double.compare(star.getDistanceTo(o1), star.getDistanceTo(o2));
			}
		});
		return neighbors;
    }

	public static Planets getInstance() {
		if (planets == null) {
            planets = new Planets();
        }
        if (!planets.initialized && !planets.initializing) {
            planets.initializing = true;
            planets.loader = new Thread(new Runnable() {
            	@Override
                public void run() {
                    planets.initialize();
                }
            }, "Planet Loader");
            planets.loader.setPriority(Thread.NORM_PRIORITY - 1);
            planets.loader.start();
        }
		return planets;
	}

	private void initialize() {
		try {
			generatePlanets();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public ConcurrentMap<String, Planet> getPlanets() {
		return planetList;
	}
	
	public Planet getPlanetById(String id) {
		return( null != id ? planetList.get(id) : null);
	}
	
	public ConcurrentMap<String, Star> getStars() {
		return starList;
	}
	
	public Star getStarById(String id) {
		return( null != id ? starList.get(id) : null);
	}

	private void done() {
        initialized = true;
        initializing = false;
	}

	public boolean isInitialized() {
        return initialized;
    }

	private void updatePlanets(FileInputStream source) {
		try {
			// JAXB unmarshaller closes the stream it doesn't own. Bad JAXB. BAD.
			InputStream is = new FilterInputStream(source) {
				@Override
				public void close() { /* ignore */ }
			};
			
			LocalStarList stars = unmarshaller.unmarshal(
					new StreamSource(is), LocalStarList.class).getValue();
			
			// Run through the stars first
			for( Star star : stars.list ) {
				// Get the previous version if there is any
				Star oldStar = starList.get(star.getId());
				if( null == oldStar ) {
					starList.put(star.getId(), star);
				} else {
					// Update with new data
					oldStar.copyDataFrom(star);
				}
			}
			
			// Reset the file stream
			source.getChannel().position(0);

			LocalPlanetList planets = unmarshaller.unmarshal(
					new StreamSource(is), LocalPlanetList.class).getValue();

			// Run through the list again, this time creating and updating planets as we go
			for( Planet planet : planets.list ) {
				Planet oldPlanet = planetList.get(planet.getId());
				if( null == oldPlanet ) {
					planetList.put(planet.getId(), planet);
				} else {
					// Update with new data
					oldPlanet.copyDataFrom(planet);
					planet = oldPlanet;
				}
				// If the planet (after merging) still has a null starId, it's an "old style"
				// entry and we need to use its id for the starId too.
				if( null == planet.getStarId() ) {
					planet.setStarId(planet.getId());
				}
			}
			
			// Process planet deletions
			for( String planetId : planets.toDelete ) {
				if( null != planetId ) {
					planetList.remove(planetId);
				}
			}
			
			// Cleanup task: Make sure the planets are in the correct stars,
			// then that stars don't have wrong planets recorded
			for( Planet planet : planetList.values() ) {
				Star star = starList.get(planet.getStarId());
				if( !star.hasPlanet(planet.getId()) ) {
					star.addPlanet(planet);
				}
			}
			for( Star star : starList.values() ) {
				for( String planetId : star.getPlanetIds() ) {
					Planet planet = planetList.get(planetId);
					if( null == planet || !planet.getStarId().equals(star.getId()) ) {
						// This planet moved to a different star somehow ...
						star.removePlanet(planetId);
					}
				}
			}
			
			// We could do some validation here, like if every planet is on a proper orbit or something,
			// but right now that'd be just too much work for too little reward.
			// In the future, this could be reported as data consistency error.
		} catch (JAXBException e) {
			MekHQ.logError(e);
		} catch(IOException e) {
			MekHQ.logError(e);
		}
	}
	
	public void generatePlanets() throws DOMException, ParseException {
		MekHQ.logMessage("Starting load of planetary data from XML...");
		long currentTime = System.currentTimeMillis();
		synchronized (LOADING_LOCK) {
			// Step 1: Initialize variables.
			if( null == planetList ) {
				planetList = new ConcurrentHashMap<String, Planet>();
			}
			planetList.clear();
			if( null == starList ) {
				starList = new ConcurrentHashMap<String, Star>();
			}
			starList.clear();
			if( null == starGrid ) {
				starGrid = new HashMap<Integer, Map<Integer, Set<Star>>>();
			}
			// Be nice to the garbage collector
			for( Map.Entry<Integer, Map<Integer, Set<Star>>> starGridColumn : starGrid.entrySet() ) {
				for( Map.Entry<Integer, Set<Star>> starGridElement : starGridColumn.getValue().entrySet() ) {
					if( null != starGridElement.getValue() ) {
						starGridElement.getValue().clear();
					}
				}
				if( null != starGridColumn.getValue() ) {
					starGridColumn.getValue().clear();
				}
			}
			starGrid.clear();
			
			// Step 2: Read the default file
			try(FileInputStream fis = new FileInputStream(MekHQ.getPreference(MekHQ.DATA_DIR) + "/universe/planets.xml")) {
				updatePlanets(fis);
			} catch (Exception ex) {
				MekHQ.logError(ex);
			}
			
			// Step 3: Load all the xml files within the planets subdirectory, if it exists
			Utilities.parseXMLFiles(MekHQ.getPreference(MekHQ.DATA_DIR) + "/universe/planets",
					new FileParser() {
						@Override
						public void parse(FileInputStream is) {
							updatePlanets(is);
						}
					});
			
			for (Star s : starList.values()) {
				int x = (int)(s.getX()/30.0);
				int y = (int)(s.getY()/30.0);
				if (starGrid.get(x) == null) {
					starGrid.put(x, new HashMap<Integer, Set<Star>>());
				}
				if (starGrid.get(x).get(y) == null) {
					starGrid.get(x).put(y, new HashSet<Star>());
				}
				if( !starGrid.get(x).get(y).contains(s) ) {
					starGrid.get(x).get(y).add(s);
				}
			}
			done();
		}
		MekHQ.logMessage(String.format("Loaded a total of %d stars and %d planets in %.2fs.",
				starList.size(), planetList.size(), (System.currentTimeMillis() - currentTime) / 1000.0));
	}
	
	public void writeStar(OutputStream out, Star star) {
		writeStar(out, star, false);
	}

	public void writeStar(OutputStream out, Star star, boolean includePlanets) {
		try {
			marshaller.marshal(star, out);
			if( includePlanets ) {
				for( Planet planet : star.getPlanets() ) {
					marshaller.marshal(planet, out);
				}
			}
		} catch (Exception e) {
			MekHQ.logError(e);
		}
	}

	public void writePlanet(OutputStream out, Planet planet) {
		try {
			marshaller.marshal(planet, out);
		} catch (Exception e) {
			MekHQ.logError(e);
		}
	}
	
	@XmlRootElement(name="planets")
	private static final class LocalPlanetList {
		@XmlElement(name="planet")
		public List<Planet> list;
		
		@XmlTransient
		public List<String> toDelete;
		
		// Ignore <star> entries
		@XmlAnyElement(lax=true)
		public List<Object> others;
		
		@SuppressWarnings("unused")
		private void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
			toDelete = new ArrayList<String>();
			if( null == list ) {
				list = new ArrayList<Planet>();
			} else {
				// Fill in the "toDelete" list
				List<Planet> filteredList = new ArrayList<Planet>(list.size());
				for( Planet planet : list ) {
					if( null != planet.delete && planet.delete && null != planet.getId() ) {
						toDelete.add(planet.getId());
					} else {
						filteredList.add(planet);
					}
				}
				list = filteredList;
			}
		}
	}
	
	@XmlRootElement(name="planets")
	private static final class LocalStarList {
		@XmlElement(name="planet")
		@XmlJavaTypeAdapter(ObsoleteStarAdapter.class)
		public List<Star> obsoleteList;
		@XmlElement(name="star")
		public List<Star> list;
		
		@SuppressWarnings("unused")
		private void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
			List<Star> finalList = new ArrayList<Star>();
			if( null != list ) {
				for( Star star : list ) {
					if( null != star ) {
						finalList.add(star);
					}
				}
				list.clear();
			}
			if( null != obsoleteList ) {
				for( Star star : obsoleteList ) {
					if( null != star ) {
						finalList.add(star);
					}
				}
				obsoleteList.clear();
				obsoleteList = new ArrayList<Star>();
			}
			list = finalList;
		}
	}

	/*
	public static Planet createNewSystem() {
	    Planet planet = new Planet();
	    planet.setSpectralClass(Planet.generateStarType());
	    planet.setSubtype(Planet.generateSubtype());
	    int slots = Planet.calculateNumberOfSlots();
	    for (int i = 0; i < slots; i++) {
	    }
	    return planet;
	}
	*/
}