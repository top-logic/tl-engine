/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.selection;

import java.io.IOException;

import com.top_logic.basic.xml.TagUtil;
import com.top_logic.layout.AbstractDisplayValue;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.form.control.AbstractFormMemberControl;
import com.top_logic.layout.form.control.SelectControl;
import com.top_logic.layout.form.control.TextInputControl;
import com.top_logic.layout.list.ListControl;

/**
 * {@link PatternInput} provides the onkeyup value of some {@link TextInputControl}
 * 
 * @see TextInputControl#setOnInput(com.top_logic.layout.DynamicText)
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class PatternInput extends AbstractDisplayValue {

	private final TextInputControl _patternFieldControl;

	private final ListControl _optionFieldControl;

	private final ListControl _selectionFieldControl;

	private final SelectControl _pageChangeControl;

	public PatternInput(TextInputControl patternFieldControl, ListControl optionFieldControl,
			ListControl selectionFieldControl, SelectControl pageChangeControl) {
		_patternFieldControl = patternFieldControl;
		_optionFieldControl = optionFieldControl;
		_selectionFieldControl = selectionFieldControl;
		_pageChangeControl = pageChangeControl;
	}

	@Override
	public void append(DisplayContext context, Appendable out) throws IOException {
		FrameScope frameScope = context.getExecutionScope().getFrameScope();

		out.append("return services.form.SelectionControl.PatternField.handleInput(arguments[0],");
		appendPatternFieldIds(out);
		out.append(',');
		appendOptionFieldId(out, frameScope);
		out.append(',');
		appendSelectionFieldId(out, frameScope);
		out.append(',');
		appendPageChangeId(out, frameScope);
		out.append(");");
	}

	private void appendPatternFieldIds(Appendable out) throws IOException {
		TagUtil.writeJsString(out, (_patternFieldControl.getID()));
		out.append(',');
		jsAppendInputId(out, _patternFieldControl);
	}

	private void appendOptionFieldId(Appendable out, FrameScope frameScope) {
		_optionFieldControl.fetchID(frameScope);
		TagUtil.writeJsString(out, (_optionFieldControl.getID()));
	}

	private void appendPageChangeId(Appendable out, FrameScope frameScope) throws IOException {
		if (_pageChangeControl != null) {
			_pageChangeControl.fetchID(frameScope);
			jsAppendInputId(out, _pageChangeControl);
		} else {
			TagUtil.writeJsNullLiteral(out);
		}
	}

	private static void jsAppendInputId(Appendable out, AbstractFormMemberControl control) throws IOException {
		TagUtil.beginJsString(out);
		// Note: It is assumed that the control ID does not require encoding.
		control.appendInputId(out);
		TagUtil.endJsString(out);
	}

	private void appendSelectionFieldId(Appendable out, FrameScope frameScope) {
		String selectionFieldID;
		if (_selectionFieldControl != null) {
			_selectionFieldControl.fetchID(frameScope);
			selectionFieldID = _selectionFieldControl.getID();
		} else {
			selectionFieldID = null;
		}
		TagUtil.writeJsString(out, (selectionFieldID));
	}

}
