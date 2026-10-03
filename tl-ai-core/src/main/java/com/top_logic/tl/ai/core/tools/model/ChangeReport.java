/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tl.ai.core.security.ModelSecurity;
import com.top_logic.tl.ai.core.security.ReadFilter;
import com.top_logic.tl.ai.core.security.ToolChangeSet;

/**
 * What a script changed in the transaction that is open: the objects it created, the ones it changed
 * - each changed attribute with its value before and after - and the ones it deleted.
 *
 * <p>
 * Read from the engine's own bookkeeping of the transaction ({@link ToolChangeSet#pending}), so the
 * report says what the script did and not what it was meant to do: that is the point of showing it
 * before the user approves a change, and of answering with it after.
 * </p>
 *
 * <p>
 * Taken while the transaction is still open, because a dry run rolls it back right after and the
 * objects it created are gone with it; a report therefore holds text only. It shows what the
 * principal may read: an attribute the account may not read is named as changed without its values,
 * and an object it may not read is counted without its label.
 * </p>
 */
public final class ChangeReport {

	/** How many objects are written out one by one; the rest are counted. */
	public static final int MAX_LISTED = 20;

	/** How long a value may be before it is cut. */
	private static final int MAX_VALUE_LENGTH = 80;

	private final int _created;

	private final int _changed;

	private final int _deleted;

	private final List<String> _lines;

	private final List<String> _violations;

	private ChangeReport(int created, int changed, int deleted, List<String> lines, List<String> violations) {
		_created = created;
		_changed = changed;
		_deleted = deleted;
		_lines = lines;
		_violations = violations;
	}

	/**
	 * What a transaction already held before a script ran, so that a report names the script's own
	 * changes and not those of an earlier call of the same turn.
	 */
	public static final class Baseline {

		/** A transaction that held nothing - the dry run's own. */
		public static final Baseline NONE = new Baseline(Set.of(), Map.of(), Set.of());

		final Set<TLObject> _created;

		final Map<TLObject, Map<TLStructuredTypePart, Object>> _values;

		final Set<TLObject> _deleted;

		Baseline(Set<TLObject> created, Map<TLObject, Map<TLStructuredTypePart, Object>> values,
				Set<TLObject> deleted) {
			_created = created;
			_values = values;
			_deleted = deleted;
		}
	}

	/**
	 * Takes the {@link Baseline} of the transaction that is open on this thread.
	 */
	public static Baseline baseline(KnowledgeBase kb) {
		ToolChangeSet pending = ToolChangeSet.pending(kb);
		if (pending.isEmpty() && ToolChangeSet.pendingLinks(kb).isEmpty()) {
			return Baseline.NONE;
		}
		Set<TLObject> created = new HashSet<>();
		for (ToolChangeSet.Creation creation : pending.created()) {
			created.add(creation.object());
		}
		Set<TLObject> deleted = new HashSet<>(pending.deleted());
		Map<TLObject, Map<TLStructuredTypePart, Object>> values = new HashMap<>();
		touched(pending, ToolChangeSet.pendingLinks(kb), created, deleted).forEach((object, parts) -> {
			Map<TLStructuredTypePart, Object> snapshot = new HashMap<>();
			for (TLStructuredTypePart part : parts) {
				snapshot.put(part, snapshot(object.tValue(part)));
			}
			values.put(object, snapshot);
		});
		return new Baseline(created, values, deleted);
	}

