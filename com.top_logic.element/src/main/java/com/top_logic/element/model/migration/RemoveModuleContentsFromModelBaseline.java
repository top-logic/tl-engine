/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.db.schema.properties.DBProperties;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.element.config.ModuleConfig;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.knowledge.service.migration.MigrationProcessor;

/**
 * {@link MigrationProcessor} removing the contents of the legacy modules form the model baseline.
 * 
 * <p>
 * This is useful, if e.g. the module references its table types. The the remove cannot be
 * automatically migrated, since the the baseline can no longer be loaded. By removing the module
 * contents in the baseline, the baseline can again be loaded and the removal of the module is
 * triggered successfully.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class RemoveModuleContentsFromModelBaseline extends
		AbstractConfiguredInstance<RemoveModuleContentsFromModelBaseline.Config<?>> implements MigrationProcessor {

	/**
	 * Configuration options for {@link RemoveModuleContentsFromModelBaseline}.
	 */
	public interface Config<I extends RemoveModuleContentsFromModelBaseline> extends PolymorphicConfiguration<I> {

		/**
		 * Modules to drop from the baseline.
		 */
		@ListBinding(attribute = "name")
		List<String> getModules();

	}

	/**
	 * Creates a {@link RemoveModuleContentsFromModelBaseline} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public RemoveModuleContentsFromModelBaseline(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public void doMigration(Log log, PooledConnection connection) {
		try {
			String xml = DBProperties.getProperty(connection, DBProperties.GLOBAL_PROPERTY,
				DynamicModelService.APPLICATION_MODEL_PROPERTY);
			Document document = DOMUtil.parse(xml);
			NodeList modules = document.getElementsByTagName(ModuleConfig.TAG_NAME);
			List<String> found = new ArrayList<>();
			Set<String> modulesToRemove = new HashSet<>(getConfig().getModules());
			for (int n = 0, cnt = modules.getLength(); n < cnt; n++) {
				Element module = (Element) modules.item(n);
				String name = module.getAttribute(ModuleConfig.NAME);
				if (modulesToRemove.contains(name)) {
					found.add(name);
					for (Node child : DOMUtil.children(module)) {
						module.removeChild(child);
					}
				}
			}
			if (!found.isEmpty()) {
				String update = DOMUtil.toString(document);
				DBProperties.setProperty(connection, DBProperties.GLOBAL_PROPERTY,
					DynamicModelService.APPLICATION_MODEL_PROPERTY, update);

				log.info("Fixed model baseline by removing contents of modules " + found + ".");
			}
		} catch (Throwable ex) {
			log.error("Upgrading model baseline failed: " + ex.getMessage(), ex);
		}
	}

}
