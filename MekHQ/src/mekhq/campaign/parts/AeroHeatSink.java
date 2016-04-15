/*
 * AeroHeatSink.java
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
import org.w3c.dom.NodeList;

import megamek.common.Aero;
import megamek.common.Compute;
import megamek.common.EquipmentType;
import megamek.common.TechConstants;
import mekhq.MekHqXmlUtil;
import mekhq.campaign.Campaign;
import mekhq.campaign.parts.component.Installable;
import mekhq.campaign.personnel.SkillType;

/**
 *
 * @author Jay Lawson <jaylawson39 at yahoo.com>
 */
public class AeroHeatSink extends Part {
	private static final long serialVersionUID = -717866644605314883L;

	private int type;
	
	public AeroHeatSink() {
    	this(Aero.HEAT_SINGLE, null);
    }
    
    public AeroHeatSink(int type, Campaign c) {
        super(c);
        this.name = "Aero Heat Sink"; //$NON-NLS-1$
        this.type = type;
        if(type == Aero.HEAT_DOUBLE) {
            this.name = "Aero Double Heat Sink"; //$NON-NLS-1$
        }
        add(new Installable());
    }
    
    @Override
    public AeroHeatSink clone() {
    	AeroHeatSink clone = new AeroHeatSink(type, campaign);
        clone.copyBaseData(this);
    	return clone;
    }
        
	@Override
	public void updateConditionFromEntity(boolean checkForDestruction) {
		int priorHits = hits;
		Aero aero = get(Installable.class).getEntity(Aero.class);
		if(null != aero && hits == 0) {
			//ok this is really ugly, but we don't track individual heat sinks, so I have no idea of
			//a better way to do it
			int hsDamage = aero.getHeatSinkHits();
			for(Part part : get(Installable.class).getUnit().getParts()) {
				if(hsDamage == 0) {
					break;
				}
				else if((part instanceof AeroHeatSink && part.getHits() > 0) || part instanceof MissingAeroHeatSink) {
					hsDamage--;
				}
			}
			if(hsDamage > 0) {
				hits = 1;
			} else {
				hits = 0;
			}
			if(checkForDestruction 
					&& hits > priorHits 
					&& Compute.d6(2) < campaign.getCampaignOptions().getDestroyPartTarget()) {
				remove(false);
				return;
			}
		}
	}
	
	@Override 
	public int getBaseTime() {
		return 90;
	}
	
	@Override
	public int getDifficulty() {
		if(isSalvaging()) {
			return -2;
		}
		return -1;
	}

	@Override
	public void updateConditionFromPart() {
	    Aero a = get(Installable.class).getEntity(Aero.class);
		if((null != a) && (hits == 0)) {
			a.setHeatSinks(Math.min(a.getHeatSinks() + 1, a.getOHeatSinks()));
		}
	}

	@Override
	public void fix() {
		super.fix();
        Aero aero = get(Installable.class).getEntity(Aero.class);
		if(null != aero) {
		    aero.setHeatSinks(aero.getHeatSinks() + 1);
		}
	}

	@Override
	public void remove(boolean salvage) {
        Aero aero = get(Installable.class).getEntity(Aero.class);
		if(null != aero) {
			if(hits == 0) {
			    aero.setHeatSinks(aero.getHeatSinks() - 1);
			}
			Part spare = campaign.checkForExistingSparePart(this);
			if(!salvage) {
				campaign.removePart(this);
			} else if(null != spare) {
				spare.incrementQuantity();
				campaign.removePart(this);
			}
			get(Installable.class).getUnit().removePart(this);
			Part missing = getMissingPart();
			get(Installable.class).getUnit().addPart(missing);
			campaign.addPart(missing, 0);
		}
		get(Installable.class).setUnit(null);
		updateConditionFromEntity(false);
	}

	@Override
	public MissingPart getMissingPart() {
		return new MissingAeroHeatSink(type, campaign);
	}

	@Override
	public String checkFixable() {
		return null;
	}

	@Override
	public boolean needsFixing() {
		return hits > 0;
	}

	@Override
	public long getStickerPrice() {
		if(type == Aero.HEAT_DOUBLE) {
			return 6000;
		} else {
			return 2000;
		}
	}

	@Override
	public double getTonnage() {
		return 1;
	}

	@Override
	public int getTechRating() {
		if(type == Aero.HEAT_DOUBLE) {
			return EquipmentType.RATING_D;
		} else {
			return EquipmentType.RATING_E;
		}
	}

	@Override
	public int getAvailability(int era) {
		if(type == Aero.HEAT_DOUBLE) {
		if(era == EquipmentType.ERA_SL) {
			return EquipmentType.RATING_C;
		} else if(era == EquipmentType.ERA_SW) {
			return EquipmentType.RATING_E;
		} else {
			return EquipmentType.RATING_D;
		}
		} else {
			return EquipmentType.RATING_B;
		}
	}
	
	@Override
	public int getTechLevel() {
		return TechConstants.T_ALLOWED_ALL;
	}

	@Override
	public boolean isSamePartType(Part part) {
		return part instanceof AeroHeatSink && type == ((AeroHeatSink)part).getType();
	}

	public int getType() {
		return type;
	}
	
	@Override
	public void writeToXml(PrintWriter pw1, int indent) {
		writeToXmlBegin(pw1, indent);
		pw1.println(MekHqXmlUtil.indentStr(indent+1)
				+"<type>"
				+type
				+"</type>");
		writeToXmlEnd(pw1, indent);
	}

	@Override
	protected void loadFieldsFromXmlNode(Node wn) {
		NodeList nl = wn.getChildNodes();
		
		for (int x=0; x<nl.getLength(); x++) {
			Node wn2 = nl.item(x);
			
			if (wn2.getNodeName().equalsIgnoreCase("type")) {
				type = Integer.parseInt(wn2.getTextContent());
				if(type == Aero.HEAT_DOUBLE) {
		            this.name = "Aero Double Heat Sink"; //$NON-NLS-1$
		        } 
			} 
		}
	}
	
	@Override
	public boolean isRightTechType(String skillType) {
		return skillType.equals(SkillType.S_TECH_AERO);
	}

	@Override
	public int getIntroDate() {
		if(type == Aero.HEAT_DOUBLE) {
			return 2567;
		}
		return EquipmentType.DATE_NONE;
	}

	@Override
	public int getExtinctDate() {
		//TODO: we should distinguish clan and IS here for extinction purposes
		/*if(type == Aero.HEAT_DOUBLE) {
		 * if(!isClan()) {
				return 2865;
			}
		}*/
		return EquipmentType.DATE_NONE;
	}

	@Override
	public int getReIntroDate() {
		return 3040;
	}
}