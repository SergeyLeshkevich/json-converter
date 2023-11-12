package by.leshkevich.converter;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonParser {

    public <T> Object getObjectFromJson(String json, Class<T> clazz) throws Exception {
        if (json == null || json.isEmpty()) {
            return null;
        } else if (clazz.equals(UUID.class)) {
            return UUID.fromString(json);
        }else if (clazz.isPrimitive()) {
            return parsedNumber(json,clazz);
        } else {
            Object resultObject = clazz.getDeclaredConstructor().newInstance();
            Map<String, Object> mapFields = new LinkedHashMap<>();
            Field[] fields = clazz.getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);
                mapFields.putAll(Objects.requireNonNull(parseJsonToObject(field, json)));
                assignValueToField(resultObject, mapFields, field);
            }

            return resultObject;
        }

    }

    private void assignValueToField(Object resaltObject, Map<String, Object> map, Field field) throws IllegalAccessException {
        String fieldName = field.getName();
        if (map.containsKey(fieldName)) {
            Object fieldValue = map.get(fieldName);
            if (field.getType().equals(UUID.class)) {
                field.set(resaltObject, UUID.fromString((String) fieldValue));
            } else if (field.getType().isPrimitive()) {
                field.set(resaltObject, parsedNumber((String) fieldValue, field.getType()));
            } else if (field.getType().equals(LocalDateTime.class)) {
                field.set(resaltObject, map.get(fieldName));
            } else {
                field.set(resaltObject, field.getType().cast(fieldValue));
            }
        }
    }

    public <V> Object parsJsonToList(String json, Class<V> GenericClazz) throws Exception {
        Map<String, Object> mapKeyFieldsNameAndValue = new LinkedHashMap<>();
        String[] jsonsObjects = json.split("},\\{");

        Field[] fields = GenericClazz.getDeclaredFields();
        List<Object> resultObject = new ArrayList<>();
        for (int i = 0; i < jsonsObjects.length; i++) {
            Object instance = GenericClazz.getDeclaredConstructor().newInstance();
            for (Field field : fields) {
                field.setAccessible(true);
                mapKeyFieldsNameAndValue.putAll(Objects.requireNonNull(parseJsonToObject(field, jsonsObjects[i])));
                assignValueToField(instance, mapKeyFieldsNameAndValue, field);
            }
            resultObject.add(instance);
        }
        return resultObject;
    }

    public <K, V> Object parsJsonToMap(String json, Class<K> KeyGenericClazz, Class<V> ValueGenericClazz) throws Exception {
        Map<Object, Object> resaltMap = new LinkedHashMap<>();
        Map<String, Object> mapKeyFieldsNameAndValue = new LinkedHashMap<>();

        Field[] fieldsKeys = KeyGenericClazz.getDeclaredFields();
        Field[] fieldsValues = ValueGenericClazz.getDeclaredFields();

        Map<String, String> map = getKeyAndValueMap(json);

        for (Map.Entry<String, String> entry : map.entrySet()) {
            Object keyInstance = null;
            Object valueInstance = null;

            keyInstance = parseEntryKey(KeyGenericClazz, mapKeyFieldsNameAndValue, fieldsKeys, entry);
            valueInstance = parseEntryValue(ValueGenericClazz, mapKeyFieldsNameAndValue, fieldsValues, entry);

            resaltMap.put(keyInstance, valueInstance);
        }

        return resaltMap;
    }

    private <V> Object parseEntryValue(Class<V> ValueGenericClazz, Map<String, Object> mapKeyFieldsNameAndValue,
                                       Field[] fieldsValues, Map.Entry<String, String> entry) throws Exception {
        Object valueInstance;
        if (ValueGenericClazz.equals(Integer.class)) {
            valueInstance = Integer.parseInt(entry.getValue());
        } else {
            valueInstance = ValueGenericClazz.getDeclaredConstructor().newInstance();

            for (Field field : fieldsValues) {
                field.setAccessible(true);
                mapKeyFieldsNameAndValue.putAll(Objects.requireNonNull(parseJsonToObject(field, entry.getValue())));
                assignValueToField(valueInstance, mapKeyFieldsNameAndValue, field);
            }
        }
        return valueInstance;
    }

    private <K> Object parseEntryKey(Class<K> KeyGenericClazz, Map<String, Object> mapKeyFieldsNameAndValue,
                                     Field[] fieldsKeys, Map.Entry<String, String> entry) throws Exception {
        Object keyInstance;
        if (KeyGenericClazz.equals(Integer.class)) {
            keyInstance = Integer.parseInt(entry.getKey());
        } else if (KeyGenericClazz.equals(Double.class)) {
            keyInstance = Double.parseDouble(entry.getKey());
        } else if (KeyGenericClazz.equals(Long.class)) {
            keyInstance = Long.parseLong(entry.getKey());
        } else if (KeyGenericClazz.equals(UUID.class)) {
            keyInstance = UUID.fromString(entry.getKey());
        } else {

            keyInstance = KeyGenericClazz.getDeclaredConstructor().newInstance();

            for (Field field : fieldsKeys) {
                field.setAccessible(true);
                mapKeyFieldsNameAndValue.putAll(Objects.requireNonNull(parseJsonToObject(field, entry.getKey())));
                assignValueToField(keyInstance, mapKeyFieldsNameAndValue, field);
            }
        }
        return keyInstance;
    }

    private Map<String, Object> parseJsonToObject(Field field, String json) throws Exception {
        Class<?> type = field.getType();

        if (Collection.class.isAssignableFrom(field.getType())) {
            return parseCollection(json, field);
        } else if (type.equals(Map.class)) {
            return parseMap(json, field);
        } else if (type.isPrimitive()) {
            return parsePrimitiveByField(json, field);
        } else if (type.equals(String.class) || type.equals(UUID.class)) {
            return parseStringByField(json, field);
        } else if (type.equals(LocalDateTime.class)) {
            return parseLocalDateType(json, field);
        } else {
            return parseObject(json, field);
        }
    }

    private Map<String, Object> parseLocalDateType(String json, Field field) {
        Map<String, Object> resultMap = new LinkedHashMap<>();

        String jsonField = getJsonByFieldForObject(json, field);
        String localDateTimeJson = findJsonField(jsonField, field.getName());
        List<Integer> list = getListDatesFromJson(localDateTimeJson);

        resultMap.put(field.getName(), LocalDateTime.of(list.get(0), list.get(1), list.get(2),
                list.get(3), list.get(4), list.get(5)));

        return resultMap;
    }

    private static String findJsonField(String json, String field) {
        int start = json.indexOf(field + "\":") + field.length() + 3;
        int end = json.indexOf("}", start);
        return json.substring(start, end);
    }

    private static List<Integer> getListDatesFromJson(String json) {
        List<Integer> list = new ArrayList<>();

        String[] dateAndTime = json.split(" ");
        String[] date = dateAndTime[0].split("-");
        String[] time = dateAndTime[1].split(":");

        for (String strings : date) {
            list.add(Integer.parseInt(strings));
        }

        for (String strings : time) {
            list.add(Integer.parseInt(strings.replaceAll("\"", "")));
        }

        return list;
    }

    private Map<String, Object> parseObject(String json, Field field) throws Exception {
        Map<String, Object> resultMap = new LinkedHashMap<>();
        resultMap.put(field.getName(), null);
        String jsonField = getJsonByFieldForObject(json, field);

        Object valueObject = getObjectFromJson(jsonField, field.getType());
        resultMap.put(field.getName(), valueObject);
        return resultMap;
    }

    private String getMather(Pattern pattern, String json) {
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group();
        }
        return matcher.toString();
    }

    private Map<String, Object> parseMap(String json, Field field) throws Exception {
        Map<Object, Object> map = new LinkedHashMap<>();
        String[] generics;
        String classNameGeneric = getClassNameGeneric(field);
        String jsonField = getJsonByFieldForObject(json, field);

        if (classNameGeneric != null) {
            generics = classNameGeneric.split(", ");
            Class<?> clazz1 = Class.forName(generics[0]);
            Class<?> clazz2 = Class.forName(generics[1]);

            map.putAll((Map<?, ?>) parsJsonToMap(jsonField, clazz1, clazz2));
        }
        Map<String, Object> resultMap = new LinkedHashMap<>();
        resultMap.put(field.getName(), map);
        return resultMap;
    }

    private Map<String, Object> parseCollection(String json, Field field) throws Exception {
        Object obj;
        Map<String, Object> resultMap = new LinkedHashMap<>();
        List<Object> list = new ArrayList<>();
        String key = null;
        String str;

        String regex = "\"(" + field.getName() + ")\":\\[\\{(.*?)}]";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(json);

        if (matcher.find()) {
            key = matcher.group(1);
            str = matcher.group(2);
            String[] entries = str.split("},\\{");
            String classNameGeneric = getClassNameGeneric(field);
            Class<?> clazz = Class.forName(classNameGeneric);
            for (String entry : entries) {
                obj = getObjectFromJson(entry, clazz);

                list.add(obj);
            }
        }
        resultMap.put(key, list);
        return resultMap;
    }

    private String getJsonByFieldForObject(String json, Field field) {
        String fieldName = field.getName();
        int indexOf = json.indexOf(fieldName);
        String jsonMap = json.substring(indexOf);
        char[] chars = jsonMap.toCharArray();
        StringBuilder stringBuilder = new StringBuilder();
        int count = 0;
        boolean firstOccurrence = false;

        buildStringForObjectByJson(chars, stringBuilder, count, firstOccurrence);

        return stringBuilder.toString();
    }

    private void buildStringForObjectByJson(char[] chars, StringBuilder stringBuilder, int count, boolean firstOccurrence) {
        for (char c : chars) {
            if (c == '{') {
                firstOccurrence = true;
                count++;
            }

            if (c == '}') {
                count--;
            }

            stringBuilder.append(c);

            if (count == 0 && firstOccurrence) {
                break;
            }

        }
    }

    private String getKeyToMap(String json) {
        char[] chars = json.toCharArray();
        StringBuilder stringBuilder = new StringBuilder();
        int count = 0;
        boolean firstOccurrence = false;
        for (char c : chars) {
            if (c == ':' && firstOccurrence) {
                count++;
                firstOccurrence = false;
            }

            if (firstOccurrence) {
                stringBuilder.append(c);
            }

            if (c == '{') {
                firstOccurrence = true;
                count++;
            }

            if (count == 2) {
                break;
            }

        }
        String builderString = stringBuilder.toString();
        if (builderString.equals("") || builderString.equals("}")) {
            return "";
        }

        return builderString.substring(1, builderString.length() - 1);
    }

    private Map<String, String> getKeyAndValueMap(String json) {
        Map<String, String> resultMap = new LinkedHashMap<>();
        int index;

        do {
            String key = getKeyToMap(json);
            String value = getValueToMap(json, key);
            index = json.indexOf(value);

            if (index == 0) {
                break;
            }

            resultMap.put(key, value);

            json = json.substring(index + value.length());
            json = json.replace("},", "");
            json = "{" + json;

        } while (index > 0);

        return resultMap;
    }

    public String getValueToMap(String json, String key) {

        StringBuilder stringBuilder = new StringBuilder();
        int indexKey = json.indexOf(key);
        String jsonForSearchValue = json.substring(indexKey + key.length());
        int count = 0;
        char[] chars = jsonForSearchValue.toCharArray();

        boolean firstOccurrence = false;
        buildStringForObjectByJson(chars, stringBuilder, count, firstOccurrence);

        String builderString = stringBuilder.toString();
        if (builderString.length() <= 2) {
            return "";
        }

        return builderString.substring(1, builderString.length() - 1);
    }


    private String getClassNameGeneric(Field field) {
        String genericType = field.getGenericType().getTypeName();
        Pattern pattern = Pattern.compile("<(.*)>");
        Matcher matcher = pattern.matcher(genericType);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private Map<String, Object> parseStringByField(String json, Field field) {
        Map<String, Object> map = new LinkedHashMap<>();
        String regex = "\"" + field.getName() + "\":\"(.*?)\"";
        Pattern pattern = Pattern.compile(regex);
        String resultMatcher = getMather(pattern, json);

        String[] entries = resultMatcher.split(":");
        String key = entries[0].substring(1, entries[0].length() - 1);
        String value = entries[1].substring(1, entries[1].length() - 1);
        map.put(key, value);
        return map;
    }

    private Object parsedNumber(String value, Class<?> type) {
        return switch (type.getSimpleName()) {
            case "BigDecimal" -> new BigDecimal(value);
            case "BigInteger" -> new BigInteger(value);
            case "Integer", "int" -> Integer.parseInt(value);
            case "Long", "long" -> Long.parseLong(value);
            case "Byte", "byte" -> Byte.parseByte(value);
            case "Short", "short" -> Short.parseShort(value);
            case "Float", "float" -> Float.parseFloat(value);
            case "Boolean", "boolean" -> Boolean.parseBoolean(value);
            default -> Double.parseDouble(value);
        };
    }

    private Map<String, Object> parsePrimitiveByField(String json, Field field) {
        Map<String, Object> resultMap = new LinkedHashMap<>();
        String regex = "\"(" + field.getName() + ")\":(\\w+.?\\w+|true|false)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            String key = matcher.group(1);
            String value = matcher.group(2);
            resultMap.put(key, value);
        }
        return resultMap;
    }
}