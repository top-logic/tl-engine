/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.server;

import java.util.Collection;
import java.util.Collections;

import com.top_logic.graph.common.model.Edge;
import com.top_logic.graph.common.model.GraphModel;
import com.top_logic.graph.common.model.GraphPart;
import com.top_logic.graph.common.model.Label;
import com.top_logic.graph.common.model.Node;
import com.top_logic.graph.diagramjs.model.impl.DefaultDiagramJSGraphModel;
import com.top_logic.graph.diagramjs.server.commands.CreateClassCommand;
import com.top_logic.graph.diagramjs.server.commands.CreateEnumerationCommand;
import com.top_logic.graph.diagramjs.server.util.GraphModelUtil;
import com.top_logic.graph.diagramjs.server.util.layout.Bounds;
import com.top_logic.graph.diagramjs.server.util.model.TLInheritance;
import com.top_logic.graph.diagramjs.server.util.model.TLInheritanceImpl;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.form.component.PostCreateAction;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassProperty;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;

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

		DefaultDiagramJSGraphModel graph = getSharedGraphModel(graphComponent);

		GraphPart createdPart = createGraphPart(component, getLabelProvider(graphComponent), newModel, graph,
			graphComponent.getHiddenGraphParts(), graphComponent.getInvisibleGraphParts());
		graph.setSelectedGraphParts(Collections.singleton(createdPart));
	}

	private GraphPart createGraphPart(LayoutComponent component, LabelProvider labelProvider, Object newModel,
			GraphModel graph, Collection<Object> hiddenElements, Collection<Object> invisibleElements) {
		if (newModel instanceof TLClassProperty) {
			TLClassProperty property = (TLClassProperty) newModel;

			return GraphModelUtil.createClassProperty(labelProvider, graph.getNode(property.getOwner()), property);
		} else if (newModel instanceof TLReference) {
			return createReference(newModel, labelProvider, graph);
		} else if (newModel instanceof TLClass) {
			Node classNode =
				createNode(labelProvider, newModel, graph, component.get(CreateClassCommand.BOUNDS), hiddenElements,
					invisibleElements);

			createEdgesForNewClassNode(classNode, labelProvider, graph);

			return classNode;
		} else if (newModel instanceof TLEnumeration) {
			return createNode(labelProvider, newModel, graph, component.get(CreateEnumerationCommand.BOUNDS),
				hiddenElements, invisibleElements);
		} else {
			throw new UnsupportedOperationException("diagram part for " + newModel + " could not be created.");
		}
	}

	private void createEdgesForNewClassNode(Node node, LabelProvider labelProvider, GraphModel graph) {
		TLClass clazz = (TLClass) node.getTag();

		for (TLClass generalization : clazz.getGeneralizations()) {
			Node target = graph.getNode(generalization);

			if (target != null) {
				TLInheritance inheritance = new TLInheritanceImpl(clazz, generalization);

				GraphModelUtil.createDiagramJSEdge(labelProvider, graph, inheritance, node, target);
			}
		}
	}

	private GraphPart createReference(Object newModel, LabelProvider labelProvider, GraphModel graph) {
		TLReference reference = (TLReference) newModel;

		if (GraphModelUtil.isBackReference(reference)) {
			return createSourceNameLabel(labelProvider, graph, reference);
		} else {
			return GraphModelUtil.createDiagramJSEdge(labelProvider, graph, reference);
		}
	}

	private Label createSourceNameLabel(LabelProvider labelProvider, GraphModel graphModel, TLReference reference) {
		GraphPart graphPart = getForwardReferenceEdge(graphModel, reference);

		return GraphModelUtil.createSourceNameLabel(labelProvider, (Edge) graphPart, reference);
	}

	private GraphPart getForwardReferenceEdge(GraphModel graphModel, TLReference reference) {
		GraphPart graphPart = graphModel.getGraphPart(TLModelUtil.getForeignName(reference));

		if (graphPart instanceof Label) {
			graphPart = ((Label) graphPart).getOwner();
		}

		return graphPart;
	}

	private LabelProvider getLabelProvider(DiagramJSGraphComponent component) {
		return component.getLabelProvider();
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

	private Node createNode(LabelProvider labelProvider, Object newModel, GraphModel graph, Bounds bounds,
			Collection<Object> hiddenElements, Collection<Object> invisibleElements) {
		return GraphModelUtil.createDiagramJSNode(labelProvider, graph, (TLType) newModel, bounds, hiddenElements,
			invisibleElements);
	}

	private DefaultDiagramJSGraphModel getSharedGraphModel(DiagramJSGraphComponent component) {
		return (DefaultDiagramJSGraphModel) component.getGraphModel();
	}
}
