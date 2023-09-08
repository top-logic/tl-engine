/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.messagebox;

import java.util.Optional;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.html.template.TagTemplate;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.ValueDisplayControl.ValueDisplay;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.template.FormPatternConstants;
import com.top_logic.layout.form.template.FormTemplate;
import com.top_logic.layout.form.template.FormTemplateConstants;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.util.Resources;

/**
 * {@link AbstractFormDialog} that can display a single input element.
 * 
 * @see MessageBox Confirm dialogs without further user interaction.
 * 
 * @deprecated Use {@link SimpleTemplateDialog} with {@link TagTemplate}s.
 */
@Deprecated
public abstract class SimpleFormDialog extends AbstractFormDialog {

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

	private Optional<String> _iconSrc;

	@Deprecated
	private final ResPrefix _resourcePrefix;

	private final ResKey _header;

	private final ResKey _message;

	/**
	 * Creates a {@link SimpleFormDialog}.
	 * 
	 * @deprecated Use
	 *             {@link #SimpleFormDialog(ResKey, ResKey, ResKey, DisplayDimension, DisplayDimension)}
	 */
	@Deprecated
	public SimpleFormDialog(ResPrefix resourcePrefix, DisplayDimension width, DisplayDimension height) {
		this(resourcePrefix, width, height, null);
	}

	/**
	 * Creates a {@link SimpleFormDialog} with an icon.
	 * 
	 * @deprecated Use
	 *             {@link #SimpleFormDialog(ResKey, ResKey, ResKey, DisplayDimension, DisplayDimension, String)}
	 */
	@Deprecated
	public SimpleFormDialog(ResPrefix resourcePrefix, DisplayDimension width, DisplayDimension height, String iconSrc) {
		super(resourcePrefix.key("title"), width, height);
		_resourcePrefix = resourcePrefix;

		_header = resourcePrefix.key(HEADER);
		_message = resourcePrefix.key(MESSAGE);
		_iconSrc = Optional.ofNullable(iconSrc);
	}

	/**
	 * Creates a {@link SimpleFormDialog}.
	 */
	public SimpleFormDialog(ResKey titleKey, ResKey header, ResKey message, DisplayDimension width,
			DisplayDimension height) {
		this(titleKey, header, message, width, height, null);
	}

	/**
	 * Creates a {@link SimpleFormDialog} with an icon.
	 */
	public SimpleFormDialog(ResKey titleKey, ResKey header, ResKey message, DisplayDimension width,
			DisplayDimension height, String iconSrc) {
		super(titleKey, width, height);

		_header = header;
		_message = message;
		_iconSrc = Optional.ofNullable(iconSrc);

		_resourcePrefix = ResPrefix.NONE;
	}

	@Override
	protected FormTemplate getTemplate() {
		StringBuilder builder = new StringBuilder();

		builder.append("<div");
		builder.append(" xmlns='" + HTMLConstants.XHTML_NS + "'");
		builder.append(" xmlns:t='" + FormTemplateConstants.TEMPLATE_NS + "'");
		builder.append(" xmlns:p='" + FormPatternConstants.PATTERN_NS + "'");
		builder.append(">");
		appendImage(builder);
		appendHeader(builder);
		appendMessage(builder);
		appendInput(builder);
		builder.append("</div>");

		return defaultTemplate(DOMUtil.parseThreadSafe(builder.toString()), false, _resourcePrefix);
    }

	@Override
	@Deprecated
	protected ResPrefix getResourcePrefix() {
		return _resourcePrefix;
	}

	@Override
	public FormContext getFormContext() {
		FormContext result = super.getFormContext();

		if (result != null && result.hasMember(INPUT_FIELD)) {
			FormMember input = result.getMember(INPUT_FIELD);
			if (!input.hasLabel()) {
				input.setLabel(Resources.getInstance().getString(_message, null));
			}
		}

		return result;
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
		if (_header != null) {
			formContext.addMember(display(HEADER, _header));
		}
		formContext.addMember(display(MESSAGE, _message));
	}

	private static FormField display(String name, Object value) {
		FormField headerField = FormFactory.newHiddenField(name, value);
		headerField.setControlProvider(ValueDisplay.INSTANCE);
		return headerField;
	}

	private void appendImage(StringBuilder builder) {
		_iconSrc.ifPresent(iconSrc -> {
			builder.append("<div class='mboxImage'>");
			builder.append("<t:img src='" + iconSrc + "' alt=''/>");
			builder.append("</div>");
		});
	}

	private void appendHeader(StringBuilder builder) {
		if (_header != null) {
			builder.append("<div class='mboxHeader'>");
			builder.append("<p:field name='" + HEADER + "' />");
			builder.append("</div>");
		}
	}

	private void appendMessage(StringBuilder builder) {
		builder.append("<div class='mboxMessage'>");
		builder.append("<p:field name='" + MESSAGE + "' />");
		builder.append("</div>");
	}

	private void appendInput(StringBuilder builder) {
		builder.append("<div class='mboxInput'>");
		builder.append("<p:field name='" + INPUT_FIELD + "' />");
		builder.append(HTMLConstants.NBSP);
		builder.append("<p:field name='" + INPUT_FIELD + "' style='" + FormTemplateConstants.STYLE_ERROR_VALUE + "' />");
		builder.append("</div>");
	}

}

