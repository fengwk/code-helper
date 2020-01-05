package com.fengwk.codehelper.util;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * @author fengwk
 */
class ProjectClassLoader extends URLClassLoader {

    volatile Set<URL> set = Collections.newSetFromMap(new ConcurrentHashMap<>());
    
    ProjectClassLoader(ClassLoader parent) {
        super(new URL[0], parent);
    }
    
    void addUrls(Collection<URL> urls) {
        for (URL url : urls) {
            if (!set.contains(url)) {
                addURL(url);
                set.add(url);
            }
        }
    }
    
}
