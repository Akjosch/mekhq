package mekhq.campaign.personnel;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.IntUnaryOperator;

import mekhq.campaign.Campaign;
import mekhq.campaign.LogEntry;
import mekhq.campaign.mod.am.BodyLocation;

/**
 * Default injury types. Custom ones can extend this class and register their own.
 */
public class InjuryType {
    // Predefined types
    public static final InjuryType CUT = new InjuryType(0, 0) {
        @Override
        public String getFluffText(BodyLocation loc, int severity, int gender) {
            return "Some cuts on " + Person.getGenderPronoun(gender, Person.PRONOUN_HISHER) + " "
                + loc.readableName;
        }
    };
    public static final InjuryType BRUISE = new InjuryType(1, 0) {
        @Override
        public String getFluffText(BodyLocation loc, int severity, int gender) {
            return "A bruise on " + Person.getGenderPronoun(gender, Person.PRONOUN_HISHER) + " "
                + loc.readableName;
        }
    };
    public static final InjuryType LACERATION = new InjuryType(2, 0) {
        @Override
        public String getFluffText(BodyLocation loc, int severity, int gender) {
            return "A laceration on " + Person.getGenderPronoun(gender, Person.PRONOUN_HISHER) + " head";
        }
    };
    public static final InjuryType SPRAIN = new InjuryType(3, 12) {
        @Override
        public String getFluffText(BodyLocation loc, int severity, int gender) {
            return "A sprained " + loc.readableName;
        }
    };
    public static final InjuryType CONCUSSION = new InjuryType(4, 14) {
        @Override
        public int getRecoveryTime(int severity) {
            return severity >= 2 ? 42 : 14;
        }
        
        @Override
        public List<InjuryAction> genStressEffect(Campaign c, Person p, Injury i, int hits) {
            String secondEffectFluff = (i.getSeverity() == 1)
                ? "concussion worsening" : "development of a cerebral contusion";
            if(hits < 5) {
                int worseningChance = Math.max((int) Math.round((1 + hits) * 100.0 / 6.0), 100);
                secondEffectFluff = worseningChance + "% chance of " + secondEffectFluff;
            }
            return Arrays.asList(
                newResetRecoveryTimeAction(i),
                new InjuryAction(
                    secondEffectFluff,
                    (rnd, gen) -> {
                        if(rnd.applyAsInt(6) + hits >= 5) {
                            if(i.getSeverity() == 1) {
                                i.setSeverity(2);
                            } else {
                                Injury cerebralContusion = gen.gen(BodyLocation.HEAD, CEREBRAL_CONTUSION, 1);
                                p.addInjury(cerebralContusion);
                                p.removeInjury(i);
                                p.addLogEntry(new LogEntry(c.getDate(), "Developed a cerebral contusion"));
                            }
                        }
                    })
                );
        }
    };
    public static final InjuryType BROKEN_RIB = new InjuryType(5, 20) {
        @Override
        public List<InjuryAction> genStressEffect(Campaign c, Person p, Injury i, int hits) {
            return Arrays.asList(new InjuryAction(
                "1% chance of death; 9% chance of puncturing a lung",
                (rnd, gen) -> {
                    int rib = rnd.applyAsInt(100);
                    if(rib < 1) {
                        p.changeStatus(Person.S_KIA);
                        p.addLogEntry(new LogEntry(c.getDate(), "Had a broken rib puncturing "
                            + Person.getGenderPronoun(p.getGender(), Person.PRONOUN_HISHER) + " heart, dying"));
                    } else if(rib < 10) {
                        Injury puncturedLung = gen.gen(BodyLocation.CHEST, PUNCTURED_LUNG, 1);
                        p.addInjury(puncturedLung);
                        p.addLogEntry(new LogEntry(c.getDate(), "Had a broken rib puncturing "
                            + Person.getGenderPronoun(p.getGender(), Person.PRONOUN_HISHER) + " lung"));
                    }
                }));
        }
    };
    public static final InjuryType BRUISED_KIDNEY = new InjuryType(6, 10) {
        @Override
        public List<InjuryAction> genStressEffect(Campaign c, Person p, Injury i, int hits) {
            return Arrays.asList(new InjuryAction(
                "10% chance of internal bleeding",
                (rnd, gen) -> {
                    if(rnd.applyAsInt(100) < 10) {
                        Injury bleeding = gen.gen(BodyLocation.ABDOMEN, INTERNAL_BLEEDING, 1);
                        p.addInjury(bleeding);
                        p.addLogEntry(new LogEntry(c.getDate(), "Had a broken rib puncturing "
                            + Person.getGenderPronoun(p.getGender(), Person.PRONOUN_HISHER) + " lung"));
                    }
                }));
        }
    };
    public static final InjuryType BROKEN_LIMB = new InjuryType(7, 30) {
        @Override
        public String getFluffText(BodyLocation loc, int severity, int gender) {
            return "A broken " + loc.readableName;
        }
        
        @Override
        public List<InjuryAction> genStressEffect(Campaign c, Person p, Injury i, int hits) {
            return Arrays.asList(newResetRecoveryTimeAction(i));
        }
    };
    public static final InjuryType BROKEN_COLLAR_BONE = new InjuryType(8, 22) {
        @Override
        public List<InjuryAction> genStressEffect(Campaign c, Person p, Injury i, int hits) {
            return Arrays.asList(newResetRecoveryTimeAction(i));
        }
    };
    public static final InjuryType INTERNAL_BLEEDING = new InjuryType(9, 20) {
        @Override
        public int getRecoveryTime(int severity) {
            return 20 * severity;
        }
        
        @Override
        public String getFluffText(BodyLocation loc, int severity, int gender) {
            switch(severity) {
                case 2: return "Severe internal bleeding";
                case 3: return "Critical internal bleeding";
                default: return "Internal bleeding";
            }
        }

        @Override
        public List<InjuryAction> genStressEffect(Campaign c, Person p, Injury i, int hits) {
            String secondEffectFluff = (i.getSeverity() < 3)
                ? "internal bleeding worsening" : "death";
            if(hits < 5) {
                int worseningChance = Math.max((int) Math.round((1 + hits) * 100.0 / 6.0), 100);
                secondEffectFluff = worseningChance + "% chance of " + secondEffectFluff;
            }
            if(hits >= 5 && i.getSeverity() >= 3) {
                // Don't even bother doing anything else; we're dead
                return Arrays.asList(
                    new InjuryAction(
                        "certain death",
                        (rnd, gen) -> {
                            p.setStatus(Person.S_KIA);
                            p.addLogEntry(new LogEntry(c.getDate(), "Died of critical internal bleeding"));
                        })
                    );
            } else {
                // We have a chance!
                return Arrays.asList(
                    newResetRecoveryTimeAction(i),
                    new InjuryAction(
                        secondEffectFluff,
                        (rnd, gen) -> {
                            if(rnd.applyAsInt(6) + hits >= 5) {
                                if(i.getSeverity() < 3) {
                                    i.setSeverity(i.getSeverity() + 1);
                                } else {
                                    p.setStatus(Person.S_KIA);
                                    p.addLogEntry(new LogEntry(c.getDate(), "Died of critical internal bleeding"));
                                }
                            }
                        })
                    );
            }
        }
    };
    public static final InjuryType LOST_LIMB = new InjuryType(10, 28) {
        @Override
        public String getFluffText(BodyLocation loc, int severity, int gender) {
            return "Lost " + Person.getGenderPronoun(gender, Person.PRONOUN_HISHER) + " "
                + loc.readableName;
        }
    };
    public static final InjuryType CEREBRAL_CONTUSION = new InjuryType(11, 90) {
        @Override
        public List<InjuryAction> genStressEffect(Campaign c, Person p, Injury i, int hits) {
            String secondEffectFluff = "development of a chronic traumatic encephalopathy";
            if(hits < 5) {
                int worseningChance = Math.max((int) Math.round((1 + hits) * 100.0 / 6.0), 100);
                secondEffectFluff = worseningChance + "% chance of " + secondEffectFluff;
            }
            return Arrays.asList(
                newResetRecoveryTimeAction(i),
                new InjuryAction(
                    secondEffectFluff,
                    (rnd, gen) -> {
                        if(rnd.applyAsInt(6) + hits >= 5) {
                            Injury cte = gen.gen(BodyLocation.HEAD, CTE, 1);
                            p.addInjury(cte);
                            p.removeInjury(i);
                            p.addLogEntry(new LogEntry(c.getDate(), "Developed a chronic traumatic encephalopathy"));
                        }
                    })
                );
        }
    };
    public static final InjuryType PUNCTURED_LUNG = new InjuryType(12, 20) {
        @Override
        public List<InjuryAction> genStressEffect(Campaign c, Person p, Injury i, int hits) {
            return Arrays.asList(newResetRecoveryTimeAction(i));
        }
    };
    public static final InjuryType CTE = new InjuryType(13, 180) {
        @Override
        public List<InjuryAction> genStressEffect(Campaign c, Person p, Injury i, int hits) {
            int deathchance = Math.max((int) Math.round((1 + hits) * 100.0 / 6.0), 100);
            if(hits > 4) {
                return Arrays.asList(
                    new InjuryAction(
                        "certain death",
                        (rnd, gen) -> {
                            p.setStatus(Person.S_KIA);
                            p.addLogEntry(new LogEntry(c.getDate(), "Died due to brain trauma"));
                        }));
            } else {
                // We have a chance!
                return Arrays.asList(
                    newResetRecoveryTimeAction(i),
                    new InjuryAction(
                        deathchance + "% chance of death",
                        (rnd, gen) -> {
                            if(rnd.applyAsInt(6) + hits >= 5) {
                                p.setStatus(Person.S_KIA);
                                p.addLogEntry(new LogEntry(c.getDate(), "Died due to brain trauma"));
                        }
                    }));
            }
        }
    };
    public static final InjuryType BROKEN_BACK = new InjuryType(14, 150) {
        @Override
        public List<InjuryAction> genStressEffect(Campaign c, Person p, Injury i, int hits) {
            return Arrays.asList(new InjuryAction(
                "20% chance of severing the spine, permanently paralizing the character",
                (rnd, gen) -> {
                    if(rnd.applyAsInt(100) < 20) {
                        Injury severedSpine = gen.gen(BodyLocation.CHEST, SEVERED_SPINE, 1);
                        p.addInjury(severedSpine);
                        p.addLogEntry(new LogEntry(c.getDate(), "Severed " + Person.getGenderPronoun(p.getGender(), Person.PRONOUN_HISHER)
                            + " spine, leaving " + Person.getGenderPronoun(p.getGender(), Person.PRONOUN_HIMHER)
                            + " paralyzed"));
                    }
                }));
        }
    };
    // New injury types go here (or extend the class)
    public static final InjuryType SEVERED_SPINE = new InjuryType(15, 180) {
        @Override
        public String getFluffText(BodyLocation loc, int severity, int gender) {
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
