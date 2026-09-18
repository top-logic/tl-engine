/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.gui.DesignTokenKind;
import com.top_logic.gui.DesignTokenService;
import com.top_logic.gui.Theme;
import com.top_logic.gui.ThemeDesignTokens;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.gui.config.ThemeSetting;
import com.top_logic.gui.config.ThemeSetting.ColorSetting;
import com.top_logic.gui.config.ThemeSetting.DimSetting;
import com.top_logic.gui.config.ThemeSettings;
import com.top_logic.layout.basic.ThemeImage;

/**
 * Test for {@link ThemeDesignTokens}.
 */
@SuppressWarnings("javadoc")
public class TestThemeDesignTokens extends BasicTestCase {

	public void testColorSettingsAreColorTokens() {
		Collection<String> colorTokens = DesignTokenService.tokenNames(DesignTokenKind.COLOR);
		assertFalse("The themes emit color tokens.", colorTokens.isEmpty());

		for (ThemeSetting setting : settings()) {
			if (setting instanceof ColorSetting && ThemeDesignTokens.isEmitted(setting)) {
				assertTrue("Emitted color setting is a color token: " + setting.getName(),
					colorTokens.contains(setting.getLocalName()));
			}
		}
	}

	public void testLengthSettingsAreLengthTokens() {
		Collection<String> lengthTokens = DesignTokenService.tokenNames(DesignTokenKind.LENGTH);
		Collection<String> colorTokens = DesignTokenService.tokenNames(DesignTokenKind.COLOR);

		for (ThemeSetting setting : settings()) {
			if (setting instanceof DimSetting && ThemeDesignTokens.isEmitted(setting)) {
				String name = setting.getLocalName();
				assertTrue("Emitted dimension setting is a length token: " + setting.getName(),
					lengthTokens.contains(name));
				assertFalse("A length token is no color token: " + setting.getName(),
					colorTokens.contains(name));
			}
		}
	}

	public void testTokensAreEmittedSettings() {
		Set<String> emitted = emittedLocalNames();

		for (DesignTokenKind kind : DesignTokenKind.values()) {
			for (String name : DesignTokenService.tokenNames(kind)) {
				assertTrue("Token is emitted as a CSS custom property: " + name, emitted.contains(name));
			}
		}
	}

	public void testSettingsWithoutCssAreNoTokens() {
		for (ThemeSetting setting : settings()) {
			if (!setting.isCssRelevant()) {
				assertFalse("Setting without a CSS custom property is no token: " + setting.getName(),
					ThemeDesignTokens.isEmitted(setting));
				assertNull("Setting without a CSS custom property has no token kind: " + setting.getName(),
					ThemeDesignTokens.kindOf(setting));
			}
		}
	}

	public void testMimeSettingsAreNoTokens() {
		for (ThemeSetting setting : settings()) {
			if (setting.getName().startsWith(ThemeImage.MIME_PREFIX)) {
				assertFalse("A mime type setting is not emitted: " + setting.getName(),
					ThemeDesignTokens.isEmitted(setting));
			}
		}
	}

	private Set<String> emittedLocalNames() {
		Set<String> result = new HashSet<>();
		for (ThemeSetting setting : settings()) {
			if (ThemeDesignTokens.isEmitted(setting)) {
				result.add(setting.getLocalName());
			}
		}
		return result;
	}

	/**
	 * All settings of all themes.
	 */
	private List<ThemeSetting> settings() {
		List<ThemeSetting> result = new ArrayList<>();
		for (Theme theme : ThemeFactory.getInstance().getAllThemes()) {
			ThemeSettings settings = theme.getSettings();
			if (settings != null) {
				result.addAll(settings.getSettings());
			}
		}
		assertFalse("The themes declare settings.", result.isEmpty());
		return result;
	}

	/**
	 * The suite of tests.
	 */
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(
			ServiceTestSetup.createSetup(TestThemeDesignTokens.class, DesignTokenService.Module.INSTANCE));
	}

}
