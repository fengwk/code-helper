package com.fengwk.codehelper.gen;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.operation.IRunnableContext;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionStatusDialog;
import org.eclipse.jdt.core.search.IJavaSearchScope;

import org.eclipse.jdt.ui.dialogs.TypeSelectionExtension;

import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.dialogs.FilteredTypesSelectionDialog;

/**
 * 
 * @author fengwk
 */
@SuppressWarnings("restriction")
public class TypeSelectorDialog extends FilteredTypesSelectionDialog {

    private static final String DIALOG_SETTINGS= "org.eclipse.jdt.internal.ui.dialogs.OpenTypeSelectionDialog2"; //$NON-NLS-1$

    public TypeSelectorDialog(Shell parent, boolean multi, IRunnableContext context, IJavaSearchScope scope, int elementKinds) {
        this(parent, multi, context, scope, elementKinds, null);
    }

    public TypeSelectorDialog(Shell parent, boolean multi, IRunnableContext context, IJavaSearchScope scope, int elementKinds, TypeSelectionExtension extension) {
        super(parent, multi, context, scope, elementKinds, extension);
    }

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        PlatformUI.getWorkbench().getHelpSystem().setHelp(newShell, IJavaHelpContextIds.OPEN_TYPE_DIALOG);
    }

    @Override
    protected IDialogSettings getDialogSettings() {
        IDialogSettings settings= JavaPlugin.getDefault().getDialogSettings().getSection(DIALOG_SETTINGS);

        if (settings == null) {
            settings= JavaPlugin.getDefault().getDialogSettings().addNewSection(DIALOG_SETTINGS);
        }

        return settings;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        if (getClass() == TypeSelectorDialog.class) {
            createButton(parent, IDialogConstants.OK_ID, "&Select", true);
            createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
        } else
            super.createButtonsForButtonBar(parent);
    }

}
