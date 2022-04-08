package com.hao.postgres.util;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import java.io.IOException;
import java.util.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

@Slf4j
public class JsonUtils {
    private JsonUtils() {
    }

    private static final String JSON_PATH_DELIMITER = "/";
    static final ObjectMapper JSON_OBJ_MAPPER;
    private static final ObjectWriter JSON_OBJ_WRITER;
    private static final ObjectReader JSON_OBJ_READER;

    static {
        JSON_OBJ_MAPPER = createObjectMapper();
        JSON_OBJ_WRITER = JSON_OBJ_MAPPER.writer();
        JSON_OBJ_READER = JSON_OBJ_MAPPER.reader();
    }

    public static ObjectMapper createObjectMapper() {
        var om = new ObjectMapper();
        om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        om.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true);
        om.registerModule(new ParameterNamesModule());
        return om;
    }

    public static ObjectWriter writer() {
        return JSON_OBJ_WRITER;
    }

    public static ObjectReader reader() {
        return JSON_OBJ_READER;
    }

    /**
     * Convert the object to a JSON string.
     */
    @SneakyThrows
    public static String objectToString(final Object object) {
        return writer().writeValueAsString(Objects.requireNonNull(object));
    }

    /**
     * Convert the JSON string to a object.
     */
    public static <T> T stringToObject(final String string, final Class<T> objectType) {
        Objects.requireNonNull(string);
        Objects.requireNonNull(objectType);
        JavaType javaType = JSON_OBJ_MAPPER.getTypeFactory().constructType(objectType);
        return stringToObject(string, javaType);
    }

    /**
     * Convert the JSON string to a object.
     */
    public static <T> T stringToObject(final String string, final TypeReference<T> objectType) {
        Objects.requireNonNull(string);
        Objects.requireNonNull(objectType);
        JavaType javaType = JSON_OBJ_MAPPER.getTypeFactory().constructType(objectType);
        return stringToObject(string, javaType);
    }

    public static <T> T stringToObject(final String string, final JavaType objectType) {
        try {
            return JSON_OBJ_MAPPER.readValue(string, objectType);
        } catch (IOException ex) {
            log.error("Could not convert raw string data {} to object type {}", string, objectType.getRawClass(), ex);
            throw new RuntimeException(ex);
        }
    }

    /**
     * Convert the object to a raw map.
     * The naming strategy will be applied, e.g. "templateName" in object will be set to "template-name" in map.
     */
    @SuppressWarnings("rawtypes")
    public static Map objectToMap(final Object object) {
        return JSON_OBJ_MAPPER.convertValue(object, Map.class);
    }

    /**
     * Convert the object to a map with key and value as string.
     * The naming strategy will be applied, e.g. "templateName" in object will be set to "template-name" in map.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String> objectToStringMap(final Object object) {
        Map<String, String> result = new HashMap<>();
        objectToMap(object).forEach((k, v) -> result.put(k.toString(), v != null ? v.toString() : null));
        return result;
    }

    /**
     * Read data in a raw map and convert to an object of target class.
     * The naming strategy will be applied, e.g. "template-name" in map will be set to "templateName" in object.
     */
    public static <T> T mapToObject(final Map<?, ?> map, final Class<T> objectType) {
        Objects.requireNonNull(map);
        Objects.requireNonNull(objectType);
        return JSON_OBJ_MAPPER.convertValue(map, objectType);
    }

    /**
     * Convert the object to a JsonNode object.
     * The naming strategy will be applied, e.g. "templateName" in object will be set to "template-name" in JsonNode.
     */
    public static JsonNode objectToJsonNode(final Object object) {
        return JSON_OBJ_MAPPER.valueToTree(object);
    }

    /**
     * Convert the JSON string to JsonNode
     */
    @SneakyThrows
    public static JsonNode stringToJsonNode(final String string) {
        return JSON_OBJ_MAPPER.readTree(string);
    }

    /**
     * Convert the JsonNode to an object of target class.
     * The naming strategy will be applied, e.g. "template-name" in JsonNode will be set to "templateName" in object.
     */
    @SneakyThrows
    public static <T> T jsonNodeToObject(final JsonNode jsonNode, Class<T> objectType) {
        return JSON_OBJ_MAPPER.treeToValue(jsonNode, objectType);
    }

    public static ObjectNode createObjectNode() {
        return JSON_OBJ_MAPPER.createObjectNode();
    }

    public static ArrayNode createArrayNode() {
        return JSON_OBJ_MAPPER.createArrayNode();
    }

    public static JsonNode setJsonNodeValueAt(final JsonNode targetJsonNode, final JsonPointer ptr, final JsonNode value) {
        JsonPointer parentPtr = ptr.head();
        JsonNode parentNode = targetJsonNode.at(parentPtr);
        String fieldName = ptr.last().toString().substring(1);
        if (parentNode.isMissingNode() || parentNode.isNull()) {
            parentNode = StringUtils.isNumeric(fieldName) ? JSON_OBJ_MAPPER.createArrayNode() : JSON_OBJ_MAPPER.createObjectNode();
            setJsonNodeValueAt(targetJsonNode, parentPtr, parentNode); // recursively reconstruct hierarchy
        }

        if (parentNode.isObject()) {
            if (value.isMissingNode()) {
                ((ObjectNode) parentNode).remove(fieldName);
            } else {
                ((ObjectNode) parentNode).set(fieldName, value);
            }
        } else if (parentNode.isArray()) {
            ArrayNode arrayNode = (ArrayNode) parentNode;
            int index = Integer.parseInt(fieldName);
            // expand array in case index is greater than array size
            for (int i = arrayNode.size(); i <= index; i++) {
                arrayNode.addNull();
            }
            arrayNode.set(index, value);
        } else {
            throw new IllegalArgumentException(String.format("%s can't be set for parent node %s because parent is not a container", fieldName, parentPtr));
        }
        return targetJsonNode;
    }

    public static ArrayNode merge(List<ArrayNode> arrayNodeList) {
        ArrayNode result = createArrayNode();
        for (ArrayNode a : arrayNodeList) {
            result.addAll(a);
        }
        return result;
    }

    public static ArrayNode merge(ArrayNode... arrayNodes) {
        ArrayNode result = createArrayNode();
        for (ArrayNode a : arrayNodes) {
            result.addAll(a);
        }
        return result;
    }

    public static String toJsonPath(final String... fields) {
        return JSON_PATH_DELIMITER.concat(String.join(JSON_PATH_DELIMITER, fields));
    }

    public static String toJsonPath(final List<String> fields) {
        return JSON_PATH_DELIMITER.concat(String.join(JSON_PATH_DELIMITER, fields));
    }

    public static List<BeanPropertyDefinition> getBeanProperties(final Class<?> clazz) {
        JavaType javaType = JSON_OBJ_MAPPER.getTypeFactory().constructType(clazz);
        return JSON_OBJ_MAPPER.getSerializationConfig().introspect(javaType).findProperties();
    }

    public static Optional<BeanPropertyDefinition> findBeanProperty(final String property, final List<BeanPropertyDefinition> beanProperties) {
        if (CollectionUtils.isEmpty(beanProperties)) {
            return Optional.empty();
        }
        return beanProperties.stream().filter(x -> Objects.equals(property, x.getName())).findFirst();
    }
}
