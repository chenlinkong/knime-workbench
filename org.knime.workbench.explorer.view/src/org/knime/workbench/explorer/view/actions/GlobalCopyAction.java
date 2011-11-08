/* ------------------------------------------------------------------
  * This source code, its documentation and all appendant files
  * are protected by copyright law. All rights reserved.
  *
  * Copyright, 2008 - 2011
  * KNIME.com, Zurich, Switzerland
  *
  * You may not modify, publish, transmit, transfer or sell, reproduce,
  * create derivative works from, distribute, perform, display, or in
  * any way exploit any of the content, in whole or in part, except as
  * otherwise expressly permitted in writing by the copyright owner or
  * as specified in the license file distributed with this product.
  *
  * If you have any questions please contact the copyright holder:
  * website: www.knime.com
  * email: contact@knime.com
  * ---------------------------------------------------------------------
  *
  * History
  *   Oct 31, 2011 (morent): created
  */

package org.knime.workbench.explorer.view.actions;

import org.eclipse.jface.viewers.TreeViewer;

/**
 * Action to copy files to another location in KNIME Explorer.
 *
 * @author Dominik Morent, KNIME.com, Zurich, Switzerland
 */
public class GlobalCopyAction extends AbstractCopyMoveAction {

    /** ID of the global copy action in the explorer menu. */
    public static final String ACTION_ID =
            "org.knime.workbench.explorer.action.copy";

    /**
     * @param viewer the viewer
     */
    public GlobalCopyAction(final TreeViewer viewer) {
        super(viewer, "Copy...", false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return ACTION_ID;
    }

}
