package com.spotfinder.backend.v1.analytics.domain.model;

public record OccupancyByHourDTO(int hour, int percentage, int occupied, int total) {}

