/*
 * MissingVeeStabiliser.java
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

import megamek.common.EquipmentType;
import megamek.common.Tank;
import mekhq.MekHqXmlUtil;
import mekhq.campaign.Campaign;
import mekhq.campaign.parts.component.Installable;
import mekhq.campaign.unit.Unit;

/**
 *
 * @author Jay Lawson <jaylawson39 at yahoo.com>
 */
public class MissingVeeStabiliser extends MissingPart {
     private static final long serialVersionUID = 2806921577150714477L;
     
     public MissingVeeStabiliser() {
         this(0, null);
    }
    
    public MissingVeeStabiliser(int loc, Campaign c) {
         super(c);
         this.name = "Vehicle Stabiliser";
        get(Installable.class).setLocations(loc);
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
     public String checkFixable() {
          return null;
     }

     @Override
     public Part getNewPart() {
          return new VeeStabiliser(get(Installable.class).getMainLocation(), campaign);
     }

     @Override
     public boolean isAcceptableReplacement(Part part, boolean refit) {
          return part instanceof VeeStabiliser;
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
     public void writeToXml(PrintWriter pw1, int indent) {
          writeToXmlBegin(pw1, indent);
          pw1.println(MekHqXmlUtil.indentStr(indent+1)
                    +"<loc>"
                    +loc
                    +"</loc>");
          writeToXmlEnd(pw1, indent);
     }

     @Override
     protected void loadFieldsFromXmlNode(Node wn) {
          NodeList nl = wn.getChildNodes();
          
          for (int x=0; x<nl.getLength(); x++) {
               Node wn2 = nl.item(x);
               
               if (wn2.getNodeName().equalsIgnoreCase("loc")) {
                   get(Installable.class).setLocations(Integer.parseInt(wn2.getTextContent()));
               }
          }
     }
     
     @Override 
     public void fix() {
          VeeStabiliser replacement = (VeeStabiliser)findReplacement(false);
          Unit unit = get(Installable.class).getUnit();
          if(null != replacement && null != unit) {
               VeeStabiliser actualReplacement = replacement.clone();
               unit.addPart(actualReplacement);
               campaign.addPart(actualReplacement, 0);
               replacement.decrementQuantity();
               actualReplacement.setLocation(get(Installable.class).getMainLocation());
               remove(false);
               //assign the replacement part to the unit               
               actualReplacement.updateConditionFromPart();
          }
     }
     
     @Override
     public void updateConditionFromPart() {
         Tank tank = get(Installable.class).getEntity(Tank.class);
          if(null != tank) {
              tank.setStabiliserHit(get(Installable.class).getMainLocation());
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