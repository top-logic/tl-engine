/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.demo;

import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.layout.form.values.edit.ControlAnnotationCoverage;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.EditorFactory.AnnotationControlMapping;
import com.top_logic.layout.form.values.edit.annotation.ControlAnnotation;

/**
 * Test that the classic UI maps every {@link ControlAnnotation} of the application to a control.
 */
@SuppressWarnings("javadoc")
public class TestControlAnnotationCoverage extends TestCase {

	public void testClassicUI() {
		EditorFactory.Config config = ApplicationConfig.getInstance().getConfig(EditorFactory.Config.class);
		ControlAnnotationCoverage.assertMapped("classic UI",
			config.getAnnotationControls().stream().map(AnnotationControlMapping::getAnnotation).toList(),
			Map.of());
	}

	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestControlAnnotationCoverage.class, TypeIndex.Module.INSTANCE));
	}

}
