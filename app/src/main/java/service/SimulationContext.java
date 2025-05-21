package service;
// File: com/papertradefx/SimulationContext.java

import java.time.LocalDate;

/**
 * SimulationContext holds shared simulation state.
 */
public class SimulationContext {
    private static LocalDate startDate;

    /** Sets simulation start date */
    public static void setStartDate(LocalDate date) {
        startDate = date;
    }

    /** Gets simulation start date */
    public static LocalDate getStartDate() {
        return startDate;
    }
}
