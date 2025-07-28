package com.itau.hr.people_management.application.employee.dto;

import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SearchEmployeeRequest {
    private String name;
    private String emailAddress;
    private String employeeStatus;
    private UUID departmentId;
    private String departmentName;
    private UUID positionId;
    private String positionTitle;
    private String positionLevel;
}
