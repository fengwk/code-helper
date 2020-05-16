package com.fengwk.codehelper.gen;

import java.beans.IntrospectionException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fengwk.codehelper.util.BeanUtils;
import com.fengwk.codehelper.util.BeanUtils.PropertyDesc;

/**
 * 
 * @author fengwk
 */
public class Pojo {
    
    final Class<?> cls;
    final String clsPath;
    final String clsName;
    
    String allSource;// java源码（包括父类）

    public Pojo(Class<?> cls) {
        this.cls = Objects.requireNonNull(cls);
        clsPath = cls.getName();
        clsName = cls.getSimpleName();
        tryReadAllSource();
    }

    public PropertyDesc[] propertyDescriptors() throws IntrospectionException {
        PropertyDesc[] pds = BeanUtils.getPropertyDescriptors(cls);
        Arrays.sort(pds, hasSource() ? this::compareToPds : this::defaultCompareToPds);
        return pds;
    }
    
    public PropertyDesc propertyDescriptor(String propertyName) {
        return BeanUtils.getPropertyDescriptor(cls, propertyName);
    }
    
    public String clsName() {
        return clsName;
    }

    public boolean hasSource() {
        return allSource != null && !allSource.isEmpty();
    }
    
    void tryReadAllSource() {
        StringBuilder allSource = new StringBuilder();
        tryReadAllSource(cls, allSource);
        this.allSource = allSource.toString();
    }
    
    public static void main(String[] args) {
        System.out.println(Pojo.class.getSimpleName());
    }
    
    void tryReadAllSource(Class<?> cls, StringBuilder allSource) {
        Class<?> superCls = cls.getSuperclass();
        if (superCls != null) {
            tryReadAllSource(superCls, allSource);
        }
        
        String source = tryReadSource(cls);
        if (source != null) {
            String regex = "class[\\s]+" + cls.getSimpleName() + "[\\s]*\\{";
            Matcher m = Pattern.compile(regex).matcher(source);
            if (m.find()) {
                source = source.substring(m.start());
                int n = 1;// counter
                int i = 0;
                for (; n > 0 && i < source.length(); i++) {
                    if (source.charAt(i) == '{') { 
                        n++;
                    } else if (source.charAt(i) == '}') {
                        n--;
                    }
                }
                source = source.substring(0, i);
                allSource.append(source);
            }
        }
    }
    
    String tryReadSource(Class<?> cls) {
        cls = getTopCls(cls);
        URL url = cls.getResource(cls.getSimpleName() + ".class");
        if (url == null) {
            return null;
        }
        File f = new File(url.getFile().replace("target/classes", "src/main/java").replace(".class", ".java"));
        if (!f.exists()) {
            f = new File(url.getFile().replace("target/test-classes", "src/test/java").replace(".class", ".java"));
        }
        if (f.exists()) {
            try {
                return readFileToString(f);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    
    Class<?> getTopCls(Class<?> cls) {
        Class<?> dcls = cls.getDeclaringClass();
        return dcls == null ? cls : getTopCls(dcls);
    }
    
    String readFileToString(File f) throws IOException {
        try (FileReader reader = new FileReader(f)) {
            StringBuilder builder = new StringBuilder();
            char[] cs = new char[1024];
            int len;
            while ((len = reader.read(cs)) != -1) {
                builder.append(new String(cs, 0, len));
            }
            return builder.toString();
        }
    }
    
    int defaultCompareToPds(PropertyDesc pd1, PropertyDesc pd2) {
        return pd1.getName().compareTo(pd2.getName());
    }
    
    int compareToPds(PropertyDesc pd1, PropertyDesc pd2) {
    	Matcher m1 = Pattern.compile(pd1.getName() + "( )*;").matcher(allSource);
    	Matcher m2 = Pattern.compile(pd2.getName() + "( )*;").matcher(allSource);
    	Integer pos1 = Integer.MAX_VALUE, pos2 = Integer.MAX_VALUE;
    	if (m1.find()) {
    		pos1 = m1.start();
    	}
    	if (m2.find()) {
    		pos2 = m2.start();
    	}
    	
    	int firstComp = pos1.compareTo(pos2);
    	return firstComp == 0 ? pd1.getName().compareTo(pd2.getName()) : firstComp;
    }
    
    String lowerFirst(String str) {
        if(str != null && str.length() > 0) {
            str = Character.toLowerCase(str.charAt(0)) + str.substring(1);
        }
        return str;
    }
    
}
