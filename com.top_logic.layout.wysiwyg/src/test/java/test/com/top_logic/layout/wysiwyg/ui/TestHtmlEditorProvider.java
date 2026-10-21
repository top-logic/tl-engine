/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.wysiwyg.ui;

import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.annotation.Html;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay.ItemDisplayType;
import com.top_logic.layout.form.values.edit.annotation.MultiLine;
import com.top_logic.layout.form.values.edit.initializer.InitializerIndex;
import com.top_logic.layout.form.values.edit.initializer.InitializerUtil;
import com.top_logic.layout.wysiwyg.ui.HtmlEditorProvider;
import com.top_logic.layout.wysiwyg.ui.StructuredTextConfigService;
import com.top_logic.layout.wysiwyg.ui.StructuredTextControlProvider;
import com.top_logic.model.search.expr.config.MacroFormat;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * Test that the configuration of this module edits a property annotated with {@link Html} in a
 * declarative form with the WYSIWYG editor of the annotated feature set, chosen by the
 * {@link HtmlEditorProvider}.
 */
@SuppressWarnings("javadoc")
public class TestHtmlEditorProvider extends TestCase {

	public interface HtmlProperties extends ConfigurationItem {

		String TEXT = "text";

		String MACRO_TEXT = "macro-text";

		String MULTI_LINE_TEXT = "multi-line-text";

		String MACRO = "macro";

		String I18N = "i18n";

		@Name(TEXT)
		@Html
		String getText();

		@Name(MACRO_TEXT)
		@Html(features = StructuredTextConfigService.FEATURE_SET_MACRO)
		String getMacroText();

		@Name(MULTI_LINE_TEXT)
		@Html
		@MultiLine(rows = 3)
		String getMultiLineText();

		@Name(MACRO)
		@Html(features = StructuredTextConfigService.FEATURE_SET_MACRO)
		@Format(MacroFormat.class)
		Expr getMacro();

		@Name(I18N)
		@Html
		@ItemDisplay(ItemDisplayType.MONOMORPHIC)
		ResKey getI18n();

	}

	/** An HTML text is edited in the standard WYSIWYG editor. */
	public void testStandardFeatureSet() {
		assertEditor(null, build().getField(HtmlProperties.TEXT));
	}

	/** The feature set of the annotation reaches the editor. */
	public void testFeatureSet() {
		assertEditor(StructuredTextConfigService.FEATURE_SET_MACRO, build().getField(HtmlProperties.MACRO_TEXT));
	}

	/**
	 * The overriding entry keeps the position of the entry it overrides, before {@link MultiLine}.
	 */
	public void testHtmlBeforeMultiLine() {
		assertEditor(null, build().getField(HtmlProperties.MULTI_LINE_TEXT));
	}

	/**
	 * A TL-Script expression written as HTML with embedded expressions is edited in the WYSIWYG
	 * editor, not in the TL-Script editor its value type would choose.
	 */
	public void testMacro() {
		assertEditor(StructuredTextConfigService.FEATURE_SET_MACRO, build().getField(HtmlProperties.MACRO));
	}

	/** Each translation of an internationalized HTML text is edited in the WYSIWYG editor. */
	public void testInternationalized() {
		FormGroup translations = (FormGroup) build().getMember(HtmlProperties.I18N);
		int inputs = 0;
		for (Iterator<FormField> it = translations.getFields(); it.hasNext();) {
			FormField translation = it.next();
			if (!(translation instanceof StringField) || translation.getName().contains("_")) {
				// Not a translation, but e.g. the translation of a derived key such as a tooltip.
				continue;
			}
			assertEditor(null, translation);
			inputs++;
		}
		assertTrue("No translation input.", inputs > 0);
	}

	private static void assertEditor(String expectedFeatureSet, FormField field) {
		ControlProvider controlProvider = field.getControlProvider();
		assertTrue("Not a WYSIWYG editor for '" + field.getName() + "': " + controlProvider,
			controlProvider instanceof StructuredTextControlProvider);
		assertEquals("Feature set of '" + field.getName() + "'.", expectedFeatureSet,
			((StructuredTextControlProvider) controlProvider).getFeatureSet());
	}

	private static FormGroup build() {
		EditorFactory.Config config = ApplicationConfig.getInstance().getConfig(EditorFactory.Config.class);
		FormGroup group = new FormGroup("group", null);
		ConfigurationItem model = TypedConfiguration.newConfigItem(HtmlProperties.class);
		EditorFactory editorFactory = new EditorFactory(config, new InitializerIndex());
		InitializerUtil.initAll(editorFactory.getInitializer(), model);
		editorFactory.initEditorGroup(group, model);
		return group;
	}

	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(TestHtmlEditorProvider.class);
	}

}
