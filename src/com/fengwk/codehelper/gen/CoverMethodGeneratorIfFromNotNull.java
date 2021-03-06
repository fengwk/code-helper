package com.fengwk.codehelper.gen;

import java.beans.IntrospectionException;
import com.fengwk.codehelper.util.BeanUtils.MethodDesc;
import com.fengwk.codehelper.util.BeanUtils.PropertyDesc;
import com.fengwk.codehelper.util.TypeUtils;

/**
 * 
 * @author fengwk
 * @version V1.0
 * @since 2019-01-15 10:39
 */
public class CoverMethodGeneratorIfFromNotNull {

    final Pojo from;
    final String fromName;
    final Pojo to;
    final String toName;
    
    public CoverMethodGeneratorIfFromNotNull(Pojo from, String fromName, Pojo to, String toName) {
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
        PropertyDesc[] toPds = to.propertyDescriptors();
        for (int i = 0; i < toPds.length; i++) {
            PropertyDesc toPd = toPds[i];
            MethodDesc targetReadMethod = toPd.getReadMethod();
            MethodDesc targetWriteMethod = toPd.getWriteMethod();
            if (targetReadMethod != null && targetWriteMethod != null) {
                PropertyDesc fromPd = from.propertyDescriptor(toPd.getName());
                sb.append("        if (").append(fromName).append('.').append(fromPd.getReadMethod().getName()).append("() != null) {\n");
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
