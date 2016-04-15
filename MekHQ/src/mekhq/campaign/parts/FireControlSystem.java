/*
 * FireControlSystem.java
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

import megamek.common.Aero;
import megamek.common.Compute;
import megamek.common.EquipmentType;
import megamek.common.TechConstants;
import mekhq.campaign.Campaign;
import mekhq.campaign.parts.component.Installable;
import mekhq.campaign.personnel.SkillType;

/**
 * This class actually treats the Fire Control Computer and Gunnery Control Systems
 * as one part (see TM 283).
 * 
 * @author Jay Lawson <jaylawson39 at yahoo.com>
 */
public class FireControlSystem extends Part {
    private static final long serialVersionUID = -717866644605314883L;
    
    private int firingArcs;
    
    public FireControlSystem() {
        this(0, null);
    }
    
    public FireControlSystem(int firingArcs, Campaign c) {
        super(c);
        this.firingArcs = firingArcs;
        this.name = "Fire Control System"; //$NON-NLS-1$
        add(new Installable());
    }
        
    public FireControlSystem clone() {
        FireControlSystem clone = new FireControlSystem(firingArcs, campaign);
        clone.copyBaseData(this);
        return clone;
    }
    
    @Override
    public void updateConditionFromEntity(boolean checkForDestruction) {
        int priorHits = hits;
        Aero aero = get(Installable.class).getEntity(Aero.class);
        if(null != aero) {
            hits = aero.getFCSHits();
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
            return 4320;
        }
        return 120;
    }
    
    @Override
    public int getDifficulty() {
        if(isSalvaging()) {
            return 0;
        }
        return 1;
    }

    @Override
    public void updateConditionFromPart() {
        Aero aero = get(Installable.class).getEntity(Aero.class);
        if(null != aero) {
            aero.setFCSHits(hits);
        }
    }

    @Override
    public void fix() {
        super.fix();
        Aero aero = get(Installable.class).getEntity(Aero.class);
        if(null != aero) {
            aero.setFCSHits(0);
        }
    }

    @Override
    public void remove(boolean salvage) {
        Aero aero = get(Installable.class).getEntity(Aero.class);
        if(null != aero) {
            aero.setFCSHits(3);
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
        return new MissingFireControlSystem(firingArcs, campaign);
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
        return 10000L * (10 + firingArcs);
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
        return EquipmentType.RATING_C;
    }
    
    @Override
    public int getTechLevel() {
        return TechConstants.T_ALLOWED_ALL;
    }

    @Override
    public boolean isSamePartType(Part part) {
        return part instanceof FireControlSystem && firingArcs == ((FireControlSystem) part).firingArcs;
    }
    
    @Override
    public boolean isRightTechType(String skillType) {
        return skillType.equals(SkillType.S_TECH_AERO);
    }
    
    @Override
    protected void loadFieldsFromXmlNode(Node wn) {
        NodeList nl = wn.getChildNodes();
        
        for (int x=0; x<nl.getLength(); x++) {
            Node wn2 = nl.item(x);        
            if (wn2.getNodeName().equalsIgnoreCase("firingArcs")) {
                firingArcs = Integer.parseInt(wn2.getTextContent());
            } 
        }
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

    public int getFiringArcs() {
        return firingArcs;
    }
}