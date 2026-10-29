/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin.system;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.Log;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKeyN;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.compact.HistoryCompactionOperation;
import com.top_logic.knowledge.service.db2.HistoryCompaction.Report;
import com.top_logic.knowledge.service.maintenance.PersistencyMaintenance;
import com.top_logic.knowledge.service.migration.dump.KBDump;
import com.top_logic.knowledge.service.migration.load.KBLoad;
import com.top_logic.model.search.expr.config.operations.AdminOnly;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.SideEffectFree;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;
import com.top_logic.util.Resources;
import com.top_logic.util.error.TopLogicException;

/**
 * TL-Script functions maintaining the database of the application: the commands of the database
 * view of the administration.
 *
 * <p>
 * {@link #dump()} hands out the data of the application as a file, written while the browser
 * downloads it. {@link #compactHistory(Date)} discards the history before a point in time and may
 * take a long time, so a view runs it as the body of a background job:
 * </p>
 *
 * <pre>
 * &lt;start-job job="job" function="databaseCompactHistory($before)"/&gt;
 * </pre>
 *
 * <p>
 * The analysis and the compaction write their progress and the numbers per table to the
 * application log and return the totals as a message.
 * </p>
 *
 * <p>
 * A dump is loaded again by converting it into an SQL script, {@link #dumpToSql(BinaryData)},
 * which also runs as a background job, and replacing all data of the application by the data of
 * that script, {@link #replaceData(BinaryData)}.
 * </p>
 *
 * @implNote Every public static method here is a TL-Script function named by the
 *           {@link ScriptPrefix} of this class followed by the capitalized method name, so
 *           {@link #dump()} is called <code>databaseDump</code>. The functions read all data and
 *           rewrite the history of the application, which only an administrator may do from a
 *           script entered interactively.
 */
@ScriptPrefix(DatabaseFunctions.PREFIX)
@AdminOnly
public class DatabaseFunctions extends TLScriptFunctions {

	/** Prefix all function names of this class start with. */
	public static final String PREFIX = "database";

	/**
	 * Key of the data version of a dump in the result of {@link #dumpInfo(BinaryData)}.
	 */
	public static final String DUMP_VERSIONS = "versions";

	/**
	 * Key of the migration applied while loading a dump in the result of
	 * {@link #dumpInfo(BinaryData)}.
	 */
	public static final String DUMP_MIGRATION = "migration";

	/**
	 * Key of the reason why a dump cannot be loaded in the result of {@link #dumpInfo(BinaryData)}.
	 */
	public static final String DUMP_PROBLEM = "problem";

	/**
	 * The dump of all data of the application, a gzip-compressed XML file.
	 *
	 * <p>
	 * The dump is written while the file is read, e.g. downloaded, so producing it costs nothing
	 * before. The security storage is left out, since it is computed from the role rules, and so is
	 * the cluster state of the installation, e.g. the state of the maintenance window.
	 * </p>
	 *
	 * @return The file, named after the application, its version and the current revision.
	 *
	 * @see KBDump#newDump(com.top_logic.knowledge.service.KnowledgeBase, java.util.Set)
	 */
	@Label("Dump of the database")
	public static BinaryData dump() {
		return KBDump.newDump(PersistencyLayer.getKnowledgeBase(), KBDump.DEFAULT_EXCLUDE_TYPES);
	}

	/**
	 * The point in time a compaction of the history is offered for.
	 *
	 * @return The point in time one year ago.
	 *
	 * @see HistoryCompactionOperation#defaultBeforeDate()
	 */
	@Label("Default date of a history compaction")
	public static Date defaultCompactionDate() {
		return HistoryCompactionOperation.defaultBeforeDate();
	}

	/**
	 * Fails unless an operation rewriting the stored data may run now: a compaction of the history
	 * or the replacement of all data by an imported dump.
	 *
	 * <p>
	 * Such an operation needs an active maintenance window and, in a cluster, that this node is the
	 * only active one.
	 * </p>
	 *
	 * @throws TopLogicException
	 *         With the message describing the violated precondition.
	 *
	 * @see PersistencyMaintenance#checkPreconditions()
	 */
	@Label("Check that the stored data may be rewritten")
	public static void checkMaintenance() {
		PersistencyMaintenance.requirePreconditions();
	}

	/**
	 * Counts what a compaction of the history before the given point in time would change, without
	 * changing anything.
	 *
	 * @param before
	 *        The point in time before which the history would be discarded.
	 * @return What a compaction would change. The numbers per table are written to the
	 *         application log.
	 *
	 * @see HistoryCompactionOperation#analyze(long, Log)
	 */
	@Label("Analyze a compaction of the history")
	public static ResKey analyzeHistory(@Mandatory Date before) {
		Report report = HistoryCompactionOperation.analyze(before.getTime(), log());
		return summary(I18NConstants.DATABASE_ANALYSIS__REVISION_DELETED_REWRITTEN_REPINNED_CLEARED_DROPPED, report);
	}

	/**
	 * Collapses all revisions committed before the given point in time into the newest revision
	 * committed at or before that point in time, discarding the history before it irrevocably.
	 *
	 * <p>
	 * The running application keeps the discarded revisions in its caches until
	 * {@link #restartPersistency()} is called.
	 * </p>
	 *
	 * @param before
	 *        The point in time before which the history is discarded.
	 * @return What the compaction changed. The numbers per table are written to the application
	 *         log.
	 *
	 * @throws TopLogicException
	 *         If the compaction cannot run now, see {@link #checkMaintenance()}, or fails.
	 *
	 * @see HistoryCompactionOperation#compact(long, Log)
	 */
	@Label("Compact the history")
	public static ResKey compactHistory(@Mandatory Date before) {
		Report report = HistoryCompactionOperation.compact(before.getTime(), log());
		return summary(
			com.top_logic.knowledge.service.compact.I18NConstants.TASK_COMPACTION_DONE__REVISION_DELETED_REWRITTEN_REPINNED_CLEARED_DROPPED,
			report);
	}

