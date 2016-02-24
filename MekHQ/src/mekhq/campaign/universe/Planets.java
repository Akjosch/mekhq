package mekhq.campaign.universe;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import mekhq.MekHQ;

public class Planets {

	private boolean initialized = false;
	private boolean initializing = false;
	private static Planets planets;
	private static ConcurrentMap<String, Planet> planetList = new ConcurrentHashMap<String, Planet>();
	private static ConcurrentMap<String, Star> starList = new ConcurrentHashMap<String, Star>();
 	private static HashMap<Integer, HashMap<Integer,ArrayList<Star>>> starGrid;
	/*organizes systems into a grid of 30lyx30ly squares so we can find
	 * nearby systems without iterating through the entire planet list*/
    private Thread loader;


    private Planets() {
        planetList = new ConcurrentHashMap<String, Planet>();
        starList = new ConcurrentHashMap<String, Star>();
		starGrid = new HashMap<Integer,HashMap<Integer,ArrayList<Star>>>();
   }

    private static List<Star> getStarGrid(int x, int y) {
    	if( !starGrid.containsKey(x) ) {
    		return null;
    	}
    	return starGrid.get(x).get(y);
    }
    
    public static ArrayList<String> getNearbyStars(Planet p, int distance) {
    	return getNearbyStars(p.getStar(), distance);
    }
    
    public static ArrayList<String> getNearbyStars(Star star, int distance) {
    	ArrayList<String> neighbors = new ArrayList<String>();
    	int gridRadius = (int)Math.ceil(distance / 30.0);
		int gridX = (int)(star.getX() / 30.0);
		int gridY = (int)(star.getY() / 30.0);
		for (int x = gridX - gridRadius; x <= gridX + gridRadius; x++) {
			for (int y = gridY - gridRadius; y <= gridY + gridRadius; y++) {
				List<Star> grid = getStarGrid(x, y);
				if( null != grid ) {
					for( Star s : grid ) {
						if( star.getDistanceTo(s) <= distance ) {
							neighbors.add(s.getId());
						}
					}
				}
			}
		}
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
			planetList = generatePlanets();
			starList = new ConcurrentHashMap<String,Star>();
			for( Planet planet : planetList.values() ) {
				starList.put(planet.getStar().getId(), planet.getStar());
			}
		} catch (DOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected ConcurrentMap<String,Planet> getPlanets() {
		return planetList;
	}
	
	public Planet getPlanetById(String id) {
		return( null != id ? planetList.get(id) : null);
	}
	
	public ConcurrentMap<String,Star> getStars() {
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

	public  ConcurrentMap<String,Planet> generatePlanets() throws DOMException, ParseException {
		MekHQ.logMessage("Starting load of planetary data from XML...");
		// Initialize variables.
		ConcurrentMap<String,Planet> retVal = new ConcurrentHashMap<String,Planet>();
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		Document xmlDoc = null;

		// Step 1: Load the default planets.xml
		try {
			FileInputStream fis = new FileInputStream(MekHQ.getPreference(MekHQ.DATA_DIR) + "/universe/planets.xml");
			// Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			// Parse using builder to get DOM representation of the XML file
			xmlDoc = db.parse(fis);
		} catch (Exception ex) {
			MekHQ.logError(ex);
		}

		Element planetEle = xmlDoc.getDocumentElement();
		NodeList nl = planetEle.getChildNodes();

		// Get rid of empty text nodes and adjacent text nodes...
		// Stupid weird parsing of XML.  At least this cleans it up.
		planetEle.normalize();

		// Okay, lets iterate through the children, eh?
		for (int x = 0; x < nl.getLength(); x++) {
			Node wn = nl.item(x);

			if (wn.getParentNode() != planetEle)
				continue;

			int xc = wn.getNodeType();

			if (xc == Node.ELEMENT_NODE) {
				// This is what we really care about.
				// All the meat of our document is in this node type, at this
				// level.
				// Okay, so what element is it?
				String xn = wn.getNodeName();

				if (xn.equalsIgnoreCase("planet")) {
					Planet p = Planet.getPlanetFromXML(wn);
					if(null == p.getBaseFactions().get(0)) {
						MekHQ.logMessage("The base factions are null for planet " + p.getName());
					}
					String name = p.getName();
					if(null == retVal.get(name)) {
						retVal.put(name, p);
					} else {
						//for duplicate planets, put a faction name behind them
						//There could still be duplicates in theory, but I don't think there are in practice
						Planet oldPlanet = retVal.get(name);
						retVal.remove(name);
						oldPlanet.resetName(oldPlanet.getName() + " (" + oldPlanet.getBaseFactions().get(0).getFullName(Era.E_AOW) + ")");
						retVal.put(oldPlanet.getName(), oldPlanet);
						p.resetName(p.getName() + " (" + p.getBaseFactions().get(0).getFullName(Era.E_AOW) + ")");
						retVal.put(p.getName(), p);
					}

				}
			}
		}
		
		// Step 2: Load all the xml files within the planets subdirectory, if it exists
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
						DocumentBuilder db = dbf.newDocumentBuilder();
						xmlDoc = db.parse(fis);
						planetEle = xmlDoc.getDocumentElement();
						nl = planetEle.getChildNodes();
						planetEle.normalize();
						for (int x = 0; x < nl.getLength(); x++) {
							Node wn = nl.item(x);
							if (wn.getParentNode() != planetEle) {
								continue;
							}
	
							int xc = wn.getNodeType();
							if (xc == Node.ELEMENT_NODE) {
								// TODO: Read the star and planet data here and update if the ids match
							}
						}
					} catch(Exception ex) {
						// Ignore this file then
						MekHQ.logError("Exception trying to parse " + planetFile.getPath() + " - ignoring.");
						MekHQ.logError(ex);
					}
				}
			}
		}
		
		for (Planet p : retVal.values()) {
			int x = (int)(p.getX()/30.0);
			int y = (int)(p.getY()/30.0);
			if (starGrid.get(x) == null) {
				starGrid.put(x, new HashMap<Integer,ArrayList<Star>>());
			}
			if (starGrid.get(x).get(y) == null) {
				starGrid.get(x).put(y, new ArrayList<Star>());
			}
			if( !starGrid.get(x).get(y).contains(p.getStar()) ) {
				starGrid.get(x).get(y).add(p.getStar());
			}
		}
		MekHQ.logMessage("Loaded a total of " + retVal.size() + " planets");
		done();
		return retVal;
	}

	public static Planet createNewSystem() {
	    Planet planet = new Planet();
	    planet.setSpectralClass(Planet.generateStarType());
	    planet.setSubtype(Planet.generateSubtype());
	    int slots = Planet.calculateNumberOfSlots();
	    for (int i = 0; i < slots; i++) {
	    }
	    return planet;
	}
}