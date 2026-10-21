/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.codeedit;

import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.format.configured.FormatterService;
import com.top_logic.basic.func.Function0;
import com.top_logic.basic.html.SafeHTML;
import com.top_logic.basic.mime.MimeTypesModule;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.html.template.config.HTMLTemplate;
import com.top_logic.layout.configedit.ConfigControlService;
import com.top_logic.layout.configedit.ConfigFieldModel;
import com.top_logic.layout.form.values.edit.annotation.Code;
import com.top_logic.layout.form.values.edit.annotation.CodeLanguage;
import com.top_logic.layout.form.values.edit.annotation.MultiLine;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.codeedit.CodeConfigProvider;
import com.top_logic.layout.react.codeedit.CodeEditorLanguage;
import com.top_logic.layout.react.codeedit.ReactCodeEditorControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.select.ReactDropdownSelectControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.FieldState;
import com.top_logic.layout.react.window.ReactWindowRegistry;

/**
 * Test case for {@link CodeConfigProvider} in the configuration of the application, where it
 * overrides the text area the configuration editor uses for {@link Code} without a code editor.
 */
@SuppressWarnings("javadoc")
public class TestCodeConfigProvider extends TestCase {

	public static class Formats extends Function0<List<String>> {
		@Override
		public List<String> apply() {
			return List.of("a", "b");
		}
	}

	public interface CodeConfig extends ConfigurationItem {

		String JSON = "json";

		String HTML_TEMPLATE = "html-template";

		String JSON_LIST = "json-list";

		String JSON_CHOICE = "json-choice";

		String MULTI_LINE_XML = "multi-line-xml";

		@Name(JSON)
		@Code(language = CodeLanguage.JSON)
		String getJson();

		@Name(HTML_TEMPLATE)
		HTMLTemplate getHtmlTemplate();

		@Name(JSON_LIST)
		@Code(language = CodeLanguage.JSON)
		@Format(CommaSeparatedStrings.class)
		List<String> getJsonList();

		@Name(JSON_CHOICE)
		@Code(language = CodeLanguage.JSON)
		@Options(fun = Formats.class)
		String getJsonChoice();

		@Name(MULTI_LINE_XML)
		@Code(language = CodeLanguage.XML)
		@MultiLine(rows = 3)
		String getMultiLineXml();

	}

	private CodeConfig _config;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_config = TypedConfiguration.newConfigItem(CodeConfig.class);
	}

	/** A {@link Code} text is edited in the code editor for its language. */
	public void testCodeEditor() {
		assertCodeEditor(CodeEditorLanguage.JSON, true, control(model(CodeConfig.JSON)));
	}

	/** A {@link Code} text that cannot be edited is displayed in a read-only code editor. */
	public void testReadOnlyCodeEditor() {
		ConfigFieldModel model = model(CodeConfig.JSON);
		model.setEditable(false);
		assertCodeEditor(CodeEditorLanguage.JSON, false, control(model));
	}

	/** An HTML template is edited in the HTML code editor by its value type. */
	public void testHtmlTemplate() {
		assertCodeEditor(CodeEditorLanguage.HTML, true, control(model(CodeConfig.HTML_TEMPLATE)));
	}

	/** A formatted value is edited as its text. */
	public void testFormattedValue() {
		assertCodeEditor(CodeEditorLanguage.JSON, true, control(model(CodeConfig.JSON_LIST)));
	}

	/**
	 * The overriding entry keeps the position of the entry it overrides, before {@link MultiLine}.
	 */
	public void testCodeBeforeMultiLine() {
		assertCodeEditor(CodeEditorLanguage.XML, true, control(model(CodeConfig.MULTI_LINE_XML)));
	}

	/** A property edited by selecting stays a select. */
	public void testSelectStaysASelect() {
		assertTrue(control(model(CodeConfig.JSON_CHOICE)) instanceof ReactDropdownSelectControl);
	}

	private static void assertCodeEditor(CodeEditorLanguage language, boolean editable, ReactControl control) {
		assertTrue("Not a code editor: " + control, control instanceof ReactCodeEditorControl);
		Map<String, Object> state = control.scriptingScalarState();
		assertEquals(language.clientId(), state.get(ReactCodeEditorControl.LANGUAGE));
		assertEquals(Boolean.valueOf(editable), state.get(FieldState.EDITABLE__PROP));
	}

	private ConfigFieldModel model(String propertyName) {
		PropertyDescriptor property = _config.descriptor().getProperty(propertyName);
		return ConfigControlService.getInstance().createModel(_config, property);
	}

	private static ReactControl control(ConfigFieldModel model) {
		return ConfigControlService.getInstance().createControl(context(), model);
	}

	private static ReactContext context() {
		return new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
	}

	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestCodeConfigProvider.class,
				ThreadContextManager.Module.INSTANCE, TypeIndex.Module.INSTANCE, FormatterService.Module.INSTANCE,
				MimeTypesModule.Module.INSTANCE, ThemeFactory.Module.INSTANCE, SafeHTML.Module.INSTANCE,
				ConfigControlService.Module.INSTANCE));
	}

}
