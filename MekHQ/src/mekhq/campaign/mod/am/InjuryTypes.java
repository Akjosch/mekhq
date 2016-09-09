/*
 * Copyright (C) 2016 MegaMek team
 *
 * This file is part of MekHQ.
 *
 * MekHQ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 * 
 * MekHQ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MekHQ.  If not, see <http://www.gnu.org/licenses/>.
 */
package mekhq.campaign.mod.am;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import mekhq.Utilities;
import mekhq.campaign.Campaign;
import mekhq.campaign.LogEntry;
import mekhq.campaign.personnel.BodyLocation;
import mekhq.campaign.personnel.Injury;
import mekhq.campaign.personnel.InjuryType;
import mekhq.campaign.personnel.Person;

public class InjuryTypes {
    // Predefined types
    public static final InjuryType CUT = new InjuryTypes.Cut();
    public static final InjuryType BRUISE = new InjuryTypes.Bruise();
    public static final InjuryType LACERATION = new InjuryTypes.Laceration();
    public static final InjuryType SPRAIN = new InjuryTypes.Sprain();
    public static final InjuryType CONCUSSION = new InjuryTypes.Concussion();
    public static final InjuryType BROKEN_RIB = new InjuryTypes.BrokenRib();
    public static final InjuryType BRUISED_KIDNEY = new InjuryTypes.BruisedKidney();
    public static final InjuryType BROKEN_LIMB = new InjuryTypes.BrokenLimb();
    public static final InjuryType BROKEN_COLLAR_BONE = new InjuryTypes.BrokenCollarBone();
    public static final InjuryType INTERNAL_BLEEDING = new InjuryTypes.InternalBleeding();
    public static final InjuryType LOST_LIMB = new InjuryTypes.LostLimb();
    public static final InjuryType CEREBRAL_CONTUSION = new InjuryTypes.CerebralContusion();
    public static final InjuryType PUNCTURED_LUNG = new InjuryTypes.PuncturedLung();
    public static final InjuryType CTE = new InjuryTypes.Cte();
    public static final InjuryType BROKEN_BACK = new InjuryTypes.BrokenBack();
    // New injury types go here (or extend the class)
    public static final InjuryType SEVERED_SPINE = new InjuryTypes.SeveredSpine();

    /** Register all injury types defined here. Don't use them until you called this once! */
    public static void registerAll() {
        InjuryType.register(0, "am:cut", CUT);
        InjuryType.register(1, "am:bruise", BRUISE);
        InjuryType.register(2, "am:sprain", SPRAIN);
        InjuryType.register(3, "am:concussion", CONCUSSION);
        InjuryType.register(4, "am:broken_rib", BROKEN_RIB);
        InjuryType.register(5, "am:bruised_kidney", BRUISED_KIDNEY);
        InjuryType.register(6, "am:broken_limb", BROKEN_LIMB);
        InjuryType.register(7, "am:broken_collar_bone", BROKEN_COLLAR_BONE);
        InjuryType.register(8, "am:internal_bleeding", INTERNAL_BLEEDING);
        InjuryType.register(9, "am:lost_limb", LOST_LIMB);
        InjuryType.register(10, "am:cerebral_contusion", CEREBRAL_CONTUSION);
        InjuryType.register(11, "am:punctured_lung", PUNCTURED_LUNG);
        InjuryType.register(12, "am:cte", CTE);
        InjuryType.register(13, "am:broken_back", BROKEN_BACK);
        InjuryType.register("am:severed_spine", SEVERED_SPINE);
    }
    
    public static final class SeveredSpine extends InjuryType {
        public SeveredSpine() {
            recoveryTime = 180;
            allowedLocations = EnumSet.of(BodyLocation.CHEST, BodyLocation.ABDOMEN);
            permanent = true;
        }
    
        @Override
        public String getFluffText(BodyLocation loc, int severity, int gender) {
            return "A severed spine in " + ((loc == BodyLocation.CHEST) ? "upper" : "lower") + " body";
        }
    }

    public static final class BrokenBack extends InjuryType {
        public BrokenBack() {
            recoveryTime = 150;
            allowedLocations = EnumSet.of(BodyLocation.CHEST);
            fluffText = "A broken back";
        }
    
