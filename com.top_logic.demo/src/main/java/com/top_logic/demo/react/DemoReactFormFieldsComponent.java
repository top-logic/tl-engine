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
import com.top_logic.layout.react.ReactControl;
import com.top_logic.layout.react.control.layout.ReactFormFieldChromeControl;
import com.top_logic.layout.react.control.layout.ReactFormGroupControl;
import com.top_logic.layout.react.control.layout.ReactFormLayoutControl;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Demo {@link LayoutComponent} that showcases the React form layout system.
 *
 * <p>
 * Demonstrates {@link ReactFormLayoutControl} with responsive columns,
 * {@link ReactFormGroupControl} with collapsible sections, and
 * {@link ReactFormFieldChromeControl} with label, error, help text, required indicator, and dirty
 * state around React field controls.
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
		// -- Personal Information group (collapsible, subtle border, full line) --

		ReactControl nameInput = createField("TLTextInput", "value", "John Doe");
		ReactFormFieldChromeControl nameField = new ReactFormFieldChromeControl(
			"Full Name", true, false, null, "Enter your full legal name", null, false, true, nameInput);

		ReactControl emailInput = createField("TLTextInput", "value", "john@example.com");
		ReactFormFieldChromeControl emailField = new ReactFormFieldChromeControl(
			"Email", true, true, null, null, null, false, true, emailInput);

		ReactControl phoneInput = createField("TLTextInput", "value", "");
		ReactFormFieldChromeControl phoneField = new ReactFormFieldChromeControl(
			"Phone", false, false, "Please enter a valid phone number", null, null, false, true, phoneInput);

		ReactControl dobInput = createField("TLDatePicker", "value", "1990-06-15");
		ReactFormFieldChromeControl dobField = new ReactFormFieldChromeControl(
			"Date of Birth", false, false, null, null, null, false, true, dobInput);

		ReactControl bioInput = createField("TLTextInput", "value", "Software developer with 10 years experience...");
		ReactFormFieldChromeControl bioField = new ReactFormFieldChromeControl(
			"Biography", false, false, null, "A short description of yourself",
			"top", true, true, bioInput);

		ReactControl activeInput = createField("TLCheckbox", "value", Boolean.TRUE);
		ReactFormFieldChromeControl activeField = new ReactFormFieldChromeControl(
			"Active", false, false, null, null, null, false, true, activeInput);

		ReactFormGroupControl personalGroup = new ReactFormGroupControl(
			"Personal Information", true, false, "subtle", true, List.of(),
			List.of(nameField, emailField, phoneField, dobField, bioField, activeField));

		// -- Preferences group (outlined border, not collapsible) --

		ReactControl langInput = createSelectField(
			createOption("en", "English"),
			createOption("de", "Deutsch"),
			createOption("fr", "Fran\u00e7ais"),
			createOption("es", "Espa\u00f1ol"));
		ReactFormFieldChromeControl langField = new ReactFormFieldChromeControl(
			"Language", true, false, null, "Select your preferred language", null, false, true, langInput);

		ReactControl notifInput = createField("TLNumberInput", "value", Integer.valueOf(5));
		ReactFormFieldChromeControl notifField = new ReactFormFieldChromeControl(
			"Notification Limit", false, false, null, "Maximum notifications per day", null, false, true, notifInput);

		ReactControl themeInput = createSelectField(
			createOption("light", "Light"),
			createOption("dark", "Dark"),
			createOption("system", "System Default"));
		ReactFormFieldChromeControl themeField = new ReactFormFieldChromeControl(
			"Theme", false, false, null, null, null, false, true, themeInput);

		ReactFormGroupControl prefsGroup = new ReactFormGroupControl(
			"Preferences", true, false, "outlined", true, List.of(),
			List.of(langField, notifField, themeField));

		// -- Top-level form layout: 3 columns, auto label position --

		return new ReactFormLayoutControl(3, "auto", false,
			List.of(personalGroup, prefsGroup));
	}

	private static ReactControl createField(String module, String stateKey, Object value) {
		ReactControl control = new ReactControl(null, module);
		control.getReactState().put(stateKey, value);
		control.getReactState().put("editable", Boolean.TRUE);
		return control;
	}

	private static ReactControl createSelectField(Map<String, Object>... options) {
		ReactControl control = new ReactControl(null, "TLSelect");
		List<Map<String, Object>> optionList = new ArrayList<>();
		for (Map<String, Object> option : options) {
			optionList.add(option);
		}
		control.getReactState().put("options", optionList);
		control.getReactState().put("editable", Boolean.TRUE);
		return control;
	}

	private static Map<String, Object> createOption(String value, String label) {
		Map<String, Object> option = new LinkedHashMap<>();
		option.put("value", value);
		option.put("label", label);
		return option;
	}

}
