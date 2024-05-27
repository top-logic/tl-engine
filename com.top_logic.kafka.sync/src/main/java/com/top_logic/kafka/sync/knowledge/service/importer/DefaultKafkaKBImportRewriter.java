/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.sync.knowledge.service.importer;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.kafka.sync.knowledge.service.KafkaExportImportConfiguration;
import com.top_logic.kafka.sync.knowledge.service.exporter.DefaultKafkaKBExportRewriter;
import com.top_logic.knowledge.event.convert.EventRewriter;
import com.top_logic.knowledge.event.convert.EventRewriterProxy;
import com.top_logic.knowledge.event.convert.StackedEventRewriter;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.model.TLModel;

/**
 * Default implementation for an Kafka {@link KnowledgeBase KB} importer, based on the
 * {@link TLModel}.
 * 
 * @see DefaultKafkaKBExportRewriter
 * @see KafkaImportConfiguration
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultKafkaKBImportRewriter extends EventRewriterProxy<DefaultKafkaKBImportRewriter.Config> {

	/**
	 * Configuration of the {@link DefaultKafkaKBImportRewriter}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends EventRewriterProxy.Config<DefaultKafkaKBImportRewriter> {

		/**
		 * Configuration of the import types.
		 */
		KafkaImportConfiguration getImportConfig();

		/**
		 * Setter for {@link #getImportConfig()}.
		 */
		void setImportConfig(KafkaImportConfiguration config);
	}

	/**
	 * Creates a new {@link DefaultKafkaKBImportRewriter} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link DefaultKafkaKBImportRewriter}.
	 */
	public DefaultKafkaKBImportRewriter(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected EventRewriter createImplementation(InstantiationContext context, Config config) {
		KafkaImportConfiguration importConfig = config.getImportConfig();
		if (importConfig == null) {
			importConfig = KafkaExportImportConfiguration.getImportConfig();
		}
		List<EventRewriter> rewriters = new ArrayList<>();
		rewriters.add(new TLSyncStructureRootCreationToUpdateEventRewriter());
		rewriters.add(newTypeRewriter(context, importConfig));

		return StackedEventRewriter.getRewriter(rewriters);
	}

	private static TTypeRewriter newTypeRewriter(InstantiationContext context, KafkaImportConfiguration config) {
		TTypeRewriter.Config typeRewriterConf = TypedConfiguration.newConfigItem(TTypeRewriter.Config.class);
		typeRewriterConf.setImplementationClass(TTypeRewriter.class);
		typeRewriterConf.setImportConfiguration(config);
		return context.getInstance(typeRewriterConf);
	}

}

