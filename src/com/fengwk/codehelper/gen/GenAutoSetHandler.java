package com.fengwk.codehelper.gen;

import java.beans.IntrospectionException;
import java.io.IOException;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.text.edits.InsertEdit;

import com.fengwk.codehelper.util.ProjectClassLoaderFactory;

/**
 * 
 * @author fengwk
 */
public class GenAutoSetHandler {

    @Execute
    public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell s) {
        ICompilationUnit cu = GenMethodTemplate.getICompilationUnit();
        ITextSelection ts = GenMethodTemplate.getITextSelection();
        new TypeSelectorAction(iType -> {
            try {
                ClassLoader cl = ProjectClassLoaderFactory.get(cu.getJavaProject());
                Class<?> clazz = cl.loadClass(iType.getFullyQualifiedName());
                int offset = ts.getOffset();
                String source = cu.getSource();
                int i = offset;
                boolean r = false;
                for (; i > 0; i--) {
                    char c = source.charAt(i);
                    if (c == '\n') {
                        break;
                    }
                    if (c == '{') {
                        r = true;
                        break;
                    }
                }
                i++;
                cu.applyTextEdit(new InsertEdit(i, (r ? "\n" : "") + new AutoSetGenerator(new Pojo(clazz)).gen()), null);
            } catch (JavaModelException | IntrospectionException | IOException | ClassNotFoundException e) {
                MessageDialog.openError(s, "Code Helper", "Error: " + e.getMessage() + ".");
            }
        }, s).run();
    }
    
}
