/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.selection;

import java.io.IOException;
import java.util.ArrayList;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.layout.AbstractDisplayValue;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.buttonbar.ButtonBarControl;
import com.top_logic.layout.buttonbar.ButtonBarFactory;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.control.DefaultButtonRenderer;
import com.top_logic.layout.form.control.SelectControl;
import com.top_logic.layout.form.control.TextInputControl;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.ListField;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.form.selection.ListModifySelection.ModifyMode;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.list.DoubleClickCommand;
import com.top_logic.layout.list.DropAcceptor;
import com.top_logic.layout.list.ListControl;

/**
 * {@link ControlProvider} to create controls for a pop up selector in list form.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class DefaultListSelectDialogControlProvider extends DefaultFormFieldControlProvider {

	/** Control to render the field to type search string */
	private final TextInputControl _patternFieldControl;

	/** Control to render the option field. Not <code>null</code> */
	private final ListControl _optionFieldControl;

	/** Control to render the field containing the current collection. May be <code>null</code> */
	private final ListControl _selectionFieldControl;

	/** control rendering the select box to change page */
	private final SelectControl _pageChangeControl;

	private final ButtonBarControl _buttonBarControl;

	public DefaultListSelectDialogControlProvider(SelectorContext context, SelectDialogConfig selectDialogConfig) {
		if (!context.hasMember(SelectorContext.PAGE_FIELD_NAME)) {
			_pageChangeControl = null;
		} else {
			_pageChangeControl = createPageChangeControl(context);
		}

		// create Control for option field
		if (selectDialogConfig.getShowOptions()) {
			_optionFieldControl = createOptionFieldControl(context, selectDialogConfig);
		} else {
			_optionFieldControl = null;
		}

		if (!context.hasMember(SelectorContext.SELECTION_FIELD_NAME)) {
			_selectionFieldControl = null;
		}
		else {
			_selectionFieldControl = createSelectionFieldControl(context, _optionFieldControl, selectDialogConfig);
		}

		_patternFieldControl = createPatternFieldControl(context, _selectionFieldControl, _optionFieldControl, _pageChangeControl);

		_buttonBarControl = createButtonBarControl(context);

	}

	private ButtonBarControl createButtonBarControl(SelectorContext context) {
		ArrayList<FormMember> buttonList = CollectionUtil.toList(context.getButtons().getDescendants());
		return ButtonBarFactory
			.createButtonBar(CollectionUtil.dynamicCastView(CommandModel.class, buttonList));
	}

	private SelectControl createPageChangeControl(SelectorContext context) {
		FormMember member = context.getMember(SelectorContext.PAGE_FIELD_NAME);
		SelectControl control = new SelectControl(((SelectField) member));
		// Width must be smaller than 100%, due to defined border of select control
		control.setInputStyle("width:98%;");
		return control;
	}

	private TextInputControl createPatternFieldControl(SelectorContext context,
			ListControl selectionFieldControl, ListControl optionFieldControl, SelectControl pageChangeControl) {
		StringField patternField = (StringField) context.getMember(SelectorContext.PATTERN_FIELD_NAME);
		TextInputControl result = new TextInputControl(patternField);
		// Width must be smaller than 100%, due to defined border of text input control
		result.setInputStyle("width:97%;float:left;");

		PatternInput onKeyUp = new PatternInput(result, optionFieldControl, selectionFieldControl, pageChangeControl);
		result.setOnInput(onKeyUp);
		return result;
	}

	/**
	 * Creates the control for the field containing the current selection (field with name
	 * {@link SelectorContext#SELECTION_FIELD_NAME} in given {@link SelectorContext}).
	 */
	private ListControl createSelectionFieldControl(SelectorContext context, ListControl optionFieldControl, SelectDialogConfig selectDialogConfig) {
		ListField selectionField = (ListField) context.getMember(SelectorContext.SELECTION_FIELD_NAME);
		ListControl resultControl = new ListControl(selectionField);
		ListModifySelection removeAction = new ListModifySelection(context, ModifyMode.REMOVE);
		resultControl.setListRenderer(selectDialogConfig.getDialogListRenderer());

		// connect selection field control with option field control via D&D
		DropAcceptor<Object> comparableAcceptor = new SelectionDropAccessor(context.getSelectField());
		if (context.getSelectField().hasCustomOrder()) {
			resultControl.addDropTarget(resultControl);
		} else {
			resultControl.setDropAcceptor(comparableAcceptor);
		}

		if (selectDialogConfig.getShowOptions()) {
			optionFieldControl.addDropTarget(resultControl);
			optionFieldControl.setDropAcceptor(comparableAcceptor);
			resultControl.setDblClickAction(new DoubleClickCommand(removeAction));
			resultControl.addDropTarget(optionFieldControl);
		}
		return resultControl;
	}

	/**
	 * Creates a control for the option field. Never <code>null</code>.
	 */
	private ListControl createOptionFieldControl(SelectorContext context, SelectDialogConfig selectDialogConfig) {
		ListField optionsField = (ListField) context.getMember(SelectorContext.OPTIONS_FIELD_NAME);
		ListControl resultControl = new ListControl(optionsField);
		if (context.getSelectField().isMultiple()) {
			final ListModifySelection addAction = new ListModifySelection(context, ModifyMode.ADD);
			resultControl.setDblClickAction(new DoubleClickCommand(addAction));
		} else {
			DoubleClickCommand dblClickCommand = new DoubleClickCommand(context.getAcceptCommand());
			dblClickCommand.setIsWaitPaneRequested(true);
			resultControl.setDblClickAction(dblClickCommand);
		}
		resultControl.setListRenderer(selectDialogConfig.getDialogListRenderer());
		return resultControl;
	}

	@Override
	public Control visitStringField(StringField member, Void arg) {
		if (member.getName().equals(SelectorContext.PATTERN_FIELD_NAME)) {
			return _patternFieldControl;
		}
		return super.visitStringField(member, arg);
	}

	@Override
	public Control visitSelectField(SelectField member, Void arg) {
		if (member.getName().equals(SelectorContext.PAGE_FIELD_NAME)) {
			return _pageChangeControl;
		}
		return super.visitSelectField(member, arg);
	}

	@Override
	public Control visitCommandField(CommandField member, Void arg) {
		ButtonControl control = new ButtonControl(member, DefaultButtonRenderer.NO_REASON_INSTANCE);
		member.setShowProgress();
		member.set(ButtonControl.SHOW_PROGRESS_DIV_ID, new AbstractDisplayValue() {
			@SuppressWarnings("synthetic-access")
			@Override
			public void append(DisplayContext context, Appendable out) throws IOException {
				_buttonBarControl.fetchID(context.getExecutionScope().getFrameScope());
				_buttonBarControl.appendProgressDivID(out);
			}
		});
		return control;
	}

	@Override
	public Control visitListField(final ListField member, Void arg) {
		if (member.getName().equals(SelectorContext.OPTIONS_FIELD_NAME)) {
			return _optionFieldControl;
		}
		if (member.getName().equals(SelectorContext.SELECTION_FIELD_NAME)) {
			assert _selectionFieldControl != null : "visit for field " + SelectorContext.SELECTION_FIELD_NAME
				+ " which does not exist. ";
			return _selectionFieldControl;
		}
		return super.visitListField(member, arg);
	}

	@Override
	public Control visitFormGroup(FormGroup member, Void arg) {
		if (member.getName().equals(SelectorContext.BUTTONS)) {
			return _buttonBarControl;
		}
		return super.visitFormGroup(member, arg);
	}
}
