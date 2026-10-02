/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.blob;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.format.MillisFormat;
import com.top_logic.basic.io.blob.BlobStore;
import com.top_logic.basic.io.blob.BlobStoreNames;
import com.top_logic.basic.io.blob.BlobStoreService;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.util.sched.task.impl.StateHandlingTask;
import com.top_logic.util.sched.task.result.TaskResult.ResultType;

/**
 * Task deleting the content of blob stores that is no longer referenced from the database.
 *
 * <p>
 * Each run collects the configured stores one after another. A blob is deleted if no row of any
 * table references it, including historic revisions, and if it was written before the grace
 * period. A store whose keys or referenced keys are not delivered in lexicographic order is not
 * touched; the task then fails.
 * </p>
 *
 * @implNote The collection of a single store is performed by
 *           {@link BlobGarbageCollector#collect(BlobStore, Duration, Instant, Log)}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp
public class BlobGarbageCollectionTask<C extends BlobGarbageCollectionTask.Config<?>> extends StateHandlingTask<C> {

	/**
	 * Configuration options for {@link BlobGarbageCollectionTask}.
	 */
	public interface Config<I extends BlobGarbageCollectionTask<?>> extends StateHandlingTask.Config<I> {

		/** Name of the property {@link #getGracePeriod()}. */
		String GRACE_PERIOD = "grace-period";

		/** Name of the property {@link #getStores()}. */
		String STORES = "stores";

		/** Default value of {@link #getGracePeriod()}: 24 hours. */
		long DEFAULT_GRACE_PERIOD = 24L * 60 * 60 * 1000;

		/**
		 * Time span an unreferenced blob is kept after it was written.
		 *
		 * <p>
		 * The grace period protects content that has been uploaded while its transaction has not
		 * yet committed, so it must be much longer than any transaction.
		 * </p>
		 *
		 * <p>
		 * The grace period must also be at least as long as the backup retention, i.e. the age of
		 * the oldest database backup that may be restored. Historic revisions of versioned tables
		 * keep their content referenced forever, but content of unversioned tables, of a knowledge
		 * base without versioning, and of purged history becomes unreferenced when its row is
		 * removed. A restored backup that still references such content finds it only if the
		 * content was not collected after the backup was taken.
		 * </p>
		 */
		@Name(GRACE_PERIOD)
		@Format(MillisFormat.class)
		@LongDefault(DEFAULT_GRACE_PERIOD)
		long getGracePeriod();

		/**
		 * Names of the blob stores to collect.
		 *
		 * <p>
		 * If empty, all stores of the blob store service are collected.
		 * </p>
		 */
		@Name(STORES)
		@Format(CommaSeparatedStrings.class)
		@Options(fun = BlobStoreNames.class)
		List<String> getStores();

	}

	/**
	 * Creates a {@link BlobGarbageCollectionTask} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public BlobGarbageCollectionTask(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	protected void runHook() {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		if (!(kb instanceof DBKnowledgeBase)) {
			throw new IllegalStateException("Blob garbage collection requires a database knowledge base: " + kb);
		}
		BlobGarbageCollector collector = BlobGarbageCollector.newInstance((DBKnowledgeBase) kb);
		BlobStoreService service = BlobStoreService.getInstance();
		List<String> storeNames = getConfig().getStores();
		if (storeNames.isEmpty()) {
			storeNames = new ArrayList<>(service.getStores().keySet());
		}

		Duration gracePeriod = Duration.ofMillis(getConfig().getGracePeriod());
		Log log = new LogProtocol(BlobGarbageCollectionTask.class);
		long deleted = 0;
		long keptByGrace = 0;
		int collected = 0;
		List<String> aborted = new ArrayList<>();
		for (String storeName : storeNames) {
			if (getShouldStop()) {
				return;
			}
			BlobStore store = service.getStore(storeName);
			BlobGarbageCollector.Result result;
			try {
				result = collector.collect(store, gracePeriod, Instant.now(), log);
			} catch (IOException | SQLException ex) {
				throw new RuntimeException("Blob garbage collection of store '" + storeName + "' failed.", ex);
			}
			collected++;
			if (result.aborted()) {
				aborted.add(storeName);
			}
			if (result.failures() > 0) {
				getLog().getCurrentResult().addWarning(result.toString());
			}
			deleted += result.deleted();
			keptByGrace += result.keptByGrace();
		}

		if (!aborted.isEmpty()) {
			getLog().taskEnded(ResultType.FAILURE,
				I18NConstants.ERROR_BLOB_GC_ABORTED__STORES.fill(String.join(", ", aborted)));
		} else {
			ResultType resultType =
				getLog().getCurrentResult().hasWarnings() ? ResultType.WARNING : ResultType.SUCCESS;
			getLog().taskEnded(resultType, I18NConstants.BLOB_GC_DONE__STORES_DELETED_KEPT.fill(
				Integer.valueOf(collected), Long.valueOf(deleted), Long.valueOf(keptByGrace)));
		}
	}

	@Override
	public boolean isNodeLocal() {
		// Deletes shared content; must run only once in the cluster.
		return false;
	}

}
