/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Log;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.db2.DeletedObjectPurge.Blocker;
import com.top_logic.knowledge.service.db2.DeletedObjectPurge.Origin;
import com.top_logic.knowledge.service.db2.DeletedObjectPurge.Report;
import com.top_logic.knowledge.service.db2.DeletedObjectPurge.TableReport;
import com.top_logic.knowledge.service.maintenance.PersistencyMaintenance;
import com.top_logic.knowledge.service.purge.DeletedObjectPurgeOperation;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.util.error.TopLogicException;

/**
 * TL-Script functions that remove deleted objects from the database as if they had never existed.
 *
 * <p>
 * An object that is deleted is still stored: its rows describe the revisions it lived in, and the
 * history shows it there. Purging it erases those rows, so no revision knows it any more. Because a
 * row that refers to a removed object would be left with a value pointing nowhere, the objects that
 * are removed are not only the ones named: every object holding a mandatory reference to one of
 * them belongs to the removal too, which in particular takes every link that touches them, and so
 * does every object that one of them contained and that was deleted together with it. An optional
 * reference to a removed object is reset instead and then answers nothing.
 * </p>
 *
 * <p>
 * Everything in that hull must be deleted. If a living object holds a mandatory reference to one of
 * the objects, it blocks the removal, which then changes nothing and reports the blockers.
 * </p>
 *
 * <pre>
 * {
 *   gone = $project.inRevision(revisionAt(dateTime(2026, 1, 1))).get(`my:Project#members`);
 *   purgeAnalyze($gone);
 * }
 * </pre>
 *
 * @implNote Every public static method here is a TL-Script function named by the
 *           {@link ScriptPrefix} of this class followed by the capitalized method name, so
 *           {@link #analyze(Object)} is called <code>purgeAnalyze</code>. An object is named to
 *           these functions by the key {@link ObjectFunctions#key(TLObject)} computes. All
 *           functions are {@link AdminOnly}.
 */
@ScriptPrefix(PurgeFunctions.PREFIX)
@AdminOnly
public class PurgeFunctions extends TLScriptFunctions {

	/** Prefix all function names of this class start with. */
	public static final String PREFIX = "purge";

	/** Whether the report describes a run that only counted, see {@link #analyze(Object)}. */
	public static final String KEY_DRY_RUN = "dryRun";

	/** Whether a living object prevents the removal, see {@link #KEY_BLOCKERS}. */
	public static final String KEY_BLOCKED = "blocked";

	/** Whether there is nothing to remove. */
	public static final String KEY_EMPTY = "empty";

	/**
	 * All objects that are removed, the named ones among them; a list of structures with the entries
	 * {@link #KEY_KEY}, {@link #KEY_ORIGIN}, {@link #KEY_REFERENCE} and {@link #KEY_CAUSE}.
	 */
	public static final String KEY_HULL = "hull";

	/**
	 * The objects of the {@link #KEY_HULL} that are not deleted and therefore block the removal;
	 * structures like the ones of the {@link #KEY_HULL}.
	 */
	public static final String KEY_BLOCKERS = "blockers";

	/** Number of rows that are erased, by the name of the database table holding them. */
	public static final String KEY_ERASED_ROWS = "erasedRows";

	/**
	 * Number of reference values that are reset, by the name of the database table holding them and
	 * the name of the reference.
	 */
	public static final String KEY_CLEARED_REFERENCES = "clearedReferences";

	/**
	 * The key of an object of the {@link #KEY_HULL}: its table and identifier, and its branch unless
	 * it is the trunk, in the form {@link ObjectFunctions#key(TLObject)} computes and the functions
	 * of this class accept.
	 */
	public static final String KEY_KEY = "key";

	/**
	 * Why an object belongs to the {@link #KEY_HULL}: {@link #ORIGIN_SEED}, {@link #ORIGIN_REFERENCE}
	 * or {@link #ORIGIN_CONTENT}.
	 */
	public static final String KEY_ORIGIN = "origin";

	/** The {@link #KEY_ORIGIN} of an object that was named. */
	public static final String ORIGIN_SEED = "SEED";

	/** The {@link #KEY_ORIGIN} of an object holding a mandatory reference to a member of the hull. */
	public static final String ORIGIN_REFERENCE = "REFERENCE";

	/**
	 * The {@link #KEY_ORIGIN} of an object that a member of the hull contained and that was deleted
	 * together with it.
	 */
	public static final String ORIGIN_CONTENT = "CONTENT";

	/**
	 * The reference an object of the {@link #KEY_HULL} was reached by, as the name of the table
	 * holding it and the name of the reference; nothing for a named object.
	 */
	public static final String KEY_REFERENCE = "reference";

