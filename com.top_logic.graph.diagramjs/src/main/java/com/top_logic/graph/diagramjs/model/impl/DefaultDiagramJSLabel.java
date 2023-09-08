/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.model.impl;

import com.top_logic.common.remote.factory.ReflectionFactory;
import com.top_logic.common.remote.shared.DefaultSharedObject;
import com.top_logic.common.remote.shared.ObjectScope;
import com.top_logic.graph.common.model.impl.DefaultLabel;
import com.top_logic.graph.diagramjs.model.DiagramJSLabel;

/**
 * {@link DefaultSharedObject} {@link DefaultDiagramJSLabel} implementation.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class DefaultDiagramJSLabel extends DefaultLabel implements DiagramJSLabel {

	/**
	 * Creates a {@link DefaultDiagramJSLabel}.
	 * <p>
	 * This method has to be public, as it is called via reflection from the
	 * {@link ReflectionFactory}.
	 * </p>
	 *
	 * @param scope
	 *        See {@link DefaultSharedObject#DefaultSharedObject(ObjectScope)}.
	 */
	public DefaultDiagramJSLabel(ObjectScope scope) {
		super(scope);
	}

	@Override
	public String getType() {
		return get(DiagramJSLabel.LABEL_TYPE);
	}

	@Override
	public void setType(String type) {
		set(DiagramJSLabel.LABEL_TYPE, type);
	}
}
