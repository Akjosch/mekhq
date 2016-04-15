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

import megamek.common.Aero;
import megamek.common.Entity;
import megamek.common.EquipmentType;
import mekhq.campaign.Campaign;
import mekhq.campaign.parts.component.Installable;

import org.w3c.dom.Node;

/**
 *
 * @author Jay Lawson <jaylawson39 at yahoo.com>
 */
public class MissingAvionics extends MissingPart {
    private static final long serialVersionUID = 2806921577150714477L;

    public MissingAvionics() {
        this(null);
    }
    
    public MissingAvionics(Campaign c) {
        super(c);
        this.name = "Avionics"; //$NON-NLS-1$
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
        return new Avionics(campaign);
    }

    @Override
    public boolean isAcceptableReplacement(Part part, boolean refit) {
        return part instanceof Avionics;
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
        //go with conventional fighter avionics
        if(era == EquipmentType.ERA_SL) {
            return EquipmentType.RATING_C;
        } else if(era == EquipmentType.ERA_SW) {
            return EquipmentType.RATING_D;
        } else {
            return EquipmentType.RATING_C;
        }
    }

    @Override
    public void updateConditionFromPart() {
        Aero aero = get(Installable.class).getEntity(Aero.class);
        if(null != aero) {
            aero.setAvionicsHits(3);
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