	/**
	 * The {@link #KEY_KEY} of the member of the {@link #KEY_HULL} an object was reached from;
	 * nothing for a named object.
	 */
	public static final String KEY_CAUSE = "cause";

	/**
	 * Counts what removing the given objects would erase, without changing anything.
	 *
	 * <p>
	 * The objects are given as a single object or as a list of objects. Each of them is a business
	 * object - in any revision, since an object is removed from all of them - or the key of one as
	 * the function computing the key of an object delivers it, in particular the key of a deleted
	 * object read from the history; anything else ends the function with an error.
	 * </p>
	 *
	 * <p>
	 * The answer is a structure with these entries:
	 * </p>
	 *
	 * <ul>
	 * <li>{@link #KEY_DRY_RUN}: whether nothing was changed, which is always so here.</li>
	 * <li>{@link #KEY_BLOCKED}: whether a living object prevents the removal, see the blockers.</li>
	 * <li>{@link #KEY_EMPTY}: whether there is nothing to remove.</li>
	 * <li>{@link #KEY_HULL}: all objects that are removed, the named ones among them. Each is a
	 * structure with the entries {@link #KEY_KEY}, the key of the object as the function computing
	 * the key of an object delivers it; {@link #KEY_ORIGIN}, why the object belongs to the hull:
	 * {@link #ORIGIN_SEED} for an object that was named, {@link #ORIGIN_REFERENCE} for one holding a
	 * mandatory reference to a member of the hull, {@link #ORIGIN_CONTENT} for one that a member of
	 * the hull contained and that was deleted together with it; {@link #KEY_REFERENCE}, the reference
	 * the object was reached by, as the name of the table holding it and the name of the reference,
	 * for a named object nothing; and {@link #KEY_CAUSE}, the key of the member of the hull the
	 * object was reached from, for a named object nothing.</li>
	 * <li>{@link #KEY_BLOCKERS}: the objects of the hull that are not deleted and therefore block
	 * the removal, as structures like the ones of the hull.</li>
	 * <li>{@link #KEY_ERASED_ROWS}: the number of rows that are erased, by the name of the database
	 * table holding them; a table without rows to erase is not listed.</li>
	 * <li>{@link #KEY_CLEARED_REFERENCES}: the number of reference values that are reset, by the
	 * name of the database table holding them and, within it, by the name of the reference; a table
	 * without values to reset is not listed.</li>
	 * </ul>
	 *
	 * <p>
	 * The function is safe to run at any time and is the way to see what {@link #deleted(Object)}
	 * would do. The keys of the hull are what {@link #deleted(Object)} takes, so the objects of an
	 * analysis can be removed as they were counted.
	 * </p>
	 *
	 * <p>
	 * In a script entered interactively, for example in the script console, only an administrator
	 * may call this function. A script that is part of the application configuration may call it
	 * for every user.
	 * </p>
	 *
	 * <pre>
	 * {
	 *   gone = $project.inRevision(revisionAt(dateTime(2026, 1, 1))).get(`my:Project#members`);
	 *   report = purgeAnalyze($gone);
	 *   if ($report[`blocked`], $report[`blockers`], purgeDeleted($report[`hull`].map(m -> $m[`key`])));
	 * }
	 * </pre>
	 *
	 * @param objects
	 *        The objects whose removal is analyzed.
	 * @return What a removal would erase.
	 */
	@Label("Analyze the removal of deleted objects")
	public static Object analyze(@Mandatory Object objects) {
		return toScriptValue(DeletedObjectPurgeOperation.analyze(seeds(objects), log()));
	}

	/**
	 * Removes the given deleted objects from the database as if they had never existed.
	 *
	 * <p>
	 * The objects are given the same way as for {@link #analyze(Object)} and the answer has the
	 * same structure. Nothing is removed while a living object blocks the removal; the answer then
	 * names the blockers.
	 * </p>
	 *
	 * <p>
	 * The removal rewrites data that the running application still holds in its caches, so it needs
	 * an active maintenance window and, in a cluster, the single active node. Unless it was blocked
	 * or found nothing, the persistency layer restarts a few seconds later, which ends every
	 * session including the one that called the function.
	 * </p>
	 *
	 * <p>
	 * In a script entered interactively, for example in the script console, only an administrator
	 * may call this function. A script that is part of the application configuration may call it
	 * for every user.
	 * </p>
	 *
	 * @param objects
	 *        The objects to remove.
	 * @return What was removed, or what blocks the removal.
	 *
	 * @see PersistencyMaintenance#checkPreconditions()
	 */
	@Label("Remove deleted objects")
	public static Object deleted(@Mandatory Object objects) {
		return toScriptValue(DeletedObjectPurgeOperation.purgeAndRestart(seeds(objects), log()));
	}