	/**
	 * The totals of the given report, filled into the given message.
	 */
	private static ResKey summary(ResKeyN message, Report report) {
		return message.fill(
			Long.valueOf(report.getCompactionRevision()),
			Long.valueOf(report.getDeletedRows()),
			Long.valueOf(report.getRewrittenRows()),
			Long.valueOf(report.getRewrittenPins()),
			Long.valueOf(report.getClearedPins()),
			Long.valueOf(report.getRowsDeletedForDanglingPins()));
	}

	/**
	 * What the given dump contains and whether it can be loaded into this application.
	 *
	 * @param dump
	 *        A dump as written by {@link #dump()}.
	 * @return A map with the entries {@link #DUMP_VERSIONS} (the versions of the modules the data
	 *         of the dump was written with, one line per module), {@link #DUMP_MIGRATION} (the
	 *         versions the data is migrated to while loading it, one line per module) and
	 *         {@link #DUMP_PROBLEM} (the reason why the dump cannot be loaded, the other entries
	 *         are then <code>null</code>; <code>null</code> if it can be loaded). <code>null</code>
	 *         without a dump.
	 *
	 * @see KBLoad#open(Log, BinaryData)
	 */
	@Label("Description of a dump")
	@SideEffectFree
	public static Map<String, Object> dumpInfo(BinaryData dump) {
		if (dump == null) {
			return null;
		}
		Map<String, Object> result = new HashMap<>();
		KBLoad load;
		try {
			load = KBLoad.open(log(), dump);
		} catch (TopLogicException ex) {
			result.put(DUMP_VERSIONS, null);
			result.put(DUMP_MIGRATION, null);
			result.put(DUMP_PROBLEM, Resources.getInstance().getString(ex.getErrorKey()));
			return result;
		}

		Resources resources = Resources.getInstance();
		String versions = load.hasDataVersion() ? KBLoad.toText(load.getDataVersions())
			: resources.getString(com.top_logic.knowledge.service.migration.load.I18NConstants.NO_DATA_VERSION);
		String migrated = KBLoad.toText(load.getMigratedVersions());
		if (migrated.isEmpty()) {
			migrated = resources.getString(com.top_logic.knowledge.service.migration.load.I18NConstants.NO_MIGRATIONS);
		}
		result.put(DUMP_VERSIONS, versions.strip());
		result.put(DUMP_MIGRATION, migrated.strip());
		result.put(DUMP_PROBLEM, null);
		return result;
	}

	/**
	 * Converts the given dump into a gzip-compressed SQL script loading its data into an empty
	 * database of the kind this application uses.
	 *
	 * <p>
	 * The conversion migrates the data to the version of this application and may take a long time.
	 * </p>
	 *
	 * @param dump
	 *        A dump as written by {@link #dump()}.
	 * @return The SQL script, named after the dump.
	 *
	 * @see KBLoad#createSqlScript(Log)
	 */
	@Label("Convert a dump to an SQL script")
	public static BinaryData dumpToSql(@Mandatory BinaryData dump) {
		Log log = log();
		return KBLoad.open(log, dump).createSqlScript(log);
	}

	/**
	 * Converts the given dump into control files of the Oracle SQL*Loader, packed as ZIP archive.
	 *
	 * <p>
	 * The conversion migrates the data to the version of this application and may take a long time.
	 * </p>
	 *
	 * @param dump
	 *        A dump as written by {@link #dump()}.
	 * @return The archive, named after the dump.
	 *
	 * @see KBLoad#createLoaderScript(Log)
	 */
	@Label("Convert a dump to SQL*Loader scripts")
	public static BinaryData dumpToLoaderScript(@Mandatory BinaryData dump) {
		Log log = log();
		return KBLoad.open(log, dump).createLoaderScript(log);
	}

	/**
	 * Replaces all data of the application by the data of the given SQL script a few seconds from
	 * now, irrevocably.
	 *
	 * <p>
	 * All tables of the application are dropped and created again while the persistency layer is
	 * stopped, then the script is executed. Restarting the persistency layer ends every session,
	 * the one of the caller included.
	 * </p>
	 *
	 * @param sqlScript
	 *        The SQL script created by {@link #dumpToSql(BinaryData)}.
	 *
	 * @throws TopLogicException
	 *         If the data cannot be replaced now, see {@link #checkMaintenance()}.
	 *
	 * @see KBLoad#replaceDatabase(Log, BinaryData)
	 */
	@Label("Replace all data")
	public static void replaceData(@Mandatory BinaryData sqlScript) {
		KBLoad.replaceDatabase(new LogProtocol(DatabaseFunctions.class), sqlScript);
	}

	/**
	 * Restarts the persistency layer a few seconds from now, dropping the caches that still
	 * describe a discarded history.
	 *
	 * <p>
	 * The restart ends every session, the one of the caller included. An active maintenance window
	 * stays active.
	 * </p>
	 *
	 * @see HistoryCompactionOperation#scheduleRestart(Log, boolean)
	 */
	@Label("Restart the persistency layer")
	public static void restartPersistency() {
		HistoryCompactionOperation.scheduleRestart(new LogProtocol(DatabaseFunctions.class), false);
	}

	private static Log log() {
		return new LogProtocol(DatabaseFunctions.class);
	}

}
