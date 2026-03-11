/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.react;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import com.top_logic.layout.Flavor;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.control.form.ReactCheckboxControl;
import com.top_logic.layout.react.control.form.ReactDatePickerControl;
import com.top_logic.layout.react.control.form.ReactNumberInputControl;
import com.top_logic.layout.react.control.form.ReactSelectFormFieldControl;
import com.top_logic.layout.react.control.form.ReactTextInputControl;
import com.top_logic.layout.react.control.layout.ReactFormFieldChromeControl;
import com.top_logic.layout.react.control.layout.ReactFormGroupControl;
import com.top_logic.layout.react.control.layout.ReactFormLayoutControl;
import com.top_logic.layout.react.control.select.ReactDropdownSelectControl;
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

	private ReactContext _context;

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
			_context = ReactContext.fromDisplayContext(displayContext);
			_formLayout = createFormLayout();
		}

		_formLayout.write(displayContext, out);
	}

	private ReactFormLayoutControl createFormLayout() {
		ReactContext ctx = _context;
		FormContext formContext = new FormContext(this);

		// -- Personal Information group (collapsible, subtle border, full line) --

		FormField nameField = FormFactory.newStringField("name", "John Doe", false);
		formContext.addMember(nameField);
		ReactTextInputControl nameInput = new ReactTextInputControl(ctx, nameField);
		ReactFormFieldChromeControl nameChrome = new ReactFormFieldChromeControl(ctx,
			"Full Name", true, false, null, "Enter your full legal name", null, false, true, nameInput);

		FormField emailField = FormFactory.newStringField("email", "john@example.com", false);
		formContext.addMember(emailField);
		ReactTextInputControl emailInput = new ReactTextInputControl(ctx, emailField);
		ReactFormFieldChromeControl emailChrome = new ReactFormFieldChromeControl(ctx,
			"Email", true, true, null, null, null, false, true, emailInput);

		FormField phoneField = FormFactory.newStringField("phone", "", false);
		formContext.addMember(phoneField);
		ReactTextInputControl phoneInput = new ReactTextInputControl(ctx, phoneField);
		ReactFormFieldChromeControl phoneChrome = new ReactFormFieldChromeControl(ctx,
			"Phone", false, false, "Please enter a valid phone number", null, null, false, true, phoneInput);

		FormField dobField = FormFactory.newStringField("dob", "1990-06-15", false);
		formContext.addMember(dobField);
		ReactDatePickerControl dobInput = new ReactDatePickerControl(ctx, dobField);
		ReactFormFieldChromeControl dobChrome = new ReactFormFieldChromeControl(ctx,
			"Date of Birth", false, false, null, null, null, false, true, dobInput);

		FormField bioField = FormFactory.newStringField("bio", "Software developer with 10 years experience...", false);
		formContext.addMember(bioField);
		ReactTextInputControl bioInput = new ReactTextInputControl(ctx, bioField);
		ReactFormFieldChromeControl bioChrome = new ReactFormFieldChromeControl(ctx,
			"Biography", false, false, null, "A short description of yourself",
			"top", true, true, bioInput);

		FormField activeField = FormFactory.newBooleanField("active", Boolean.TRUE, false);
		formContext.addMember(activeField);
		ReactCheckboxControl activeInput = new ReactCheckboxControl(ctx, activeField);
		ReactFormFieldChromeControl activeChrome = new ReactFormFieldChromeControl(ctx,
			"Active", false, false, null, null, null, false, true, activeInput);

		ReactButtonControl editButton = new ReactButtonControl(ctx, "Edit", c -> HandlerResult.DEFAULT_RESULT);
		ReactButtonControl resetButton = new ReactButtonControl(ctx, "Reset", c -> HandlerResult.DEFAULT_RESULT);

		ReactFormGroupControl personalGroup = new ReactFormGroupControl(ctx,
			"Personal Information", true, false, "subtle", true, List.of(editButton, resetButton),
			List.of(nameChrome, emailChrome, phoneChrome, dobChrome, bioChrome, activeChrome));

		// -- Preferences group (outlined border, not collapsible) --

		FormField langField = FormFactory.newStringField("language", "en", false);
		formContext.addMember(langField);
		ReactSelectFormFieldControl langInput = new ReactSelectFormFieldControl(ctx, langField,
			createOptionList(
				createOption("en", "English"),
				createOption("de", "Deutsch"),
				createOption("fr", "Fran\u00e7ais"),
				createOption("es", "Espa\u00f1ol")));
		ReactFormFieldChromeControl langChrome = new ReactFormFieldChromeControl(ctx,
			"Language", true, false, null, "Select your preferred language", null, false, true, langInput);

		FormField notifField = FormFactory.newIntField("notificationLimit", Integer.valueOf(5), false);
		formContext.addMember(notifField);
		ReactNumberInputControl notifInput = new ReactNumberInputControl(ctx, notifField, 0);
		ReactFormFieldChromeControl notifChrome = new ReactFormFieldChromeControl(ctx,
			"Notification Limit", false, false, null, "Maximum notifications per day", null, false, true, notifInput);

		FormField themeField = FormFactory.newStringField("theme", "light", false);
		formContext.addMember(themeField);
		ReactSelectFormFieldControl themeInput = new ReactSelectFormFieldControl(ctx, themeField,
			createOptionList(
				createOption("light", "Light"),
				createOption("dark", "Dark"),
				createOption("system", "System Default")));
		ReactFormFieldChromeControl themeChrome = new ReactFormFieldChromeControl(ctx,
			"Theme", false, false, null, null, null, false, true, themeInput);

		ReactButtonControl addPrefButton = new ReactButtonControl(ctx, "Add", c -> HandlerResult.DEFAULT_RESULT);

		ReactFormGroupControl prefsGroup = new ReactFormGroupControl(ctx,
			"Preferences", true, false, "outlined", true, List.of(addPrefButton),
			List.of(langChrome, notifChrome, themeChrome));

		// -- Dropdown Select group (demonstrates the new TLDropdownSelect control) --

		List<String> countryOptions = Arrays.asList(
			"Germany", "France", "Italy", "Spain", "United Kingdom",
			"Netherlands", "Belgium", "Austria", "Switzerland", "Sweden",
			"Norway", "Denmark", "Finland", "Poland", "Czech Republic");

		ResourceProvider countryResourceProvider = new DemoCountryResourceProvider();

		SelectField countrySingle = FormFactory.newSelectField("country", countryOptions, false, false);
		countrySingle.setOptionLabelProvider(countryResourceProvider);
		formContext.addMember(countrySingle);
		ReactDropdownSelectControl countrySingleInput = new ReactDropdownSelectControl(ctx, countrySingle);
		ReactFormFieldChromeControl countrySingleChrome = new ReactFormFieldChromeControl(ctx,
			"Country (single)", false, false, null, "Pick one country", null, false, true, countrySingleInput);

		SelectField countryMulti = FormFactory.newSelectField("countries", countryOptions, true, false);
		countryMulti.setOptionLabelProvider(countryResourceProvider);
		countryMulti.setAsSelection(Arrays.asList("Germany", "France"));
		formContext.addMember(countryMulti);
		ReactDropdownSelectControl countryMultiInput = new ReactDropdownSelectControl(ctx, countryMulti);
		ReactFormFieldChromeControl countryMultiChrome = new ReactFormFieldChromeControl(ctx,
			"Countries (multi)", false, false, null, "Pick multiple countries", null, false, true, countryMultiInput);

		SelectField countryMandatory = FormFactory.newSelectField("mandatoryCountry", countryOptions, false, true, false, null);
		countryMandatory.setOptionLabelProvider(countryResourceProvider);
		countryMandatory.setAsSelection(Collections.singletonList("Italy"));
		formContext.addMember(countryMandatory);
		ReactDropdownSelectControl countryMandatoryInput = new ReactDropdownSelectControl(ctx, countryMandatory);
		ReactFormFieldChromeControl countryMandatoryChrome = new ReactFormFieldChromeControl(ctx,
			"Country (mandatory)", true, false, null, "Required \u2013 clear button is hidden", null, false, true, countryMandatoryInput);

		SelectField countryDisabled = FormFactory.newSelectField("disabledCountry", countryOptions, false, false);
		countryDisabled.setOptionLabelProvider(countryResourceProvider);
		countryDisabled.setAsSelection(Collections.singletonList("Spain"));
		countryDisabled.setDisabled(true);
		formContext.addMember(countryDisabled);
		ReactDropdownSelectControl countryDisabledInput = new ReactDropdownSelectControl(ctx, countryDisabled);
		ReactFormFieldChromeControl countryDisabledChrome = new ReactFormFieldChromeControl(ctx,
			"Country (disabled)", false, false, null, null, null, false, true, countryDisabledInput);

		SelectField countryImmutable = FormFactory.newSelectField("immutableCountry", countryOptions, false, true);
		countryImmutable.setOptionLabelProvider(countryResourceProvider);
		countryImmutable.setAsSelection(Collections.singletonList("Switzerland"));
		formContext.addMember(countryImmutable);
		ReactDropdownSelectControl countryImmutableInput = new ReactDropdownSelectControl(ctx, countryImmutable);
		ReactFormFieldChromeControl countryImmutableChrome = new ReactFormFieldChromeControl(ctx,
			"Country (read-only)", false, false, null, null, null, false, true, countryImmutableInput);

		SelectField countryCustomOrder = FormFactory.newSelectField("customOrderCountries", countryOptions, true, false);
		countryCustomOrder.setOptionLabelProvider(countryResourceProvider);
		countryCustomOrder.setCustomOrder(true);
		countryCustomOrder.setAsSelection(Arrays.asList("Germany", "France", "Italy"));
		formContext.addMember(countryCustomOrder);
		ReactDropdownSelectControl countryCustomOrderInput = new ReactDropdownSelectControl(ctx, countryCustomOrder);
		ReactFormFieldChromeControl countryCustomOrderChrome = new ReactFormFieldChromeControl(ctx,
			"Countries (custom order)", false, false, null, "Drag chips to reorder", null, false, true, countryCustomOrderInput);

		ReactFormGroupControl dropdownGroup = new ReactFormGroupControl(ctx,
			"Dropdown Select", true, false, "outlined", true, List.of(),
			List.of(countrySingleChrome, countryMultiChrome, countryMandatoryChrome,
				countryDisabledChrome, countryImmutableChrome, countryCustomOrderChrome));

		// -- Top-level form layout: 3 columns, auto label position --

		return new ReactFormLayoutControl(ctx, 3, "auto", false,
			List.of(personalGroup, prefsGroup, dropdownGroup));
	}

	/**
	 * A simple {@link ResourceProvider} for demo country options that provides CSS icon class names
	 * based on Bootstrap Icons.
	 */
	static class DemoCountryResourceProvider implements ResourceProvider {

		@Override
		public String getLabel(Object object) {
			return (String) object;
		}

		@Override
		public ThemeImage getImage(Object object, Flavor flavor) {
			return ThemeImage.cssIcon("bi bi-geo-alt-fill");
		}

		@Override
		public String getType(Object object) {
			return "Country";
		}

		@Override
		public String getTooltip(Object object) {
			return null;
		}

		@Override
		public String getLink(DisplayContext context, Object object) {
			return null;
		}

		@Override
		public String getCssClass(Object object) {
			return null;
		}
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
