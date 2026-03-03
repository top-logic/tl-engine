/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.react;

import java.io.IOException;
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
import com.top_logic.layout.react.control.ReactButtonControl;
import com.top_logic.layout.react.control.form.ReactCheckboxControl;
import com.top_logic.layout.react.control.form.ReactDatePickerControl;
import com.top_logic.layout.react.control.form.ReactNumberInputControl;
import com.top_logic.layout.react.control.form.ReactSelectFormFieldControl;
import com.top_logic.layout.react.control.form.ReactTextInputControl;
import com.top_logic.layout.react.control.layout.ReactFormFieldChromeControl;
import com.top_logic.layout.react.control.layout.ReactFormGroupControl;
import com.top_logic.layout.react.control.layout.ReactFormLayoutControl;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Demo {@link LayoutComponent} that showcases the React form layout system.
 *
 * <p>
 * Demonstrates {@link ReactFormLayoutControl} with responsive columns,
 * {@link ReactFormGroupControl} with collapsible sections, and
 * {@link ReactFormFieldChromeControl} with label, error, help text, required indicator, and dirty
 * state around React field controls backed by proper {@link FormField} instances.
 * </p>
 */
public class DemoReactFormFieldsComponent extends LayoutComponent {

	/**
	 * Configuration for {@link DemoReactFormFieldsComponent}.
	 */
	public interface Config extends LayoutComponent.Config {
		// No additional configuration needed.
	}

	private ReactFormLayoutControl _formLayout;

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

		if (_formLayout == null) {
			_formLayout = createFormLayout();
		}

		_formLayout.write(displayContext, out);
	}

	private ReactFormLayoutControl createFormLayout() {
		FormContext formContext = new FormContext(this);

		// -- Personal Information group (collapsible, subtle border, full line) --

		FormField nameField = FormFactory.newStringField("name", "John Doe", false);
		formContext.addMember(nameField);
		ReactTextInputControl nameInput = new ReactTextInputControl(nameField);
		ReactFormFieldChromeControl nameChrome = new ReactFormFieldChromeControl(
			"Full Name", true, false, null, "Enter your full legal name", null, false, true, nameInput);

		FormField emailField = FormFactory.newStringField("email", "john@example.com", false);
		formContext.addMember(emailField);
		ReactTextInputControl emailInput = new ReactTextInputControl(emailField);
		ReactFormFieldChromeControl emailChrome = new ReactFormFieldChromeControl(
			"Email", true, true, null, null, null, false, true, emailInput);

		FormField phoneField = FormFactory.newStringField("phone", "", false);
		formContext.addMember(phoneField);
		ReactTextInputControl phoneInput = new ReactTextInputControl(phoneField);
		ReactFormFieldChromeControl phoneChrome = new ReactFormFieldChromeControl(
			"Phone", false, false, "Please enter a valid phone number", null, null, false, true, phoneInput);

		FormField dobField = FormFactory.newStringField("dob", "1990-06-15", false);
		formContext.addMember(dobField);
		ReactDatePickerControl dobInput = new ReactDatePickerControl(dobField);
		ReactFormFieldChromeControl dobChrome = new ReactFormFieldChromeControl(
			"Date of Birth", false, false, null, null, null, false, true, dobInput);

		FormField bioField = FormFactory.newStringField("bio", "Software developer with 10 years experience...", false);
		formContext.addMember(bioField);
		ReactTextInputControl bioInput = new ReactTextInputControl(bioField);
		ReactFormFieldChromeControl bioChrome = new ReactFormFieldChromeControl(
			"Biography", false, false, null, "A short description of yourself",
			"top", true, true, bioInput);

		FormField activeField = FormFactory.newBooleanField("active", Boolean.TRUE, false);
		formContext.addMember(activeField);
		ReactCheckboxControl activeInput = new ReactCheckboxControl(activeField);
		ReactFormFieldChromeControl activeChrome = new ReactFormFieldChromeControl(
			"Active", false, false, null, null, null, false, true, activeInput);

		ReactButtonControl editButton = new ReactButtonControl("Edit", c -> HandlerResult.DEFAULT_RESULT);
		ReactButtonControl resetButton = new ReactButtonControl("Reset", c -> HandlerResult.DEFAULT_RESULT);

		ReactFormGroupControl personalGroup = new ReactFormGroupControl(
			"Personal Information", true, false, "subtle", true, List.of(editButton, resetButton),
			List.of(nameChrome, emailChrome, phoneChrome, dobChrome, bioChrome, activeChrome));

		// -- Preferences group (outlined border, not collapsible) --

		FormField langField = FormFactory.newStringField("language", "en", false);
		formContext.addMember(langField);
		ReactSelectFormFieldControl langInput = new ReactSelectFormFieldControl(langField,
			createOptionList(
				createOption("en", "English"),
				createOption("de", "Deutsch"),
				createOption("fr", "Fran\u00e7ais"),
				createOption("es", "Espa\u00f1ol")));
		ReactFormFieldChromeControl langChrome = new ReactFormFieldChromeControl(
			"Language", true, false, null, "Select your preferred language", null, false, true, langInput);

		FormField notifField = FormFactory.newIntField("notificationLimit", Integer.valueOf(5), false);
		formContext.addMember(notifField);
		ReactNumberInputControl notifInput = new ReactNumberInputControl(notifField, 0);
		ReactFormFieldChromeControl notifChrome = new ReactFormFieldChromeControl(
			"Notification Limit", false, false, null, "Maximum notifications per day", null, false, true, notifInput);

		FormField themeField = FormFactory.newStringField("theme", "light", false);
		formContext.addMember(themeField);
		ReactSelectFormFieldControl themeInput = new ReactSelectFormFieldControl(themeField,
			createOptionList(
				createOption("light", "Light"),
				createOption("dark", "Dark"),
				createOption("system", "System Default")));
		ReactFormFieldChromeControl themeChrome = new ReactFormFieldChromeControl(
			"Theme", false, false, null, null, null, false, true, themeInput);

		ReactButtonControl addPrefButton = new ReactButtonControl("Add", c -> HandlerResult.DEFAULT_RESULT);

		ReactFormGroupControl prefsGroup = new ReactFormGroupControl(
			"Preferences", true, false, "outlined", true, List.of(addPrefButton),
			List.of(langChrome, notifChrome, themeChrome));

		// -- Top-level form layout: 3 columns, auto label position --

		return new ReactFormLayoutControl(3, "auto", false,
			List.of(personalGroup, prefsGroup));
	}

	@SafeVarargs
	private static List<Map<String, Object>> createOptionList(Map<String, Object>... options) {
		List<Map<String, Object>> list = new ArrayList<>();
		for (Map<String, Object> option : options) {
			list.add(option);
		}
		return list;
	}

	private static Map<String, Object> createOption(String value, String label) {
		Map<String, Object> option = new LinkedHashMap<>();
		option.put("value", value);
		option.put("label", label);
		return option;
	}

}
