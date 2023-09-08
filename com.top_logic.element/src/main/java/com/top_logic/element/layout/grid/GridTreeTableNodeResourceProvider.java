/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.grid;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.tree.model.TLTreeNodeResourceProvider;

/**
 * {@link Accessor} for {@link GridTreeTableNode}s.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class GridTreeTableNodeResourceProvider extends TLTreeNodeResourceProvider {

	/**
	 * Create a {@link GridTreeTableNodeResourceProvider}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public GridTreeTableNodeResourceProvider(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected Object mapValue(Object node) {
		FormMember formMember = (FormMember) super.mapValue(node);
		return formMember.get(GridComponent.PROP_ATTRIBUTED);
	}

}
