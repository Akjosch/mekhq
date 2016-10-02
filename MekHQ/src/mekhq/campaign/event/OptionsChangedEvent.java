package mekhq.campaign.event;

import java.util.Objects;

import mekhq.campaign.Campaign;
import mekhq.campaign.CampaignOptions;

/**
 * An event thrown after the campaign options were changed. The event handlers aren't supposed
 * to modify those options.
 */
public class OptionsChangedEvent extends CampaignEvent {
    private CampaignOptions options;

    public OptionsChangedEvent(Campaign campaign) {
        this(campaign, campaign.getCampaignOptions());
    }
    
    public OptionsChangedEvent(Campaign campaign, CampaignOptions options) {
        super(campaign);
        this.options = Objects.requireNonNull(options);
    }
    
    public CampaignOptions getOptions() {
        return options;
    }
}
