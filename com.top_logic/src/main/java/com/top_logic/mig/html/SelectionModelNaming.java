/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

import com.top_logic.basic.col.Maybe;
import com.top_logic.layout.scripting.recorder.ref.GlobalModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * {@link ModelNamingScheme} of the {@link SelectionModel}, uses the {@link SelectionModelName}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class SelectionModelNaming extends
		GlobalModelNamingScheme<SelectionModel, SelectionModelNaming.SelectionModelName> {

	/** {@link ModelName} of the {@link SelectionModel}, used by the {@link SelectionModelNaming}. */
	public interface SelectionModelName extends ModelName {

		/**
		 * The {@link ModelName} of the {@link SelectionModel#getOwner() selecton model's owner}.
		 */
		ModelName getOwner();

		/** @see #getOwner() */
		void setOwner(ModelName value);

	}

	@Override
	public Class<SelectionModelName> getNameClass() {
		return SelectionModelName.class;
	}

	@Override
	public Class<SelectionModel> getModelClass() {
		return SelectionModel.class;
	}

	@Override
	public SelectionModel locateModel(ActionContext context, SelectionModelName name) {
		SelectionModelOwner owner = (SelectionModelOwner) ModelResolver.locateModel(context, name.getOwner());
		return owner.getSelectionModel();
	}

	@Override
	public Maybe<SelectionModelName> buildName(SelectionModel model) {
		Maybe<? extends ModelName> modelName = ModelResolver.buildModelNameIfAvailable(model.getOwner());
		if (!modelName.hasValue()) {
			return Maybe.none();
		}
		SelectionModelName name = createName();
		name.setOwner(modelName.get());
		return Maybe.some(name);
	}

}
