/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.inspector.model.nodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.top_logic.basic.col.Filter;
import com.top_logic.layout.tree.model.AbstractMutableTLTreeModel;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.annotate.DisplayAnnotations;

/**
 * The WrapperTreeNode is a special {@link InspectorTreeNode}. The user object of this
 * <tt>ObjectTreeNode</tt> is a {@link TLObject} and the children of this node are nodes based on
 * the attributes and the value of the attributes of the wrapper.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class InspectorTLObjectNode extends InspectorTreeNode {

	/**
	 * Creates a {@link InspectorTLObjectNode}.
	 */
	public InspectorTLObjectNode(AbstractMutableTLTreeModel<InspectorTreeNode> model, InspectorTreeNode parent,
			InspectProperty property, TLObject userObject) {
		super(model, parent, userObject, property);
	}

	@Override
	public boolean isStatic() {
		return false;
	}

	@Override
	public List<InspectorTreeNode> makeChildren() {
		List<InspectorTreeNode> result = new ArrayList<>();
		TLObject obj = (TLObject) getBusinessObject();
		TLStructuredType type = obj.tType();

		List<? extends TLStructuredTypePart> allParts = new ArrayList<>(type.getAllParts());
		Collections.sort(allParts, new Comparator<TLTypePart>() {
			@Override
			public int compare(TLTypePart o1, TLTypePart o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});

		Filter<? super InspectorTreeNode> filter = filter();
		for (TLStructuredTypePart part : allParts) {
			Object value = obj.tValue(part);
			InspectorTreeNode newNode = makeValueNode(new PartProperty(part), value);
			if (filter.accept(newNode)) {
				result.add(newNode);
			}
		}

		return result;
	}

	/**
	 * {@link InspectProperty} representing a {@link TLStructuredTypePart}.
	 */
	public static class PartProperty extends InspectProperty {

		private final TLStructuredTypePart _part;

		/**
		 * Creates a {@link PartProperty}.
		 *
		 * @param part
		 *        See #getPart()
		 */
		public PartProperty(TLStructuredTypePart part) {
			_part = part;
		}

		/**
		 * The {@link TLStructuredTypePart} being displayed.
		 */
		public TLStructuredTypePart getPart() {
			return _part;
		}

		@Override
		public String name() {
			return _part.getName();
		}

		@Override
		public Object getValue() {
			return getPart();
		}

		@Override
		public Object staticType() {
			return _part.getType();
		}

		@Override
		public boolean isPrivate() {
			return DisplayAnnotations.isHidden(_part);
		}

	}
}
