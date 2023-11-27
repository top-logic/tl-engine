/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.client.service.scope.listener;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.top_logic.client.diagramjs.core.Diagram;
import com.top_logic.client.diagramjs.model.Base;
import com.top_logic.client.diagramjs.model.Label;
import com.top_logic.client.diagramjs.model.Shape;
import com.top_logic.common.remote.shared.ScopeEvent;
import com.top_logic.common.remote.shared.ScopeListener;
import com.top_logic.graph.common.model.GraphPart;
import com.top_logic.graph.common.model.LabelOwner;
import com.top_logic.graph.diagramjs.model.DiagramJSClassNode;
import com.top_logic.graph.diagramjs.model.DiagramJSEdge;
import com.top_logic.graph.diagramjs.model.impl.DefaultDiagramJSClassNode;
import com.top_logic.graph.diagramjs.model.impl.DefaultDiagramJSEdge;
import com.top_logic.graph.diagramjs.model.impl.DefaultDiagramJSGraphModel;
import com.top_logic.graph.diagramjs.model.impl.DefaultDiagramJSLabel;

/**
 * Default listener for {@link ScopeEvent} on shared graphs.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class DefaultGraphScopeListener implements ScopeListener {

	private Set<Base> _drawingNewGraphParts;

	private Diagram _diagram;

	private DisplayGraphPartCreator _graphPartCreator;

	private DisplayGraphUpdatePostProcesser _postProcesser;

	private DisplayGraphPartUpdater _graphPartUpdater;

	private Collection<? extends GraphPart> _selectedGraphParts;

	/**
	 * Creates a {@link DefaultGraphScopeListener}.
	 */
	public DefaultGraphScopeListener(Diagram diagram) {
		_diagram = diagram;

		_graphPartCreator = new DisplayGraphPartCreator(diagram);
		_postProcesser = new DisplayGraphUpdatePostProcesser(diagram);
		_graphPartUpdater = new DisplayGraphPartUpdater(diagram);

		_drawingNewGraphParts = new HashSet<>();
		_selectedGraphParts = new HashSet<>();
	}

	@Override
	public void handleObjectScopeEvent(ScopeEvent event) {
		switch (event.getKind()) {
			case CREATE:
				createDisplayGraphPart(event);
				break;

			case UPDATE:
				updateDisplayGraphPart(event);
				break;

			case DELETE:
				deleteDisplayGraphPart(event);
				break;

			case POST_PROCESS:
				postProcess();
				break;

			case PREPARE:
				break;

			default:
				break;
		}
	}

	private void postProcess() {
		_postProcesser.finish(_drawingNewGraphParts, _selectedGraphParts);

		_drawingNewGraphParts = new HashSet<>();
	}

	private void updateDisplayGraphPart(ScopeEvent event) {
		Object object = event.getObj();

		String property = ((ScopeEvent.Update) event).getProperty();

		if (property == "owner") {
			if (object instanceof DefaultDiagramJSLabel) {
				LabelOwner owner = ((DefaultDiagramJSLabel) object).getOwner();
				if (owner != null) {
					if (owner instanceof DiagramJSClassNode) {
						DefaultDiagramJSClassNode node = (DefaultDiagramJSClassNode) owner;
						Shape shape = (Shape) node.getTag();
						if (!_drawingNewGraphParts.contains(shape)) {
							Base label = (Base) ((DefaultDiagramJSLabel) object).getTag();
							_graphPartUpdater.update(label);
						}
					} else if (owner instanceof DiagramJSEdge) {
						Base parent = (Base) owner.getTag();
						if (!_drawingNewGraphParts.contains(parent)) {
							Label label = (Label) ((DefaultDiagramJSLabel) object).getTag();
							_diagram.getCanvas().addShape(label, parent);
						}
					}
				}
			}
		} else if ("selectedGraphParts".equals(property)) {
			_selectedGraphParts = ((DefaultDiagramJSGraphModel) event.getObj()).getSelectedGraphParts();
		}

	}

	private void createDisplayGraphPart(ScopeEvent event) {
		Object object = event.getObj();

		if (object instanceof GraphPart) {
			createDisplayGraphPart((GraphPart) object);
		}
	}

	private void createDisplayGraphPart(GraphPart graphPart) {
		Base displayGraphPart = null;

		if (graphPart instanceof DefaultDiagramJSLabel) {
			displayGraphPart = _graphPartCreator.createLabel((DefaultDiagramJSLabel) graphPart);
		} else if (graphPart instanceof DefaultDiagramJSClassNode) {
			displayGraphPart = _graphPartCreator.createNode((DefaultDiagramJSClassNode) graphPart);
		} else if (graphPart instanceof DefaultDiagramJSEdge) {
			displayGraphPart = _graphPartCreator.createEdge((DefaultDiagramJSEdge) graphPart);
		}

		if (displayGraphPart != null) {
			_drawingNewGraphParts.add(displayGraphPart);
		}
	}

	private void deleteDisplayGraphPart(ScopeEvent event) {
		GraphPart element = (GraphPart) event.getObj();
		Base displayElement = (Base) element.getTag();

		if (displayElement != null) {
			if (!isClassifierLabel(element)) {
				_diagram.getModeler().removeElement(displayElement);
			}
		}
	}

	private boolean isClassifierLabel(GraphPart element) {
		if (element instanceof DefaultDiagramJSLabel) {
			return isClassifierLabel((DefaultDiagramJSLabel) element);
		}

		return false;
	}

	private boolean isClassifierLabel(DefaultDiagramJSLabel label) {
		return "classifier".equals(label.getType());
	}

}
