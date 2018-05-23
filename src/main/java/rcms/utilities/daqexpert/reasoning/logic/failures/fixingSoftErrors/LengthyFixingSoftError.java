package rcms.utilities.daqexpert.reasoning.logic.failures.fixingSoftErrors;

import java.util.Map;
import java.util.HashMap;
import java.util.Properties;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqexpert.FailFastParameterReader;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;
import rcms.utilities.daqexpert.reasoning.logic.basic.Parameterizable;
import rcms.utilities.daqexpert.reasoning.logic.failures.KnownFailure;

public class LengthyFixingSoftError extends KnownFailure implements Parameterizable {

    private static final String levelZeroProblematicState = "FixingSoftError";
    private int thresholdPeriod;
    private long timestampOfBegin;
    private HashMap<String,SubSystem> problematicSubSystems;

    public LengthyFixingSoftError() {
        this.name = "Lengthy fixing-soft-error";
        this.action = new SimpleAction("Call the DOC of subsystem {{SUBSYSTEM}}", "Ask the DCS shifter to check the status of subsystem {{SUBSYSTEM}}");
        this.problematicSubSystems = new HashMap<>();
    }

    @Override
    public void parametrize(Properties properties) {

        this.thresholdPeriod = FailFastParameterReader.getIntegerParameter(properties, Setting.EXPERT_LOGIC_LENGHTYFIXINGSOFTERROR_THESHOLD_PERIOD, LengthyFixingSoftError.class);

        this.description = "Level zero in FixingSoftError longer than " + (thresholdPeriod / 1000)
                + " sec. This is caused by subsystem(s) {{SUBSYSTEM}}";

    }

    @Override
    public boolean satisfied(DAQ daq, Map<String, Output> results) {

        boolean result = false;

        String currentState = daq.getLevelZeroState();

        if (levelZeroProblematicState.equalsIgnoreCase(currentState)) {


            for (SubSystem subsystem : daq.getSubSystems()) {
                if (levelZeroProblematicState.equalsIgnoreCase(subsystem.getStatus())) {
                    problematicSubSystems.put(subsystem.getName(), subsystem);
                }
            }

            if (timestampOfBegin == 0) {
                timestampOfBegin = daq.getLastUpdate();
            } else {

                if (timestampOfBegin + thresholdPeriod < daq.getLastUpdate()) {
                    result = true;
                    problematicSubSystems.values().stream().forEach(s->contextHandler.register("SUBSYSTEM", s.getName()));
                }

            }
        } else {
            this.timestampOfBegin = 0L;
            this.problematicSubSystems = new HashMap<>();
        }

        return result;
    }

}
