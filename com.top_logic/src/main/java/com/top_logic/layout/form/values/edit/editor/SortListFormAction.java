/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.values.edit.editor;

import java.util.List;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.ValueModel;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.AbstractApplicationActionOp;

/**
 * {@link ApplicationAction} reordering a list of form groups in a declarative form.
 * 
 * @see ListEditor
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface SortListFormAction extends ApplicationAction {

	/**
	 * @see #getList()
	 */
	String LIST = "list";

	/**
	 * @see #getItems()
	 */
	String ITEMS = "items";

	@Override
	@ClassDefault(SortListFormActionOp.class)
	Class<? extends SortListFormActionOp> getImplementationClass();

	/**
	 * {@link ModelName} of the {@link FormGroup} representing the list property being updated.
	 */
	@Name(LIST)
	ModelName getList();

	/**
	 * @see #getList()
	 */
	void setList(ModelName buildModelName);

	/**
	 * Ordered names of entries in the {@link #getList()} member.
	 */
	@Name(ITEMS)
	@ListBinding
	List<String> getItems();

	/**
	 * @see #getItems()
	 */
	void setItems(List<String> labels);

	/**
	 * Implementation aspect of {@link SortListFormAction}.
	 */
	class SortListFormActionOp extends AbstractApplicationActionOp<SortListFormAction> {

		public SortListFormActionOp(InstantiationContext context, SortListFormAction config) {
			super(context, config);
		}

		@Override
		protected Object processInternal(ActionContext context, Object argument) throws Throwable {
			FormContainer listGroup = (FormContainer) ModelResolver.locateModel(context, config.getList());
			ValueModel valueModel = EditorFactory.getValueModel(listGroup);
			ListEditor.updateListOrder(config, listGroup, valueModel, config.getItems());
			return argument;
		}

	}

}
