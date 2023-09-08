/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

import java.util.Set;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.layout.scripting.recorder.ref.AbstractModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * {@link ModelName} for the selection of a {@link SelectionModel}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SelectionNaming extends AbstractModelNamingScheme<Set, SelectionNaming.Name> {

	/**
	 * Name of the selection of a {@link SelectionModel}
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Name extends ModelName {
		
		/**
		 * Name of the {@link SelectionModel}.
		 */
		@Mandatory
		ModelName getSelectionModel();

		/**
		 * Sets {@link #getSelectionModel()}.
		 */
		void setSelectionModel(ModelName name);
		
		
	}

	@Override
	public Class<Name> getNameClass() {
		return Name.class;
	}

	@Override
	public Class<Set> getModelClass() {
		return Set.class;
	}

	@Override
	public Set locateModel(ActionContext context, Name name) {
		SelectionModel selectionModel = (SelectionModel) ModelResolver.locateModel(context, name.getSelectionModel());
		return selectionModel.getSelection();
	}

	@Override
	protected void initName(Name name, Set model) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected boolean isCompatibleModel(Set model) {
		return false;
	}
}

