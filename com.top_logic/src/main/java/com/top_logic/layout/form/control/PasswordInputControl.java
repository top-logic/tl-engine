/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.awt.Color;
import java.io.IOException;
import java.util.Map;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.gui.ThemeVar;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.TemplateVariable;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.format.ColorFormat;
import com.top_logic.util.Resources;

/**
 * A {@link TextInputControl} for passwords, which will be validated at client-side.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class PasswordInputControl extends TextInputControl {
	
	/**
	 * {@link TemplateVariable} for rendering the password validator view.
	 * 
	 * @see #writePasswordValidationSnippet(TagWriter)
	 */
	public static final String VALIDATOR_PROPERTY = "validator";

	private static final String PASSWORD_INPUT_TYPE = "password";
	private static final ResKey PASSWORD_VERDICT_KEY_WEAK = I18NConstants.PASSWORD_WEAK;
	private static final ResKey PASSWORD_VERDICT_KEY_NORMAL = I18NConstants.PASSWORD_NORMAL;
	private static final ResKey PASSWORD_VERDICT_KEY_MEDIUM = I18NConstants.PASSWORD_MEDIUM;
	private static final ResKey PASSWORD_VERDICT_KEY_STRONG = I18NConstants.PASSWORD_STRONG;
	private static final ResKey PASSWORD_VERDICT_KEY_VERY_STRONG = I18NConstants.PASSWORD_VERY_STRONG;

	private int minCharLength;
	private int tlCriteriaCount;
	
	/**
	 * Create a new {@link PasswordInputControl} with the default {@link #COMMANDS} map.
	 */
	public PasswordInputControl(FormField model, int minCharLength, int tlCriteriaCount) {
		this(model, COMMANDS, minCharLength, tlCriteriaCount);
	}

	/** Create a new {@link PasswordInputControl}. */
	public PasswordInputControl(FormField model, Map commands, int minCharLength, int tlCriteriaCount) {
		super(model, commands);
		setType(PASSWORD_INPUT_TYPE);
		this.minCharLength = minCharLength;
		this.tlCriteriaCount = tlCriteriaCount;
	}
	
	@Override
	protected void writeEditable(DisplayContext context, TagWriter out) throws IOException {
		Icons.PASSWORD_INPUT_EDIT_TEMPLATE.get().write(context, out, this);
	}

	/** Validates the entered password. */
	@TemplateVariable(VALIDATOR_PROPERTY)
	public void writePasswordValidationSnippet(TagWriter out) throws IOException {
		FormField field = (FormField) getModel();
		if (!field.isDisabled()) {
			String elementId = getInputIdForJQuery();
			writeJQuerySnippet(out, elementId);
		}
	}

	private String getInputIdForJQuery() {
		return getInputId().replace(".", "\\\\.");
	}

	private void writeJQuerySnippet(TagWriter out, String elementId) throws IOException {
		out.beginScript();
		writeJQuerySnippetContent(out, elementId);
		out.endScript();
	}

	private void writeJQuerySnippetContent(TagWriter out, String elementId) throws IOException {
		out.append("(function(){");
		out.append("var options = {passwordFieldId: ");
		writeIdJsString(out);
		out.append(",minChar: ");
		out.writeInt(minCharLength);
		out.append(",tlCriteriaMinCount:");
		out.writeInt(tlCriteriaCount);
		out.append(",verdicts:");
		appendVerdictMessages(out);
		out.append(",colors:");
		appendBasswordBarColors(out);
		out.append("};");
		out.append("$(document).ready( function() {$('#");
		out.append(elementId);
		out.append("').pstrength(options);});");
		out.append("})();");
	}

	private void appendVerdictMessages(TagWriter out) throws IOException {
		out.append("[");
		appendPasswordVerdictToBuilder(out, PASSWORD_VERDICT_KEY_WEAK);
		out.append(",");
		appendPasswordVerdictToBuilder(out, PASSWORD_VERDICT_KEY_NORMAL);
		out.append(",");
		appendPasswordVerdictToBuilder(out, PASSWORD_VERDICT_KEY_MEDIUM);
		out.append(",");
		appendPasswordVerdictToBuilder(out, PASSWORD_VERDICT_KEY_STRONG);
		out.append(",");
		appendPasswordVerdictToBuilder(out, PASSWORD_VERDICT_KEY_VERY_STRONG);
		out.append("]");
	}

	private void appendPasswordVerdictToBuilder(TagWriter out, ResKey messageKey) throws IOException {
		Resources resources = Resources.getInstance();
		out.writeJsString(resources.getString(messageKey));
	}

	private void appendBasswordBarColors(TagWriter out) throws IOException {
		out.append("[");
		appendPasswordColorToBuilder(out, Icons.PASSWORD_BAR_WEAK_COLOR);
		out.append(",");
		appendPasswordColorToBuilder(out, Icons.PASSWORD_BAR_NORMAL_COLOR);
		out.append(",");
		appendPasswordColorToBuilder(out, Icons.PASSWORD_BAR_MEDIUM_COLOR);
		out.append(",");
		appendPasswordColorToBuilder(out, Icons.PASSWORD_BAR_STRONG_COLOR);
		out.append(",");
		appendPasswordColorToBuilder(out, Icons.PASSWORD_BAR_VERY_STRONG_COLOR);
		out.append("]");
	}

	private void appendPasswordColorToBuilder(TagWriter out, ThemeVar<Color> colorKey) throws IOException {
		out.append("'");
		out.append(getPasswordBarColor(colorKey));
		out.append("'");
	}

	private String getPasswordBarColor(ThemeVar<Color> colorKey) {
		return ColorFormat.formatColor(ThemeFactory.getTheme().getValue(colorKey));
	}
}
