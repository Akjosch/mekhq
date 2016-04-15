/*
 * MissingMekLocation.java
 *
 * Copyright (c) 2009 Jay Lawson <jaylawson39 at yahoo.com>. All rights reserved.
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

package mekhq.campaign.parts;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import megamek.common.CriticalSlot;
import megamek.common.EquipmentType;
import megamek.common.IArmorState;
import megamek.common.Protomech;
import megamek.common.TechConstants;
import mekhq.campaign.Campaign;
import mekhq.campaign.parts.component.Installable;
import mekhq.campaign.unit.Unit;

/**
 *
 * @author Jay Lawson <jaylawson39 at yahoo.com>
 */
public class MissingProtomekLocation extends MissingPart {
    private static final long serialVersionUID = -122291037522319765L;

    protected int structureType;
    protected boolean booster;
    protected double percent;
    protected boolean forQuad;

    public MissingProtomekLocation() {
        this(0, 0, 0, false, false, null);
    }


    public MissingProtomekLocation(int loc, int tonnage, int structureType, boolean hasBooster, boolean quad, Campaign c) {
        super(c);
        this.structureType = structureType;
        this.booster = hasBooster;
        this.percent = 1.0;
        this.forQuad = quad;
        //TODO: need to account for internal structure and myomer types
        //crap, no static report for location names?
        this.name = "Mech Location";
        this.name = "Protomech Location";
        switch(loc) {
        case(Protomech.LOC_HEAD):
            this.name = "Protomech Head";
            break;
        case(Protomech.LOC_TORSO):
            this.name = "Protomech Torso";
            break;
        case(Protomech.LOC_LARM):
            this.name = "Protomech Left Arm";
            break;
        case(Protomech.LOC_RARM):
            this.name = "Protomech Right Arm";
            break;
        case(Protomech.LOC_LEG):
            this.name = "Protomech Legs";
            if(forQuad) {
                this.name = "Protomech Legs (Quad)";
            }
            break;
        case(Protomech.LOC_MAINGUN):
            this.name = "Protomech Main Gun";
            break;
        }
        if(booster) {
            this.name += " (Myomer Booster)";
        }
        get(Installable.class).setLocations(loc);
        get(Installable.class).setUnitTonnage(tonnage);
        get(Installable.class).setTonnageLimited(true);
    }

    @Override
    public int getBaseTime() {
        return 240;
    }

    @Override
    public int getDifficulty() {
        return 3;
    }

    public boolean hasBooster() {
        return booster;
    }

    public int getStructureType() {
        return structureType;
    }


    @Override
    public double getTonnage() {
        //TODO: how much should this weigh?
        return 0;
    }

    @Override
    protected void loadFieldsFromXmlNode(Node wn) {
        NodeList nl = wn.getChildNodes();

        for (int x=0; x<nl.getLength(); x++) {
            Node wn2 = nl.item(x);

            if (wn2.getNodeName().equalsIgnoreCase("loc")) {
                get(Installable.class).setLocations(Integer.parseInt(wn2.getTextContent()));
            } else if (wn2.getNodeName().equalsIgnoreCase("structureType")) {
                structureType = Integer.parseInt(wn2.getTextContent());
            } else if (wn2.getNodeName().equalsIgnoreCase("percent")) {
                percent = Double.parseDouble(wn2.getTextContent());
            } else if (wn2.getNodeName().equalsIgnoreCase("booster")) {
                if (wn2.getTextContent().equalsIgnoreCase("true"))
                    booster = true;
                else
                    booster = false;
            } else if (wn2.getNodeName().equalsIgnoreCase("forQuad")) {
                if (wn2.getTextContent().equalsIgnoreCase("true"))
                    forQuad = true;
                else
                    forQuad = false;
            }
        }
    }

    @Override
    public int getAvailability(int era) {
        if(era == EquipmentType.ERA_CLAN) {
            return EquipmentType.RATING_E;
        } else {
            return EquipmentType.RATING_X;
        }
    }

