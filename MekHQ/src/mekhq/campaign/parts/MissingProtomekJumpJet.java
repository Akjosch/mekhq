/*
 * MissingProtomekJumpJet.java
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
public class MissingProtomekJumpJet extends MissingPart {
    private static final long serialVersionUID = 719878556021696393L;

    public MissingProtomekJumpJet() {
        this(0, null);
    }
    
    public MissingProtomekJumpJet(int tonnage, Campaign c) {
        super(c);
        this.name = "Protomech Jump Jet"; //$NON-NLS-1$
        get(Installable.class).setLocations(Protomech.LOC_TORSO);
        get(Installable.class).setUnitTonnage(tonnage);
        get(Installable.class).setTonnageLimited(true);
    }
    
    @Override 
	public int getBaseTime() {
		return 60;
	}
	
	@Override
	public int getDifficulty() {
		return 0;
	}
   
    @Override
    public double getTonnage() {
        if(get(Installable.class).getUnitTonnage() <= 5) {
            return 0.05;
        } else if (get(Installable.class).getUnitTonnage() <= 9) {
            return 0.1;
        } else {
            return 0.15;
        }
    }

    @Override
    protected void loadFieldsFromXmlNode(Node wn) {
        
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
            if(null != unit) {
                int damageJJ = getOtherDamagedJumpJets() + 1;
                if(damageJJ < (int)Math.ceil(unit.getEntity().getOriginalJumpMP() / 2.0)) {
                    unit.repairSystem(CriticalSlot.TYPE_SYSTEM, Protomech.SYSTEM_TORSOCRIT, Protomech.LOC_TORSO);
                    unit.damageSystem(CriticalSlot.TYPE_SYSTEM, Protomech.SYSTEM_TORSOCRIT, Protomech.LOC_TORSO, 1);
                } else {
                    unit.repairSystem(CriticalSlot.TYPE_SYSTEM, Protomech.SYSTEM_TORSOCRIT, Protomech.LOC_TORSO);
                    unit.damageSystem(CriticalSlot.TYPE_SYSTEM, Protomech.SYSTEM_TORSOCRIT, Protomech.LOC_TORSO, 2);
                }
            }
        }
    }

    @Override
    public String checkFixable() {
        Unit unit = get(Installable.class).getUnit();
    	if(null == unit) {
    		return null;
    	}
        if(unit.isLocationBreached(Protomech.LOC_TORSO)) {
            return unit.getEntity().getLocationName(Protomech.LOC_TORSO) + " is breached.";
        }
        if(unit.isLocationDestroyed(Protomech.LOC_TORSO)) {
            return unit.getEntity().getLocationName(Protomech.LOC_TORSO) + " is destroyed.";
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
            remove(false);
            //assign the replacement part to the unit           
            actualReplacement.updateConditionFromPart();
        }
    }
    
    @Override
    public boolean isAcceptableReplacement(Part part, boolean refit) {
        return part instanceof ProtomekJumpJet
                && get(Installable.class).getUnitTonnage() == part.get(Installable.class).getUnitTonnage();
    }

    @Override
    public Part getNewPart() {
        return new ProtomekJumpJet(get(Installable.class).getUnitTonnage(), campaign);
    }
    
    private int getOtherDamagedJumpJets() {
        int damagedJJ = 0;
        Unit unit = get(Installable.class).getUnit();
        if(null != unit) {
            for(Part p : unit.getParts()) {
                if(p.getId() == this.getId()) {
                    continue;
                }
                if(p instanceof MissingProtomekJumpJet 
                        || (p instanceof ProtomekJumpJet && ((ProtomekJumpJet)p).needsFixing())) {
                    damagedJJ++;
                }
            }
        }
        return damagedJJ;
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
