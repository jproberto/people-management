package com.itau.hr.people_management.application.position.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreatePositionRequest {

    @NotBlank(message = "{validation.position.title.blank}")
    @Size(min = 3, max = 100, message = "{validation.position.title.length}")
    private String title; 

    @NotBlank(message = "{validation.positionlevel.name.blank}")
    private String positionLevelName;
}