package com.fengwk.codehelper.gen;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.text.edits.InsertEdit;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;

import com.fengwk.codehelper.util.JavaClassParser;
import com.fengwk.codehelper.util.JavaClassParser.MethodMateData;
import com.fengwk.codehelper.util.ProjectClassLoaderFactory;

/**
 * 
 * @author fengwk
 */
public abstract class GenMethodTemplate {
    
    private TextMethod parseHighlightMethodName(String source, int offset, String highlightMethodName) {
        if (highlightMethodName == null || highlightMethodName.isEmpty()) {
            return null;
        }
        
        int s = 0;
        int pBegin = -1;
        int pEnd = -1;
        for (int i = offset; i < source.length(); i++) {
            char c = source.charAt(i);
            
            if (s == 0) {
                if (c == ')' || c == '{' || c == '}') {
                    return null;
                }
                if (c == '(') {
                    pBegin = i + 1;
                    s++;
                }
            }
            
            else if (s == 1) {
                if (c == '(' || c == '{' || c == '}') {
                    return null;
                }
                if (c == ')') {
                    pEnd = i;
                    s++;
                }
            }
            
            else if (s == 2) {
                if (c == '(' || c == ')' || c == '}') {
                    return null;
                }
                if (c == '{') {
                    return new TextMethod(highlightMethodName, source.substring(pBegin, pEnd), i + 1);
                }
            }
        }
        
        return null;
    }
    
    private TextMethod parseInMethodName(String source, int offset) {
        int mBegin = -1;
        for (int i = offset; i >= 0; i--) {
            char c = source.charAt(i);
            if (c == ')' || c == '{' || c == '}') {
                return null;
            }
            if (c == ' ' || c == '\n' || c == '\r' || c == '\t') {
                mBegin = i + 1;
                break;
            }
        }
        if (mBegin == -1) {
            return null;
        }
        
        int s = 0;
        int mEnd = -1;
        int pBegin = -1;
        int pEnd = -1;
        for (int i = mBegin; i < source.length(); i++) {
            char c = source.charAt(i);
            
            if (s == 0) {
                if (c == ')' || c == '{' || c == '}') {
                    return null;
                }
                if (c == ' ' || c == '\n' || c == '\r' || c == '\t' || c == '(' ) {
                    mEnd = i;
                    s++;
                }
            }
            
            if (s == 1) {
                if (c == ')' || c == '{' || c == '}') {
                    return null;
                }
                if (c == '(') {
                    pBegin = i + 1;
                    s++;
                }
            }
            
            else if (s == 2) {
                if (c == '(' || c == '{' || c == '}') {
                    return null;
                }
                if (c == ')') {
                    pEnd = i;
                    s++;
                }
            }
            
            else if (s == 3) {
                if (c == '(' || c == ')' || c == '}') {
                    return null;
                }
                if (c == '{') {
                    return new TextMethod(source.substring(mBegin, mEnd), source.substring(pBegin, pEnd), i + 1);
                }
            }
            
        }
        
        return null;
    }
    
    private TextMethod parseInMethodBody(String source, int offset) {
        int s = 0;
        int methodBodyStart = -1;
        int mEnd = -1;
        int pBegin = -1;
        int pEnd = -1;
        for (int i = offset; i >= 0; i--) {
            char c = source.charAt(i);
            
            if (s == 0) {
                if (c == '(' || c == ')' || c == '}') {
                    return null;
                }
                if (c == '{') {
                    methodBodyStart = i + 1;
                    s++;
                }
            }
            
            else if (s == 1) {
                if (c == '(' || c == '{' || c == '}') {
                    return null;
                }
                if (c == ')') {
                    pEnd = i;
                    s++;
                }
            }
            
            else if (s == 2) {
                if (c == ')' || c == '{' || c == '}') {
                    return null;
                }
                if (c == '(' ) {
                    pBegin = i + 1;
                    s++;
                }
            }
            
            else if (s == 3) {
                if (c == '(' || c == ')' || c == '{' || c == '}') {
                    return null;
                }
                if (c != ' ' && c != '\n' && c != '\r' && c != '\t') {
                    mEnd = i + 1;
                    s++;
                }
            }
            
            else if (s == 4) {
                if (c == '(' || c == ')' || c == '{' || c == '}') {
                    return null;
                }
                if (c == ' ' || c == '\n' || c == '\r' || c == '\t') {
                    return new TextMethod(source.substring(i + 1, mEnd), source.substring(pBegin, pEnd), methodBodyStart);
                }
            }
            
        }
        return null;
    }
    
    
    
