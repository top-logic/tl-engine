/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.table;

import com.top_logic.element.meta.gui.MetaAttributeGUIHelper;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.ReadOnlyAccessor;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.treetable.component.StructureEditComponent;
import com.top_logic.layout.form.treetable.component.StructureEditComponentConstants;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.model.TLStructuredTypePart;

/**
 * {@link Accessor} for {@link StructureEditComponent}.
 * 
 * <p>
 * Returns the {@link FormField} which belongs to the {@link TLStructuredTypePart} with the given property
 * name.
 * </p>
 * 
 * @see AttributedStructureEditComponent
 * 
 * @since 5.7.6
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class AttributedNodeGroupAccessor extends ReadOnlyAccessor<FormGroup> {

	/** Singleton {@link AttributedNodeGroupAccessor} instance. */
	public static final AttributedNodeGroupAccessor INSTANCE = new AttributedNodeGroupAccessor();

	/**
	 * Creates a new {@link AttributedNodeGroupAccessor}.
	 */
	protected AttributedNodeGroupAccessor() {
		// singleton instance
	}

	@Override
	public Object getValue(FormGroup group, String property) {
		TLTreeNode<?> treeNode = (TLTreeNode<?>) group.get(StructureEditComponentConstants.NODE_PROPERTY);
		Wrapper attributed = (Wrapper) treeNode.getBusinessObject();
		TLStructuredTypePart part = attributed.tType().getPart(property);
		if (part == null) {
			// attribute does neither exists in the type nor in any super type.
			return null;
		}
		String fieldName = MetaAttributeGUIHelper.getAttributeID(part, attributed);
		if (!group.hasMember(fieldName)) {
			return null;
		}
		return group.getField(fieldName);
	}

}

