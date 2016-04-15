/*
 * MissingVeeSensor.java
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

import megamek.common.EquipmentType;
import megamek.common.Tank;
import mekhq.campaign.Campaign;
import mekhq.campaign.parts.component.Installable;

/**
 *
 * @author Jay Lawson <jaylawson39 at yahoo.com>
 */
public class MissingVeeSensor extends MissingPart {
    private static final long serialVersionUID = 2806921577150714477L;

    public MissingVeeSensor() {
        this(null);
    }
    
    public MissingVeeSensor(Campaign c) {
        super(c);
        this.name = "Vehicle Sensors"; //$NON-NLS-1$
    }
    
    @Override 
    public int getBaseTime() {
        return 260;
    }
    
    @Override
    public int getDifficulty() {
        return 0;
    }
    
    @Override
    public String checkFixable() {
        return null;
    }

    @Override
    public Part getNewPart() {
        return new VeeSensor(campaign);
    }

    @Override
    public boolean isAcceptableReplacement(Part part, boolean refit) {
        return part instanceof VeeSensor;
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
        Tank tank = get(Installable.class).getEntity(Tank.class);
        if(null != tank) {
            tank.setSensorHits(4);
        }
    }
    
    @Override
    protected void loadFieldsFromXmlNode(Node wn) {
        //nothing to load
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