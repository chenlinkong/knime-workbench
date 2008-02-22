/* 
 * -------------------------------------------------------------------
 * This source code, its documentation and all appendant files
 * are protected by copyright law. All rights reserved.
 *
 * Copyright, 2003 - 2007
 * University of Konstanz, Germany
 * Chair for Bioinformatics and Information Mining (Prof. M. Berthold)
 * and KNIME GmbH, Konstanz, Germany
 *
 * You may not modify, publish, transmit, transfer or sell, reproduce,
 * create derivative works from, distribute, perform, display, or in
 * any way exploit any of the content, in whole or in part, except as
 * otherwise expressly permitted in writing by the copyright owner or
 * as specified in the license file distributed with this product.
 *
 * If you have any questions please contact the copyright holder:
 * website: www.knime.org
 * email: contact@knime.org
 * -------------------------------------------------------------------
 * 
 * History
 *   25.05.2005 (Florian Georg): created
 */
package org.knime.workbench.editor2.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.workflow.NodeContainer;

import org.knime.workbench.editor2.ImageRepository;

/**
 * Action to open a view of a node.
 * 
 * TODO: Embedd view in an eclipse view (preference setting)
 * 
 * @author Florian Georg, University of Konstanz
 */
public class OpenViewAction extends Action {
    private NodeContainer m_nodeContainer;

    private int m_index;

    private static final NodeLogger LOGGER = NodeLogger
            .getLogger(OpenPortViewAction.class);

    /**
     * New action to opne a node view.
     * 
     * @param nodeContainer The node
     * @param viewIndex The index of the node view
     */
    public OpenViewAction(final NodeContainer nodeContainer, final int viewIndex) {
        m_nodeContainer = nodeContainer;
        m_index = viewIndex;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ImageDescriptor getImageDescriptor() {
        return ImageRepository.getImageDescriptor("icons/openView.gif");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getToolTipText() {
        return "Opens node view " + m_index + ": "
                + m_nodeContainer.getViewName(m_index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getText() {
        return "View: " + m_nodeContainer.getViewName(m_index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        LOGGER.debug("Open Node View " + m_nodeContainer.nodeToString() + " (#"
                + m_index + ")");
        m_nodeContainer.showView(m_index);
    }
}
