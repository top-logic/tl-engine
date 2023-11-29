/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.server;

import com.top_logic.graph.common.model.GraphPart;
import com.top_logic.graph.common.model.Node;
import com.top_logic.graph.diagramjs.server.commands.CreateClassCommand;
import com.top_logic.graph.diagramjs.server.util.GraphModelUtil;
import com.top_logic.graph.diagramjs.server.util.layout.Bounds;
import com.top_logic.layout.form.component.PostCreateAction;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLModelPart;

/**
 * Creates a new {@link GraphPart}, client side representation, for a newly created, diagram
 * relevant, {@link TLModelPart}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class PostGraphPartCreationAction implements PostCreateAction {

	/**
	 * Singleton {@link PostGraphPartCreationAction} instance.
	 */
	public static final PostGraphPartCreationAction INSTANCE = new PostGraphPartCreationAction();

	private PostGraphPartCreationAction() {
		// Singleton constructor.
	}

	@Override
	public void handleNew(LayoutComponent component, Object newModel) {
		DiagramJSGraphComponent graphComponent = getDiagramJSGraphComponent(component);

		GraphPart newPart = graphComponent.getOrCreateGraphPart(newModel);

		if (newPart instanceof Node) {
			GraphModelUtil.applyBounds((Node) newPart, getCreatedBounds(component));
		}
	}

	private Bounds getCreatedBounds(LayoutComponent component) {
		return component.get(CreateClassCommand.BOUNDS);
	}

	private DiagramJSGraphComponent getDiagramJSGraphComponent(LayoutComponent component) {
		LayoutComponent dialogParent = component.getDialogParent();
		LayoutComponent master = component.getMaster();

		if (dialogParent instanceof DiagramJSGraphComponent) {
			return (DiagramJSGraphComponent) dialogParent;
		} else if (master instanceof DiagramJSGraphComponent) {
			return (DiagramJSGraphComponent) master;
		}

		return null;
	}
}
