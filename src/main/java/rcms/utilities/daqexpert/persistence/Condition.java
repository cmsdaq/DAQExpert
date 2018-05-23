package rcms.utilities.daqexpert.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.log4j.Logger;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.Index;
import rcms.utilities.daqexpert.processing.context.Context;
import rcms.utilities.daqexpert.processing.context.ContextEntry;
import rcms.utilities.daqexpert.processing.context.ContextHandler;
import rcms.utilities.daqexpert.processing.context.ContextNotifier;
import rcms.utilities.daqexpert.reasoning.base.ContextLogicModule;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionGroup;
import rcms.utilities.daqexpert.reasoning.base.enums.ConditionPriority;
import rcms.utilities.daqexpert.reasoning.base.enums.EntryState;
import rcms.utilities.daqexpert.reasoning.processing.ConditionProducer;

import javax.persistence.*;
import java.util.*;

/**
 * Base object of analysis result. Shows LM results in time
 *
 * @author Maciej Gladki (maciej.szymon.gladki@cern.ch)
 */
@Entity
public class Condition extends Observable implements Comparable<Condition>, Observer {

    private static final Logger logger = Logger.getLogger(Condition.class);

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;

    @JsonIgnore
    private long duration;

    /** Do not show this condition */
    @JsonIgnore
    @Transient
    private boolean show;

    /** Do not generate notifications based on this condition */
    @JsonIgnore
    @Transient
    private boolean holdNotifications;

    /** Is problematic condition */
    @JsonIgnore
    @Transient
    private boolean problematic;

    private boolean mature;

    @JsonIgnore
    @Transient
    private EntryState state;

    @JsonIgnore
    @Enumerated(EnumType.ORDINAL)
    @Column(name = "logic_module")
    private LogicModuleRegistry logicModule;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "group_name")
    private ConditionGroup group;


    /**
     * Map of contextHandler elements
     */
    @OneToMany(cascade=CascadeType.ALL)
    @JoinTable(name="condition_context_map")
    private Map<String, ContextEntry> context;

    @JsonIgnore
    @ElementCollection
    @CollectionTable(name = "Action", joinColumns = @JoinColumn(name = "condition_id"))
    @Column(name = "action")
    private List<String> actionSteps;

    /**
     * Short title of condition. Displayed in main expert view
     */
    @JsonProperty("content")
    @Column(columnDefinition = "varchar(30)", name = "title")
    private String title;

    @JsonIgnore
    @Column(columnDefinition = "varchar(4000)", name = "description")
    private String description;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "start_date")
    @Index(name = "idx_startdate")
    private Date start;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "end_date")
    @Index(name = "idx_enddate")
    private Date end;

    /**
     * Priority of the condition, indicates if is important and should be
     * highlighted or not
     */
    @Enumerated(EnumType.ORDINAL)
    private ConditionPriority priority;

    public boolean isShow() {
        return show;
    }

    public void setShow(boolean show) {
        this.show = show;
    }

    public Condition() {
        show = true;
        this.state = EntryState.NEW;

    }

    /**
     * Get duration in ms
     *
     * @return duration in ms
     */
    public long getDuration() {
        return duration;
    }

    public void calculateDuration() {
        this.duration = this.getEnd().getTime() - this.getStart().getTime();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title.length() > 30) {
            logger.info("Title too long: " + title + ", trimming to : " + title.substring(0, 30));
            title = title.substring(0, 30);
        }
        this.title = title;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
        setChanged();
        publishUpdate();
    }

    @Override
    public int compareTo(Condition arg0) {
        return (int) (this.duration - arg0.duration);
    }

    public ConditionPriority getClassName() {
        return priority;
    }

    public void setClassName(ConditionPriority className) {
        this.priority = className;
    }

    public EntryState getState() {
        return state;
    }

    public void setState(EntryState state) {
        this.state = state;
    }

    public LogicModuleRegistry getLogicModule() {
        return logicModule;
    }

    public void setLogicModule(LogicModuleRegistry eventFinder) {
        this.logicModule = eventFinder;
    }


    public Map<String, ContextEntry> getContext() {
        return context;
    }

    public void setContext(Map<String, ContextEntry>  context) {
        this.context = context;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getActionSteps() {
        return actionSteps;
    }

    public void setActionSteps(List<String> actionSteps) {
        this.actionSteps = actionSteps;
    }

    public ConditionGroup getGroup() {
        return group;
    }

    public void setGroup(ConditionGroup group) {
        this.group = group;
    }


    public boolean isHoldNotifications() {
        return holdNotifications;
    }

    public void setHoldNotifications(boolean holdNotifications) {
        this.holdNotifications = holdNotifications;
    }

    @Override
    public String toString() {
        return "Condition [id=" + id + ", duration=" + duration + ", show=" + show + ", state=" + state
                + ", logicModule=" + logicModule + ", group=" + group
                + ", title=" + title + ", description=" + description + ", start=" + start + ", end=" + end
                + ", priority=" + priority + "]";
    }

    public ConditionPriority getPriority() {
        return priority;
    }

    public void setPriority(ConditionPriority priority) {
        this.priority = priority;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (duration ^ (duration >>> 32));
        result = prime * result + ((group == null) ? 0 : group.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((logicModule == null) ? 0 : logicModule.hashCode());
        result = prime * result + (show ? 1231 : 1237);
        result = prime * result + ((start == null) ? 0 : start.hashCode());
        result = prime * result + ((title == null) ? 0 : title.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Condition other = (Condition) obj;
        if (duration != other.duration)
            return false;
        if (group != other.group)
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (logicModule != other.logicModule)
            return false;
        if (show != other.show)
            return false;
        if (start == null) {
            if (other.start != null)
                return false;
        } else if (!start.equals(other.start))
            return false;
        if (title == null) {
            if (other.title != null)
                return false;
        } else if (!title.equals(other.title))
            return false;
        return true;
    }

    public boolean isMature() {
        return mature;
    }

    public void setMature(boolean mature) {
        this.mature = mature;
    }

    @Override
    public void update(Observable o, Object arg) {

        if (o instanceof ContextNotifier) {
            if (logicModule.getLogicModule() instanceof ContextLogicModule) {
                description = ((ContextLogicModule) logicModule.getLogicModule()).getDescriptionWithContext();

                logger.debug("Condition '" + title + "' received update from it's contextHandler, now description is: " + description);
            }
            ((ContextNotifier) o).clearChangeset();
            setChanged();
            logger.debug("Condition '" + title + "' received update from it's contextHandler, " + this.start);
        }

    }

    public void publishUpdate() {
        logger.trace("Checking if condition '" + title + "'changed " + start);
        if (hasChanged()) {

            notifyObservers();
        }
    }

    public boolean isProblematic() {
        return problematic;
    }

    public void setProblematic(boolean problematic) {
        this.problematic = problematic;
    }
}
