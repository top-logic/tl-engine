/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui.i18n;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.i18n.I18NField;
import com.top_logic.element.i18n.I18NStringTextPopupControl;
import com.top_logic.layout.Control;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.PopupEditControl;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.structure.LayoutData;

/**
 * {@link PopupEditControl} for I18n attributes.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class I18NStructuredTextPopupControl extends I18NStringTextPopupControl {

	/**
	 * Creates a new {@link I18NStructuredTextPopupControl}.
	 */
	public I18NStructuredTextPopupControl(Settings settings, FormField model) {
		super(settings, model);
	}

	@Override
	protected FormField createEditField(FormField originalField) {
		String name = originalField.getName() + POPUP_SUFFIX;
		boolean mandatory = originalField.isMandatory();
		boolean immutable = originalField.isImmutable();
		Constraint constraint = getConstraint(originalField);
		List<String> featureConfig = getFeatureConfig(originalField);
		List<String> templateFiles = getTemplateFiles(originalField);
		String templates = getTemplates(originalField);
		return I18NStructuredTextField.new18NStructuredTextField(name, mandatory, immutable, constraint, featureConfig,
			templateFiles, templates);
	}

	private Constraint getConstraint(FormField originalField) {
		if (originalField instanceof I18NField) {
			return ((I18NField<?, ?, ?>) originalField).getConstraint();
		}
		return null;
	}

	private List<String> getFeatureConfig(FormField originalField) {
		if (originalField instanceof I18NStructuredTextField) {
			return ((I18NStructuredTextField) originalField).getFeatureConfig();
		}
		return null;
	}

	private List<String> getTemplateFiles(FormField originalField) {
		if (originalField instanceof I18NStructuredTextField) {
			return ((I18NStructuredTextField) originalField).getTemplateFiles();
		}
		return null;
	}

	private String getTemplates(FormField originalField) {
		if (originalField instanceof I18NStructuredTextField) {
			return ((I18NStructuredTextField) originalField).getTemplates();
		}
		return null;
	}

	@Override
	protected Control createEditControl(FormField editField) {
		return I18NStructuredTextControlProvider.INSTANCE.createControl(editField);
	}

	@Override
	protected LayoutData getDialogLayout(FormField editField) {
		return getConfiguredDialogLayout();
	}

	/**
	 * {@link ControlProvider} creating {@link I18NStringTextPopupControl}s.
	 */
	public static class CP extends I18NStringTextPopupControl.CP {

		/**
		 * Creates a {@link CP} from configuration.
		 *
		 * @param context
		 *        The context for instantiating sub configurations.
		 * @param config
		 *        The configuration.
		 */
		@CalledByReflection
		public CP(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		protected Control createInput(FormMember member) {
			return new I18NStructuredTextPopupControl(getSettings(), (FormField) member);
		}

	}

}
