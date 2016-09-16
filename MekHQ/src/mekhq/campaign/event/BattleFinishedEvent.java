package mekhq.campaign.event;

import mekhq.campaign.ResolveScenarioTracker.PersonStatus;
import mekhq.campaign.personnel.Person;

/**
 * An event fired for every person who just came back from battle, dead or alive.
 */
public class BattleFinishedEvent extends PersonEvent {
    private PersonStatus status;
    
    public BattleFinishedEvent(Person person, PersonStatus status) {
        super(person);
        this.status = status;
    }
    
    public PersonStatus getStatus() {
        return status;
    }
    
    public void setStatus(PersonStatus status) {
        this.status = status;
    }
}
