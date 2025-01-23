/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.instances.importer;

import java.util.Collections;
import java.util.Map;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.component.AbstractCreateCommandHandler;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLModel;
import com.top_logic.model.instance.importer.XMLInstanceImporter;
import com.top_logic.model.instance.importer.schema.ObjectsConf;
import com.top_logic.util.model.ModelService;

/**
 * 
 */
public class InstanceImportCommand extends AbstractCreateCommandHandler {

	/**
	 * Creates a {@link InstanceImportCommand}.
	 */
	public InstanceImportCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Object createObject(LayoutComponent component, Object createContext, FormContainer formContainer,
			Map<String, Object> arguments) {
		try {
			UploadForm form = (UploadForm) EditorFactory.getModel(formContainer);
			BinaryData data = form.getData();

			TLModel model = ModelService.getApplicationModel();

			XMLInstanceImporter importer = new XMLInstanceImporter(model, ModelService.getInstance().getFactory());

			InstantiationContext context = new DefaultInstantiationContext(InstanceImportCommand.class);
			Map<String, ConfigurationDescriptor> types =
				Collections.singletonMap("objects", TypedConfiguration.getConfigurationDescriptor(ObjectsConf.class));
			ObjectsConf objects = (ObjectsConf) ConfigurationReader.readContent(context, types, data);
			importer.importInstances(objects);
			return null;
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(ex);
		}
	}

}
