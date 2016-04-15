/*
 * MissingAeroSensor.java
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

/**
 *
 * @author Jay Lawson <jaylawson39 at yahoo.com>
 */
public class MissingAeroSensor extends MissingPart {

    /**
     * 
     */
    private static final long serialVersionUID = 2806921577150714477L;

    private boolean dropship;
    
    public MissingAeroSensor() {
        this(0, false, null);
    }
    
    public MissingAeroSensor(double tonnage, boolean drop, Campaign c) {
        super(c);
        this.name = "Aero Sensors";
        this.dropship = drop;
        get(Installable.class).setUnitTonnage(tonnage);
        get(Installable.class).setTonnageLimited(!drop);
    }
    
    @Override 
    public int getBaseTime() {
        return 1200;
    }
    
    @Override
    public int getDifficulty() {
        return -2;
    }
    
    @Override
    public String checkFixable() {
        return null;
    }

    @Override
    public Part getNewPart() {
        return new AeroSensor(get(Installable.class).getUnitTonnage(), dropship, campaign);
    }

    @Override
    public boolean isAcceptableReplacement(Part part, boolean refit) {
        return part instanceof AeroSensor && dropship == ((AeroSensor)part).isForDropShip()
                && (dropship || get(Installable.class).getUnitTonnage() == part.get(Installable.class).getUnitTonnage());
    }

    @Override
    public double getTonnage() {
        return 0;
    }

    @Override
    public int getTechRating() {
        //go with ASF sensors
        return EquipmentType.RATING_C;
    }

    @Override
    public int getAvailability(int era) {
        //go with ASF sensors
        return EquipmentType.RATING_C;
    }
    
    @Override
    protected void loadFieldsFromXmlNode(Node wn) {
        NodeList nl = wn.getChildNodes();
        
        for (int x=0; x<nl.getLength(); x++) {
            Node wn2 = nl.item(x);        
            if (wn2.getNodeName().equalsIgnoreCase("dropship")) {
                if(wn2.getTextContent().trim().equalsIgnoreCase("true")) {
                    dropship = true;
                } else {
                    dropship = false;
                }
            }
        }
    }

    @Override
    public void updateConditionFromPart() {
        Aero aero = get(Installable.class).getEntity(Aero.class);
        if(null != aero) {
            aero.setSensorHits(3);
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
}