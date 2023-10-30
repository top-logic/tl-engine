/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.messagebox;

import static com.top_logic.layout.form.template.model.Templates.*;

import com.top_logic.basic.util.ResKey;
import com.top_logic.html.template.TagTemplate;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.control.ValueDisplayControl.ValueDisplay;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;

/**
 * {@link AbstractTemplateDialog} that can display a single input element.
 * 
 * @see MessageBox Confirm dialogs without further user interaction.
 */
public abstract class SimpleTemplateDialog extends AbstractTemplateDialog {

	private static final String HEADER = "header";

	private static final String MESSAGE = "message";

	/**
	 * Name of the single input field displayed in this dialog.
	 * 
	 * <p>
	 * If this name is changed, {@link #getTemplate()} must be overridden.
	 * </p>
	 */
	public static final String INPUT_FIELD = "input";

	private ThemeImage _icon;

	private final ResKey _header;

	private final ResKey _message;

	/**
	 * Creates a {@link SimpleTemplateDialog}.
	 */
	public SimpleTemplateDialog(ResKey titleKey, ResKey header, ResKey message, DisplayDimension width,
			DisplayDimension height) {
		this(titleKey, header, message, width, height, null);
	}

	/**
	 * Creates a {@link SimpleTemplateDialog} with an icon.
	 */
	public SimpleTemplateDialog(ResKey titleKey, ResKey header, ResKey message, DisplayDimension width,
			DisplayDimension height, ThemeImage icon) {
		super(titleKey, width, height);

		_header = header;
		_message = message;
		_icon = icon;
	}

	@Override
	protected TagTemplate getTemplate() {
		return div(
			_icon != null ? div(css("mboxImage"), htmlTemplate(_icon)) : empty(),
			div(css("mboxHeader"), member(HEADER)),
			div(css("mboxMessage"), member(MESSAGE)),
			div(css("mboxInput"), member(INPUT_FIELD), error(INPUT_FIELD)));
    }

	@Override
	protected FormContext createFormContext() {
		FormContext result = super.createFormContext();
		createHeaderMembers(result);
		return result;
	}

	/**
	 * Creates {@link #HEADER} and {@link #MESSAGE} fields.
	 */
	protected void createHeaderMembers(FormContext formContext) {
		formContext.addMember(display(HEADER, _header));
		formContext.addMember(display(MESSAGE, _message));
	}

	private static FormField display(String name, Object value) {
		FormField headerField = FormFactory.newHiddenField(name, value);
		headerField.setControlProvider(ValueDisplay.INSTANCE);
		return headerField;
	}

}

