package rcms.utilities.daqexpert.reasoning.logic.basic;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqaggregator.data.TCDSGlobalInfo;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.processing.context.ContextHandler;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.ResultSupplier;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class CriticalDeadtimeTest {

    DAQ snapshot;

    CriticalDeadtime lm;
    ResultSupplier resultSupplier;


    Logger logger = Logger.getLogger(CriticalDeadtimeTest.class);

    public CriticalDeadtimeTest() {


        lm = new CriticalDeadtime();

        ResultSupplier resultSupplier = new ResultSupplier();
        lm.setResultSupplier(resultSupplier);


        Properties p = new Properties();
        p.put(Setting.EXPERT_LOGIC_DEADTIME_THESHOLD_TOTAL.getKey(), "1");

        lm.parametrize(p);


        snapshot = new DAQ();

        snapshot.setTcdsGlobalInfo(new TCDSGlobalInfo());
        HashMap<String, Double> d = new HashMap<>();
        snapshot.getTcdsGlobalInfo().setDeadTimesInstant(d);

        d.put("beamactive_trg_rules", 13d);
        d.put("beamactive_daq_bp", 13d);
        d.put("beamactive_bx_mask", 31d);
        d.put("beamactive_retri", 33d);
        d.put("beamactive_tts", 0d);
        d.put("beamactive_sw_pause", 0d);
        d.put("beamactive_calib", 53d);
        d.put("beamactive_total", 13d);
        d.put("beamactive_apve", 93d);
        d.put("beamactive_fw_pause", 1d);

        d.put("trg_rules", 2d);
        d.put("daq_bp", 2d);
        d.put("bx_mask", 12d);
        d.put("retri", 0d);
        d.put("tts", 2d);
        d.put("sw_pause", 2d);
        d.put("calib", 20d);
        d.put("total", 80d);
        d.put("apve", 0d);
        d.put("fw_pause", 2d);
    }


    @Test
    public void testNoBeamactive() throws Exception {


        resultSupplier.update(LogicModuleRegistry.ExpectedRate, new Output(true));
        resultSupplier.update(LogicModuleRegistry.BeamActive, new Output(false));

        lm.satisfied(snapshot);
        ContextHandler.highlightMarkup = false;
        lm.getContextHandler().getContext().getContextEntryMap().entrySet().stream().map(c -> c.getKey() + "=" + c.getValue().getTextRepresentation()).forEach(logger::info);

        logger.info(lm.getDescriptionWithContext());
        Assert.assertEquals("Deadtime during running is 80%, the threshold is 1.0%. There are following contributions: [calib=20%, bx_mask=12%, fw_pause=2%, sw_pause=2%, trg_rules=2%, tts=2%, daq_bp=2%]",
                lm.getDescriptionWithContext());

    }

    @Test
    public void testBeamactive() throws Exception {

        resultSupplier.update(LogicModuleRegistry.ExpectedRate, new Output(true));
        resultSupplier.update(LogicModuleRegistry.BeamActive, new Output(true));

        lm.satisfied(snapshot);
        ContextHandler.highlightMarkup = false;
        lm.getContextHandler().getContext().getContextEntryMap().entrySet().stream().map(c -> c.getKey() + "=" + c.getValue().getTextRepresentation()).forEach(logger::info);

        logger.info(lm.getDescriptionWithContext());
        Assert.assertEquals("Deadtime during running is 13%, the threshold is 1.0%. There are following contributions: [beamactive_apve=93%, beamactive_calib=53%, beamactive_retri=33%, beamactive_bx_mask=31%, beamactive_trg_rules=13%, beamactive_daq_bp=13%, beamactive_fw_pause=1%]",
                lm.getDescriptionWithContext());

    }

    @Test
    public void multipleValues() {
        resultSupplier.update(LogicModuleRegistry.ExpectedRate, new Output(true));
        resultSupplier.update(LogicModuleRegistry.BeamActive, new Output(true));

        lm.satisfied(snapshot);

        snapshot.getTcdsGlobalInfo().getDeadTimesInstant().put("beamactive_apve", 0d);

        lm.satisfied(snapshot);
        ContextHandler.highlightMarkup = false;
        lm.getContextHandler().getContext().getContextEntryMap().entrySet().stream().map(c -> c.getKey() + "=" + c.getValue().getTextRepresentation()).forEach(logger::info);

        logger.info(lm.getDescriptionWithContext());
        Assert.assertEquals("Deadtime during running is 13%, the threshold is 1.0%. There are following contributions: [beamactive_calib=53%, beamactive_apve=( last: 0%,  avg: 46.5%,  min: 0%,  max: 93%), beamactive_retri=33%, beamactive_bx_mask=31%, beamactive_trg_rules=13%, beamactive_daq_bp=13%, beamactive_fw_pause=1%]",
                lm.getDescriptionWithContext());
    }


}