/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.i18n;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.translation.TranslationService;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.tag.DisplayProvider;
import com.top_logic.layout.CompositeControl;
import com.top_logic.layout.Control;
import com.top_logic.layout.DefaultControlRenderer;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.TextInputControl;
import com.top_logic.layout.form.template.FormTemplateConstants;
import com.top_logic.layout.form.values.MultiLineText;
import com.top_logic.layout.form.values.edit.editor.I18NTranslationUtil;
import com.top_logic.layout.form.values.edit.editor.InternationalizationEditor;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.annotate.ui.MultiLine;
import com.top_logic.model.form.ReactiveFormCSS;
import com.top_logic.util.Resources;

/**
 * {@link DisplayProvider} for I18N attributes.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class I18NStringTagProvider implements DisplayProvider {

	@Override
	public Control createDisplay(EditContext editContext, FormMember member) {
		int rows;
		boolean multiline;
		if (AttributeOperations.isMultiline(editContext) && !editContext.isSearchUpdate()) {
			MultiLine annotation = editContext.getAnnotation(MultiLine.class);
			if (annotation != null) {
				rows = annotation.getRows();
			} else {
				rows = MultiLineText.DEFAULT_ROWS;
			}

			multiline = true;
		} else {
			rows = 0;
			multiline = false;
		}
		return new I18NStringControlProvider(multiline, rows,
			DisplayAnnotations.inputSize(editContext, TextInputControl.NO_COLUMNS)).createControl(member,
				FormTemplateConstants.STYLE_DIRECT_VALUE);
	}

	/**
	 * Control renderer for {@link I18NField} fields that renders only the value of the current
	 * {@link Locale}.
	 */
	public static abstract class I18NFieldActiveLanguageControlRenderer
			extends DefaultControlRenderer<CompositeControl> {

		@Override
		protected String getControlTag(CompositeControl control) {
			return SPAN;
		}

		@Override
		protected void writeControlContents(DisplayContext context, TagWriter out, CompositeControl control)
				throws IOException {
			I18NField<?, ?, ?> i18nField = (I18NField<?, ?, ?>) control.getModel();
			writeFieldValue(context, out, i18nField);
		}

		/**
		 * Writes the value of the given {@link I18NField}.
		 */
		protected abstract void writeFieldValue(DisplayContext context, TagWriter out, I18NField<?, ?, ?> i18nField)
				throws IOException;

	}

	/**
	 * {@link I18NFieldActiveLanguageControlRenderer} rendering {@link I18NStringField}.
	 */
	public static class I18NStringActiveLanguageControlRenderer extends I18NFieldActiveLanguageControlRenderer {

		/** Instance of this class. */
		public static final I18NStringActiveLanguageControlRenderer INSTANCE =
			new I18NStringActiveLanguageControlRenderer();

		@Override
		protected void writeFieldValue(DisplayContext context, TagWriter out, I18NField<?, ?, ?> i18nField)
				throws IOException {
			boolean multiline = ((I18NStringField) i18nField).isMultiline();
			InternationalizationEditor.writeResKey(context, out, (ResKey) i18nField.getValue(), multiline);
		}

	}

	/**
	 * Control renderer for I18NString fields.
	 */
	public static class I18NStringControlRenderer extends DefaultControlRenderer<CompositeControl> {

		private static final String TL_I18N_TRANSLATE = "tl-i18n-translate";

		/** Instance of this class drawing language labels in same line as the fields. */
		public static final I18NStringControlRenderer INSTANCE = new I18NStringControlRenderer(false);

		/** Instance of this class drawing language labels above the fields. */
		public static final I18NStringControlRenderer ABOVE_INSTANCE = new I18NStringControlRenderer(true);

		/**
		 * Flag whether to draw the language labels above the fields (true) or in same line (false).
		 */
		private final boolean _languagesAboveField;

		/**
		 * Creates a new {@link I18NStringControlRenderer}.
		 */
		public I18NStringControlRenderer(boolean languagesAboveField) {
			_languagesAboveField = languagesAboveField;
		}

		@Override
		protected String getControlTag(CompositeControl control) {
			return SPAN;
		}

		@Override
		protected void writeControlContents(DisplayContext context, TagWriter out, CompositeControl control)
				throws IOException {
			Resources res = Resources.getInstance();
			List<? extends HTMLFragment> controls = control.getChildren();
			out.beginBeginTag(DIV);
			out.writeAttribute(CLASS_ATTR, ReactiveFormCSS.RF_COLUMNS_LAYOUT + " cols1");
			out.endBeginTag();

			boolean noTanslation = !TranslationService.isActive();

			int i = 0;
			I18NField<?, ?, ?> i18nField = (I18NField<?, ?, ?>) control.getModel();
			for (FormField field : i18nField.getLanguageFields()) {
				HTMLFragment fieldControl = controls.get(i++);
				HTMLFragment errorControl = controls.get(i++);
				// There is no translate button for the field containing the source that is translated.
				HTMLFragment translateButton = noTanslation || isSourceField(field) ? null : controls.get(i++);

				out.beginBeginTag(DIV);
				out.writeAttribute(CLASS_ATTR, ReactiveFormCSS.RF_INPUT_CELL
					+ (_languagesAboveField ? (" " + ReactiveFormCSS.RF_LABEL_ABOVE) : ""));
				out.endBeginTag();
				{
					{
						out.beginBeginTag(SPAN);
						out.writeAttribute(CLASS_ATTR, ReactiveFormCSS.RF_LABEL);
						out.endBeginTag();
						{
							writeLanguage(out, res, field);
							writeTranslateButton(context, out, translateButton);
							errorControl.write(context, out);
						}
						out.endTag(SPAN);
					}

					if (field.isActive()) {
						out.beginTag(SPAN, CLASS_ATTR, ReactiveFormCSS.RF_CELL);
						fieldControl.write(context, out);
						out.endTag(SPAN);
					} else {
						out.beginTag(SPAN, CLASS_ATTR, ReactiveFormCSS.RF_CELL);
						fieldControl.write(context, out);
						out.endTag(SPAN);
					}
				}
				out.endTag(DIV);
			}
			out.endTag(DIV);
		}

		private void writeLanguage(TagWriter out, Resources res, FormField field) {
			Locale language = field.get(I18NField.LANGUAGE);
			out.beginTag(LABEL);
			out.writeText(InternationalizationEditor.translateLanguageName(res, language) + ":");
			out.endTag(LABEL);
		}

		private void writeTranslateButton(DisplayContext context, TagWriter out, HTMLFragment translateButton)
				throws IOException {
			if (translateButton != null) {
				out.beginTag(SPAN, CLASS_ATTR, TL_I18N_TRANSLATE);
				translateButton.write(context, out);
				out.endTag(SPAN);
			}
		}

		private boolean isSourceField(FormField field) {
			return I18NTranslationUtil.isSourceField(field);
		}

	}

}
