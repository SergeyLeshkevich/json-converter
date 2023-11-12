package by.leshkevich.converter;

import by.leshkevich.entity.Company;
import by.leshkevich.entity.Employee;
import by.leshkevich.entity.Person;
import by.leshkevich.entity.Phone;
import by.leshkevich.util.BuilderGson;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JsonParserTest {

    private final Gson GSON = BuilderGson.buildGson();

    @Test
    void checkParsToDifficultCompanyObjectFromValidJson() throws Exception {
        // given
        JsonParser parser = new JsonParser();

        Phone phone1 = new Phone("+37544111111", "A1");
        Phone phone2 = new Phone("+375292222222", "MTS");
        Phone phone3 = new Phone("+37525333333", "Life");

        Person person1 = new Person("Ivan", "Ivanov", List.of(phone1, phone2));
        Person person2 = new Person("Petr", "Petrov", List.of(phone3));

        Employee employee1 = new Employee(UUID.fromString("052a72eb-0d8c-43a6-aba4-70daddc802d2"), person1, 1000.1, "engineer");
        Employee employee2 = new Employee(UUID.fromString("1c021dfc-e2c8-432e-ab47-2afaabafef8c"), person2, 5000, "director");

        Company expected = new Company("Clever", Map.of(
                employee1.getId(), employee1,
                employee2.getId(), employee2),
                true,
                LocalDateTime.of(2023, 11, 10, 10, 30, 0));

        String json = "{\"name\":\"Clever\",\"employees\":{\"052a72eb-0d8c-43a6-aba4-70daddc802d2\":{\"id\"" +
                ":\"052a72eb-0d8c-43a6-aba4-70daddc802d2\",\"person\":{\"firstname\":\"Ivan\",\"surname\"" +
                ":\"Ivanov\",\"phones\":[{\"number\":\"+37544111111\",\"operator\":\"A1\"},{\"number\":" +
                "\"+375292222222\",\"operator\":\"MTS\"}]},\"salary\":1000.1,\"post\":\"engineer\"}," +
                "\"1c021dfc-e2c8-432e-ab47-2afaabafef8c\":{\"id\":\"1c021dfc-e2c8-432e-ab47-2afaabafef8c\"," +
                "\"person\":{\"firstname\":\"Petr\",\"surname\":\"Petrov\",\"phones\":[{\"number\":\"+37525333333\"," +
                "\"operator\":\"Life\"}]},\"salary\":5000.0,\"post\":\"director\"}},\"isBelarusian\":true," +
                "\"dateTime\":\"2023-11-10 10:30:0\"}";

        // when
        Company actual = (Company) parser.getObjectFromJson(json, Company.class);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void checkParsToDifficultCompanyObjectWithLessNestingFieldsFromJson() throws Exception {
        // given
        JsonParser parser = new JsonParser();
        Phone phone1 = new Phone("+37544111111", "A1");
        Phone phone2 = new Phone("+375292222222", "MTS");

        Person person1 = new Person("Ivan", "Ivanov", List.of(phone1, phone2));

        Employee employee1 = new Employee(UUID.fromString("052a72eb-0d8c-43a6-aba4-70daddc802d2"), person1,
                1000.1, "engineer");

        Company company = new Company("Clever", Map.of(
                employee1.getId(), employee1),
                true,
                LocalDateTime.of(2023, 11, 10, 10, 30, 0));

        String json = GSON.toJson(company);

        // when
        Company company1 = (Company) parser.getObjectFromJson(json, Company.class);
        // then
        assertThat(company1).isEqualTo(company);
    }

    @Test
    void checkParsToListFromJson() throws Exception {
        // given
        JsonParser parser = new JsonParser();
        Phone phone1 = new Phone("+37544111111", "A1");
        Phone phone2 = new Phone("+375292222222", "MTS");

        List<Phone> expected = List.of(phone1, phone2);


        String json = GSON.toJson(expected);

        // when

        List<Phone> actual = (List<Phone>) parser.parsJsonToList(json, Phone.class);
        // then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void checkParsToMapFromJson() throws Exception {
        // given
        JsonParser parser = new JsonParser();
        Phone phone1 = new Phone("+37544111111", "A1");
        Phone phone2 = new Phone("+375292222222", "MTS");

        Map<Integer, Phone> phones = Map.of(1, phone1, 2, phone2);

        String json = GSON.toJson(phones);
        // when
        Map<Integer, Phone> actual = (Map<Integer, Phone>) parser.parsJsonToMap(json, Integer.class, Phone.class);

        // then
        assertThat(actual).isEqualTo(phones);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void checkNullAndEmptyJson(String json) throws Exception {
        // given
        JsonParser parser = new JsonParser();

        // when
        Company actual = (Company) parser.getObjectFromJson(json, Company.class);

        // then
        assertThat(actual).isNull();
    }

    @ParameterizedTest
    @ValueSource(doubles = {
            10.5, 100.0
    })
    void checkParsePrimitiveDouble(double parameter) throws Exception {
        // given
        JsonParser parser = new JsonParser();
        String json = GSON.toJson(parameter);

        // when
        Double actual = (Double) parser.getObjectFromJson(json, double.class);

        // then
        assertThat(actual).isEqualTo(parameter);
    }

    @ParameterizedTest
    @ValueSource(ints = {
            10,100
    })
    void checkParsePrimitiveInt(double parameter) throws Exception {
        // given
        JsonParser parser = new JsonParser();
        String json = GSON.toJson(parameter);

        // when
        Double actual = (Double) parser.getObjectFromJson(json, double.class);

        // then
        assertThat(actual).isEqualTo(parameter);
    }
}