/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.common.model.impl;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.common.remote.factory.ReflectionFactory;
import com.top_logic.common.remote.shared.DefaultSharedObject;
import com.top_logic.common.remote.shared.ObjectScope;
import com.top_logic.graph.common.model.Edge;
import com.top_logic.graph.common.model.Node;
import com.top_logic.graph.common.model.styles.EdgeStyle;

/**
 * {@link DefaultSharedObject} {@link Edge} implementation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultEdge extends DefaultLabelOwner implements Edge {

	static final String SRC = "src";

	static final String DST = "dst";

	/**
	 * Creates a {@link DefaultEdge}.
	 * <p>
	 * This method has to be public, as it is called via reflection from the
	 * {@link ReflectionFactory}.
	 * </p>
	 *
	 * @param scope
	 *        See {@link DefaultSharedObject#DefaultSharedObject(ObjectScope)}.
	 */
	@CalledByReflection
	public DefaultEdge(ObjectScope scope) {
		super(scope);
	}

	@Override
	public DefaultNode getSource() {
		return get(SRC);
	}

	@Override
	public void setSource(Node source) {
		set(SRC, source);
	}

	@Override
	public DefaultNode getDestination() {
		return get(DST);
	}

	@Override
	public void setDestination(Node dest) {
		set(DST, dest);
	}

	@Override
	public EdgeStyle getEdgeStyle() {
		return get(EDGE_STYLE);
	}

	@Override
	public void setEdgeStyle(EdgeStyle value) {
		set(EDGE_STYLE, value);
	}

	@Override
	protected void onDelete() {
		super.onDelete();
		deleteNodes();
		getGraph().removeInternal(this);
	}

	void deleteNodes() {
		setSource(null);
		setDestination(null);
	}

}
