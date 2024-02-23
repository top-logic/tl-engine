/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.client.service.scope.listener;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gwt.core.client.JavaScriptObject;

import com.top_logic.client.diagramjs.core.Diagram;
import com.top_logic.client.diagramjs.core.TextRenderer;
import com.top_logic.client.diagramjs.model.Base;
import com.top_logic.client.diagramjs.model.Connection;
import com.top_logic.client.diagramjs.model.ConnectionOptions;
import com.top_logic.client.diagramjs.model.Label;
import com.top_logic.client.diagramjs.model.LabelOptions;
import com.top_logic.client.diagramjs.model.Shape;
import com.top_logic.client.diagramjs.model.ShapeOptions;
import com.top_logic.client.diagramjs.model.util.Bounds;
import com.top_logic.client.diagramjs.model.util.Dimension;
import com.top_logic.client.diagramjs.model.util.MathUtil;
import com.top_logic.client.diagramjs.model.util.Position;
import com.top_logic.client.diagramjs.model.util.Waypoint;
import com.top_logic.client.diagramjs.util.DiagramUtil;
import com.top_logic.client.diagramjs.util.JavaScriptObjectUtil;
import com.top_logic.graph.common.model.LabelOwner;
import com.top_logic.graph.diagramjs.model.DiagramJSEdge;
import com.top_logic.graph.diagramjs.model.impl.DefaultDiagramJSClassNode;
import com.top_logic.graph.diagramjs.model.impl.DefaultDiagramJSEdge;
import com.top_logic.graph.diagramjs.model.impl.DefaultDiagramJSLabel;
import com.top_logic.graph.diagramjs.util.GraphLayoutConstants;

