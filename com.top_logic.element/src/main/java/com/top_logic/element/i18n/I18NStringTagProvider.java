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
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.tag.DisplayProvider;
import com.top_logic.layout.CompositeControl;
import com.top_logic.layout.Control;
import com.top_logic.layout.DefaultControlRenderer;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.FormConstants;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.TextInputControl;
import com.top_logic.layout.form.template.FormTemplateConstants;
import com.top_logic.layout.form.values.MultiLineText;
import com.top_logic.layout.form.values.edit.editor.I18NTranslationUtil;
import com.top_logic.layout.form.values.edit.editor.InternationalizationEditor;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.annotate.ui.MultiLine;
import com.top_logic.util.Resources;

/**
 * {@link DisplayProvider} for I18N attributes.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class I18NStringTagProvider implements DisplayProvider {

	/** CSS class for I18N String fields table tag. */
	public static final String I18N_STRING_TABLE_CSS_CLASS = "i18nStringTable tl-table";

	/** CSS class for I18N String fields TD tag. */
	public static final String I18N_STRING_TD_CSS_CLASS = "i18nStringTD";

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
	 * Control renderer for {@link I18NStringField} fields that renders only the value of the
	 * current {@link Locale}.
	 */
	public static class I18NStringActiveLanguageControlRenderer extends DefaultControlRenderer<CompositeControl> {

		/** Instance of this class. */
		public static final I18NStringActiveLanguageControlRenderer INSTANCE =
			new I18NStringActiveLanguageControlRenderer();

		@Override
		protected String getControlTag(CompositeControl control) {
			return SPAN;
		}

		@Override
		protected void writeControlContents(DisplayContext context, TagWriter out, CompositeControl control)
				throws IOException {
			Locale currentLocale = context.getResources().getLocale();
			List<? extends HTMLFragment> controls = control.getChildren();
			int i = 0;
			I18NField<?, ?, ?> i18nField = (I18NField<?, ?, ?>) control.getModel();
			for (FormField field : i18nField.getLanguageFields()) {
				Locale fieldLocale = I18NTranslationUtil.getLocaleFromField(field);
				HTMLFragment fieldControl = controls.get(i++);
				if (!I18NTranslationUtil.equalLanguage(currentLocale, fieldLocale)) {
					continue;
				}
				fieldControl.write(context, out);
			}
		}

	}

	/**
	 * Control renderer for I18NString fields.
	 */
	public static class I18NStringControlRenderer extends DefaultControlRenderer<CompositeControl> {

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
			out.beginBeginTag(TABLE);
			out.writeAttribute(CLASS_ATTR, I18N_STRING_TABLE_CSS_CLASS);
			out.endBeginTag();

			boolean noTanslation = !TranslationService.isActive();

			int i = 0;
			I18NField<?, ?, ?> i18nField = (I18NField<?, ?, ?>) control.getModel();
			for (FormField field : i18nField.getLanguageFields()) {
				HTMLFragment fieldControl = controls.get(i++);
				HTMLFragment errorControl = controls.get(i++);
				// There is no translate button for the field containing the source that is translated.
				HTMLFragment translateButton = noTanslation || isSourceField(field) ? null : controls.get(i++);

				if (_languagesAboveField) {
					out.beginTag(TR);

					out.beginBeginTag(TD);
					out.writeAttribute(CLASS_ATTR, I18N_STRING_TD_CSS_CLASS);
					out.endBeginTag();
					writeLanguage(out, res, field);
					writeTranslateButton(context, out, translateButton);
					out.endTag(TD);

					out.endTag(TR);
					out.beginTag(TR);

					out.beginTag(TD);
					fieldControl.write(context, out);
					errorControl.write(context, out);
					out.endTag(TD);

					out.endTag(TR);
				} else {
					out.beginTag(TR);

					out.beginBeginTag(TD);
					out.writeAttribute(CLASS_ATTR, I18N_STRING_TD_CSS_CLASS);
					out.endBeginTag();
					writeLanguage(out, res, field);
					out.endTag(TD);

					out.beginTag(TD);
					if (field.isActive()) {
						out.beginTag(SPAN, CLASS_ATTR, "cDecoratedCell");
						out.beginTag(SPAN, CLASS_ATTR, FormConstants.FLEXIBLE_CSS_CLASS);
						fieldControl.write(context, out);
						out.endTag(SPAN);
						out.beginTag(SPAN, CLASS_ATTR, FormConstants.FIXED_RIGHT_CSS_CLASS);
						writeTranslateButton(context, out, translateButton);
						errorControl.write(context, out);
						out.endTag(SPAN);
						out.endTag(SPAN);
					} else {
						fieldControl.write(context, out);
						errorControl.write(context, out);
					}
					out.endTag(TD);

					out.endTag(TR);
				}

			}
			out.endTag(TABLE);
		}

		private void writeLanguage(TagWriter out, Resources res, FormField field) {
			Locale language = field.get(I18NField.LANGUAGE);
			out.writeText(InternationalizationEditor.translateLanguageName(res, language) + ":");
		}

		private void writeTranslateButton(DisplayContext context, TagWriter out, HTMLFragment translateButton)
				throws IOException {
			if (translateButton != null) {
				translateButton.write(context, out);
			}
		}

		private boolean isSourceField(FormField field) {
			return I18NTranslationUtil.isSourceField(field);
		}

	}

}
