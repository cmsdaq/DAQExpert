package rcms.utilities.daqexpert.reasoning.logic.basic.helper;

import java.time.*;

public class WorkingHourResolver {

    public static Time determineTime(Long epochMilli) {

        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneId.of("Europe/Zurich"));
        LocalTime localTime = LocalTime.from(localDateTime);


        LocalTime startExtendedWorkingHour = LocalTime.of(8,0);
        LocalTime startWorkingHour = LocalTime.of(8,30);
        LocalTime endWorkingHour = LocalTime.of(17,30);
        LocalTime endExtendedWorkingHour = LocalTime.of(22,0);


        if(localTime.isAfter(startWorkingHour) && localTime.isBefore(endWorkingHour)){
            return Time.WORKING_HOURS;
        } else if(localTime.isAfter(startExtendedWorkingHour) && localTime.isBefore(endExtendedWorkingHour)){
            return Time.EXTENDED_WORKING_HOURS;
        } else {
            return Time.OUTSIDE_OF_EXTENDED_WORKING_HOURS;
        }

    }
}
