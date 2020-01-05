package com.fengwk.codehelper.util;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * copy from spring
 * 
 * @author fengwk
 */
public class TypeUtils {

    /**
     * Check if the right-hand side type may be assigned to the left-hand side
     * type following the Java generics rules.
     * @param lhsType the target type
     * @param rhsType the value type that should be assigned to the target type
     * @return true if rhs is assignable to lhs
     */
    public static boolean isAssignable(Type lhsType, Type rhsType) {
        // all types are assignable to themselves and to class Object
        if (lhsType.equals(rhsType) || Object.class == lhsType) {
            return true;
        }

        if (lhsType instanceof Class) {
            Class<?> lhsClass = (Class<?>) lhsType;

            // just comparing two classes
            if (rhsType instanceof Class) {
                return isAssignable(lhsClass, (Class<?>) rhsType);
            }

            if (rhsType instanceof ParameterizedType) {
                Type rhsRaw = ((ParameterizedType) rhsType).getRawType();

                // a parameterized type is always assignable to its raw class type
                if (rhsRaw instanceof Class) {
                    return isAssignable(lhsClass, (Class<?>) rhsRaw);
                }
            }
            else if (lhsClass.isArray() && rhsType instanceof GenericArrayType) {
                Type rhsComponent = ((GenericArrayType) rhsType).getGenericComponentType();

                return isAssignable(lhsClass.getComponentType(), rhsComponent);
            }
        }

        // parameterized types are only assignable to other parameterized types and class types
        if (lhsType instanceof ParameterizedType) {
            if (rhsType instanceof Class) {
                Type lhsRaw = ((ParameterizedType) lhsType).getRawType();

                if (lhsRaw instanceof Class) {
                    return isAssignable((Class<?>) lhsRaw, (Class<?>) rhsType);
                }
            }
            else if (rhsType instanceof ParameterizedType) {
                return isAssignable((ParameterizedType) lhsType, (ParameterizedType) rhsType);
            }
        }

        if (lhsType instanceof GenericArrayType) {
            Type lhsComponent = ((GenericArrayType) lhsType).getGenericComponentType();

            if (rhsType instanceof Class) {
                Class<?> rhsClass = (Class<?>) rhsType;

                if (rhsClass.isArray()) {
                    return isAssignable(lhsComponent, rhsClass.getComponentType());
                }
            }
            else if (rhsType instanceof GenericArrayType) {
                Type rhsComponent = ((GenericArrayType) rhsType).getGenericComponentType();

                return isAssignable(lhsComponent, rhsComponent);
            }
        }

        if (lhsType instanceof WildcardType) {
            return isAssignable((WildcardType) lhsType, rhsType);
        }

        return false;
    }

    private static boolean isAssignable(ParameterizedType lhsType, ParameterizedType rhsType) {
        if (lhsType.equals(rhsType)) {
            return true;
        }

        Type[] lhsTypeArguments = lhsType.getActualTypeArguments();
        Type[] rhsTypeArguments = rhsType.getActualTypeArguments();

        if (lhsTypeArguments.length != rhsTypeArguments.length) {
            return false;
        }

        for (int size = lhsTypeArguments.length, i = 0; i < size; ++i) {
            Type lhsArg = lhsTypeArguments[i];
            Type rhsArg = rhsTypeArguments[i];

            if (!lhsArg.equals(rhsArg) &&
                    !(lhsArg instanceof WildcardType && isAssignable((WildcardType) lhsArg, rhsArg))) {
                return false;
            }
        }

        return true;
    }

