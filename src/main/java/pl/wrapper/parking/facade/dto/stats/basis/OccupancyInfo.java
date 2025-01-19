package pl.wrapper.parking.facade.dto.stats.basis;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record OccupancyInfo(DayOfWeek dayOfWeek, LocalTime time) {}
