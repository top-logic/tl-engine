/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.server.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.top_logic.basic.shared.collection.CollectionUtilShared;
import com.top_logic.common.remote.factory.ReflectionFactory;
import com.top_logic.common.remote.shared.ObjectScope;
import com.top_logic.common.remote.shared.SharedObject;
import com.top_logic.graph.common.model.Edge;
import com.top_logic.graph.common.model.GraphModel;
import com.top_logic.graph.common.model.GraphPart;
import com.top_logic.graph.common.model.Label;
import com.top_logic.graph.common.model.LabelOwner;
import com.top_logic.graph.common.model.Node;
import com.top_logic.graph.common.model.impl.SharedGraph;
import com.top_logic.graph.diagramjs.model.DiagramJSEdge;
import com.top_logic.graph.diagramjs.model.DiagramJSLabel;
import com.top_logic.graph.diagramjs.model.impl.DefaultDiagramJSClassNode;
import com.top_logic.graph.diagramjs.model.impl.DefaultDiagramJSEdge;
import com.top_logic.graph.diagramjs.model.impl.DefaultDiagramJSGraphModel;
import com.top_logic.graph.diagramjs.model.impl.DefaultDiagramJSLabel;
import com.top_logic.graph.diagramjs.server.handler.CreateInheritanceHandler;
import com.top_logic.graph.diagramjs.server.util.layout.Bounds;
import com.top_logic.graph.diagramjs.server.util.layout.Dimension;
import com.top_logic.graph.diagramjs.server.util.layout.Position;
import com.top_logic.graph.diagramjs.server.util.model.TLInheritance;
import com.top_logic.graph.diagramjs.server.util.model.TLInheritanceImpl;
import com.top_logic.graph.diagramjs.util.GraphLayoutConstants;
import com.top_logic.graph.layouter.LayoutContext;
import com.top_logic.graph.layouter.model.LayoutGraph;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutEdge;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutNode;
import com.top_logic.graph.layouter.model.Waypoint;
import com.top_logic.graph.layouter.model.util.LayoutGraphUtil;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassPart;
import com.top_logic.model.TLClassProperty;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.util.TLModelUtil;

