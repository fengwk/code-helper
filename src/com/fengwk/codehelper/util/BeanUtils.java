package com.fengwk.codehelper.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

/**
 * 
 * @author fengwk
 */
public class BeanUtils {
    
    private BeanUtils() {}
    
    public static PropertyDescriptor getPropertyDescriptor(Class<?> beanClass, String propertyName) {
        try {
            return new PropertyDescriptor(propertyName, beanClass);
        } catch (IntrospectionException e) {
            return null;
        }
    }
    
    public static PropertyDescriptor[] getPropertyDescriptors(Class<?> beanClass) throws IntrospectionException {
        BeanInfo beanInfo = Introspector.getBeanInfo(beanClass);
        return beanInfo.getPropertyDescriptors();
    }

}
