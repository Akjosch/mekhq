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
package mekhq.campaign.event;

import java.util.ArrayList;
import java.util.Collection;

import mekhq.campaign.personnel.Injury;
import mekhq.campaign.personnel.Person;

/**
 * An event announcing an injury. If cancelled, the injury will not be applied.
 * <p>
 * It's valid to modify the amount of hits an injury will apply as well as the list
 * of specific injuries in any way you like. The person itself should however
 * never be modified. The event can be cancelled further down the event
 * handler chain, and it might be just a "simulated" injury run, with the results
 * not meant to be actually applied.
 */
public class InjuryEvent extends PersonEvent {
    private int hits;
    private Collection<Injury> injuries;

    public InjuryEvent(Person person, int hits, Collection<Injury> injuries) {
        super(person);
        this.hits = Math.max(hits, 0);
        this.injuries = (null != injuries) ? new ArrayList<>(injuries) : new ArrayList<>();
    }

    public int getHits() {
        return hits;
    }
    
    public void setHits(int hits) {
        this.hits = Math.max(hits, 0);
    }
    
    public Collection<Injury> getInjuries() {
        return injuries;
    }
    
    @Override
    public boolean isCancellable() {
        return true;
    }
}
