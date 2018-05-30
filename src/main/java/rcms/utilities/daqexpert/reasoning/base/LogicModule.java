package rcms.utilities.daqexpert.reasoning.base;

import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;
import rcms.utilities.daqexpert.processing.Requiring;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;
import rcms.utilities.daqexpert.reasoning.causality.CausalityNode;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Elementary part of expert knowledge. Logic Module (abbreviated LM) is a piece
 * of knowledge focusing on one aspect. E.g.:
 *
 * <ul>
 * <li>NoRate LM - this Logic Module identifies when there is no rate in DAQ
 * system</li>
 * <li>Downtime LM - this LM identifies when there is downtime in CMS Detector -
 * note that not all no-rate is downtime (e.g. when there is no stable beams)
 * </li>
 * <li>FED backpressured LM - identifies failure case when FED is backpressured
 * by DAQ system</li>
 * </ul>
 *
 * Note that each Logic Module should focus on one aspect, and one aspect only.
 * Results of a Logic Modules can be used in other Logic Modules so that there
 * is no duplication of code. It is recommended to reuse results from Logic
 * Modules for better performance.
 *
 * Please follow the
 * <a href="http://daq-expert.cern.ch/contributing.html">step-by-step guide</a>
 * before adding new Logic Modules.
 *
 *
 * @see <a href="http://daq-expert.cern.ch/contributing.html">step-by-step
 *      guide</a>
 *
 *
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 *
 */
public abstract class LogicModule implements Requiring, CausalityNode {

	/**
	 * Name of the condition found
	 */
	protected String name;

	/**
	 * Priority of the condition found
	 */
	protected ConditionPriority priority;

	protected LogicModuleRegistry logicModuleRegistry;

	/**
	 * Condition description
	 */
	protected String description;


	/**
	 * Brief description, introduced for Runtime logger
	 */
	protected String briefDescription;


	/**
	 * Hold generation of notifications
	 */
	protected boolean holdNotifications;

	/**
	 * Flag indicating whether this LM identifies problematic condition
	 */
	protected boolean problematic;

	/**
	 * Set of required logic modules
	 */
	protected Set<Requiring> required;

	protected Set<CausalityNode> affected;

	protected Set<CausalityNode> causing;


	public LogicModule(){
		this.required = new LinkedHashSet<>();
		this.causing = new LinkedHashSet<>();
		this.affected = new LinkedHashSet<>();
		this.problematic = true;
	}
	/**
	 * Get name of the condition
	 *
	 * @return name of the condition
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set name of the condition
	 *
	 * @param name
	 *            name of the condition
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Get priority of the condition
	 *
	 * @return priority of the condition
	 */
	public ConditionPriority getPriority() {
		return priority;
	}

	/**
	 * Set the priority of the condition
	 *
	 * @param priority
	 *            priority of the condition
	 */
	public void setPriority(ConditionPriority priority) {
		this.priority = priority;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public LogicModuleRegistry getLogicModuleRegistry() {
		return logicModuleRegistry;
	}

	public void setLogicModuleRegistry(LogicModuleRegistry logicModuleRegistry) {
		this.logicModuleRegistry = logicModuleRegistry;
	}

	public boolean isHoldNotifications() {
		return holdNotifications;
	}

	public void setHoldNotifications(boolean holdNotifications) {
		this.holdNotifications = holdNotifications;
	}


	public Set<Requiring> getRequired(){
		return required;
	}

	protected void require(LogicModuleRegistry logicModuleRegistry){

		required.add(logicModuleRegistry.getLogicModule());
	}

	protected void declareCause(LogicModuleRegistry logicModuleRegistry){
		declareCausing(logicModuleRegistry.getLogicModule());
	}

	protected void declareAffected(LogicModuleRegistry logicModuleRegistry){
		declareAffected(logicModuleRegistry.getLogicModule());
	}

	@Override
	public Set<CausalityNode> getCausing() {
		return causing;
	}

	@Override
	public Set<CausalityNode> getAffected() {
		return affected;
	}

	private int level;

	@Override
	public int getLevel() {
		return level;
	}

	@Override
	public void setLevel(int level) {
		this.level = level;
	}

	@Override
	public String getNodeName() {
		return getLogicModuleRegistry().name();
	}


	public void declareRelations() {return;}

	public boolean isProblematic() {
		return problematic;
	}

	public void setProblematic(boolean problematic) {
		this.problematic = problematic;
	}

	public String getBriefDescription() {
		return briefDescription;
	}

	public void setBriefDescription(String briefDescription) {
		this.briefDescription = briefDescription;
	}
}
