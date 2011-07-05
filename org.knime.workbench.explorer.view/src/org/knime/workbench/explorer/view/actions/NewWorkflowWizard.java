/*
 * ------------------------------------------------------------------------
 *
 *  Copyright (C) 2003 - 2011
 *  University of Konstanz, Germany and
 *  KNIME GmbH, Konstanz, Germany
 *  Website: http://www.knime.org; Email: contact@knime.org
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME GMBH herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * Created: May 19, 2011
 * Author: ohl
 */
package org.knime.workbench.explorer.view.actions;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.internal.filesystem.local.LocalFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.knime.core.node.workflow.WorkflowPersistor;
import org.knime.workbench.explorer.filesystem.ExplorerFileStore;
import org.knime.workbench.explorer.localworkspace.LocalWorkspaceFileStore;
import org.knime.workbench.explorer.view.AbstractContentProvider;
import org.knime.workbench.explorer.view.ContentObject;

/**
 *
 * @author ohl, University of Konstanz
 */
public class NewWorkflowWizard extends Wizard implements INewWizard {

    private NewWorkflowWizardPage m_page;

    private final AbstractContentProvider m_contentProvider;

    private List<ExplorerFileStore> m_initialSelection;

    /**
     * Creates the wizard.
     */
    public NewWorkflowWizard(final AbstractContentProvider spaceProvider) {
        m_contentProvider = spaceProvider;
        setNeedsProgressMonitor(true);
    }

    protected AbstractContentProvider getConentProvider() {
        return m_contentProvider;
    }

    protected List<ExplorerFileStore> getInitialSelection() {
        return m_initialSelection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final IWorkbench workbench,
            final IStructuredSelection selection) {
        m_initialSelection = new LinkedList<ExplorerFileStore>();
        if (selection != null && selection.size() > 0) {
            String mountID = m_contentProvider.getMountID();
            @SuppressWarnings("rawtypes")
            Iterator iter = selection.iterator();
            while (iter.hasNext()) {
                Object n = iter.next();
                ExplorerFileStore file = null;
                if (n instanceof ContentObject) {
                    file = ((ContentObject)n).getObject();
                } else if (n instanceof AbstractContentProvider) {
                    file = ((AbstractContentProvider)n).getFileStore("/");
                }
                if (file != null && file.getMountID().equals(mountID)) {
                    m_initialSelection.add(file);
                }
            }
        }
    }

    /**
     * Adding the page to the wizard.
     */
    @Override
    public void addPages() {
        NewWorkflowWizardPage page =
                new NewWorkflowWizardPage(m_contentProvider,
                        m_initialSelection, true);
        addPage(page);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addPage(final IWizardPage page) {
        if (page instanceof NewWorkflowWizardPage) {
            m_page = (NewWorkflowWizardPage)page;
        }
        super.addPage(page);
    }

    /**
     * Perform finish - queries the page and creates the project / file.
     *
     * {@inheritDoc}
     */
    @Override
    public boolean performFinish() {
        final ExplorerFileStore newitem = m_page.getNewFile();
        if (!newitem.getMountID().equals(m_contentProvider.getMountID())) {
            MessageDialog.openError(getShell(), "Internal Error",
                    "Internal Error: Unable to create a new item in this "
                            + "context (wrong content mount id).");
            return false;
        }
        // Create new runnable
        IRunnableWithProgress op = new IRunnableWithProgress() {
            @Override
            public void run(final IProgressMonitor monitor)
                    throws InvocationTargetException {
                try {
                    // call the worker method
                    doFinish(newitem, monitor);
                } catch (CoreException e) {
                    throw new InvocationTargetException(e);
                } finally {
                    monitor.done();
                }
            }
        };
        try {
            getContainer().run(true, false, op);
        } catch (InterruptedException e) {
            return false;
        } catch (InvocationTargetException e) {
            // get the exception that issued this async exception
            Throwable realException = e.getTargetException();
            MessageDialog.openError(getShell(), "Error",
                    realException.getMessage());
            return false;
        }

        return true;
    }

    /**
     * Worker method, creates the project using the given options.
     *
     * @param workflowPath path of the workflow to create in workspace
     * @param monitor Progress monitor
     * @throws CoreException if error while creating the project
     */
    protected void doFinish(final ExplorerFileStore newItem,
            final IProgressMonitor monitor) throws CoreException {

        if (newItem.fetchInfo().exists()) {
            throwCoreException("Resource \"" + newItem.getFullName()
                    + "\" already exists.", null);
        }

        if (!newItem.getParent().fetchInfo().exists()) {
            throwCoreException("Parent directory doesn't exist. "
                    + "Create a workflow group before you place "
                    + "a workflow in.", null);
        }

        // create workflow dir
        newItem.mkdir(EFS.NONE, monitor);

        // create a new empty workflow file
        final ExplorerFileStore workflowFile =
                newItem.getChild(WorkflowPersistor.WORKFLOW_FILE);
        OutputStream outStream =
                workflowFile.openOutputStream(EFS.NONE, monitor);
        try {
            outStream.close();
        } catch (IOException e1) {
            // don't close - don't open editor
            return;
        }

        // open workflow in a new editor
        monitor.setTaskName("Opening new workflow for editing...");
        File locFile = workflowFile.toLocalFile(EFS.NONE, monitor);
        if (locFile == null) {
            // only local workflows can be opened
            return;
        }
        final LocalFile editorFile = new LocalFile(locFile);
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                IWorkbenchPage page =
                        PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                                .getActivePage();
                try {
                    IDE.openEditorOnFileStore(page, editorFile);
                } catch (PartInitException e) {
                    // ignore it
                }
            }
        });
        if (newItem instanceof LocalWorkspaceFileStore) {
            ((LocalWorkspaceFileStore)newItem).refreshParentResource();
        }
    }

    protected static void throwCoreException(final String message,
            final Throwable t) throws CoreException {
        IStatus status =
                new Status(IStatus.ERROR, "org.knime.workbench.ui", IStatus.OK,
                        message, t);
        throw new CoreException(status);
    }

}
