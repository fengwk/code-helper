package com.fengwk.codehelper.gen;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Arrays;

import com.fengwk.codehelper.util.BeanUtils;
import com.fengwk.codehelper.util.TypeUtils;

/**
 * 
 * @author fengwk
 * @version V1.0
 * @since 2019-01-15 10:39
 */
public class CoverMethodGeneratorIfToIsNull {

    final Class<?> from;
    final String fromName;
    final Class<?> to;
    final String toName;
    
    public CoverMethodGeneratorIfToIsNull(Class<?> from, String fromName, Class<?> to, String toName) {
        this.from = from;
        this.fromName = fromName;
        this.to = to;
        this.toName = toName;
    }
    
    public String generate() throws IntrospectionException {
        StringBuilder sb = new StringBuilder();
        sb.append("        if (").append(fromName).append(" == null || ").append(toName).append(" == null) {\n");
        sb.append("            return;\n");
        sb.append("        }\n");
        PropertyDescriptor[] toPds = BeanUtils.getPropertyDescriptors(to);
        Arrays.sort(toPds, (x1, x2) -> x1.getName().compareTo(x2.getName()));
        for (int i = 0; i < toPds.length; i++) {
            PropertyDescriptor toPd = toPds[i];
            Method targetReadMethod = toPd.getReadMethod();
            Method targetWriteMethod = toPd.getWriteMethod();
            if (targetReadMethod != null && targetWriteMethod != null) {
                PropertyDescriptor fromPd = BeanUtils.getPropertyDescriptor(from, toPd.getName());
                sb.append("        if (").append(toName).append('.').append(toPd.getReadMethod().getName()).append("() == null) {\n");
                if (fromPd != null && fromPd.getReadMethod() != null && TypeUtils.isAssignable(targetWriteMethod.getGenericParameterTypes()[0], fromPd.getReadMethod().getGenericReturnType())) {
                    sb.append("            ").append(toName).append('.').append(toPd.getWriteMethod().getName()).append('(').append(fromName).append('.').append(fromPd.getReadMethod().getName()).append("());\n");
                } else {
                    sb.append("            ").append(toName).append('.').append(toPd.getWriteMethod().getName()).append("(null);// TODO not auto cover\n");
                }
                sb.append("        }");
                if (i < toPds.length - 1) {
                    sb.append('\n');
                }
            }
        }
        return sb.toString();
    }
    
}