    @Override
    public int getTechRating() {
       return EquipmentType.RATING_E;
    }

    @Override
    public int getTechLevel() {
        return TechConstants.T_CLAN_TW;
    }

    @Override
    public int getTechBase() {
        return T_CLAN;
    }

    public boolean forQuad() {
        return forQuad;
    }

    @Override
    public boolean isAcceptableReplacement(Part part, boolean refit) {
        if(get(Installable.class).getMainLocation() == Protomech.LOC_TORSO && !refit) {
            //you can't replace a center torso
            return false;
        }
        if(part instanceof ProtomekLocation) {
            ProtomekLocation mekLoc = (ProtomekLocation)part;
            return mekLoc.get(Installable.class).getMainLocation() == get(Installable.class).getMainLocation()
                && mekLoc.get(Installable.class).getUnitTonnage() == get(Installable.class).getUnitTonnage()
                && mekLoc.hasBooster() == booster
                && (!isLeg() || mekLoc.forQuad() == forQuad);
                //&& mekLoc.getStructureType() == structureType;
        }
        return false;
    }

    private boolean isLeg() {
        return get(Installable.class).getMainLocation() == Protomech.LOC_LEG;
    }

    @Override
    public String checkFixable() {
        Unit unit = get(Installable.class).getUnit();
        if(null == unit) {
             return null;
         }
        int loc = get(Installable.class).getMainLocation();
        //there must be no usable equipment currently in the location
        //you can only salvage a location that has nothing left on it
        for (int i = 0; i < unit.getEntity().getNumberOfCriticals(loc); i++) {
            CriticalSlot slot = unit.getEntity().getCritical(loc, i);
            // ignore empty & non-hittable slots
            if ((slot == null) || !slot.isEverHittable()) {
                continue;
            }
            if (slot.isRepairable()) {
                return "Repairable parts in " + unit.getEntity().getLocationName(loc) + " must be salvaged or scrapped first. They can then be re-installed.";
            }
        }
        return null;
    }

    @Override
    public Part getNewPart() {
        return new ProtomekLocation(get(Installable.class).getMainLocation(),
            get(Installable.class).getUnitTonnage(), structureType, booster, forQuad, campaign);
    }

    private int getAppropriateSystemIndex() {
        switch(get(Installable.class).getMainLocation()) {
        case(Protomech.LOC_LEG):
            return Protomech.SYSTEM_LEGCRIT;
        case(Protomech.LOC_LARM):
        case(Protomech.LOC_RARM):
            return Protomech.SYSTEM_ARMCRIT;
        case(Protomech.LOC_HEAD):
            return Protomech.SYSTEM_HEADCRIT;
        case(Protomech.LOC_TORSO):
            return Protomech.SYSTEM_TORSOCRIT;
        default:
            return -1;
        }
    }

    @Override
    public void updateConditionFromPart() {
        Unit unit = get(Installable.class).getUnit();
        if(null != unit) {
            int loc = get(Installable.class).getMainLocation();
            unit.getEntity().setInternal(IArmorState.ARMOR_DESTROYED, loc);
            //need to assign all possible crits to the appropriate system
            unit.destroySystem(CriticalSlot.TYPE_SYSTEM, getAppropriateSystemIndex(), loc);
        }
    }

    @Override
    public void fix() {
        Part replacement = findReplacement(false);
        Unit unit = get(Installable.class).getUnit();
        if(null != replacement && null != unit) {
            Part actualReplacement = replacement.clone();
            unit.addPart(actualReplacement);
            campaign.addPart(actualReplacement, 0);
            replacement.decrementQuantity();
            remove(false);
            actualReplacement.updateConditionFromPart();
        }
    }

    @Override
    public int getIntroDate() {
        return 3055;
    }

    @Override
    public int getExtinctDate() {
        return EquipmentType.DATE_NONE;
    }

    @Override
    public int getReIntroDate() {
        return EquipmentType.DATE_NONE;
    }
}
