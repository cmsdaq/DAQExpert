package rcms.utilities.daqexpert.reasoning.base;

import rcms.utilities.daqexpert.notifications.Sound;
import rcms.utilities.daqexpert.reasoning.base.enums.EventGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.EventPriority;

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
public abstract class LogicModule {
	/**
	 * Name of the event found
	 */
	protected String name;

	/**
	 * Group of the event found
	 */
	protected EventGroup group;

	/**
	 * Priority of the event found
	 */
	protected EventPriority priority;

	/**
	 * Condition description
	 */
	protected String description;

	private boolean notificationPlay;
	private boolean notificationEndPlay = false;

	private boolean notificationDisplay = false;

	private String prefixToPlay;
	private String suffixToPlay;
	private Sound soundToPlay;
	private boolean skipText;

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
	 * Get the group of the condition
	 * 
	 * @return group of the condition
	 */
	public EventGroup getGroup() {
		return group;
	}

	/**
	 * Set the group of the condition
	 * 
	 * @param group
	 *            group of the condition
	 */
	public void setGroup(EventGroup group) {
		this.group = group;
	}

	/**
	 * Get priority of the condition
	 * 
	 * @return priority of the condition
	 */
	public EventPriority getPriority() {
		return priority;
	}

	/**
	 * Set the priority of the condition
	 * 
	 * @param priority
	 *            priority of the condition
	 */
	public void setPriority(EventPriority priority) {
		this.priority = priority;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isNotificationPlay() {
		return notificationPlay;
	}

	public void setNotificationPlay(boolean notificationPlay) {
		this.notificationPlay = notificationPlay;
	}

	public boolean isNotificationDisplay() {
		return notificationDisplay;
	}

	public void setNotificationDisplay(boolean notificationDisplay) {
		this.notificationDisplay = notificationDisplay;
	}

	public String getPrefixToPlay() {
		return prefixToPlay;
	}

	public void setPrefixToPlay(String prefixToPlay) {
		this.prefixToPlay = prefixToPlay;
	}

	public String getSuffixToPlay() {
		return suffixToPlay;
	}

	public void setSuffixToPlay(String suffixToPlay) {
		this.suffixToPlay = suffixToPlay;
	}

	public Sound getSoundToPlay() {
		return soundToPlay;
	}

	public void setSoundToPlay(Sound soundToPlay) {
		this.soundToPlay = soundToPlay;
	}

	public boolean isSkipText() {
		return skipText;
	}

	public void setSkipText(boolean skipText) {
		this.skipText = skipText;
	}

	public boolean isNotificationEndPlay() {
		return notificationEndPlay;
	}

	public void setNotificationEndPlay(boolean notificationEndPlay) {
		this.notificationEndPlay = notificationEndPlay;
	}

}
