package com.fengwk.codehelper.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author fengwk
 */
public class BeanUtils {
    
    private BeanUtils() {}
    
    public static PropertyDesc getPropertyDescriptor(Class<?> beanClass, String propertyName) {
        try {
            return convert(beanClass, new PropertyDescriptor(propertyName, beanClass));
        } catch (IntrospectionException e) {
            return null;
        }
    }
    
    public static PropertyDesc[] getPropertyDescriptors(Class<?> beanClass) throws IntrospectionException {
        BeanInfo beanInfo = Introspector.getBeanInfo(beanClass);
        PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
        PropertyDesc[] nPds = new PropertyDesc[pds.length];
        for (int i = 0; i < pds.length; i++) {
            nPds[i] = convert(beanClass, pds[i]);
        }
        return nPds;
    }
    
    private static PropertyDesc convert(Class<?> beanClass, PropertyDescriptor pd) {
        return new PropertyDesc(pd.getName(), convert(beanClass, pd.getReadMethod()), convert(beanClass, pd.getWriteMethod()));
    }
    
    @SuppressWarnings("rawtypes")
    private static MethodDesc convert(Class beanClass, Method m) {
        if (m == null) {
            return null;
        }
        Class declaringClass = m.getDeclaringClass();
        Type[] types = m.getGenericParameterTypes();
        Type[] genericParameterTypes = new Type[types.length];
        for (int i = 0; i < types.length; i++) {
            genericParameterTypes[i] = convert(beanClass, declaringClass, types[i]);
        }
        return new MethodDesc(m.getName(), genericParameterTypes, convert(beanClass, declaringClass, m.getGenericReturnType()));
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Type convert(Class beanClass, Class declaringClass, Type type) {
        if (type instanceof TypeVariable) {
            return GenericUtils.findNearestVarType(beanClass, declaringClass, (TypeVariable<?>) type);
        }
        if (type instanceof ParameterizedType) {
            List<Type> nAtas = new ArrayList<>();
            for (Type ata : ((ParameterizedType) type).getActualTypeArguments()) {
                nAtas.add(convert(beanClass, declaringClass, ata));
            }
            return new ParameterizedTypeImpl(nAtas.toArray(new Type[nAtas.size()]), ((ParameterizedType) type).getOwnerType(), ((ParameterizedType) type).getRawType());
        }
        if (type instanceof GenericArrayType) {
            return new GenericArrayTypeImpl(convert(beanClass, declaringClass, ((GenericArrayType) type).getGenericComponentType()));
        }
        return type;
    }
    
    public static class PropertyDesc {
        
        String name;
        MethodDesc readMethod;
        MethodDesc writeMethod;
        
        public PropertyDesc(String name, MethodDesc readMethod, MethodDesc writeMethod) {
            this.name = name;
            this.readMethod = readMethod;
            this.writeMethod = writeMethod;
        }

        public String getName() {
            return name;
        }

        public MethodDesc getReadMethod() {
            return readMethod;
        }

        public MethodDesc getWriteMethod() {
            return writeMethod;
        }
        
    }
    
    public static class MethodDesc {
        
        String name;
        Type[] genericParameterTypes;
        Type genericReturnType;
        
        public MethodDesc(String name, Type[] genericParameterTypes, Type genericReturnType) {
            this.name = name;
            this.genericParameterTypes = genericParameterTypes;
            this.genericReturnType = genericReturnType;
        }

        public String getName() {
            return name;
        }

        public Type[] getGenericParameterTypes() {
            return genericParameterTypes;
        }

        public Type getGenericReturnType() {
            return genericReturnType;
        }
        
    }

}
