/*
 * MekSensor.java
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

import megamek.common.Compute;
import megamek.common.CriticalSlot;
import megamek.common.Entity;
import megamek.common.EquipmentType;
import megamek.common.Mech;
import megamek.common.TechConstants;
import mekhq.campaign.Campaign;
import mekhq.campaign.parts.component.Installable;
import mekhq.campaign.personnel.SkillType;
import mekhq.campaign.unit.Unit;

/**
 *
 * @author Jay Lawson <jaylawson39 at yahoo.com>
 */
public class MekSensor extends Part {
    private static final long serialVersionUID = 931907976883324097L;
    
    private boolean torsoMounted;

    public MekSensor() {
        this(0, false, null);
    }

    public MekSensor(int tonnage, boolean torsoMounted, Campaign c) {
        super(c);
        this.name = "Mech Sensors";
        this.torsoMounted = torsoMounted;
        add(new Installable());
        get(Installable.class).setUnitTonnage(tonnage);
        get(Installable.class).setTonnageLimited(true);
        if(torsoMounted) {
            get(Installable.class).setLocations(Mech.LOC_HEAD, Mech.LOC_CT);
        } else {
            get(Installable.class).setLocations(Mech.LOC_HEAD);
        }
    }

    public MekSensor clone() {
        MekSensor clone = new MekSensor(get(Installable.class).getUnitTonnage(), torsoMounted, campaign);
        clone.copyBaseData(this);
        return clone;
    }

    @Override
    public double getTonnage() {
        //TODO: what should this tonnage be?
        return 0;
    }

    @Override
    public long getStickerPrice() {
        return 2000 * get(Installable.class).getUnitTonnage();
    }

    @Override
    public boolean isSamePartType(Part part) {
        return part instanceof MekSensor 
            && get(Installable.class).getUnitTonnage() == part.get(Installable.class).getUnitTonnage()
            && torsoMounted == ((MekSensor) part).torsoMounted;
    }

    @Override
    public int getTechLevel() {
        return TechConstants.T_ALLOWED_ALL;
    }

    @Override
    protected void loadFieldsFromXmlNode(Node wn) {
        // Do nothing - no fields to load.
    }

    @Override
    public int getAvailability(int era) {
        return EquipmentType.RATING_C;
    }

    @Override
    public int getTechRating() {
        return EquipmentType.RATING_C;
    }

    @Override
    public void fix() {
        super.fix();
        Unit unit = get(Installable.class).getUnit();
        if(null != unit) {
            unit.repairSystem(CriticalSlot.TYPE_SYSTEM, Mech.SYSTEM_SENSORS);
        }
    }

    @Override
    public MissingPart getMissingPart() {
        return new MissingMekSensor(get(Installable.class).getUnitTonnage(), torsoMounted, campaign);
    }

    @Override
    public void remove(boolean salvage) {
        Unit unit = get(Installable.class).getUnit();
        if(null != unit) {
            unit.destroySystem(CriticalSlot.TYPE_SYSTEM, Mech.SYSTEM_SENSORS);
            Part spare = campaign.checkForExistingSparePart(this);
            if(!salvage) {
                campaign.removePart(this);
            } else if(null != spare) {
                spare.incrementQuantity();
                campaign.removePart(this);
            }
            unit.removePart(this);
            Part missing = getMissingPart();
            unit.addPart(missing);
            campaign.addPart(missing, 0);
        }
        get(Installable.class).setUnit(null);
        updateConditionFromEntity(false);
    }

    @Override
    public void updateConditionFromEntity(boolean checkForDestruction) {
        Unit unit = get(Installable.class).getUnit();
        if(null != unit) {
            int priorHits = hits;
            Entity entity = unit.getEntity();
            for (int i = 0; i < entity.locations(); i++) {
                if (entity.getNumberOfCriticals(CriticalSlot.TYPE_SYSTEM, Mech.SYSTEM_SENSORS, i) > 0) {
                    if (!unit.isSystemMissing(Mech.SYSTEM_SENSORS, i)) {
                        hits = entity.getDamagedCriticals(CriticalSlot.TYPE_SYSTEM, Mech.SYSTEM_SENSORS, i);
                        break;
                    } else {
                        remove(false);
                        return;
                    }
                }
            }
            if(checkForDestruction
                    && hits > priorHits && hits >= 2
                    && Compute.d6(2) < campaign.getCampaignOptions().getDestroyPartTarget()) {
                remove(false);
                return;
            }
        }
    }

    @Override
    public int getBaseTime() {
        if(isSalvaging()) {
            return 260;
        }
        if(hits > 1) {
            return 150;
        }
        return 75;
    }

    @Override
    public int getDifficulty() {
        if(isSalvaging()) {
            return 0;
        }
        if(hits > 1) {
            return 3;
        }
        return 0;
    }

    @Override
    public boolean needsFixing() {
        return hits > 0;
    }

    @Override
    public void updateConditionFromPart() {
        Unit unit = get(Installable.class).getUnit();
        if(null != unit) {
            if(hits == 0) {
                unit.repairSystem(CriticalSlot.TYPE_SYSTEM, Mech.SYSTEM_SENSORS);
            } else {
                unit.damageSystem(CriticalSlot.TYPE_SYSTEM, Mech.SYSTEM_SENSORS, hits);
            }
        }
    }

    @Override
    public String checkFixable() {
        Unit unit = get(Installable.class).getUnit();
        if(null == unit) {
            return null;
        }
        if(isSalvaging()) {
            return null;
        }
        for(int i = 0; i < unit.getEntity().locations(); i++) {
            if(unit.getEntity().getNumberOfCriticals(CriticalSlot.TYPE_SYSTEM, Mech.SYSTEM_SENSORS, i) > 0) {
                if(unit.isLocationBreached(i)) {
                    return unit.getEntity().getLocationName(i) + " is breached.";
                }
                if(unit.isLocationDestroyed(i)) {
                    return unit.getEntity().getLocationName(i) + " is destroyed.";
                }

            }
        }
        return null;
    }

    @Override
    public String getDetails() {
        return super.getDetails() + ", " + get(Installable.class).getUnitTonnage() + " tons";
    }

    @Override
    public boolean isPartForEquipmentNum(int index, int loc) {
        return Mech.SYSTEM_SENSORS == index;
    }

    @Override
    public boolean isRightTechType(String skillType) {
        return skillType.equals(SkillType.S_TECH_MECH);
    }

    @Override
    public int getIntroDate() {
        return EquipmentType.DATE_NONE;
    }

    @Override
    public int getExtinctDate() {
        return EquipmentType.DATE_NONE;
    }

    @Override
    public int getReIntroDate() {
        return EquipmentType.DATE_NONE;
    }
    
    public boolean isTorsoMounted() {
        return torsoMounted;
    }
}
