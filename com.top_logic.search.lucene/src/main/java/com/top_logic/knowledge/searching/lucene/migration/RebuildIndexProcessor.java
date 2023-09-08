/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.searching.lucene.migration;

import java.io.File;

import com.top_logic.basic.Log;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.module.ManagedClass.ServiceConfiguration;
import com.top_logic.knowledge.indexing.lucene.LuceneIndex;
import com.top_logic.knowledge.indexing.lucene.LuceneIndex.LuceneConfig;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.migration.MigrationPostProcessor;

/**
 * {@link MigrationPostProcessor} that rebuilds the {@link LuceneIndex}.
 * 
 * @implNote The processor just removes the stored index files. The {@link LuceneIndex}
 *           automatically recreates the index (and re-indexes all elements) on start-up, when no
 *           index exists.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class RebuildIndexProcessor implements MigrationPostProcessor {

	@Override
	public void afterMigration(Log log, KnowledgeBase kb) {
		if (LuceneIndex.Module.INSTANCE.isActive()) {
			LuceneIndex luceneIndex = LuceneIndex.Module.INSTANCE.getImplementationInstance();
			if (luceneIndex.indexExists()) {
				log.info("No index rebuild necessary, because no index exists.");
			} else {
				log.info("Deleting index files.");
				luceneIndex.deleteIndex();
				log.info(
					"Index files deleted. The index is recreated when the application starts. Creating the index can take a lot of time.");
			}
		} else {
			try {
				ServiceConfiguration<LuceneIndex> luceneConfiguration =
					ApplicationConfig.getInstance().getServiceConfiguration(LuceneIndex.class);
				LuceneIndex.LuceneConfig config = (LuceneConfig) luceneConfiguration;
				File indexLocation = new File(config.getIndexLocation());
				if (!indexLocation.exists()) {
					log.info("No index rebuild necessary, because no index exists.");
				} else {
					log.info("Deleting index files.");
					FileUtilities.deleteR(indexLocation);
					log.info(
						"Index files deleted. The index is recreated when the application starts. Creating the index can take a lot of time.");

				}
			} catch (ConfigurationException ex) {
				log.error("Unable to get configuration for " + LuceneIndex.class.getName());
			}
		}

	}

}