/**
 * Handles creation for display graph parts.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class DisplayGraphPartCreator {

	private static final String CONNECTION_TARGET = "target";

	private static final String CONNECTION_SOURCE = "source";

	private static final String CONNECTION_TARGET_LABEL_TYPE = "target";

	private static final String CONNECTION_SOURCE_LABEL_TYPE = "source";

	private Diagram _diagram;

	/**
	 * Creates a {@link DisplayGraphPartCreator}.
	 */
	public DisplayGraphPartCreator(Diagram diagram) {
		_diagram = diagram;
	}

	enum StraightEdgeDirection {
		TOP, BOTTOM, RIGHT, LEFT
	}

	/**
	 * Creates the display label.
	 */
	public Base createLabel(DefaultDiagramJSLabel label) {
		Label displayLabel = createDisplayLabel(label);

		LabelOwner owner = label.getOwner();

		if (owner != null) {
			if (owner instanceof DefaultDiagramJSClassNode) {
				setClassPropertyLabelPosition(displayLabel, (DefaultDiagramJSClassNode) owner);
			} else if (owner instanceof DiagramJSEdge) {
				setEdgleLabelPosition(displayLabel, (DiagramJSEdge) owner);
			}
		}

		label.setTag(displayLabel);

		if (owner != null) {
			return null;
		}

		return displayLabel;
	}

	private void setEdgleLabelPosition(Label displayLabel, DiagramJSEdge owner) {
		Connection connection = (Connection) owner.getTag();
		String type = displayLabel.getType();

		if (GraphLayoutConstants.LABEL_EDGE_SOURCE_CARDINALITY_TYPE.equals(type)) {
			setSourceCardEdgeLabelPosition(displayLabel, connection);
		} else if (GraphLayoutConstants.LABEL_EDGE_SOURCE_NAME_TYPE.equals(type)) {
			setSourceNameEdgeLabelPosition(displayLabel, connection, owner);
		} else if (GraphLayoutConstants.LABEL_EDGE_TARGET_CARDINALITY_TYPE.equals(type)) {
			setTargetCardEdgeLabelPosition(displayLabel, connection);
		} else if (GraphLayoutConstants.LABEL_EDGE_TARGET_NAME_TYPE.equals(type)) {
			setTargetNameEdgeLabelPosition(displayLabel, connection, owner);
		}
	}

	private void setTargetNameEdgeLabelPosition(Label displayLabel, Connection connection, DiagramJSEdge owner) {
		boolean existMarker = existAnyMarker(connection, false);
		Waypoint[] waypoints = connection.getWaypoints();

		Waypoint connectionSource = waypoints[waypoints.length - 1];
		Waypoint connectionTarget = waypoints[waypoints.length - 2];

		StraightEdgeDirection direction = getStraightLinePartDirection(connectionSource, connectionTarget);
		Position referencePosition = getReferencePoint(connectionSource, connectionTarget, direction, existMarker);

		boolean existAnotherEdgeLabel = existEdgeLabel(owner, GraphLayoutConstants.LABEL_EDGE_TARGET_CARDINALITY_TYPE);
		int moveFactor = existAnotherEdgeLabel ? 2 : 1;

		moveReferencePosition(displayLabel, direction, referencePosition, moveFactor);

		if (existAnotherEdgeLabel) {
			adjustReferencePosition(displayLabel, direction, referencePosition);
		}

		displayLabel.getPosition().move(referencePosition.getX(), referencePosition.getY());

		moveDisplayLabel(displayLabel, referencePosition);
	}

	private void moveReferencePosition(Label displayLabel, StraightEdgeDirection direction, Position referencePosition,
			int moveFactor) {
		if (direction == StraightEdgeDirection.LEFT) {
			referencePosition.move(moveFactor * -displayLabel.getWidth(), 0);
		} else if (direction == StraightEdgeDirection.TOP) {
			referencePosition.move(0, moveFactor * -displayLabel.getHeight());
		}
		referencePosition.move(-displayLabel.getX(), -displayLabel.getY());
	}

	private void adjustReferencePosition(Label displayLabel, StraightEdgeDirection direction, Position referencePosition) {
		if (direction == StraightEdgeDirection.LEFT) {
			referencePosition.move(-5, 0);
		} else if (direction == StraightEdgeDirection.RIGHT) {
			referencePosition.move(5 + displayLabel.getWidth(), 0);
		} else if (direction == StraightEdgeDirection.TOP) {
			referencePosition.move(0, -5);
		} else if (direction == StraightEdgeDirection.BOTTOM) {
			referencePosition.move(0, 5 + displayLabel.getHeight());
		}
	}

	private boolean existEdgeLabel(DiagramJSEdge owner, String labelType) {
		Set<String> labelTypes = owner.getLabels().stream().map(label -> label.getType()).collect(Collectors.toSet());

		return labelTypes.contains(labelType);
	}

	private void setTargetCardEdgeLabelPosition(Label displayLabel, Connection connection) {
		boolean existMarker = existAnyMarker(connection, false);
		Waypoint[] waypoints = connection.getWaypoints();

		Waypoint connectionSource = waypoints[waypoints.length - 1];
		Waypoint connectionTarget = waypoints[waypoints.length - 2];

		StraightEdgeDirection direction = getStraightLinePartDirection(connectionSource, connectionTarget);
		Position referencePosition = getReferencePoint(connectionSource, connectionTarget, direction, existMarker);

		moveReferencePosition(displayLabel, direction, referencePosition, 1);

		displayLabel.getPosition().move(referencePosition.getX(), referencePosition.getY());

		moveDisplayLabel(displayLabel, referencePosition);
	}

	private void setSourceCardEdgeLabelPosition(Label displayLabel, Connection connection) {
		boolean existMarker = existAnyMarker(connection, true);
		Waypoint[] waypoints = connection.getWaypoints();

		Waypoint connectionSource = waypoints[0];
		Waypoint connectionTarget = waypoints[1];

		StraightEdgeDirection direction = getStraightLinePartDirection(connectionSource, connectionTarget);
		Position referencePosition = getReferencePoint(connectionSource, connectionTarget, direction, existMarker);

		moveReferencePosition(displayLabel, direction, referencePosition, 1);

		moveDisplayLabel(displayLabel, referencePosition);
	}

	private void setSourceNameEdgeLabelPosition(Label displayLabel, Connection connection, DiagramJSEdge owner) {
		boolean existMarker = existAnyMarker(connection, true);
		Waypoint[] waypoints = connection.getWaypoints();

		Waypoint connectionSource = waypoints[0];
		Waypoint connectionTarget = waypoints[1];

		StraightEdgeDirection direction = getStraightLinePartDirection(connectionSource, connectionTarget);
		Position referencePosition = getReferencePoint(connectionSource, connectionTarget, direction, existMarker);

		boolean existAnotherEdgeLabel = existEdgeLabel(owner, GraphLayoutConstants.LABEL_EDGE_SOURCE_CARDINALITY_TYPE);
		int moveFactor = existAnotherEdgeLabel ? 2 : 1;

		moveReferencePosition(displayLabel, direction, referencePosition, moveFactor);

		if (existAnotherEdgeLabel) {
			adjustReferencePosition(displayLabel, direction, referencePosition);
		}

		moveDisplayLabel(displayLabel, referencePosition);
	}

	private void moveDisplayLabel(Label displayLabel, Position deltaPosition) {
		Position labelPosition = displayLabel.getPosition();

		labelPosition.move(deltaPosition.getX(), deltaPosition.getY());

		displayLabel.setX(labelPosition.getX());
		displayLabel.setY(labelPosition.getY());
	}

	private Position getReferencePoint(Waypoint connectionSource, Waypoint connectionTarget,
			StraightEdgeDirection direction, boolean existMarker) {
		Position source = transform(connectionSource);
		Position target = transform(connectionTarget);

		Position referencePoint = MathUtil.getAbsoluteClosePoint(source, target, getCardinalityDistance(existMarker));

		if (isHorizontal(direction)) {
			referencePoint.move(0, 5);
		} else {
			referencePoint.move(5, 0);
		}

		return referencePoint;
	}

	private Position transform(Waypoint waypoint) {
		Position position = JavaScriptObject.createObject().cast();

		position.setX(waypoint.getX());
		position.setY(waypoint.getY());

		return position;
	}

	private double getCardinalityDistance(boolean existMarker) {
		if (existMarker) {
			return 15;
		}

		return 5;
	}

	private boolean existAnyMarker(Connection connection, boolean isSourceSide) {
		String connectionType = connection.getType();

		if (isSourceSide) {
			return GraphLayoutConstants.EDGE_AGGREGATION_TYPE.equals(connectionType)
				|| GraphLayoutConstants.EDGE_COMPOSITION_TYPE.equals(connectionType);
		} else {
			return GraphLayoutConstants.EDGE_ASSOCIATION_TYPE.equals(connectionType)
				|| GraphLayoutConstants.EDGE_INHERITANCE_TYPE.equals(connectionType);
		}
	}

	private StraightEdgeDirection getStraightLinePartDirection(Waypoint connectionSource, Waypoint connectionTarget) {
		if (connectionSource.getY() == connectionTarget.getY()) {
			return getHorizontalDirection(connectionSource, connectionTarget);
		} else {
			return getVerticalDirection(connectionSource, connectionTarget);
		}
	}

	private StraightEdgeDirection getVerticalDirection(Waypoint connectionSource, Waypoint connectionTarget) {
		if (connectionSource.getY() < connectionTarget.getY()) {
			return StraightEdgeDirection.BOTTOM;
		} else {
			return StraightEdgeDirection.TOP;
		}
	}

	private StraightEdgeDirection getHorizontalDirection(Waypoint connectionSource, Waypoint connectionTarget) {
		if (connectionSource.getX() < connectionTarget.getX()) {
			return StraightEdgeDirection.RIGHT;
		} else {
			return StraightEdgeDirection.LEFT;
		}
	}

	private boolean isHorizontal(StraightEdgeDirection direction) {
		return direction == StraightEdgeDirection.LEFT || direction == StraightEdgeDirection.RIGHT;
	}

	/**
	 * Creates the display label.
	 */
	protected Label createDisplayLabel(DefaultDiagramJSLabel sharedLabel) {
		Optional<LabelOptions> labelOptions =
			createLabelOptions(sharedLabel.getType(), sharedLabel, sharedLabel.isVisible());

		Base labelOwner = getLabelOwner(sharedLabel);
		String text = sharedLabel.getText();

		return createDisplayLabel(getBounds(text), text, labelOptions, labelOwner);
	}

	void setClassPropertyLabelPosition(Label label, DefaultDiagramJSClassNode classNode) {
		Shape clazz = (Shape) classNode.getTag();

		double y = getNextLabelAbsoluteYCoordinate(classNode);

		label.setX(clazz.getX() + 5);
		label.setY(y);

		double newHeight = getRelativeYCoordinate(clazz, label) + label.getHeight();
		if (newHeight > clazz.getHeight()) {
			clazz.setHeight(newHeight);
			clazz.setIsResized(true);
		}

		double newWidth = 5 + label.getWidth();
		if (newWidth > clazz.getWidth()) {
			clazz.setWidth(newWidth);
			clazz.setIsResized(true);
		}
	}

	private double getRelativeYCoordinate(Shape parent, Label child) {
		return child.getY() - parent.getY();
	}

	private double getNextLabelAbsoluteYCoordinate(DefaultDiagramJSClassNode classNode) {
		Shape clazz = (Shape) classNode.getTag();

		double y = clazz.getY() + 5;

		y += getClassNameHeight(classNode) + 5;
		y += getStereotypesHeight(classNode);
		y += getPropertyLabelsHeight(clazz);

		return y;
	}

	private double getStereotypesHeight(DefaultDiagramJSClassNode classNode) {
		List<String> stereotypes = classNode.getStereotypes();

		if (stereotypes != null) {
			return stereotypes.stream().map(stereotype -> _diagram.getTextRenderer().getHeight(stereotype)).reduce(0.,
				Double::sum);
		}

		return 0.;
	}

	private double getPropertyLabelsHeight(Shape clazz) {
		Label[] displayLabels = clazz.getLabels();
		double y = 0.;

		for (int i = 0; i < displayLabels.length - 1; i++) {
			y += displayLabels[i].getHeight();
		}

		return y;
	}

	private double getClassNameHeight(DefaultDiagramJSClassNode classNode) {
		return _diagram.getTextRenderer().getHeight(classNode.getClassName());
	}

	private Bounds getBounds(String text) {
		Bounds bounds = JavaScriptObject.createObject().cast();

		bounds.setX(0);
		bounds.setY(0);
		bounds.setDimension(getTextDimensions(text));

		return bounds;
	}

	private Dimension getTextDimensions(String text) {
		return _diagram.getTextRenderer().getDimensions(text);
	}

	private Label createDisplayLabel(Bounds bounds, String text, Optional<LabelOptions> options, Base owner) {
		JavaScriptObject attributes = JavaScriptObject.createObject();

		putBounds(attributes, bounds);
		putText(attributes, text);
		putOptions(attributes, options);
		putLabelOwner(attributes, owner);

		return _diagram.getElementFactory().createLabel(attributes);
	}

	private void putOptions(JavaScriptObject attributes, Optional<? extends JavaScriptObject> options) {
		options.ifPresent(optionsObject -> JavaScriptObjectUtil.assign(attributes, optionsObject));
	}

	private Base getLabelOwner(DefaultDiagramJSLabel label) {
		return (Base) label.getOwner().getTag();
	}

	private void putBounds(JavaScriptObject object, Bounds bounds) {
		JavaScriptObjectUtil.assign(object, bounds);
	}

	private void putText(JavaScriptObject object, String text) {
		JavaScriptObjectUtil.put(object, "businessObject", text);
	}

	private void putLabelOwner(JavaScriptObject object, Base owner) {
		JavaScriptObjectUtil.put(object, "labelTarget", owner);
	}

	/**
	 * Creates the display node.
	 */
	public Base createNode(DefaultDiagramJSClassNode node) {
		Shape displayNode = createDisplayNode(node);

		node.setTag(displayNode);

		return displayNode;
	}

	/**
	 * Creates the display node.
	 */
	protected Shape createDisplayNode(DefaultDiagramJSClassNode sharedNode) {
		Optional<ShapeOptions> options = Optional.of(createShapeOptions(sharedNode));

		return createDisplayNode(getBounds(sharedNode), sharedNode.getClassName(), options);
	}

	private ShapeOptions createShapeOptions(DefaultDiagramJSClassNode sharedNode) {
		ShapeOptions options = JavaScriptObject.createObject().cast();

		options.setSharedGraphPart(sharedNode);
		options.setImported(sharedNode.isImported());
		options.setVisibility(sharedNode.isVisible());

		setModifiers(sharedNode, options);
		setStereotypes(sharedNode, options);

		return options;
	}

	private void setStereotypes(DefaultDiagramJSClassNode node, ShapeOptions options) {
		List<String> stereotypes = node.getStereotypes();

		if (stereotypes != null) {
			options.setStereotypes(stereotypes.toArray(new String[stereotypes.size()]));
		}
	}

	private void setModifiers(DefaultDiagramJSClassNode node, ShapeOptions options) {
		List<String> classModifiers = node.getClassModifiers();

		if (classModifiers != null) {
			options.setModifiers(classModifiers);
		}
	}

	private Bounds getBounds(DefaultDiagramJSClassNode node) {
		Bounds bounds = JavaScriptObject.createObject().cast();

		bounds.setX(node.getX());
		bounds.setY(node.getY());
		bounds.setWidth(getClassNodeWidth(node));
		bounds.setHeight(getClassNodeHeight(node));

		return bounds;
	}

	private Shape createDisplayNode(Bounds bounds, String name, Optional<ShapeOptions> options) {
		JavaScriptObject attributes = JavaScriptObject.createObject();

		putBounds(attributes, bounds);
		putName(attributes, name);
		putOptions(attributes, options);

		return _diagram.getElementFactory().createClass(attributes);
	}

	private void putName(JavaScriptObject object, String name) {
		JavaScriptObjectUtil.put(object, "name", name);
	}

	private double getClassNodeHeight(DefaultDiagramJSClassNode node) {
		return node.getHeight() == 0 ? _diagram.getTextRenderer().getHeight(node.getClassName()) + 5 : node.getHeight();
	}

	private double getClassNodeWidth(DefaultDiagramJSClassNode node) {
		double width = node.getWidth();

		return width == 0 ? calculateClassNodeWidth(node) : width;
	}

	private double calculateClassNodeWidth(DefaultDiagramJSClassNode node) {
		return Math.max(getClassNodeNameWidth(node), getClassNodeStereotypesWidth(node).orElse(0.0)) + 5;
	}

	private double getClassNodeNameWidth(DefaultDiagramJSClassNode node) {
		TextRenderer textRenderer = _diagram.getTextRenderer();

		return textRenderer.getWidth(node.getClassName());
	}

	private Optional<Double> getClassNodeStereotypesWidth(DefaultDiagramJSClassNode node) {
		TextRenderer textRenderer = _diagram.getTextRenderer();
		List<String> stereotypes = node.getStereotypes();

		if (stereotypes != null) {
			return stereotypes.stream().map(stereotype -> textRenderer.getWidth("<<" + stereotype + ">>")).max(Double::compare);
		}

		return Optional.empty();
	}

	/**
	 * Creates the display edge.
	 */
	public Base createEdge(DefaultDiagramJSEdge edge) {
		Connection displayEdge = createDisplayEdge(edge);

		if (displayEdge.getWaypoints() == null) {
			Waypoint[] newWaypoints = _diagram.getLayouter().layoutConnection(displayEdge);

			displayEdge.setWaypoints(newWaypoints);
		}

		createDisplayEdgeLabels(edge, displayEdge);

		edge.setTag(displayEdge);

		return displayEdge;
	}

	/**
	 * Creates the display edge.
	 */
	protected Connection createDisplayEdge(DefaultDiagramJSEdge sharedEdge) {
		Shape source = (Shape) sharedEdge.getSource().getTag();
		Shape target = (Shape) sharedEdge.getDestination().getTag();

		if (GraphLayoutConstants.EDGE_INHERITANCE_TYPE.equals(sharedEdge.getType())) {
			return createDisplayEdge(source, target, Optional.of(getConnectionOptions(sharedEdge)));
		} else {
			return createDisplayEdge(target, source, Optional.of(getRevConnectionOptions(sharedEdge)));
		}
	}

	private ConnectionOptions getConnectionOptions(DefaultDiagramJSEdge sharedEdge) {
		ConnectionOptions options = JavaScriptObject.createObject().cast();

		options.setSharedGraphPart(sharedEdge);
		options.setType(sharedEdge.getType());
		options.setVisibility(sharedEdge.isVisible());
		getEdgeWaypoints(sharedEdge).ifPresent(waypoints -> options.setWaypoints(waypoints));

		return options;
	}

	private ConnectionOptions getRevConnectionOptions(DefaultDiagramJSEdge sharedEdge) {
		ConnectionOptions options = JavaScriptObject.createObject().cast();

		options.setSharedGraphPart(sharedEdge);
		options.setType(sharedEdge.getType());
		options.setVisibility(sharedEdge.isVisible());
		getEdgeWaypoints(sharedEdge).ifPresent(waypoints -> options.setWaypoints(getReversedWaypoints(waypoints)));

		return options;
	}

	private Waypoint[] getReversedWaypoints(Waypoint[] waypoints) {
		List<Waypoint> waypointsList = Arrays.asList(waypoints);

		Collections.reverse(waypointsList);

		return waypointsList.toArray(new Waypoint[waypointsList.size()]);
	}

	private Optional<Waypoint[]> getEdgeWaypoints(DefaultDiagramJSEdge edge) {
		Optional<List<List<Double>>> waypoints = Optional.ofNullable(edge.getWaypoints());

		return waypoints.map(waypointsInternal -> DiagramUtil.transformWaypointsBack(waypointsInternal));
	}

	private Connection createDisplayEdge(Shape source, Shape target, Optional<ConnectionOptions> options) {
		JavaScriptObject attributes = JavaScriptObject.createObject();

		putEdgeSource(attributes, source);
		putEdgeTarget(attributes, target);
		putOptions(attributes, options);

		return _diagram.getElementFactory().createConnection(attributes);
	}

	private void putEdgeSource(JavaScriptObject attributes, Shape source) {
		JavaScriptObjectUtil.put(attributes, CONNECTION_SOURCE, source);
	}

	private void putEdgeTarget(JavaScriptObject attributes, Shape target) {
		JavaScriptObjectUtil.put(attributes, CONNECTION_TARGET, target);
	}

	void createDisplayEdgeLabels(DefaultDiagramJSEdge edge, Connection displayEdge) {
		createDisplayEdgeSourceLabels(edge, displayEdge);
		createDisplayEdgeTargetLabels(edge, displayEdge);
	}

	private void createDisplayEdgeTargetLabels(DefaultDiagramJSEdge edge, Connection displayEdge) {
		if (edge.getTargetName() != null) {
			createDisplayEdgeTargetLabel(edge.getTargetName(), displayEdge);
		}

		if (edge.getTargetCardinality() != null) {
			createDisplayEdgeTargetLabel(edge.getTargetCardinality(), displayEdge);
		}
	}

	private void createDisplayEdgeSourceLabels(DefaultDiagramJSEdge edge, Connection displayEdge) {
		if (edge.getSourceName() != null) {
			createDisplayEdgeSourceLabel(edge.getSourceName(), displayEdge);
		}

		if (edge.getSourceCardinality() != null) {
			createDisplayEdgeSourceLabel(edge.getSourceCardinality(), displayEdge);
		}
	}

	private Label createDisplayEdgeSourceLabel(String text, Connection owner) {
		return createDisplayEdgeLabel(text, CONNECTION_SOURCE_LABEL_TYPE, owner);
	}

	private Label createDisplayEdgeTargetLabel(String text, Connection owner) {
		return createDisplayEdgeLabel(text, CONNECTION_TARGET_LABEL_TYPE, owner);
	}

	private Label createDisplayEdgeLabel(String text, String type, Connection owner) {
		DefaultDiagramJSEdge sharedEdge = (DefaultDiagramJSEdge) owner.getSharedGraphPart();

		return createDisplayLabel(getBounds(text), text, createLabelOptions(type, null, sharedEdge.isVisible()), owner);
	}

	private Optional<LabelOptions> createLabelOptions(String type, Object sharedLabel, boolean isVisible) {
		LabelOptions options = JavaScriptObject.createObject().cast();

		if (type != null && !type.isEmpty()) {
			options.setType(type);
		}

		if (sharedLabel != null) {
			options.setSharedGraphPart(sharedLabel);
		}

		options.setVisibility(isVisible);

		return Optional.of(options);
	}

}
