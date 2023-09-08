/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.tag;

import javax.servlet.jsp.tagext.Tag;

import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.tag.CustomInputTag;
import com.top_logic.layout.form.tag.ErrorTag;
import com.top_logic.layout.form.tag.LabelTag;
import com.top_logic.layout.form.template.ControlProvider;

/**
 * {@link BoxTag} for creating a lable/description cell for a single {@link FormMember}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class InputCellTag extends AbstractInputCellTag {

	/**
	 * XML name of this tag.
	 */
	public static final String INPUT_CELL_TAG = "form:inputCell";

	/**
	 * Default value of {@link #setColon(boolean)}.
	 * 
	 * <p>
	 * Used for changing the default behavior in contrast to {@link LabelTag}.
	 * </p>
	 */
	public static final boolean COLON_DEFAULT = true;

	private final LabelTag _labelTag = new LabelTag();
	private final CustomInputTag _inputTag = new CustomInputTag();
	private final ErrorTag _errorTag = new ErrorTag();

	private String _name;

	private boolean _errorAsText = false;

	/**
	 * @see #setColon(boolean)
	 */
	private boolean _colon = COLON_DEFAULT;

	@Override
	protected String getTagName() {
		return INPUT_CELL_TAG;
	}

	/**
	 * The {@link FormField} name of the field to display.
	 */
	public void setName(String name) {
		_name = name;
	}

	/**
	 * @see LabelTag#setColon(boolean)
	 */
	public void setColon(boolean colon) {
		_colon = colon;
	}

	/**
	 * Whether a validation error should not be displayed as icon.
	 */
	public void setErrorAsText(boolean errorAsText) {
		_errorAsText = errorAsText;
	}

	/**
	 * Sets the {@link ControlProvider} for displaying the form field.
	 */
	public void setControlProvider(ControlProvider controlProvider) {
		_inputTag.setControlProvider(controlProvider);
	}

	@Override
	protected Tag setupErrorTag() {
		ErrorTag errorTag = _errorTag;

		setupTag(errorTag);
		errorTag.setName(_name);
		errorTag.setIcon(!_errorAsText);
		return errorTag;
	}

	@Override
	protected Tag setupInputTag() {
		CustomInputTag inputTag = _inputTag;

		setupTag(inputTag);
		inputTag.setName(_name);
		return inputTag;
	}

	@Override
	protected Tag setupLabelTag() {
		LabelTag labelTag = _labelTag;

		setupTag(labelTag);
		labelTag.setName(_name);
		labelTag.setColon(_colon);
		return labelTag;
	}

	@Override
	protected void tearDown() {
		super.tearDown();
		_name = null;
		_colon = COLON_DEFAULT;
		_errorAsText = false;
	}
}
