/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.ui.form;

import javax.swing.ListModel;

import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.form.model.ListField;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;

/**
 * {@link ModelNamingScheme} that resolves {@link SelectField} options using only their labels.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ListFieldIndexNaming extends ModelNamingScheme<ListField, Object, ListFieldIndexNaming.Name> {

	/**
	 * {@link ModelName} pointing to a {@link SelectField} option by its label.
	 */
	public interface Name extends ModelName {

		/**
		 * The option label.
		 */
		int getIndex();

		/**
		 * @see #getIndex()
		 */
		void setIndex(int value);

	}

	@Override
	public Class<Name> getNameClass() {
		return Name.class;
	}

	@Override
	public Class<Object> getModelClass() {
		return Object.class;
	}

	@Override
	public Class<ListField> getContextClass() {
		return ListField.class;
	}

	@Override
	public Maybe<Name> buildName(ListField valueContext, Object model) {
		int index = index(valueContext, model);
		if (index < 0) {
			return Maybe.none();
		}

		Name name = TypedConfiguration.newConfigItem(Name.class);
		name.setIndex(index);
		return Maybe.some(name);
	}

	private int index(ListField valueContext, Object model) {
		for (int cnt = valueContext.getListModel().getSize(), n = 0; n < cnt; n++) {
			if (valueContext.getListModel().getElementAt(n) == model) {
				return n;
			}
		}
		return -1;
	}

	@Override
	public Object locateModel(ActionContext context, ListField valueContext, Name name) {
		int index = name.getIndex();

		ListModel<?> listModel = valueContext.getListModel();
		if (index >= listModel.getSize()) {
			throw ApplicationAssertions.fail(name, "Cannot resolve list element at index '" + index
				+ "', list in context only has '" + listModel.getSize() + "' entries.");
		}

		return listModel.getElementAt(index);
	}

}
