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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collectors;

import megamek.common.Aero;
import megamek.common.Compute;
import megamek.common.Entity;
import megamek.common.Mech;
import mekhq.campaign.Campaign;
import mekhq.campaign.event.InjuryEvent;
import mekhq.campaign.personnel.BodyLocation;
import mekhq.campaign.personnel.Injury;
import mekhq.campaign.personnel.InjuryType;
import mekhq.campaign.personnel.Person;
import mekhq.campaign.unit.Unit;

public final class InjuryUtil {
    private static InjuryType.InjuryProducer newInjuryGenerator(Person person) {
        return (loc, type, severity) ->
        {
            int recoveryTime = genHealingTime(person, type, severity);
            String fluff = type.getFluffText(loc, severity, person.getGender());
            return new Injury(recoveryTime, fluff, loc, type, severity, false);
        };
    }
    
    private static void addHitToAccumulator(Map<BodyLocation, Integer> acc, BodyLocation loc) {
        if(!acc.containsKey(loc)) {
            acc.put(loc, Integer.valueOf(1));
        } else {
            acc.put(loc, acc.get(loc) + 1);
        }
    }
    
    public static Collection<Injury> genInjuries(Campaign campaign, Person person, int hits) {
        final Unit u = campaign.getUnit(person.getUnitId());
        final Entity en = (null != u) ? u.getEntity() : null;
        final boolean mwasf = (null != en) && ((en instanceof Mech) || (en instanceof Aero));
        final int critMod = mwasf ? 0 : 2;
        final BiFunction<IntUnaryOperator, Function<BodyLocation, Boolean>, BodyLocation> generator
            = mwasf ? HitLocationGen::mechAndAsf : HitLocationGen::generic;
        final Map<BodyLocation, Integer> hitAccumulator = new HashMap<>();
        
        for (int i = 0; i < hits; i++) {
            BodyLocation location
                = generator.apply(Compute::randomInt, (loc) -> !isLocationMissing(person, loc));

            // apply hit here
            addHitToAccumulator(hitAccumulator, location);
            int roll = Compute.d6(2);
            if(roll + hits + critMod > 12) {
                addHitToAccumulator(hitAccumulator, location);
            }
        }
        List<Injury> newInjuries = new ArrayList<>();
        for(Entry<BodyLocation, Integer> accEntry : hitAccumulator.entrySet()) {
            newInjuries.addAll(applyDamage(person, accEntry.getKey(), accEntry.getValue().intValue()));
        }
        String ni_report = "";
        for (Injury ni : newInjuries) {
            ni_report += "\n\t\t" + ni.getFluff();
        }
        if (newInjuries.size() > 0) {
            person.addLogEntry(campaign.getDate(), "Returned from combat with the following new injuries:" + ni_report);
        }
        //setHits(0);
        return newInjuries;
    }

    /** Resolve injury modifications in case of entering combat with active ones */
    public static void resolveCombat(Campaign c, Person person, int hits) {
        // Gather all the injury actions resulting from the combat situation
        final List<InjuryType.InjuryAction> actions = new ArrayList<>();
        person.getInjuries().forEach((i) ->
        {
            actions.addAll(i.getType().genStressEffect(c, person, i, hits));
        });
        
        // We could do some fancy display-to-the-user thing here, but for now just resolve all actions
        actions.forEach((ia) ->
        {
            ia.action.accept(Compute::randomInt, newInjuryGenerator(person));
        });
    }
    
    /**
     * @return <tt>true</tt> if the location (or any of its parent locations) has an injury
     * which implies that the location (most likely a limb) is severed.
     */
    public static boolean isLocationMissing(Person p, BodyLocation loc) {
        // AM doesn't actually care about missing heads right now ...
        if((null == loc) || !loc.isLimb) {
            return false;
        }
        for(Injury i : p.getInjuriesByLocation(loc)) {
            if(i.getType().impliesMissingLocation()) {
                return true;
            }
        }
        // Check parent locations as well (a hand can be missing if the corresponding arm is)
        return isLocationMissing(p, loc.parent);
    }


