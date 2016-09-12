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
import java.util.function.Function;
import java.util.function.IntUnaryOperator;

import megamek.common.Aero;
import megamek.common.Compute;
import megamek.common.Entity;
import megamek.common.Mech;
import mekhq.campaign.Campaign;
import mekhq.campaign.personnel.BodyLocation;
import mekhq.campaign.personnel.Injury;
import mekhq.campaign.personnel.InjuryType;
import mekhq.campaign.personnel.InjuryType.InjuryProducer;
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
            // critical hits add to the amount
            int roll = Compute.d6(2);
            if(roll + hits + critMod > 12) {
                addHitToAccumulator(hitAccumulator, location);
            }
        }
        List<Injury> newInjuries = new ArrayList<>();
        for(Entry<BodyLocation, Integer> accEntry : hitAccumulator.entrySet()) {
            newInjuries.addAll(genSpecificInjury(person, accEntry.getKey(), accEntry.getValue().intValue()));
        }
        return newInjuries;
    }

    /** Resolve injury modifications in case of entering combat with active ones */
    public static void resolveAfterCombat(Campaign c, Person person, int hits) {
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
    
    public static void resolveCombatDamage(Campaign c, Person person, int hits) {
        Collection<Injury> newInjuries = genInjuries(c, person, hits);
        newInjuries.forEach((inj) -> person.addInjury(inj));
        if (newInjuries.size() > 0) {
            StringBuilder sb = new StringBuilder("Returned from combat with the following new injuries:");
            newInjuries.forEach((inj) -> sb.append("\n\t\t").append(inj.getFluff()));
            person.addLogEntry(c.getDate(), sb.toString());
        }
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
            if(i.getType().impliesMissingLocation(loc)) {
                return true;
            }
        }
        // Check parent locations as well (a hand can be missing if the corresponding arm is)
        return isLocationMissing(p, loc.parent);
    }


    private static List<Injury> genSpecificInjury(Person p, BodyLocation loc, int hits) {
        List<Injury> newInjuries = new ArrayList<Injury>();
        final InjuryProducer gen = newInjuryGenerator(p);
        
        switch(loc) {
            case LEFT_ARM: case LEFT_HAND: case LEFT_LEG: case LEFT_FOOT:
            case RIGHT_ARM: case RIGHT_HAND: case RIGHT_LEG: case RIGHT_FOOT:
                switch(hits) {
                    case 1:
                        newInjuries.add(gen.gen(loc,
                            Compute.randomInt(2) == 0 ? InjuryTypes.CUT : InjuryTypes.BRUISE, 1));
                        break;
                    case 2:
                        newInjuries.add(gen.gen(loc, InjuryTypes.SPRAIN, 1));
                        break;
                    case 3:
                        newInjuries.add(gen.gen(loc, InjuryTypes.BROKEN_LIMB, 1));
                        break;
                    case 4:
                        newInjuries.add(gen.gen(loc, InjuryTypes.LOST_LIMB, 1));
                        break;
                }
                break;
            case HEAD:
                switch(hits) {
                    case 1:
                        newInjuries.add(gen.gen(loc, InjuryTypes.LACERATION, 1));
                        break;
                    case 2: case 3:
                        newInjuries.add(gen.gen(loc, InjuryTypes.CONCUSSION, hits - 1));
                        break;
                    case 4:
                        newInjuries.add(gen.gen(loc, InjuryTypes.CEREBRAL_CONTUSION, 1));
                        break;
                    default:
                        newInjuries.add(gen.gen(loc, InjuryTypes.CTE, 1));
                        break;
                }
                break;
            case CHEST:
                switch(hits) {
                    case 1:
                        newInjuries.add(gen.gen(loc,
                            Compute.randomInt(2) == 0 ? InjuryTypes.CUT : InjuryTypes.BRUISE, 1));
                        break;
                    case 2:
                        newInjuries.add(gen.gen(loc, InjuryTypes.BROKEN_RIB, 1));
                        break;
                    case 3:
                        newInjuries.add(gen.gen(loc, InjuryTypes.BROKEN_COLLAR_BONE, 1));
                        break;
                    case 4:
                        newInjuries.add(gen.gen(loc, InjuryTypes.PUNCTURED_LUNG, 1));
                        break;
                    default:
                        newInjuries.add(gen.gen(loc, InjuryTypes.BROKEN_BACK, 1));
                        if(Compute.randomInt(100) < 15) {
                            newInjuries.add(gen.gen(loc, InjuryTypes.SEVERED_SPINE, 1));
                        }
                        break;
                }
                break;
            case ABDOMEN:
                switch(hits) {
                    case 1:
                        newInjuries.add(gen.gen(loc,
                            Compute.randomInt(2) == 0 ? InjuryTypes.CUT : InjuryTypes.BRUISE, 1));
                        break;
                    case 2:
                        newInjuries.add(gen.gen(loc, InjuryTypes.BRUISED_KIDNEY, 1));
                        break;
                    default:
                        newInjuries.add(gen.gen(loc, InjuryTypes.INTERNAL_BLEEDING, hits - 2));
                        break;
                }
                break;
            default:
                break;
        }
        return newInjuries;
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
