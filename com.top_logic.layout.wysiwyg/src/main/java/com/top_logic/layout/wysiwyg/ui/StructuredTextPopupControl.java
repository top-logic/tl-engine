/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.base.services.simpleajax.HTMLFragmentProvider;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.element.i18n.I18NStringTextPopupControl;
import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.PopupEditControl;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.template.ControlProvider;

/**
 * Popup for the WYSIWYG editor which is used by a button in grids and tables.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class StructuredTextPopupControl extends PopupEditControl {

	/**
	 * Creates a new {@link StructuredTextPopupControl}.
	 */
	public StructuredTextPopupControl(Settings settings, FormField model) {
		super(settings, model);
	}

	@Override
	protected FormField createEditField(FormField originalField) {
		return FormFactory.newHiddenField(originalField.getName() + POPUP_SUFFIX);
	}

	@Override
	protected HTMLFragment createEditFragment(FormField editField) {
		HTMLFragmentProvider cp = editField.getControlProvider();
		if (cp != null) {
			return cp.createFragment(editField);
		}
		return new StructuredTextControl(editField);
	}

	/**
	 * {@link ControlProvider} creating {@link I18NStringTextPopupControl}s.
	 */
	public static class CP extends PopupEditControl.CP {

		/**
		 * Configuration options for {@link StructuredTextPopupControl.CP}.
		 */
		public interface Config<I extends StructuredTextPopupControl.CP> extends PopupEditControl.CP.Config<I> {
			// Pure marker interface.
		}

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
			return new StructuredTextPopupControl(getSettings(), (FormField) member);
		}

	}

}
