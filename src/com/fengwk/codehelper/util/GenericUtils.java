package com.fengwk.codehelper.util;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author fengwk
 */
public class GenericUtils {

    private GenericUtils() {}
    
    /**
     * 查找最接近srcClass的TypeVariable类型
     * 
     * @param <C> S的子孙
     * @param <S>
     * @param srcClass 从该类开始查找
     * @param targetClass 到该类结束
     * @param targetVar 目标TypeVariable应当与targetClass匹配
     * @return
     */
    public static <C extends S, S> Type findNearestVarType(Class<C> srcClass, Class<S> targetClass, TypeVariable<?> targetVar) {
        if (srcClass == null) {
            throw new NullPointerException("SrcClass cannot be null.");
        }
        if (targetClass == null) {
            throw new NullPointerException("TargetClass cannot be null.");
        }
        if (targetVar == null) {
            throw new NullPointerException("TargetVar cannot be null.");
        }
        TypeVariable<?>[] vars = targetClass.getTypeParameters();
        for (int i = 0; i < vars.length; i++) {
            if (vars[i].equals(targetVar)) {
                return doFindNearestVarType(srcClass, targetClass, i).type;
            }
        }
        return targetVar; 
    }
    
    /**
     * 查找最接近srcClass的TypeVariable类型
     * 
     * @param <C> S的子孙
     * @param <S>
     * @param srcClass 从该类开始查找
     * @param targetClass 到该类结束
     * @param varIndex 目标TypeVariable在targetClass的索引,从0开始
     * @return
     */
    public static <C extends S, S> Type findNearestVarType(Class<C> srcClass, Class<S> targetClass, int varIndex) {
        if (srcClass == null) {
            throw new NullPointerException("SrcClass cannot be null.");
        }
        if (targetClass == null) {
            throw new NullPointerException("TargetClass cannot be null.");
        }
        if (varIndex < 0) {
            throw new IllegalArgumentException("VarIndex cannot less than zero.");
        }
        if (varIndex >= targetClass.getTypeParameters().length) {
            throw new IllegalArgumentException("VarIndex must be less than targetClass's varLen.");
        }
        return doFindNearestVarType(srcClass, targetClass, varIndex).type;
    }
    
    private static <C extends S, S> TypeWrapper doFindNearestVarType(Class<?> clazz, Class<S> targetClass, int varIndex) {
        if (clazz == targetClass) {
            return new TypeWrapper(clazz.getTypeParameters()[varIndex]);
        }
        
        TypeWrapper ret = doFindNearestVarType0(clazz, targetClass, varIndex);
        if (ret == null) {
            throw new IllegalStateException("Cannot found " + targetClass + " from " + clazz + ".");
        }
        
        return ret;
    }
    
    private static <C extends S, S> TypeWrapper doFindNearestVarType0(Class<?> clazz, Class<S> targetClass, int varIndex) {
        // 从当前类或接口的父接口中尝试能否查找到targetClass
        for (Type supItfType : clazz.getGenericInterfaces()) {
            if (supItfType instanceof ParameterizedType) {
                ParameterizedType supItfPt = (ParameterizedType) supItfType;
                Class<?> supIftClass = null;
                if ((supIftClass = (Class<?>) supItfPt.getRawType()) == targetClass) {
                    return findMatchTypeWrapper(clazz, supItfPt, varIndex);
                }
                
                // 递归父接口
                TypeWrapper ret = doFindNearestVarType0(supIftClass, targetClass, varIndex);
                if (ret == null) {
                    continue;
                }
                
                // 更新父类返回的类型包装
                return updateTypeWrapper(clazz, supItfPt, ret);
            } else if (supItfType instanceof Class) {
                TypeWrapper ret = doFindNearestVarType0((Class<?>) supItfType, targetClass, varIndex);
                if (ret != null) {
                    return ret;
                }
            }
        }
        
        Type supType = clazz.getGenericSuperclass();
        if (supType == null) {// clazz为Object或者clazz为接口的情况
            return null;
        }
        
        // 从当前类的父接口中尝试能否查找到targetClass
        if (supType instanceof ParameterizedType) {
            ParameterizedType supPt = (ParameterizedType) supType;
            if (supPt.getRawType() == targetClass) {
                return findMatchTypeWrapper(clazz, supPt, varIndex);
            }
            
            // 递归父类
            TypeWrapper ret = doFindNearestVarType0(clazz.getSuperclass(), targetClass, varIndex);
            if (ret == null) {
                return null;
            }
            return updateTypeWrapper(clazz, supPt, ret);
        } else if (supType instanceof Class) {
            TypeWrapper ret = doFindNearestVarType0((Class<?>) supType, targetClass, varIndex);
            if (ret != null) {
                return ret;
            }
        }
        
        return null;
    }
    
