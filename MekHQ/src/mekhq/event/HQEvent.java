package mekhq.event;

/**
 * Base class for all events
 */
public abstract class HQEvent {
    protected boolean cancelled = false;
    
    public HQEvent() {
    }
    
    /** @return true if the event can be cancelled (aborted) */
    public boolean isCancellable() {
        return false;
    }
    
    /** @return true if the event is cancelled */
    public boolean isCancelled() {
        return cancelled;
    }

    public void cancel() {
        if(isCancellable()) {
            cancelled = true;
        }
    }
}
