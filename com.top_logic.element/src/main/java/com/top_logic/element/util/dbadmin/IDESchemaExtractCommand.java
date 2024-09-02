/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.util.dbadmin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.equal.ConfigEquality;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.dob.schema.config.MetaObjectName;
import com.top_logic.dsa.DatabaseAccessException;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link CommandHandler} that writes back the schema configuration to the file system.
 * 
 * <p>
 * During next boot, the changes will be applied (if only changes are performed that support
 * automatic migration).
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class IDESchemaExtractCommand extends DBSchemaChangingCommand {

	/**
	 * Creates a {@link IDESchemaExtractCommand} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public IDESchemaExtractCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected HandlerResult changeSchema(Object model) {
		SchemaConfiguration oldSchema = DBTablesModelProvider.loadSchema();
		SchemaConfiguration newSchema = (SchemaConfiguration) model;

		List<MetaObjectName> changes = new ArrayList<>();
		Set<String> changedResources = new HashSet<>();
		Map<String, List<MetaObjectName>> typesByLocation = new HashMap<>();
		for (MetaObjectName type : newSchema.getMetaObjects().getTypes().values()) {
			String resource = type.location().getResource();
			List<MetaObjectName> types = typesByLocation.get(resource);
			if (types == null) {
				types = new ArrayList<>();
				typesByLocation.put(resource, types);
			}
			types.add(type);
			
			MetaObjectName oldType = oldSchema.getMetaObjects().getTypes().get(type.getObjectName());
			
			if (!ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(type, oldType)) {
				changes.add(type);
				changedResources.add(resource);
			}
		}

		for (MetaObjectName type : oldSchema.getMetaObjects().getTypes().values()) {
			if (newSchema.getMetaObjects().getTypes().get(type.getObjectName()) == null) {
				// Type was deleted.
				String resource = type.location().getResource();
				changedResources.add(resource);
			}
		}

		for (String resource : changedResources) {
			// Note: If all types of a resource are deleted, no map entry is found.
			List<MetaObjectName> types = typesByLocation.getOrDefault(resource, Collections.emptyList());
			try {
				writeTypes(resource, types);
			} catch (IOException | DatabaseAccessException | XMLStreamException ex) {
				Logger.error("Exporting schema to '" + resource + "' failed.", ex, IDESchemaExtractCommand.class);
			}
		}

		return HandlerResult.DEFAULT_RESULT;
	}

}
