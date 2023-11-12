package by.leshkevich.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Person {

    private String firstname;
    private String surname;
    private List<Phone> phones;
}
