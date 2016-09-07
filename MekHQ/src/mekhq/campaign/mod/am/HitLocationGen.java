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

import java.util.function.Function;

import mekhq.Utilities;

/**
 * Home to static methods returning a random hit location given a random value from 0 to 199
 * and a function to check if a given {@link BodyLocation} is missing.
 */
public class HitLocationGen {
    public static BodyLocation generic(int roll, Function<BodyLocation, Boolean> missingCheck) {
        missingCheck = Utilities.nonNull(missingCheck, (bl) -> false);
        BodyLocation result = BodyLocation.GENERIC;
        if (roll < 10) {
            result = BodyLocation.HEAD;
        } else if (roll < 30) {
            result = BodyLocation.CHEST;
        } else if (roll < 40) {
            result = BodyLocation.ABDOMEN;
        } else if (roll < 55 && !missingCheck.apply(BodyLocation.LEFT_ARM)) {
            result = BodyLocation.LEFT_ARM;
        } else if (roll < 70 && !missingCheck.apply(BodyLocation.RIGHT_ARM)) {
            result = BodyLocation.RIGHT_ARM;
        } else if (roll < 100 && !missingCheck.apply(BodyLocation.LEFT_LEG)) {
            result = BodyLocation.LEFT_LEG;
        } else if (roll < 130 && !missingCheck.apply(BodyLocation.RIGHT_LEG)) {
            result = BodyLocation.RIGHT_LEG;
        } else if (roll < 145 && !missingCheck.apply(BodyLocation.RIGHT_ARM)) {
            result = BodyLocation.RIGHT_ARM;
        } else if (roll < 160 && !missingCheck.apply(BodyLocation.LEFT_ARM)) {
            result = BodyLocation.LEFT_ARM;
        } else if (roll < 170) {
            result = BodyLocation.ABDOMEN;
        } else if (roll < 190) {
            result = BodyLocation.CHEST;
        } else {
            result = BodyLocation.HEAD;
        }
        return result;
    }
    
    public static BodyLocation mechAndAsf(int roll, Function<BodyLocation, Boolean> missingCheck) {
        missingCheck = Utilities.nonNull(missingCheck, (bl) -> false);
        BodyLocation result = BodyLocation.GENERIC;
        if (roll < 25) {
            result = BodyLocation.HEAD;
        } else if (roll < 41) {
            result = BodyLocation.CHEST;
        } else if (roll < 48) {
            result = BodyLocation.ABDOMEN;
        } else if (roll < 61 && !missingCheck.apply(BodyLocation.LEFT_ARM)) {
            result = BodyLocation.LEFT_ARM;
        } else if (roll < 74 && !missingCheck.apply(BodyLocation.RIGHT_ARM)) {
            result = BodyLocation.RIGHT_ARM;
        } else if (roll < 100 && !missingCheck.apply(BodyLocation.LEFT_LEG)) {
            result = BodyLocation.LEFT_LEG;
        } else if (roll < 126 && !missingCheck.apply(BodyLocation.RIGHT_LEG)) {
            result = BodyLocation.RIGHT_LEG;
        } else if (roll < 139 && !missingCheck.apply(BodyLocation.RIGHT_ARM)) {
            result = BodyLocation.RIGHT_ARM;
        } else if (roll < 152 && !missingCheck.apply(BodyLocation.LEFT_ARM)) {
            result = BodyLocation.LEFT_ARM;
        } else if (roll < 159) {
            result = BodyLocation.ABDOMEN;
        } else if (roll < 176) {
            result = BodyLocation.CHEST;
        } else {
            result = BodyLocation.HEAD;
        }
        return result;
    }
}
