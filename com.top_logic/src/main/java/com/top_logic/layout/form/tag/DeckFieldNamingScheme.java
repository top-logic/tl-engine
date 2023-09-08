/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.tag;

import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.FormMemberBasedName;
import com.top_logic.layout.form.FormMemberNamingScheme;
import com.top_logic.layout.form.model.DeckField;
import com.top_logic.layout.form.model.DeckField.CP.DeckFieldAdapter;
import com.top_logic.layout.scripting.recorder.ref.AbstractModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.NamedModel;
import com.top_logic.layout.scripting.recorder.ref.ReferenceFactory;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.tabbar.TabBarModel;

/**
 * {@link ModelNamingScheme} for a {@link TabBarModel} in a {@link DeckField}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DeckFieldNamingScheme extends AbstractModelNamingScheme<DeckFieldAdapter, DeckFieldNamingScheme.DeckFieldName> {

	/**
	 * Singleton {@link DeckFieldNamingScheme} instance.
	 */
	public static final DeckFieldNamingScheme INSTANCE = new DeckFieldNamingScheme();

	private DeckFieldNamingScheme() {
		/* Private singleton constructor */
	}

	/**
	 * Identifier for the {@link TabBarModel} contained in a {@link DeckField}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public interface DeckFieldName extends FormMemberBasedName {
		// Pure marker interface.
	}

	@Override
	protected void initName(DeckFieldName name, DeckFieldAdapter model) {
		DeckField field = model.getField();
		NamedModel fieldModel = field.getModel();

		name.setFormHandlerName(fieldModel.getModelName());
		name.setPath(ReferenceFactory.referenceField(field));
	}

	@Override
	public Class<DeckFieldAdapter> getModelClass() {
		return DeckFieldAdapter.class;
	}

	@Override
	public Class<DeckFieldName> getNameClass() {
		return DeckFieldName.class;
	}

	@Override
	public DeckFieldAdapter locateModel(ActionContext context, DeckFieldName name) {
		FormMember field = FormMemberNamingScheme.INSTANCE.locateModel(context, name);
		return field.get(DeckField.CP.TAB_BAR_MODEL);
	}

}
