package com.fengwk.codehelper.gen;

import java.util.List;

import com.fengwk.codehelper.util.JavaClassParser.MethodMateData;
import com.fengwk.codehelper.util.JavaClassParser.Param;

/**
 * 
 * @author fengwk
 */
public class GenCoverMethodHandlerIfFromNotNull extends GenMethodTemplate {

    @Override
    protected boolean canGen(MethodMateData methodMateData) {
        return methodMateData.getParamTypes().size() >= 2;
    }

    @Override
    protected String genMethodContent(MethodMateData methodMateData) throws Exception {
        List<Param> params = methodMateData.getParamTypes();
        Class<?> fromType = params.get(0).getClazz();
        String fromName = params.get(0).getName();
        Class<?> toType = params.get(1).getClazz();
        String toName =  GenConvertMethodHandler.getToName(params.get(1).getName(), fromName);
        CoverMethodGeneratorIfFromNotNull gen = new CoverMethodGeneratorIfFromNotNull(new Pojo(fromType), fromName, new Pojo(toType), toName);
        return gen.generate();
    }
    
}
