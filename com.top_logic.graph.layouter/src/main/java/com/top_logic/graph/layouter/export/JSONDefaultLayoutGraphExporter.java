/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.export;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.json.JSON;
import com.top_logic.graph.layouter.model.LayoutGraph;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutEdge;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutNode;
import com.top_logic.graph.layouter.model.Waypoint;
import com.top_logic.graph.layouter.model.util.LayoutGraphUtil;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassPart;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelUtil;

/**
 * Default JSON graph exporter. Adds businessObjects to nodes and edges. In addition it generates
 * cardinalities out of the edge's businessObject.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class JSONDefaultLayoutGraphExporter implements JSONGraphExporter<LayoutGraph> {

	private static final String NODE_BUSINESSOBJECT_PROPERTIES_ATTRIBUTE = "properties";

	private static final String NODE_FINAL_MODIFIER = "final";

	private static final String NODE_ABSTRACT_MODIFIER = "abstract";

	private static final String EDGE_COMPOSITION_TYPE = "composition";

	private static final String EDGE_AGGREGATION_TYPE = "aggregation";

	private static final String GRAPH_ATTRIBUTE = "graph";

	private static final String GRAPH_EDGES_ATTRIBUTE = "edges";

	private static final String GRAPH_NODES_ATTRIBUTE = "nodes";

	private static final String NODE_BUSINESSOBJECT_NAME_ATTRIBUTE = "name";

	private static final String EDGE_ASSOCIATION_TYPE = "association";

	private static final String EDGE_INHERITANCE_TYPE = "inheritance";

	private static final String EDGE_TYPE_ATTRIBUTE = "type";

	private static final String EDGE_TARGET_CARDINALITY_ATTRIBUTE = "targetCardinality";

	private static final String EDGE_SOURCE_CARDINALITY_ATTRIBUTE = "sourceCardinality";

	private static final String EDGE_TARGET_NAME_ATTRIBUTE = "targetName";

	private static final String EDGE_SOURCE_NAME_ATTRIBUTE = "sourceName";

	private static final String EDGE_WAYPOINTS_ATTRIBUTE = "waypoints";

	private static final String EDGE_TARGET_ATTRIBUTE = "target";

	private static final String EDGE_SOURCE_ATTRIBUTE = "source";

	private static final String NODE_BUSINESS_OBJECT_ATTRIBUTE = "businessObject";

	private static final String NODE_WIDTH_ATTRIBUTE = "width";

	private static final String NODE_HEIGHT_ATTRIBUTE = "height";

	private static final String NODE_Y_ATTRIBUTE = "y";

	private static final String NODE_X_ATTRIBUTE = "x";

	private static final String NODE_ID_ATTRIBUTE = "id";

	private static final String NODE_BUSINESSOBJECT_MODIFIERS_ATTRIBUTE = "modifiers";

	private static final ResourceProvider _resourceProvider = MetaResourceProvider.INSTANCE;

	@Override
	public String export(LayoutGraph layoutGraph) {
		LinkedHashMap<String, Object> diagram = new LinkedHashMap<>();

		LinkedHashMap<String, Object> graph = new LinkedHashMap<>();

		LinkedList<Object> nodes = new LinkedList<>();
		LinkedList<Object> edges = new LinkedList<>();

		Map<LayoutNode, String> nodeIDMap = new LinkedHashMap<>();

		for (LayoutNode layoutNode : layoutGraph.nodes()) {
			LinkedHashMap<String, Object> node = new LinkedHashMap<>();

			String nodeID = StringServices.randomUUID();

			node.put(NODE_ID_ATTRIBUTE, nodeID);
			node.put(NODE_X_ATTRIBUTE, layoutNode.getX());
			node.put(NODE_Y_ATTRIBUTE, layoutNode.getY());
			node.put(NODE_HEIGHT_ATTRIBUTE, layoutNode.getHeight());
			node.put(NODE_WIDTH_ATTRIBUTE, layoutNode.getWidth());
			node.put(NODE_BUSINESS_OBJECT_ATTRIBUTE, getBusinessObject(layoutNode));

			nodes.add(node);

			nodeIDMap.put(layoutNode, nodeID);
		}

		for (LayoutNode layoutNode : layoutGraph.nodes()) {
			for (LayoutEdge layoutEdge : layoutNode.outgoingEdges()) {
				LinkedHashMap<String, Object> edge = new LinkedHashMap<>();

				edge.put(EDGE_SOURCE_ATTRIBUTE, nodeIDMap.get(layoutNode));
				edge.put(EDGE_TARGET_ATTRIBUTE, nodeIDMap.get(layoutEdge.target()));
				edge.put(EDGE_WAYPOINTS_ATTRIBUTE, transformWaypointsToJSONFormat(layoutEdge.getWaypoints()));

				addCardinalities(layoutEdge, edge);
				addNames(layoutEdge, edge);

				edge.put(EDGE_TYPE_ATTRIBUTE, getEdgeType(layoutEdge));

				edges.add(edge);
			}
		}

		graph.put(GRAPH_NODES_ATTRIBUTE, nodes);
		graph.put(GRAPH_EDGES_ATTRIBUTE, edges);

		diagram.put(GRAPH_ATTRIBUTE, graph);

		return JSON.toString(diagram);
	}

	private void addNames(LayoutEdge layoutEdge, LinkedHashMap<String, Object> edge) {
		getSourceName(layoutEdge).ifPresent(name -> {
			edge.put(EDGE_SOURCE_NAME_ATTRIBUTE, name);
		});

		getTargetName(layoutEdge).ifPresent(name -> {
			edge.put(EDGE_TARGET_NAME_ATTRIBUTE, name);
		});
	}

	private void addCardinalities(LayoutEdge layoutEdge, LinkedHashMap<String, Object> edge) {
		getSourceCardinality(layoutEdge).ifPresent(cardinality -> {
			edge.put(EDGE_SOURCE_CARDINALITY_ATTRIBUTE, cardinality);
		});

		getTargetCardinality(layoutEdge).ifPresent(cardinality -> {
			edge.put(EDGE_TARGET_CARDINALITY_ATTRIBUTE, cardinality);
		});
	}

	private Optional<String> getTargetName(LayoutEdge edge) {
		Object businessObject = edge.getBusinessObject();

		if (businessObject != null) {
			TLStructuredTypePart part = (TLStructuredTypePart) businessObject;

			return Optional.of(LayoutGraphUtil.getLabel(_resourceProvider, part));
		}

		return Optional.empty();
	}

	private Optional<String> getSourceName(LayoutEdge edge) {
		Object businessObject = edge.getBusinessObject();

		if (businessObject != null) {
			TLReference reference = (TLReference) businessObject;
			
			TLAssociationEnd otherEnd = TLModelUtil.getOtherEnd(reference.getEnd());
			TLReference endReference = otherEnd.getReference();

			if (endReference != null) {
				return Optional.of(LayoutGraphUtil.getLabel(_resourceProvider, endReference));
			}
		}

		return Optional.empty();
	}

	private Optional<String> getSourceCardinality(LayoutEdge edge) {
		Object businessObject = edge.getBusinessObject();

		if (businessObject != null) {
			TLReference reference = (TLReference) businessObject;
			TLAssociationEnd otherEnd = TLModelUtil.getOtherEnd(reference.getEnd());

			return Optional.of(LayoutGraphUtil.getCardinality(otherEnd));
		}

		return Optional.empty();
	}

	private Optional<String> getTargetCardinality(LayoutEdge edge) {
		Object businessObject = edge.getBusinessObject();

		if (businessObject != null) {
			TLStructuredTypePart part = (TLStructuredTypePart) businessObject;

			return Optional.of(LayoutGraphUtil.getCardinality(part));
		}

		return Optional.empty();
	}

	private LinkedHashMap<String, Object> getBusinessObject(LayoutNode layoutNode) {
		LinkedHashMap<String, Object> businessObject = new LinkedHashMap<>();
		TLClass userObject = (TLClass) layoutNode.getUserObject();

		businessObject.put(NODE_BUSINESSOBJECT_NAME_ATTRIBUTE, LayoutGraphUtil.getLabel(_resourceProvider, userObject));
		businessObject.put(NODE_BUSINESSOBJECT_MODIFIERS_ATTRIBUTE, getModifiers(userObject));
		businessObject.put(NODE_BUSINESSOBJECT_PROPERTIES_ATTRIBUTE, getProperties(userObject));

		return businessObject;
	}

	private List<String> getProperties(TLClass clazz) {
		List<String> properties = new LinkedList<>();

		for (TLClassPart clazzPart : clazz.getLocalClassParts()) {
			if (clazzPart.getModelKind() == ModelKind.PROPERTY) {
				properties.add(LayoutGraphUtil.getLabel(_resourceProvider, clazzPart));
			}
		}

		return properties;
	}

	private List<String> getModifiers(TLClass clazz) {
		List<String> modifiers = new LinkedList<>();

		if (clazz.isAbstract()) {
			modifiers.add(NODE_ABSTRACT_MODIFIER);
		}

		if (clazz.isFinal()) {
			modifiers.add(NODE_FINAL_MODIFIER);
		}

		return modifiers;
	}

	private Object getEdgeType(LayoutEdge layoutEdge) {
		Object businessObject = layoutEdge.getBusinessObject();

		if (businessObject == null) {
			return EDGE_INHERITANCE_TYPE;
		} else {
			return getEdgeReferenceType((TLReference) businessObject);
		}
	}

	private Object getEdgeReferenceType(TLReference reference) {
		TLAssociationEnd end = reference.getEnd();

		if (end.isAggregate()) {
			return EDGE_AGGREGATION_TYPE;
		} else if (end.isComposite()) {
			return EDGE_COMPOSITION_TYPE;
		} else {
			return EDGE_ASSOCIATION_TYPE;
		}
	}

	private List<Map<String, Double>> transformWaypointsToJSONFormat(List<Waypoint> waypoints) {
		return waypoints.stream().map(waypoint -> {
			Map<String, Double> coordinates = new LinkedHashMap<>();

			coordinates.put(NODE_X_ATTRIBUTE, waypoint.getX());
			coordinates.put(NODE_Y_ATTRIBUTE, waypoint.getY());

			return coordinates;
		}).collect(Collectors.toList());
	}
}
