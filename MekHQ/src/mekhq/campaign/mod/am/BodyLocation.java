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
import java.util.Locale;

public enum BodyLocation {
    HEAD(0), LEFT_LEG(1, true), LEFT_ARM(2, true), CHEST(3), ABDOMEN(4), RIGHT_ARM(5, true), RIGHT_LEG(6, true), INTERNAL(7), GENERIC(-1);

    // Initialize by-id array lookup table
    private static BodyLocation[] idMap;
    static {
        int maxId = 0;
        for(BodyLocation workTime : values()) {
            maxId = Math.max(maxId, workTime.id);
        }
        idMap = new BodyLocation[maxId + 1];
        Arrays.fill(idMap, GENERIC);
        for(BodyLocation workTime : values()) {
            if(workTime.id > 0) {
                idMap[workTime.id] = workTime;
            }
        }
    }
    
    /** @return the body location corresponding to the (old) ID */
    public static BodyLocation of(int id) {
        return ((id > 0) && (id < idMap.length)) ? idMap[id] : GENERIC;
    }
    
    /** @return the body location corresponding to the given string */
    public static BodyLocation of(String str) {
        try {
            return of(Integer.valueOf(str));
        } catch(NumberFormatException nfex) {
            // Try something else
        }
        return valueOf(str.toUpperCase(Locale.ROOT));
    }
    
    public final int id;
    public final boolean isLimb;

    private BodyLocation(int id) {
        this(id, false);
    }
    
    private BodyLocation(int id, boolean isLimb) {
        this.id = id;
        this.isLimb = isLimb;
    }
}

