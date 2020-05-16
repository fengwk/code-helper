package com.fengwk.codehelper.gen;

import java.beans.IntrospectionException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fengwk.codehelper.util.TypeUtils;
import com.fengwk.codehelper.util.BeanUtils.MethodDesc;
import com.fengwk.codehelper.util.BeanUtils.PropertyDesc;

/**
 * 
 * @author fengwk
 */
public class AutoSetGenerator {

	Pojo pojo;
    
    public AutoSetGenerator(Pojo pojo) {
        this.pojo = pojo;
    }

    public String gen() throws IntrospectionException {
        StringBuilder builder = new StringBuilder();
        String cname = GenConvertMethodHandler.regularize(pojo.clsName());
        builder.append("        ").append(pojo.clsName()).append(' ').append(cname).append(" = new ").append(pojo.clsName()).append("();\n");
        PropertyDesc[] pds = pojo.propertyDescriptors();
        for (int i = 0; i < pds.length; i++) {
            PropertyDesc pd = pds[i];
            MethodDesc wm = pd.getWriteMethod();
            if (wm == null || wm.getGenericParameterTypes() == null || wm.getGenericParameterTypes().length != 1) {
                continue;
            }
            builder.append("        ").append(cname).append('.').append(wm.getName()).append('(').append(get(wm.getGenericParameterTypes()[0], pd.getName())).append(");");
            if (i < pds.length - 1) {
                builder.append('\n');
            }
        }
        return builder.toString();
    }
    
    public String get(Type type, String name) {
        if (type instanceof Class) {
            Class<?> c = (Class<?>) type;
            if (c == String.class) {
                return '\"' + name + '\"';
            }
            if (c == Integer.class || c == int.class) {
                return "0";
            }
            if (c == Long.class || c == long.class) {
                return "0L";
            }
            if (c == Short.class || c == short.class) {
                return "0";
            }
            if (c == Byte.class || c == byte.class) {
                return "0";
            }
            if (c == Character.class || c == char.class) {
                return "'0'";
            }
            if (c == Boolean.class || c == boolean.class) {
                return "true";
            }
            if (c == Float.class || c == float.class) {
                return "0.f";
            }
            if (c == Double.class || c == double.class) {
                return "0.";
            }
            if (c == Date.class) {
                return "new java.util.Date()";
            }
            if (c == LocalDateTime.class) {
                return "java.time.LocalDateTime.now()";
            }
            if (c == LocalDate.class) {
                return "java.time.LocalDate.now()";
            }
            if (c == Object.class) {
                return "new Object()";
            }
            if (c == Object[].class) {
                return "new Object[0]";
            }
            if (c == int[].class) {
                return "new int[0]";
            }
            if (c == long[].class) {
                return "new long[0]";
            }
            if (c == short[].class) {
                return "new short[0]";
            }
            if (c == byte[].class) {
                return "new byte[0]";
            }
            if (c == char[].class) {
                return "new char[0]";
            }
            if (c == boolean[].class) {
                return "new boolean[0]";
            }
            if (c == float[].class) {
                return "new float[0]";
            }
            if (c == double[].class) {
                return "new double[0]";
            }
            if (c == BigDecimal.class) {
                return "new java.math.BigDecimal(\"0\")";
            }
        }
        if (TypeUtils.isAssignable(List.class, type)) {
            return "java.util.Arrays.asList()";
        }
        if (TypeUtils.isAssignable(Set.class, type)) {
            return "new java.util.HashSet<>(java.util.Arrays.asList())";
        }
        if (TypeUtils.isAssignable(Map.class, type)) {
            return "new java.util.HashMap<>()";
        }
        return "null";
    }
    
}
