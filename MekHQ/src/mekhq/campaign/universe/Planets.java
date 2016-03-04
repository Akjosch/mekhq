package mekhq.campaign.universe;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.w3c.dom.DOMException;

import mekhq.MekHQ;
import mekhq.adapters.PlanetAdapter;
import mekhq.adapters.StarAdapter;

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

	private void updatePlanets(InputStream source) {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(PlanetListXMLData.class, StarXMLData.class, PlanetXMLData.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			PlanetListXMLData list = (PlanetListXMLData)jaxbUnmarshaller.unmarshal(source);
			
			// Run through the explicitly defined stars first
			for( Object obj : list.objects ) {
				if( obj instanceof StarXMLData ) {
					Star star = Star.getStarFromXMLData((StarXMLData)obj);
					// Get the previous version if there is any
					Star oldStar = starList.get(star.getId());
					if( null == oldStar ) {
						starList.put(star.getId(), star);
					} else {
						// Update with new data
						oldStar.copyDataFrom(star);
					}
				}
			}
			
			// Run through the list again, this time creating and updating planets as we go
			for( Object obj : list.objects ) {
				if( obj instanceof PlanetXMLData ) {
					Planet planet = Planet.getPlanetFromXMLData((PlanetXMLData)obj);

					// First check if we aren't supposed to delete this one
					Boolean deleteThisPlanet = ((PlanetXMLData)obj).delete;
					if( null != deleteThisPlanet && deleteThisPlanet ) {
						planetList.remove(planet.getId());
						continue;
					}

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
					// Check if we need to create a star from the planet data (old-style planetary info)
					if( !starList.containsKey(planet.getStarId()) ) {
						Star star = Star.getStarFromXMLData((PlanetXMLData)obj);
						starList.put(star.getId(), star);
					}
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
		}
	}
	
	public void generatePlanets() throws DOMException, ParseException {
		MekHQ.logMessage("Starting load of planetary data from XML...");
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
			try {
				FileInputStream fis = new FileInputStream(MekHQ.getPreference(MekHQ.DATA_DIR) + "/universe/planets.xml");
				updatePlanets(fis);
				fis.close();
			} catch (Exception ex) {
				MekHQ.logError(ex);
			}
			
			// Step 3: Load all the xml files within the planets subdirectory, if it exists
			File planetDir = new File(MekHQ.getPreference(MekHQ.DATA_DIR) + "/universe/planets");
			if( planetDir.isDirectory() ) {
				File[] planetFiles = planetDir.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.toLowerCase(Locale.ROOT).endsWith(".xml");
					}
				});
				if( null != planetFiles && planetFiles.length > 0 ) {
					// Case-insensitive sorting. Yes, even on Windows. Deal with it.
					Arrays.sort(planetFiles, new Comparator<File>() {
						@Override
						public int compare(File f1, File f2) {
							return f1.getPath().compareTo(f2.getPath());
						}
					});
					// Try parsing and updating the main list, one by one
					for( File planetFile : planetFiles ) {
						try {
							FileInputStream fis = new FileInputStream(planetFile);
							updatePlanets(fis);
						} catch(Exception ex) {
							// Ignore this file then
							MekHQ.logError("Exception trying to parse " + planetFile.getPath() + " - ignoring.");
							MekHQ.logError(ex);
						}
					}
				}
			}
			
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
		MekHQ.logMessage("Loaded a total of " + starList.size() + " stars and " + planetList.size() + " planets");
	}
	
	public void writeStar(OutputStream out, Star star) {
		writeStar(out, star, false);
	}

	public void writeStar(OutputStream out, Star star, boolean includePlanets) {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(PlanetListXMLData.class, StarXMLData.class, PlanetXMLData.class, Planet.class, Star.class);
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty("jaxb.fragment", Boolean.TRUE);
			marshaller.marshal(new StarAdapter().marshal(star), out);
			if( includePlanets ) {
				for( Planet planet : star.getPlanets() ) {
					marshaller.marshal(new PlanetAdapter().marshal(planet), out);
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void writePlanet(OutputStream out, Planet planet) {
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(PlanetListXMLData.class, StarXMLData.class, PlanetXMLData.class, Planet.class, Star.class);
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty("jaxb.fragment", Boolean.TRUE);
			marshaller.marshal(new PlanetAdapter().marshal(planet), out);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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