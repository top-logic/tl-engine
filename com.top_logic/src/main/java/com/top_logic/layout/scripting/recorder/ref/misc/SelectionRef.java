/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.misc;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.value.BooleanValue;
import com.top_logic.layout.scripting.recorder.ref.value.ValueRef;

/**
 * Represents an Object being selected or unselected and it's requested selection state.
 * 
 * @author     <a href="mailto:Jan.Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public interface SelectionRef extends ConfigurationItem {

	/** Returns a {@link ValueRef} to the Object being selected or unselected. */
	ModelName getSelectee();

	/** Sets the Object being selected or unselected. */
	void setSelectee(ModelName selection);

	/** Returns whether the selectee is meant to be selected (true) or unselected (false). */
	BooleanValue getSelectionState();

	/** Sets whether the selectee is meant to be selected (true) or unselected (false). */
	void setSelectionState(BooleanValue selectionState);

}
