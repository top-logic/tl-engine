/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.form.implementation.additional;


import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.io.Content;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.util.ResKey;
import com.top_logic.gui.MultiThemeFactory;
import com.top_logic.gui.Theme;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.gui.ThemeUtil;
import com.top_logic.gui.config.ThemeSetting;
import com.top_logic.gui.config.ThemeSettings.Config;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.model.form.implementation.FormEditorContext;
import com.top_logic.model.form.implementation.FormElementTemplateProvider;

/**
 * {@link FormElementTemplateProvider} that allows to switch between the dark and light theme for
 * the edited {@link Person}.
 *
 * @author <a href="mailto:pja@top-logic.com">Petar Janosevic</a>
 */
@InApp
@TagName("dark-theme-toggle")
public class DarkThemeToggle extends PersonTemplateProvider {

	@Override
	protected FormField createField(FormEditorContext context, String fieldName, Person account) {
		// TO-DO Den Zustand, ob der User dark oder light theme ausgewählt hat
		// muss in der Datenbank bestimmt sein und der boolean Wert soll von dort geholt werden.
		BooleanField toggle = FormFactory.newBooleanField(fieldName, false, false);
		ThemeFactory themeFactory = ThemeFactory.getInstance();
		String currentThemeId = fetchCurrentThemeID(account);
		Theme currentTheme = resolveTheme(themeFactory, currentThemeId);

		FileManager fm = FileManager.getInstance();
		String themePrefix = ThemeUtil.getThemePath(currentThemeId);

//		Gibt nur die Theme Variablen zurück, die in den Icons.java Klassen erstellt wurden!
//		Map<String, ThemeVar<?>> declaredVars = themeFactory.getDeclaredVars();
//		declaredVars.forEach((key, value) -> System.err.println(key + " = " + value));


		context.getFormContext().addStoreAlgorithm(ctx -> {
			if (!toggle.isChanged()) {
				return;
			}
			try {
				List<BinaryData> settingFiles = fm.getDataOverlays(themePrefix + "theme-settings.xml");
				for (BinaryData settingFile : settingFiles) {
					toggleTheme(currentThemeId, currentTheme, settingFile);
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			} catch (ConfigurationException ex) {
				ex.printStackTrace();
			}

			// TO-DO Datenbank updaten
			boolean updateValue = toggle.getAsBoolean() == true ? true : false;
			toggle.setAsBoolean(updateValue);
		});

		return toggle;
	}

	private void toggleTheme(String themeId, Theme currentTheme, Content source) throws ConfigurationException {
		InstantiationContext context = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY;
		Config configs = (Config) new ConfigurationReader(context,
			Collections.singletonMap("settings", TypedConfiguration.getConfigurationDescriptor(Config.class)))
				.setSource(source).read();
		String sourceName = source.toString();

		for (ThemeSetting.Config<?> config : configs.getSettings().values()) {

			String expr = config.getExpr();
			if (expr == null || expr.isEmpty()) {
				continue;
			}

			if (expr.contains("light")) {
				System.err.println(config.toString()); // TO-DO DELETE

				config.setExpr(expr.replace("light", "dark"));
			} else if (expr.contains("dark")) {
				System.err.println(config.toString()); // TO-DO DELETE

				config.setExpr(expr.replace("dark", "light"));
			} else {
				continue;
			}
			System.out.println(config.toString()); // TO-DO DELETE
		}
		
		/*
		 * Die Configs sehen so aus...da müsste man "expr" austauschen und das dann in der config speichern.
		 * 
		 	<config config:interface="com.top_logic.gui.config.ThemeSetting$ColorSetting$Config"
  				xmlns:config="http://www.top-logic.com/ns/config/6.0"
  				class="com.top_logic.gui.config.ThemeSetting$ColorSetting"
  				expr="%light-text-on-color%"
  				name="text-on-color"
			/>
		 */
	}

	@Override
	protected String fieldName() {
		return "darkThemeToggle";
	}

	@Override
	protected ResKey fieldLabel() {
		return I18NConstants.LABEL_DARK_THEME_TOGGLE;
	}

	private String fetchCurrentThemeID(Person account) {
		PersonalConfiguration pc = getPersonalConfiguration(account);
		return pc == null ? null : MultiThemeFactory.getPersonalThemeId(pc);
	}

	private Theme resolveTheme(ThemeFactory themeFactory, String currentThemeId) {
		Theme currentTheme;
		if (currentThemeId == null) {
			currentTheme = themeFactory.getDefaultTheme();
		} else {
			currentTheme = themeFactory.getTheme(currentThemeId);
			if (currentTheme == null) {
				// Theme no longer available.
				currentTheme = themeFactory.getDefaultTheme();
			}
		}
		return currentTheme;
	}

}
