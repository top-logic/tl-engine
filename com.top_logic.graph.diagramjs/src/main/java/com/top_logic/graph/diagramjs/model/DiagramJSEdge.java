/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.model;

import java.util.Collection;
import java.util.List;

import com.top_logic.graph.common.model.Edge;
import com.top_logic.graph.common.model.GraphModel;

/**
 * An diagramjs edge between two {@link DiagramJSClassNode}s in a {@link GraphModel}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public interface DiagramJSEdge extends Edge {

	/**
	 * Name of {@link #getType()} property.
	 */
	public String EDGE_TYPE = "edgeType";

	/**
	 * Name of {@link #getWaypoints()} property.
	 */
	public String WAYPOINTS = "waypoints";

	/**
	 * Name of {@link #getSourceCardinality()} property.
	 */
	public String SOURCE_CARDINALITY = "sourceCardinality";

	/**
	 * Name of {@link #getSourceName()} property.
	 */
	public String SOURCE_NAME = "sourceName";

	/**
	 * Name of {@link #getTargetCardinality()} property.
	 */
	public String TARGET_CARDINALITY = "targetCardinality";

	/**
	 * Name of {@link #getTargetName()} property.
	 */
	public String TARGET_NAME = "targetName";

	/**
	 * UML type of this {@link Edge}.
	 */
	String getType();

	/**
	 * @see #getType()
	 */
	void setType(String type);

	/**
	 * An Edge consists of parts which are determined by her waypoints.
	 */
	List<List<Double>> getWaypoints();

	/**
	 * @see #getWaypoints()
	 */
	void setWaypoints(List<List<Double>> waypoints);

	/**
	 * Edge cardinality for the source {@link DiagramJSClassNode}.
	 */
	String getSourceCardinality();

	/**
	 * @see #getSourceCardinality()
	 */
	void setSourceCardinality(String sourceCardinalty);

	/**
	 * Edge name for the source {@link DiagramJSClassNode}.
	 */
	String getSourceName();

	/**
	 * @see #getSourceName()
	 */
	void setSourceName(String sourceName);

	/**
	 * Edge cardinality for the target {@link DiagramJSClassNode}.
	 */
	String getTargetCardinality();

	/**
	 * @see #getTargetCardinality()
	 */
	void setTargetCardinality(String targetCardinalty);

	/**
	 * Edge name for the target {@link DiagramJSClassNode}.
	 */
	String getTargetName();

	/**
	 * @see #getTargetName()
	 */
	void setTargetName(String targetName);

	@Override
	public Collection<? extends DiagramJSLabel> getLabels();
}
