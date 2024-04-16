/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.i18n;

import java.util.List;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.listener.EventType.Bubble;
import com.top_logic.basic.translation.TranslationService;
import com.top_logic.element.i18n.I18NStringTagProvider.I18NStringActiveLanguageControlRenderer;
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
import com.top_logic.layout.form.control.TextInputControl;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.form.values.edit.editor.I18NTranslationUtil;
import com.top_logic.layout.form.values.edit.editor.I18NTranslationUtil.StringValuedFieldTranslator;

/**
 * {@link ControlProvider} creating a a suitable control for an {@link I18NStringField}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class I18NStringControlProvider implements ControlProvider {

	private boolean _multiline;

	private int _rows;

	private int _columns;

	/**
	 * This constructor creates a new {@link I18NStringControlProvider}.
	 * 
	 * @param multiline
	 *        Whether the field should be displayed in multi-line mode.
	 * @param rows
	 *        If <code>multiline</code>, the number of rows to display.
	 * @param columns
	 *        Number of columns to display.
	 * 
	 * @see TextInputControl#NO_COLUMNS
	 */
	public I18NStringControlProvider(boolean multiline, int rows, int columns) {
		_multiline = multiline;
		_rows = rows;
		_columns = columns;
	}

	@Override
	public HTMLFragment createFragment(Object model, String style) {
		if (model instanceof I18NStringField) {
			I18NStringField member = (I18NStringField) model;
			return createControl(member);
		}
		return DefaultFormFieldControlProvider.INSTANCE.createFragment(model, style);
	}

	@Override
	public Control createControl(Object model, String style) {
		if (model instanceof I18NStringField) {
			I18NStringField member = (I18NStringField) model;
			return createControl(member);
		}
		return DefaultFormFieldControlProvider.INSTANCE.createControl(model, style);
	}

	private Control createControl(I18NStringField member) {
		OnVisibleControl block = new OnVisibleControl(member);
		List<StringField> languageFields = member.getLanguageFields();
		for (FormField field : languageFields) {
			TextInputControl control = new TextInputControl(field);
			control.setMultiLine(_multiline);
			control.setColumns(_columns);
			if (_multiline) {
				control.setRows(_rows);
			}
			block.addChild(control);
			block.addChild(new ErrorControl(field, true));
			if (TranslationService.isActive()) {
				if (!I18NTranslationUtil.isSourceField(field)) {
					block.addChild(I18NTranslationUtil.getTranslateControl(field, languageFields,
						StringValuedFieldTranslator.INSTANCE));
				}
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

	private <I extends AbstractCompositeControl<?>> ImmutablePropertyListener immutableListener(
			AbstractCompositeControl<I> composite) {
		return new ImmutablePropertyListener() {

			@Override
			public Bubble handleImmutableChanged(FormMember sender, Boolean oldValue, Boolean newValue) {
				ControlRenderer<CompositeControl> newRenderer;
				if (newValue) {
					// Display only the value for the current locale in view mode.
					newRenderer = I18NStringActiveLanguageControlRenderer.INSTANCE;
				} else {
					// Display each value in edit mode.
					newRenderer = editModeRenderer();
				}
				composite.setRenderer(newRenderer);
				return Bubble.BUBBLE;
			}

		};
	}

	ControlRenderer<CompositeControl> editModeRenderer() {
		ControlRenderer<CompositeControl> newRenderer;
		if (_multiline) {
			newRenderer = I18NStringControlRenderer.ABOVE_INSTANCE;
		} else {
			newRenderer = I18NStringControlRenderer.INSTANCE;
		}
		return newRenderer;
	}
}

