package by.leshkevich.converter;

import by.leshkevich.entity.Company;
import by.leshkevich.entity.Employee;
import by.leshkevich.entity.Person;
import by.leshkevich.entity.Phone;
import by.leshkevich.util.BuilderGson;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import net.javacrumbs.jsonunit.core.Option;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

class JsonConverterTest {

    private final Gson GSON = BuilderGson.buildGson();
    private final ObjectMapper MAPPER = new ObjectMapper();

    private static Stream<Arguments> getArgumentSourceForDifficultObject() {
        Phone phone1 = new Phone("+37544111111", "A1");
        Phone phone2 = new Phone("+375292222222", "MTS");
        Phone phone3 = new Phone("+37525333333", "Life");

        Person person1 = new Person("Ivan", "Ivanov", List.of(phone1, phone2));
        Person person2 = new Person("Petr", "Petrov", List.of(phone3));

        Employee employee1 = new Employee(UUID.randomUUID(), person1, 1000.1, "engineer");
        Employee employee2 = new Employee(UUID.randomUUID(), person2, 5000, "director");

        Company company1 = new Company("Clever", Map.of(
                employee2.getId(), employee2),
                true,
                LocalDateTime.of(2023, 11, 10, 10, 30, 10));

        Company company2 = new Company("Clever", Map.of(
                employee1.getId(), employee1,
                employee2.getId(), employee2),
                true,
                LocalDateTime.of(2023, 11, 10, 10, 30, 10));
        return Stream.of(
            Arguments.of(company1),
            Arguments.of(company2)
        );
    }

    @ParameterizedTest
    @NullSource
    @EmptySource
    void checkConvertNullAndEmptyObject(String parameter) {
        // given

        String expected = GSON.toJson(parameter);

        // when
        String actual = JsonConverter.convertToJson(parameter);

        // then
        assertThatJson(actual).when(Option.IGNORING_ARRAY_ORDER)
                .isEqualTo(expected);
    }

    @Test
    void checkConvertInt() {
        // given
        int number = 1;
        String expected = GSON.toJson(number);

        // when
        String actual = JsonConverter.convertToJson(number);

        // then
        assertThatJson(actual).when(Option.IGNORING_ARRAY_ORDER)
                .isEqualTo(expected);
    }

    @Test
    void checkConvertIntegerObject() {
        // given
        Integer number = 1;
        String expected = GSON.toJson(number);

        // when
        String actual = JsonConverter.convertToJson(number);

        // then
        assertThatJson(actual).when(Option.IGNORING_ARRAY_ORDER)
                .isEqualTo(expected);
    }

    @Test
    void checkConvertDoubleObject() {
        // given
        Double number = 1.5;
        String expected = GSON.toJson(number);

        // when
        String actual = JsonConverter.convertToJson(number);

        // then
        assertThatJson(actual).when(Option.IGNORING_ARRAY_ORDER)
                .isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(doubles = {
            3.5,100
    })
    void checkConvertDouble(double parameter) {
        // given
        String expected = GSON.toJson(parameter);

        // when
        String actual = JsonConverter.convertToJson(parameter);

        // then
        assertThatJson(actual).when(Option.IGNORING_ARRAY_ORDER)
                .isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(chars = {
            'c','1'
    })
    void checkConvertChar(char parameter) {
        // given
        String expected = GSON.toJson(parameter);

        // when
        String actual = JsonConverter.convertToJson(parameter);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void checkConvertLocalDateTime() {
        // given
        LocalDateTime date = LocalDateTime.of(2023, 11, 10, 10, 30, 10);
        String expected = GSON.toJson(date);

        // when
        String actual = JsonConverter.convertToJson(date);

        // then
        assertThatJson(actual).when(Option.IGNORING_ARRAY_ORDER)
                .isEqualTo(expected);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Clever", "Test"
    })
    void checkConvertString(String parameter) {
        // given
        String expected = GSON.toJson(parameter);

        // when
        String actual = JsonConverter.convertToJson(parameter);

        // then
        assertThatJson(actual).when(Option.IGNORING_ARRAY_ORDER)
                .isEqualTo(expected);
    }

    @Test
    void checkConvertListObjects() {
        // given
        Phone phone1 = new Phone("+37544111111", "A1");
        Phone phone2 = new Phone("+375292222222", "MTS");
        Phone phone3 = new Phone("+37525333333", "Life");
        List<Phone> list = List.of(phone1, phone2, phone3);
        String expected = GSON.toJson(list);

        // when
        String actual = JsonConverter.convertToJson(list);

        // then
        assertThatJson(actual).when(Option.IGNORING_ARRAY_ORDER)
                .isEqualTo(expected);
    }

    @Test
    void checkConvertMapObjects() {
        // given
        Phone phone1 = new Phone("+37544111111", "A1");
        Phone phone2 = new Phone("+375292222222", "MTS");
        Phone phone3 = new Phone("+37525333333", "Life");
        Map<Integer, Phone> list = Map.of(1, phone1,
                2, phone2,
                3, phone3);
        String expected = GSON.toJson(list);

        // when
        String actual = JsonConverter.convertToJson(list);

        // then
        assertThatJson(actual).when(Option.IGNORING_ARRAY_ORDER)
                .isEqualTo(expected);
    }


    @ParameterizedTest
    @MethodSource("getArgumentSourceForDifficultObject")
    void checkConvertDifficultObjectWithDifferentFieldsToJson(Company company) {
        // given
        String expected = GSON.toJson(company);

        // when
        String actual = JsonConverter.convertToJson(company);

        // then
        assertThatJson(actual).when(Option.IGNORING_ARRAY_ORDER)
                .isEqualTo(expected);
    }

    @Test
    void checkConvertMapWithPhoneToKey() throws JsonProcessingException {
        // given
        Phone phone1 = new Phone("+37544111111", "A1");
        Phone phone2 = new Phone("+375292222222", "MTS");

        Map<Phone, Integer> phones = Map.of(phone1, 1, phone2, 2);
        String expected = MAPPER.writeValueAsString(phones);

        // when
        String actual = JsonConverter.convertToJson(phones);

        // then
        assertThatJson(actual).when(Option.IGNORING_ARRAY_ORDER)
                .isEqualTo(expected);
    }
}