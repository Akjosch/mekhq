/*
 * MissingProtomekActuator.java
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
import megamek.common.Protomech;
import megamek.common.TechConstants;
import mekhq.campaign.Campaign;
import mekhq.campaign.parts.component.Installable;
import mekhq.campaign.unit.Unit;

/**
 *
 * @author Jay Lawson <jaylawson39 at yahoo.com>
 */
public class MissingProtomekArmActuator extends MissingPart {
    private static final long serialVersionUID = 719878556021696393L;
    protected int location;

    public MissingProtomekArmActuator() {
        this(0, 0, null);
    }
    
    public MissingProtomekArmActuator(int tonnage, Campaign c) {
        this(tonnage, -1, c);
    }
    
    public MissingProtomekArmActuator(int tonnage, int loc, Campaign c) {
        super(tonnage, c);
        this.name = "Protomech Arm Actuator";
        this.location = loc;
    }
    
    @Override 
    public int getBaseTime() {
        return 120;
    }
    
    @Override
    public int getDifficulty() {
        return 0;
    }
    
    public void setLocation(int loc) {
        this.location = loc;
    }

    @Override
    public double getTonnage() {
        //TODO: how much do actuators weight?
        //apparently nothing
        return 0;
    }
    
    public int getLocation() {
        return location;
    }
    
    @Override
    protected void loadFieldsFromXmlNode(Node wn) {
        NodeList nl = wn.getChildNodes();
        
        for (int x=0; x<nl.getLength(); x++) {
            Node wn2 = nl.item(x);
            
            if (wn2.getNodeName().equalsIgnoreCase("location")) {
                location = Integer.parseInt(wn2.getTextContent());
            } 
        }
    }

    @Override
    public int getAvailability(int era) {
        if(era == EquipmentType.ERA_CLAN) {
            return EquipmentType.RATING_D;
        } else {
            return EquipmentType.RATING_X;
        }
    }

    @Override
    public int getTechRating() {
        return EquipmentType.RATING_D;
    }
    
    @Override
    public int getTechBase() {
        return T_CLAN;
    }
    
    @Override
    public int getTechLevel() {
        return TechConstants.T_CLAN_TW;
    }

    @Override
    public void updateConditionFromPart() {
        Unit unit = get(Installable.class).getUnit();
        if(null != unit) {
              unit.destroySystem(CriticalSlot.TYPE_SYSTEM, Protomech.SYSTEM_ARMCRIT, location, 1);
        }
    }

    @Override
    public String checkFixable() {
        Unit unit = get(Installable.class).getUnit();
        if(null == unit) {
            return null;
        }
        if(unit.isLocationBreached(location)) {
            return unit.getEntity().getLocationName(location) + " is breached.";
        }
        if(unit.isLocationDestroyed(location)) {
            return unit.getEntity().getLocationName(location) + " is destroyed.";
        }
        return null;
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
            ((ProtomekArmActuator)actualReplacement).setLocation(location);
            remove(false);
            //assign the replacement part to the unit           
            actualReplacement.updateConditionFromPart();
        }
    }
    
    @Override
    public boolean isAcceptableReplacement(Part part, boolean refit) {
        return part instanceof ProtomekArmActuator
                && get(Installable.class).getUnitTonnage() == part.get(Installable.class).getUnitTonnage();
    }

    @Override
    public Part getNewPart() {
        return new ProtomekArmActuator(get(Installable.class).getUnitTonnage(), location, campaign);
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