	/**
	 * What the transaction that is open on this thread changed since the given baseline.
	 *
	 * @param before
	 *        What the transaction held before the script ran.
	 * @param kb
	 *        The knowledge base whose transaction to read.
	 * @param security
	 *        The rights of the principal the report is written for.
	 */
	public static ChangeReport since(Baseline before, KnowledgeBase kb, ModelSecurity security) {
		ToolChangeSet now = ToolChangeSet.pending(kb);
		ReadFilter filter = ReadFilter.forSecurity(security);
		List<String> changedLines = new ArrayList<>();
		List<String> createdLines = new ArrayList<>();
		List<String> deletedLines = new ArrayList<>();
		List<String> violations = new ArrayList<>();

		Set<TLObject> createdNow = new HashSet<>();
		for (ToolChangeSet.Creation creation : now.created()) {
			createdNow.add(creation.object());
		}
		Set<TLObject> deletedNow = new HashSet<>(now.deleted());
		int changed = 0;
		for (Map.Entry<TLObject, Set<TLStructuredTypePart>> entry : touched(now, ToolChangeSet.pendingLinks(kb),
			createdNow, deletedNow).entrySet()) {
			TLObject object = entry.getKey();
			Map<TLStructuredTypePart, Object> earlier = before._values.getOrDefault(object, Map.of());
			List<TLStructuredTypePart> differing = new ArrayList<>();
			Map<TLStructuredTypePart, Object> olds = new LinkedHashMap<>();
			for (TLStructuredTypePart part : entry.getValue()) {
				Object old = earlier.containsKey(part) ? earlier.get(part)
					: snapshot(kb.withoutModifications(() -> object.tValue(part)));
				if (!Objects.equals(old, snapshot(object.tValue(part)))) {
					differing.add(part);
					olds.put(part, old);
				}
			}
			if (differing.isEmpty()) {
				continue;
			}
			changed++;
			if (changedLines.size() < MAX_LISTED) {
				changedLines.add(changedLine(object, differing, olds, earlier, kb, security, filter));
			}
			violations.addAll(violations(object, security));
		}

		int created = 0;
		for (ToolChangeSet.Creation creation : now.created()) {
			TLObject object = creation.object();
			if (before._created.contains(object)) {
				continue;
			}
			created++;
			if (createdLines.size() < MAX_LISTED) {
				// An object the transaction created is readable by everybody until it is committed.
				createdLines.add("created " + ReadFilter.withId(MetaLabelProvider.INSTANCE.getLabel(object)
					+ " [type:" + TLModelUtil.qualifiedName(object.tType()) + "]", ReadFilter.idOf(object)));
			}
			violations.addAll(violations(object, security));
		}

		int deleted = 0;
		for (TLObject object : now.deleted()) {
			if (before._deleted.contains(object) || !isApplicationObject(object, kb)) {
				continue;
			}
			deleted++;
			if (deletedLines.size() < MAX_LISTED) {
				// Deleted in this transaction, so it is only readable as it was before.
				deletedLines.add("deleted " + kb.withoutModifications(
					() -> security.canRead(object) ? ReadFilter.labelled(object) : "an object your account may not read"));
			}
		}

		List<String> lines = new ArrayList<>(changedLines);
		lines.addAll(createdLines);
		lines.addAll(deletedLines);
		if (lines.size() > MAX_LISTED) {
			lines = new ArrayList<>(lines.subList(0, MAX_LISTED));
		}
		int all = changed + created + deleted;
		if (all > lines.size()) {
			lines.add("... and " + (all - lines.size()) + " more objects.");
		}
		return new ChangeReport(created, changed, deleted, List.copyOf(lines), List.copyOf(violations));
	}

	/**
	 * The attributes changed on objects that exist before and after: those booked as changed, and
	 * those whose values are stored as links. An object created or deleted in the transaction is
	 * reported as that, not with its attributes.
	 */
	private static Map<TLObject, Set<TLStructuredTypePart>> touched(ToolChangeSet changes,
			Map<TLObject, Set<TLStructuredTypePart>> links, Set<TLObject> created, Set<TLObject> deleted) {
		Map<TLObject, Set<TLStructuredTypePart>> result = new LinkedHashMap<>();
		changes.updated().forEach((object, parts) -> result.put(object, new LinkedHashSet<>(parts)));
		links.forEach((object, parts) -> {
			if (!created.contains(object) && !deleted.contains(object)) {
				result.computeIfAbsent(object, key -> new LinkedHashSet<>()).addAll(parts);
			}
		});
		return result;
	}

	/**
	 * Whether the given deleted object is a business object, and not a link that stored one of
	 * their values: removing a reference deletes such a link, and the change is reported on the
	 * object that held it.
	 */
	private static boolean isApplicationObject(TLObject object, KnowledgeBase kb) {
		if (object.tHandle() instanceof KnowledgeAssociation) {
			return false;
		}
		try {
			return kb.withoutModifications(() -> object.tType() instanceof TLClass);
		} catch (RuntimeException ex) {
			return false;
		}
	}

	/** Whether the script changed nothing. */
	public boolean isEmpty() {
		return _created + _changed + _deleted == 0;
	}

