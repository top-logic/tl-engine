/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.util.dbadmin;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.db.schema.setup.SchemaSetup;
import com.top_logic.basic.db.schema.setup.config.ApplicationTypes;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.layout.component.model.ModelProvider;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ModelProvider} loading the {@link SchemaConfiguration} of the application.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DBTablesModelProvider implements ModelProvider {

	/**
	 * Singleton {@link DBTablesModelProvider} instance.
	 */
	public static final DBTablesModelProvider INSTANCE = new DBTablesModelProvider();

	private DBTablesModelProvider() {
		// Singleton constructor.
	}

	@Override
	public Object getBusinessModel(LayoutComponent businessComponent) {
		return loadSchema();
	}

	static SchemaConfiguration loadSchema() {
		try {
			InstantiationContext context = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY;
			ApplicationTypes applicationTypes = ApplicationConfig.getInstance().getConfig(ApplicationTypes.class);
			String typeSystemName = PersistencyLayer.getKnowledgeBase().getConfiguration().getTypeSystem();
			SchemaSetup defaultTypeSystem = applicationTypes.getTypeSystem(typeSystemName);
			
			SchemaConfiguration schema = TypedConfiguration.copy(defaultTypeSystem.getConfig());
			SchemaSetup.resolveDeclarations(context, schema);
			return schema;
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(ex);
		}
	}

}