    private static List<Injury> applyDamage(Person p, BodyLocation loc, int hits) {
        ArrayList<Injury> new_injuries = new ArrayList<Injury>();
        boolean bad_status = (p.getStatus() == Person.S_KIA || p.getStatus() == Person.S_MIA);
        int roll = Compute.randomInt(2);
        InjuryType type = Injury.getInjuryTypeByLocation(loc, roll, hits);
        if (p.hasInjury(loc, type)) {
            Injury injury = p.getInjuryByLocationAndType(loc, type);
            injury.setTime(genHealingTime(p, injury));
            return new_injuries;
        }
        switch (location) {
            case BODY_LEFT_ARM:
            case BODY_RIGHT_ARM:
            case BODY_LEFT_LEG:
            case BODY_RIGHT_LEG:
                if (hit_location[location] == 1) {
                    if (roll == 2) {
                        new_injuries.add(new Injury(Injury.generateHealingTime(campaign, Injury.INJ_CUT, hit_location[location], this), Injury.generateInjuryFluffText(Injury.INJ_CUT, location, gender), location, Injury.INJ_CUT, hit_location[location], false, bad_status));
                    } else {
                        new_injuries.add(new Injury(Injury.generateHealingTime(campaign, Injury.INJ_BRUISE, hit_location[location], this), Injury.generateInjuryFluffText(Injury.INJ_BRUISE, location, gender), location, Injury.INJ_BRUISE, hit_location[location], false, bad_status));
                    }
                } else if (hit_location[location] == 2) {
                    new_injuries.add(new Injury(Injury.generateHealingTime(campaign, Injury.INJ_SPRAIN, hit_location[location], this), Injury.generateInjuryFluffText(Injury.INJ_SPRAIN, location, gender), location, Injury.INJ_SPRAIN, hit_location[location], false, bad_status));
                } else if (hit_location[location] == 3) {
                    new_injuries.add(new Injury(Injury.generateHealingTime(campaign, Injury.INJ_BROKEN_LIMB, hit_location[location], this), Injury.generateInjuryFluffText(Injury.INJ_BROKEN_LIMB, location, gender), location, Injury.INJ_BROKEN_LIMB, hit_location[location], false, bad_status));
                } else if (hit_location[location] > 3) {
                    new_injuries.add(new Injury(Injury.generateHealingTime(campaign, Injury.INJ_LOST_LIMB, hit_location[location], this), Injury.generateInjuryFluffText(Injury.INJ_LOST_LIMB, location, gender), location, Injury.INJ_LOST_LIMB, hit_location[location], true));
                }
                break;
            case BODY_HEAD:
                if (hit_location[location] == 1) {
                    new_injuries.add(new Injury(Injury.generateHealingTime(campaign, Injury.INJ_LACERATION, hit_location[location], this), Injury.generateInjuryFluffText(Injury.INJ_LACERATION, location, gender), location, Injury.INJ_LACERATION, hit_location[location], false, bad_status));
                } else if (hit_location[location] == 2 || hit_location[location] == 3) {
                    new_injuries.add(new Injury(Injury.generateHealingTime(campaign, Injury.INJ_CONCUSSION, hit_location[location], this), Injury.generateInjuryFluffText(Injury.INJ_CONCUSSION, location, gender), location, Injury.INJ_CONCUSSION, hit_location[location], false, bad_status));
                } else if (hit_location[location] == 4) {
                    new_injuries.add(new Injury(Injury.generateHealingTime(campaign, Injury.INJ_CEREBRAL_CONTUSION, hit_location[location], this), Injury.generateInjuryFluffText(Injury.INJ_CEREBRAL_CONTUSION, location, gender), location, Injury.INJ_CEREBRAL_CONTUSION, hit_location[location], false, bad_status));
                } else if (hit_location[location] > 4) {
                    new_injuries.add(new Injury(Injury.generateHealingTime(campaign, Injury.INJ_CTE, hit_location[location], this), Injury.generateInjuryFluffText(Injury.INJ_CTE, location, gender), location, Injury.INJ_CTE, hit_location[location], true));
                }
                break;
            case BODY_CHEST:
                if (hit_location[location] == 1) {
                    if (roll == 2) {
                        new_injuries.add(new Injury(Injury.generateHealingTime(campaign, Injury.INJ_CUT, hit_location[location], this), Injury.generateInjuryFluffText(Injury.INJ_CUT, location, gender), location, Injury.INJ_CUT, hit_location[location], false, bad_status));
                    } else {
                        new_injuries.add(new Injury(Injury.generateHealingTime(campaign, Injury.INJ_BRUISE, hit_location[location], this), Injury.generateInjuryFluffText(Injury.INJ_BRUISE, location, gender), location, Injury.INJ_BRUISE, hit_location[location], false, bad_status));
                    }
                } else if (hit_location[location] == 2) {
                    new_injuries.add(new Injury(Injury.generateHealingTime(campaign, Injury.INJ_BROKEN_RIB, hit_location[location], this), Injury.generateInjuryFluffText(Injury.INJ_BROKEN_RIB, location, gender), location, Injury.INJ_BROKEN_RIB, hit_location[location], false, bad_status));
                } else if (hit_location[location] == 3) {
                    new_injuries.add(new Injury(Injury.generateHealingTime(campaign, Injury.INJ_BROKEN_COLLAR_BONE, hit_location[location], this), Injury.generateInjuryFluffText(Injury.INJ_BROKEN_COLLAR_BONE, location, gender), location, Injury.INJ_BROKEN_COLLAR_BONE, hit_location[location], false, bad_status));
                } else if (hit_location[location] == 4) {
                    new_injuries.add(new Injury(Injury.generateHealingTime(campaign, Injury.INJ_PUNCTURED_LUNG, hit_location[location], this), Injury.generateInjuryFluffText(Injury.INJ_PUNCTURED_LUNG, location, gender), location, Injury.INJ_PUNCTURED_LUNG, hit_location[location], false, bad_status));
                } else if (hit_location[location] > 4) {
                    if (Compute.randomInt(100) < 15) {
                        changeStatus(Person.S_RETIRED);
                        new_injuries.add(new Injury(Injury.generateHealingTime(campaign, Injury.INJ_BROKEN_BACK, hit_location[location], this), Injury.generateInjuryFluffText(Injury.INJ_BROKEN_BACK, location, gender), location, Injury.INJ_BROKEN_BACK, hit_location[location], true));
                    } else {
                        new_injuries.add(new Injury(Injury.generateHealingTime(campaign, Injury.INJ_BROKEN_BACK, hit_location[location], this), Injury.generateInjuryFluffText(Injury.INJ_BROKEN_BACK, location, gender), location, Injury.INJ_BROKEN_BACK, hit_location[location], false, bad_status));
                    }
                }
                break;
            case BODY_ABDOMEN:
                if (hit_location[location] == 1) {
                    if (roll == 2) {
                        new_injuries.add(new Injury(Injury.generateHealingTime(campaign, Injury.INJ_CUT, hit_location[location], this), Injury.generateInjuryFluffText(Injury.INJ_CUT, location, gender), location, Injury.INJ_CUT, hit_location[location], false, bad_status));
                    } else {
                        new_injuries.add(new Injury(Injury.generateHealingTime(campaign, Injury.INJ_BRUISE, hit_location[location], this), Injury.generateInjuryFluffText(Injury.INJ_BRUISE, location, gender), location, Injury.INJ_BRUISE, hit_location[location], false, bad_status));
                    }
                } else if (hit_location[location] == 2) {
                    new_injuries.add(new Injury(Injury.generateHealingTime(campaign, Injury.INJ_BRUISED_KIDNEY, hit_location[location], this), Injury.generateInjuryFluffText(Injury.INJ_BRUISED_KIDNEY, location, gender), location, Injury.INJ_BRUISED_KIDNEY, hit_location[location], false, bad_status));
                } else if (hit_location[location] > 2) {
                    new_injuries.add(new Injury(Injury.generateHealingTime(campaign, Injury.INJ_INTERNAL_BLEEDING, hit_location[location], this), Injury.generateInjuryFluffText(Injury.INJ_INTERNAL_BLEEDING, location, gender), location, Injury.INJ_INTERNAL_BLEEDING, hit_location[location], false, bad_status));
                }
                break;
            default:
                System.err.println("ERROR: Default CASE reached in (Advanced Medical Section) Person.applyDamage()");
        }
        if (location == BODY_HEAD) {
            Injury inj = getInjuryByLocation(BODY_HEAD);
            if (inj != null && new_injuries != null && new_injuries.size() > 0) {
                if (inj.getType() > new_injuries.get(0).getType()) {
                    inj.setTime(Injury.generateHealingTime(campaign, inj.getHits(), inj.getType(), this));
                    new_injuries.clear();
                } else if (inj.getType() < new_injuries.get(0).getType()) {
                    injuries.remove(inj);
                }
            }
        }
        return new_injuries;
    }
    
