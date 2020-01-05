package com.fengwk.codehelper.gen;

import java.beans.IntrospectionException;
import java.util.Arrays;

import com.fengwk.codehelper.util.BeanUtils;
import com.fengwk.codehelper.util.BeanUtils.MethodDesc;
import com.fengwk.codehelper.util.BeanUtils.PropertyDesc;
import com.fengwk.codehelper.util.TypeUtils;

/**
 * 
 * @author fengwk
 * @version V1.0
 * @since 2019-01-15 10:39
 */
public class ConvertMethodGenerator {

    final Class<?> from;
    final String fromName;
    final Class<?> to;
    final String toName;
    
    public ConvertMethodGenerator(Class<?> from, String fromName, Class<?> to, String toName) {
        this.from = from;
        this.fromName = fromName;
        this.to = to;
        this.toName = toName;
    }
    
    public String generate() throws IntrospectionException {
        StringBuilder sb = new StringBuilder();
        sb.append("        if (").append(fromName).append(" == null) {\n");
        sb.append("            return null;\n");
        sb.append("        }\n");
        sb.append("        ").append(to.getSimpleName()).append(' ').append(toName).append(" = new ").append(to.getSimpleName()).append("();\n");
        PropertyDesc[] toPds = BeanUtils.getPropertyDescriptors(to);
        Arrays.sort(toPds, (x1, x2) -> x1.getName().compareTo(x2.getName()));
        for (PropertyDesc toPd : toPds) {
            MethodDesc targetWriteMethod = toPd.getWriteMethod();
            if (targetWriteMethod != null) {
                PropertyDesc fromPd = BeanUtils.getPropertyDescriptor(from, toPd.getName());
                if (fromPd != null && fromPd.getReadMethod() != null && TypeUtils.isAssignable(targetWriteMethod.getGenericParameterTypes()[0], fromPd.getReadMethod().getGenericReturnType())) {
                    sb.append("        ").append(toName).append('.').append(toPd.getWriteMethod().getName()).append('(').append(fromName).append('.').append(fromPd.getReadMethod().getName()).append("());\n");
                } else {
                    sb.append("        ").append(toName).append('.').append(toPd.getWriteMethod().getName()).append("(null);// TODO not auto convert\n");
                }
            }
        }
        sb.append("        return ").append(toName).append(';');
        return sb.toString();
    }
    
}
