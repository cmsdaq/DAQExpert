package rcms.utilities.daqexpert.processing.context;

import org.apache.log4j.Logger;

import java.util.HashSet;
import java.util.Observable;
import java.util.Set;

public class ContextNotifier extends Observable{

    Logger logger = Logger.getLogger(ContextNotifier.class);

    public ContextNotifier(){
        this.changeset = new HashSet<>();
    }


    /** List of keys that changed in contextHandler in current round */
    private Set<String> changeset;

    public void clearChangeset(){
        this.changeset.clear();
    }

    public void registerChange(String key){
        if(!changeset.contains(key)) {
            this.changeset.add(key);
            setChanged();
        }
    }

    public void triggerReady() {
        if (hasChanged()) {
            logger.debug("Notifying "+this.countObservers() + " observers about change in following keys: " + changeset);
            notifyObservers();
            clearChanged();
        }
    }

    public boolean isChanged(String key){
        if(changeset.contains(key)){
            return true;
        }
        return false;
    }


}
