package rcms.utilities.daqexpert.servlets;

import lombok.Builder;
import lombok.Data;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;

import java.util.Date;
import java.util.Set;

@Data
@Builder
public class ConditionDetailedDTO {

    Long id;

    String title;

    Date start;

    Date end;

    Long duration;

    String description;

    String code;

    Set<String> problematicFed;
    Set<String> problematicPartition;
    Set<String> problematicSubsystem;

}
