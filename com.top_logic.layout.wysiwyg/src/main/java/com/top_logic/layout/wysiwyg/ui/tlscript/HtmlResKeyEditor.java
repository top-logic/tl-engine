/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui.tlscript;

import static com.top_logic.layout.form.template.model.Templates.*;
import static com.top_logic.layout.form.values.edit.editor.ItemEditor.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey.Builder;
import com.top_logic.basic.util.ResKeyUtil;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.html.i18n.DefaultHtmlResKey;
import com.top_logic.html.i18n.HtmlResKey;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.BlockControl;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.values.Fields;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.ValueModel;
import com.top_logic.layout.form.values.edit.editor.AbstractEditor;
import com.top_logic.layout.form.values.edit.editor.EditorUtils;
import com.top_logic.layout.form.values.edit.editor.InternationalizationEditor;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.layout.wysiwyg.ui.StructuredTextUtil;
import com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredText;
import com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredTextControlProvider;
import com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredTextField;

/**
 * {@link AbstractEditor} displaying an {@link HtmlResKey} using an {@link I18NStructuredTextField}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class HtmlResKeyEditor extends AbstractEditor {

	@Override
	public FormMember createUI(EditorFactory editorFactory, FormContainer container, ValueModel model) {
		PropertyDescriptor property = model.getProperty();
		FormGroup group = Fields.group(container, editorFactory, property);

		FormMember i18nField = super.createUI(editorFactory, group, model);
		BlockControl errorBlock = EditorUtils.errorBlock(i18nField);
		boolean minimized = Fields.displayMinimized(editorFactory, property);
		template(group, div(css(ITEM_CSS_CLASS),
			fieldsetBox(
				span(css(ITEM_TITLE_CSS_CLASS), label(), htmlTemplate(errorBlock)),
				member(i18nField),
				ConfigKey.field(group)).setInitiallyCollapsed(minimized)));
		return group;
	}

	@Override
	protected FormField addField(EditorFactory editorFactory, FormContainer container, ValueModel model,
			String fieldName) {

		I18NStructuredTextField field = I18NStructuredTextField.new18NStructuredTextField(fieldName, false, false, null, null);
		field.setControlProvider(I18NStructuredTextControlProvider.INSTANCE);
		container.addMember(field);

		init(editorFactory, model, field,
			new HtmlResKeyToI18NStructuredText(),
			new I18NStructuredTextToHtmlResKey((HtmlResKey) model.getValue()));

		return field;
	}

	/**
	 * Mapping from {@link HtmlResKey} to {@link I18NStructuredText}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public class HtmlResKeyToI18NStructuredText implements Mapping<Object, Object> {

		@Override
		public Object map(Object modelValue) {
			if (modelValue == null) {
				return I18NStructuredText.EMPTY;
			}
			if (!(modelValue instanceof HtmlResKey)) {
				throw new IllegalArgumentException("Expected '" + HtmlResKey.class.getName() + "' but got '"
					+ modelValue.getClass().getName() + "': " + modelValue);
			}
			ResKey key = ((DefaultHtmlResKey) modelValue).content();
			Map<Locale, StructuredText> content = new HashMap<>();
			for (Entry<Locale, String> entry : ResKeyUtil.toMap(key).entrySet()) {
				content.put(entry.getKey(), new StructuredText(entry.getValue()));
			}
			if (content.isEmpty()) {
				return I18NStructuredText.EMPTY;
			}
			return new I18NStructuredText(content);
		}

	}

	/**
	 * Mapping from {@link I18NStructuredText} to {@link HtmlResKey}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public class I18NStructuredTextToHtmlResKey implements Mapping<Object, Object> {

		private HtmlResKey _baseValue;

		/**
		 * Creates a {@link I18NStructuredTextToHtmlResKey}.
		 */
		public I18NStructuredTextToHtmlResKey(HtmlResKey baseValue) {
			_baseValue = baseValue;
		}

		@Override
		public Object map(Object uiValue) {
			if (uiValue == null) {
				return null;
			}
			if (!(uiValue instanceof I18NStructuredText)) {
				throw new IllegalArgumentException("Expected '" + I18NStructuredText.class.getName() + "' but got '"
					+ uiValue.getClass().getName() + "': " + uiValue);
			}
			I18NStructuredText text = (I18NStructuredText) uiValue;
			Builder keyBuilder = builder();
			ResourcesModule resources = ResourcesModule.getInstance();
			List<Locale> supportedLocales = resources.getSupportedLocales();
			for (Locale supportedLocale : supportedLocales) {
				StructuredText structured = text.localizeStrict(supportedLocale);
				if (structured != null) {
					String html;
					if (structured.getImages().isEmpty()) {
						html = structured.getSourceCode();
					} else {
						try {
							html = StructuredTextUtil.getCodeWithInlinedImages(structured);
						} catch (IOException ex) {
							throw new UncheckedIOException(ex);
						}
					}
					keyBuilder.add(supportedLocale, html);
				}
			}
			ResKey key = keyBuilder.build();
			if (key == null) {
				return null;
			}
			return new DefaultHtmlResKey(key);
		}

		private Builder builder() {
			ResKey baseKey;
			if (_baseValue != null) {
				baseKey = ((DefaultHtmlResKey) _baseValue).content();
			} else {
				baseKey = null;
			}
			return InternationalizationEditor.builder(baseKey);
		}

	}

}
