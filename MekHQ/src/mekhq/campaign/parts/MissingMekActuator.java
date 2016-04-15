/*
 * MissingMekActuator.java
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

import megamek.common.BipedMech;
import megamek.common.CriticalSlot;
import megamek.common.EquipmentType;
import megamek.common.Mech;
import mekhq.campaign.Campaign;
import mekhq.campaign.parts.component.Installable;
import mekhq.campaign.unit.Unit;

/**
 *
 * @author Jay Lawson <jaylawson39 at yahoo.com>
 */
public class MissingMekActuator extends MissingPart {
    private static final long serialVersionUID = 719878556021696393L;
    protected int type;

    public MissingMekActuator() {
        this(0, 0, null);
    }
    
    public int getType() {
        return type;
    }
    
    public MissingMekActuator(int tonnage, int type, Campaign c) {
        this(tonnage, type, -1, c);
    }
    
    public MissingMekActuator(double tonnage, int type, int loc, Campaign c) {
        super(c);
        this.type = type;
        Mech m = new BipedMech();
        this.name = m.getSystemName(type) + " Actuator" ;
        get(Installable.class).setLocations(loc);
        get(Installable.class).setUnitTonnage(tonnage);
        get(Installable.class).setTonnageLimited(true);
    }
    
    @Override 
    public int getBaseTime() {
        return 90;
    }
    
    @Override
    public int getDifficulty() {
        return -3;
    }

    @Override
    public double getTonnage() {
        //TODO: how much do actuators weight?
        //apparently nothing
        return 0;
    }
    
    @Override
    protected void loadFieldsFromXmlNode(Node wn) {
        NodeList nl = wn.getChildNodes();
        
        for (int x=0; x<nl.getLength(); x++) {
            Node wn2 = nl.item(x);
            
            if (wn2.getNodeName().equalsIgnoreCase("type")) {
                type = Integer.parseInt(wn2.getTextContent());
            } else if (wn2.getNodeName().equalsIgnoreCase("location")) {
                get(Installable.class).setLocations(Integer.parseInt(wn2.getTextContent()));
            } 
        }
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
        Part replacement = findReplacement(false);
        Unit unit = get(Installable.class).getUnit();
        if(null != replacement && null != unit) {
            Part actualReplacement = replacement.clone();
            unit.addPart(actualReplacement);
            campaign.addPart(actualReplacement, 0);
            replacement.decrementQuantity();
            ((MekActuator)actualReplacement).get(Installable.class).setLocations(get(Installable.class).getMainLocation());
            remove(false);
            //assign the replacement part to the unit            
            actualReplacement.updateConditionFromPart();
        }
    }
    
    @Override
    public boolean isAcceptableReplacement(Part part, boolean refit) {
        if(part instanceof MekActuator) {
            MekActuator actuator = (MekActuator)part;
            return actuator.getType() == type && get(Installable.class).getUnitTonnage() == actuator.get(Installable.class).getUnitTonnage();
        }
        return false;
    }
    
    @Override
    public String checkFixable() {
        Unit unit = get(Installable.class).getUnit();
        if(null == unit) {
             return null;
        }
        int location = get(Installable.class).getMainLocation();
        if(unit.isLocationBreached(location)) {
            return unit.getEntity().getLocationName(location) + " is breached.";
        }
        if(unit.isLocationDestroyed(location)) {
            return unit.getEntity().getLocationName(location) + " is destroyed.";
        }
        return null;
    }
    
    @Override
    public boolean onBadHipOrShoulder() {
        Unit unit = get(Installable.class).getUnit();
        return null != unit && unit.hasBadHipOrShoulder(get(Installable.class).getMainLocation());
    }

    @Override
    public Part getNewPart() {
        return new MekActuator(get(Installable.class).getUnitTonnage(), type, -1, campaign);
    }

    @Override
    public void updateConditionFromPart() {
        Unit unit = get(Installable.class).getUnit();
        if(null != unit) {
            unit.destroySystem(CriticalSlot.TYPE_SYSTEM, type, get(Installable.class).getMainLocation());
        }
    }
    
    @Override
    public boolean isOmniPoddable() {
        return type == Mech.ACTUATOR_LOWER_ARM || type == Mech.ACTUATOR_HAND;
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
