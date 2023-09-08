/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.model;

import com.top_logic.layout.scripting.recorder.ref.AbstractModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * {@link ModelNamingScheme} of the {@link NoSelectionModel}, uses the {@link NoSelectionModelName}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class NoSelectionModelNaming extends
		AbstractModelNamingScheme<NoSelectionModel, NoSelectionModelNaming.NoSelectionModelName> {

	/**
	 * {@link ModelName} of the {@link NoSelectionModel},
	 * used by the {@link NoSelectionModelNaming}.
	 */
	public interface NoSelectionModelName extends ModelName {
		// No information needed, as the model is a singleton
	}

	@Override
	public Class<NoSelectionModelName> getNameClass() {
		return NoSelectionModelName.class;
	}

	@Override
	public Class<NoSelectionModel> getModelClass() {
		return NoSelectionModel.class;
	}

	@Override
	public NoSelectionModel locateModel(ActionContext context, NoSelectionModelName name) {
		return NoSelectionModel.INSTANCE;
	}

	@Override
	protected void initName(NoSelectionModelName name, NoSelectionModel model) {
		// Nothing to do, as no information are needed, as the model is a singleton.
	}

}
