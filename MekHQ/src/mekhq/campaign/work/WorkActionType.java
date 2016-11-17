/*
 * WorkActionType.java
 * 
 * Copyright (C) 2016 MegaMek team
 * 
 * This file is part of MekHQ.
 * 
 * MekHQ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
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
package mekhq.campaign.work;

/**
 * This enum lists all the possible actions one can do to a unit in a centralized location.
 * The expectation is that it will grow quite big.
 * 
 * For now, it'll only be used for refits.
 */
public enum WorkActionType {
    // Refit-specific actions
    WEAPON_REPLACEMENT_EASY("replace one weapon with another of the same category and with the same (or fewer) critical spaces (including ammunition)"),
    WEAPON_REPLACEMENT_MODERATE("replace one category of weapon with another class, but with the same or fewer critical spaces (including ammunition)"),
    WEAPON_REPLACEMENT_HARD("replace one weapon or item of equipment with any other, even if it is larger than the item(s) being replaced"),
    CHANGE_ARMOR_TYPE("change type of armor"),
    CHANGE_ARMOR_DISTRIBUTION("change armor quantity or distribution"),
    MOVE_COMPONENT("move a component"),
    ADD_AMMO_BIN("add ammunition"),
    ADD_HEAT_SINK("add heat sinks"),
    INSTALL_NEW_ITEM("install a new item"),
    INSTALL_ECM("install an ECM suite"),
    INSTALL_C3("install a C³ system"),
    INSTALL_TARCOMP("install a targeting computer"),
    CHANGE_HEAT_SINK_TYPE("change heat sink type"),
    CHANGE_ENGINE_RATING("change engine rating"),
    CHANGE_MYOMER_TYPE("change myomer type"),
    INSTALL_CASE("install or remove CASE"),
    CHANGE_IS_TYPE("change internal structure type"),
    CHANGE_ENGINE_TYPE("change engine type"),
    CHANGE_GYRO_TYPE("change gyro type"),
    CHANGE_COCKPIT_TYPE("change cockpit type");

    // User-displayable. TODO: Localize
    public final String name;

    private WorkActionType(String name) {
        this.name = name;
    }
}
