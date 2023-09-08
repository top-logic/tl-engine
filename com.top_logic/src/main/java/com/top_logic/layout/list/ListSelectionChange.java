/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.list;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ListModel;
import javax.swing.ListSelectionModel;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.form.model.ListField;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.AbstractApplicationActionOp;

/**
 * {@link ApplicationAction} that changes the selection in a {@link ListField}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ListSelectionChange extends ApplicationAction {

	@Override
	@ClassDefault(Op.class)
	Class<? extends AbstractApplicationActionOp<?>> getImplementationClass();

	/**
	 * Description of the {@link ListField} to update.
	 */
	ModelName getModel();

	/**
	 * @see #getModel()
	 */
	void setModel(ModelName value);

	/**
	 * Descriptions of values added to the {@link #getModel()} selection.
	 */
	@EntryTag("element")
	List<ModelName> getAdded();

	/**
	 * Descriptions of values removed to the {@link #getModel()} selection.
	 */
	@EntryTag("element")
	List<ModelName> getRemoved();

	/**
	 * Default implementation of {@link ListSelectionChange}.
	 */
	class Op<S extends ListSelectionChange> extends AbstractApplicationActionOp<S> {

		/**
		 * Creates a {@link ListSelectionChange.Op} from configuration.
		 * 
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public Op(InstantiationContext context, S config) {
			super(context, config);
		}

		@Override
		protected Object processInternal(ActionContext context, Object argument) throws Throwable {
			ListField field = (ListField) ModelResolver.locateModel(context, getConfig().getModel());

			Set<Object> added = locate(context, field, getConfig().getAdded());
			Set<Object> removed = locate(context, field, getConfig().getRemoved());

			ListModel<?> listModel = field.getListModel();
			ListSelectionModel selectionModel = field.getSelectionModel();
			for (int n = 0, cnt = listModel.getSize(); n < cnt; n++) {
				Object element = listModel.getElementAt(n);
				if (added.contains(element)) {
					selectionModel.addSelectionInterval(n, n);
				}
				else if (removed.contains(element)) {
					selectionModel.removeSelectionInterval(n, n);
				}
			}

			return argument;
		}

		private Set<Object> locate(ActionContext context, ListField valueContext, List<ModelName> added2) {
			Set<Object> added = new HashSet<>();
			for (ModelName entryRef : added2) {
				Object entry = ModelResolver.locateModel(context, valueContext, entryRef);
				added.add(entry);
			}
			return added;
		}

	}
}
