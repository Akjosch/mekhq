package mekhq.campaign.personnel;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;

import mekhq.campaign.Campaign;
import mekhq.campaign.mod.am.BodyLocation;

public class InjuryType {
    // Predefined types
    public static final InjuryType CUT = new InjuryType(0, 0) {
        @Override
        public String getFluffText(BodyLocation loc, int gender) {
            return "Some cuts on " + Person.getGenderPronoun(gender, Person.PRONOUN_HISHER) + " "
                + loc.readableName;
        }
    };
    public static final InjuryType BRUISE = new InjuryType(1, 0) {
        @Override
        public String getFluffText(BodyLocation loc, int gender) {
            return "A bruise on " + Person.getGenderPronoun(gender, Person.PRONOUN_HISHER) + " "
                + loc.readableName;
        }
    };
    public static final InjuryType LACERATION = new InjuryType(2, 0) {
        @Override
        public String getFluffText(BodyLocation loc, int gender) {
            return "A laceration on " + Person.getGenderPronoun(gender, Person.PRONOUN_HISHER) + " head";
        }
    };
    public static final InjuryType SPRAIN = new InjuryType(3, 12) {
        @Override
        public String getFluffText(BodyLocation loc, int gender) {
            return "A sprained " + loc.readableName;
        }
    };
    public static final InjuryType CONCUSSION = new InjuryType(4, 14);
    public static final InjuryType BROKEN_RIB = new InjuryType(5, 20);
    public static final InjuryType BRUISED_KIDNEY = new InjuryType(6, 10);
    public static final InjuryType BROKEN_LIMB = new InjuryType(7, 30) {
        @Override
        public String getFluffText(BodyLocation loc, int gender) {
            return "A broken " + loc.readableName;
        }
    };
    public static final InjuryType BROKEN_COLLAR_BONE = new InjuryType(8, 22);
    public static final InjuryType INTERNAL_BLEEDING = new InjuryType(9, 20);
    public static final InjuryType LOST_LIMB = new InjuryType(10, 28) {
        @Override
        public String getFluffText(BodyLocation loc, int gender) {
            return "Lost " + Person.getGenderPronoun(gender, Person.PRONOUN_HISHER) + " "
                + loc.readableName;
        }
    };
    public static final InjuryType CEREBRAL_CONTUSION = new InjuryType(11, 90);
    public static final InjuryType PUNCTURED_LUNG = new InjuryType(12, 20);
    public static final InjuryType CTE = new InjuryType(13, 180);
    public static final InjuryType BROKEN_BACK = new InjuryType(14, 150);
    // New injury types go here (or extend the class)
    public static final InjuryType SEVERED_SPINE = new InjuryType(15, 180) {
        @Override
        public String getFluffText(BodyLocation loc, int gender) {
            return "A severed spine in " + ((loc == BodyLocation.CHEST) ? "upper" : "lower") + " body";
        }
    };

    // Data structures initialization
    static {
        // Location checks
        CUT.locationAllow = (loc) -> loc.isLimb || (loc == BodyLocation.CHEST) || (loc == BodyLocation.ABDOMEN);
        BRUISE.locationAllow = (loc) -> loc.isLimb || (loc == BodyLocation.CHEST) || (loc == BodyLocation.ABDOMEN);
        LACERATION.locationAllow = (loc) -> (loc == BodyLocation.HEAD);
        SPRAIN.locationAllow = (loc) -> loc.isLimb;
        CONCUSSION.locationAllow = (loc) -> (loc == BodyLocation.HEAD);
        BROKEN_RIB.locationAllow = (loc) -> (loc == BodyLocation.CHEST);
        BRUISED_KIDNEY.locationAllow = (loc) -> (loc == BodyLocation.ABDOMEN);
        BROKEN_LIMB.locationAllow = (loc) -> loc.isLimb;
        BROKEN_COLLAR_BONE.locationAllow = (loc) -> (loc == BodyLocation.CHEST);
        INTERNAL_BLEEDING.locationAllow = (loc) -> (loc == BodyLocation.ABDOMEN) || (loc == BodyLocation.INTERNAL);
        LOST_LIMB.locationAllow = (loc) -> loc.isLimb;
        CEREBRAL_CONTUSION.locationAllow = (loc) -> (loc == BodyLocation.HEAD);
        PUNCTURED_LUNG.locationAllow = (loc) -> (loc == BodyLocation.CHEST);
        CTE.locationAllow = (loc) -> (loc == BodyLocation.HEAD);
        BROKEN_BACK.locationAllow = (loc) -> (loc == BodyLocation.CHEST);
        SEVERED_SPINE.locationAllow = (loc) -> (loc == BodyLocation.CHEST) || (loc == BodyLocation.ABDOMEN);
        
        // Mark injury flags and values
        LOST_LIMB.permanent = true;
        CTE.permanent = true;
        SEVERED_SPINE.permanent = true;
        CONCUSSION.maxSeverity = 2;
        INTERNAL_BLEEDING.maxSeverity = 3;
        
        // Texts. TODO: Localization
        CONCUSSION.fluffText = "A concussion";
        BROKEN_RIB.fluffText = "A broken rib";
        BRUISED_KIDNEY.fluffText = "A bruised kidney";
        BROKEN_COLLAR_BONE.fluffText = "A broken collar bone";
        CEREBRAL_CONTUSION.fluffText = "A cerebral contusion";
        PUNCTURED_LUNG.fluffText = "A punctured lung";
        CTE.fluffText = "Chronic traumatic encephalopathy";
        BROKEN_BACK.fluffText = "A broken back";
    }

    public final int id;
    /** Base recovery time in days */
    private final int recoveryTime;
    private boolean permanent = false;
    private int maxSeverity = 1;
    private String fluffText = "";
    
    private ToBooleanFunction<BodyLocation> locationAllow = (loc) -> true;
    
    private InjuryType(int id, int recoveryTime) {
        this.id = id;
        this.recoveryTime = recoveryTime;
    }
    
    public boolean isAllowedInLocation(BodyLocation loc) {
        return locationAllow.applyAsBoolean(loc);
    }
    
    public int getRecoveryTime() {
        return recoveryTime;
    }
    
    public boolean isPermanent() {
        return permanent;
    }
    
    public int getMaxSeverity() {
        return maxSeverity;
    }
    
    public String getFluffText(BodyLocation loc, int gender) {
        return fluffText;
    }
    
    /**
     * Return a function which will generate a list of effects combat while injured would have
     * on the person in question given the random integer source. Descriptions should be
     * something like "50% chance of losing a leg" and similar.
     * <p>
     * Note that specific systems aren't required to use this generator. They are free to
     * implement their own.
     */
    public Function<IntUnaryOperator, List<InjuryAction>> genCombatEffect(Campaign c, Person p, Injury i, int hits) {
        return (randomizer) -> Arrays.asList();
    }
    
    /** Why you no have this in java.util.function?!? */
    @FunctionalInterface
    public static interface ToBooleanFunction<T> {
        boolean applyAsBoolean(T value);
    }
    
    public static final class InjuryAction {
        String desc;
        Runnable action;
    }
}
