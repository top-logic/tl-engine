/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.inspector.model.nodes;

import java.lang.reflect.Modifier;
import java.util.List;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.filter.TrueFilter;
import com.top_logic.layout.inspector.InspectorComponent;
import com.top_logic.layout.inspector.model.InspectorTreeBuilder;
import com.top_logic.layout.inspector.model.InspectorTreeTableModel;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
import com.top_logic.layout.tree.model.AbstractTreeTableModel;
import com.top_logic.layout.tree.model.TreeBuilder;

/**
 * Represents an object in the {@link InspectorComponent}.
 * 
 * @see InspectorTreeBuilder
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public abstract class InspectorTreeNode extends AbstractTreeTableModel.AbstractTreeTableNode<InspectorTreeNode> {

	/** The field represented, must not be <code>null</code>. */
	protected final InspectProperty _property;

	/**
	 * Create a new instance of this class.
	 */
	public InspectorTreeNode(AbstractMutableTLTreeModel<InspectorTreeNode> model, InspectorTreeNode parent,
			Object userObject, InspectProperty property) {
		super(model, parent, userObject);

		_property = property;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getName());
		builder.append(" [");
		builder.append("field: ");
		builder.append(_property);
		builder.append(", model: ");
		builder.append(getBusinessObject());
		builder.append(']');
		return (builder.toString());
	}

	/**
	 * The property this node represents within its parent's model.
	 */
	public InspectProperty getProperty() {
		return _property;
	}

	/**
	 * true when {@link #_property} is static.
	 */
	public boolean isStatic() {
		return _property.isStatic();
	}

	/**
	 * The name of the property this node represents the value of.
	 */
	public String propertyName() {
		return _property.name();
	}

	/**
	 * Return the fields (and values) of the given business object as child to the given node.
	 * 
	 * <p>
	 * This is the node-local implementation of
	 * {@link TreeBuilder#createChildList(com.top_logic.layout.tree.model.AbstractMutableTLTreeNode)}.
	 * </p>
	 * 
	 * <p>
	 * Implementations must only add nodes matching {@link #filter()}.
	 * </p>
	 */
	public abstract List<InspectorTreeNode> makeChildren();

	/**
	 * The filter to apply when creating children.
	 * 
	 * @see #makeChildren()
	 */
	protected final Filter<? super InspectorTreeNode> filter() {
		AbstractTreeTableModel<InspectorTreeNode> model = getModel();
		Filter<? super InspectorTreeNode> filter;
		if (model instanceof InspectorTreeTableModel) {
			filter = ((InspectorTreeTableModel) model).getNodeFilter();
		} else {
			filter = TrueFilter.INSTANCE;
		}
		return filter;
	}

	/**
	 * Creates a direct child node of this node (part of the builder's
	 * {@link TreeBuilder#createChildList(com.top_logic.layout.tree.model.AbstractMutableTLTreeNode)}
	 * implementation.
	 */
	protected InspectorTreeNode makeValueNode(InspectProperty field, Object value) {
		AbstractTreeTableModel<InspectorTreeNode> model = getModel();
		InspectorTreeBuilder builder = (InspectorTreeBuilder) model.getBuilder();

		return builder.makeNode(model, this, field, value);
	}

	/**
	 * Representation of the static (declared) type of the {@link #getProperty()}.
	 */
	public Object staticType() {
		return getProperty().staticType();
	}

	/**
	 * Visibility of the {@link #getProperty()}.
	 */
	public Object visibility() {
		if (getProperty().isPrivate()) {
			return Modifier.toString(Modifier.PRIVATE);
		} else {
			return Modifier.toString(Modifier.PUBLIC);
		}
	}

	/**
	 * Whether the {@link #getBusinessObject()} is the default-value of {@link #getProperty()}.
	 */
	public boolean hasDefaultValue() {
		return getProperty().isDefaultValue(this);
	}

}
