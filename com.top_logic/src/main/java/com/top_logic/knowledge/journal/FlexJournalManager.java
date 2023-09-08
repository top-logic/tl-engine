/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.journal;

import java.sql.SQLException;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.db.schema.setup.config.TypeProvider;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.dob.MOFactory;
import com.top_logic.dob.ex.DuplicateTypeException;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.knowledge.journal.persistancy.FlexDataChangeEntryJournal;
import com.top_logic.knowledge.journal.persistancy.JournalPersistancyHandler;
import com.top_logic.knowledge.service.FlexDataManagerFactory;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.AbstractFlexDataManager;

/**
 * {@link JournalManager} that writes to tables with flex data schema.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@ServiceDependencies({
	PersistencyLayer.Module.class,
	FlexDataManagerFactory.Module.class })
public class FlexJournalManager extends JournalManager {


	/**
	 * @param context
	 *        {@link InstantiationContext} context to instantiate sub configurations.
	 * @param config
	 *        Configuration for {@link FlexJournalManager}.
	 */
	public FlexJournalManager(InstantiationContext context, Config config) throws SQLException {
		super(context, config);
	}

	@Override
	protected JournalPersistancyHandler createAttributeHandler(Config config) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		return new FlexDataChangeEntryJournal(kb);
	}

	/**
	 * {@link TypeProvider} for {@link FlexJournalManager}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class JournalTypes implements TypeProvider {

		/**
		 * Creates a new {@link JournalTypes}.
		 */
		@CalledByReflection
		public JournalTypes(InstantiationContext context, PolymorphicConfiguration<? extends TypeProvider> config) {

		}

		@Override
		public void createTypes(Log log, MOFactory typeFactory, MORepository typeRepository) {
			try {
				typeRepository.addMetaObject(
					AbstractFlexDataManager.createFlexDataType(
						FlexDataChangeEntryJournal.JOURNAL_PRE_VALUE_TYPE_NAME, null,
						typeRepository.multipleBranches()));
			} catch (DuplicateTypeException ex) {
				log.error("Journal pre-value type already exists.", ex);
			}
			try {
				typeRepository.addMetaObject(
					AbstractFlexDataManager.createFlexDataType(
						FlexDataChangeEntryJournal.JOURNAL_POST_VALUE_TYPE_NAME, null,
						typeRepository.multipleBranches()));
			} catch (DuplicateTypeException ex) {
				log.error("Journal post-value type already exists.", ex);
			}
		}

	}

}
