/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.common.model.impl;

import com.top_logic.common.remote.shared.DefaultSharedObject;
import com.top_logic.common.remote.shared.ObjectScope;
import com.top_logic.graph.common.model.Label;
import com.top_logic.graph.common.model.LabelOwner;
import com.top_logic.graph.common.model.layout.LabelLayout;

/**
 * {@link DefaultSharedObject} {@link Label} implementation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultLabel extends DefaultGraphPart implements Label {

	/**
	 * Creates a {@link DefaultLabel}.
	 *
	 * @param scope
	 *        See {@link #scope()}.
	 */
	public DefaultLabel(ObjectScope scope) {
		super(scope);
	}

	@Override
	public LabelOwner getOwner() {
		return get(OWNER);
	}

	@Override
	public void initOwner(LabelOwner owner) {
		checkNotInitialized(owner, getOwner());
		set(OWNER, owner);
	}

	private void clearOwner() {
		set(OWNER, null);
	}

	@Override
	protected void onDelete() {
		clearOwner();
		getGraph().removeInternal(this);
	}

	@Override
	public String getText() {
		return get(Label.TEXT);
	}

	@Override
	public void setText(String value) {
		set(TEXT, value);
	}

	@Override
	public LabelLayout getLayout() {
		return get(LAYOUT);
	}

	@Override
	public void setLayout(LabelLayout value) {
		set(LAYOUT, value);
	}

}
