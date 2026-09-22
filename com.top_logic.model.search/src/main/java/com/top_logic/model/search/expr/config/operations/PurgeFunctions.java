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
import com.top_logic.knowledge.service.KBUtils;
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
 *           {@link #analyze(Object)} is called <code>purgeAnalyze</code>.
 */
@ScriptPrefix(PurgeFunctions.PREFIX)
public class PurgeFunctions extends TLScriptFunctions {

	/** Prefix all function names of this class start with. */
	public static final String PREFIX = "purge";

	/** Whether the report describes a run that only counted, see {@link #analyze(Object)}. */
	public static final String KEY_DRY_RUN = "dryRun";

	/** Whether a living object prevents the removal, see {@link #KEY_BLOCKERS}. */
	public static final String KEY_BLOCKED = "blocked";

	/** Whether there is nothing to remove. */
	public static final String KEY_EMPTY = "empty";

	/** All objects that are removed, the named ones among them. */
	public static final String KEY_HULL = "hull";

	/** The objects of the {@link #KEY_HULL} that are not deleted and therefore block the removal. */
	public static final String KEY_BLOCKERS = "blockers";

	/** Number of rows that are erased, by the name of the database table holding them. */
	public static final String KEY_ERASED_ROWS = "erasedRows";

	/**
	 * Number of reference values that are reset, by the name of the database table holding them and
	 * the name of the reference.
	 */
	public static final String KEY_CLEARED_REFERENCES = "clearedReferences";

	/** The name of the type of an object of the {@link #KEY_HULL}. */
	public static final String KEY_TYPE = "type";

	/** The identifier of an object of the {@link #KEY_HULL}. */
	public static final String KEY_ID = "id";

	/** The branch an object of the {@link #KEY_HULL} lives on. */
	public static final String KEY_BRANCH = "branch";

	/** Why an object belongs to the {@link #KEY_HULL}: a seed, a reference or a content. */
	public static final String KEY_ORIGIN = "origin";

	/** The table and the name of the reference an object of the {@link #KEY_HULL} was reached by. */
	public static final String KEY_REFERENCE = "reference";

	/** The object of the {@link #KEY_HULL} another one was reached from, as its type and identifier. */
	public static final String KEY_CAUSE = "cause";

	private static final String CAUSE_SEPARATOR = "#";

	/**
	 * Counts what removing the given objects would erase, without changing anything.
	 *
	 * <p>
	 * The objects are given as a single object or as a list of objects. Each of them is a business
	 * object - in any revision, since an object is removed from all of them - a knowledge item, an
	 * object identifier, or the text form of one; anything else ends the function with an error.
	 * </p>
	 *
	 * <p>
	 * The answer is a structure with the entries described by {@link #KEY_HULL},
	 * {@link #KEY_BLOCKERS}, {@link #KEY_ERASED_ROWS} and {@link #KEY_CLEARED_REFERENCES}, together
	 * with {@link #KEY_DRY_RUN}, {@link #KEY_BLOCKED} and {@link #KEY_EMPTY}. It is safe to run at
	 * any time and is the way to see what {@link #deleted(Object)} would do.
	 * </p>
	 *
	 * <pre>
	 * {
	 *   gone = $project.inRevision(revisionAt(dateTime(2026, 1, 1))).get(`my:Project#members`);
	 *   purgeAnalyze($gone)[`blocked`];
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
				return ObjectKey.fromStringObjectKey(KBUtils.typeSystem(PersistencyLayer.getKnowledgeBase()), text);
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
		result.put(KEY_TYPE, identity.getObjectType().getName());
		result.put(KEY_ID, identity.getObjectName().toString());
		result.put(KEY_BRANCH, SearchExpression.toNumber(identity.getBranchId()));
		result.put(KEY_ORIGIN, origin == null ? null : origin.getKind().name());
		result.put(KEY_REFERENCE, origin == null ? null : origin.getReference());
		result.put(KEY_CAUSE, origin == null ? null : name(origin.getCause()));
		return result;
	}

	/**
	 * The given object as the type and identifier a report names it by.
	 */
	private static String name(ObjectBranchId identity) {
		if (identity == null) {
			return null;
		}
		return identity.getObjectType().getName() + CAUSE_SEPARATOR + identity.getObjectName();
	}

}
