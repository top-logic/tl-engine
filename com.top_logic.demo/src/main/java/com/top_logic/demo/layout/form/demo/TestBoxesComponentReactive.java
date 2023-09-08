/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.demo;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.PlainKeyResources;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.form.tag.TextInputTag;

/**
 * Test reactive behavior of box layout with input fields.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class TestBoxesComponentReactive extends FormComponent {

	/**
	 * Default group.
	 */
	public static final String GROUP1_GROUP = "group1";

	/**
	 * Inner group of a default group.
	 */
	public static final String GROUP2_GROUP = "group2";

	/**
	 * Group with 1 column inside.
	 */
	public static final String GROUP3_GROUP = "group3";

	/**
	 * Inner group of an 1-column group.
	 */
	public static final String GROUP4_GROUP = "group4";

	/**
	 * Group with 2 columns inside.
	 */
	public static final String GROUP5_GROUP = "group5";

	/**
	 * Inner group of a 2-column group.
	 */
	public static final String GROUP6_GROUP = "group6";

	/**
	 * Group with 3 columns inside.
	 */
	public static final String GROUP7_GROUP = "group7";

	/**
	 * Inner group of a 3-column group.
	 */
	public static final String GROUP8_GROUP = "group8";

	/**
	 * Group with 4 columns inside.
	 */
	public static final String GROUP9_GROUP = "group9";

	/**
	 * Inner group of a 4-column group.
	 */
	public static final String GROUP10_GROUP = "group10";

	/**
	 * Group with 5 columns inside.
	 */
	public static final String GROUP11_GROUP = "group11";

	/**
	 * Inner group of a 5-column group.
	 */
	public static final String GROUP12_GROUP = "group12";

	/**
	 * Group inside of a columns layout.
	 */
	public static final String GROUP13_GROUP = "group13";

	/**
	 * Inner group of a group in a columns layout.
	 */
	public static final String GROUP14_GROUP = "group14";

	/**
	 * Group with wholeLine-attribute inside of a columns layout.
	 */
	public static final String GROUP15_GROUP = "group15";

	/**
	 * Inner group of a group with wholeLine-attribute in a columns layout.
	 */
	public static final String GROUP16_GROUP = "group16";

	/**
	 * Group with wholeLine-attribute and 3 columns inside of a columns layout.
	 */
	public static final String GROUP17_GROUP = "group17";

	/**
	 * Inner group of a group with wholeLine-attribute and 3 columns in a columns layout.
	 */
	public static final String GROUP18_GROUP = "group18";

	/**
	 * A {@link StringField}.
	 */
	public static final String STRING_FIELD1 = "string1";

	/**
	 * A {@link StringField}.
	 */
	public static final String STRING_FIELD2 = "string2";

	/**
	 * A {@link StringField}.
	 */
	public static final String STRING_FIELD3 = "string3";

	/**
	 * A {@link StringField}.
	 */
	public static final String STRING_FIELD4 = "string4";

	/**
	 * A {@link StringField}.
	 */
	public static final String STRING_FIELD5 = "string5";

	/**
	 * A {@link StringField}.
	 */
	public static final String STRING_FIELD6 = "string6";

	/**
	 * A {@link StringField} with a multiple columns.
	 */
	public static final String TEXT_FIELD1 = "text1";

	/**
	 * A {@link StringField} with a multiple columns.
	 */
	public static final String TEXT_FIELD2 = "text2";

	/**
	 * {@link FormComponent} configuration to display all occurrences even without a model.
	 *
	 * @author <a href=mailto:iwi@top-logic.com>Isabell Wittich</a>
	 */
	public interface Config extends FormComponent.Config {
		@Override
		@Name(DISPLAY_WITHOUT_MODEL)
		@BooleanDefault(true)
		boolean getDisplayWithoutModel();
	}

	/**
	 * Creates a new {@link TestBoxesComponentReactive}.
	 * 
	 * @param context
	 *        The {@link InstantiationContext}.
	 * @param config
	 *        The configuration for this {@link FormComponent}.
	 */
	public TestBoxesComponentReactive(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	protected boolean supportsInternalModel(Object anObject) {
		return anObject == null;
	}

	@Override
	public FormContext createFormContext() {
		FormContext fc = new FormContext(this);
		fc.setResources(PlainKeyResources.INSTANCE);

		fill(fc, true);

		final FormGroup group1 = add(fc, new FormGroup(GROUP1_GROUP, PlainKeyResources.INSTANCE));
		final FormGroup group2 = add(group1, new FormGroup(GROUP2_GROUP, PlainKeyResources.INSTANCE));

		final FormGroup group3 = add(fc, new FormGroup(GROUP3_GROUP, PlainKeyResources.INSTANCE));
		final FormGroup group4 = add(group3, new FormGroup(GROUP4_GROUP, PlainKeyResources.INSTANCE));

		final FormGroup group5 = add(fc, new FormGroup(GROUP5_GROUP, PlainKeyResources.INSTANCE));
		final FormGroup group6 = add(group5, new FormGroup(GROUP6_GROUP, PlainKeyResources.INSTANCE));

		final FormGroup group7 = add(fc, new FormGroup(GROUP7_GROUP, PlainKeyResources.INSTANCE));
		final FormGroup group8 = add(group7, new FormGroup(GROUP8_GROUP, PlainKeyResources.INSTANCE));

		final FormGroup group9 = add(fc, new FormGroup(GROUP9_GROUP, PlainKeyResources.INSTANCE));
		final FormGroup group10 = add(group9, new FormGroup(GROUP10_GROUP, PlainKeyResources.INSTANCE));

		final FormGroup group11 = add(fc, new FormGroup(GROUP11_GROUP, PlainKeyResources.INSTANCE));
		final FormGroup group12 = add(group11, new FormGroup(GROUP12_GROUP, PlainKeyResources.INSTANCE));

		final FormGroup group13 = add(fc, new FormGroup(GROUP13_GROUP, PlainKeyResources.INSTANCE));
		final FormGroup group14 = add(group13, new FormGroup(GROUP14_GROUP, PlainKeyResources.INSTANCE));

		final FormGroup group15 = add(fc, new FormGroup(GROUP15_GROUP, PlainKeyResources.INSTANCE));
		final FormGroup group16 = add(group15, new FormGroup(GROUP16_GROUP, PlainKeyResources.INSTANCE));

		final FormGroup group17 = add(fc, new FormGroup(GROUP17_GROUP, PlainKeyResources.INSTANCE));
		final FormGroup group18 = add(group17, new FormGroup(GROUP18_GROUP, PlainKeyResources.INSTANCE));

		fill(group1, true);
		fill(group2, false);
		fill(group3, true);
		fill(group4, false);
		fill(group5, true);
		fill(group6, false);
		fill(group7, true);
		fill(group8, false);
		fill(group9, true);
		fill(group10, false);
		fill(group11, true);
		fill(group12, false);
		fill(group13, true);
		fill(group14, false);
		fill(group15, true);
		fill(group16, false);
		fill(group17, true);
		fill(group18, false);

		return fc;
	}

	private static FormContainer fill(FormContainer context, boolean many) {
		List<FormMember> stringFields = new ArrayList<>();
		stringFields.add(FormFactory.newStringField(STRING_FIELD1));
		stringFields.add(FormFactory.newStringField(STRING_FIELD2));
		stringFields.add(FormFactory.newStringField(STRING_FIELD3));

		if (many) {
			stringFields.add(FormFactory.newStringField(STRING_FIELD4));
			stringFields.add(FormFactory.newStringField(STRING_FIELD5));
			stringFields.add(FormFactory.newStringField(STRING_FIELD6));

			TextInputTag multiLine = new TextInputTag();
			multiLine.setMultiLine(true);
			multiLine.setColumns(30);
			multiLine.setRows(6);

			StringField text = FormFactory.newStringField(TEXT_FIELD1);
			text.setControlProvider(multiLine);
			stringFields.add(text);

			StringField text2 = FormFactory.newStringField(TEXT_FIELD2);
			text2.setControlProvider(multiLine);
			stringFields.add(text2);
		}

		for (FormMember field : stringFields) {
			add(context, field);
		}

		return context;
	}

	private static <F extends FormMember> F add(FormContainer fc, F field) {
		fc.addMember(field);
		return field;
	}

}
