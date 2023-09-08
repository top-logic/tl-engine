/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.knowledge.service.exporter;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.kafka.knowledge.service.KafkaExportImportConfiguration;
import com.top_logic.kafka.knowledge.service.importer.DefaultKafkaKBImportRewriter;
import com.top_logic.kafka.knowledge.service.importer.KafkaImportConfiguration;
import com.top_logic.knowledge.event.convert.EventRewriter;
import com.top_logic.knowledge.event.convert.EventRewriterProxy;
import com.top_logic.knowledge.event.convert.StackedEventRewriter;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.db2.migration.rewriters.EmptyRevisionFilter;
import com.top_logic.knowledge.service.db2.migration.rewriters.OnlyPersistentAttributes;
import com.top_logic.model.TLModel;

/**
 * Default implementation for an Kafka {@link KnowledgeBase KB} exporter, based on the
 * {@link TLModel}.
 * 
 * @see DefaultKafkaKBImportRewriter
 * @see KafkaImportConfiguration
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultKafkaKBExportRewriter extends EventRewriterProxy<DefaultKafkaKBExportRewriter.Config> {

	/**
	 * Configuration of the {@link DefaultKafkaKBExportRewriter}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends EventRewriterProxy.Config<DefaultKafkaKBExportRewriter> {

		/**
		 * Export configuration.
		 */
		@InstanceFormat
		KafkaExportConfiguration getExportConfig();

		/**
		 * Setter for {@link #getExportConfig()}.
		 */
		void setExportConfig(KafkaExportConfiguration config);
	}

	/**
	 * Creates a new {@link DefaultKafkaKBExportRewriter} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link DefaultKafkaKBExportRewriter}.
	 */
	public DefaultKafkaKBExportRewriter(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected EventRewriter createImplementation(InstantiationContext context, Config config) {
		KafkaExportConfiguration exportConfig = config.getExportConfig();
		if (exportConfig == null) {
			exportConfig = KafkaExportImportConfiguration.getExportConfig();
		}
		List<EventRewriter> rewriters = new ArrayList<>();
		rewriters.add(new TypeFilterRewriter(exportConfig));
		rewriters.add(new OnlyPersistentAttributes());
		rewriters.add(new EmptyRevisionFilter());
		rewriters.add(new TLSyncStructureRootCreationMarkerEventRewriter());

		return StackedEventRewriter.getRewriter(rewriters);
	}

}

