package rcms.utilities.daqexpert.servlets;

import lombok.Data;
import rcms.utilities.daqexpert.persistence.LogicModuleRegistry;

import java.util.Date;

@Data
public class ConditionDetailedDTO {

    Long id;

    String title;

    LogicModuleRegistry logicModule;

    Date start;

    Date end;

    Long duration;

    String description;
}
