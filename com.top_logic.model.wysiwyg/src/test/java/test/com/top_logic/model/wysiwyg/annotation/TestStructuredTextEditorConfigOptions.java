/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.wysiwyg.annotation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.form.values.edit.initializer.InitializerIndex;
import com.top_logic.layout.form.values.edit.initializer.InitializerUtil;
import com.top_logic.layout.wysiwyg.ui.StructuredTextConfigService;
import com.top_logic.model.wysiwyg.annotation.StructuredEditorConfigOptions;
import com.top_logic.model.wysiwyg.annotation.StructuredTextEditorConfig;

/**
 * Test that the configuration of this module offers the features of the HTML editor as options for
 * {@link StructuredTextEditorConfig#getFeatures()}.
 */
@SuppressWarnings("javadoc")
public class TestStructuredTextEditorConfigOptions extends TestCase {

	/** The options are not declared in the annotation interface itself. */
	public void testNotDeclaredInCode() {
		assertNull(featuresProperty().getAnnotation(Options.class));
	}

	/** The form customizations of the application attach the options function. */
	public void testCustomization() {
		Options options = newEditorFactory().getAnnotation(featuresProperty(), Options.class);
		assertNotNull("No options customization for '" + StructuredTextEditorConfig.FEATURES + "'.", options);
		assertEquals(StructuredEditorConfigOptions.class, options.fun());
	}

	/** A form editing the annotation offers the features of the HTML editor. */
	public void testFormOptions() {
		EditorFactory editorFactory = newEditorFactory();
		FormGroup group = new FormGroup("group", null);
		ConfigurationItem model = TypedConfiguration.newConfigItem(StructuredTextEditorConfig.class);
		InitializerUtil.initAll(editorFactory.getInitializer(), model);
		editorFactory.initEditorGroup(group, model);

		FormField field = group.getField(StructuredTextEditorConfig.FEATURES);
		assertTrue("Not a select field: " + field, field instanceof SelectField);
		Set<Object> options = new HashSet<>((List<?>) ((SelectField) field).getOptions());
		assertEquals(StructuredTextConfigService.getInstance().getFeatures().keySet(), options);
		assertFalse(options.isEmpty());
	}

	private static PropertyDescriptor featuresProperty() {
		return TypedConfiguration.getConfigurationDescriptor(StructuredTextEditorConfig.class)
			.getProperty(StructuredTextEditorConfig.FEATURES);
	}

	private static EditorFactory newEditorFactory() {
		EditorFactory.Config config = ApplicationConfig.getInstance().getConfig(EditorFactory.Config.class);
		return new EditorFactory(config, new InitializerIndex());
	}

	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestStructuredTextEditorConfigOptions.class,
				StructuredTextConfigService.Module.INSTANCE));
	}

}
