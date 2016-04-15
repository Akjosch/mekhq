/*
 * MotiveSystem.java
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

import megamek.common.EquipmentType;
import megamek.common.Tank;
import megamek.common.TechConstants;
import mekhq.campaign.Campaign;
import mekhq.campaign.parts.component.Installable;

/**
 *
 * @author Jay Lawson <jaylawson39 at yahoo.com>
 */
public class MotiveSystem extends Part {
    private static final long serialVersionUID = -5637743997294510810L;

    int damage;
    int penalty;
    
    public MotiveSystem() {
        this(null);
    }
    
    public MotiveSystem(Campaign c) {
        super(c);
        this.name = "Motive System";
        this.damage = 0;
        this.penalty = 0;
        add(new Installable());
    }
    
    @Override 
    public int getBaseTime() {
        return 60;
    }
    
    @Override
    public int getDifficulty() {
        return -1;
    }
    
    public MotiveSystem clone() {
        MotiveSystem clone = new MotiveSystem(campaign);
        clone.copyBaseData(this);
        return clone;
    }
    
    @Override
    public int getAvailability(int era) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long getStickerPrice() {
        // TODO Auto-generated method stub
        return 0;
    }
    
    @Override
    public int getTechLevel() {
        return TechConstants.T_ALLOWED_ALL;
    }

    @Override
    public int getTechRating() {
        return EquipmentType.RATING_C;
    }

    @Override
    public double getTonnage() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public boolean isSamePartType(Part part) {
        return part instanceof MotiveSystem;
    }

    @Override
    protected void loadFieldsFromXmlNode(Node wn) {
        NodeList nl = wn.getChildNodes();
        
        for (int x=0; x<nl.getLength(); x++) {
            Node wn2 = nl.item(x);
            
            if (wn2.getNodeName().equalsIgnoreCase("damage")) {
                damage = Integer.parseInt(wn2.getTextContent());
            } else if (wn2.getNodeName().equalsIgnoreCase("penalty")) {
                penalty = Integer.parseInt(wn2.getTextContent());
            } 
        }
        
    }

    @Override
    public String checkFixable() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void fix() {
        super.fix();
        damage = 0;
        penalty = 0;
        Tank tank = get(Installable.class).getEntity(Tank.class);
        if(null != tank) {
            tank.resetMovementDamage();
        }
    }

    @Override
    public MissingPart getMissingPart() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void remove(boolean salvage) {
        // you can't do this so nothing here
        
    }

    @Override
    public void updateConditionFromEntity(boolean checkForDestruction) {
        //motive systems don't have to check for destruction since they 
        //cannot be removed
        Tank tank = get(Installable.class).getEntity(Tank.class);
        if(null != tank) {
            damage = tank.getMotiveDamage();
            penalty = tank.getMotivePenalty();
        }
    }

    @Override
    public void updateConditionFromPart() {
        //you can't get here so, dont worry about it
    }

    @Override
    public boolean needsFixing() {
        return damage > 0 || penalty > 0;
    }
    
    @Override
    public String getDetails() {
        return "-" + damage + " MP/-" + penalty + " Piloting";
    }
    
    @Override
    public String checkScrappable() {
        return "Motive type cannot be scrapped";
    }
    
    @Override
    public boolean canNeverScrap() {
        return true;
    }

    @Override
    public boolean isSalvaging() {
        return false;
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