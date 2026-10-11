/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.views.tools;

import java.util.List;
import java.util.function.Supplier;

import com.top_logic.ai.views.ViewChanges;
import com.top_logic.ai.views.ViewChanges.Change;
import com.top_logic.ai.views.ViewElements;
import com.top_logic.ai.views.ViewFiles;
import com.top_logic.ai.views.source.ViewEdit;
import com.top_logic.ai.views.source.ViewSource;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.security.ReadFilter;
import com.top_logic.tl.ai.core.tools.context.DevelopmentWorkspace;
import com.top_logic.tl.ai.tool.api.AnswerMarking;
import com.top_logic.tl.ai.tool.api.P;
import com.top_logic.tl.ai.tool.api.RunInfo;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.util.TLContext;

/**
 * The tools that read, check and change the views of the running application.
 *
 * <p>
 * Every change is checked before it is written ({@link com.top_logic.ai.views.ViewCheck}) and
 * recorded as a change set ({@link ViewChanges}), which is what makes it reversible. A change is
 * shown on the user's open page at once.
 * </p>
 *
 * <p>
 * A write is an {@link ToolMode#EXTERNAL} call the user approves: the view file is written when the
 * call runs and no transaction of the turn can take it back, so its change-set record is committed
 * with it ({@link CommittedNow}). A turn that rolls back afterwards therefore neither loses the
 * record nor reports the write as undone. The files are the application's source, so the writes are
 * offered only where the application runs from it (see {@link #noWorkspace}), as the engine's own
 * structural changes are.
 * </p>
 */
public class ViewTools {

	/**
	 * What {@link #editView} does to one element.
	 */
	public enum Op {
		/** Sets an attribute, or removes it when the value is empty. */
		SET,
		/** Replaces the element by XML. */
		REPLACE,
		/** Inserts XML as the element's last child. */
		INSERT_INTO,
		/** Inserts XML before the element. */
		INSERT_BEFORE,
		/** Inserts XML after the element. */
		INSERT_AFTER,
		/** Removes the element. */
		REMOVE;
	}

	/**
	 * One element edit, as the model writes it.
	 *
	 * @param op
	 *        What to do.
	 * @param at
	 *        The element's address.
	 * @param xml
	 *        The XML of {@link Op#REPLACE} and the insertions.
	 * @param attribute
	 *        The attribute of {@link Op#SET}.
	 * @param value
	 *        Its new value.
	 */
	public record Edit(Op op, String at, String xml, String attribute, String value) {

		ViewEdit toViewEdit() {
			if (op == null || at == null) {
				throw new IllegalArgumentException("Every edit needs 'op' and 'at'.");
			}
			return switch (op) {
				case SET -> new ViewEdit.Set(at, attribute, value == null || value.isEmpty() ? null : value);
				case REPLACE -> new ViewEdit.Replace(at, requiredXml());
				case INSERT_INTO -> new ViewEdit.Insert(at, requiredXml(), ViewEdit.Position.INTO);
				case INSERT_BEFORE -> new ViewEdit.Insert(at, requiredXml(), ViewEdit.Position.BEFORE);
				case INSERT_AFTER -> new ViewEdit.Insert(at, requiredXml(), ViewEdit.Position.AFTER);
				case REMOVE -> new ViewEdit.Remove(at);
			};
		}

		private String requiredXml() {
			if (xml == null || xml.isBlank()) {
				throw new IllegalArgumentException(op + " at " + at + " needs 'xml'.");
			}
			return xml;
		}
	}

	/**
	 * Creates a {@link ViewTools}.
	 */
	public ViewTools() {
		// For the reflective tool factory.
	}

