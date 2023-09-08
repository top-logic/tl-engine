/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.doc.control;

import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.Control;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.CompositeField;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.wysiwyg.ui.StructuredTextConfigService;
import com.top_logic.layout.wysiwyg.ui.StructuredTextControl;
import com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredTextField;

/**
 * A {@link TranslatedI18nFieldControl} for wysiwyg attributes.
 * <p>
 * The {@link FormField} has to be a {@link I18NStructuredTextField}.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TranslatedI18nStructuredTextFieldControl extends TranslatedI18nFieldControl<StructuredTextControl> {

	/** The {@link ControlProvider} for the {@link TranslatedI18nStructuredTextFieldControl}. */
	public static class Provider extends TranslatedI18nFieldControl.Provider {

		/**
		 * Create a {@link TranslatedI18nStructuredTextFieldControl.Provider}.
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
		protected Control createControl(CompositeField i18nField, ComponentChannel language, String style) {
			return new TranslatedI18nStructuredTextFieldControl((I18NStructuredTextField) i18nField, language, style);
		}

	}

	/**
	 * Creates a {@link TranslatedI18nStructuredTextFieldControl}.
	 * 
	 * @see TranslatedI18nFieldControl#TranslatedI18nFieldControl(CompositeField, ComponentChannel,
	 *      String) Parameter definitions.
	 */
	public TranslatedI18nStructuredTextFieldControl(I18NStructuredTextField i18nField, ComponentChannel language,
			String style) {
		super(i18nField, language, style);
	}

	@Override
	protected StructuredTextControl createInnerControl(FormField innerField, String style) {
		I18NStructuredTextField fieldModel = getFieldModel();
		List<String> featureConfig = fieldModel.getFeatureConfig();
		List<String> templateFiles = fieldModel.getTemplateFiles();
		String templates = fieldModel.getTemplates();
		if (CollectionUtil.isEmptyOrNull(featureConfig)) {
			return new StructuredTextControl(innerField, templateFiles, templates);
		} else {
			String editorConfig =
				StructuredTextConfigService.getInstance().getEditorConfig(featureConfig, templateFiles,
					templates);
			return new StructuredTextControl(innerField, editorConfig);
		}
	}

	@Override
	protected I18NStructuredTextField getFieldModel() {
		return (I18NStructuredTextField) super.getFieldModel();
	}

}
