/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.i18n;

import static com.top_logic.layout.DisplayDimension.*;
import static com.top_logic.layout.DisplayUnit.*;
import static com.top_logic.layout.form.template.model.Templates.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.util.ResKey;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.CommandModelFactory;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.control.PopupEditControl;
import com.top_logic.layout.form.control.TextInputControl;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.model.MemberStyle;
import com.top_logic.layout.form.template.model.Templates;
import com.top_logic.layout.form.values.MultiLineText;
import com.top_logic.layout.form.values.edit.editor.InternationalizationEditor;
import com.top_logic.layout.form.values.edit.editor.TranslateButtonCP;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.DialogModel;
import com.top_logic.layout.structure.LayoutData;
import com.top_logic.layout.structure.MediaQueryControl;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.model.annotate.LabelPosition;
import com.top_logic.model.form.definition.LabelPlacement;

/**
 * {@link PopupEditControl} for I18n attributes.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class I18NStringTextPopupControl extends PopupEditControl {

	private static final String ADDITIONAL_RESOURCE_CSS = "additional-resource";

	private static final String ADDITIONAL_RESOURCE_SPACING_CSS = "with-spacing";

	private int _rows;

	private int _columns;

	/**
	 * Creates a new {@link I18NStringTextPopupControl}.
	 */
	public I18NStringTextPopupControl(Settings settings, FormField model, int rows, int columns) {
		super(settings, model);
		_rows = rows;
		_columns = columns;
	}

	@Override
	public void valueChanged(FormField field, Object oldValue, Object newValue) {
		super.valueChanged(field, oldValue, newValue);
		requestRepaint();
	}

	@Override
	protected FormField createEditField(FormField originalField) {
		Map<Locale, StringField> baseFields = Collections.emptyMap();
		Map<String, ResKey> derivedKeys = Collections.emptyMap();
		if (originalField instanceof I18NStringField) {
			I18NStringField i18nField = (I18NStringField) originalField;
			baseFields = i18nField.getLanguageFieldsByLocale();
			derivedKeys = i18nField.derivedResources();
		}
		String name = originalField.getName() + POPUP_SUFFIX;
		boolean mandatory = originalField.isMandatory();
		boolean immutable = originalField.isImmutable();
		I18NStringField copy = I18NStringField.newI18NStringField(name, mandatory, immutable);
		copy.enableDerivedResources(derivedKeys);
		copyConstraints(copy, baseFields);
		return copy;
	}

	private void copyConstraints(I18NField<?, ?, ?> copy, Map<Locale, ? extends FormField> baseFields) {
		for (Entry<Locale, ? extends FormField> field : copy.getLanguageFieldsByLocale().entrySet()) {
			FormField baseField = baseFields.get(field.getKey());
			if (baseField != null) {
				baseField.getConstraints().forEach(field.getValue()::addConstraint);
				baseField.getWarningConstraints().forEach(field.getValue()::addWarningConstraint);
			}
		}
	}

	@Override
	protected HTMLFragment createEditFragment(FormField editField) {
		I18NStringField i18nField = (I18NStringField) editField;
		Set<String> derivedKeys = i18nField.derivedResources().keySet();
		TranslateButtonCP cp = new TranslateButtonCP(i18nField.getLanguageFields(), derivedKeys);
		List<HTMLTemplateFragment> contentTemplates = new ArrayList<>();
		ControlProvider languageFieldCP = languageFieldCP();
		List<StringField> languageFields = i18nField.getLanguageFields();
		for (int i = 0, numberLanguageFields = languageFields.size(); i < numberLanguageFields; i++) {
			StringField languageField = languageFields.get(i);
			Locale lang = languageField.get(I18NStringField.LANGUAGE);
			HTMLTemplateFragment self;
			if (languageFieldCP != null) {
				self = Templates.self(MemberStyle.NONE, languageFieldCP);
			} else {
				self = Templates.self();
			}
			contentTemplates.add(member(languageField,
				descriptionBox(
					fragment(labelWithColon(), direct(cp), error()),
					self, LabelPosition.DEFAULT, LabelPlacement.ABOVE)));
			boolean withSpacing = i < (numberLanguageFields - 1);
			for (Iterator<String> suffixIt = derivedKeys.iterator(); suffixIt.hasNext();) {
				String suffix = suffixIt.next();
				String additionalResourceCss = ADDITIONAL_RESOURCE_CSS;
				if (withSpacing && !suffixIt.hasNext()) {
					additionalResourceCss += " " + ADDITIONAL_RESOURCE_SPACING_CSS;
				}
				String suffixMemberName = I18NStringField.suffixFieldName(lang, suffix);
				contentTemplates.add(div(css(additionalResourceCss), fieldBox(suffixMemberName)));
			}
		}
		template(i18nField, div(contentTemplates));
		return new MediaQueryControl(i18nField.getControlProvider().createFragment(i18nField));
	}

	private ControlProvider languageFieldCP() {
		ControlProvider languageFieldCP;
		if (_rows > 0) {
			languageFieldCP = MultiLineText.newInstance(_rows, _columns);
		} else if (_columns != TextInputControl.NO_COLUMNS) {
			languageFieldCP = textInputWithColumns(_columns);
		} else {
			languageFieldCP = null;
		}
		return languageFieldCP;
	}

	private ControlProvider textInputWithColumns(int columns) {
		return new ControlProvider() {

			@Override
			public Control createControl(Object model, String style) {
				TextInputControl textInput = new TextInputControl((FormField) model);
				textInput.setColumns(columns);
				return textInput;
			}

		};
	}

	@Override
	protected LayoutData getDialogLayout(FormField editField) {
		I18NStringField i18nField = (I18NStringField) editField;
		int numberFields = i18nField.getLanguageFields().size();
		float height = 30 * numberFields; // Labels
		if (_rows > 0) {
			int inputHeight = (15 * _rows) + 20;
			height += inputHeight * numberFields; // Input fields
		} else {
			int inputHeight = 30;
			height += inputHeight * numberFields; // Input fields
		}
		DisplayDimension titleBarHeight =
			ThemeFactory.getTheme().getValue(com.top_logic.layout.structure.Icons.DIALOG_TITLE_HEIGHT);
		DisplayDimension buttonBarHeight =
			ThemeFactory.getTheme().getValue(com.top_logic.layout.Icons.BUTTON_COMP_HEIGHT);
		height += titleBarHeight.getValue() + buttonBarHeight.getValue();

		return new DefaultLayoutData(dim(720, PIXEL), 100, dim(height, PIXEL), 100, Scrolling.AUTO);
	}

	@Override
	protected DialogModel createDialogModel(FormField editField) {
		DialogModel dialogModel = super.createDialogModel(editField);
		I18NStringField i18nField = (I18NStringField) editField;
		if (!i18nField.derivedResources().isEmpty()) {
			dialogModel.getToolbar()
				.defineGroup("basic-edit")
				.addButton(displayDerivedCommand(i18nField));
		}
		return dialogModel;
	}

	private CommandModel displayDerivedCommand(I18NStringField i18nField) {
		List<StringField> derivedMembers = new ArrayList<>();
		Set<String> derivedKeys = i18nField.derivedResources().keySet();
		for (StringField languageField : i18nField.getLanguageFields()) {
			Locale lang = languageField.get(I18NStringField.LANGUAGE);
			for (String suffix : derivedKeys) {
				String suffixMemberName = I18NStringField.suffixFieldName(lang, suffix);
				derivedMembers.add((StringField) i18nField.getMember(suffixMemberName));
			}
		}
		return InternationalizationEditor.displayDerivedCommand(derivedMembers,
			CommandModelFactory::commandModel, getConfigKey(i18nField));
	}

}