	private static Log log() {
		return new LogProtocol(PurgeFunctions.class);
	}

	/**
	 * The identifiers of the given script value.
	 */
	private static Collection<ObjectKey> seeds(Object objects) {
		List<ObjectKey> result = new ArrayList<>();
		for (Object object : CollectionUtil.asList(objects)) {
			result.add(seed(object));
		}
		return result;
	}

	/**
	 * The identifier of the given script value.
	 */
	private static ObjectKey seed(Object object) {
		if (object instanceof ObjectKey key) {
			return key;
		}
		if (object instanceof KnowledgeItem item) {
			return item.tId();
		}
		if (object instanceof TLObject wrapper) {
			KnowledgeItem handle = wrapper.tHandle();
			if (handle != null) {
				return handle.tId();
			}
		}
		if (object instanceof String text) {
			try {
				return ObjectFunctions.parseKey(PersistencyLayer.getKnowledgeBase(), text);
			} catch (UnknownTypeException | IllegalArgumentException ex) {
				throw new TopLogicException(I18NConstants.ERROR_NOT_AN_OBJECT_TO_REMOVE__VALUE.fill(text), ex);
			}
		}
		throw new TopLogicException(I18NConstants.ERROR_NOT_AN_OBJECT_TO_REMOVE__VALUE.fill(object));
	}

	/**
	 * The given report as a script value.
	 *
	 * @param report
	 *        What a purge removed, or would remove.
	 * @return A structure with the entries named by the key constants of this class.
	 */
	private static Map<String, Object> toScriptValue(Report report) {
		Map<String, Object> result = new LinkedHashMap<>();
		result.put(KEY_DRY_RUN, Boolean.valueOf(report.isDryRun()));
		result.put(KEY_BLOCKED, Boolean.valueOf(report.isBlocked()));
		result.put(KEY_EMPTY, Boolean.valueOf(report.isEmpty()));

		List<Object> hull = new ArrayList<>();
		for (ObjectBranchId identity : report.getHull()) {
			hull.add(toScriptValue(identity, report.getOrigin(identity)));
		}
		result.put(KEY_HULL, hull);

		List<Object> blockers = new ArrayList<>();
		for (Blocker blocker : report.getBlockers()) {
			blockers.add(toScriptValue(blocker.getIdentity(), blocker.getOrigin()));
		}
		result.put(KEY_BLOCKERS, blockers);

		Map<String, Object> erasedRows = new LinkedHashMap<>();
		Map<String, Object> clearedReferences = new LinkedHashMap<>();
		for (TableReport table : report.getTables()) {
			if (table.getErasedRows() > 0) {
				erasedRows.put(table.getTableName(), SearchExpression.toNumber(table.getErasedRows()));
			}
			Map<String, Long> cleared = table.getClearedReferences();
			if (!cleared.isEmpty()) {
				Map<String, Object> byReference = new LinkedHashMap<>();
				for (Entry<String, Long> entry : cleared.entrySet()) {
					byReference.put(entry.getKey(), SearchExpression.toNumber(entry.getValue().longValue()));
				}
				clearedReferences.put(table.getTableName(), byReference);
			}
		}
		result.put(KEY_ERASED_ROWS, erasedRows);
		result.put(KEY_CLEARED_REFERENCES, clearedReferences);

		return result;
	}

	/**
	 * An object of the hull as a script value.
	 */
	private static Map<String, Object> toScriptValue(ObjectBranchId identity, Origin origin) {
		Map<String, Object> result = new LinkedHashMap<>();
		result.put(KEY_KEY, key(identity));
		result.put(KEY_ORIGIN, origin == null ? null : origin(origin.getKind()));
		result.put(KEY_REFERENCE, origin == null ? null : origin.getReference());
		result.put(KEY_CAUSE, origin == null ? null : key(origin.getCause()));
		return result;
	}

	/**
	 * The script value of the given origin kind.
	 */
	private static String origin(Origin.Kind kind) {
		return switch (kind) {
			case SEED -> ORIGIN_SEED;
			case REFERENCE -> ORIGIN_REFERENCE;
			case CONTENT -> ORIGIN_CONTENT;
		};
	}

	/**
	 * The given object as the key a report names it by, see {@link ObjectFunctions#key(TLObject)}.
	 */
	private static String key(ObjectBranchId identity) {
		if (identity == null) {
			return null;
		}
		return identity.toCurrentObjectKey().asString();
	}

}
