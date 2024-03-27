/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.io.IOException;

import com.top_logic.basic.xml.TagUtil;
import com.top_logic.layout.AbstractDisplayValue;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.FormConstants;

/**
 * {@link PatternTextInput} appends the "handle on change" command of some {@link TextInputControl}.
 * 
 * @see TextInputControl#setOnInput(com.top_logic.layout.DynamicText)
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public final class PatternTextInput extends AbstractDisplayValue {
	private final TextInputControl _patternFieldControl;

	/**
	 * Creates a {@link PatternTextInput} for a {@link TextInputControl}.
	 * 
	 * @param patternFieldControl
	 *        {@link TextInputControl} with "handle on change" command.
	 */
	public PatternTextInput(TextInputControl patternFieldControl) {
		_patternFieldControl = patternFieldControl;
	}

	@Override
	public void append(DisplayContext context, Appendable out) throws IOException {
		out.append(FormConstants.TEXT_INPUT_CONTROL_CLASS + ".handleOnChange(this, ");
		TagUtil.writeJsString(out, (_patternFieldControl.getID()));
		out.append(")");
	}
}
