/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

/**
 * Decision about adding or removing an object to/from a current view.
 */
public enum ElementUpdate {

	/**
	 * Ignore the change and keep the current view as is.
	 */
	NO_CHANGE,

	/**
	 * Display the object in the current view.
	 */
	ADD() {
		@Override
		public boolean shouldAdd() {
			return true;
		}
	},

	/**
	 * Remove the object from the current view.
	 */
	REMOVE() {
		@Override
		public boolean shouldRemove() {
			return true;
		}
	},

	/** It is unknown whether the object will be displayed in the current view. */
	UNKNOWN,

	;

	/**
	 * Whether a created/changed object should now be displayed.
	 */
	public boolean shouldAdd() {
		return false;
	}

	/**
	 * Whether a created/changed object should not/no longer be displayed.
	 */
	public boolean shouldRemove() {
		return false;
	}

	/**
	 * Conversion from a boolean decision about displaying an object.
	 */
	public static ElementUpdate fromDecision(boolean shouldDisplay) {
		return shouldDisplay ? ElementUpdate.ADD : ElementUpdate.REMOVE;
	}

}
