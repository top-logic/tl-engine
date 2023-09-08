/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.value.object;

import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ValueResolver;
import com.top_logic.layout.scripting.recorder.ref.value.ValueRef;

/**
 * {@link ValueRef} that can be used to identify the selected object of the referenced value. It is
 * expected that the referenced component is a {@link Selectable}.
 * 
 * @see ValueResolver#visitSelectedModelValue(SelectedModelRef,
 *      com.top_logic.layout.scripting.runtime.ActionContext)
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Deprecated
public interface SelectedModelRef extends ValueRef {

	/**
	 * A reference to the component whose selection is meant by this
	 *         {@link SelectedModelRef}. Has to be a {@link Selectable}.
	 */
	ModelName getComponent();

	/**
	 * @param component
	 *        Has to be a {@link Selectable}.
	 * 
	 * @see #getComponent()
	 */
	void setComponent(ModelName component);

}

