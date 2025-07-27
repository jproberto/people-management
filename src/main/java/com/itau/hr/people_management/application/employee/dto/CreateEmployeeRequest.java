package com.itau.hr.people_management.application.employee.dto;

import java.time.LocalDate;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateEmployeeRequest {
    @NotBlank(message = "{validation.employee.name.blank}")
    @Size(min = 2, max = 100, message = "{validation.employee.name.length}")
    private String name;

    @NotBlank(message = "{validation.email.address.blank}")
    @Email(message = "{validation.email.address.invalid}")
    @Size(min = 5, max = 100, message = "{validation.email.address.length}")
    private String email;

    @NotNull(message = "{validation.employee.hiredate.null}")
    private LocalDate hireDate;

    @NotNull(message = "{validation.employee.departmentId.notNull}")
    private UUID departmentId;

    @NotNull(message = "{validation.employee.positionId.notNull}")
    private UUID positionId;
}
