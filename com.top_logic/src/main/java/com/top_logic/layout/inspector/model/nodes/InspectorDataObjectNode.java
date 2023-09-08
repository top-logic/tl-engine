/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.inspector.model.nodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.Filter;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.MOAttribute;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;

/**
 * Provide data for a {@link DataObject}.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class InspectorDataObjectNode extends InspectorTreeNode {

	/**
	 * Creates a {@link InspectorDataObjectNode}.
	 */
	public InspectorDataObjectNode(AbstractMutableTLTreeModel<InspectorTreeNode> treeModel, InspectorTreeNode parent,
			InspectProperty property, DataObject aBO) {
		super(treeModel, parent, aBO, property);
	}

	@Override
	public List<InspectorTreeNode> makeChildren() {
		DataObject data = (DataObject) getBusinessObject();
		Iterable<? extends MOAttribute> attributes = data.getAttributes();
		List<MOAttribute> sortedAttributes = CollectionUtil.toListIterable(attributes);
		Collections.sort(sortedAttributes, new Comparator<MOAttribute>() {
			@Override
			public int compare(MOAttribute o1, MOAttribute o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		
		Filter<? super InspectorTreeNode> filter = filter();
		List<InspectorTreeNode> result = new ArrayList<>(sortedAttributes.size());
		for (MOAttribute attribute : sortedAttributes) {
			Object value = data.getValue(attribute);
			InspectorTreeNode newNode = makeValueNode(new MOProperty(attribute), value);
			if (filter.accept(newNode)) {
				result.add(newNode);
			}
		}
		
		return result;
	}

	/**
	 * {@link InspectProperty} representing a {@link MOAttribute}.
	 */
	public static class MOProperty extends InspectProperty {

		private MOAttribute _attribute;

		/**
		 * Creates a {@link MOProperty}.
		 *
		 * @param attribute
		 *        See {@link #getAttribute()}.
		 */
		public MOProperty(MOAttribute attribute) {
			_attribute = attribute;
		}

		/**
		 * The {@link MOAttribute} being inspected.
		 */
		public MOAttribute getAttribute() {
			return _attribute;
		}

		@Override
		public String name() {
			return _attribute.getName();
		}

		@Override
		public Object getValue() {
			return getAttribute();
		}

		@Override
		public Object staticType() {
			return _attribute.getMetaObject();
		}

		@Override
		public boolean isPrivate() {
			return _attribute.isSystem();
		}

	}

}
