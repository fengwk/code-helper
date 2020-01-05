package com.fengwk.codehelper.gen;

import java.beans.IntrospectionException;
import java.util.List;
import org.eclipse.jdt.core.JavaModelException;
import com.fengwk.codehelper.util.JavaClassParser.MethodMateData;
import com.fengwk.codehelper.util.JavaClassParser.Param;

/**
 * 
 * @author fengwk
 */
public class GenConvertMethodHandler extends GenMethodTemplate {

    @Override
    protected boolean canGen(MethodMateData methodMateData) {
        return methodMateData.getReturnType() != Void.class && !methodMateData.getParamTypes().isEmpty();
    }
    
    @Override
    protected String genMethodContent(MethodMateData methodMateData) throws JavaModelException, IntrospectionException {
        List<Param> params = methodMateData.getParamTypes();
        Class<?> fromType = params.get(0).getClazz();
        String fromName = params.get(0).getName();
        Class<?> toType = methodMateData.getReturnType();
        String toName = getToName(toType.getSimpleName(), fromName);
        ConvertMethodGenerator gen = new ConvertMethodGenerator(fromType, fromName, toType, toName);
        return gen.generate();
    }
    
    static String getToName(String toName, String fromName) {
        if (toName.equalsIgnoreCase(fromName)) {
            toName = "to" + toName;
        } else {
            toName = regularize(toName);
        }
        return toName;
    }
    
    static String regularize(String name) {
        char[] cs = name.toCharArray();
        int i = 0;
        for (; i < cs.length; i++) {
            if (Character.isLowerCase(cs[i])) {
                break;
            }
        }
        if (i == 0) {
            return name;
        } else if (i == 1) {
            return Character.toLowerCase(name.charAt(0)) + name.substring(1);
        } else if (i == cs.length) {
            return name.toLowerCase();
        } else {
            return name.substring(0, i - 1).toLowerCase() + name.substring(i - 1);
        }
    }

}
