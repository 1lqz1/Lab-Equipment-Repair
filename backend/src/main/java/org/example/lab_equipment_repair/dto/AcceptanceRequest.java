package org.example.lab_equipment_repair.dto;

import lombok.Data;

@Data
public class AcceptanceRequest {

    private Boolean accepted = true;

    private String comment;
}
