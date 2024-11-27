/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.form;

/**
 * CSS classes required for in-app editing of forms.
 */
public class FormEditorCSS {

	/**
	 * Marker CSS class to identify drop targets of groups where form elements can be placed by
	 * drag&drop operations by the form editor.
	 */
	public static final String FE_CONTAINER = "fe_container";

	/**
	 * CSS class dynamically added to a form element while it is being dragged within the editor.
	 */
	public static final String FE_DRAGGED = "fe_dragged";

	/**
	 * CSS class used by the form editor to mark overlay elements capturing drop events.
	 */
	public static final String FE_DROP_AREA = "fe_dropArea";

	/**
	 * CSS class used by the form editor for text elements within the {@link #FE_DROP_AREA}.
	 */
	public static final String FE_DROP_TEXT = "fe_dropText";

	/**
	 * CSS class dynamically added to a form container when a form element enters its drop area.
	 */
	public static final String FE_PARENT = "fe_parent";

	/** CSS class for elements in which an object can be dropped when editing forms. */
	public static final String FE_DROP_TARGET = "fe_dropTarget";

	/**
	 * CSS class for the form editor to mark an part at the GUI as "locked", i.e. it is not possible
	 * to drag an form element into or out of it's content area.
	 */
	public static final String FE_LOCKED = "fe_locked";

	/**
	 * CSS class for the top-level form editor area, where the form is being composed.
	 */
	public static final String FE_EDITOR = "fe_editor";

	/** CSS class for form wrapper elements. */
	public static final String FE_WRAPPER = "fe_wrapper";

	/**
	 * CSS class dynamically set by the form editor to highlight areas during a drag&drop operation.
	 */
	public static final String FE_HIGHLIGHTED = "fe_highlighted";

}
