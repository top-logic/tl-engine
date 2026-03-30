/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.model.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import com.top_logic.graph.layouter.DiagramJSLayoutContext;
import com.top_logic.graph.layouter.GraphConstants;
import com.top_logic.graph.layouter.LayoutDirection;
import com.top_logic.graph.layouter.algorithm.node.size.DefaultNodeSizer;
import com.top_logic.graph.layouter.math.util.MathUtil;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutEdge;
import com.top_logic.graph.layouter.model.NodePort;
import com.top_logic.graph.layouter.text.util.DiagramTextRenderingUtil;
import com.top_logic.layout.LabelProvider;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassPart;
import com.top_logic.model.TLClassProperty;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.util.TLModelUtil;

/**
 * TLModel-specific utility methods for the layout graph, extracted from {@link LayoutGraphUtil}.
 *
 * <p>
 * These methods depend on {@link DiagramJSLayoutContext} and the TLModel API and are therefore
 * located in the diagramjs.server module, not in the GWT-compatible tl-graph-layouter module.
 * </p>
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven F&ouml;rster</a>
 */
public class DiagramJSLayoutGraphUtil implements LayoutGraphUtilConstants {

	private static final String ENUMERATION_STEREOTYPE = "<<enumeration>>";

	/**
	 * Sum of each {@link TLTypePart} property heights.
	 */
	public static Double getNodeAttributesHeight(DiagramJSLayoutContext context, TLType type) {
		return getNodeAttributesHeightStream(context, type).reduce(0., Double::sum);
	}

	/**
	 * Maximal {@link TLTypePart} property width, 0 if no property exists.
	 */
	public static Double getNodeAttributesWidth(DiagramJSLayoutContext context, TLType type) {
		return getNodeAttributesWidthStream(context, type).reduce(Double::max).orElse(0.);
	}

	/**
	 * Label of this {@link TLTypePart}. Property representation in form of "name :
	 *         typeName".
	 */
	public static String getLabel(LabelProvider labelProvider, TLTypePart part) {
		if (part instanceof TLClassProperty) {
			return labelProvider.getLabel(part) + " : " + labelProvider.getLabel(part.getType());
		}

		return labelProvider.getLabel(part);
	}

	/**
	 * Label of this {@link TLType}.
	 */
	public static String getLabel(LabelProvider labelProvider, TLType type) {
		return labelProvider.getLabel(type);
	}

	/**
	 * Stream of {@link TLTypePart} attributes for the given node.
	 */
	public static Stream<? extends TLTypePart> getNodeAttributesStream(TLType type, Collection<Object> hiddenElements) {
		if (type instanceof TLClass) {
			return ((TLClass) type).getLocalClassParts().stream()
				.filter(part -> isTLProperty(part) && !hiddenElements.contains(part));
		} else if (type instanceof TLEnumeration) {
			return ((TLEnumeration) type).getClassifiers().stream().filter(part -> !hiddenElements.contains(part));
		} else {
			return null;
		}
	}

	private static boolean isTLProperty(TLClassPart clazzPart) {
		return clazzPart.getModelKind() == ModelKind.PROPERTY;
	}

	private static Stream<Double> getNodeAttributesHeightStream(DiagramJSLayoutContext context, TLType type) {
		return getNodeAttributesStream(type, context.getHiddenElements()).map(attributeHeightCalculater(context));
	}

	private static Stream<Double> getNodeAttributesWidthStream(DiagramJSLayoutContext context, TLType type) {
		return getNodeAttributesStream(type, context.getHiddenElements()).map(nodeAttributeWidthCalculater(context));
	}

	private static Function<? super TLTypePart, ? extends Double> attributeHeightCalculater(
			DiagramJSLayoutContext context) {
		return attribute -> DiagramTextRenderingUtil.getTextHeight(getLabel(context.getLabelProvider(), attribute));
	}

	private static Function<? super TLTypePart, ? extends Double> nodeAttributeWidthCalculater(
			DiagramJSLayoutContext context) {
		return attribute -> DiagramTextRenderingUtil.getTextWidth(getLabel(context.getLabelProvider(), attribute));
	}

	/**
	 * String representation of the cardinality of this {@link TLStructuredTypePart}.
	 */
	public static String getCardinality(TLStructuredTypePart part) {
		boolean isMultiple = part.isMultiple();
		boolean isMandatory = part.isMandatory();

		if (isMultiple && isMandatory) {
			return ONE_CARDINALITY + CARDINALITY_POINTS + ARBITRARY_CARDINALITY;
		} else if (isMultiple && !isMandatory) {
			return ARBITRARY_CARDINALITY;
		} else if (!isMultiple && isMandatory) {
			return ONE_CARDINALITY;
		} else {
			return ZERO_CARDINALITY + CARDINALITY_POINTS + ONE_CARDINALITY;
		}
	}

