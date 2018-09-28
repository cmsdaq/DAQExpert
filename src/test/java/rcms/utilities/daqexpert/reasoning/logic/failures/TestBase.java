package rcms.utilities.daqexpert.reasoning.logic.failures;

import org.junit.Assert;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.persistence.StructureSerializer;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.events.ConditionEvent;
import rcms.utilities.daqexpert.events.collectors.EventRegister;
import rcms.utilities.daqexpert.persistence.Condition;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.processing.context.ContextHandler;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.causality.DominatingSelector;
import rcms.utilities.daqexpert.reasoning.processing.ConditionProducer;
import rcms.utilities.daqexpert.reasoning.processing.LogicModuleManager;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class TestBase {

    public Condition dominating;

    public Map<String, Output> result;

    public DAQ getSnapshot(String fileName) throws URISyntaxException {

        StructureSerializer serializer = new StructureSerializer();

        URL url = KnownFailure.class.getResource(fileName);

        File file = new File(url.toURI());

        return serializer.deserialize(file.getAbsolutePath());
    }

    public Map<String, Output> runLogic(String filaname, Properties properties) {

        try {
            return runLogic(getSnapshot(filaname), properties);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Map<String, Output> runLogic(String filaname) throws URISyntaxException {
        return runLogic(getSnapshot(filaname), getDefaultProperties());
    }

    public Map<String, Output> runLogic(DAQ daq, Properties properties) {

        LogicModuleManagerMock.properties = properties;
        ConditionProducer cp = new ConditionProducer();
        cp.setEventRegister(new EventRegisterMock());
        LogicModuleManager logicModuleManager = new LogicModuleManagerMock(cp);

        List<Condition> conditions = new ArrayList<>();
        Long initialTimestamp = daq.getLastUpdate();
        if(true) {
            daq.setLastUpdate(initialTimestamp- 120000); // some LM have holdoffs
            conditions.addAll(logicModuleManager.runLogicModules(daq, false));
            daq.setLastUpdate(initialTimestamp- 110000); // some LM have holdoffs
            conditions.addAll(logicModuleManager.runLogicModules(daq, false));
        }


        daq.setLastUpdate(initialTimestamp);
        ContextHandler.highlightMarkup = false;
        conditions.addAll(logicModuleManager.runLogicModules(daq, false));

        System.out.println("All satisfied Logic Modules: ");
        HashMap<String, Output> results = logicModuleManager.getLastRoundOutput();
        results.entrySet().stream().filter(c->c.getValue().getResult())
                .map(c-> "  " + c.getKey()  + (
                        c.getValue().getContext()!= null ? ": " +
                                c.getValue().getContext().getContextEntryMap().entrySet()
                                        .stream().map(e->e.getKey()+"="+ e.getValue().getTextRepresentation())
                                        .collect(Collectors.toList())
                                : " "))
                .forEach(System.out::println);
        System.out.println();
        DominatingSelector ds = new DominatingSelector();


        Set<Condition> allActiveConditions = conditions.stream().filter(c->c.getEnd() == null && c.isShow() && c.isProblematic() && !c.isHoldNotifications()).collect(Collectors.toSet());
        dominating = ds.selectDominating(allActiveConditions);
        System.out.println("Dominating: " + dominating.getLogicModule());

        this.result = results;
        return results;
    }

    public Properties getEmptyProperties(){
        Properties properties = new Properties();

        for(Setting setting: Setting.values()){
            properties.put(setting.getKey(), "0");

        }

        return properties;
    }

    public Properties getDefaultProperties(){
        Properties properties = this.getEmptyProperties();
        properties.setProperty(Setting.EXPERT_LOGIC_DEADTIME_THESHOLD_FED.getKey(),"2");
        properties.setProperty(Setting.EXPERT_LOGIC_DEADTIME_BACKPRESSURE_FED.getKey(),"2");
        properties.setProperty(Setting.EXPERT_LOGIC_DEADTIME_THESHOLD_TTS.getKey(), "1");
        properties.setProperty(Setting.EXPERT_LOGIC_DEADTIME_THESHOLD_TOTAL.getKey(), "5");

        properties.setProperty(Setting.EXPERT_TCDS_INPUT_RATE_VERYHIGH.getKey(), "100000");
        properties.setProperty(Setting.EXPERT_TCDS_INPUT_RATE_HIGH.getKey(), "200000");
        properties.setProperty(Setting.EXPERT_L1_RATE_MAX.getKey(), "100000");

        properties.setProperty(Setting.EXPERT_LOGIC_EVM_FEW_EVENTS.getKey(),"100");

        properties.setProperty(Setting.EXPERT_HLT_OUTPUT_BANDWITH_TOO_HIGH.getKey(), "4.5");
        properties.setProperty(Setting.EXPERT_HLT_OUTPUT_BANDWITH_EXTREME.getKey(), "6.0");
        properties.setProperty(Setting.EXPERT_LOGIC_HLT_CPU_LOAD_THRESHOLD.getKey(), "0.9");

        return  properties;
    }

    public Output getOutputOf(LogicModuleRegistry logicModule) {
        Output output = result.get(logicModule.getLogicModule().getClass().getSimpleName());
        return output;
    }

    public void assertSatisfied(LogicModuleRegistry logicModule){
        Assert.assertTrue(result.get(logicModule.getLogicModule().getClass().getSimpleName()).getResult());
    }
}




class LogicModuleManagerMock extends LogicModuleManager {

    public static Properties properties;

    public LogicModuleManagerMock(ConditionProducer conditionProducer) {
        super(conditionProducer);
    }

    @Override
    protected Properties getProperties() {
        return properties;
    }
}

class EventRegisterMock implements EventRegister {

    @Override
    public void registerBegin(Condition condition) {

    }

    @Override
    public void registerEnd(Condition condition) {

    }

    @Override
    public void registerUpdate(Condition condition) {

    }

    @Override
    public List<ConditionEvent> getEvents() {
        return null;
    }
}