package pl.wrapper.parking.facade.dto.stats;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record OccupancyInfo(DayOfWeek dayOfWeek, LocalTime time) {}
