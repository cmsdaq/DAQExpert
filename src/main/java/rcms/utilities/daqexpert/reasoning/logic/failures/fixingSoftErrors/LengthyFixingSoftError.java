package rcms.utilities.daqexpert.reasoning.logic.failures.fixingSoftErrors;

import org.apache.log4j.Logger;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.SubSystem;
import rcms.utilities.daqexpert.FailFastParameterReader;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.action.SimpleAction;
import rcms.utilities.daqexpert.reasoning.logic.basic.Parameterizable;
import rcms.utilities.daqexpert.reasoning.logic.failures.KnownFailure;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

public class LengthyFixingSoftError extends KnownFailure implements Parameterizable {

    private static final String levelZeroProblematicState = "FixingSoftError";

    private static final Logger logger = Logger.getLogger(LengthyFixingSoftError.class);

    /**
     * Default threshold
     */
    private int thresholdPeriod;

    private Map<String, Integer> subsystemSpecificThresholds;

    private Map<String, Long> subsystemSpecificTimestamps;


    public LengthyFixingSoftError() {
        this.name = "Lengthy fixing-soft-error";
        this.action = new SimpleAction("Call the DOC of subsystem {{PROBLEM-SUBSYSTEM}}", "Ask the DCS shifter to check the status of subsystem {{PROBLEM-SUBSYSTEM}}");
        this.subsystemSpecificThresholds = new HashMap<>();
        this.subsystemSpecificTimestamps = new HashMap<>();
    }

    @Override
    public void parametrize(Properties properties) {

        this.thresholdPeriod = FailFastParameterReader.getIntegerParameter(properties, Setting.EXPERT_LOGIC_LENGHTYFIXINGSOFTERROR_THESHOLD_PERIOD, LengthyFixingSoftError.class);

        Set<String> specificThresholdKeys = properties.stringPropertyNames().stream().filter(
                p -> p.startsWith(Setting.EXPERT_LOGIC_LENGHTYFIXINGSOFTERROR_THESHOLD_PERIOD.getKey())
        ).filter(p -> !p.equals(Setting.EXPERT_LOGIC_LENGHTYFIXINGSOFTERROR_THESHOLD_PERIOD.getKey()))
                .collect(Collectors.toSet());

        logger.debug("Found specific thresholds for subsystems: " + specificThresholdKeys);

        for (String key : specificThresholdKeys) {
            String subsystemName = key.substring(Setting.EXPERT_LOGIC_LENGHTYFIXINGSOFTERROR_THESHOLD_PERIOD.getKey().length() + 1).toLowerCase();
            Integer subsystemThrshold = FailFastParameterReader.getIntegerParameter(properties, key, LengthyFixingSoftError.class);
            logger.debug("Subystem '" + subsystemName + "' has a specific threshold of " + subsystemThrshold + "ms");
            subsystemSpecificThresholds.put(subsystemName, subsystemThrshold);
        }

        this.description = "Level zero in FixingSoftError longer than expected. "
                + "This is caused by subsystem(s) {{PROBLEM-SUBSYSTEM}}. The default threshold is " +
                (thresholdPeriod / 1000) + " s. " +
                (subsystemSpecificThresholds.size() > 0 ? "Note there are subsystem specific threshold(s): " +
                        subsystemSpecificThresholds.entrySet().stream().map(e-> e.getKey() + ": " + e.getValue()/1000 + " s").collect(Collectors.toList()) : "");

    }

    private int getSubsystemThresholdOrDefault(String subsystem) {
        int threshold =  subsystemSpecificThresholds.containsKey(subsystem) ? subsystemSpecificThresholds.get(subsystem) : thresholdPeriod;
        logger.debug("Using threshold " + threshold + " for subsystem " + subsystem);
        return threshold;
    }

    @Override
    public boolean satisfied(DAQ daq, Map<String, Output> results) {

        boolean result = false;
        String currentState = daq.getLevelZeroState();

        if (levelZeroProblematicState.equalsIgnoreCase(currentState)) {


            for (SubSystem subsystem : daq.getSubSystems()) {
                String subsystemName = subsystem.getName().toLowerCase();
                if (levelZeroProblematicState.equalsIgnoreCase(subsystem.getStatus())) {

                    if (!subsystemSpecificTimestamps.containsKey(subsystemName)) {
                        subsystemSpecificTimestamps.put(subsystemName, daq.getLastUpdate());
                        logger.debug("Time for subsystem " + subsystemName + " has been registered "+ daq.getLastUpdate());
                    } else {
                        if (subsystemSpecificTimestamps.get(subsystemName) +
                                getSubsystemThresholdOrDefault(subsystemName) <
                                daq.getLastUpdate()) {
                            result = true;
                            contextHandler.register("PROBLEM-SUBSYSTEM", subsystem.getName());
                        }
                    }
                } else{
                    subsystemSpecificTimestamps.remove(subsystemName);
                }
            }
        } else {
            if(this.subsystemSpecificTimestamps.size() > 0) {
                this.subsystemSpecificTimestamps = new HashMap<>();
            }
        }
        return result;
    }

}