	/**
	 * The view elements of this application.
	 */
	@ToolMethod(description = "The tags a view of this application can use - the engine's and the application's"
		+ " own. Without a tag: every element, command and command rule. With a tag: its properties with their"
		+ " types, the tags each list takes, and how this application's views use it. Look up the tags you are"
		+ " about to write.",
		mode = ToolMode.READ, group = ToolGroups.MODEL_READ, marking = AnswerMarking.NONE)
	public String viewElements(@P(value = "A tag, for example table or form", required = false) String tag) {
		if (tag == null || tag.isBlank()) {
			return ViewElements.summary();
		}
		String described = ViewElements.describe(tag.strip());
		return described != null ? described : "There is no tag <" + tag.strip() + ">. " + ViewElements.summary();
	}

	/**
	 * The views of this application.
	 */
	@ToolMethod(description = "The view files of this application, relative to /WEB-INF/views/, one per line;"
		+ " those marked [jar] come from a module and cannot be changed here.",
		mode = ToolMode.READ, group = ToolGroups.MODEL_READ, marking = AnswerMarking.NONE)
	public String listViews(@P(value = "Only paths containing this text", required = false) String filter) {
		StringBuilder out = new StringBuilder();
		for (String view : ViewFiles.list()) {
			if (filter == null || view.contains(filter.strip())) {
				out.append(view).append(ViewFiles.file(ViewFiles.resource(view)) == null ? " [jar]" : "").append('\n');
			}
		}
		return out.length() == 0 ? "No view matches." : out.toString();
	}

	/**
	 * One view with the addresses of its elements.
	 */
	@ToolMethod(description = "Read one view: the outline of its elements - line, address and what names them -"
		+ " and its XML as written, comments included. The addresses are what editView takes.",
		mode = ToolMode.READ, group = ToolGroups.MODEL_READ, marking = AnswerMarking.NONE)
	public ToolResult readView(@P("View path relative to /WEB-INF/views/") String path) {
		try {
			String resource = ViewFiles.resource(path);
			String text = ViewFiles.read(resource);
			if (text == null) {
				return ToolResult.failure("There is no view " + path + ". listViews names them.");
			}
			return ToolResult.of("Outline:\n" + ViewSource.parse(text).outline() + "\nXML:\n" + text);
		} catch (IllegalArgumentException ex) {
			return ToolResult.failure(ex.getMessage());
		}
	}

	/**
	 * Writes a whole view.
	 */
	@ToolMethod(description = "Save a view from XML: create it, or replace an existing one (for a small change of"
		+ " an existing view, editView keeps the rest of the file as written). It is checked first; with an"
		+ " error nothing is saved and every error names its line - fix those and call again. The user's open page"
		+ " shows the change.",
		mode = ToolMode.EXTERNAL, group = ToolGroups.MODEL_STRUCTURE, marking = AnswerMarking.NONE,
		unavailable = "noWorkspace")
	public ToolResult writeView(
			@P("View path relative to /WEB-INF/views/") String path,
			@P("The complete <view> document") String xml,
			ToolExecutionContext context) {
		return write("Write " + path, () -> List.of(new Change(ViewFiles.resource(path), xml)), context);
	}

	/**
	 * Edits elements of a view.
	 */
	@ToolMethod(description = "Change elements of a view; everything else in the file - comments, formatting - stays."
		+ " The edits apply in order, each to the text the previous one left, and are saved together after the"
		+ " check: {op:SET, at, attribute, value} (empty value removes it), {op:REPLACE, at, xml},"
		+ " {op:INSERT_INTO|INSERT_BEFORE|INSERT_AFTER, at, xml}, {op:REMOVE, at}. 'at' is an address from"
		+ " readView, e.g. /view/split-panel/panes/pane[2]/form; tag[id=x] selects by an attribute.",
		mode = ToolMode.EXTERNAL, group = ToolGroups.MODEL_STRUCTURE, marking = AnswerMarking.NONE,
		unavailable = "noWorkspace")
	public ToolResult editView(
			@P("View path relative to /WEB-INF/views/") String path,
			@P("The edits") List<Edit> edits,
			ToolExecutionContext context) {
		return write("Edit " + path, () -> List.of(edited(path, edits)), context);
	}

