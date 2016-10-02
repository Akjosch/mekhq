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
package mekhq.campaign.mod.am;

import java.util.Objects;

import megamek.common.event.Subscribe;
import mekhq.campaign.Campaign;
import mekhq.campaign.event.PersonBattleFinishedEvent;
import mekhq.campaign.event.NewDayEvent;
import mekhq.campaign.event.OptionsChangedEvent;
import mekhq.campaign.event.medical.MedicalCheckEvent;
import mekhq.campaign.event.medical.PersonHealingEvent;
import mekhq.campaign.personnel.Injury;
import mekhq.campaign.personnel.Person;
import mekhq.campaign.unit.Unit;

public final class AMEventHandler {
    private Campaign campaign;
    private boolean enabled;
    
    public AMEventHandler(Campaign campaign) {
        this.campaign = Objects.requireNonNull(campaign);
        this.enabled = campaign.getCampaignOptions().useAdvancedMedical();
    }
    
    @Subscribe
    public void campaignOptionsChangedHandler(OptionsChangedEvent e) {
        enabled = e.getOptions().useAdvancedMedical();
    }
    
    @Subscribe
    public void battleHandler(PersonBattleFinishedEvent e) {
        if(!enabled) {
            return;
        }
        InjuryUtil.resolveAfterCombat(campaign, e.getPerson(), e.getStatus().getHits());
        InjuryUtil.resolveCombatDamage(campaign, e.getPerson(), e.getStatus().getHits());
        e.getStatus().setHits(0);
        if(e.getPerson().getStatus() == Person.S_KIA) {
            e.getStatus().setDead(true);
        }
    }
    
    @Subscribe
    public void newDayHandler(NewDayEvent e) {
        if(!enabled) {
            return;
        }
        for(Person p : campaign.getPersonnel()) {
            for(Injury i : p.getInjuries()) {
                campaign.addReport(p.getHyperlinkedFullTitle() + " spent time resting to heal "
                          + p.getGenderPronoun(Person.PRONOUN_HISHER)
                          + " " + i.getName() + "!");
            }
            InjuryUtil.resolveDailyHealing(campaign, p);
            Unit u = campaign.getUnit(p.getUnitId());
            if(null != u) {
                u.resetPilotAndEntity();
            }
        }
    }
    
    @Subscribe
    public void healingHandler(PersonHealingEvent e) {
        if(enabled) {
            // We replace the default healing in the NewDayEvent handler
            e.cancel();
        }
    }
    
    @Subscribe
    public void medicalCheckHandler(MedicalCheckEvent e) {
        if(!enabled) {
            return;
        }
        for(Injury i : e.getPerson().getInjuries()) {
            if(i.getTime() > 0) {
                e.setNeedsHealing(true);
                break;
            }
        }
    }
}
