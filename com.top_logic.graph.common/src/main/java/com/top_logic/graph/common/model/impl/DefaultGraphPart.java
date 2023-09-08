/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.common.model.impl;

import static com.top_logic.basic.shared.string.StringServicesShared.*;

import com.top_logic.common.remote.shared.DefaultSharedObject;
import com.top_logic.common.remote.shared.ObjectScope;
import com.top_logic.graph.common.model.GraphModel;
import com.top_logic.graph.common.model.GraphPart;

/**
 * {@link DefaultSharedObject} {@link GraphPart} implementation.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public abstract class DefaultGraphPart extends DefaultSharedObject implements GraphPart {

	/** The property storing the {@link GraphModel}. */
	public static final String GRAPH = "graph";

	private Object _tag;

	private Object _builder;

	/**
	 * Creates a {@link DefaultGraphPart}.
	 *
	 * @param scope
	 *        See {@link #scope()}.
	 */
	public DefaultGraphPart(ObjectScope scope) {
		super(scope);
	}

	@Override
	public DefaultGraphModel getGraph() {
		return get(GRAPH);
	}

	@Override
	public void initGraph(GraphModel graph) {
		checkNotInitialized(graph, getGraph());
		set(GRAPH, graph);
	}

	/** Check if the old value is either null or the same as the new value. */
	public static void checkNotInitialized(Object newValue, Object oldValue) {
		if (oldValue == null) {
			return;
		}
		// Check if it is initialized to the same value twice. That is okay, too.
		if (oldValue.equals(newValue)) {
			return;
		}
		throw new IllegalStateException("The property is already initialized.");
	}

	@Override
	public Object getTag() {
		return _tag;
	}

	@Override
	public void setTag(Object newValue) {
		_tag = newValue;

		Object oldValue = getTag();
		updateGraphIndex(newValue, oldValue);

		scope().notifyUpdate(this, TAG);
	}

	/**
	 * Update the mapping from "tag" to "graph element" in the {@link DefaultGraphModel}.
	 * <p>
	 * This is the same code for nodes and edges. But it is not possible to write that here: There
	 * are distinct maps for nodes and edges. The {@link DefaultGraphModel} would have to make a
	 * type check. And if another subtype of {@link DefaultLabelOwner} is added, there would be no
	 * compile error showing the missing type check. Therefore, this abstract method exists, which
	 * shows that each {@link DefaultLabelOwner} subtype has to take care of that.
	 * </p>
	 */
	protected void updateGraphIndex(Object newValue, Object oldValue) {
		getGraph().updateBusinessObject(this, oldValue, newValue);
	}

	@Override
	public void delete() {
		onDelete();
		scope().notifyDelete(data());
		set(GRAPH, null);
	}

	@Override
	public boolean isVisible() {
		return getDataBoolean(VISIBLE, true);
	}

	@Override
	public void setVisible(boolean value) {
		set(VISIBLE, Boolean.valueOf(value));
	}

	@Override
	public Object getBuilder() {
		return _builder;
	}

	@Override
	public void setBuilder(Object builder) {
		if (_builder != null && builder != _builder) {
			throw new RuntimeException("The node " + debug(this) + " has already a builder: " + debug(_builder)
				+ " It cannot get another builder: " + debug(builder));
		}
		_builder = builder;
	}

}
