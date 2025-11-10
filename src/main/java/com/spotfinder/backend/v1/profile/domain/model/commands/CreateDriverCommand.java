package com.spotfinder.backend.v1.profile.domain.model.commands;

public record CreateDriverCommand(String fullName, String city, String country,
                                  String phone, String dni) {
}