	/**
	 * Get the maximal label width for the given outgoing {@link NodePort}.
	 */
	public static double getBottomPortLabelWidth(DiagramJSLayoutContext context, NodePort port, double defaultWidth) {
		if (context.getDirection() == LayoutDirection.VERTICAL_FROM_SOURCE) {
			return getReferenceEdgesStream(port).map(edge -> {
				if (edge.isReversed()) {
					return getEdgeSourceLabelWidth(context, edge);
				} else {
					return getEdgeTargetLabelWidth(context, edge);
				}
			}).reduce(Double::max).orElse(defaultWidth);
		} else {
			return getReferenceEdgesStream(port).map(edge -> {
				if (edge.isReversed()) {
					return getEdgeTargetLabelWidth(context, edge);
				} else {
					return getEdgeSourceLabelWidth(context, edge);
				}
			}).reduce(Double::max).orElse(defaultWidth);
		}
	}

	/**
	 * Get the maximal label width for the given outgoing {@link NodePort}.
	 */
	public static double getOutgoingPortLabelWidth(DiagramJSLayoutContext context, NodePort port, double defaultWidth) {
		return getReferenceEdgesStream(port).map(edge -> {
			if (edge.isReversed()) {
				return getEdgeSourceLabelWidth(context, edge);
			} else {
				return getEdgeTargetLabelWidth(context, edge);
			}
		}).reduce(Double::max).orElse(defaultWidth);
	}

	private static double getEdgeSourceLabelWidth(DiagramJSLayoutContext context, LayoutEdge edge) {
		TLAssociationEnd otherEnd = TLModelUtil.getOtherEnd(((TLReference) edge.getBusinessObject()).getEnd());

		double textWidth = otherEnd.getReference() != null ? getLabelWidth(context, otherEnd.getReference()) : 0;

		return Math.max(getCardinalityWidth(otherEnd), textWidth);
	}

	/**
	 * Get the maximal label width for the given incoming {@link NodePort}.
	 */
	public static double getTopPortLabelWidth(DiagramJSLayoutContext context, NodePort port, double defaultWidth) {
		if (context.getDirection() == LayoutDirection.VERTICAL_FROM_SOURCE) {
			return getReferenceEdgesStream(port).map(edge -> {
				if (edge.isReversed()) {
					return getEdgeTargetLabelWidth(context, edge);
				} else {
					return getEdgeSourceLabelWidth(context, edge);
				}
			}).reduce(Double::max).orElse(defaultWidth);
		} else {
			return getReferenceEdgesStream(port).map(edge -> {
				if (edge.isReversed()) {
					return getEdgeSourceLabelWidth(context, edge);
				} else {
					return getEdgeTargetLabelWidth(context, edge);
				}
			}).reduce(Double::max).orElse(defaultWidth);
		}
	}

	/**
	 * Get the maximal label width for the given incoming {@link NodePort}.
	 */
	public static double getIncomingPortLabelWidth(DiagramJSLayoutContext context, NodePort port, double defaultWidth) {
		return getReferenceEdgesStream(port).map(edge -> {
			if (edge.isReversed()) {
				return getEdgeTargetLabelWidth(context, edge);
			} else {
				return getEdgeSourceLabelWidth(context, edge);
			}
		}).reduce(Double::max).orElse(defaultWidth);
	}

	private static double getEdgeTargetLabelWidth(DiagramJSLayoutContext context, LayoutEdge edge) {
		TLStructuredTypePart part = (TLStructuredTypePart) edge.getBusinessObject();

		return Math.max(getCardinalityWidth(part), getLabelWidth(context, part));
	}

	/**
	 * Width of the {@link TLStructuredTypePart} label text.
	 */
	public static double getLabelWidth(DiagramJSLayoutContext context, TLStructuredTypePart part) {
		return DiagramTextRenderingUtil.getTextWidth(getLabel(context.getLabelProvider(), part));
	}

	/**
	 * Width of the {@link TLType} label text.
	 */
	public static double getLabelWidth(DiagramJSLayoutContext context, TLType type) {
		return DiagramTextRenderingUtil.getTextWidth(getLabel(context.getLabelProvider(), type));
	}

	/**
	 * Height of the {@link TLType} label text.
	 */
	public static double getLabelHeight(DiagramJSLayoutContext context, TLType type) {
		return DiagramTextRenderingUtil.getTextHeight(getLabel(context.getLabelProvider(), type));
	}

	private static double getCardinalityWidth(TLStructuredTypePart part) {
		return DiagramTextRenderingUtil.getTextWidth(DiagramJSLayoutGraphUtil.getCardinality(part));
	}

	private static Stream<LayoutEdge> getReferenceEdgesStream(NodePort port) {
		return port.getAttachedEdges().stream()
			.filter(edge -> edge.getBusinessObject() instanceof TLStructuredTypePart);
	}

	/**
	 * Width on the grid for the given incoming port inclusive label w.r.t. to the given
	 *         minimal width.
	 */
	public static double getTopPortGridLabelWidth(DiagramJSLayoutContext context, NodePort port, int gridStepSize,
			int minWidth) {
		int topPortLabelWidth = (int) getTopPortLabelWidth(context, port, 0);

		return MathUtil.roundUpperMultiple(Math.max(minWidth, topPortLabelWidth), gridStepSize);
	}

