package mekhq.campaign.universe;

import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import mekhq.campaign.universe.Planet.PlanetaryType;
import mekhq.campaign.universe.planetaryclass.*;

/** Big varied list of planetary classes, several per type */
public abstract class PlanetaryClass {
	private static Object REGISTRY_LOCK = new Object[0];
	
	public static ConcurrentMap<String, PlanetaryClass> registry;
	public static ConcurrentMap<String, String> aliases;
	
	private static void initRegistry() {
		synchronized(REGISTRY_LOCK) {
			if( null == registry ) {
				registry = new ConcurrentHashMap<String, PlanetaryClass>();
				// Defaults
				for( PlanetaryClass pc : Arrays.asList(
						// Random rock
						new MinorAsteroid(),
						// Minor planets and big asteroids
						new CarbonaceousPlanetoid(), new IcePlanetoid(), new SilicatePlanetoid(),
						new MetallicPlanetoid(), new GravelPlanetoid(), new SulfurPlanetoid(),
						// Terrestrials
						new DesertPlanet(), new EarthlikePlanet(), new GreenhousePlanet(), new HellPlanet(),
						new FrozenRockPlanet(), new AirlessRockPlanet(), new DryRockPlanet(),
						new RockyCorePlanet(), new WaterIcePlanet(), new AmmoniaIcePlanet(),
						new MethaneIcePlanet(), new IronPlanet(), new LavaPlanet(), new OceanPlanet(),
						// Giant terrestrials
						new CthonianPlanet(), new GasDwarf(), new HighPressurePlanet(),
						new ExtremeGreenhousePlanet(),
						// Gas giants
						new GasGiantAmmonia(), new GasGiantWater(), new GasGiantCloudless(),
						new GasGiantAlkali(), new GasGiantSilicate(), new ColdPuffyGiant(),
						new HotPuffyGiant(), new BoilingGiant(),
						// Ice giants
						new IceGiant()
						) ) {
					registry.put(pc.id.toUpperCase(Locale.ROOT), pc);
				}
				aliases = new ConcurrentHashMap<String, String>();
			}
		}
	}
	
	public static void registerPlanetaryClass(PlanetaryClass pc) {
		initRegistry();
		String id = pc.id.toUpperCase(Locale.ROOT);
		synchronized(REGISTRY_LOCK) {
			registry.putIfAbsent(id, pc);
			if( aliases.containsKey(id) ) {
				aliases.remove(id);
			}
		}
	}
	
	public static void registerPlanetaryAlias(String aliasId, String baseId) {
		aliasId = aliasId.toUpperCase(Locale.ROOT);
		baseId = baseId.toUpperCase(Locale.ROOT);
		synchronized(REGISTRY_LOCK) {
			// Don't register if we already have a "real" planetary class
			if( !registry.containsKey(aliasId) ) {
				// We don't care if the base ID exists at this point
				aliases.put(aliasId, baseId);
			}
		}
	}
	
	public static PlanetaryClass get(String id) {
		initRegistry();
		id = id.toUpperCase(Locale.ROOT);
		PlanetaryClass result = null;
		synchronized(REGISTRY_LOCK) {
			result = registry.get(id);
			if( null == result && null != aliases.get(id) ) {
				result = registry.get(aliases.get(id));
			}
		}
		return result;
	}

	public final PlanetaryType type;
	private final String id;
	public final String name;
	
	protected PlanetaryClass(PlanetaryType type, String id, String name) {
		this.type = type;
		this.id = id;
		this.name = name;
	}
	
	/**
	 * @param rndValue supplied random value in the range [0.0, 1.0)
	 * @return the coverage of liquid water on the surface
	 */
	public int generateWaterCoverage(Double rndValue) {
		return 0;
	}
}