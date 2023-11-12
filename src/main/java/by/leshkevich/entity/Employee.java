package by.leshkevich.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Employee {
    private UUID id;
    private Person person;
    private double salary;
    private String post;
}
