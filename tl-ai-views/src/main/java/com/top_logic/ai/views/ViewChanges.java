/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.views;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.top_logic.ai.views.ViewCheck.Finding;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLObject;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Changes of view files, recorded as change sets ({@code tl.ai.views:ViewChangeSet}) with the text
 * of every view before and after.
 *
 * <p>
 * A change is checked first and nothing is written while a view would not work ({@link #write});
 * the record is what {@link #revert} puts back. The text a view had before a change is its base for
 * the revert: a view that changed since is a conflict, and the revert is refused rather than
 * overwriting what somebody else wrote.
 * </p>
 *
 * <p>
 * The caller runs this in a transaction of the knowledge base.
 * </p>
 */
public final class ViewChanges {

	private static final String MODULE = "tl.ai.views";

	private static final String SET = MODULE + ":ViewChangeSet";

	private static final String CHANGE = MODULE + ":ViewChange";

	/** Status of a set whose views are written. */
	public static final String APPLIED = "APPLIED";

	/** Status of a set that was applied and put back. */
	public static final String REVERTED = "REVERTED";

	private ViewChanges() {
		// Static utility.
	}

	/**
	 * The new text of one view.
	 *
	 * @param resource
	 *        The view, see {@link ViewFiles#resource(String)}.
	 * @param after
	 *        Its new text, <code>null</code> to delete it.
	 */
	public record Change(String resource, String after) {
		// A value.
	}

	/**
	 * Refusal of a change because a view would not work or changed meanwhile.
	 */
	public static class RefusedException extends IllegalArgumentException {

		/**
		 * Creates a {@link RefusedException}.
		 */
		public RefusedException(String message) {
			super(message);
		}
	}

	/**
	 * Checks and writes changes at once, and records them as an applied change set.
	 *
	 * @return The change set.
	 * @throws RefusedException
	 *         When a view would not work; nothing is written or recorded.
	 */
	public static TLObject write(String title, String author, List<Change> changes) {
		Map<String, String> before = currentTexts(changes);
		Map<String, String> after = after(changes);
		List<Finding> findings = ViewCheck.check(after);
		if (!findings.isEmpty()) {
			throw new RefusedException("Nothing was written, because the view would not work:\n" + lines(findings));
		}
		writeAll(after);
		TLObject set = record(title, author, before, changes);
		setStatus(set, APPLIED);
		return set;
	}

	/**
	 * Puts back the views an applied change set wrote.
	 *
	 * @return The views written, as resource paths.
	 * @throws RefusedException
	 *         When the set is not applied, or a view changed after it was applied.
	 */
	public static List<String> revert(TLObject set) {
		if (!APPLIED.equals(status(set))) {
			throw new RefusedException("Only an applied change can be undone; this one is " + status(set) + ".");
		}
		Map<String, String> before = new LinkedHashMap<>();
		for (TLObject change : changes(set)) {
			String resource = ViewFiles.resource(string(change, "name"));
			requireText(resource, string(change, "after"));
			before.put(resource, string(change, "before"));
		}
		writeAll(before);
		setStatus(set, REVERTED);
		return new ArrayList<>(before.keySet());
	}

	/** The status of a change set, as one of the constants of this class. */
	public static String status(TLObject set) {
		return set.tValueByName("status") instanceof TLClassifier status ? status.getName() : null;
	}

	/** The views a change set changes. */
	@SuppressWarnings("unchecked")
	private static List<TLObject> changes(TLObject set) {
		Object changes = set.tValueByName("changes");
		return changes == null ? List.of() : new ArrayList<>((List<TLObject>) changes);
	}

	/**
	 * Every change set.
	 */
	public static List<TLObject> all() {
		return new ArrayList<>(MetaElementUtil.getAllDirectInstancesOf((TLClass) TLModelUtil.findType(SET), TLObject.class));
	}

	/**
	 * The newest change set an account made that is still applied, or <code>null</code>.
	 *
	 * @param account
	 *        The name of the account, as {@link #write} records it in the author.
	 */
	public static TLObject newestApplied(String account) {
		TLObject newest = null;
		Date newestDate = null;
		for (TLObject set : all()) {
			Object author = set.tValueByName("author");
			Date date = (Date) set.tValueByName("date");
			if (APPLIED.equals(status(set)) && author != null
				&& (author.equals(account) || author.toString().startsWith(account + " ("))
				&& (newestDate == null || date != null && date.after(newestDate))) {
				newest = set;
				newestDate = date;
			}
		}
		return newest;
	}

	/** One line per finding. */
	private static String lines(List<Finding> findings) {
		return findings.stream().map(Finding::toString).collect(Collectors.joining("\n"));
	}

	private static Map<String, String> currentTexts(List<Change> changes) {
		Map<String, String> texts = new LinkedHashMap<>();
		for (Change change : changes) {
			ViewFiles.writableFile(change.resource());
			texts.put(change.resource(), ViewFiles.read(change.resource()));
		}
		return texts;
	}

	private static Map<String, String> after(List<Change> changes) {
		Map<String, String> after = new LinkedHashMap<>();
		changes.forEach(change -> after.put(change.resource(), change.after()));
		return after;
	}

	private static void writeAll(Map<String, String> texts) {
		texts.forEach(ViewFiles::write);
	}

	private static void requireText(String resource, String expected) {
		if (!Objects.equals(ViewFiles.read(resource), expected)) {
			throw new RefusedException(ViewFiles.relative(resource)
				+ " was changed after this change; revert the newer change first.");
		}
	}

	private static TLObject record(String title, String author, Map<String, String> before, List<Change> changes) {
		ModelService models = ModelService.getInstance();
		TLObject set = models.getFactory().createObject((TLClass) TLModelUtil.findType(SET));
		set.tUpdateByName("name", title);
		set.tUpdateByName("author", author);
		set.tUpdateByName("date", new Date());
		List<TLObject> parts = new ArrayList<>();
		for (Change change : changes) {
			TLObject part = models.getFactory().createObject((TLClass) TLModelUtil.findType(CHANGE));
			part.tUpdateByName("name", ViewFiles.relative(change.resource()));
			part.tUpdateByName("before", before.get(change.resource()));
			part.tUpdateByName("after", change.after());
			parts.add(part);
		}
		set.tUpdateByName("changes", parts);
		return set;
	}

	private static void setStatus(TLObject set, String status) {
		set.tUpdateByName("status", TLModelUtil.findPart(MODULE + ":ViewChangeStatus#" + status));
	}

	private static String string(TLObject object, String attribute) {
		Object value = object.tValueByName(attribute);
		return value == null ? null : value.toString();
	}
}
