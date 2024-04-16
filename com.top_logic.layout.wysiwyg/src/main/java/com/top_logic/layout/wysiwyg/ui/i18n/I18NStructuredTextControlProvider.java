/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui.i18n;

import java.util.List;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.element.i18n.I18NField;
import com.top_logic.element.i18n.I18NStringTagProvider.I18NStringControlRenderer;
import com.top_logic.layout.CompositeControl;
import com.top_logic.layout.Control;
import com.top_logic.layout.basic.ControlRenderer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.ImmutablePropertyListener;
import com.top_logic.layout.form.control.AbstractCompositeControl;
import com.top_logic.layout.form.control.ErrorControl;
import com.top_logic.layout.form.control.OnVisibleControl;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.form.values.edit.editor.I18NTranslationUtil;
import com.top_logic.layout.wysiwyg.ui.StructuredTextControlProvider;
import com.top_logic.layout.wysiwyg.ui.i18n.I18NStructureTextTagProvider.StructuredTextFieldTranslator;

/**
 * {@link ControlProvider} creating a suitable {@link Control} for a
 * {@link I18NStructuredTextField}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class I18NStructuredTextControlProvider implements ControlProvider {

	/**
	 * Singleton {@link I18NStructuredTextControlProvider} instance.
	 */
	public static final I18NStructuredTextControlProvider INSTANCE = new I18NStructuredTextControlProvider();

	private I18NStructuredTextControlProvider() {
		// Singleton constructor.
	}

	@Override
	public Control createControl(Object model, String style) {
		if (model instanceof I18NField) {
			return internalCreateControl((I18NField<?, ?, ?>) model, style);
		}
		return DefaultFormFieldControlProvider.INSTANCE.createControl(model, style);
	}

	@Override
	public HTMLFragment createFragment(Object model, String style) {
		if (model instanceof I18NField) {
			return internalCreateControl((I18NField<?, ?, ?>) model, style);
		}
		return DefaultFormFieldControlProvider.INSTANCE.createFragment(model, style);
	}

	private Control internalCreateControl(I18NField<?, ?, ?> member, String style) {
		OnVisibleControl block = new OnVisibleControl(member);
		List<? extends FormField> languageFields = member.getLanguageFields();
		for (FormField field : languageFields) {
			block.addChild(StructuredTextControlProvider.INSTANCE.createControl(field, style));
			block.addChild(new ErrorControl(field, true));
			if (!I18NTranslationUtil.isSourceField(field)) {
				block.addChild(I18NTranslationUtil.getTranslateControl(field, languageFields,
					StructuredTextFieldTranslator.INSTANCE));
			}
		}
		if (member.get(I18NField.DISPLAY_ALL_LANGUAGES_IN_VIEW_MODE)) {
			block.setRenderer(editModeRenderer());
		} else {
			ImmutablePropertyListener immutableListener = immutableListener(block);
			member.addListener(FormField.IMMUTABLE_PROPERTY, immutableListener);
			immutableListener.handleImmutableChanged(member, !member.isImmutable(), member.isImmutable());
		}
		return block;
	}

	private ImmutablePropertyListener immutableListener(AbstractCompositeControl<?> composite) {
		return new ImmutablePropertyListener() {

			@Override
			public Bubble handleImmutableChanged(FormMember sender, Boolean oldValue, Boolean newValue) {
				ControlRenderer<CompositeControl> newRenderer;
				if (newValue) {
					// Display only the value for the current locale in view mode.
					newRenderer = I18NStructuedTextActiveLanguageControlRenderer.INSTANCE;
				} else {
					// Display each value in edit mode.
					newRenderer = editModeRenderer();
				}
				composite.setRenderer(newRenderer);
				return Bubble.BUBBLE;
			}
		};

	}

	I18NStringControlRenderer editModeRenderer() {
		return I18NStringControlRenderer.ABOVE_INSTANCE;
	}

}

