package org.example.lab_equipment_repair;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("org.example.lab_equipment_repair.mapper")
@SpringBootApplication
public class LabEquipmentRepairApplication {

    public static void main(String[] args) {
        SpringApplication.run(LabEquipmentRepairApplication.class, args);
    }

}
