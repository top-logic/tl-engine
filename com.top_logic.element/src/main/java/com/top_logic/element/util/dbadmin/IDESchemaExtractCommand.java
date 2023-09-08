/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.util.dbadmin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
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
import com.top_logic.basic.StringServices;
import com.top_logic.basic.XMLProperties;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.equal.ConfigEquality;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.db.schema.io.MORepositoryBuilder;
import com.top_logic.basic.db.schema.setup.config.ApplicationTypes;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.xml.XMLPrettyPrinter;
import com.top_logic.dob.schema.config.AlternativeConfig;
import com.top_logic.dob.schema.config.AssociationConfig;
import com.top_logic.dob.schema.config.MetaObjectConfig;
import com.top_logic.dob.schema.config.MetaObjectName;
import com.top_logic.dob.schema.config.MetaObjectsConfig;
import com.top_logic.dsa.DataAccessProxy;
import com.top_logic.dsa.DatabaseAccessException;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.TypeSystemConfiguration;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
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
public class IDESchemaExtractCommand extends AbstractCommandHandler {

	private static final Property<Boolean> NOTICE_GIVEN =
		TypedAnnotatable.property(Boolean.class, "noticeGiven", Boolean.FALSE);

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
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent aComponent, Object model,
			Map<String, Object> arguments) {
		
		String resumed = "resumed";
		if (arguments.get(resumed) == null && !context.getSubSessionContext().get(NOTICE_GIVEN)) {
			HandlerResult result = HandlerResult.suspended();
			Command resume = result.resumeContinuation(Collections.singletonMap("resumed", Boolean.TRUE));
			MessageBox.confirm(context.getWindowScope(), new DefaultLayoutData(
				DisplayDimension.dim(400, DisplayUnit.PIXEL), 100,
				DisplayDimension.dim(250, DisplayUnit.PIXEL), 100, Scrolling.AUTO), true,
				Fragments.message(I18NConstants.UPDATE_SCHEMA_TITLE),
				Fragments.message(I18NConstants.UPDATE_SCHEMA_NOTICE),
				MessageBox.button(ButtonType.NO), MessageBox.button(ButtonType.YES, resume));
			return result;
		}

		context.getSubSessionContext().set(NOTICE_GIVEN, Boolean.TRUE);

		return extractSchema(model);
	}

	private HandlerResult extractSchema(Object model) {
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

	private void writeTypes(String resource, List<MetaObjectName> types) throws XMLStreamException, IOException {
		DataAccessProxy proxy = StringServices.isEmpty(resource) ? null : new DataAccessProxy(resource);
		if (proxy != null && proxy.exists()) {
			MetaObjectsConfig config = TypedConfiguration.newConfigItem(MetaObjectsConfig.class);
			Collections.sort(types, (t1, t2) -> {
				int kindOrder = Integer.compare(kind(t1), kind(t2));
				if (kindOrder != 0) {
					return kindOrder;
				}

				return t1.getObjectName().compareTo(t2.getObjectName());
			});
			for (MetaObjectName type : types) {
				config.getTypes().put(type.getObjectName(), type);
			}

			try (OutputStream out = proxy.getEntryOutputStream()) {
				writeSchema(out, config);
			}
		} else {
			File autoconfFolder = XMLProperties.Setting.resolveAutoconfFolder();
			FileUtilities.enforceDirectory(autoconfFolder);

			for (MetaObjectName type : types) {
				MetaObjectsConfig config = TypedConfiguration.newConfigItem(MetaObjectsConfig.class);
				config.getTypes().put(type.getObjectName(), type);

				String configName = type.getObjectName() + ".table.config.xml";
				try (OutputStream out = new FileOutputStream(new File(autoconfFolder, configName))) {
					writeConfig(out, createConfigFragment(config));
				}
			}
		}
	}


	private int kind(MetaObjectName t) {
		if (t instanceof AlternativeConfig) {
			// At the beginning.
			return 0;
		}
		if (t instanceof AssociationConfig) {
			// At the end.
			return 4;
		}
		if (t instanceof MetaObjectConfig) {
			// Templates before tables.
			return ((MetaObjectConfig) t).isAbstract() ? 1 : 2;
		}
		return 3;
	}

	private static ApplicationConfig.Config createConfigFragment(MetaObjectsConfig config) {
		ApplicationConfig.Config appCofig = TypedConfiguration.newConfigItem(ApplicationConfig.Config.class);
		ApplicationTypes typesConfig = TypedConfiguration.newConfigItem(ApplicationTypes.class);
		appCofig.getConfigs().put(ApplicationTypes.class, typesConfig);
		TypeSystemConfiguration typeSystemConfig =
			TypedConfiguration.newConfigItem(TypeSystemConfiguration.class);
		typeSystemConfig.setName(PersistencyLayer.getKnowledgeBase().getConfiguration().getTypeSystem());
		typeSystemConfig.setMetaObjects(config);

		typesConfig.getTypeSystems().add(TypedConfigUtil.createInstance(typeSystemConfig));
		return appCofig;
	}

	private static void writeSchema(OutputStream out, MetaObjectsConfig config) throws XMLStreamException, IOException {
		writePretty(out, MORepositoryBuilder.ROOT_TAG, MetaObjectsConfig.class, config);
	}

	private static void writeConfig(OutputStream out, ApplicationConfig.Config config)
			throws XMLStreamException, IOException {
		writePretty(out, ApplicationConfig.ROOT_TAG, ApplicationConfig.Config.class, config);
	}

	private static <T extends ConfigurationItem> void writePretty(OutputStream out, String rootTag, Class<T> staticType,
			T config) throws XMLStreamException, IOException, UnsupportedEncodingException {
		TypedConfiguration.minimize(config);
		try (OutputStreamWriter writer = new OutputStreamWriter(out, "utf-8")) {
			StringWriter buffer = new StringWriter();
			ConfigurationWriter w = new ConfigurationWriter(buffer);
			w.write(rootTag, staticType, config);

			writer.write(XMLPrettyPrinter.prettyPrint(buffer.toString()));
		}
	}

}