/**
 * Util methods to create and manipulate graph models.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class GraphModelUtil implements GraphLayoutConstants {

	private static final String TL_TABLES_MODULE_NAME = ApplicationObjectUtil.TL_TABLES_MODULE;

	private static final int EDGE_HIGH_PRIORITY = 3;

	private static final int EDGE_MIDDLE_PRIORITY = 2;

	private static final int EDGE_LOW_PRIORITY = 1;

	/**
	 * Creates a {@link LayoutGraph} for the given {@link TLModule}.
	 */
	public static LayoutGraph createLayoutGraph(TLModule module, Collection<Object> hiddenElements) {
		LayoutGraph graph = new LayoutGraph();
		Map<TLType, LayoutNode> mapping = new LinkedHashMap<>();

		List<TLType> moduleTypes = getSupportedTypes(module, hiddenElements);

		createLayoutNodes(graph, mapping, moduleTypes);
		createLayoutEdges(graph, mapping, moduleTypes, hiddenElements);

		connectTypes(graph, mapping, getOutsideModuleTLTypes(mapping, moduleTypes), hiddenElements);

		return graph;
	}

	private static void connectTypes(LayoutGraph graph, Map<TLType, LayoutNode> mapping, Set<TLType> types,
			Collection<Object> hiddenElements) {
		types.stream().forEach(type -> {
			LayoutNode node = mapping.get(type);

			if (type.getModelKind() == ModelKind.CLASS) {
				connectGeneralizations(graph, mapping, node, hiddenElements);
				connectSpecializations(graph, mapping, node, hiddenElements);
			}

			connectReferences(graph, mapping, node, hiddenElements);
		});
	}

	private static Set<TLType> getOutsideModuleTLTypes(Map<TLType, LayoutNode> mapping, List<TLType> moduleTypes) {
		return CollectionUtilShared.difference(mapping.keySet(), new HashSet<>(moduleTypes));
	}

	private static List<TLType> getSupportedTypes(TLModule module, Collection<Object> hiddenElements) {
		return getSupportedNodeTypesStream(module, hiddenElements).sorted(getTLTypeNameComparator())
			.collect(Collectors.toList());
	}

	private static Stream<TLType> getSupportedNodeTypesStream(TLModule module, Collection<Object> hiddenElements) {
		return getModuleTypes(module).stream().filter(type -> isSupportedNodeType(type, hiddenElements));
	}

	private static Collection<TLType> getModuleTypes(TLModule module) {
		return module.getTypes();
	}

	private static Comparator<TLType> getTLTypeNameComparator() {
		return Comparator.comparing(TLType::getName, Comparator.naturalOrder());
	}

	/**
	 * Creates a {@link SharedGraph} for the given {@link LayoutGraph}.
	 */
	public static DefaultDiagramJSGraphModel createDiagramJSSharedGraphModel(LabelProvider labelProvider,
			LayoutGraph graph, TLModule module, Collection<Object> hiddenElements,
			Collection<Object> invisibleElements) {
		DefaultDiagramJSGraphModel graphModel = new DefaultDiagramJSGraphModel(new ObjectScope(ReflectionFactory.INSTANCE));

		graphModel.setTag(module);

		Map<LayoutNode, Node> nodeCorrespondence = new LinkedHashMap<>();

		createDiagramJSNodes(labelProvider, graph, graphModel, nodeCorrespondence, hiddenElements, invisibleElements);
		createDiagramJSEdges(labelProvider, graph, graphModel, nodeCorrespondence, invisibleElements);

		return graphModel;
	}

	/**
	 * Creates a {@link GraphPart} into the given {@link GraphModel}.
	 * @param graph
	 *        {@link GraphModel} which contains the created part.
	 * @param newModel
	 *        Business model of this graph part.
	 * @param labelProvider
	 *        Provider to determine labels when this graph part is displayed.
	 * @param hiddenElements
	 *        Collection of business objects that are hidden.
	 * @param invisibleElements
	 *        Collection of business objects that are invisible.
	 */
	public static GraphPart createGraphPart(GraphModel graph, Object newModel,
			LabelProvider labelProvider, Collection<Object> hiddenElements, Collection<Object> invisibleElements) {
		if (newModel instanceof TLClassProperty) {
			TLClassProperty property = (TLClassProperty) newModel;

			return createClassProperty(labelProvider, graph.getNode(property.getOwner()), property);
		} else if (newModel instanceof TLReference) {
			return createReference(newModel, labelProvider, graph);
		} else if (newModel instanceof TLClass) {
			Node node = createDiagramJSNode(labelProvider, graph, (TLType) newModel, hiddenElements, invisibleElements);

			createEdgesForNewClassNode(node, labelProvider, graph);

			return node;
		} else if (newModel instanceof TLEnumeration) {
			return createDiagramJSNode(labelProvider, graph, (TLType) newModel, hiddenElements, invisibleElements);
		} else {
			throw new UnsupportedOperationException("diagram part for " + newModel + " could not be created.");
		}
	}

	private static GraphPart createReference(Object newModel, LabelProvider labelProvider, GraphModel graph) {
		TLReference reference = (TLReference) newModel;

		if (GraphModelUtil.isBackReference(reference)) {
			return createSourceNameLabel(labelProvider, graph, reference);
		} else {
			return createDiagramJSEdge(labelProvider, graph, reference);
		}
	}

	private static Label createSourceNameLabel(LabelProvider labelProvider, GraphModel graphModel,
			TLReference reference) {
		GraphPart graphPart = getForwardReferenceEdge(graphModel, reference);

		return createSourceNameLabel(labelProvider, (Edge) graphPart, reference);
	}

	private static GraphPart getForwardReferenceEdge(GraphModel graphModel, TLReference reference) {
		GraphPart graphPart = graphModel.getGraphPart(TLModelUtil.getForeignName(reference));

		if (graphPart instanceof Label) {
			graphPart = ((Label) graphPart).getOwner();
		}

		return graphPart;
	}

	private static void createEdgesForNewClassNode(Node node, LabelProvider labelProvider, GraphModel graph) {
		TLClass clazz = (TLClass) node.getTag();

		for (TLClass generalization : clazz.getGeneralizations()) {
			Node target = graph.getNode(generalization);

			if (target != null) {
				TLInheritance inheritance = new TLInheritanceImpl(clazz, generalization);

				createDiagramJSEdge(labelProvider, graph, inheritance, node, target);
			}
		}
	}

	private static void createDiagramJSNodes(LabelProvider labelProvider, LayoutGraph graph,
			DefaultDiagramJSGraphModel graphModel,
			Map<LayoutNode, Node> nodeCorrespondence, Collection<Object> hiddenElements,
			Collection<Object> invisibleElements) {
		for (LayoutNode layoutNode : graph.nodes()) {
			DefaultDiagramJSClassNode node =
				createDiagramJSNode(labelProvider, graphModel, layoutNode, hiddenElements, invisibleElements);

			nodeCorrespondence.put(layoutNode, node);
		}
	}

	private static DefaultDiagramJSClassNode createDiagramJSNode(LabelProvider labelProvider,
			DefaultDiagramJSGraphModel graphModel,
			LayoutNode layoutNode, Collection<Object> hiddenElements, Collection<Object> invisibleElements) {
		TLType model = (TLType) layoutNode.getUserObject();
		
		DefaultDiagramJSClassNode node =
			createDiagramJSNode(labelProvider, graphModel, model, hiddenElements, invisibleElements);

		node.setX(layoutNode.getX());
		node.setY(layoutNode.getY());
		node.setHeight(layoutNode.getHeight());
		node.setWidth(layoutNode.getWidth());
		
		if (invisibleElements.contains(model)) {
			node.setVisible(false);
		}

		return node;
	}

	/**
	 * Sets the nodes bounds (i. e. position and dimension) of the given node.
	 */
	public static void applyBounds(Node node, Bounds bounds) {
		Position position = bounds.getPosition();
		Dimension dimension = bounds.getDimension();

		node.setX(position.getX());
		node.setY(position.getY());
		node.setWidth(dimension.getWidth());
		node.setHeight(dimension.getHeight());
	}

	/**
	 * Creates a {@link SharedObject} that represents a TLClass.
	 */
	public static DefaultDiagramJSClassNode createDiagramJSNode(LabelProvider labelProvider, GraphModel graphModel,
			TLType type, Collection<Object> hiddenElements, Collection<Object> invisibleElements) {
		DefaultDiagramJSClassNode node = (DefaultDiagramJSClassNode) graphModel.createNode(null, type);

		node.setClassName(LayoutGraphUtil.getLabel(labelProvider, type));
		node.setImported(((TLModule) graphModel.getTag()).getType(type.getName()) == null);

		getModifiers(type).ifPresent(modifiers -> node.setClassModifiers(modifiers));
		getStereotypes(type).ifPresent(stereotypes -> node.setStereotypes(stereotypes));

		createAttributes(labelProvider, node, type, hiddenElements, invisibleElements);

		return node;
	}

	/**
	 * All stereotypes of the given {@link TLType}.
	 */
	private static Optional<List<String>> getStereotypes(TLType type) {
		if (type instanceof TLEnumeration) {
			return Optional.of(Arrays.asList(ENUMERATION_STEREOTYPE));
		}

		return Optional.empty();
	}

	private static void createAttributes(LabelProvider labelProvider, DefaultDiagramJSClassNode node, TLType type,
			Collection<Object> hiddenElements, Collection<Object> invisibleElements) {
		if (type instanceof TLClass) {
			createClassProperties(labelProvider, node, type, hiddenElements, invisibleElements);
		} else if (type instanceof TLEnumeration) {
			createClassifiers(labelProvider, node, type, hiddenElements, invisibleElements);
		}
	}

	private static void createClassifiers(LabelProvider labelProvider, DefaultDiagramJSClassNode node, TLType type,
			Collection<Object> hiddenElements, Collection<Object> invisibleElements) {
		List<TLClassifier> classifiers = ((TLEnumeration) type).getClassifiers();

		for (TLClassifier classifier : classifiers) {
			if (!hiddenElements.contains(classifier)) {
				Label classifierLabel = createClassifier(labelProvider, node, classifier);

				if (invisibleElements.contains(classifier)) {
					classifierLabel.setVisible(false);
				}
			}
		}
	}

	private static void createClassProperties(LabelProvider labelProvider, DefaultDiagramJSClassNode node,
			TLType type, Collection<Object> hiddenElements, Collection<Object> invisibleElements) {
		List<TLClassPart> localClassParts = ((TLClass) type).getLocalClassParts();

		for (TLClassPart clazzPart : localClassParts) {
			if (!hiddenElements.contains(clazzPart)) {
				if (clazzPart.getModelKind() == ModelKind.PROPERTY) {
					Label property = createClassProperty(labelProvider, node, (TLClassProperty) clazzPart);

					if (invisibleElements.contains(clazzPart)) {
						property.setVisible(false);
					}
				}
			}
		}
	}

	private static Label createClassifier(LabelProvider labelProvider, DefaultDiagramJSClassNode node,
			TLClassifier classifier) {
		return createDiagramJSLabel(node, classifier, LayoutGraphUtil.getLabel(labelProvider, classifier),
			LABEL_CLASSIFIER_TYPE);
	}

	/**
	 * Creates a class property.
	 * 
	 * @see DefaultDiagramJSClassNode#getClassProperties()
	 */
	public static Label createClassProperty(LabelProvider labelProvider, Node node, TLClassProperty property) {
		String propertyLabel = LayoutGraphUtil.getLabel(labelProvider, property);

		return createDiagramJSLabel(node, property, propertyLabel, LABEL_PROPERTY_TYPE);
	}

	private static Label createDiagramJSLabel(LabelOwner owner, Object tag, String text, String type) {
		DefaultDiagramJSLabel label = (DefaultDiagramJSLabel) owner.createLabel();

		label.setTag(tag);
		label.setText(text);
		label.setType(type);

		return label;
	}

	/**
	 * All modifiers of the given {@link TLType}.
	 */
	private static Optional<List<String>> getModifiers(TLType type) {
		if (type instanceof TLClass) {
			return Optional.of(getClassModifiers((TLClass) type));
		}

		return Optional.empty();
	}

	/**
	 * All modifiers of the given {@link TLClass}.
	 */
	private static List<String> getClassModifiers(TLClass clazz) {
		List<String> modifiers = new LinkedList<>();

		if (clazz.isAbstract()) {
			modifiers.add(NODE_ABSTRACT_MODIFIER);
		}

		if (clazz.isFinal()) {
			modifiers.add(NODE_FINAL_MODIFIER);
		}

		return modifiers;
	}

	private static void createDiagramJSEdges(LabelProvider labelProvider, LayoutGraph graph, GraphModel graphModel,
			Map<LayoutNode, Node> nodes, Collection<Object> invisibleElements) {
		for (LayoutNode layoutNode : graph.nodes()) {
			for (LayoutEdge layoutEdge : layoutNode.outgoingEdges()) {
				createDiagramJSEdge(labelProvider, graphModel, nodes, layoutEdge, invisibleElements);
			}
		}
	}

	private static void createDiagramJSEdge(LabelProvider labelProvider, GraphModel graph, Map<LayoutNode, Node> nodes,
			LayoutEdge layoutEdge, Collection<Object> hiddenElements) {
		Node source = nodes.get(layoutEdge.source());
		Node target = nodes.get(layoutEdge.target());

		Object businessObject = layoutEdge.getBusinessObject();

		DefaultDiagramJSEdge edge =
			(DefaultDiagramJSEdge) createDiagramJSEdge(labelProvider, graph, businessObject, source, target);

		edge.setWaypoints(getWaypoints(layoutEdge.getWaypoints()));

		if (isEdgeHidden(hiddenElements, businessObject, layoutEdge.source().getUserObject(),
			layoutEdge.target().getUserObject())) {
			edge.setVisible(false);
		}
	}

	private static boolean isEdgeHidden(Collection<Object> hiddenElements, Object edgeObject, Object sourceObject,
			Object targetObject) {
		return hiddenElements.contains(edgeObject)
			|| hiddenElements.contains(sourceObject)
			|| hiddenElements.contains(targetObject);
	}

	/**
	 * @see #createDiagramJSEdge(LabelProvider, GraphModel, Object, Node, Node)
	 */
	public static Edge createDiagramJSEdge(LabelProvider labelProvider, GraphModel graphModel, TLReference reference) {
		if (reference != null) {
			Node source = graphModel.getNode(reference.getOwner());
			Node target = graphModel.getNode(reference.getType());

			return createDiagramJSEdge(labelProvider, graphModel, reference, target, source);
		}

		return null;
	}

	/**
	 * Creates a client-side diagramjs edge.
	 * 
	 * @see DiagramJSEdge
	 */
	public static Edge createDiagramJSEdge(LabelProvider labelProvider, GraphModel graphModel, Object businessObject,
			Node source, Node target) {
		DefaultDiagramJSEdge edge = (DefaultDiagramJSEdge) graphModel.createEdge(source, target, businessObject);

		getEdgeType(businessObject).ifPresent(type -> edge.setType(type));

		if (businessObject instanceof TLReference) {
			TLReference reference = (TLReference) businessObject;

			createTargetNameLabel(labelProvider, edge, reference);
			createTargetCardinalityLabel(edge, reference);

			TLAssociationEnd otherEnd = TLModelUtil.getOtherEnd(reference.getEnd());

			createSourceCardinalityLabel(edge, reference);
			createSourceNameLabel(labelProvider, edge, otherEnd);
		}

		return edge;
	}

	private static void createSourceCardinalityLabel(DefaultDiagramJSEdge edge, TLReference reference) {
		if (!EDGE_COMPOSITION_TYPE.equals(edge.getType())) {
			String cardinality = LayoutGraphUtil.getCardinality(TLModelUtil.getOtherEnd(reference.getEnd()));

			createDiagramJSLabel(edge, reference, cardinality, LABEL_EDGE_SOURCE_CARDINALITY_TYPE);
		}
	}

	/**
	 * Create the name label for the backwards reference.
	 */
	public static Label createSourceNameLabel(LabelProvider labelProvider, Edge edge, TLReference reference) {
		String text = getTargetName(labelProvider, reference).get();

		return createDiagramJSLabel(edge, reference, text, LABEL_EDGE_SOURCE_NAME_TYPE);
	}

	private static void createSourceNameLabel(LabelProvider labelProvider, Edge edge, TLAssociationEnd end) {
		TLReference reference = end.getReference();

		if (reference != null) {
			createSourceNameLabel(labelProvider, edge, reference);
		}
	}

	private static void createTargetCardinalityLabel(DefaultDiagramJSEdge edge, TLReference reference) {
		String cardinality = LayoutGraphUtil.getCardinality(reference);

		createDiagramJSLabel(edge, reference, cardinality, LABEL_EDGE_TARGET_CARDINALITY_TYPE);
	}

	private static void createTargetNameLabel(LabelProvider labelProvider, DefaultDiagramJSEdge edge,
			TLReference reference) {
		String text = getTargetName(labelProvider, reference).get();

		createDiagramJSLabel(edge, reference, text, LABEL_EDGE_TARGET_NAME_TYPE);
	}

	private static Optional<TLStructuredTypePart> getTypePartOptional(Object object) {
		if (object instanceof TLStructuredTypePart) {
			return Optional.of((TLStructuredTypePart) object);
		} else {
			return Optional.empty();
		}
	}

	private static List<List<Double>> getWaypoints(List<Waypoint> waypoints) {
		return waypoints.stream().map(waypoint -> {
			return Arrays.asList(waypoint.getX(), waypoint.getY());
		}).collect(Collectors.toList());
	}

	private static Optional<String> getEdgeType(Object businessObject) {
		if (businessObject instanceof TLReference) {
			return Optional.of(getEdgeReferenceType((TLReference) businessObject));
		} else if (businessObject instanceof TLInheritance) {
			return Optional.of(EDGE_INHERITANCE_TYPE);
		} else {
			return Optional.empty();
		}
	}

	private static String getEdgeReferenceType(TLReference reference) {
		TLAssociationEnd end = reference.getEnd();

		if (end.isAggregate()) {
			return EDGE_AGGREGATION_TYPE;
		} else if (end.isComposite()) {
			return EDGE_COMPOSITION_TYPE;
		} else {
			return EDGE_ASSOCIATION_TYPE;
		}
	}

	private static void createLayoutNodes(LayoutGraph graph, Map<TLType, LayoutNode> mapping, List<TLType> types) {
		for (TLType type : types) {
			createTypeNode(graph, mapping, type);
		}
	}

	private static boolean isSupportedNodeType(TLType type, Collection<Object> hiddenElements) {
		ModelKind modelKind = type.getModelKind();

		if (hiddenElements.contains(type)) {
			return false;
		}

		return modelKind == ModelKind.CLASS || modelKind == ModelKind.ENUMERATION;
	}

	private static LayoutNode createTypeNode(LayoutGraph graph, Map<TLType, LayoutNode> mapping, TLType type) {
		LayoutNode node = graph.newNode(type);

		mapping.put(type, node);
		graph.add(node);

		return node;
	}

	private static void createLayoutEdges(LayoutGraph graph, Map<TLType, LayoutNode> mapping, List<TLType> types,
			Collection<Object> hiddenElements) {
		for (TLType type : types) {
			LayoutNode node = mapping.get(type);

			if (type.getModelKind() == ModelKind.CLASS) {
				createInheritanceEdges(graph, mapping, (TLClass) type, node, hiddenElements);
			}

			createReferenceEdges(graph, mapping, (TLStructuredType) type, node, hiddenElements);
		}
	}

	private static void createReferenceEdges(LayoutGraph graph, Map<TLType, LayoutNode> mapping, TLStructuredType type,
			LayoutNode source, Collection<Object> hiddenElements) {
		for (TLStructuredTypePart part : type.getLocalParts()) {
			if (part.getModelKind() == ModelKind.REFERENCE) {
				TLType partType = part.getType();

				if (isSupportedNodeType(partType, hiddenElements)) {
					if (!hiddenElements.contains(part)) {
						LayoutNode target = makeNode(graph, mapping, partType);

						if (isDirectReversedReference(part, partType)) {
							createReferenceEdge(graph, target, source, part);
						}
					}
				}
			}
		}
	}

	/**
	 * Creates a reference between the given source and target {@link LayoutNode} with the given
	 * {@link TLStructuredTypePart} as business object.
	 */
	private static LayoutEdge createReferenceEdge(LayoutGraph graph, LayoutNode source, LayoutNode target,
			TLStructuredTypePart part) {
		LayoutEdge referenceEdge = graph.connect(source, target, part);

		setReferenceEdgePriority(part, referenceEdge);

		return referenceEdge;
	}

	private static void setReferenceEdgePriority(TLStructuredTypePart part, LayoutEdge referenceEdge) {
		if (part instanceof TLReference) {
			TLAssociationEnd end = ((TLReference) part).getEnd();
			if (end.isComposite()) {
				referenceEdge.setPriority(EDGE_MIDDLE_PRIORITY);
			} else {
				referenceEdge.setPriority(EDGE_LOW_PRIORITY);
			}
		}
	}

	/**
	 * Checks if the {@link TLReference} is direct reversed.
	 */
	private static boolean isDirectReversedReference(TLStructuredTypePart part, TLType type) {
		TLReference reference = (TLReference) part;

		return !(isReversedReference(reference) && isTargetReversedReferenceStart(reference, type));
	}

	private static boolean isTargetReversedReferenceStart(TLReference reference, TLType type) {
		TLAssociationEnd end = reference.getEnd();
		TLAssociationEnd otherEnd = TLModelUtil.getOtherEnd(end);
		TLReference reference2 = otherEnd.getReference();

		return reference2 != null && reference2.getOwner() == type;
	}

	private static boolean isReversedReference(TLReference reference) {
		int endIndex = TLModelUtil.getEndIndex(reference.getEnd());

		return endIndex == 0;
	}

	private static void createInheritanceEdges(LayoutGraph graph, Map<TLType, LayoutNode> mapping, TLClass clazz,
			LayoutNode classNode, Collection<Object> hiddenElements) {
		for (TLClass generalization : clazz.getGeneralizations()) {
			if (isSupportedNodeType(generalization, hiddenElements)) {
				TLInheritanceImpl edge = new TLInheritanceImpl(clazz, generalization);

				if (!hiddenElements.contains(edge)) {
					LayoutNode generalizationNode = makeNode(graph, mapping, generalization);

					LayoutEdge inheritance = graph.connect(classNode, generalizationNode, edge);
					inheritance.setPriority(EDGE_HIGH_PRIORITY);
				}
			}
		}
	}

	private static LayoutNode makeNode(LayoutGraph graph, Map<TLType, LayoutNode> mapping, TLType type) {
		LayoutNode typeNode = mapping.get(type);

		if (typeNode == null) {
			typeNode = createTypeNode(graph, mapping, type);
		}

		return typeNode;
	}

	/**
	 * @see LayoutGraphUtil#getLabel(LabelProvider, TLTypePart)
	 */
	private static Optional<String> getTargetName(LabelProvider labelProvider, Object businessObject) {
		return getTypePartOptional(businessObject).map(part -> LayoutGraphUtil.getLabel(labelProvider, part));
	}

	/**
	 * Removed the graph parts from the given {@link GraphModel}.
	 */
	public static void removeGraphParts(GraphModel graphModel, Collection<? extends GraphPart> graphParts) {
		for (GraphPart graphPart : graphParts) {
			removeGraphPart(graphModel, graphPart);
		}
	}

	/**
	 * Removed the graph part from the given {@link GraphModel}.
	 */
	public static void removeGraphPart(GraphModel graphModel, GraphPart graphPart) {
		if (graphPart != null) {
			if (graphPart instanceof Label) {
				removeLabel(graphModel, graphPart);
			} else {
				graphModel.removeGraphPart(graphPart);
			}
		}
	}

	private static void removeLabel(GraphModel model, GraphPart part) {
		DiagramJSLabel label = (DiagramJSLabel) part;
		LabelOwner owner = label.getOwner();

		if (owner instanceof Edge) {
			removeEdgeLabel(model, label, owner);
		} else {
			model.removeGraphPart(label);
		}
	}

	private static void removeEdgeLabel(GraphModel model, DiagramJSLabel part, LabelOwner owner) {
		if (isForwardReferenceEdgeLabel(part)) {
			removeEdgeContainer(model, owner);
		} else {
			model.removeGraphPart(part);
		}
	}

	private static void removeEdgeContainer(GraphModel model, LabelOwner owner) {
		Collection<? extends Label> labels = new HashSet<>(owner.getLabels());

		labels.forEach(label -> label.delete());

		model.removeGraphPart(owner);
	}

	private static boolean isForwardReferenceEdgeLabel(DiagramJSLabel part) {
		return !LABEL_EDGE_SOURCE_NAME_TYPE.equals(part.getType());
	}

	/**
	 * Deletes the persistent inheritance.
	 */
	public static void deleteInheritance(TLInheritance inheritance, CreateInheritanceHandler createHandler) {
		try (Transaction transaction = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			TLClass source = inheritance.getSource();
			List<TLClass> generalizations = source.getGeneralizations();
			generalizations.remove(inheritance.getTarget());
			if (generalizations.isEmpty()) {
				createHandler.createInheritance(source, TLModelUtil.tlObjectType(source.getModel()));
			}
			transaction.commit();
		}
	}

	/**
	 * Connect to all existing specializations of the given {@link Node} in the given
	 * {@link SharedGraph}.
	 * 
	 * It is assumed that the tag of every {@link Node} is a {@link TLClass}.
	 */
	public static void connectSpecializations(LayoutContext context, GraphModel graphModel, Node node) {
		Object tag = node.getTag();

		if (tag instanceof TLClass) {
			TLClass clazz = (TLClass) tag;

			for (TLClass specialization : clazz.getSpecializations()) {
				if (isSupportedNodeType(specialization, context.getHiddenElements())) {
					GraphPart specializationNode = graphModel.getGraphPart(specialization);

					if (specializationNode instanceof Node) {
						if (!hasEdge((Node) specializationNode, node, TLInheritance.class)) {
							TLInheritanceImpl inheritance = new TLInheritanceImpl(specialization, clazz);

							if (isEdgeHidden(context.getHiddenElements(), inheritance, inheritance.getSource(),
								inheritance.getTarget())) {
								GraphModelUtil.createDiagramJSEdge(context.getLabelProvider(), graphModel, inheritance,
									(Node) specializationNode, node);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Connect to all existing specializations of the given {@link LayoutNode} in the given
	 * {@link LayoutGraph}.
	 * 
	 * It is assumed that the user object of every {@link LayoutNode} is a {@link TLClass}.
	 */
	private static void connectSpecializations(LayoutGraph graph, Map<TLType, LayoutNode> nodeMapping,
			LayoutNode node, Collection<Object> hiddenElements) {
		Object tag = node.getUserObject();

		if (tag instanceof TLClass) {
			TLClass clazz = (TLClass) tag;

			for (TLClass specialization : clazz.getSpecializations()) {
				if (isSupportedNodeType(specialization, hiddenElements)) {
					LayoutNode specializationNode = nodeMapping.get(specialization);

					if (specializationNode != null) {
						Set<LayoutEdge> edges = graph.edges(specializationNode, node);
						Set<LayoutEdge> collect = edges.stream()
							.filter(edge -> edge.getBusinessObject() instanceof TLInheritance)
							.collect(Collectors.toSet());

						if (collect.isEmpty()) {
							TLInheritanceImpl inheritance = new TLInheritanceImpl(specialization, clazz);

							if (!hiddenElements.contains(inheritance)) {
								LayoutEdge inheritanceEdge = graph.connect(specializationNode, node, inheritance);
								inheritanceEdge.setPriority(EDGE_HIGH_PRIORITY);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Connect to all existing generalizations of the given {@link Node} in the given
	 * {@link SharedGraph}.
	 * 
	 * It is assumed that the tag of every {@link Node} is a {@link TLClass}.
	 */
	public static void connectGeneralizations(LayoutContext context, GraphModel graphModel, Node node) {
		Object tag = node.getTag();

		if (tag instanceof TLClass) {
			TLClass clazz = (TLClass) tag;

			for (TLClass generalization : clazz.getGeneralizations()) {
				if (isSupportedNodeType(generalization, context.getHiddenElements())) {
					GraphPart generalizationNode = graphModel.getGraphPart(generalization);

					if (generalizationNode instanceof Node) {
						if (!hasEdge(node, (Node) generalizationNode, TLInheritance.class)) {
							TLInheritanceImpl inheritance = new TLInheritanceImpl(clazz, generalization);

							if (isEdgeHidden(context.getHiddenElements(), inheritance, inheritance.getSource(),
								inheritance.getTarget())) {
								GraphModelUtil.createDiagramJSEdge(context.getLabelProvider(), graphModel, inheritance,
									node, (Node) generalizationNode);
							}
						}
					}
				}
			}
		}
	}

	private static boolean hasEdge(Node source, Node target, Class<?> clazz) {
		return source.getOutgoingEdges().stream().anyMatch(edge -> {
			return edge.getDestination() == target && clazz.isInstance(edge.getTag());
		});
	}

	/**
	 * Connect to all existing generalizations of the given {@link LayoutNode} in the given
	 * {@link LayoutGraph}.
	 * 
	 * It is assumed that the user object of every {@link LayoutNode} is a {@link TLClass}.
	 */
	private static void connectGeneralizations(LayoutGraph graph, Map<TLType, LayoutNode> nodeMapping,
			LayoutNode node, Collection<Object> hiddenElements) {
		Object tag = node.getUserObject();

		if (tag instanceof TLClass) {
			TLClass clazz = (TLClass) tag;

			for (TLClass generalization : clazz.getGeneralizations()) {
				if (isSupportedNodeType(generalization, hiddenElements)) {
					LayoutNode generalizationNode = nodeMapping.get(generalization);

					if (generalizationNode != null) {
						Set<LayoutEdge> edges = graph.edges(node, generalizationNode);
						Set<LayoutEdge> collect = edges.stream()
							.filter(edge -> edge.getBusinessObject() instanceof TLInheritance)
							.collect(Collectors.toSet());

						if (collect.isEmpty()) {
							TLInheritanceImpl inheritance = new TLInheritanceImpl(clazz, generalization);

							if (!hiddenElements.contains(inheritance)) {
								LayoutEdge inheritanceEdge = graph.connect(node, generalizationNode, inheritance);
								inheritanceEdge.setPriority(EDGE_HIGH_PRIORITY);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Connect all {@link TLReference} from or to the current {@link Node}.
	 * 
	 * It is assumed that the tag of every {@link Node} is a {@link TLClass}.
	 */
	public static void connectReferences(LayoutContext context, GraphModel graphModel, Node node) {
		Object tag = node.getTag();

		if (tag instanceof TLType) {
			connectOutgoingReferences(context, graphModel, node);
			connectIncomingReferences(context, graphModel, getTLTypeTag(node));
		}
	}

	/**
	 * Connect all {@link TLReference} from or to the current {@link LayoutNode}.
	 * 
	 * It is assumed that the user object of every {@link LayoutNode} is a {@link TLClass}.
	 */
	private static void connectReferences(LayoutGraph graph, Map<TLType, LayoutNode> nodeMapping, LayoutNode node,
			Collection<Object> hiddenElements) {
		Object userObject = node.getUserObject();

		if (userObject instanceof TLType) {
			connectOutgoingReferences(graph, nodeMapping, node, hiddenElements);
			connectIncomingReferences(graph, nodeMapping, Optional.of((TLType) userObject), hiddenElements);
		}
	}

	private static void connectIncomingReferences(LayoutContext context, GraphModel graphModel,
			Optional<TLType> type) {
		getNodesStream(graphModel).forEach(sourceNode -> connectReference(context, graphModel, sourceNode, type));
	}
	
	private static void connectIncomingReferences(LayoutGraph graph, Map<TLType, LayoutNode> mapping,
			Optional<TLType> type, Collection<Object> hiddenElements) {
		graph.nodes().stream()
			.forEach(sourceNode -> connectReference(graph, mapping, sourceNode, type, hiddenElements));
	}

	private static void connectOutgoingReferences(LayoutContext context, GraphModel graphModel, Node node) {
		connectReference(context, graphModel, node, Optional.empty());
	}
	
	private static void connectOutgoingReferences(LayoutGraph graph, Map<TLType, LayoutNode> nodeMapping,
			LayoutNode node, Collection<Object> hiddenElements) {
		connectReference(graph, nodeMapping, node, Optional.empty(), hiddenElements);
	}

	private static Optional<TLType> getTLTypeTag(Node node) {
		return Optional.of((TLType) node.getTag());
	}

	/**
	 * Get the {@link Node} stream for the given {@link GraphModel}.
	 */
	private static Stream<? extends Node> getNodesStream(GraphModel graphModel) {
		return graphModel.getNodes().stream();
	}

	private static void connectReference(LayoutContext context, GraphModel graphModel, Node node,
			Optional<TLType> expectedTargetType) {
		TLStructuredType clazz = (TLStructuredType) node.getTag();

		for (TLStructuredTypePart part : clazz.getLocalParts()) {
			if (part.getModelKind() == ModelKind.REFERENCE) {
				TLType partType = part.getType();
				if (isSupportedNodeType(partType, context.getHiddenElements())) {
					TLType targetType = expectedTargetType.orElse(partType);

					if (partType == targetType) {
						GraphPart target = graphModel.getGraphPart(partType);

						if (target != null && target instanceof Node) {
							if (GraphModelUtil.isDirectReversedReference(part, partType)) {
								if (graphModel.getEdge(part) == null) {
									GraphModelUtil.createDiagramJSEdge(context.getLabelProvider(), graphModel, part,
										(Node) target,
										node);
								}
							}
						}
					}
				}
			}
		}
	}
	
	private static void connectReference(LayoutGraph graph, Map<TLType, LayoutNode> nodeMapping, LayoutNode node,
			Optional<TLType> expectedTargetType, Collection<Object> hiddenElements) {
		TLStructuredType type = (TLStructuredType) node.getUserObject();
		
		for (TLStructuredTypePart part : type.getLocalParts()) {
			if (part.getModelKind() == ModelKind.REFERENCE) {
				TLType partType = part.getType();
				if (isSupportedNodeType(partType, hiddenElements)) {
					TLType targetType = expectedTargetType.orElse(partType);
					
					if (partType == targetType) {
						LayoutNode target = nodeMapping.get(partType);
						
						if (target != null) {
							if (GraphModelUtil.isDirectReversedReference(part, partType)) {
								Set<LayoutEdge> edges = graph.edges(target, node);
								Set<LayoutEdge> collect = edges.stream()
									.filter(edge -> edge.getBusinessObject() == part).collect(Collectors.toSet());

								if (collect.isEmpty()) {
									LayoutEdge referenceEdge = graph.connect(target, node, part);
									setReferenceEdgePriority(part, referenceEdge);
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Enclosing {@link TLModule} for the untyped given model part.
	 */
	public static TLModule getEnclosingModule(Object model) {
		if (model instanceof TLModule) {
			return (TLModule) model;
		} else if (model instanceof TLType) {
			return ((TLType) model).getModule();
		} else if (model instanceof TLTypePart) {
			return ((TLTypePart) model).getOwner().getModule();
		} else if (model instanceof TLInheritance) {
			return ((TLInheritance) model).getSource().getModule();
		} else {
			return null;
		}
	}

	/**
	 * True if the given {@link TLReference} is backwards, otherwise false.
	 */
	public static boolean isBackReference(TLReference reference) {
		return TLModelUtil.getEndIndex(reference.getEnd()) == 0;
	}

}
