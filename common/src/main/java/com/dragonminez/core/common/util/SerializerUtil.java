package com.dragonminez.core.common.util;

import net.minecraft.network.FriendlyByteBuf;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class to serialize and deserialize arbitrary POJOs to and from
 * a {@link FriendlyByteBuf} for network transmission.
 * <p>
 * Supports primitive types, String, Lists, and nested objects. Uses reflection
 * to inspect fields and a cache to improve performance.
 * </p>
 */
public class SerializerUtil {

    /**
     * Cache of declared fields per class for faster reflection access
     */
    private static final Map<Class<?>, Field[]> FIELD_CACHE = new ConcurrentHashMap<>();

    /**
     * Writes an object to the buffer by serializing all its fields.
     *
     * @param buf The buffer to write into.
     * @param obj The object to serialize.
     * @throws IllegalAccessException If a field cannot be accessed.
     */
    public static void writeObject(FriendlyByteBuf buf, Object obj) throws IllegalAccessException {
        Class<?> clazz = obj.getClass();
        Field[] fields = FIELD_CACHE.computeIfAbsent(clazz, c -> {
            Field[] fs = c.getDeclaredFields();
            for (Field f : fs) f.setAccessible(true);
            return fs;
        });

        for (Field field : fields) {
            Object value = field.get(obj);
            writeField(buf, field.getType(), value, field);
        }
    }

    /**
     * Reads an object from the buffer by deserializing all its fields.
     *
     * @param buf   The buffer to read from.
     * @param clazz The class of the object to create.
     * @return A new instance of the object with its fields populated.
     * @throws Exception If deserialization fails.
     */
    public static Object readObject(FriendlyByteBuf buf, Class<?> clazz) throws Exception {
        Object obj = clazz.getDeclaredConstructor().newInstance();
        Field[] fields = FIELD_CACHE.computeIfAbsent(clazz, c -> {
            Field[] fs = c.getDeclaredFields();
            for (Field f : fs) f.setAccessible(true);
            return fs;
        });

        for (Field field : fields) {
            Object value = readField(buf, field.getType(), field);
            field.set(obj, value);
        }
        return obj;
    }

    /**
     * Writes a single field value to the buffer.
     *
     * @param buf   The buffer to write into.
     * @param type  The type of the field.
     * @param value The value of the field.
     * @param field The Field object (can be null for nested or list elements).
     * @throws IllegalAccessException If the field cannot be accessed.
     */
    public static void writeField(FriendlyByteBuf buf, Class<?> type, Object value, Field field)
            throws IllegalAccessException {
        if (type == int.class) buf.writeInt((int) value);
        else if (type == double.class) buf.writeDouble((double) value);
        else if (type == float.class) buf.writeFloat((float) value);
        else if (type == long.class) buf.writeLong((long) value);
        else if (type == boolean.class) buf.writeBoolean((boolean) value);
        else if (type == String.class) buf.writeUtf((String) value);
        else if (type == UUID.class) buf.writeUUID((UUID) value);
        else if (List.class.isAssignableFrom(type)) {
            List<?> list = (List<?>) value;
            buf.writeVarInt(list.size());
            Class<?> elementType = getListElementType(field);
            for (Object o : list) {
                if (isPrimitiveOrString(elementType)) writeField(buf, elementType, o, null);
                else writeObject(buf, o);
            }
        } else {
            writeObject(buf, value);
        }
    }

    /**
     * Reads a single field value from the buffer.
     *
     * @param buf   The buffer to read from.
     * @param type  The type of the field.
     * @param field The Field object (can be null for nested or list elements).
     * @return The deserialized value.
     * @throws Exception If deserialization fails.
     */
    public static Object readField(FriendlyByteBuf buf, Class<?> type, Field field) throws Exception {
        if (type == int.class) return buf.readInt();
        if (type == double.class) return buf.readDouble();
        if (type == float.class) return buf.readFloat();
        if (type == long.class) return buf.readLong();
        if (type == boolean.class) return buf.readBoolean();
        if (type == String.class) return buf.readUtf();
        if (type == UUID.class) return buf.readUUID();
        if (List.class.isAssignableFrom(type)) {
            int size = buf.readVarInt();
            List<Object> list = new ArrayList<>();
            Class<?> elementType = getListElementType(field);
            for (int i = 0; i < size; i++) {
                if (isPrimitiveOrString(elementType)) list.add(readField(buf, elementType, null));
                else list.add(readObject(buf, elementType));
            }
            return list;
        }
        return readObject(buf, type);
    }

    /**
     * Checks if the given type is a primitive type or String.
     *
     * @param type The class to check.
     * @return True if primitive or String, false otherwise.
     */
    public static boolean isPrimitiveOrString(Class<?> type) {
        return type.isPrimitive() || type == String.class;
    }

    /**
     * Attempts to determine the element type of a List field.
     *
     * @param field The field representing the list (can be null).
     * @return The class of the list elements, defaults to String if unknown.
     */
    public static Class<?> getListElementType(Field field) {
        if (field == null) return String.class;
        try {
            String typeName = field.getGenericType().getTypeName();
            if (typeName.contains("<") && typeName.contains(">")) {
                String className = typeName.substring(typeName.indexOf("<") + 1, typeName.indexOf(">"));
                return Class.forName(className);
            }
        } catch (ClassNotFoundException ignored) {
        }
        return String.class;
    }
}
