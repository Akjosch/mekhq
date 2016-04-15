/*
 * MissingSpacecraftEngine.java
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
import megamek.common.TechConstants;
import mekhq.campaign.Campaign;
import mekhq.campaign.parts.component.Installable;

/**
 * 
 * @author Jay Lawson <jaylawson39 at yahoo.com>
 */
public class MissingSpacecraftEngine extends MissingPart {
    private static final long serialVersionUID = -6961398614705924172L;
    double engineTonnage;  
    boolean clan;
    
    public MissingSpacecraftEngine() {
        this(0, 0, null, false);
    }

    public MissingSpacecraftEngine(int tonnage, double etonnage, Campaign c, boolean clan) {
        super(c);
        this.engineTonnage = etonnage;
        this.clan = clan;
        this.name = "Spacecraft Engine";
        get(Installable.class).setUnitTonnage(tonnage);
        get(Installable.class).setTonnageLimited(true);
    }
    
    @Override 
    public int getBaseTime() {
        return 43200;
    }
    
    @Override
    public int getDifficulty() {
        return 1;
    }
    
    @Override
    public double getTonnage() {
        return engineTonnage;
    }

    @Override
    public boolean isAcceptableReplacement(Part part, boolean refit) {
        return part instanceof SpacecraftEngine
                && getName().equals(part.getName())
                && getTonnage() == ((SpacecraftEngine)part).getTonnage();
    }

    @Override
    public int getTechLevel() {
        if(clan) {
            return TechConstants.T_CLAN_TW;
        } else {
            return TechConstants.T_IS_TW_NON_BOX;
        }
    }

    @Override
    protected void loadFieldsFromXmlNode(Node wn) {
        NodeList nl = wn.getChildNodes();
        
        for (int x=0; x<nl.getLength(); x++) {
            Node wn2 = nl.item(x);
            
            if (wn2.getNodeName().equalsIgnoreCase("engineTonnage")) {
                engineTonnage = Double.parseDouble(wn2.getTextContent());
            } else if (wn2.getNodeName().equalsIgnoreCase("clan")) {
                if(wn2.getTextContent().equalsIgnoreCase("true")) {
                    clan = true;
                } else {
                    clan = false;
                }
            } 
        }
    }

    @Override
    public int getAvailability(int era) {
        if(era == EquipmentType.ERA_SL) {
            return EquipmentType.RATING_C;
        } else if(era == EquipmentType.ERA_SW) {
            return EquipmentType.RATING_E;
        } else {
            return EquipmentType.RATING_C;
        }
    }

    @Override
    public int getTechRating() {
        return EquipmentType.RATING_D;
    }

    @Override
    public Part getNewPart() {
        return new SpacecraftEngine(get(Installable.class).getUnitTonnage(), engineTonnage, campaign, clan);
    }

    @Override
    public void updateConditionFromPart() {
        Aero aero = get(Installable.class).getEntity(Aero.class);
        if(null != aero) {
            aero.setEngineHits(3);
        }
    }
    
    @Override
    public String checkFixable() {
        return null;
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
