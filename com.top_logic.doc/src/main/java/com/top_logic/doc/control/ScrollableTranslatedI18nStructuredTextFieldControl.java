/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.doc.control;

import java.util.Locale;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.Control;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.i18n.I18NField;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.wysiwyg.ui.StructuredTextControl;
import com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredTextField;

/**
 * A {@link TranslatedI18nStructuredTextFieldControl} that uses the scroll container of the component
 * to save the scroll position.
 * 
 * @author <a href="mailto:Diana.Pankratz@top-logic.com">Diana Pankratz</a>
 */
public class ScrollableTranslatedI18nStructuredTextFieldControl extends TranslatedI18nStructuredTextFieldControl {

	/**
	 * The {@link ControlProvider} for the
	 * {@link ScrollableTranslatedI18nStructuredTextFieldControl}.
	 */
	public static class Provider extends TranslatedI18nFieldControl.Provider {

		/**
		 * Creates a {@link ScrollableTranslatedI18nStructuredTextFieldControl.Provider}.
		 * 
		 * @param context
		 *        the {@link InstantiationContext} to create the new object in
		 * @param config
		 *        the configuration object to be used for instantiation
		 */
		public Provider(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		protected Control createControl(I18NField<?, ?, ?> i18nField, ComponentChannel language, String style) {
			return new ScrollableTranslatedI18nStructuredTextFieldControl((I18NStructuredTextField) i18nField, language,
				style);
		}

	}

	/**
	 * Creates a {@link ScrollableTranslatedI18nStructuredTextFieldControl}.
	 * 
	 * @param i18nField
	 *        Is not allowed to be null.
	 * @param language
	 *        Channel holding the {@link Locale} of the field to display.
	 * @param style
	 *        See the second parameter of {@link ControlProvider#createControl(Object, String)}.
	 */
	public ScrollableTranslatedI18nStructuredTextFieldControl(I18NStructuredTextField i18nField,
			ComponentChannel language, String style) {
		super(i18nField, language, style);
	}

	@Override
	protected StructuredTextControl createInnerControl(FormField innerField, String style) {
		StructuredTextControl innerControl = super.createInnerControl(innerField, style);
		innerControl.setUseComponentScrollPosition(true);
		return innerControl;
	}

}
