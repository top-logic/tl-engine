/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.breadcrumb;

import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.basic.ControlRenderer;
import com.top_logic.layout.tree.TreeUpdateAccumulator.NodeUpdate;
import com.top_logic.layout.tree.TreeUpdateAccumulator.UpdateVisitor;

/**
 * The class {@link BreadcrumbRenderer} is a {@link ControlRenderer} which has a visitor for
 * {@link NodeUpdate}.
 * 
 * The renderer part writes the {@link BreadcrumbControl}, and the visitor part creates incremental
 * updates and adds them to the given {@link UpdateQueue} when the control is revalidated.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface BreadcrumbRenderer extends ControlRenderer<BreadcrumbControl> {

	/**
	 * Returns an {@link UpdateVisitor} to create incremental updates for a
	 * {@link BreadcrumbControl} which is rendered by this
	 * {@link BreadcrumbRenderer}.
	 */
	UpdateVisitor<?, UpdateQueue, BreadcrumbControl> getUpdater();

}
