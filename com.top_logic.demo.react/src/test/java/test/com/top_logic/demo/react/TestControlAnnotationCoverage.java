/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.demo.react;

import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.layout.form.values.edit.ControlAnnotationCoverage;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.configedit.ConfigControlService;
import com.top_logic.layout.configedit.ConfigControlService.AnnotationControlMapping;
import com.top_logic.layout.configedit.ConfigEditorControl;
import com.top_logic.layout.form.values.edit.annotation.ControlAnnotation;
import com.top_logic.layout.form.values.edit.annotation.Inline;

/**
 * Test that the React UI maps every {@link ControlAnnotation} of the application to a control.
 */
@SuppressWarnings("javadoc")
public class TestControlAnnotationCoverage extends TestCase {

	/**
	 * Control annotations the React UI does not map, each with the reason.
	 */
	private static final Map<String, String> EXCEPTIONS = Map.of(
		Inline.class.getName(),
		"The " + ConfigEditorControl.class.getSimpleName() + " lays out the nested item itself.",
		"com.top_logic.layout.scripting.template.gui.templates.ScriptTemplateParameters",
		"The script template module has no React UI.",
		"com.top_logic.service.openapi.server.ApplicationRelativeURL",
		"The OpenAPI server module has no React UI.");

	public void testReactUI() throws ConfigurationException {
		ConfigControlService.Config config = (ConfigControlService.Config) ApplicationConfig.getInstance()
			.getServiceConfiguration(ConfigControlService.class);
		ControlAnnotationCoverage.assertMapped("React UI",
			config.getAnnotationControls().stream().map(AnnotationControlMapping::getAnnotation).toList(),
			EXCEPTIONS);
	}

	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestControlAnnotationCoverage.class, TypeIndex.Module.INSTANCE));
	}

}
