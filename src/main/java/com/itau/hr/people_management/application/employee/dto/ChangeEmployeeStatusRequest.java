package com.itau.hr.people_management.application.employee.dto;

import com.itau.hr.people_management.domain.employee.enumeration.EmployeeStatus;

import jakarta.validation.constraints.NotNull;

public record ChangeEmployeeStatusRequest(@NotNull(message = "New status cannot be null") EmployeeStatus newStatus) {}