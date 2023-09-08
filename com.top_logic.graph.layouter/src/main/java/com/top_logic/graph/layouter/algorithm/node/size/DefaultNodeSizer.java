/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.layouter.algorithm.node.size;

import java.util.List;
import java.util.function.Function;

import com.top_logic.graph.layouter.GraphConstants;
import com.top_logic.graph.layouter.LayoutContext;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutNode;
import com.top_logic.graph.layouter.model.NodeConstants;
import com.top_logic.graph.layouter.model.NodePort;
import com.top_logic.graph.layouter.model.util.LayoutGraphUtil;
import com.top_logic.graph.layouter.text.util.DiagramTextRenderingUtil;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLType;

/**
 * Default sizer for a {@link LayoutNode}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class DefaultNodeSizer extends NodeSizer implements NodeConstants {

	/**
	 * Name of the enumeration stereotype.
	 */
	private static final String ENUMERATION_STEREOTYPE = "enumeration";

	/**
	 * Common height offset between name, separator, attributes and borders.
	 */
	private static final int HEIGHT_OFFSET = 5;

	/**
	 * Default port with inclusive label.
	 */
	public static final double DEFAULT_PORT_WIDTH = 0.;

	/**
	 * Creates a {@link NodeSizer} for the given {@link LayoutContext}.
	 */
	public DefaultNodeSizer(LayoutContext context) {
		super(getWidthSizer(context), getHeightSizer(context));
	}

	private static Function<LayoutNode, Double> getHeightSizer(LayoutContext context) {
		return node -> {
			Object userObject = node.getUserObject();

			if (userObject instanceof TLType) {
				return Math.max(MINIMUM_NODE_HEIGHT, getNodeHeight(context, (TLType) userObject));
			} else {
				return MINIMUM_NODE_HEIGHT;
			}
		};
	}

	private static double getNodeHeight(LayoutContext context, TLType type) {
		double nodeHeight = getNodeNameHeight(context, type);

		nodeHeight += getNodeStereotypeHeight(type);
		nodeHeight += getNodeAttributesHeight(context, type);
		nodeHeight += HEIGHT_OFFSET;

		return nodeHeight;
	}

	private static double getNodeAttributesHeight(LayoutContext context, TLType type) {
		double attributesHeight = LayoutGraphUtil.getNodeAttributesHeight(context, type);

		if (attributesHeight != 0) {
			attributesHeight += HEIGHT_OFFSET;
		}

		return attributesHeight;
	}

	private static double getNodeStereotypeHeight(TLType type) {
		if (type instanceof TLEnumeration) {
			return DiagramTextRenderingUtil.getTextHeight(ENUMERATION_STEREOTYPE);
		}

		return 0;
	}

	private static double getNodeNameHeight(LayoutContext context, TLType type) {
		return LayoutGraphUtil.getLabelHeight(context, type);
	}

	private static Function<LayoutNode, Double> getWidthSizer(LayoutContext context) {
		return node -> {
			return node.isDummy() ? 0. : getNodeWidth(context, node);
		};
	}

	private static Double getNodeWidth(LayoutContext context, LayoutNode node) {
		Object userObject = node.getUserObject();

		double width = getPortsWidth(context, node);

		if (userObject instanceof TLType) {
			return Math.max(width, getNodeWidth(context, (TLType) userObject));
		}

		return width;
	}

	private static double getNodeWidth(LayoutContext context, TLType type) {
		return LayoutGraphUtil.getNodeGridWidth(context, type, GraphConstants.SCALE, (int) MINIMUM_WIDTH);
	}

	private static double getPortsWidth(LayoutContext context, LayoutNode node) {
		double topPortsWidth = getTopPortsWidth(context, node);
		double bottomPortsWidth = getBottomPortsWidth(context, node);

		return Math.max(topPortsWidth, bottomPortsWidth);
	}

	private static double getBottomPortsWidth(LayoutContext context, LayoutNode node) {
		List<NodePort> bottomNodePorts = LayoutGraphUtil.getBottomNodePorts(context.getDirection(), node);

		return LayoutGraphUtil.getBottomPortsGridWidth(context, bottomNodePorts) + GraphConstants.SCALE;
	}

	private static double getTopPortsWidth(LayoutContext context, LayoutNode node) {
		List<NodePort> topNodePorts = LayoutGraphUtil.getTopNodePorts(context.getDirection(), node);

		return LayoutGraphUtil.getTopPortsGridWidth(context, topNodePorts) + GraphConstants.SCALE;
	}

}
