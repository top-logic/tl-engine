/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.wysiwyg;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

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
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKeyUtil;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.configedit.ConfigControlService;
import com.top_logic.layout.configedit.ConfigFieldModel;
import com.top_logic.layout.configedit.ConfigFormatFieldModel;
import com.top_logic.layout.form.values.edit.annotation.Html;
import com.top_logic.layout.form.values.edit.annotation.MultiLine;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.form.I18NLocalizedFieldModel;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.control.form.ReactRenderedTextControl;
import com.top_logic.layout.react.control.html.ReactHtmlControl;
import com.top_logic.layout.react.control.select.ReactDropdownSelectControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.FieldState;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.react.wysiwyg.HtmlConfigProvider;
import com.top_logic.layout.react.wysiwyg.I18NWysiwygControl;
import com.top_logic.layout.react.wysiwyg.ReactWysiwygControl;
import com.top_logic.model.search.expr.config.MacroFormat;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * Test case for {@link HtmlConfigProvider} in the configuration of the application, where it
 * overrides the text area the configuration editor uses for {@link Html} without a WYSIWYG editor.
 */
@SuppressWarnings("javadoc")
public class TestHtmlConfigProvider extends TestCase {

	public static class Formats extends Function0<List<String>> {
		@Override
		public List<String> apply() {
			return List.of("<p>a</p>", "<p>b</p>");
		}
	}

	public interface HtmlConfig extends ConfigurationItem {

		String TEXT = "text";

		String MACRO_TEXT = "macro-text";

		String MULTI_LINE_TEXT = "multi-line-text";

		String MACRO = "macro";

		String I18N = "i18n";

		String CHOICE = "choice";

		@Name(TEXT)
		@Html
		String getText();

		void setText(String value);

		@Name(MACRO_TEXT)
		@Html(features = "macro")
		String getMacroText();

		@Name(MULTI_LINE_TEXT)
		@Html
		@MultiLine(rows = 3)
		String getMultiLineText();

		@Name(MACRO)
		@Html(features = "macro")
		@Format(MacroFormat.class)
		Expr getMacro();

		@Name(I18N)
		@Html
		ResKey getI18n();

		@Name(CHOICE)
		@Html
		@Options(fun = Formats.class)
		String getChoice();

	}

