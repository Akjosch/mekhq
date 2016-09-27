/*
 * Copyright (C) 2016 MegaMek team
 *
 * This file is part of MekHQ.
 *
 * MekHQ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
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
package mekhq.campaign.event.medical;

import mekhq.campaign.event.PersonEvent;
import mekhq.campaign.personnel.Person;

/**
 * An event gathering modifying if a person needs medical attention. Event handlers can
 * intercept it and modify the <tt>needsHealing</tt> variable.
 */
public class MedicalCheckEvent extends PersonEvent {
    protected boolean needsHealing;

    public MedicalCheckEvent(Person person, boolean needsHealing) {
        super(person);
        this.needsHealing = needsHealing;
    }

    public boolean needsHealing() {
        return needsHealing;
    }
    
    public void setNeedsHealing(boolean needsHealing) {
        this.needsHealing = needsHealing;
    }
}
