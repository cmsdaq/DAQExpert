package rcms.utilities.daqexpert.reasoning.logic.basic;

import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import rcms.utilities.daqaggregator.data.BUSummary;

import rcms.utilities.daqaggregator.data.DAQ;
import rcms.utilities.daqexpert.ExpertException;
import rcms.utilities.daqexpert.ExpertExceptionCode;
import rcms.utilities.daqexpert.Setting;
import rcms.utilities.daqexpert.reasoning.base.ContextLogicModule;
import rcms.utilities.daqexpert.reasoning.base.Output;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;
import rcms.utilities.daqexpert.reasoning.base.enums.LHCBeamMode;
import rcms.utilities.daqexpert.reasoning.base.enums.LHCMachineMode;
import rcms.utilities.daqexpert.reasoning.logic.basic.helper.HoldOffTimer;

/**
 * Logic module to check whether there is a non-negligible amount
 * of filter units still in cloud mode when they should not be
 * according to the LHC beam mode.
 */
public class CloudFuNumber extends ContextLogicModule implements Parameterizable {

	/** maximum fraction of filter units allowed in cloud mode close
	 *  to stable beam state. Note that this is a fraction between 0 and 1,
	 *  not a percentage.
	 */
	private float maxCloudModeFraction;

	/** list of LHC beam modes in which the cloud is allowed to run
	 */
	private Set<LHCBeamMode> allowedBeamModes = new HashSet<>();

	/** list of LHC beam modes in which no cloud running is tolerated,
	 *  not even if the holdoff timer is still active
	 */
	private Set<LHCBeamMode> criticalBeamModes = new HashSet<>();

	private HoldOffTimer holdOffTimer;

	public CloudFuNumber() {
		this.name = "Too many FUs in cloud mode";
		this.priority = ConditionPriority.DEFAULTT;

		// default value if no value found in the configuration file
		this.maxCloudModeFraction = 0.03f;

		// LHC beam modes when the cloud may run 
		allowedBeamModes.add(LHCBeamMode.NO_BEAM);
		allowedBeamModes.add(LHCBeamMode.RAMP_DOWN);
		allowedBeamModes.add(LHCBeamMode.SETUP);
		allowedBeamModes.add(LHCBeamMode.INJECTION_PROBE_BEAM);

		allowedBeamModes.add(LHCBeamMode.ABORT);
		allowedBeamModes.add(LHCBeamMode.INJECTION_SETUP_BEAM);
		allowedBeamModes.add(LHCBeamMode.INJECTION_PHYSICS_BEAM);
		allowedBeamModes.add(LHCBeamMode.PREPARE_RAMP);
		allowedBeamModes.add(LHCBeamMode.UNSTABLE_BEAMS);
		allowedBeamModes.add(LHCBeamMode.CYCLING);
		allowedBeamModes.add(LHCBeamMode.RECOVER); // 'RECOVERY' in Diego's code
		allowedBeamModes.add(LHCBeamMode.INJECT_AND_DUMP);
		allowedBeamModes.add(LHCBeamMode.CIRCULATE_AND_DUMP);

		// no cloud running allowed in the following beam modes,
		// even not while the holdoff period is still ongoing
		criticalBeamModes.add(LHCBeamMode.STABLE_BEAMS);
		criticalBeamModes.add(LHCBeamMode.ADJUST);
		criticalBeamModes.add(LHCBeamMode.SQUEEZE);

	}

	/** @return true if this is MD/TS (in which the cloud
	 *  can typically run almost freely)
	 */
	boolean isLhcMachineTestMode(LHCMachineMode machineMode) {
		return LHCMachineMode.MACHINE_DEVELOPMENT.equals(machineMode) ||
						LHCMachineMode.SHUTDOWN.equals(machineMode); // Technical Stop
	}


