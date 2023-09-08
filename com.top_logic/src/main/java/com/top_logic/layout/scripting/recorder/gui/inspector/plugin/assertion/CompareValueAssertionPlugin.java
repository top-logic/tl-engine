/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.gui.inspector.plugin.assertion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.w3c.dom.Document;

import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.decorator.ChangeInfo;
import com.top_logic.layout.form.decorator.CompareInfo;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.template.FormPatternConstants;
import com.top_logic.layout.form.template.FormTemplateConstants;
import com.top_logic.layout.scripting.action.assertion.GuiAssertion;
import com.top_logic.layout.scripting.recorder.gui.inspector.I18NConstants;
import com.top_logic.mig.html.HTMLConstants;

/**
 * {@link AssertionPlugin} to create assertion about the comparison to another object.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class CompareValueAssertionPlugin<M> extends AssertionPlugin<M> {

	private static final String FIELD_CHANGED = "changed";

	private static final String FIELD_COMPARE_VALUE = "compare";

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
			+ "				<p:field name='" + CONTENT_GROUP + '.' + FIELD_CHANGED + "' style='label' />"
			+ "			</td>"
			+ "			<td class='content'>"
			+ "				<p:field name='" + CONTENT_GROUP + '.' + FIELD_CHANGED + "' style='input' />"
			+ "			</td>"
			+ "		</tr>"
			+ "		<tr>"
			+ "			<td class='content'>"
			+ "			</td>"
			+ "			<td class='label'>"
			+ "				<p:field name='" + CONTENT_GROUP + '.' + FIELD_COMPARE_VALUE + "' style='label' />"
			+ "			</td>"
			+ "			<td class='content'>"
			+ "				<p:field name='" + CONTENT_GROUP + '.' + FIELD_COMPARE_VALUE + "' style='input' />"
			+ "			</td>"
			+ "		</tr>"
			+ "		<tr>"
			+ "			<td class='content'>"
			+ "			</td>"
			+ "			<td class='label'>"
			+ "				<p:field name='" + CONTENT_GROUP + '.' + FIELD_NAME_COMMENT + "' style='label' />"
			+ "			</td>"
			+ "			<td class='content'>"
			+ "				<p:field name='" + CONTENT_GROUP + '.' + FIELD_NAME_COMMENT + "' style='input' />"
			+ "			</td>"
			+ "		</tr>"
			+ "	</tbody>");

	private SelectField _changedField;

	/** {@link FormField} holding the value to compare with. */
	protected FormField _compareField;

	/** The current {@link CompareInfo} to initialise expectation fields. */
	protected final CompareInfo _compareInfo;

	/**
	 * Creates a new {@link CompareValueAssertionPlugin}.
	 * 
	 * @param model
	 *        see {@link #getModel()}
	 * @param assertByDefault
	 *        see {@link AssertionPlugin#AssertionPlugin(Object, boolean, String)}
	 * @param internalName
	 *        see {@link AssertionPlugin#AssertionPlugin(Object, boolean, String)}
	 * @param compareInfo
	 *        The current compare info to create initial compare values from.
	 */
	public CompareValueAssertionPlugin(M model, boolean assertByDefault,
			String internalName, CompareInfo compareInfo) {
		super(model, assertByDefault, internalName);
		_compareInfo = compareInfo;
	}

	/**
	 * Creates a new {@link CompareValueAssertionPlugin}.
	 * 
	 * @param model
	 *        see {@link #getModel()}
	 * @param compareInfo
	 *        The current compare info to create initial compare values from.
	 * 
	 * @see CompareValueAssertionPlugin#CompareValueAssertionPlugin(Object, boolean, String,
	 *      CompareInfo)
	 */
	public CompareValueAssertionPlugin(M model, CompareInfo compareInfo) {
		this(model, false, "comparison", compareInfo);
	}

	@Override
	protected void initAssertionContents(FormContainer group) {
		_changedField = FormFactory.newSelectField(FIELD_CHANGED, Arrays.asList(ChangeInfo.values()));
		_changedField.setMandatory(true);
		_changedField.setAsSingleSelection(_compareInfo.getChangeInfo());

		String fieldCompareName = FIELD_COMPARE_VALUE;
		_compareField = createCompareValueField(fieldCompareName);

		ValueListener adaptListener = new ValueListener() {

			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				ChangeInfo changeInfo = (ChangeInfo) ((List<?>) newValue).get(0);
				switch (changeInfo) {
					case CHANGED:
					case DEEP_CHANGED:
						_compareField.setDisabled(false);
						break;
					case NO_CHANGE:
					case CREATED:
					case REMOVED:
						_compareField.setDisabled(true);
						break;
					default:
						throw ChangeInfo.noSuchChangeInfo(changeInfo);
				}
			}
		};
		_changedField.addValueListener(adaptListener);
		adaptListener.valueChanged(_changedField, Collections.emptyList(), _changedField.getValue());
		group.addMember(_changedField);
		group.addMember(_compareField);
	}

	/**
	 * Returns the expected {@link ChangeInfo}, selected by the user.
	 */
	protected ChangeInfo expectedChangeInfo() {
		return (ChangeInfo) _changedField.getSingleSelection();
	}

	/**
	 * Creates the field to let the user choose the comparison value.
	 * 
	 * @param fieldCompareName
	 *        The name of the created field.
	 */
	protected abstract FormField createCompareValueField(String fieldCompareName);

	@Override
	protected Document getFormTemplateDocument() {
		return TEMPLATE;
	}

	@Override
	protected ResPrefix getI18nPrefix() {
		return I18NConstants.COMPARE_ASSERTION;
	}

	@Override
	protected List<? extends GuiAssertion> buildAssertions() {
		ArrayList<GuiAssertion> assertions = new ArrayList<>();
		assertions.add(assertSameChangeInfo());
		if (_compareField.isActive()) {
			assertions.add(assertSameCompareObject());
		}
		return assertions;
	}

	/**
	 * Creates a assertion about the {@link CompareInfo#getChangeInfo() change type}.
	 */
	protected abstract GuiAssertion assertSameChangeInfo();

	/**
	 * Creates a assertion about the comparison object.
	 */
	protected abstract GuiAssertion assertSameCompareObject();

}

