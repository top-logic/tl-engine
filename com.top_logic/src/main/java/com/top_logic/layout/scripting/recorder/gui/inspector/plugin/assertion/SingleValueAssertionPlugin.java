/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion;

import java.util.List;

import org.w3c.dom.Document;

import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.template.FormPatternConstants;
import com.top_logic.layout.form.template.FormTemplateConstants;
import com.top_logic.layout.scripting.action.assertion.GuiAssertion;
import com.top_logic.mig.html.HTMLConstants;

/**
 * {@link AssertionPlugin} that creates a UI with exactly one expected value field.
 * 
 * @param <S>
 *        The type of the {@link FormField} which holds the value of the model in the GUI.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class SingleValueAssertionPlugin<M, S extends FormField> extends AssertionPlugin<M> {

	private static final String FIELD_NAME_EXPECTED_VALUE = "expected";

	private static final Document TEMPLATE = DOMUtil.parseThreadSafe(
		"	<tbody"
			+ "		xmlns='" + HTMLConstants.XHTML_NS + "'"
			+ "		xmlns:t='" + FormTemplateConstants.TEMPLATE_NS + "'"
			+ "		xmlns:p='" + FormPatternConstants.PATTERN_NS + "'"
			+ "	>"
			+ "		<tr>"
			+ "			<td class='content'>"
			+ "				<p:field name='" + FIELD_NAME_ADD_ASSERT + "' style='input' />"
			+ "			</td>"
			+ "			<td class='label'>"
			+ "				<p:field name='" + CONTENT_GROUP + "." + FIELD_NAME_EXPECTED_VALUE + "' style='label' />"
			+ "			</td>"
			+ "			<td class='content'>"
			+ "				<p:field name='" + CONTENT_GROUP + "." + FIELD_NAME_EXPECTED_VALUE + "' style='input' />"
			+ "			</td>"
			+ "		</tr>"
			+ "		<tr>"
			+ "			<td>"
			+ "			</td>"
			+ "			<td class='label'>"
			+ "				<p:field name='" + CONTENT_GROUP + "." + FIELD_NAME_COMMENT + "' style='label' />"
			+ "			</td>"
			+ "			<td class='content'>"
			+ "				<p:field name='" + CONTENT_GROUP + "." + FIELD_NAME_COMMENT + "' style='input' />"
			+ "			</td>"
			+ "		</tr>"
			+ "	</tbody>");

	private S expectedValueField;

	/**
	 * Creates a {@link SingleValueAssertionPlugin}.
	 */
	public SingleValueAssertionPlugin(M model, boolean assertByDefault, String internalName) {
		super(model, assertByDefault, internalName);
	}

	@Override
	protected void initAssertionContents(FormContainer group) {
		expectedValueField = createValueField(FIELD_NAME_EXPECTED_VALUE);
		expectedValueField.initializeField(getInitialValue());

		group.addMember(expectedValueField);
	}

	/**
	 * Returns the {@link FormField} storing the value the user is expecting.
	 */
	protected final S getExpectedValueField() {
		return expectedValueField;
	}

	/**
	 * Creates the {@link FormField} storing the value the user is expecting.
	 */
	protected abstract S createValueField(String name);

	/**
	 * Returns the initial value for this assertion.
	 */
	protected abstract Object getInitialValue();

	@Override
	protected Document getFormTemplateDocument() {
		return TEMPLATE;
	}

	@Override
	protected List<? extends GuiAssertion> buildAssertions() {
		return toList(buildAssertion());
	}

	protected abstract GuiAssertion buildAssertion();

}