	/** @return true if the cloud VMs can be on given the LHC
	 *  machine and beam modes.
	 *
	 *  Note that we start a hold off timer when we go
	 *  from a state where this function returns true
	 *  to a state where this function returns false.
	 */
	boolean cloudCanBeOn(LHCBeamMode beamMode, LHCMachineMode machineMode) {

		// ignore LHC beam modes during machine development
		//
		// note that beam modes like 'STABLE BEAMS' will not
		// appear e.g. during end of year shutdowns etc.
		if (isLhcMachineTestMode(machineMode)) {
			return true;
		}

		// parse the LHC beam mode
		return allowedBeamModes.contains(beamMode);

	}

	/** @return the fraction of FUs in cloud mode. Returns zero if no FUs
	    were found. */
	private double calculateCloudFraction(DAQ daq) {

		BUSummary buSummary = daq.getBuSummary();

		int numFusCloud = buSummary.getNumFUsCloud();

		// get total number of FUs \(the individual counts are non-overlapping
		// according to Remi)
		//
		// TODO: this could be made a method in class BUSummary
		int totalNumFus = numFusCloud +
						buSummary.getNumFUsCrashed() +
						buSummary.getNumFUsHLT() +
						buSummary.getNumFUsStale();


		if (numFusCloud > 0) {
			return numFusCloud / (double) totalNumFus;
		} else {
			// no FUs at all, there might be another problem (but not of interest
		  // here...)
			return 0;
		}
	}

	@Override
	public boolean satisfied(DAQ daq, Map<String, Output> results) {

		LHCBeamMode lhcBeamMode = LHCBeamMode.getModeByCode(daq.getLhcBeamMode());
		LHCMachineMode lhcMachineMode = LHCMachineMode.getModeByCode(daq.getLhcMachineMode());

		// check the LHC beam mode
		boolean shouldBeOff = ! this.cloudCanBeOn(lhcBeamMode, lhcMachineMode);

		long now = daq.getLastUpdate();

		// update the holdoff timer
		this.holdOffTimer.updateInput(shouldBeOff, now);

		boolean inCriticalBeamMode = this.criticalBeamModes.contains(LHCBeamMode.getModeByCode(daq.getLhcBeamMode()));
		// check if -- after taking into account the holdoff timer --
		// we should not see any cloud FUs anymore
		if (!inCriticalBeamMode && !this.holdOffTimer.getOutput(now)) {
			// the grace period to shut down the VMs has not yet finished,
			// do not perform further checks yet
			return false;
		}

		// now we should definitively not see any cloud FUs anymore
		// (we are in a cloud off machine mode and have been for the hold off
		// period)
		double cloudFraction = this.calculateCloudFraction(daq);
		
		if (cloudFraction > this.maxCloudModeFraction){
			contextHandler.registerForStatistics("FRACTIONFUSCLOUDMODE", cloudFraction * 100,"%",1);
			return true;
		}
		else
			return false;
	}

	@Override
	public void parametrize(Properties properties) {
		try {
			this.maxCloudModeFraction = Float
							.parseFloat(properties.getProperty(Setting.EXPERT_LOGIC_CLOUDFUNUMBER_THRESHOLD_TOTAL_FRACTION.getKey()));

			this.description = "Fraction of FUs in cloud mode is {{FRACTIONFUSCLOUDMODE}}, the threshold is " +
							(this.maxCloudModeFraction * 100) + "%";

			// holdoff time in milliseconds
			int holdOffTime = Integer
							.parseInt(properties.getProperty(Setting.EXPERT_LOGIC_CLOUDFUNUMBER_HOLDOFF_PERIOD.getKey()));

			// create a new timer
			this.holdOffTimer = new HoldOffTimer(holdOffTime);

		} catch (NumberFormatException e) {
			throw new ExpertException(ExpertExceptionCode.LogicModuleUpdateException, "Could not update LM "
					+ this.getClass().getSimpleName() + ", number parsing problem: " + e.getMessage());
		} catch (NullPointerException e) {
			throw new ExpertException(ExpertExceptionCode.LogicModuleUpdateException,
					"Could not update LM " + this.getClass().getSimpleName() + ", other problem: " + e.getMessage());
		}

	}

}
