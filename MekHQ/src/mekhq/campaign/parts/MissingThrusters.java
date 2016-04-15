/*
 * MissingAvionics.java
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
import megamek.common.EquipmentType;
import mekhq.campaign.Campaign;
import mekhq.campaign.parts.component.Installable;
import mekhq.campaign.unit.Unit;

/**
 *
 * @author Jay Lawson <jaylawson39 at yahoo.com>
 */
public class MissingThrusters extends MissingPart {
    private static final long serialVersionUID = -7402791453470647853L;
    
    private boolean isLeftThrusters = false;

    public MissingThrusters() {
        this(null, false);
    }
    
    public MissingThrusters(Campaign c) {
        this(c, false);
    }
    
    public MissingThrusters(Campaign c, boolean left) {
        super(c);
        this.name = "Thrusters";
        isLeftThrusters = left;
    }
    
    @Override 
    public int getBaseTime() {
        return 4800;
    }
    
    @Override
    public int getDifficulty() {
        return 1;
    }
    
    @Override
    public String checkFixable() {
        return null;
    }

    @Override
    public Part getNewPart() {
        return new Thrusters(campaign, isLeftThrusters);
    }

    @Override
    public boolean isAcceptableReplacement(Part part, boolean refit) {
        return part instanceof Thrusters;
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
            ((Thrusters)actualReplacement).setLeftThrusters(isLeftThrusters);
            remove(false);
            //assign the replacement part to the unit            
            actualReplacement.updateConditionFromPart();
        }
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
    public void updateConditionFromPart() {
        Aero aero = get(Installable.class).getEntity(Aero.class);
        if(null != aero) {
            if (isLeftThrusters) {
                aero.setLeftThrustHits(4);
            } else {
                aero.setRightThrustHits(4);
            }
        }
    }
    
    @Override
    protected void loadFieldsFromXmlNode(Node wn) {
        NodeList nl = wn.getChildNodes();
        
        for (int x=0; x<nl.getLength(); x++) {
            Node wn2 = nl.item(x);
            
            if (wn2.getNodeName().equalsIgnoreCase("isLeftThrusters")) {
                isLeftThrusters = Boolean.parseBoolean(wn2.getTextContent());
            }
        }
    }
    
    public boolean isLeftThrusters() {
        return isLeftThrusters;
    }
    
    public void setLeftThrusters(boolean b) {
        isLeftThrusters = b;
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