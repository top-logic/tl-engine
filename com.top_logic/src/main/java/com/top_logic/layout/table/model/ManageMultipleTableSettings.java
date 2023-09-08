/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.model;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.StringServices;
import com.top_logic.html.template.TagTemplate;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.model.AbstractExecutabilityModel;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.ExecutabilityModel;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.SelectFieldUtils;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.layout.form.template.ButtonControlProvider;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.form.template.model.internal.TemplateControl;
import com.top_logic.layout.messagebox.AbstractFormDialog;
import com.top_logic.layout.messagebox.AbstractFormDialogBase;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.MessageBoxContentView;
import com.top_logic.layout.structure.DefaultDialogModel;
import com.top_logic.layout.structure.DialogModel;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link MultipleSettingsCommand} to manage multiple settings.
 * 
 * @see LoadNamedSetting
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ManageMultipleTableSettings extends MultipleSettingsCommand {

	private static final String DELETE_INFO = "deleteSettingInfo";

	private static final String SAVE_SUFFIX = "addSettingInfo";

	private static final String MESSAGE_SUFFIX = "message";

	private static final String MULTIPLE_SETTING_PREFIX = "multipleSetting.";

	private static final String SETTING_NAME_FIELD = "newSettingName";

	private static final String ADD_SETTING_COMMAND = "addSetting";

	private static final String EXISTING_NAMES_FIELD = "existingSettings";

	private static final String DELETE_SETTING_NAMES_COMMAND = "deleteSetting";

	private static final TagTemplate TEMPLATE = div(
		div(style("margin: 5px;"),
			resource(I18NConstants.EDIT_TABLE_SETTINGS_DIALOG.key(MESSAGE_SUFFIX))),
		fieldsetBoxWrap(
			resource(I18NConstants.EDIT_TABLE_SETTINGS_DIALOG.key(SAVE_SUFFIX)),
			div(
				fieldBox(SETTING_NAME_FIELD),
				member(ADD_SETTING_COMMAND)),
			ConfigKey.none()),
		fieldsetBoxWrap(
			resource(I18NConstants.EDIT_TABLE_SETTINGS_DIALOG.key(DELETE_INFO)),
			div(
				fieldBox(EXISTING_NAMES_FIELD),
				member(DELETE_SETTING_NAMES_COMMAND)),
			ConfigKey.none()));

	/**
	 * {@link AbstractFormDialog} to let the user manage multiple table settings.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	protected class ManageMultipleTableSettingsDialog extends AbstractFormDialogBase {
		private static final String NOT_EXECUTABLE_NO_SETTING_SELECTED = "deleteSettingNoSelection";

		private static final String NOT_EXECUTABLE_NO_NAME_GIVEN = "addSettingNoName";

		Map<String, List<String>> _knownKeys;


		/**
		 * Creates a new {@link ManageMultipleTableSettingsDialog}.
		 */
		public ManageMultipleTableSettingsDialog(DialogModel dialogModel) {
			super(dialogModel);
			Map<String, List<String>> storedConfigKeys = storedConfigKeys();
			if (storedConfigKeys == null) {
				_knownKeys = new HashMap<>();
				_knownKeys.put(KEYS_KEY, new ArrayList<>());
				_knownKeys.put(NAMES_KEY, new ArrayList<>());
			} else {
				_knownKeys = storedConfigKeys;
				assert _knownKeys.get(KEYS_KEY) != null;
				assert _knownKeys.get(NAMES_KEY) != null;
			}
		}

		@Override
		protected HTMLFragment createView() {
			ControlProvider cp = DefaultFormFieldControlProvider.INSTANCE;
			Control content = new TemplateControl(getFormContext(), cp, createTemplate());
			return new MessageBoxContentView(content);
		}

		private TagTemplate createTemplate() {
			return TEMPLATE;
		}

		@Override
		protected void fillButtons(List<CommandModel> buttons) {
			addClose(buttons, ButtonType.CLOSE);
		}

		String newTableSettingKey() {
			return MULTIPLE_SETTING_PREFIX + StringServices.randomUUID();
		}

		@Override
		protected ResPrefix getResourcePrefix() {
			return I18NConstants.EDIT_TABLE_SETTINGS_DIALOG;
		}

		@Override
		protected void fillFormContext(FormContext fc) {
			StringField settingName = FormFactory.newStringField(SETTING_NAME_FIELD);
			fc.addMember(settingName);
			
			List<String> keys = _knownKeys.get(KEYS_KEY);
			List<String> names = _knownKeys.get(NAMES_KEY);
			SelectField existingSettings = FormFactory.newSelectField(EXISTING_NAMES_FIELD, keys);
			SelectFieldUtils.setOptionLabelProvider(existingSettings, key -> names.get(keys.indexOf(key)));
			fc.addMember(existingSettings);
			
			ExecutabilityModel saveSettingExec = new AbstractExecutabilityModel() {
				
				@Override
				protected ExecutableState calculateExecutability() {
					if (settingName.hasValue() && !settingName.getAsString().isEmpty()) {
						return ExecutableState.EXECUTABLE;
					}
					return ExecutableState.createDisabledState(
						I18NConstants.EDIT_TABLE_SETTINGS_DIALOG.key(NOT_EXECUTABLE_NO_NAME_GIVEN));
				}
			};
			settingName.addValueListener(new ValueListener() {

				@Override
				public void valueChanged(FormField field, Object oldValue, Object newValue) {
					/* Update executability when a setting name is given */
					saveSettingExec.updateExecutabilityState();
				}
			});
			
			CommandField addSettingsCommand = new CommandField(ADD_SETTING_COMMAND, saveSettingExec) {
				@Override
				public HandlerResult executeCommand(DisplayContext context) {
					String name = settingName.getAsString();
					int nameIndex = names.indexOf(name);
					String configKeyName;
					if (nameIndex == -1) {
						// new setting name
						configKeyName = newTableSettingKey();
						keys.add(configKeyName);
						names.add(name);

						ManageMultipleTableSettings.this.storeConfigKeys(_knownKeys);

						/* Update list of existing settings. */
						/* Copy list to ensure that the same java list instance is not re-used as
						 * option list. This may lead to strange effects. */
						existingSettings.setOptions(new ArrayList<>(keys));
					} else {
						// override existing setting
						configKeyName = keys.get(nameIndex);
					}
					_configSaver.accept(ConfigKey.named(configKeyName));
					// Clear input field.
					settingName.setValue(null);
					return HandlerResult.DEFAULT_RESULT;
				}
			};
			fc.addMember(addSettingsCommand);
			addSettingsCommand.setControlProvider(ButtonControlProvider.INSTANCE);

			ExecutabilityModel delSettingsExec = new AbstractExecutabilityModel() {

				@Override
				protected ExecutableState calculateExecutability() {
					if (existingSettings.hasValue() && existingSettings.getSingleSelection() != null) {
						return ExecutableState.EXECUTABLE;
					}
					return ExecutableState
						.createDisabledState(
							I18NConstants.EDIT_TABLE_SETTINGS_DIALOG.key(NOT_EXECUTABLE_NO_SETTING_SELECTED));
				}
			};
			existingSettings.addValueListener(new ValueListener() {

				@Override
				public void valueChanged(FormField field, Object oldValue, Object newValue) {
					/* Update executability when a setting is selected */
					delSettingsExec.updateExecutabilityState();
				}
			});
			CommandField deleteCommand = new CommandField(DELETE_SETTING_NAMES_COMMAND, delSettingsExec) {

				@Override
				public HandlerResult executeCommand(DisplayContext context) {
					String selectedSetting = (String) existingSettings.getSingleSelection();
					int removedSettingIndex = keys.indexOf(selectedSetting);
					keys.remove(removedSettingIndex);
					names.remove(removedSettingIndex);

					Map<String, List<String>> pcStorageValue;
					if (keys.isEmpty()) {
						pcStorageValue = null;
					} else {
						pcStorageValue = _knownKeys;
					}

					_configRemover.accept(ConfigKey.named(selectedSetting));
					ManageMultipleTableSettings.this.storeConfigKeys(pcStorageValue);

					/* Copy list to ensure that the same java list instance is not re-used as option
					 * list. This may lead to strange effects. */
					existingSettings.setOptions(new ArrayList<>(keys));
					return HandlerResult.DEFAULT_RESULT;
				}
			};
			deleteCommand.setControlProvider(ButtonControlProvider.INSTANCE);
			fc.addMember(deleteCommand);

		}

	}

	final Consumer<ConfigKey> _configSaver;

	final Consumer<ConfigKey> _configRemover;

	/**
	 * Creates a new {@link ManageMultipleTableSettings}.
	 */
	public ManageMultipleTableSettings(ConfigKey configKey, Consumer<ConfigKey> configSaver,
			Consumer<ConfigKey> configRemover) {
		super(configKey);
		_configSaver = configSaver;
		_configRemover = configRemover;
	}

	@Override
	public HandlerResult executeCommand(DisplayContext context) {
		ManageMultipleTableSettingsDialog dialog =
			new ManageMultipleTableSettingsDialog(DefaultDialogModel.dialogModel(I18NConstants.EDIT_TABLE_SETTINGS_DIALOG, DisplayDimension.px(480),
				DisplayDimension.px(430)));
		return dialog.open(context);
	}

}