	private HtmlConfig _config;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_config = TypedConfiguration.newConfigItem(HtmlConfig.class);
	}

	/** An HTML text is edited in the WYSIWYG editor with its standard features. */
	public void testWysiwygEditor() {
		ReactWysiwygControl editor = editor(control(model(HtmlConfig.TEXT)));
		assertTrue("The text holds no images.", editor.isTextOnly());
		assertFalse(editor.isSourceEditing());
	}

	/** The macro feature set offers editing the HTML source. */
	public void testSourceEditing() {
		assertTrue(editor(control(model(HtmlConfig.MACRO_TEXT))).isSourceEditing());
	}

	/**
	 * The overriding entry keeps the position of the entry it overrides, before {@link MultiLine}.
	 */
	public void testHtmlBeforeMultiLine() {
		editor(control(model(HtmlConfig.MULTI_LINE_TEXT)));
	}

	/** The edited text is stored as HTML source. */
	public void testEditText() {
		ReactWysiwygControl editor = editor(control(model(HtmlConfig.TEXT)));
		editor.executeCommand(ReactFormFieldControl.CMD_VALUE_CHANGED,
			Map.of(FieldState.VALUE__PROP, "<p>A <strong>bold</strong> word.</p>"));
		assertEquals("<p>A <strong>bold</strong> word.</p>", _config.getText());

		editor.executeCommand(ReactFormFieldControl.CMD_VALUE_CHANGED, Map.of(FieldState.VALUE__PROP, "<p></p>"));
		String emptied = _config.getText();
		assertTrue("An emptied editor is no text: " + emptied, emptied == null || emptied.isEmpty());
	}

	/**
	 * A text that does not pass the HTML safety check is not stored: the field reports the problem
	 * and keeps its value, until a safe text is entered.
	 */
	public void testUnsafeTextIsRejected() {
		_config.setText("<p>safe</p>");
		ConfigFieldModel model = model(HtmlConfig.TEXT);
		ReactWysiwygControl editor = editor(control(model));
		assertTrue(editor.isCheckHtml());

		editor.executeCommand(ReactFormFieldControl.CMD_VALUE_CHANGED,
			Map.of(FieldState.VALUE__PROP, "<p onclick=\"alert(1)\">unsafe</p>"));
		assertEquals("<p>safe</p>", _config.getText());
		assertNotNull("The problem is reported at the field.", model.getInputError());
		assertTrue(model.hasError());

		editor.executeCommand(ReactFormFieldControl.CMD_VALUE_CHANGED,
			Map.of(FieldState.VALUE__PROP, "<p class=\"note\">safe again</p>"));
		assertEquals("<p class=\"note\">safe again</p>", _config.getText());
		assertNull(model.getInputError());
	}

	/**
	 * Markup the editor keeps without understanding it - custom data attributes, the width of a
	 * resized column, line breaks and images written without closing slash - is stored.
	 */
	public void testKeptMarkupIsAccepted() {
		ConfigFieldModel model = model(HtmlConfig.TEXT);
		ReactWysiwygControl editor = editor(control(model));
		String html = "<p class=\"lead\" data-foo=\"1\">a<br>b <span data-y=\"2\">c</span> <img src=\"x.png\"></p>"
			+ "<table style=\"min-width: 75px\"><colgroup><col style=\"min-width: 25px\"></colgroup><tbody><tr>"
			+ "<td colspan=\"1\" rowspan=\"1\" colwidth=\"120\"><p>x</p></td></tr></tbody></table>";
		editor.executeCommand(ReactFormFieldControl.CMD_VALUE_CHANGED, Map.of(FieldState.VALUE__PROP, html));
		assertNull("Unexpected problem: " + model.getInputError(), model.getInputError());
		assertEquals(html, _config.getText());
	}

	/** An unsafe macro is rejected like an unsafe text, before it is parsed. */
	public void testUnsafeMacroIsRejected() {
		ConfigFieldModel model = model(HtmlConfig.MACRO);
		ReactWysiwygControl editor = editor(control(model));
		editor.executeCommand(ReactFormFieldControl.CMD_VALUE_CHANGED,
			Map.of(FieldState.VALUE__PROP, "<p>{$model}<script>alert(1)</script></p>"));
		assertNull("Nothing is stored.", _config.getMacro());
		assertNotNull("The problem is reported at the field.", model.getInputError());
	}

	/** An unsafe translation is rejected at the internationalized field. */
	public void testUnsafeTranslationIsRejected() {
		ConfigFieldModel model = model(HtmlConfig.I18N);
		I18NWysiwygControl inline = find(rendered(control(model)).getInput(), I18NWysiwygControl.class);
		assertTrue(inline.isCheckHtml());
		inline.executeCommand(ReactFormFieldControl.CMD_VALUE_CHANGED,
			Map.of(FieldState.VALUE__PROP, "<img src=\"x\" onerror=\"alert(1)\">"));
		assertNull("Nothing is stored.", _config.getI18n());
		assertNotNull("The problem is reported at the field.", model.getInputError());
	}

	/** A text that cannot be edited is displayed formatted, if it passes the safety check. */
	public void testReadOnly() {
		_config.setText("A <b>bold</b> word.");
		ConfigFieldModel model = model(HtmlConfig.TEXT);
		model.setEditable(false);
		ReactRenderedTextControl control = rendered(control(model));
		assertEquals("A read-only text shows the display.", 1, control.getActiveIndex());
		Map<String, Object> state = control.getDisplay().scriptingScalarState();
		assertNull(state.get(ReactHtmlControl.ERROR));
		assertEquals("A <b>bold</b> word.", state.get(ReactHtmlControl.HTML));

		_config.setText("<script>alert(1)</script>");
		state = control.getDisplay().scriptingScalarState();
		assertNotNull("The HTML safety check must refuse the text.", state.get(ReactHtmlControl.ERROR));
	}

	/**
	 * A TL-Script expression written as HTML with embedded expressions is edited as its text in
	 * the WYSIWYG editor with source editing.
	 */
	public void testMacro() {
		ConfigFieldModel model = model(HtmlConfig.MACRO);
		assertTrue(model instanceof ConfigFormatFieldModel);
		ReactWysiwygControl editor = editor(control(model));
		assertTrue(editor.isTextOnly());
		assertTrue(editor.isSourceEditing());

		editor.executeCommand(ReactFormFieldControl.CMD_VALUE_CHANGED,
			Map.of(FieldState.VALUE__PROP, "<p>Hello {$model}!</p>"));
		assertNotNull("The macro is parsed into an expression.", _config.getMacro());
		assertEquals("<p>Hello {$model}!</p>", model.getValue());
	}

	/** Each translation of an internationalized HTML text is edited in the WYSIWYG editor. */
	public void testInternationalized() {
		ConfigFieldModel model = model(HtmlConfig.I18N);
		ReactRenderedTextControl control = rendered(control(model));
		I18NWysiwygControl inline = find(control.getInput(), I18NWysiwygControl.class);
		assertNotNull("The text in the user's language is edited inline.", inline);
		assertTrue(inline.isTextOnly());

		inline.executeCommand(ReactFormFieldControl.CMD_VALUE_CHANGED,
			Map.of(FieldState.VALUE__PROP, "<p>A <em>text</em>.</p>"));
		ResKey value = _config.getI18n();
		assertNotNull(value);
		Locale locale = I18NLocalizedFieldModel.editLocale();
		assertEquals("<p>A <em>text</em>.</p>",
			ResKeyUtil.translateWithoutFallback(locale, value));

		model.setEditable(false);
		assertEquals("A read-only text shows the display.", 1, control.getActiveIndex());
		Map<String, Object> state = control.getDisplay().scriptingScalarState();
		assertNull(state.get(ReactHtmlControl.ERROR));
		assertEquals("<p>A <em>text</em>.</p>", state.get(ReactHtmlControl.HTML));
	}

	/** A property edited by selecting stays a select. */
	public void testSelectStaysASelect() {
		assertTrue(control(model(HtmlConfig.CHOICE)) instanceof ReactDropdownSelectControl);
	}

	private static ReactRenderedTextControl rendered(ReactControl control) {
		assertTrue("Not a rendered text: " + control, control instanceof ReactRenderedTextControl);
		return (ReactRenderedTextControl) control;
	}

	private static ReactWysiwygControl editor(ReactControl control) {
		ReactControl input = rendered(control).getInput();
		assertTrue("Not a WYSIWYG editor: " + input, input instanceof ReactWysiwygControl);
		return (ReactWysiwygControl) input;
	}

	/**
	 * The given control, or the first control of the given type among its descendants.
	 */
	private static <T extends ReactControl> T find(ReactControl control, Class<T> type) {
		if (type.isInstance(control)) {
			return type.cast(control);
		}
		for (ReactControl child : control.scriptingChildren()) {
			T result = find(child, type);
			if (result != null) {
				return result;
			}
		}
		return null;
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
			ServiceTestSetup.createSetup(TestHtmlConfigProvider.class,
				ThreadContextManager.Module.INSTANCE, TypeIndex.Module.INSTANCE, FormatterService.Module.INSTANCE,
				MimeTypesModule.Module.INSTANCE, ThemeFactory.Module.INSTANCE, SafeHTML.Module.INSTANCE,
				ConfigControlService.Module.INSTANCE));
	}

}
