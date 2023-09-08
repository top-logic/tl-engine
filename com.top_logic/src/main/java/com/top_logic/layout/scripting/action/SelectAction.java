/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.action;

import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.NamedModel;
import com.top_logic.layout.scripting.recorder.ref.misc.SelectionRef;
import com.top_logic.layout.scripting.runtime.action.SelectActionOp;

/**
 * The {@link ApplicationAction} representing selections.
 * 
 * @author <a href=mailto:Jan.Stolzenburg@top-logic.com>Jan.Stolzenburg</a>
 */
public interface SelectAction extends ApplicationAction {

	/**
	 * How is the selection changed? {@link #INCREMENTAL incremental} or {@link #ABSOLUTE Absolute}?
	 */
	public enum SelectionChangeKind {

		/**
		 * Only for backwards compatibility with old scripts.
		 * <p>
		 * This is the default {@link SelectionChangeKind}, as it is the first enum value. It is
		 * used to detect old scripts, where the {@link SelectionChangeKind} did not exist.
		 * </p>
		 */
		LEGACY,

		/**
		 * If a new object is selected, it is selected additionally to the already selected objects.
		 * It does not replace the old selection.
		 */
		INCREMENTAL,

		/**
		 * If a new object is selected, it replaces the old selection. If an object is deselected,
		 * the selection is cleared.
		 */
		ABSOLUTE;
	}

	@Override
	@ClassDefault(SelectActionOp.class)
	Class<SelectActionOp> getImplementationClass();

	/**
	 * Returns a {@link ModelName} identifying the {@link NamedModel} on which the selectee is being
	 * selected or unselected.
	 */
	ModelName getSelectionModelName();

	/**
	 * Sets the {@link ModelName} identifying the {@link NamedModel} on which the selectee is being
	 * selected or unselected.
	 */
	void setSelectionModelName(ModelName modelName);

	/** Returns the {@link SelectionRef} containing the selectee and its requested selection state. */
	SelectionRef getSelection();

	/** Sets the {@link SelectionRef} containing the selectee and its requested selection state. */
	void setSelection(SelectionRef selecteeRef);

	/**
	 * What {@link SelectionChangeKind kind} of change? {@link SelectionChangeKind#INCREMENTAL
	 * incremental} or {@link SelectionChangeKind#ABSOLUTE Absolute}?
	 */
	SelectionChangeKind getChangeKind();

	/**
	 * @see #getChangeKind()
	 */
	void setChangeKind(SelectionChangeKind changeKind);

}
