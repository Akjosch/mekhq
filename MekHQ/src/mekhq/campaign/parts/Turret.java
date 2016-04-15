/*
 * Turret.java
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
import megamek.common.Entity;
import megamek.common.EquipmentType;
import megamek.common.IArmorState;
import megamek.common.Tank;
import mekhq.campaign.Campaign;
import mekhq.campaign.parts.component.Installable;
import mekhq.campaign.unit.Unit;

/**
 *
 * @author Jay Lawson <jaylawson39 at yahoo.com>
 */
public class Turret extends TankLocation {
    private static final long serialVersionUID = -122291037522319765L;
    protected double weight;

    public Turret() {
        this(0, 0.0, null);
    }
    
    public Turret(int loc, Campaign c) {
        this(loc, 0.0, c);
    }
    
    @Override
    public Turret clone() {
        Turret clone = new Turret(0, weight, campaign);
        clone.copyBaseData(this);
        clone.get(Installable.class).setLocations(get(Installable.class).getLocations());
        clone.damage = this.damage;
        clone.breached = this.breached;
        return clone;
    }
    
    public Turret(int loc, double weight, Campaign c) {
        super(loc, 0, c);
        this.weight = weight;
        this.name = "Turret";
        get(Installable.class).setTonnageLimited(false);
    }
    
    @Override
    public boolean isSamePartType(Part part) {
        return part instanceof Turret 
                && get(Installable.class).getMainLocation() == part.get(Installable.class).getMainLocation() 
                && getTonnage() == ((Turret)part).getTonnage();
    }

    @Override
    protected void loadFieldsFromXmlNode(Node wn) {
        NodeList nl = wn.getChildNodes();
        
        for (int x=0; x<nl.getLength(); x++) {
            Node wn2 = nl.item(x);
            
            if (wn2.getNodeName().equalsIgnoreCase("weight")) {
                weight = Double.parseDouble(wn2.getTextContent());
            } else if (wn2.getNodeName().equalsIgnoreCase("loc")) {
                get(Installable.class).setLocations(Integer.parseInt(wn2.getTextContent()));
            } else if (wn2.getNodeName().equalsIgnoreCase("damage")) {
                damage = Integer.parseInt(wn2.getTextContent());
            }
        }
    }

    @Override
    public int getAvailability(int era) {
        return EquipmentType.RATING_C;
    }

    @Override
    public int getTechRating() {
        return EquipmentType.RATING_B;
    }

    @Override
    public MissingPart getMissingPart() {
        return new MissingTurret(weight, campaign);
    }

    @Override
    public void remove(boolean salvage) {
        Unit unit = get(Installable.class).getUnit();
        if(null != unit) {
            unit.getEntity().setInternal(IArmorState.ARMOR_DESTROYED, get(Installable.class).getMainLocation());
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
            ((Tank)unit.getEntity()).unlockTurret();
        }
        get(Installable.class).setUnit(null);
    }
    
    @Override 
    public int getBaseTime() {
        if(isSalvaging()) {
            return 160;
        }
        return 60;
    }
    
    @Override
    public int getDifficulty() {
        if(isSalvaging()) {
            return 1;
        }
        return 0;
    }

    @Override
    public void updateConditionFromPart() {
        Entity entity = get(Installable.class).getEntity();
        if(null != entity) {
            int loc = get(Installable.class).getMainLocation();
            entity.setInternal(entity.getOInternal(loc) - damage, loc);
        }
    }

    @Override 
    public String checkFixable() {
        Entity entity = get(Installable.class).getEntity();
        if(null == entity) {
            return null;
        }
        if(isSalvaging()) {
            int loc = get(Installable.class).getMainLocation();
            //check for armor
            if(entity.getArmorForReal(loc, false) > 0) {
                return "must salvage armor in this location first";
            }
            //you can only salvage a location that has nothing left on it
            for (int i = 0; i < entity.getNumberOfCriticals(loc); i++) {
                CriticalSlot slot = entity.getCritical(loc, i);
                // ignore empty & non-hittable slots
                if ((slot == null) || !slot.isEverHittable()) {
                    continue;
                }
                if (slot.isRepairable()) {
                    return "Repairable parts in " + entity.getLocationName(loc) + " must be salvaged or scrapped first.";
                } 
            }
        }
        return null;
    }
    
    @Override
    public String checkScrappable() {
        Entity entity = get(Installable.class).getEntity();
        if(null == entity) {
            return null;
        }
        int loc = get(Installable.class).getMainLocation();
        //check for armor
        if(entity.getArmor(loc, false) != IArmorState.ARMOR_DESTROYED) {
            return "You must scrap armor in the turret first";
        }
        //you can only scrap a location that has nothing left on it
        for (int i = 0; i < entity.getNumberOfCriticals(loc); i++) {
            CriticalSlot slot = entity.getCritical(loc, i);
            // ignore empty & non-hittable slots
            if ((slot == null) || !slot.isEverHittable()) {
                continue;
            }
            if (slot.isRepairable()) {
                return "You must scrap all equipment in the turret first";
            } 
        }
        return null;
    }
    
    @Override
    public boolean canNeverScrap() {
        return false;
    }
    
    @Override
    public String getDetails() {
        return weight + " tons, " + damage + " point(s) of damage";
    }

    @Override
    public double getTonnage() {
        return weight;
    }
    
    @Override
    public long getStickerPrice() {
        return (long)(5000 * weight);
    }
}
