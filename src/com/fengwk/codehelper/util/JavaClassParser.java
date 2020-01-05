package com.fengwk.codehelper.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

/**
 * 
 * @author fengwk
 */
public class JavaClassParser {
    
    final ClassLoader cl;
    
    final ICompilationUnit javaClass;
    final List<String> pkgs;
    final List<List<String>> imports;
    
    public JavaClassParser(ClassLoader cl, ICompilationUnit javaClass) throws JavaModelException {
        this.cl = cl;
        this.javaClass = javaClass;
        
        IPackageDeclaration[] pds = javaClass.getPackageDeclarations();
        if (pds.length == 0) {
            pkgs = Collections.emptyList();
        } else {
            pkgs = splitPkg(pds[0].getElementName());
        }
        
        imports = new ArrayList<>();
        IImportDeclaration[] idecs = javaClass.getImports();
        for (IImportDeclaration idec : idecs) {
            imports.add(splitPkg(idec.getElementName()));
        }
    }
    
    private List<String> splitPkg(String pkgName) {
        return Arrays.asList(pkgName.trim().split("\\."));
    }
    
    public MethodMateData findMethod(String methodName, String paramsStr) throws JavaModelException {
        for (IType type : javaClass.getAllTypes()) {
            loop2: for (IMethod method : type.getMethods()) {
                if (methodName.trim().equals(method.getElementName()) && method.getNumberOfParameters() > 0) {
                    String[] paramNames = method.getParameterNames();
                    for (String paramName : paramNames) {
                        if (!paramsStr.contains(paramName)) {
                            continue loop2;
                        }
                    }
                    String[] paramTypes = method.getParameterTypes();
                    List<List<String>> paramTypeNames = new ArrayList<>();
                    for (String paramType : paramTypes) {
                        List<String> paramTypeName = parseTypeName(paramType);
                        for (String item : paramTypeName) {
                            if (!paramsStr.contains(item)) {
                                continue loop2;
                            }
                        }
                        paramTypeNames.add(paramTypeName);
                    }
                    
                    List<Param> paramTypes0 = findParams(paramTypeNames, paramNames);
                    String returnType = method.getReturnType();
                    Class<?> returnType0 = returnType.equals("V") ? Void.class : findClass(parseTypeName(returnType));
                    if (paramTypes0 == null || returnType0 == null) {
                        return null;
                    }
                    return new MethodMateData(method.getElementName(), paramTypes0, returnType0);
                }
            }
        }
        
        return null;
    }
    
    private List<Param> findParams(List<List<String>> typeNames, String[] paramNames) {
        List<Param> params = new ArrayList<>();
        for (int i = 0; i < typeNames.size(); i++) {
            Class<?> clazz = findClass(typeNames.get(i));
            if (clazz == null) {
                return Collections.emptyList();
            }
            params.add(new Param(paramNames[i], clazz));
        }
        return params;
    }
    
    private Class<?> findClass(List<String> typeName) {
        Class<?> clazz = null;
        for (List<String> imporPkgs : imports) {
            if (imporPkgs.size() > 0 && typeName.get(0).equals(imporPkgs.get((imporPkgs.size() - 1)))) {
                clazz = testAndGetClass(imporPkgs.subList(0, imporPkgs.size() - 1), typeName);
                if (clazz != null) {
                    return clazz;
                }
            }
        }
        
        clazz = testAndGetClass(pkgs, typeName);
        if (clazz != null) {
            return clazz;
        }
        
        for (List<String> imporPkgs : imports) {
            if (imporPkgs.size() > 0 && "*".equals(imporPkgs.get((imporPkgs.size() - 1)))) {
                clazz = testAndGetClass(imporPkgs.subList(0, imporPkgs.size() - 1), typeName);
                if (clazz != null) {
                    return clazz;
                }
            }
        }
        
        clazz = testAndGetClass(Collections.emptyList(), typeName);
        if (clazz != null) {
            return clazz;
        }
        
        return null;
    }
    
    /*
        QMap.Entry<QString;QString;>;
        QHua;
        Qcom.fengwk.pro1.p.Hua;
     */
    private static List<String> parseTypeName(String typeName) {
        if (typeName == null || typeName.isEmpty()) {
            return null;
        }
        String[] parts = typeName.split("[<|>|;]");
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            for (int i = 0; i < part.length(); i++) {
                if (part.charAt(i) == 'Q') {
                    return Arrays.asList(part.substring(i + 1).split("\\."));
                }
            }
        }
        
        throw new IllegalStateException("Cannot parse TypeName[" + typeName + "].");
    }
    
    private Class<?> testAndGetClass(List<String> pkgs, List<String> simpleNames) {
        List<String> allPath = new ArrayList<>(pkgs);
        allPath.addAll(simpleNames);
        return testAndGetClass(allPath);
    }
    
    private Class<?> testAndGetClass(List<String> clsItems) {
        for (int i = 0; i < clsItems.size(); i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < clsItems.size() - i; j++) {
                if (j > 0) {
                    sb.append(".");
                }
                sb.append(clsItems.get(j));
            }
            for (int k = clsItems.size() - i; k < clsItems.size(); k++) {
                if (k > 0) {
                    sb.append("$");
                }
                sb.append(clsItems.get(k));
            }
            try {
                Class<?> clazz = Class.forName(sb.toString(), true, cl);
                if (clazz != null) {
                    return clazz;
                }
            } catch (ClassNotFoundException e) {
                // nothing to do
            }
        }
        return null;
    }
    
    public static class MethodMateData {
        
        final String methodName;
        final List<Param> paramTypes;
        final Class<?> returnType;
        
        MethodMateData(String methodName, List<Param> paramTypes, Class<?> returnType) {
            this.methodName = methodName;
            this.paramTypes = paramTypes;
            this.returnType = returnType;
        }

        public String getMethodName() {
            return methodName;
        }

        public List<Param> getParamTypes() {
            return paramTypes;
        }

        public Class<?> getReturnType() {
            return returnType;
        }
        
    }
    
    public static class Param {
        
        String name;
        Class<?> clazz;
        
        Param(String name, Class<?> clazz) {
            super();
            this.name = name;
            this.clazz = clazz;
        }

        public String getName() {
            return name;
        }

        public Class<?> getClazz() {
            return clazz;
        }
        
    }
    
}
