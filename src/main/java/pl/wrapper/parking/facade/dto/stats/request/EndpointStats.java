package pl.wrapper.parking.facade.dto.stats.request;

public record EndpointStats(long totalRequests, long successfulRequests, double successRate) {}
