package com.spotfinder.backend.v1.analytics.interfaces.rest.resources;

public record OccupancyByHourResource(int hour, int percentage, int occupied, int total) {}

