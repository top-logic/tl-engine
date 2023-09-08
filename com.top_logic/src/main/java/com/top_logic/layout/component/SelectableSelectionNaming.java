/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component;

import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.UnrecordableNamingScheme;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * {@link ModelNamingScheme} that returns the selection of a {@link Selectable}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class SelectableSelectionNaming
		extends UnrecordableNamingScheme<Object, SelectableSelectionNaming.SelectableSelectionName> {

	/** {@link ModelName} for the {@link SelectableSelectionNaming}. */
	public interface SelectableSelectionName extends ModelName {

		/** Property name of {@link #getSelectable()}. */
		String SELECTABLE = "selectable";

		/** The {@link Selectable} whose selection is referenced. */
		@Name(SELECTABLE)
		ModelName getSelectable();

	}

	/** Creates a {@link SelectableSelectionNaming}. */
	public SelectableSelectionNaming() {
		super(Object.class, SelectableSelectionName.class);
	}

	@Override
	public Object locateModel(ActionContext context, SelectableSelectionName name) {
		Selectable selectable = (Selectable) context.resolve(name.getSelectable());
		return selectable.getSelected();
	}

}