        @Override
        public List<InjuryType.InjuryAction> genStressEffect(Campaign c, Person p, Injury i, int hits) {
            return Arrays.asList(new InjuryType.InjuryAction(
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
    }

    public static final class Cte extends InjuryType {
        public Cte() {
            recoveryTime = 180;
            allowedLocations = EnumSet.of(BodyLocation.HEAD);
            permanent = true;
            fluffText = "Chronic traumatic encephalopathy";
        }
    
        @Override
        public List<InjuryType.InjuryAction> genStressEffect(Campaign c, Person p, Injury i, int hits) {
            int deathchance = Math.max((int) Math.round((1 + hits) * 100.0 / 6.0), 100);
            if(hits > 4) {
                return Arrays.asList(
                    new InjuryType.InjuryAction(
                        "certain death",
                        (rnd, gen) -> {
                            p.setStatus(Person.S_KIA);
                            p.addLogEntry(new LogEntry(c.getDate(), "Died due to brain trauma"));
                        }));
            } else {
                // We have a chance!
                return Arrays.asList(
                    newResetRecoveryTimeAction(i),
                    new InjuryType.InjuryAction(
                        deathchance + "% chance of death",
                        (rnd, gen) -> {
                            if(rnd.applyAsInt(6) + hits >= 5) {
                                p.setStatus(Person.S_KIA);
                                p.addLogEntry(new LogEntry(c.getDate(), "Died due to brain trauma"));
                        }
                    }));
            }
        }
    }

    public static final class PuncturedLung extends InjuryType {
        public PuncturedLung() {
            recoveryTime = 20;
            allowedLocations = EnumSet.of(BodyLocation.CHEST);
            fluffText = "A punctured lung";
        }
    
        @Override
        public List<InjuryType.InjuryAction> genStressEffect(Campaign c, Person p, Injury i, int hits) {
            return Arrays.asList(newResetRecoveryTimeAction(i));
        }
    }

    public static final class CerebralContusion extends InjuryType {
        public CerebralContusion() {
            recoveryTime = 90;
            allowedLocations = EnumSet.of(BodyLocation.HEAD);
            fluffText = "A cerebral contusion";
        }
    
        @Override
        public List<InjuryType.InjuryAction> genStressEffect(Campaign c, Person p, Injury i, int hits) {
            String secondEffectFluff = "development of a chronic traumatic encephalopathy";
            if(hits < 5) {
                int worseningChance = Math.max((int) Math.round((1 + hits) * 100.0 / 6.0), 100);
                secondEffectFluff = worseningChance + "% chance of " + secondEffectFluff;
            }
            return Arrays.asList(
                newResetRecoveryTimeAction(i),
                new InjuryType.InjuryAction(
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
    }

    public static final class LostLimb extends InjuryType {
        public LostLimb() {
            recoveryTime = 28;
            permanent = true;
        }
    
        @Override
        public boolean isValidInLocation(BodyLocation loc) {
            return loc.isLimb;
        }
        
        @Override
        public boolean impliesMissingLocation() {
            return true;
        }
        
        @Override
        public String getName(BodyLocation loc, int severity) {
            return "Missing " + Utilities.capitalize(loc.readableName);
        }

        @Override
        public String getFluffText(BodyLocation loc, int severity, int gender) {
            return "Lost " + Person.getGenderPronoun(gender, Person.PRONOUN_HISHER) + " "
                + loc.readableName;
        }
    }

    public static final class InternalBleeding extends InjuryType {
        public InternalBleeding() {
            recoveryTime = 20;
            allowedLocations = EnumSet.of(BodyLocation.ABDOMEN, BodyLocation.INTERNAL);
            maxSeverity = 3;
        }
    
        @Override
        public int getRecoveryTime(int severity) {
            return 20 * severity;
        }
    
        @Override
        public String getName(BodyLocation loc, int severity) {
            return Utilities.capitalize(getFluffText(loc, severity, 0));
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
        public List<InjuryType.InjuryAction> genStressEffect(Campaign c, Person p, Injury i, int hits) {
            String secondEffectFluff = (i.getSeverity() < 3)
                ? "internal bleeding worsening" : "death";
            if(hits < 5) {
                int worseningChance = Math.max((int) Math.round((1 + hits) * 100.0 / 6.0), 100);
                secondEffectFluff = worseningChance + "% chance of " + secondEffectFluff;
            }
            if(hits >= 5 && i.getSeverity() >= 3) {
                // Don't even bother doing anything else; we're dead
                return Arrays.asList(
                    new InjuryType.InjuryAction(
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
                    new InjuryType.InjuryAction(
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
    }

    public static final class BrokenCollarBone extends InjuryType {
        public BrokenCollarBone() {
            recoveryTime = 22;
            allowedLocations = EnumSet.of(BodyLocation.CHEST);
            fluffText = "A broken collar bone";
        }
    
        @Override
        public List<InjuryType.InjuryAction> genStressEffect(Campaign c, Person p, Injury i, int hits) {
            return Arrays.asList(newResetRecoveryTimeAction(i));
        }
    }

    public static final class BrokenLimb extends InjuryType {
        public BrokenLimb() {
            recoveryTime = 30;
        }
    
        @Override
        public boolean isValidInLocation(BodyLocation loc) {
            return loc.isLimb;
        }
        
        @Override
        public String getName(BodyLocation loc, int severity) {
            return "Broken " + Utilities.capitalize(loc.readableName);
        }

        @Override
        public String getFluffText(BodyLocation loc, int severity, int gender) {
            return "A broken " + loc.readableName;
        }
    
        @Override
        public List<InjuryType.InjuryAction> genStressEffect(Campaign c, Person p, Injury i, int hits) {
            return Arrays.asList(newResetRecoveryTimeAction(i));
        }
    }

    public static final class BruisedKidney extends InjuryType {
        public BruisedKidney() {
            recoveryTime = 10;
            allowedLocations = EnumSet.of(BodyLocation.ABDOMEN);
            fluffText = "A bruised kidney";
        }
    
        @Override
        public List<InjuryType.InjuryAction> genStressEffect(Campaign c, Person p, Injury i, int hits) {
            return Arrays.asList(new InjuryType.InjuryAction(
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
    }

    public static final class BrokenRib extends InjuryType {
        public BrokenRib() {
            recoveryTime = 20;
            allowedLocations = EnumSet.of(BodyLocation.CHEST);
            fluffText = "A broken rib";
        }
    
        @Override
        public List<InjuryType.InjuryAction> genStressEffect(Campaign c, Person p, Injury i, int hits) {
            return Arrays.asList(new InjuryType.InjuryAction(
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
    }

    public static final class Concussion extends InjuryType {
        public Concussion() {
            recoveryTime = 14;
            allowedLocations = EnumSet.of(BodyLocation.HEAD);
            maxSeverity = 2;
            fluffText = "A concussion";
        }
    
        @Override
        public int getRecoveryTime(int severity) {
            return severity >= 2 ? 42 : 14;
        }
    
        @Override
        public List<InjuryType.InjuryAction> genStressEffect(Campaign c, Person p, Injury i, int hits) {
            String secondEffectFluff = (i.getSeverity() == 1)
                ? "concussion worsening" : "development of a cerebral contusion";
            if(hits < 5) {
                int worseningChance = Math.max((int) Math.round((1 + hits) * 100.0 / 6.0), 100);
                secondEffectFluff = worseningChance + "% chance of " + secondEffectFluff;
            }
            return Arrays.asList(
                newResetRecoveryTimeAction(i),
                new InjuryType.InjuryAction(
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
    }

    public static final class Sprain extends InjuryType {
        public Sprain() {
            recoveryTime = 12;
        }
    
        @Override
        public boolean isValidInLocation(BodyLocation loc) {
            return loc.isLimb;
        }
        
        @Override
        public String getName(BodyLocation loc, int severity) {
            return "Sprained " + Utilities.capitalize(loc.readableName);
        }

        @Override
        public String getFluffText(BodyLocation loc, int severity, int gender) {
            return "A sprained " + loc.readableName;
        }
    }

    public static final class Laceration extends InjuryType {
        public Laceration() {
            allowedLocations = EnumSet.of(BodyLocation.HEAD);
        }
    
        @Override
        public String getName(BodyLocation loc, int severity) {
            return "Lacerated " + Utilities.capitalize(loc.readableName);
        }

        @Override
        public String getFluffText(BodyLocation loc, int severity, int gender) {
            return "A laceration on " + Person.getGenderPronoun(gender, Person.PRONOUN_HISHER) + " head";
        }
    }

    public static final class Bruise extends InjuryType {
        public Bruise() {
            allowedLocations = EnumSet.of(BodyLocation.CHEST, BodyLocation.ABDOMEN);
        }
    
        @Override
        public boolean isValidInLocation(BodyLocation loc) {
            return loc.isLimb || super.isValidInLocation(loc);
        }
        
        @Override
        public String getName(BodyLocation loc, int severity) {
            return "Bruised " + Utilities.capitalize(loc.readableName);
        }

        @Override
        public String getFluffText(BodyLocation loc, int severity, int gender) {
            return "A bruise on " + Person.getGenderPronoun(gender, Person.PRONOUN_HISHER) + " "
                + loc.readableName;
        }
    }

    public static final class Cut extends InjuryType {
        public Cut() {
            allowedLocations = EnumSet.of(BodyLocation.CHEST, BodyLocation.ABDOMEN);
        }
    
        @Override
        public boolean isValidInLocation(BodyLocation loc) {
            return loc.isLimb || super.isValidInLocation(loc);
        }

        @Override
        public String getName(BodyLocation loc, int severity) {
            return "Cut " + Utilities.capitalize(loc.readableName);
        }
        
        @Override
        public String getFluffText(BodyLocation loc, int severity, int gender) {
            return "Some cuts on " + Person.getGenderPronoun(gender, Person.PRONOUN_HISHER) + " "
                + loc.readableName;
        }
    }
}