    static class TextMethod {
        
        String method;
        String params;
        int methodBodyStart;
        
        public TextMethod
        (String method, String params, int methodBodyStart) {
            this.method = method;
            this.params = params;
            this.methodBodyStart = methodBodyStart;
        }
        
    }
    
    private List<TextMethod> guessSelectedMethods(String source, int offset, String highlightMethodName) {
        List<TextMethod> selectedMethods = new ArrayList<>();
        selectedMethods.add(parseHighlightMethodName(source, offset, highlightMethodName));
        selectedMethods.add(parseInMethodName(source, offset));
        selectedMethods.add(parseInMethodName(source, offset - 1));
        selectedMethods.add(parseInMethodBody(source, offset));
        selectedMethods.add(parseInMethodBody(source, offset - 2));
        return selectedMethods.stream().filter(x -> x != null).collect(Collectors.toList());
    }
    
    static ICompilationUnit getICompilationUnit() {
        IWorkbenchPart iWorkbenchPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().getActivePart();
        if (iWorkbenchPart instanceof EditorPart) {
            IEditorInput iEditorInput = ((EditorPart) iWorkbenchPart).getEditorInput();
            if (iEditorInput.exists() && iEditorInput instanceof FileEditorInput) {
                IJavaElement iJavaElement = JavaCore.create(((FileEditorInput) iEditorInput).getFile());
                if (iJavaElement.getElementType() == IJavaElement.COMPILATION_UNIT) {
                    ICompilationUnit cu = (ICompilationUnit) iJavaElement;
                    return cu;
                }
            }
        }
        return null;
    }
    
    static ITextSelection getITextSelection() {
        ISelection iSelection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
        if (iSelection instanceof ITextSelection) {
            return (ITextSelection) iSelection;
        }
        return null;
    }
    
    @Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell s) {
        ICompilationUnit cu = getICompilationUnit();
        ITextSelection ts = getITextSelection();
        if (cu != null && ts != null) {
            try {
                String source = cu.getSource();
                int offset = ts.getOffset();
                List<TextMethod> maySelectedMethods = guessSelectedMethods(source, offset, ts.getText());
                if (!maySelectedMethods.isEmpty()) {
                    ClassLoader cl = ProjectClassLoaderFactory.get(cu.getJavaProject());
                    JavaClassParser parser = new JavaClassParser(cl, cu);
                    for (TextMethod textMethod : maySelectedMethods) {
                        MethodMateData methodMateData = null;
                        try {
                            methodMateData = parser.findMethod(textMethod.method, textMethod.params);
                            if (methodMateData == null || methodMateData.getReturnType() == null) {
                                continue;
                            }
                        } catch (JavaModelException e) {
                            // ignore
                        }
                        if (methodMateData == null || !canGen(methodMateData)) {
                            continue;
                        }
                        cu.applyTextEdit(new InsertEdit(textMethod.methodBodyStart, "\n" + genMethodContent(methodMateData)), null);
                        return;
                    }
                }
            } catch (JavaModelException | IOException | IllegalStateException | IllegalArgumentException e) {
                MessageDialog.openError(s, "Code Helper", "Error: " + e.getMessage() + ".");
                return;
            } catch (Throwable e) {
                e.printStackTrace();
                MessageDialog.openError(s, "Code Helper", "Please select the correct convert method first.\nError: " + e.getMessage() + ".");
                return;
            }
        }
        MessageDialog.openInformation(s, "Code Helper", "Please select the correct convert method first.");
	}
    
    protected abstract boolean canGen(MethodMateData methodMateData);
    
    protected abstract String genMethodContent(MethodMateData methodMateData) throws Exception;
    
}