	/**
	 * Width on the grid for the given incoming port inclusive label w.r.t. to the given
	 *         minimal width.
	 */
	public static double getIncomingPortGridLabelWidth(DiagramJSLayoutContext context, NodePort port, int gridStepSize,
			int minWidth) {
		int topPortLabelWidth = (int) getIncomingPortLabelWidth(context, port, 0);

		return MathUtil.roundUpperMultiple(Math.max(minWidth, topPortLabelWidth), gridStepSize);
	}

	/**
	 * Width on the grid for all incoming ports inclusive label.
	 */
	public static double getTopPortsGridWidth(DiagramJSLayoutContext context, List<NodePort> ports) {
		return ports.stream()
			.map(port -> getTopPortGridLabelWidth(context, port, GraphConstants.SCALE, GraphConstants.SCALE))
				.reduce(Double::sum).orElse(DefaultNodeSizer.DEFAULT_PORT_WIDTH);
	}

	/**
	 * Width on the grid for all incoming ports inclusive label.
	 */
	public static double getIncomingPortsGridWidth(DiagramJSLayoutContext context, List<NodePort> ports) {
		return ports.stream()
			.map(port -> getIncomingPortGridLabelWidth(context, port, GraphConstants.SCALE, GraphConstants.SCALE))
			.reduce(Double::sum).orElse(DefaultNodeSizer.DEFAULT_PORT_WIDTH);
	}

	/**
	 * Width on the grid for all incoming ports inclusive label.
	 */
	public static double getBottomPortsGridWidth(DiagramJSLayoutContext context, List<NodePort> ports) {
		return ports.stream()
			.map(port -> getBottomPortGridLabelWidth(context, port, GraphConstants.SCALE, GraphConstants.SCALE))
			.reduce(Double::sum).orElse(DefaultNodeSizer.DEFAULT_PORT_WIDTH);
	}

	/**
	 * Width on the grid for all incoming ports inclusive label.
	 */
	public static double getOutgoingPortsGridWidth(DiagramJSLayoutContext context, List<NodePort> ports) {
		return ports.stream()
			.map(port -> getOutgoingPortGridLabelWidth(context, port, GraphConstants.SCALE, GraphConstants.SCALE))
			.reduce(Double::sum).orElse(DefaultNodeSizer.DEFAULT_PORT_WIDTH);
	}

	/**
	 * Width on the grid for the given outgoing port inclusive label w.r.t. to the given
	 *         minimal width.
	 */
	public static double getBottomPortGridLabelWidth(DiagramJSLayoutContext context, NodePort port, int gridStepSize,
			int minWidth) {
		int bottomPortLabelWidth = (int) getBottomPortLabelWidth(context, port, 0);

		return MathUtil.roundUpperMultiple(Math.max(minWidth, bottomPortLabelWidth), gridStepSize);
	}

	/**
	 * Width on the grid for the given outgoing port inclusive label w.r.t. to the given
	 *         minimal width.
	 */
	public static double getOutgoingPortGridLabelWidth(DiagramJSLayoutContext context, NodePort port, int gridStepSize,
			int minWidth) {
		int bottomPortLabelWidth = (int) getOutgoingPortLabelWidth(context, port, 0);

		return MathUtil.roundUpperMultiple(Math.max(minWidth, bottomPortLabelWidth), gridStepSize);
	}

	/**
	 * Width on the grid for the given {@link TLType}.
	 */
	public static double getNodeGridWidth(DiagramJSLayoutContext context, TLType type, int gridStepSize,
			double minWidth) {
		double typeWidth = getNodeWidth(context, type);

		return MathUtil.roundUpperMultiple(Math.max(typeWidth, minWidth), gridStepSize);
	}

	/**
	 * Width of the given {@link TLType}.
	 */
	public static double getNodeWidth(DiagramJSLayoutContext context, TLType type) {
		double nodeNameWidth = getLabelWidth(context, type);
		double nodeAttributesWidth = getNodeAttributesWidth(context, type);
		double nodeStereotypesWidth = getNodeStereotypesWidth(type);

		return Collections.max(Arrays.asList(nodeNameWidth, nodeAttributesWidth, nodeStereotypesWidth));
	}

	private static double getNodeStereotypesWidth(TLType type) {
		if (type instanceof TLEnumeration) {
			return DiagramTextRenderingUtil.getTextWidth(ENUMERATION_STEREOTYPE);
		}

		return 0;
	}

	/**
	 * Width of the edge source label.
	 */
	public static double getEdgeSourceLabelWidthPublic(DiagramJSLayoutContext context, LayoutEdge edge) {
		return getEdgeSourceLabelWidth(context, edge);
	}

	/**
	 * Width of the edge target label.
	 */
	public static double getEdgeTargetLabelWidthPublic(DiagramJSLayoutContext context, LayoutEdge edge) {
		return getEdgeTargetLabelWidth(context, edge);
	}

}
