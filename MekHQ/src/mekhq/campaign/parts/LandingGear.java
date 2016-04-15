/*
 * LandingGear.java
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

import megamek.common.Aero;
import megamek.common.Compute;
import megamek.common.EquipmentType;
import megamek.common.TechConstants;
import mekhq.campaign.Campaign;
import mekhq.campaign.parts.component.Installable;
import mekhq.campaign.personnel.SkillType;

/**
 *
 * @author Jay Lawson <jaylawson39 at yahoo.com>
 */
public class LandingGear extends Part {
    private static final long serialVersionUID = -717866644605314883L;

    public LandingGear() {
        this(0, null);
    }
    
    public LandingGear(double tonnage, Campaign c) {
        super(c);
        this.name = "Landing Gear"; //$NON-NLS-1$
        add(new Installable());
        get(Installable.class).setUnitTonnage(tonnage);
        get(Installable.class).setTonnageLimited(true);
    }
        
    @Override
    public LandingGear clone() {
        LandingGear clone = new LandingGear(0, campaign);
        clone.copyBaseData(this);
        return clone;
    }
    
    @Override
    public void updateConditionFromEntity(boolean checkForDestruction) {
        int priorHits = hits;
        Aero aero = get(Installable.class).getEntity(Aero.class);
        if(null != aero) {
            if(aero.isGearHit()) {
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
            return 1200;
        }
        return 120;
    }
    
    @Override
    public int getDifficulty() {
        if(isSalvaging()) {
            return 3;
        }
        return 2;
    }

    @Override
    public void updateConditionFromPart() {
        Aero aero = get(Installable.class).getEntity(Aero.class);
        if(null != aero) {
            aero.setGearHit(needsFixing());
        }
        
    }

    @Override
    public void fix() {
        super.fix();
        Aero aero = get(Installable.class).getEntity(Aero.class);
        if(null != aero) {
            aero.setGearHit(false);
        }
    }

    @Override
    public void remove(boolean salvage) {
        Aero aero = get(Installable.class).getEntity(Aero.class);
        if(null != aero) {
            aero.setGearHit(true);
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
        return new MissingLandingGear(get(Installable.class).getUnitTonnage(), campaign);
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
        return Math.round(10.0 * get(Installable.class).getUnitTonnage());
    }

    @Override
    public double getTonnage() {
        return 0;
    }

    @Override
    public int getTechRating() {
        //go with conventional fighter avionics
        return EquipmentType.RATING_B;
    }

    @Override
    public int getAvailability(int era) {
        return EquipmentType.RATING_C;
    }
    
    @Override
    public int getTechLevel() {
        return TechConstants.T_ALLOWED_ALL;
    }

    @Override
    public boolean isSamePartType(Part part) {
        return part instanceof LandingGear;
    }

    @Override
    protected void loadFieldsFromXmlNode(Node wn) {
        //nothing to load
    }
    
    @Override
    public boolean isRightTechType(String skillType) {
        return skillType.equals(SkillType.S_TECH_AERO);
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
}