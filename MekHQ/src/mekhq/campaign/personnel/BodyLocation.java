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

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

public enum BodyLocation {
    HEAD(0, "head"), LEFT_LEG(1, "left leg", true), LEFT_ARM(2, "left arm", true),
    CHEST(3, "chest"), ABDOMEN(4, "abdomen"), RIGHT_ARM(5, "right arm", true), RIGHT_LEG(6, "right leg", true),
    INTERNAL(7, "innards"), LEFT_HAND(8, "left hand", true), RIGHT_HAND(9, "right hand", true),
    LEFT_FOOT(10, "left foot", true), RIGHT_FOOT(11, "right foot", true), GENERIC(-1, "");

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
    // Includes everything attached to a limb
    public final boolean isLimb;
    public final String readableName;
    public final BodyLocation parent;
    private final Set<BodyLocation> children = EnumSet.noneOf(BodyLocation.class);

    private BodyLocation(int id, String readableName) {
        this(id, readableName, false, null);
    }
    
    private BodyLocation(int id, String readableName, boolean isLimb) {
        this(id, readableName, isLimb, null);
    }
    
    private BodyLocation(int id, String readableName, boolean isLimb, BodyLocation parent) {
        this.id = id;
        this.readableName = readableName;
        this.isLimb = isLimb;
        this.parent = parent;
        if(null != parent)
        {
            parent.addChildLocation(this);
        }
    }

    private void addChildLocation(BodyLocation child) {
        children.add(child);
    }
    
    public boolean isParentOf(BodyLocation child) {
        if(children.contains(child)) {
            return true;
        }
        for(BodyLocation myChild : children) {
            if(myChild.isParentOf(child)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isChildOf(BodyLocation parent) {
        return (null != this.parent) ? (this.parent == parent) || this.parent.isChildOf(parent) : false;
    }
}