    // Called when creating a new injury to determine the type of injury it is
    public static int getInjuryTypeByLocation(int loc, int roll, int hit_location) {
        switch (loc) {
        case Person.BODY_LEFT_ARM:
        case Person.BODY_RIGHT_ARM:
        case Person.BODY_LEFT_LEG:
        case Person.BODY_RIGHT_LEG:
            if (hit_location == 1) {
                if (roll == 2) {
                    return INJ_CUT;
                } else {
                    return INJ_BRUISE;
                }
            } else if (hit_location == 2) {
                return INJ_SPRAIN;
            } else if (hit_location == 3) {
                return INJ_BROKEN_LIMB;
            } else if (hit_location > 3) {
                return INJ_LOST_LIMB;
            }
            break;
        case Person.BODY_HEAD:
            if (hit_location == 1) {
                return INJ_LACERATION;
            } else if (hit_location == 2 || hit_location == 3) {
                return INJ_CONCUSSION;
            } else if (hit_location == 4) {
                return INJ_CEREBRAL_CONTUSION;
            } else if (hit_location > 4) {
                return INJ_CTE;
            }
            break;
        case Person.BODY_CHEST:
            if (hit_location == 1) {
                if (roll == 2) {
                    return INJ_CUT;
                } else {
                    return INJ_BRUISE;
                }
            } else if (hit_location == 2) {
                return INJ_BROKEN_RIB;
            } else if (hit_location == 3) {
                return INJ_BROKEN_COLLAR_BONE;
            } else if (hit_location == 4) {
                return INJ_PUNCTURED_LUNG;
            } else if (hit_location > 4) {
                return INJ_BROKEN_BACK;
            }
            break;
        case Person.BODY_ABDOMEN:
            if (hit_location == 1) {
                if (roll == 2) {
                    return INJ_CUT;
                } else {
                    return INJ_BRUISE;
                }
            } else if (hit_location == 2) {
                return INJ_BRUISED_KIDNEY;
            } else if (hit_location > 2) {
                return INJ_INTERNAL_BLEEDING;
            }
            break;
        }
        return 0;
    }

    /** Called when creating a new injury to generate a slightly randomized healing time */
    public static int genHealingTime(Person p, Injury i) {
        return genHealingTime(p, i.getType(), i.getSeverity());
    }
    
    public static int genHealingTime(Person p, InjuryType itype, int severity) {
        int mod = 100;
        int rand = Compute.randomInt(100);
        if(rand < 5) {
            mod += (Compute.d6() < 4) ? rand : -rand;
        }
        
        int time = itype.getRecoveryTime(severity);
        if(itype == InjuryTypes.LACERATION) {
            time += Compute.d6();
        }

        time = Math.round(time * mod * p.getAbilityTimeModifier() / 10000);
        return time;
    }
}
