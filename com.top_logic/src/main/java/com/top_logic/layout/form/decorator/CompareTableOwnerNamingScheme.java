/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.decorator;

import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.scripting.recorder.ref.AbstractModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.recorder.ref.value.ValueRef;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.table.TableDataOwner;

/**
 * {@link ModelNamingScheme} to find the {@link TableDataOwner owner} of a comparison table.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CompareTableOwnerNamingScheme extends
		AbstractModelNamingScheme<CompareTableOwner, CompareTableOwnerNamingScheme.Name> {

	/**
	 * {@link ModelName} for {@link CompareTableOwnerNamingScheme}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Name extends ModelName {

		/**
		 * A {@link ValueRef} for the field for which the comparison table is created.
		 */
		ModelName getField();
		
		/**
		 * Setter for {@link #getField()}
		 */
		void setField(ModelName field);
	}

	@Override
	public Class<Name> getNameClass() {
		return Name.class;
	}

	@Override
	public Class<CompareTableOwner> getModelClass() {
		return CompareTableOwner.class;
	}

	@Override
	public CompareTableOwner locateModel(ActionContext context, Name name) {
		FormMember member = (FormMember) context.resolve(name.getField());
		return CompareTableOwner.getOwner(member);
	}

	@Override
	protected void initName(Name name, CompareTableOwner model) {
		name.setField(ModelResolver.buildModelName(model.getHolder()));
	}

}
