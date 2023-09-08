/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.model.impl;

import com.top_logic.common.remote.shared.DefaultSharedObject;
import com.top_logic.common.remote.shared.ObjectScope;
import com.top_logic.graph.common.model.GraphModel;
import com.top_logic.graph.common.model.GraphPart;
import com.top_logic.graph.common.model.Label;
import com.top_logic.graph.common.model.impl.DefaultGraphModel;
import com.top_logic.graph.diagramjs.model.DiagramJSEdge;
import com.top_logic.graph.diagramjs.model.DiagramJSGraphModel;

/**
 * {@link DefaultSharedObject} diagramJS {@link GraphModel} implementation.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class DefaultDiagramJSGraphModel extends DefaultGraphModel implements DiagramJSGraphModel {

	/**
	 * Creates a {@link DefaultDiagramJSGraphModel}.
	 */
	public DefaultDiagramJSGraphModel(ObjectScope scope) {
		super(scope);
	}

	@Override
	protected DiagramJSEdge createEdgeInternal(ObjectScope scope) {
		return new DefaultDiagramJSEdge(scope);
	}

	@Override
	protected DefaultDiagramJSClassNode createNodeInternal(ObjectScope scope) {
		return new DefaultDiagramJSClassNode(scope);
	}

	@Override
	public DefaultDiagramJSClassNode getNode(Object businessObject) {
		GraphPart graphPart = getOwningGraphPart(businessObject);

		if (graphPart instanceof DefaultDiagramJSClassNode) {
			return (DefaultDiagramJSClassNode) graphPart;
		}

		return null;
	}

	@Override
	public DiagramJSEdge getEdge(Object businessObject) {
		GraphPart graphPart = getOwningGraphPart(businessObject);

		if (graphPart instanceof DiagramJSEdge) {
			return (DiagramJSEdge) graphPart;
		}

		return null;
	}

	private GraphPart getOwningGraphPart(Object businessObject) {
		GraphPart graphPart = getGraphPart(businessObject);

		if (graphPart instanceof Label) {
			return ((Label) graphPart).getOwner();
		}

		return graphPart;
	}

}
