/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.gui;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.DataField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;

/**
 * A {@link FormComponent} for selecting the script that should be edited in the ScriptingGui.
 * <p>
 * Model: null. This component has no model.
 * </p>
 * <p>
 * Selection: The {@link ScriptContainer} of the last opened scripted test. It has either been
 * uploaded or is a file on this server that has been selected.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class ScriptUploadComponent extends FormComponent implements Selectable {

	/**
	 * Technical name of the {@link FormField} for uploading the script file.
	 */
	public static final String FIELD_NAME_FILE_UPLOAD = "fileUpload";

	/**
	 * Configuration options for {@link ScriptUploadComponent}.
	 */
	public interface Config extends FormComponent.Config {
		// Sum interface.
	}

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link ScriptUploadComponent}.
	 * <p>
	 * <b>Don't call directly.</b> Use
	 * {@link InstantiationContext#getInstance(PolymorphicConfiguration)} instead.
	 * </p>
	 * 
	 * @param context
	 *        For error reporting and instantiation of dependent configured objects.
	 * @param config
	 *        The configuration for the new instance.
	 */
	@CalledByReflection
	public ScriptUploadComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	protected void becomingInvisible() {
		super.becomingInvisible();
		if (hasFormContext()) {
			getFormContext().reset();
		}
	}

	@Override
	protected boolean supportsInternalModel(Object object) {
		// This component has no model.
		return object == null;
	}

	@Override
	public FormContext createFormContext() {
		FormContext formContext = new FormContext(this);
		formContext.addMember(createFileUploadField());
		return formContext;
	}

	private DataField createFileUploadField() {
		return FormFactory.newDataField(FIELD_NAME_FILE_UPLOAD);
	}

	/** The {@link DataField} which contains the uploaded script. */
	public DataField getFileUploadField() {
		return (DataField) getFormContext().getField(FIELD_NAME_FILE_UPLOAD);
	}

}
