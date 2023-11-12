package by.leshkevich.converter;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class JsonConverter {

    public static String convertToJson(Object obj) {
        StringBuilder json = new StringBuilder();

        if (obj == null)
            json.append("null");
        else if (obj instanceof String || obj instanceof UUID || obj instanceof Character)
            json.append("\"").append(obj).append("\"");
        else if (obj instanceof Number || obj instanceof Boolean)
            json.append(obj);
        else if (obj.getClass().isArray() || obj instanceof Collection<?>)
            json.append(arrayOrCollectionToJson(obj));
        else if (obj instanceof Map<?, ?>)
            json.append(mapToJson(obj));
        else if (obj instanceof LocalDateTime)
            json.append(convertLocalDateTimeToJson((LocalDateTime) obj));
        else
            json.append(convertObjectToJson(obj));

        return json.toString();
    }

    private static String convertObjectToJson(Object obj) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        Field[] fields = obj.getClass().getDeclaredFields();
        List<Field> fieldList = new ArrayList<>(Arrays.asList(fields));
        fieldList.addAll(Arrays.asList(obj.getClass().getSuperclass().getDeclaredFields()));

        for (int i = 0; i < fieldList.size(); i++) {
            Field field = fieldList.get(i);
            field.setAccessible(true);
            try {
                json.append("\"").append(field.getName()).append("\":");
                Object value = field.get(obj);
                json.append(convertToJson(value));
                if (i < fieldList.size() - 1)
                    json.append(",");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        json.append("}");
        return json.toString();
    }

    private static String mapToJson(Object obj) {
        int count = 0;
        StringBuilder json = new StringBuilder();
        json.append("{");

        for (Map.Entry<?, ?> entry : ((Map<?, ?>) obj).entrySet()) {
            json
                    .append("\"")
                    .append(entry.getKey())
                    .append("\"")
                    .append(":");
            Object mapValue = entry.getValue();
            json.append(convertToJson(mapValue));
            if (++count < ((Map<?, ?>) obj).size()) {
                json.append(",");
            }
        }

        json.append("}");
        return json.toString();
    }

    private static String arrayOrCollectionToJson(Object obj) {
        StringBuilder json = new StringBuilder();
        json.append("[");

        if (obj instanceof Collection<?>) {
            obj = ((Collection<?>) obj).toArray();
        }
        int length = java.lang.reflect.Array.getLength(obj);

        for (int i = 0; i < length; i++) {
            Object value = java.lang.reflect.Array.get(obj, i);
            json.append(convertToJson(value));
            if (i < length - 1)
                json.append(",");
        }
        json.append("]");
        return json.toString();
    }

    private static String convertLocalDateTimeToJson(LocalDateTime dateTime) {
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("\"");

        int year = dateTime.getYear();
        int month = dateTime.getMonthValue();
        int day = dateTime.getDayOfMonth();
        int hour = dateTime.getHour();
        int minute = dateTime.getMinute();
        int second = dateTime.getSecond();

        jsonBuilder.append(year).append("-");
        jsonBuilder.append(month).append("-");
        jsonBuilder.append(day).append(" ");
        jsonBuilder.append(hour).append(":");
        jsonBuilder.append(minute).append(":");
        jsonBuilder.append(second);

        jsonBuilder.append("\"");
        return jsonBuilder.toString();
    }

}


