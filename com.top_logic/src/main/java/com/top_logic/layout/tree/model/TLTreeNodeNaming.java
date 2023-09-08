/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import java.util.Collections;
import java.util.Map;

import com.top_logic.layout.scripting.recorder.ref.ValueNamingScheme;

/**
 * {@link ValueNamingScheme} for {@link TLTreeNode}s that identifies them by their
 * {@link TLTreeNode#getBusinessObject() business objects}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TLTreeNodeNaming extends ValueNamingScheme<TLTreeNode<?>> {

	private static final String BUSINESS_OBJECT = "businessObject";

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public Class<TLTreeNode<?>> getModelClass() {
		return (Class) TLTreeNode.class;
	}

	@Override
	public Map<String, Object> getName(TLTreeNode<?> model) {
		return Collections.singletonMap(BUSINESS_OBJECT, model.getBusinessObject());
	}

	@Override
	public boolean matches(Map<String, Object> name, TLTreeNode<?> model) {
		return matchesDefault(name, model);
	}

}