	/** What the script did, counted: e.g. {@code "changed 3 objects, created 1"}. */
	public String summary() {
		if (isEmpty()) {
			return "changed no object";
		}
		List<String> parts = new ArrayList<>();
		if (_changed > 0) {
			parts.add("changed " + objects(_changed));
		}
		if (_created > 0) {
			parts.add("created " + objects(_created));
		}
		if (_deleted > 0) {
			parts.add("deleted " + objects(_deleted));
		}
		return String.join(", ", parts);
	}

	/** One line per object, at most {@link #MAX_LISTED} of them and a count of the rest. */
	public List<String> lines() {
		return _lines;
	}

	/**
	 * The model's constraints the changed and created objects violate: why the commit would refuse
	 * the change, one sentence each, naming object and attribute.
	 */
	public List<String> violations() {
		return _violations;
	}

	private static String objects(int count) {
		return count + (count == 1 ? " object" : " objects");
	}

	/**
	 * One changed object, each attribute with its value before and after.
	 *
	 * <p>
	 * A value before the transaction is shown as it was then - in the committed view, where an object
	 * the transaction deleted still exists and can be named. A report never fails the change it
	 * describes: a value that cannot be shown is left out, and the attribute is named without it.
	 * </p>
	 */
	private static String changedLine(TLObject object, List<TLStructuredTypePart> differing,
			Map<TLStructuredTypePart, Object> olds, Map<TLStructuredTypePart, Object> earlier, KnowledgeBase kb,
			ModelSecurity security, ReadFilter filter) {
		try {
			if (!security.canRead(object)) {
				return "changed an object your account may not read";
			}
			List<String> values = new ArrayList<>();
			for (TLStructuredTypePart part : differing) {
				if (!security.canRead(object, part)) {
					values.add(part.getName() + " (your account may not read it)");
					continue;
				}
				Object old = olds.get(part);
				String before;
				try {
					before = earlier.containsKey(part) ? text(AttributeValues.echo(part, old, filter))
						: kb.withoutModifications(() -> text(AttributeValues.echo(part, old, filter)));
				} catch (RuntimeException ex) {
					before = "(not shown)";
				}
				String after;
				try {
					after = text(AttributeValues.echo(part, snapshot(object.tValue(part)), filter));
				} catch (RuntimeException ex) {
					after = "(not shown)";
				}
				values.add(part.getName() + " " + before + " -> " + after);
			}
			return ReadFilter.labelled(object) + ": " + String.join("; ", values);
		} catch (RuntimeException ex) {
			return "changed " + ReadFilter.idOf(object);
		}
	}

	private static List<String> violations(TLObject object, ModelSecurity security) {
		List<String> problems = ObjectConstraints.violations(object);
		if (problems.isEmpty()) {
			return problems;
		}
		String name = security.canRead(object) ? ReadFilter.labelled(object) : "an object";
		List<String> result = new ArrayList<>(problems.size());
		for (String problem : problems) {
			result.add(name + ": " + problem);
		}
		return result;
	}

	/** A value as the model reads it in one line: texts quoted and cut, objects by label and id. */
	private static String text(Object value) {
		if (value == null) {
			return "(empty)";
		}
		if (value instanceof String string) {
			String cut = string.length() > MAX_VALUE_LENGTH ? string.substring(0, MAX_VALUE_LENGTH) + "..." : string;
			return "\"" + cut + "\"";
		}
		if (value instanceof Number number) {
			double asDouble = number.doubleValue();
			if (asDouble == Math.rint(asDouble) && !Double.isInfinite(asDouble) && Math.abs(asDouble) < 1e15) {
				return Long.toString((long) asDouble);
			}
			return String.valueOf(number);
		}
		if (value instanceof Map<?, ?> identity && identity.containsKey("label")) {
			return ReadFilter.withId(String.valueOf(identity.get("label")),
				identity.get("id") == null ? null : String.valueOf(identity.get("id")));
		}
		if (value instanceof Collection<?> many) {
			List<String> items = new ArrayList<>(many.size());
			for (Object item : many) {
				items.add(text(item));
			}
			return items.toString();
		}
		return String.valueOf(value);
	}

	/** A value that does not change when the object does: a list is copied. */
	private static Object snapshot(Object value) {
		if (value instanceof Collection<?> many) {
			return new ArrayList<>(many);
		}
		return value;
	}
}
