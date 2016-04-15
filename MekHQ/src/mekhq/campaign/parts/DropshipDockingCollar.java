/*
 * DropshipDockingCollar.java
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
import megamek.common.Dropship;
import megamek.common.EquipmentType;
import megamek.common.TechConstants;
import mekhq.campaign.Campaign;
import mekhq.campaign.parts.component.Installable;
import mekhq.campaign.personnel.SkillType;

/**
 *
 * @author Jay Lawson <jaylawson39 at yahoo.com>
 */
public class DropshipDockingCollar extends Part {
    private static final long serialVersionUID = -717866644605314883L;
    
    public DropshipDockingCollar() {
        this(null);
    }
    
    public DropshipDockingCollar(Campaign c) {
        super(c);
        this.name = "Dropship Docking Collar"; //$NON-NLS-1$
        add(new Installable());
    }
    
    public DropshipDockingCollar clone() {
        DropshipDockingCollar clone = new DropshipDockingCollar(campaign);
        clone.copyBaseData(this);
        return clone;
    }
        
    @Override
    public void updateConditionFromEntity(boolean checkForDestruction) {
        int priorHits = hits;
        Dropship dropship = get(Installable.class).getEntity(Dropship.class);
        if(null != dropship) {
             if(dropship.isDockCollarDamaged()) {
                 hits = 1;
             } else { 
                 hits = 0;
             }
             if(checkForDestruction 
                     && hits > priorHits 
                     && Compute.d6(2) < campaign.getCampaignOptions().getDestroyPartTarget()) {
                 remove(false);
                 return;
             }
        }
    }
    
    @Override 
    public int getBaseTime() {
        if(isSalvaging()) {
            return 2880;
        }
        return 120;
    }
    
    @Override
    public int getDifficulty() {
        if(isSalvaging()) {
            return -2;
        }
        return 3;
    }

    @Override
    public void updateConditionFromPart() {
        Dropship dropship = get(Installable.class).getEntity(Dropship.class);
        if(null != dropship) {
            if(hits > 0) {
                dropship.setDamageDockCollar(true);
            } else {
                dropship.setDamageDockCollar(false);
            }
        }
        
    }

    @Override
    public void fix() {
        super.fix();
        Dropship dropship = get(Installable.class).getEntity(Dropship.class);
        if(null != dropship) {
            dropship.setDamageDockCollar(false);
        }
    }

    @Override
    public void remove(boolean salvage) {
        Dropship dropship = get(Installable.class).getEntity(Dropship.class);
        if(null != dropship) {
            dropship.setDamageDockCollar(true);
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
        }
        get(Installable.class).setUnit(null);
        updateConditionFromEntity(false);
    }

    @Override
    public MissingPart getMissingPart() {
        return new MissingDropshipDockingCollar(campaign);
    }

    @Override
    public String checkFixable() {
        return null;
    }

    @Override
    public boolean needsFixing() {
        return hits > 0;
    }

    @Override
    public long getStickerPrice() {
        return 10000;
    }
    
    @Override
    public double getTonnage() {
        return 0;
    }

    @Override
    public int getTechRating() {
        return EquipmentType.RATING_C;
    }

    @Override
    public int getAvailability(int era) {
        if(era == EquipmentType.ERA_SL) {
            return EquipmentType.RATING_C;
        } else if(era == EquipmentType.ERA_SW) {
            return EquipmentType.RATING_D;
        } else {
            return EquipmentType.RATING_C;
        }
    }
    
    @Override
    public int getTechLevel() {
        return TechConstants.T_ALLOWED_ALL;
    }

    @Override
    public boolean isSamePartType(Part part) {
        return part instanceof DropshipDockingCollar;
    }
    
    @Override
    protected void loadFieldsFromXmlNode(Node wn) {
        //nothing
    }
    
    @Override
    public boolean isRightTechType(String skillType) {
        return skillType.equals(SkillType.S_TECH_AERO);
    }

    @Override
    public int getIntroDate() {
        return 2304;
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