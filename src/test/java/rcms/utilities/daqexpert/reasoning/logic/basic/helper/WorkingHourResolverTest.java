package rcms.utilities.daqexpert.reasoning.logic.basic.helper;

import org.junit.Assert;
import org.junit.Test;

import java.time.*;

public class WorkingHourResolverTest {

    @Test
    public void test() {


        Assert.assertEquals(Time.OUTSIDE_OF_EXTENDED_WORKING_HOURS,
                            WorkingHourResolver.determineTime(convert(LocalTime.of(0, 0, 0))));
        Assert.assertEquals(Time.OUTSIDE_OF_EXTENDED_WORKING_HOURS,
                            WorkingHourResolver.determineTime(convert(LocalTime.of(8, 0, 0))));

        Assert.assertEquals(Time.EXTENDED_WORKING_HOURS,
                            WorkingHourResolver.determineTime(convert(LocalTime.of(8, 0, 1))));
        Assert.assertEquals(Time.EXTENDED_WORKING_HOURS,
                            WorkingHourResolver.determineTime(convert(LocalTime.of(8, 30, 0))));

        Assert.assertEquals(Time.WORKING_HOURS,
                            WorkingHourResolver.determineTime(convert(LocalTime.of(8, 30, 1))));
        Assert.assertEquals(Time.WORKING_HOURS,
                            WorkingHourResolver.determineTime(convert(LocalTime.of(10, 0, 0))));
        Assert.assertEquals(Time.WORKING_HOURS,
                            WorkingHourResolver.determineTime(convert(LocalTime.of(17, 29, 59))));


        Assert.assertEquals(Time.EXTENDED_WORKING_HOURS,
                            WorkingHourResolver.determineTime(convert(LocalTime.of(17, 30, 0))));
        Assert.assertEquals(Time.EXTENDED_WORKING_HOURS,
                            WorkingHourResolver.determineTime(convert(LocalTime.of(21, 59, 59))));

        Assert.assertEquals(Time.OUTSIDE_OF_EXTENDED_WORKING_HOURS,
                            WorkingHourResolver.determineTime(convert(LocalTime.of(22, 0, 0))));
        Assert.assertEquals(Time.OUTSIDE_OF_EXTENDED_WORKING_HOURS,
                            WorkingHourResolver.determineTime(convert(LocalTime.of(23, 0, 0))));
        Assert.assertEquals(Time.OUTSIDE_OF_EXTENDED_WORKING_HOURS,
                            WorkingHourResolver.determineTime(convert(LocalTime.of(23, 59, 0))));

    }

    private Long convert(LocalTime localTime) {

        LocalDateTime localDateTime = LocalDateTime.of(LocalDate.now(), localTime);
        ZonedDateTime zonedDateTime = ZonedDateTime.ofLocal(localDateTime, ZoneId.of("Europe/Zurich"), ZoneOffset.UTC);
        return zonedDateTime.toInstant().toEpochMilli();

    }

}