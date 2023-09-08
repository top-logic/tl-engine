/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import java.util.Comparator;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;

/**
 * {@link Comparator} of {@link TLTreeNode}s that keep those nodes at the beginning of the children
 * list, that have the same business object than the parent node.
 * 
 * <p>
 * This compare algorithm is useful, if a node has additional information stored in child nodes that
 * immediately belongs to the parent node. E.g. the node is a sum line with several detail lines as
 * children.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TreeNodeComparatorLocal implements Comparator<TLTreeNode<?>> {

	public interface Config extends PolymorphicConfiguration<Comparator<?>> {
		@Name(BUSINESS_OBJECT_COMPARATOR_PROPERTY)
		@Mandatory
		@InstanceFormat
		Comparator<Object> getBusinessObjectComparator();

		@Name(DESCENDING_PROPERTY)
		@BooleanDefault(false)
		boolean getDescending();
	}

	/**
	 * The {@link Comparator} for business objects, if the criterion that a node belongs immediately
	 * to its parent is not applicable.
	 */
	private static final String BUSINESS_OBJECT_COMPARATOR_PROPERTY = "businessObjectComparator";

	/**
	 * Whether the result of the inner {@link Comparator} should be inverted. This is useful when
	 * constructing descending comparators that <b>only</b> should invert the regular comparison but
	 * should not break the binding of parent-affine nodes.
	 */
	private static final String DESCENDING_PROPERTY = "descending";

	private final Comparator<Object> _businessObjectComparator;

	private int _descending;

	/** Create a {@link TreeNodeComparatorLocal}. */
	public TreeNodeComparatorLocal(InstantiationContext context, Config config) throws ConfigurationException {
		this(
			config.getBusinessObjectComparator(),
			config.getDescending());
	}

	/**
	 * Creates a {@link TreeNodeComparatorLocal}.
	 * 
	 * @param businessObjectComparator
	 *        See {@link #BUSINESS_OBJECT_COMPARATOR_PROPERTY}.
	 * @param descending
	 *        See {@link #DESCENDING_PROPERTY}.
	 */
	public TreeNodeComparatorLocal(Comparator businessObjectComparator, boolean descending) {
		_businessObjectComparator = businessObjectComparator;
		_descending = descending ? -1 : 1;
	}

	@Override
	public final int compare(TLTreeNode<?> n1, TLTreeNode<?> n2) {
		if (belongsToParent(n1)) {
			if (belongsToParent(n2)) {
				return compareRegular(n1, n2);
			} else {
				return -1;
			}
		}
		if (belongsToParent(n2)) {
			return 1;
		}

		return compareRegular(n1, n2);
	}

	private boolean belongsToParent(TLTreeNode<?> node) {
		TLTreeNode<?> parent = node.getParent();
		return parent != null && belongsToParent(node, parent);
	}

	/**
	 * Whether the given node is tied to its parent node.
	 * @param node
	 *        Must not be <code>null</code>.
	 * @param parent
	 *        Must not be <code>null</code>.
	 */
	protected boolean belongsToParent(TLTreeNode<?> node, TLTreeNode<?> parent) {
		return sameBusinessObject(node.getBusinessObject(), parent.getBusinessObject());
	}

	/**
	 * Whether the business objects of the parent and some node should be treated as equivalent.
	 * 
	 * @param nodeObject
	 *        The business object the the node, is allowed to be <code>null</code>.
	 * @param parentObject
	 *        The business object the the parent, is allowed to be <code>null</code>.
	 */
	protected boolean sameBusinessObject(Object nodeObject, Object parentObject) {
		if (nodeObject == parentObject) {
			return true;
		}
		if (nodeObject == null || parentObject == null) {
			return false;
		}
		return _businessObjectComparator.compare(nodeObject, parentObject) == 0;
	}

	/**
	 * Comparison of nodes that are not (or both) tied to their parent node.
	 * 
	 * @param n1
	 *        Must not be <code>null</code>.
	 * @param n2
	 *        Must not be <code>null</code>.
	 */
	protected int compareRegular(TLTreeNode<?> n1, TLTreeNode<?> n2) {
		Object b1 = n1.getBusinessObject();
		Object b2 = n2.getBusinessObject();
		if (b1 == b2) {
			return 0;
		}
		if (b1 == null) {
			return _descending;
		}
		if (b2 == null) {
			return -1 * _descending;
		}
		return _descending * _businessObjectComparator.compare(b1, b2);
	}

}
