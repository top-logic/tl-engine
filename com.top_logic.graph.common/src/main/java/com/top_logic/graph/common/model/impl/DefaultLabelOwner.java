/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.common.model.impl;

import java.util.ArrayList;
import java.util.Collection;

import com.top_logic.common.remote.shared.DefaultSharedObject;
import com.top_logic.common.remote.shared.ObjectScope;
import com.top_logic.graph.common.model.Label;
import com.top_logic.graph.common.model.LabelOwner;

/**
 * {@link DefaultSharedObject} {@link LabelOwner} implementation.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class DefaultLabelOwner extends DefaultGraphPart implements LabelOwner {

	/**
	 * Creates a {@link DefaultLabelOwner}.
	 *
	 * @param scope
	 *        See {@link #scope()}.
	 */
	public DefaultLabelOwner(ObjectScope scope) {
		super(scope);
	}

	@Override
	public Collection<? extends Label> getLabels() {
		return getReferrers(DefaultLabel.class, Label.OWNER);
	}

	@Override
	public Label createLabel() {
		Label result = createLabelInternal();
		result.initGraph(getGraph());
		result.initOwner(this);
		return result;
	}

	/**
	 * Creates only the {@link Label} instance without any further initialization.
	 * <p>
	 * For subclasses that have to create a more specific type.
	 * </p>
	 */
	protected Label createLabelInternal() {
		return new DefaultLabel(scope());
	}

	@Override
	protected void onDelete() {
		for (Label label : new ArrayList<>(getLabels())) {
			label.delete();
		}
	}

}
