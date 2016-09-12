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
package mekhq.campaign.personnel;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A modifier is some kind of (usually temporary) effect that influences the character's base values.
 * <p>
 * Modifiers have three values: Which value they apply to, how much they change the value by (can
 * be positive or negative) and optionally what type of modifier they are. If a person has
 * multiple modifiers of the same type, only the highest positive one and the lowest negative
 * one applies. All modifiers without a type apply fully.
 */
public class Modifier {
    public final Value value;
    public final int mod;
    public final String type;
    
    public static int calcTotalModifier(Collection<Modifier> mods, Value val) {
        Map<String, Integer> posMods = new HashMap<>();
        Map<String, Integer> negMods = new HashMap<>();
        long result = 0;
        for(Modifier mod : mods) {
            if(mod.value == val) {
                if(null != mod.type) {
                    int posMod = Math.max(0, mod.mod);
                    int negMod = Math.min(0, mod.mod);
                    if(posMods.containsKey(mod.type)) {
                        posMods.put(mod.type, Math.max(posMod, posMods.get(mod.type)));
                        negMods.put(mod.type, Math.min(negMod, posMods.get(mod.type)));
                    } else {
                        posMods.put(mod.type, posMod);
                        negMods.put(mod.type, negMod);
                    }
                }
            }
        }
        for(String type : posMods.keySet()) {
            result += posMods.get(type);
            result += negMods.get(type);
        }
        if(result > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        if( result < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }
        return (int) result;
    }
    
    public Modifier(Value value, int mod) {
        this(value, mod, null);
    }
    
    public Modifier(Value value, int mod, String type) {
        this.value = value;
        this.mod = mod;
        this.type = type;
    }
    
    public static enum Value {
        PILOTING, GUNNERY
    }
}
