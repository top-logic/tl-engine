/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.react;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.control.form.ReactSelectFormFieldControl;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Demo {@link LayoutComponent} that showcases all React form field types.
 *
 * <p>
 * Demonstrates {@code TLTextInput}, {@code TLCheckbox}, {@code TLNumberInput}, {@code TLDatePicker}
 * and {@code TLSelect} rendered via {@link ReactFormFieldControl}, with a
 * {@link DemoFieldTogglesControl} composite React control to exercise the SSE patch handlers for
 * disabled, immutable, and mandatory state changes.
 * </p>
 */
public class DemoReactFormFieldsComponent extends LayoutComponent {

	/**
	 * Configuration for {@link DemoReactFormFieldsComponent}.
	 */
	public interface Config extends LayoutComponent.Config {
		// No additional configuration needed.
	}

	private List<FieldDemo> _demos;

	/**
	 * Creates a new {@link DemoReactFormFieldsComponent}.
	 */
	public DemoReactFormFieldsComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	public void writeBody(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response,
			TagWriter out) throws IOException, ServletException {
		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext(request);

		if (_demos == null) {
			_demos = createDemos();
		}

		out.beginTag(HTMLConstants.H2);
		out.writeText("React Form Field Demos");
		out.endTag(HTMLConstants.H2);

		out.beginTag(HTMLConstants.PARAGRAPH);
		out.writeText("Each field below is rendered by a React component via ReactFormFieldControl. "
			+ "Use the toggle buttons to change field properties; changes are pushed as SSE patches.");
		out.endTag(HTMLConstants.PARAGRAPH);

		for (FieldDemo demo : _demos) {
			out.beginBeginTag(HTMLConstants.DIV);
			out.writeAttribute(HTMLConstants.CLASS_ATTR, "demoFieldGroup");
			out.writeAttribute(HTMLConstants.STYLE_ATTR, "margin-bottom: 1.5em;");
			out.endBeginTag();

			out.beginTag(HTMLConstants.H3);
			out.writeText(demo._label);
			out.endTag(HTMLConstants.H3);

			demo._fieldControl.write(displayContext, out);
			demo._togglesControl.write(displayContext, out);

			out.endTag(HTMLConstants.DIV);
		}
	}

	private List<FieldDemo> createDemos() {
		FormContext formContext = new FormContext(this);

		List<FieldDemo> demos = new ArrayList<>();

		// Text input
		FormField textField = FormFactory.newStringField("text", "Hello World", false);
		formContext.addMember(textField);
		demos.add(new FieldDemo("Text Input (TLTextInput)", textField,
			new ReactFormFieldControl(textField, "TLTextInput")));

		// Checkbox
		FormField checkboxField = FormFactory.newBooleanField("checkbox", Boolean.FALSE, false);
		formContext.addMember(checkboxField);
		demos.add(new FieldDemo("Checkbox (TLCheckbox)", checkboxField,
			new ReactFormFieldControl(checkboxField, "TLCheckbox")));

		// Number input (ComplexField with integer NumberFormat)
		NumberFormat intFormat = NumberFormat.getIntegerInstance();
		intFormat.setGroupingUsed(false);
		FormField numberField = FormFactory.newComplexField("number", intFormat, Integer.valueOf(42), false);
		formContext.addMember(numberField);
		demos.add(new FieldDemo("Number Input (TLNumberInput)", numberField,
			new ReactFormFieldControl(numberField, "TLNumberInput")));

		// Date picker (string field with ISO date format for HTML date input)
		FormField dateField = FormFactory.newStringField("date");
		formContext.addMember(dateField);
		demos.add(new FieldDemo("Date Picker (TLDatePicker)", dateField,
			new ReactFormFieldControl(dateField, "TLDatePicker")));

		// Select (with options passed via state)
		FormField selectField = FormFactory.newStringField("select");
		formContext.addMember(selectField);
		List<Map<String, Object>> selectOptions = createSelectOptions();
		demos.add(new FieldDemo("Select (TLSelect)", selectField,
			new ReactSelectFormFieldControl(selectField, selectOptions)));

		return demos;
	}

	private static List<Map<String, Object>> createSelectOptions() {
		List<Map<String, Object>> options = new ArrayList<>();
		options.add(createOption("apple", "Apple"));
		options.add(createOption("banana", "Banana"));
		options.add(createOption("cherry", "Cherry"));
		options.add(createOption("date", "Date"));
		return options;
	}

	private static Map<String, Object> createOption(String value, String label) {
		Map<String, Object> option = new LinkedHashMap<>();
		option.put("value", value);
		option.put("label", label);
		return option;
	}

	/**
	 * Groups a form field with its React field control and toggle control.
	 */
	private static class FieldDemo {

		final String _label;

		final ReactFormFieldControl _fieldControl;

		final DemoFieldTogglesControl _togglesControl;

		FieldDemo(String label, FormField field, ReactFormFieldControl fieldControl) {
			_label = label;
			_fieldControl = fieldControl;
			_togglesControl = new DemoFieldTogglesControl(field);
		}
	}

}
