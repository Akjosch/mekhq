/*
 * MissingDropshipDockingCollar.java
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

import java.io.PrintWriter;

import org.w3c.dom.Node;

import megamek.common.Dropship;
import megamek.common.EquipmentType;
import megamek.common.TechConstants;
import mekhq.campaign.Campaign;
import mekhq.campaign.parts.component.Installable;

/**
 *
 * @author Jay Lawson <jaylawson39 at yahoo.com>
 */
public class MissingDropshipDockingCollar extends MissingPart {
    private static final long serialVersionUID = -717866644605314883L;
    
    public MissingDropshipDockingCollar() {
        this(null);
    }
    
    public MissingDropshipDockingCollar(Campaign c) {
        super(c);
        this.name = "Dropship Docking Collar"; //$NON-NLS-1$
    }
    
    @Override 
    public int getBaseTime() {
        return 2880;
    }
    
    @Override
    public int getDifficulty() {
        return -2;
    }

    @Override
    public void updateConditionFromPart() {
        Dropship dropship = get(Installable.class).getEntity(Dropship.class);
        if(null != dropship) {
            dropship.setDamageDockCollar(true);
        }
    }

    @Override
    public Part getNewPart() {
        return new DropshipDockingCollar(campaign);
    }

    @Override
    public String checkFixable() {
        return null;
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
        return TechConstants.T_IS_TW_ALL;
    }
    
    @Override 
    public int getTechBase() {
        return T_BOTH;    
    }
    
    @Override
    public void writeToXml(PrintWriter pw1, int indent) {
        writeToXmlBegin(pw1, indent);
        writeToXmlEnd(pw1, indent);
    }

    @Override
    protected void loadFieldsFromXmlNode(Node wn) {
        //nothing
    }

    @Override
    public boolean isAcceptableReplacement(Part part, boolean refit) {
        return part instanceof DropshipDockingCollar;
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