    private static boolean isAssignable(WildcardType lhsType, Type rhsType) {
        Type[] lUpperBounds = lhsType.getUpperBounds();

        // supply the implicit upper bound if none are specified
        if (lUpperBounds.length == 0) {
            lUpperBounds = new Type[] { Object.class };
        }

        Type[] lLowerBounds = lhsType.getLowerBounds();

        // supply the implicit lower bound if none are specified
        if (lLowerBounds.length == 0) {
            lLowerBounds = new Type[] { null };
        }

        if (rhsType instanceof WildcardType) {
            // both the upper and lower bounds of the right-hand side must be
            // completely enclosed in the upper and lower bounds of the left-
            // hand side.
            WildcardType rhsWcType = (WildcardType) rhsType;
            Type[] rUpperBounds = rhsWcType.getUpperBounds();

            if (rUpperBounds.length == 0) {
                rUpperBounds = new Type[] { Object.class };
            }

            Type[] rLowerBounds = rhsWcType.getLowerBounds();

            if (rLowerBounds.length == 0) {
                rLowerBounds = new Type[] { null };
            }

            for (Type lBound : lUpperBounds) {
                for (Type rBound : rUpperBounds) {
                    if (!isAssignableBound(lBound, rBound)) {
                        return false;
                    }
                }

                for (Type rBound : rLowerBounds) {
                    if (!isAssignableBound(lBound, rBound)) {
                        return false;
                    }
                }
            }

            for (Type lBound : lLowerBounds) {
                for (Type rBound : rUpperBounds) {
                    if (!isAssignableBound(rBound, lBound)) {
                        return false;
                    }
                }

                for (Type rBound : rLowerBounds) {
                    if (!isAssignableBound(rBound, lBound)) {
                        return false;
                    }
                }
            }
        }
        else {
            for (Type lBound : lUpperBounds) {
                if (!isAssignableBound(lBound, rhsType)) {
                    return false;
                }
            }

            for (Type lBound : lLowerBounds) {
                if (!isAssignableBound(rhsType, lBound)) {
                    return false;
                }
            }
        }

        return true;
    }
    
    public static boolean isAssignableBound(Type lhsType, Type rhsType) {
        if (rhsType == null) {
            return true;
        }
        if (lhsType == null) {
            return false;
        }
        return isAssignable(lhsType, rhsType);
    }
    
    /**
     * Map with primitive wrapper type as key and corresponding primitive
     * type as value, for example: Integer.class -> int.class.
     */
    private static final Map<Class<?>, Class<?>> primitiveWrapperTypeMap = new IdentityHashMap<>(8);

    /**
     * Map with primitive type as key and corresponding wrapper
     * type as value, for example: int.class -> Integer.class.
     */
    private static final Map<Class<?>, Class<?>> primitiveTypeToWrapperMap = new IdentityHashMap<>(8);
    
    static {
        primitiveWrapperTypeMap.put(Boolean.class, boolean.class);
        primitiveWrapperTypeMap.put(Byte.class, byte.class);
        primitiveWrapperTypeMap.put(Character.class, char.class);
        primitiveWrapperTypeMap.put(Double.class, double.class);
        primitiveWrapperTypeMap.put(Float.class, float.class);
        primitiveWrapperTypeMap.put(Integer.class, int.class);
        primitiveWrapperTypeMap.put(Long.class, long.class);
        primitiveWrapperTypeMap.put(Short.class, short.class);
        primitiveWrapperTypeMap.put(Void.class, void.class);

        // Map entry iteration is less expensive to initialize than forEach with lambdas
        for (Map.Entry<Class<?>, Class<?>> entry : primitiveWrapperTypeMap.entrySet()) {
            primitiveTypeToWrapperMap.put(entry.getValue(), entry.getKey());
        }
    }
    
    public static boolean isAssignable(Class<?> lhsType, Class<?> rhsType) {
        if (lhsType.isAssignableFrom(rhsType)) {
            return true;
        }
        if (lhsType.isPrimitive()) {
            Class<?> resolvedPrimitive = primitiveWrapperTypeMap.get(rhsType);
            if (lhsType == resolvedPrimitive) {
                return true;
            }
        }
        else {
            Class<?> resolvedWrapper = primitiveTypeToWrapperMap.get(rhsType);
            if (resolvedWrapper != null && lhsType.isAssignableFrom(resolvedWrapper)) {
                return true;
            }
        }
        return false;
    }

}
