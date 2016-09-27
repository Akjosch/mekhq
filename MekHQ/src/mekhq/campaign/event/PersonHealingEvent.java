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

import java.util.Objects;

import mekhq.campaign.personnel.Person;

/**
 * An event triggered just before a treatment is administered by a trained professional.
 * <p>
 * Can be cancelled, in which case the normal healing routine isn't run either. If not
 * cancelled, event handlers can modify the assigned doctor.
 */
public class PersonHealingEvent extends PersonEvent {
    private Person doctor;
    // HTML-formatted report on the healing effects
    private String report = "";

    public PersonHealingEvent(Person person, Person doctor) {
        super(person);
        this.doctor = Objects.requireNonNull(doctor);
    }

    /**
     * @return the assigned doctor, which may be different from the default one, or <tt>null</tt> if
     *          an event handler removed it.
     */
    public Person getDoctor() {
        return doctor;
    }
    
    public void setDoctor(Person doctor) {
        this.doctor = doctor;
    }
    
    public String getReport() {
        return report;
    }
    
    public void setReport(String report) {
        this.report = Objects.requireNonNull(report);
    }
    
    public void appendReport(String reportPart) {
        this.report = report + reportPart;
    }
    
    @Override
    public boolean isCancellable() {
        return true;
    }
}
