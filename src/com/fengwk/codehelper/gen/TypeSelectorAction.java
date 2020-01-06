package com.fengwk.codehelper.gen;

import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;

import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.NewProjectAction;
import org.eclipse.ui.dialogs.SelectionDialog;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchConstants;

import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.JavaUIMessages;

/**
 * 
 * @author fengwk
 */
@SuppressWarnings("restriction")
public class TypeSelectorAction extends Action implements IWorkbenchWindowActionDelegate, IActionDelegate2 {

    TypeHandler typeHandler;
    Shell parent;
    
    public TypeSelectorAction(TypeHandler typeHandler, Shell parent) {
        this.typeHandler = typeHandler;
        this.parent = parent;
        setText("Type Selector");
        setDescription("Type Selector");
        setToolTipText("Type Selector");
        setImageDescriptor(JavaPluginImages.DESC_TOOL_OPENTYPE);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IJavaHelpContextIds.OPEN_TYPE_ACTION);
    }

    @Override
    public void run() {
        runWithEvent(null);
    }

    @Override
    public void runWithEvent(Event e) {
        if (!doCreateProjectFirstOnEmptyWorkspace(parent)) {
            return;
        }

        SelectionDialog dialog= new TypeSelectorDialog(parent, true, PlatformUI.getWorkbench().getProgressService(), null, IJavaSearchConstants.TYPE);
        dialog.setTitle("Type Selector");
        dialog.setMessage(JavaUIMessages.OpenTypeAction_dialogMessage);

        int result= dialog.open();
        if (result != IDialogConstants.OK_ID)
            return;

        Object[] types= dialog.getResult();
        if (types == null || types.length == 0)
            return;
        
        
        
        for (Object o : types) {
            if (!(o instanceof IType)) {
                continue;
            }
            typeHandler.handle((IType) o);
        }
    }

    /**
     * Opens the new project dialog if the workspace is empty.
     * @param parent the parent shell
     * @return returns <code>true</code> when a project has been created, or <code>false</code> when the
     * new project has been canceled.
     */
    protected boolean doCreateProjectFirstOnEmptyWorkspace(Shell parent) {
        IWorkspaceRoot workspaceRoot= ResourcesPlugin.getWorkspace().getRoot();
        if (workspaceRoot.getProjects().length == 0) {
            String title= JavaUIMessages.OpenTypeAction_dialogTitle;
            String message= JavaUIMessages.OpenTypeAction_createProjectFirst;
            if (MessageDialog.openQuestion(parent, title, message)) {
                new NewProjectAction().run();
                return workspaceRoot.getProjects().length != 0;
            }
            return false;
        }
        return true;
    }

    // ---- IWorkbenchWindowActionDelegate
    // ------------------------------------------------

    @Override
    public void run(IAction action) {
        run();
    }

    @Override
    public void dispose() {
        // do nothing.
    }

    @Override
    public void init(IWorkbenchWindow window) {
        // do nothing.
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        // do nothing. Action doesn't depend on selection.
    }

    // ---- IActionDelegate2
    // ------------------------------------------------

    @Override
    public void runWithEvent(IAction action, Event event) {
        runWithEvent(event);
    }

    @Override
    public void init(IAction action) {
        // do nothing.
    }
    
    public interface TypeHandler {
        
        void handle(IType type);
        
    }
    
}
