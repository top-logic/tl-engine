/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.selection;

import static com.top_logic.layout.DisplayDimension.*;
import static com.top_logic.layout.DisplayUnit.*;

import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.DisplayValue;
import com.top_logic.layout.WindowScope;
import com.top_logic.layout.basic.ResourceText;
import com.top_logic.layout.form.I18NConstants;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.structure.DefaultDialogModel;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.DialogModel;
import com.top_logic.layout.structure.DialogWindowControl;
import com.top_logic.layout.structure.LayoutControl;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.layout.table.ConfigKey;

/**
 * The class {@link AbstractSelectDialog} is a abstract superclass for opening
 * some dialog for some {@link SelectField}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractSelectDialog {

	private final SelectField targetField;
	private DisplayValue title;
	private SelectDialogConfig _config;

	private final ConfigKey _configKey;

	/**
	 * Creates a {@link AbstractSelectDialog}.
	 * 
	 * @param targetField
	 *        See {@link #getTargetField()}.
	 * @param config
	 * 		  Configuration of the dialog.
	 */
	public AbstractSelectDialog(SelectField targetField, SelectDialogConfig config) {
		this.targetField = targetField;
		_config = config;
		this.title =
			new ResourceText(I18NConstants.POPUP_SELECT_TITLE__FIELD.fill(targetField.getLabel()));
		_configKey = targetField.getConfigKey();
	}
	
	/**
	 * The {@link SelectField} that is being edited.
	 */
	public final SelectField getTargetField() {
		return targetField;
	}

	/**
	 * The dialog title.
	 */
	protected DisplayValue getTitle() {
		return title;
	}

	/**
	 * Configuration of the SelectDialog
	 */
	public SelectDialogConfig getConfig() {
		return (_config);
	}

	/** ConfigKey for {@link PersonalConfiguration} */
	public ConfigKey getConfigKey() {
		return _configKey;
	}

	/**
	 * Getter for the {@link PersonalConfiguration}.
	 */
	protected PersonalConfiguration getPersonalConfiguration() {
		return PersonalConfiguration.getPersonalConfiguration();
	}

	/**
	 * Opens this dialog.
	 * 
	 * @param windowScope
	 *        The window in which to open the dialog.
	 * @return The {@link DialogModel} of the opened dialog.
	 */
	public DialogModel open(WindowScope windowScope) {
		DefaultDialogModel dialogModel = createDialogModel();
		DialogWindowControl dialogControl = getDialogControl(dialogModel);
		windowScope.openDialog(dialogControl);
		return dialogModel;
	}

	private DialogWindowControl getDialogControl(DefaultDialogModel dialogModel) {
		DialogWindowControl result = new DialogWindowControl(dialogModel);
		result.setChildControl(this.createContentView(result));
		return result;
	}

	private DefaultDialogModel createDialogModel() {
		DefaultLayoutData layoutData =
			new DefaultLayoutData(dim(getConfig().getWidth(), PIXEL), 100, dim(getConfig().getHeight(), PIXEL), 100,
				Scrolling.AUTO);
		DefaultDialogModel dialogModel =
			new DefaultDialogModel(layoutData, this.getTitle(), /* resizable */true, /* closable */true, null,
				_configKey);
		return dialogModel;
	}

	/**
	 * Creates the actual content of the given {@link DialogWindowControl}.
	 * 
	 * @return the content which will be set as content of the given
	 *         {@link DialogWindowControl}.
	 * 
	 */
	protected abstract LayoutControl createContentView(DialogWindowControl result);

}
