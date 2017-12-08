package rcms.utilities.daqexpert.reasoning.base;


import rcms.utilities.daqexpert.processing.context.Context;
import rcms.utilities.daqexpert.processing.context.ContextHandler;

/**
 * Represents output of LM. Consists of boolean value stating if LM is satisfied and all of the contextHandler information.
 */
public class Output {

    Boolean result;

    Context context;

    public Output(Boolean result) {
        this.result = result;
    }

    public Boolean getResult() {
        return result;
    }

    public void setResult(Boolean result) {
        this.result = result;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
