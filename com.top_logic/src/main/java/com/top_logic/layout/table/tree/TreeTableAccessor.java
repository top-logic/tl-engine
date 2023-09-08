/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.tree;

import com.top_logic.layout.Accessor;
import com.top_logic.layout.tree.model.TLTreeNode;

/**
 * {@link Accessor} of nodes displayed in tree tables. It transparently wraps access to values
 * provided by given column accessor.
 * 
 * <p>
 * Note: It is not necessary to explicitly use this class in table configuration of tree table
 * components, since it is wrapped automatically around custom accessors.
 * </p>
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public final class TreeTableAccessor implements Accessor<TLTreeNode<?>> {

	/**
	 * Parameter to use in configuration for the inner accessor.
	 */
	public static final String INNER_ACCESSOR_CONFIG_PARAM = "innerAccessor";

	private Accessor<Object> _wrappedAccessor;

	/**
	 * Create a new {@link TreeTableAccessor}.
	 * 
	 * <p>
	 * Note: It is not necessary to explicitly use this class in table configurations of tree table
	 * components, since it is wrapped automatically around custom accessors. Therefore there is no
	 * need to make this class configurable.
	 * </p>
	 */
	@SuppressWarnings("unchecked")
	public TreeTableAccessor(Accessor<?> wrappedAccessor) {
		_wrappedAccessor = (Accessor<Object>) wrappedAccessor;
	}
	
	@Override
	public Object getValue(TLTreeNode<?> node, String property) {
		return _wrappedAccessor.getValue(node.getBusinessObject(), property);
	}

	@Override
	public void setValue(TLTreeNode<?> node, String property, Object value) {
		_wrappedAccessor.setValue(node.getBusinessObject(), property, value);
	}

}