	/**
	 * Deletes a view.
	 */
	@ToolMethod(description = "Delete a view file. Remove the references to it first (a navigation entry, a"
		+ " view-ref); revertViewChange brings it back.",
		mode = ToolMode.EXTERNAL, group = ToolGroups.MODEL_STRUCTURE, marking = AnswerMarking.NONE,
		unavailable = "noWorkspace")
	public ToolResult deleteView(@P("View path relative to /WEB-INF/views/") String path, ToolExecutionContext context) {
		return write("Delete " + path, () -> List.of(new Change(ViewFiles.resource(path), null)), context);
	}

	/**
	 * Undoes an applied change set.
	 */
	@ToolMethod(description = "Undo a view change: every view it changed gets its text from before again, a view it"
		+ " created is deleted. Without an id: the newest change of this user still applied. Refused when a view was changed again"
		+ " afterwards; undo the newer change first.",
		mode = ToolMode.EXTERNAL, group = ToolGroups.MODEL_STRUCTURE, marking = AnswerMarking.NONE,
		unavailable = "noWorkspace")
	public ToolResult revertViewChange(
			@P(value = "The change set id a write answered with; empty for the newest", required = false) TLObject changeSet,
			ToolExecutionContext context) {
		TLObject set = changeSet != null ? changeSet : ViewChanges.newestApplied(account());
		if (set == null) {
			return ToolResult.failure("There is no applied view change to undo.");
		}
		ObjectKey key = set.tId();
		try {
			List<String> written = CommittedNow.run(() -> ViewChanges.revert(
				PersistencyLayer.getKnowledgeBase().resolveObjectKey(key).getWrapper()));
			context.catchUpWithCommits();
			return ToolResult.of("Reverted " + ReadFilter.idOf(set) + ". " + RunPage.show(written));
		} catch (IllegalArgumentException ex) {
			return ToolResult.failure("Nothing was reverted: " + ex.getMessage());
		}
	}

	private static ToolResult write(String title, Supplier<List<Change>> changes, ToolExecutionContext context) {
		try {
			List<Change> resolved = changes.get();
			String author = author(context);
			String set = CommittedNow.run(() -> ReadFilter.idOf(ViewChanges.write(title, author, resolved)));
			context.catchUpWithCommits();
			return ToolResult.of("Saved as change set " + set + ". "
				+ RunPage.show(resolved.stream().map(Change::resource).toList()));
		} catch (IllegalArgumentException ex) {
			return ToolResult.failure(ex.getMessage());
		}
	}

	private static Change edited(String path, List<Edit> edits) {
		String resource = ViewFiles.resource(path);
		String text = ViewFiles.read(resource);
		if (text == null) {
			throw new IllegalArgumentException("There is no view " + path + "; writeView creates one.");
		}
		int n = 0;
		for (Edit edit : edits) {
			n++;
			try {
				text = edit.toViewEdit().apply(text);
			} catch (IllegalArgumentException ex) {
				throw new IllegalArgumentException("Edit " + n + " (" + edit.op() + " at " + edit.at() + "): "
					+ ex.getMessage() + " Nothing was saved.");
			}
		}
		return new Change(resource, text);
	}

	/**
	 * Why the writes are not offered in this run, or <code>null</code> when they are.
	 */
	String noWorkspace(ToolExecutionContext context) {
		return DevelopmentWorkspace.webapp() != null ? null
			: "The view files are the application's source, so they are only changed where the application runs from"
				+ " it - started with tl_operation_mode=development. Show the user the XML instead.";
	}

	private static String account() {
		Person person = TLContext.currentUser();
		return person == null ? "system" : person.getName();
	}

	private static String author(ToolExecutionContext context) {
		String account = account();
		RunInfo run = context.runInfo();
		return run.agentName() == null ? account : account + " (" + run.agentName() + ")";
	}
}
