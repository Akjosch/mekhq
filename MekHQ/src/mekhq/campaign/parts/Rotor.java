/*
 * Rotor.java
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

import megamek.common.EquipmentType;
import megamek.common.IArmorState;
import megamek.common.VTOL;
import mekhq.campaign.Campaign;
import mekhq.campaign.parts.component.Installable;

/**
 *
 * @author Jay Lawson <jaylawson39 at yahoo.com>
 */
public class Rotor extends TankLocation {
    private static final long serialVersionUID = -122291037522319765L;

    public Rotor() {
        this(0, null);
    }
    
    public Rotor(int tonnage, Campaign c) {
        super(VTOL.LOC_ROTOR, tonnage, c);
        this.name = "Rotor";
        this.damage = 0;
    }
    
    public Rotor clone() {
        Rotor clone = new Rotor(get(Installable.class).getUnitTonnage(), campaign);
        clone.copyBaseData(this);
        clone.get(Installable.class).setLocations(get(Installable.class).getLocations());
        clone.damage = this.damage;
        clone.breached = this.breached;
        return clone;
    }
 
    @Override
    public boolean isSamePartType(Part part) {
        return part instanceof Rotor 
                && get(Installable.class).getMainLocation() == part.get(Installable.class).getMainLocation()
                && get(Installable.class).getUnitTonnage() == part.get(Installable.class).getUnitTonnage()
                && getDamage() == ((Rotor)part).getDamage()
                && part.getSkillMin() == getSkillMin();
    }

    @Override
    public int getAvailability(int era) {
        //go with conventional fighter avionics
        if(era == EquipmentType.ERA_SL) {
            return EquipmentType.RATING_C;
        } else if(era == EquipmentType.ERA_SW) {
            return EquipmentType.RATING_D;
        } else {
            return EquipmentType.RATING_C;
        }
    }

    @Override
    public int getTechRating() {
        return EquipmentType.RATING_B;
    }

    @Override
    public void fix() {
        super.fix();
        damage--;
        VTOL vtol = get(Installable.class).getEntity(VTOL.class);
        if(null != vtol) {
            int currIsVal = vtol.getInternal(VTOL.LOC_ROTOR); 
            int maxIsVal = vtol.getOInternal(VTOL.LOC_ROTOR); 
            int repairedIsVal = Math.min(maxIsVal, currIsVal + 1);
            vtol.setInternal(repairedIsVal, VTOL.LOC_ROTOR);
        }
    }

    @Override
    public MissingPart getMissingPart() {
        return new MissingRotor(get(Installable.class).getUnitTonnage(), campaign);
    }

    @Override
    public void remove(boolean salvage) {
        VTOL vtol = get(Installable.class).getEntity(VTOL.class);
        if(null != vtol) {
            vtol.setInternal(IArmorState.ARMOR_DESTROYED, VTOL.LOC_ROTOR);
            Part spare = campaign.checkForExistingSparePart(this);
            if(!salvage) {
                campaign.removePart(this);
            } else if(null != spare) {
                spare.incrementQuantity();
                campaign.removePart(this);
            }
            get(Installable.class).getUnit().removePart(this);
            Part missing = getMissingPart();
            get(Installable.class).getUnit().addPart(missing);
            campaign.addPart(missing, 0);
            vtol.resetMovementDamage();
            for(Part part : get(Installable.class).getUnit().getParts()) {
                if(part instanceof MotiveSystem) {
                    part.updateConditionFromEntity(false);
                }
            }
        }
        get(Installable.class).setUnit(null);
    }
    
    @Override 
    public int getBaseTime() {
        if(isSalvaging()) {
            return 300;
        }
        return 120;
    }
    
    @Override
    public int getDifficulty() {
        if(isSalvaging()) {
            return 0;
        }
        return 2;
    }
    
    @Override
    public void updateConditionFromPart() {
        VTOL vtol = get(Installable.class).getEntity(VTOL.class);
        if(null != vtol) {
            vtol.setInternal(vtol.getOInternal(VTOL.LOC_ROTOR) - damage, VTOL.LOC_ROTOR);
        }
    }
    
    @Override 
    public String checkFixable() {
        VTOL vtol = get(Installable.class).getEntity(VTOL.class);
        if(null == vtol) {
            return null;
        }
        if(isSalvaging()) {
            //check for armor
            if(vtol.getArmorForReal(get(Installable.class).getMainLocation(), false) > 0) {
                return "must salvage armor in this location first";
            }
        }
        return null;
    }
    
    @Override
    public String checkScrappable() {
        VTOL vtol = get(Installable.class).getEntity(VTOL.class);
        //check for armor
        if(null != vtol) {
            if(vtol.getArmor(get(Installable.class).getMainLocation(), false) != IArmorState.ARMOR_DESTROYED) {
                return "You must scrap armor in the rotor first";
            }
        }
        return null;
    }
    
    @Override
    public boolean canNeverScrap() {
        return false;
    }
    
    @Override
    public double getTonnage() {
        return Math.ceil(0.2 * get(Installable.class).getUnitTonnage()) / 2.0;
    }

    @Override
    public long getStickerPrice() {
        return Math.round(40000L * getTonnage());
    }
}