    private static TypeWrapper findMatchTypeWrapper(Class<?> clazz, ParameterizedType pt, int varIndex) {
        Type[] atas = pt.getActualTypeArguments();
        Type ata = atas[varIndex];
        TypeWrapper typeWrapper = new TypeWrapper(ata);
        typeWrapper.varWrappers = collectVarWrappers(clazz, ata);
        return typeWrapper;
    }
    
    private static int findVarIdx(TypeVariable<?>[] vars, TypeVariable<?> useToFind) {
        for (int i = 0; i < vars.length; i++) {
            if (vars[i].equals(useToFind)) {
                return i;
            }
        }
        throw new IllegalStateException("Cannot match TypeVariable[" + useToFind + "] from " + Arrays.toString(vars) + ".");
    }
    
    private static TypeWrapper updateTypeWrapper(Class<?> clazz, ParameterizedType pt, TypeWrapper ret) {
        if (ret.varWrappers == null || ret.varWrappers.isEmpty()) {
            return ret;
        }
        
        Type[] atas = pt.getActualTypeArguments();
        Map<Type, Type> tMap = new HashMap<Type, Type>();
        for (VarWrapper varWrapper : ret.varWrappers) {
            tMap.put(varWrapper.var, atas[varWrapper.i]);
        }
        ret.type = mapType(ret.type, tMap);
        ret.varWrappers = collectVarWrappers(clazz, ret.type);
        return ret;
    }
    
    private static Type mapType(Type type, Map<Type, Type> tMap) {
        if (type instanceof TypeVariable) {
            Type mType = tMap.get(type);
            return mType == null ? type : mType;
        }
        if (type instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) type;
            Type[] atas = pt.getActualTypeArguments();
            if (atas.length == 0) {
                return type;
            }
            Type[] nAtas = atas.clone();
            for (int i = 0; i < nAtas.length; i++) {
                Type mType = mapType(nAtas[i], tMap);
                if (!nAtas[i].equals(mType)) {
                    nAtas[i] = mType;
                }
            }
            return new ParameterizedTypeImpl(nAtas, pt.getOwnerType(), pt.getRawType());
        }
        if (type instanceof GenericArrayType) {
            GenericArrayType gat = (GenericArrayType) type;
            Type ct = gat.getGenericComponentType();
            Type mType = mapType(ct, tMap);
            if (ct.equals(mType)) {
                return type;
            } else {
                return new GenericArrayTypeImpl(mType);
            }
        }
        return type;
    }
    
    private static Set<VarWrapper> collectVarWrappers(Class<?> clazz, Type type) {
        Set<TypeVariable<?>> vars = new HashSet<>();
        collectVars(type, vars);
        if (vars.isEmpty()) {
            return null;
        }
        
        Set<VarWrapper> varWrappers = new HashSet<>();
        TypeVariable<?>[] classVars = clazz.getTypeParameters();
        for (TypeVariable<?> var : vars) {
            varWrappers.add(new VarWrapper(var, findVarIdx(classVars, var)));
        }
        return varWrappers;
    }
    
    private static void collectVars(Type type, Set<TypeVariable<?>> vars) {
        if (type instanceof TypeVariable) {
            vars.add((TypeVariable<?>) type);
        }
        if (type instanceof ParameterizedType) {
            for (Type ata : ((ParameterizedType) type).getActualTypeArguments()) {
                collectVars(ata, vars);
            }
        }
        if (type instanceof GenericArrayType) {
            collectVars(((GenericArrayType) type).getGenericComponentType(), vars);
        }
    }
    
    static class VarWrapper {
        
        TypeVariable<?> var;
        int i;
        
        VarWrapper(TypeVariable<?> var, int i) {
            this.var = var;
            this.i = i;
        }
        
    }
    
    static class TypeWrapper {
        
        Type type;
        Set<VarWrapper> varWrappers;
        
        TypeWrapper(Type type) {
            this.type = type;
        }
        
    }
    
}
