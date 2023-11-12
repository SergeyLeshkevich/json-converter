package by.leshkevich.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Company {
    private String name;
    private Map<UUID, Employee> employees;
    private boolean isBelarusian;
    private LocalDateTime dateTime;
}
