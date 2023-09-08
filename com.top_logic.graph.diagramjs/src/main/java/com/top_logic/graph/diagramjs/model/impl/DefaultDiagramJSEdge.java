/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.model.impl;

import java.util.Collection;
import java.util.List;

import com.top_logic.common.remote.factory.ReflectionFactory;
import com.top_logic.common.remote.shared.DefaultSharedObject;
import com.top_logic.common.remote.shared.ObjectScope;
import com.top_logic.graph.common.model.Label;
import com.top_logic.graph.common.model.impl.DefaultEdge;
import com.top_logic.graph.diagramjs.model.DiagramJSEdge;
import com.top_logic.graph.diagramjs.model.DiagramJSLabel;

/**
 * {@link DefaultSharedObject} {@link DiagramJSEdge} implementation.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class DefaultDiagramJSEdge extends DefaultEdge implements DiagramJSEdge {

	/**
	 * Creates a {@link DefaultDiagramJSEdge}.
	 * <p>
	 * This method has to be public, as it is called via reflection from the
	 * {@link ReflectionFactory}.
	 * </p>
	 *
	 * @param scope
	 *        See {@link DefaultSharedObject#DefaultSharedObject(ObjectScope)}.
	 */
	public DefaultDiagramJSEdge(ObjectScope scope) {
		super(scope);
	}

	@Override
	public String getType() {
		return get(DiagramJSEdge.EDGE_TYPE);
	}

	@Override
	public void setType(String type) {
		set(DiagramJSEdge.EDGE_TYPE, type);
	}

	@Override
	public List<List<Double>> getWaypoints() {
		return get(DiagramJSEdge.WAYPOINTS);
	}

	@Override
	public void setWaypoints(List<List<Double>> waypoints) {
		set(DiagramJSEdge.WAYPOINTS, waypoints);
	}

	@Override
	public String getSourceCardinality() {
		return get(DiagramJSEdge.SOURCE_CARDINALITY);
	}

	@Override
	public void setSourceCardinality(String sourceCardinalty) {
		set(DiagramJSEdge.SOURCE_CARDINALITY, sourceCardinalty);
	}

	@Override
	public String getSourceName() {
		return get(DiagramJSEdge.SOURCE_NAME);
	}

	@Override
	public void setSourceName(String sourceName) {
		set(DiagramJSEdge.SOURCE_NAME, sourceName);
	}

	@Override
	public String getTargetCardinality() {
		return get(DiagramJSEdge.TARGET_CARDINALITY);
	}

	@Override
	public void setTargetCardinality(String targetCardinalty) {
		set(DiagramJSEdge.TARGET_CARDINALITY, targetCardinalty);
	}

	@Override
	public String getTargetName() {
		return get(DiagramJSEdge.TARGET_NAME);
	}

	@Override
	public void setTargetName(String targetName) {
		set(DiagramJSEdge.TARGET_NAME, targetName);
	}

	@Override
	protected Label createLabelInternal() {
		return new DefaultDiagramJSLabel(scope());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<? extends DiagramJSLabel> getLabels() {
		return (Collection<? extends DiagramJSLabel>) super.getLabels();
	}

}
