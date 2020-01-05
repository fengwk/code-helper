package com.fengwk.codehelper.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/**
 * 
 * @author fengwk
 */
public class ProjectClassLoaderFactory {
    
    static volatile Map<String, ProjectClassLoader> cache = new ConcurrentHashMap<>();
    
    private ProjectClassLoaderFactory() {}
    
    public static synchronized ClassLoader get(IJavaProject pro) throws JavaModelException, IOException {
        Collector collector = collect(pro);
        String proId = getProPath(pro);
        ProjectClassLoader cl = cache.get(proId);
        if (cl == null) {
            ProjectClassLoader jarCl = new ProjectClassLoader(null);
            jarCl.addUrls(collector.jarCps);
            cl = new ProjectClassLoader(jarCl);
            cl.addUrls(collector.proCps);
            cache.put(proId, cl);
            return cl;
        } else {
            ProjectClassLoader jarCl = (ProjectClassLoader) cl.getParent();
            
            for (URL oldUrl : jarCl.set) {
                if (!collector.jarCps.contains(oldUrl)) {
                    jarCl.close();
                    cl.close();
                    jarCl = new ProjectClassLoader(null);
                    jarCl.addUrls(collector.jarCps);
                    cl = new ProjectClassLoader(jarCl);
                    cl.addUrls(collector.proCps);
                    cache.put(proId, cl);
                    return cl;
                }
            }
            
            jarCl.addUrls(collector.jarCps);
            cl.close();
            cl = new ProjectClassLoader(jarCl);
            cl.addUrls(collector.proCps);
            cache.put(proId, cl);
            return cl;
        }
    }
    
    static String getProPath(IJavaProject pro) {
        return pro.getPath().toString();
    }
    
    static Collector collect(IJavaProject pro) throws JavaModelException, MalformedURLException  {
        Set<URL> proCps = new HashSet<>();
        Set<URL> jarCps = new HashSet<>();
        Set<String> cycle = new HashSet<>();
        collect(pro, proCps, jarCps, cycle);
        return new Collector(proCps, jarCps);
    }
    
    static void collect(IJavaProject pro, Set<URL> proCps, Set<URL> jarCps, Set<String> cycle) throws JavaModelException, MalformedURLException  {
        if (pro == null) {
            return;
        }
        String proId = ProjectClassLoaderFactory.getProPath(pro);
        if (cycle.contains(proId)) {
            return;
        } else {
            cycle.add(proId);
        }
        
        IClasspathEntry[] resolvedClass = pro.getResolvedClasspath(true);
        for (IClasspathEntry iClasspathEntry : resolvedClass) {
            if (iClasspathEntry.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
                collect((IJavaProject) JavaCore.create("=" + iClasspathEntry.getPath().toString().substring(1)), proCps, jarCps, cycle);
            } else if (iClasspathEntry.getOutputLocation() != null) {
                String path = pro.getProject().getLocation().toOSString() + iClasspathEntry.getOutputLocation().toOSString().substring(iClasspathEntry.getOutputLocation().toOSString().indexOf('\\', 1));
                proCps.add(new File(path).toURI().toURL());
            } else {
                String path = iClasspathEntry.getPath().toOSString();
                jarCps.add(new File(path).toURI().toURL());
            }
        }
        String path = pro.getProject().getLocation().toOSString() + pro.getOutputLocation().toOSString().substring(pro.getOutputLocation().toOSString().indexOf('\\', 1));
        proCps.add(new File(path).toURI().toURL());
    }
    
    static class Collector {
        
        final Set<URL> proCps;
        final Set<URL> jarCps;
        
        public Collector(Set<URL> proCps, Set<URL> jarCps) {
            this.proCps = proCps;
            this.jarCps = jarCps;
        }
    }
    
}
