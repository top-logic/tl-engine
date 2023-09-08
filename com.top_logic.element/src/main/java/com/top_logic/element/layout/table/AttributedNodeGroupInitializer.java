/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.table;

import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.AttributeUpdateContainer;
import com.top_logic.element.meta.AttributeUpdateFactory;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.FormTree;
import com.top_logic.layout.form.model.NodeGroupInitializer;
import com.top_logic.layout.form.treetable.component.StructureEditComponent;
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link NodeGroupInitializer} for a {@link StructureEditComponent}.
 * 
 * <p>
 * Creates for each {@link TLStructuredTypePart} of the displayed {@link Wrapper} a {@link FormField}
 * and inserts it into the node group.
 * </p>
 * 
 * @see AttributedStructureEditComponent
 * 
 * @since 5.7.6
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class AttributedNodeGroupInitializer implements NodeGroupInitializer {

	/** Singleton {@link AttributedNodeGroupInitializer} instance. */
	public static final AttributedNodeGroupInitializer INSTANCE = new AttributedNodeGroupInitializer();

	/**
	 * Creates a new {@link AttributedNodeGroupInitializer}.
	 */
	protected AttributedNodeGroupInitializer() {
		// singleton instance
	}

	@Override
	public void createNodeGroup(FormTree formTree, FormGroup nodeGroup, Object node) {
		TLTreeNode<?> treeNode = (TLTreeNode<?>) node;
		Wrapper attributed = (Wrapper) treeNode.getBusinessObject();

		AttributeFormContext formContext = (AttributeFormContext) formTree.getFormContext();
		TLClass me = (TLClass) attributed.tType();
		AttributeUpdateContainer updateContainer = formContext.getAttributeUpdateContainer();
		for (TLStructuredTypePart attribute : TLModelUtil.getMetaAttributes(me)) {
			AttributeUpdate update =
				AttributeUpdateFactory.createAttributeUpdateForEdit(updateContainer, attribute, attributed, false);
			if (update == null) {
				continue;
			}
			FormMember theMember = formContext.createFormMemberForUpdate(update);
			if (theMember != null) {
				nodeGroup.addMember(theMember);
			}
		}
	}

}

