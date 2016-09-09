package mekhq.campaign.personnel;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.IntUnaryOperator;

import mekhq.Utilities;
import mekhq.campaign.Campaign;
import mekhq.campaign.mod.am.BodyLocation;

public class InjuryType {
    // Registry methods
    private static final Map<String, InjuryType> REGISTRY = new HashMap<>();
    private static final Map<InjuryType, String> REV_REGISTRY = new HashMap<>();
    private static final Map<Integer, InjuryType> ID_REGISTRY = new HashMap<>();
    private static final Map<InjuryType, Integer> REV_ID_REGISTRY = new HashMap<>();
    
    public static InjuryType byKey(String key) {
        InjuryType result = REGISTRY.get(key);
        if(null == result) {
            try {
                result = ID_REGISTRY.get(Integer.valueOf(key));
            } catch(NumberFormatException nfex) {
                // Do nothing
            }
        }
        return result;
    }
    
    public static InjuryType byId(int id) {
        return ID_REGISTRY.get(Integer.valueOf(id));
    }
    
    public static void register(int id, String key, InjuryType injType) {
        Objects.requireNonNull(injType);
        if(id >= 0) {
            if(ID_REGISTRY.containsKey(Integer.valueOf(id))) {
                throw new IllegalArgumentException("Injury type ID " + id + " is already registered.");
            }
        }
        if(REGISTRY.containsKey(Objects.requireNonNull(key))) {
            throw new IllegalArgumentException("Injury type key \"" + key + "\" is already registered.");
        }
        if(key.isEmpty()) {
            throw new IllegalArgumentException("Injury type key can't be an empty string.");
        }
        if(REV_REGISTRY.containsKey(injType)) {
            throw new IllegalArgumentException("Injury type " + injType + " is already registered");
        }
        // All checks done
        if(id >= 0) {
            ID_REGISTRY.put(Integer.valueOf(id), injType);
            REV_ID_REGISTRY.put(injType, Integer.valueOf(id));
        }
        REGISTRY.put(key, injType);
        REV_REGISTRY.put(injType, key);
    }
    
    public static void register(String key, InjuryType injType) {
        register(-1, key, injType);
    }
    
    /** Default injury type: reduction in hit points */
    public static InjuryType BAD_HEALTH = new InjuryType();
    static {
        BAD_HEALTH.recoveryTime = 7;
        BAD_HEALTH.fluffText = "Damaged health";
        BAD_HEALTH.maxSeverity = 5;
        BAD_HEALTH.allowedLocations = EnumSet.of(BodyLocation.GENERIC);
        register("bad_health", BAD_HEALTH);
    }
    
    /** Base recovery time in days */
    protected int recoveryTime = 0;
    protected boolean permanent = false;
    protected int maxSeverity = 1;
    protected String fluffText = "";
    
    protected Set<BodyLocation> allowedLocations = EnumSet.allOf(BodyLocation.class);
    
    protected InjuryType() {
    }
    
    public final int getId() {
        return Utilities.nonNull(InjuryType.REV_ID_REGISTRY.get(this), Integer.valueOf(-1)).intValue();
    }
    
    public final String getKey() {
        return InjuryType.REV_REGISTRY.get(this);
    }
    
    public boolean isValidInLocation(BodyLocation loc) {
        return allowedLocations.contains(loc);
    }
    
    public int getBaseRecoveryTime() {
        return recoveryTime;
    }
    
    public int getRecoveryTime(int severity) {
        return recoveryTime;
    }
    
    public int getRecoveryTime(Injury i) {
        return getRecoveryTime(i.getSeverity());
    }
    
    public boolean isPermanent() {
        return permanent;
    }
    
    public int getMaxSeverity() {
        return maxSeverity;
    }
    
    public String getName(BodyLocation loc, int severity) {
        return Utilities.capitalize(fluffText);
    }
    
    public String getFluffText(BodyLocation loc, int severity, int gender) {
        return fluffText;
    }
    
    /**
     * Return a function which will generate a list of effects combat and similar stressful
     * situation while injured would have on the person in question given the random integer source.
     * Descriptions should be something like "50% chance of losing a leg" and similar.
     * <p>
     * Note that specific systems aren't required to use this generator. They are free to
     * implement their own.
     */
    public List<InjuryAction> genStressEffect(Campaign c, Person p, Injury i, int hits) {
        return Arrays.asList();
    }

    // Standard actions generators
    
    protected InjuryAction newResetRecoveryTimeAction(Injury i) {
        return new InjuryAction(
            i.getFluff() + ": recovery timer reset",
            (rnd, gen) -> {
                i.setTime(i.getOriginalTime());
            });
    }

    // Helper classes and interfaces
    
    /** Why you no have this in java.util.function?!? */
    @FunctionalInterface
    public static interface ToBooleanFunction<T> {
        boolean applyAsBoolean(T value);
    }
    
    @FunctionalInterface
    public static interface InjuryProducer {
        Injury gen(BodyLocation loc, InjuryType type, int severity);
    }
    
    public static final class InjuryAction {
        public String desc;
        public final BiConsumer<IntUnaryOperator, InjuryProducer> action;
        
        public InjuryAction(String desc, BiConsumer<IntUnaryOperator, InjuryProducer> action) {
            this.desc = desc;
            this.action = action;
        }
    }
}
