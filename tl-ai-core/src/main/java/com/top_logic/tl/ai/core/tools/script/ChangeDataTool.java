/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.script;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.SubSessionContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.security.ModelSecurity;
import com.top_logic.tl.ai.core.security.ReadFilter;
import com.top_logic.tl.ai.core.tools.model.ChangeReport;
import com.top_logic.tl.ai.core.tools.script.TLScriptExecutor.Change;
import com.top_logic.tl.ai.tool.api.AnswerMarking;
import com.top_logic.tl.ai.tool.api.P;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.util.TLContext;

/**
 * Changes data with one script in the application's query language (TL-Script), with the user's
 * rights: the tool for a set of objects chosen by a condition, values calculated from other data, or
 * several steps that belong together. One known object with given values is changed by the object
 * tools instead (#625).
 *
 * <h2>Only changes</h2>
 *
 * <p>
 * A script without any effect is refused, and pointed to {@code query} and {@code present}, which
 * read without anybody being asked. The refusal is given before anybody is asked to approve the call
 * ({@link ToolMethod#check()}), so a read never shows an approval prompt, and reading and changing
 * have one tool each (#630). The same check refuses a script that does not compile and, for a real
 * change, an account without {@link ToolGroups#DATA_WRITE} - each of them a prompt the user would
 * otherwise answer in vain.
 * </p>
 *
 * <h2>The dry run</h2>
 *
 * <p>
 * {@code dryRun=true} runs the change and answers what it would change - the same report a real run
 * answers with, values before and after - and saves nothing. It is a read
 * ({@link ToolMethod#dryRun()}): no approval, and no part in the turn's transaction. That holds by
 * construction rather than by care:
 * </p>
 * <ul>
 * <li>It runs in an interaction of its own for the same user, on a worker thread, in a transaction
 * that is always rolled back. The engine's transactions do not nest: rolling back one opened inside
 * the turn's transaction would fail the turn, and a separate interaction has a transaction context
 * of its own.</li>
 * <li>It runs only a script whose every effect is a write to the knowledge base ({@link SideEffects}),
 * because that is all a rollback takes back. A script that also sends a mail, draws a number or calls
 * another system is checked and not run.</li>
 * <li>It runs with the user's rights: the engine checks them inside the script, so a dry run refuses
 * what the real run would refuse.</li>
 * </ul>
 *
 * <h2>Rights</h2>
 *
 * <p>
 * The tool is in {@link ToolGroups#SCRIPT}; making a change costs {@link ToolGroups#DATA_WRITE} on top
 * (a dry run does not), decided by the run's own policy - background allowlist, administrator bypass,
 * role grants - exactly as for a {@code data.write} tool. A call in a system context is refused: the
 * engine checks no rights there and records no user as the author. What the answer shows passes
 * {@link ReadFilter}; the pre-commit guard asks again for whatever the turn changed.
 * </p>
 */
public class ChangeDataTool {

	/** What a refused change says it was trying to do. */
	private static final String OPERATION = "changing data with changeData";

	/** What the transaction of a dry run is called. */
	private static final ResKey DRY_RUN = ResKey.text("changeData dry run");

	/** How many entries of a list the script returns are written out. */
	private static final int MAX_RESULT_ENTRIES = 10;

	/** How long a dry run waits between two looks at whether the run was cancelled. */
	private static final long POLL_MS = 200;

	/**
	 * Where dry runs run: in an interaction of their own, which needs a thread of its own.
	 */
	private static final ExecutorService DRY_RUNS = Executors.newCachedThreadPool(runnable -> {
		Thread thread = new Thread(runnable, "changeData-dry-run");
		thread.setDaemon(true);
		return thread;
	});

	/**
	 * Configuration for {@link ChangeDataTool}.
	 */
	public interface Config extends PolymorphicConfiguration<ChangeDataTool> {
		// no additional properties
	}

	/**
	 * Creates a new {@link ChangeDataTool}.
	 */
	public ChangeDataTool() {
		// stateless tool
	}

	/**
	 * Creates a new {@link ChangeDataTool} from configuration.
	 */
	@SuppressWarnings("unused")
	@CalledByReflection
	public ChangeDataTool(InstantiationContext context, Config config) {
		// no-op for stateless tools
	}

	/**
	 * Runs a change, or with {@code dryRun} answers what it would change.
	 *
	 * @param script
	 *        The change, as one script.
	 * @param dryRun
	 *        Whether to run the change without saving it.
	 * @param context
	 *        The run: its rights and the turn's transaction.
	 * @return What was changed, or would be; a failed result for a script that is refused.
	 */
	@ToolMethod(description = "Change data with ONE script in the application's query language, with the"
		+ " user's rights: objects chosen by a condition, values calculated from other data, or several"
		+ " steps that belong together. E.g. raise the price of every open item by 10 %:"
		+ " all(`m:Item`).filter(i -> $i.get(`m:Item#status`) == `m:Status#OPEN`).foreach(i ->"
		+ " $i.set(`m:Item#price`, round(value: $i.get(`m:Item#price`) * 1.1, digits: 2)))."
		+ " One known object with given values needs no script: use updateObject, createObject or"
		+ " deleteObject. A script that only reads is refused: answer a question with query, show objects"
		+ " with present. Call it with dryRun=true first: the change runs without being saved, needs no"
		+ " approval, and the answer lists what it would change, with old and new values. Then call it"
		+ " again without dryRun: the user approves the change, and the answer lists what was changed -"
		+ " that is the confirmation.",
		mode = ToolMode.DATA_WRITE, group = ToolGroups.SCRIPT, marking = AnswerMarking.TOOL,
		dryRun = "dryRun", check = "refusal")
	public ToolResult changeData(
			@P("The change as one script. Use the qualified names outlineModel or searchModel reported, and"
				+ " \"Type:123\".objectResolveKey() for an object whose id you have.") String script,
			@P(value = "true: run the change without saving it and answer what it would change. Needs no"
				+ " approval.", required = false) Boolean dryRun,
			ToolContext context) {
		boolean dry = Boolean.TRUE.equals(dryRun);
		Checked checked = check(script, dry, context);
		if (checked.refusal() != null) {
			return ToolResult.failure(checked.refusal());
		}
		if (dry) {
			return ToolResult.of(dryRun(checked.change(), context));
		}
		return ToolResult.of(change(checked.change(), context));
	}

	/**
	 * Why a call of {@link #changeData(String, Boolean, ToolContext)} is refused, asked before
	 * anybody is asked to approve it.
	 *
	 * @return The refusal, or <code>null</code> if the call may proceed.
	 */
	String refusal(String script, Boolean dryRun, ToolContext context) {
		return check(script, Boolean.TRUE.equals(dryRun), context).refusal();
	}

	/**
	 * Everything that is decided before the script runs, in the order the model can act on it.
	 */
	private static Checked check(String script, boolean dry, ToolContext context) {
		String source = script == null ? "" : script.trim();
		if (source.isEmpty()) {
			return Checked.refused("No script. Pass the change as one script, e.g."
				+ " \"Item:12\".objectResolveKey().set(`m:Item#price`, 450).");
		}
		if (ThreadContext.isSystemContext() || TLContext.currentUser() == null) {
			return Checked.refused("This call runs without a user account, in a system context where the"
				+ " application checks no rights and records no author. Data is only changed in the name of the"
				+ " user the run acts for. Nothing was changed.");
		}
		Change change;
		try {
			change = TLScriptExecutor.compileChange(source);
		} catch (IllegalArgumentException ex) {
			return Checked.refused("The script does not compile, nothing was changed: " + ex.getMessage());
		}
		if (!change.changes()) {
			return Checked.refused("This script changes nothing - it only reads, and changeData is for changes."
				+ " To answer a question about the data, call query with the expression; to show objects to"
				+ " the user, call present with it. Neither needs an approval.");
		}
		if (!dry) {
			String denial = context.security().capabilityDenialReason(ToolGroups.DATA_WRITE, OPERATION);
			if (denial != null) {
				return Checked.refused(denial);
			}
		}
		return new Checked(null, change);
	}

	/**
	 * Runs the change in the turn's transaction and answers what it changed.
	 *
	 * <p>
	 * A failure, and a change the model's constraints refuse, escapes as an exception: what the script
	 * wrote before cannot be taken back on its own, so the turn has to be rolled back as a whole, and
	 * the agent loop does that for a call that fails this way.
	 * </p>
	 */
	private static String change(Change change, ToolContext context) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		ModelSecurity security = context.modelSecurity();
		ChangeReport.Baseline before = ChangeReport.baseline(kb);
		Answer answer;
		try {
			answer = TLScriptExecutor.executeChange(change.executor(), result -> {
				ChangeReport report = ChangeReport.since(before, kb, security);
				if (!report.violations().isEmpty()) {
					throw new BreaksRules(report.violations());
				}
				return new Answer(report, result(result, security));
			});
		} catch (BreaksRules ex) {
			throw new IllegalStateException(ex.getMessage() + "\nAll changes of this turn are rolled back.", ex);
		} catch (RuntimeException ex) {
			throw new IllegalStateException("The script failed, so the changes of this turn are rolled back: "
				+ TLScriptExecutor.messageOf(ex), ex);
		}
		ChangeReport report = answer.report();
		String outside = change.outsideKnowledgeBase() == null ? ""
			: "\nIt also called " + change.outsideKnowledgeBase()
				+ ", whose effect is outside the database and not listed here.";
		if (report.isEmpty()) {
			return "Done, but the script changed no data: no object matched its condition, or every object"
				+ " already had the new values." + outside + answer.result();
		}
		return "Done: the script " + report.summary() + ". The change is saved when this turn ends." + outside
			+ "\n" + ReadFilter.marked("changeData", String.join("\n", report.lines()) + answer.result());
	}

	/**
	 * Runs the change where nothing of it can stay, and answers what it would change.
	 */
	private static String dryRun(Change change, ToolContext context) {
		if (change.outsideKnowledgeBase() != null) {
			return "Checked, not run: the script compiles and changes data, but it calls "
				+ change.outsideKnowledgeBase() + ", whose effect is not in the database and would not be"
				+ " taken back. So the dry run did not run it, and nothing was changed. To make the change, call"
				+ " changeData without dryRun; the user is asked to approve it.";
		}
		SubSessionContext session = ThreadContextManager.getSubSession();
		if (session == null) {
			return "The dry run needs the session of the user the run acts for, and this call has none."
				+ " Nothing was changed.";
		}
		ModelSecurity security = context.modelSecurity();
		Future<String> future =
			DRY_RUNS.submit(() -> ThreadContextManager.inContext(session, () -> sandbox(change, security)));
		while (true) {
			if (context.cancellationToken().isCancelled()) {
				future.cancel(true);
				throw new CancellationException("The dry run was cancelled.");
			}
			try {
				return future.get(POLL_MS, TimeUnit.MILLISECONDS);
			} catch (TimeoutException ex) {
				// Still running - look at the cancellation again.
			} catch (InterruptedException ex) {
				future.cancel(true);
				Thread.currentThread().interrupt();
				throw new CancellationException("The dry run was interrupted.");
			} catch (ExecutionException ex) {
				Throwable cause = ex.getCause();
				if (cause instanceof RuntimeException failure) {
					throw failure;
				}
				throw new IllegalStateException("The dry run failed: " + cause.getMessage(), cause);
			}
		}
	}

	/**
	 * The dry run itself: the change in a transaction that is never committed.
	 *
	 * <p>
	 * The transaction is the first one of this interaction, so leaving the block rolls it back
	 * completely - the engine's rollback of an outermost transaction drops every modification of it.
	 * </p>
	 */
	private static String sandbox(Change change, ModelSecurity security) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction(DRY_RUN)) {
			Object result;
			try {
				result = TLScriptExecutor.run(change.executor());
			} catch (RuntimeException ex) {
				return "Dry run, nothing was saved: the script failed - " + TLScriptExecutor.messageOf(ex)
					+ " Fix the script, then run the dry run again.";
			}
			ChangeReport report = ChangeReport.since(ChangeReport.Baseline.NONE, kb, security);
			String returned = result(result, security);
			if (!report.violations().isEmpty()) {
				return "Dry run, nothing was saved: the change would be refused, because it breaks the model's"
					+ " rules:\n- " + String.join("\n- ", report.violations())
					+ "\nFix the script, then run the dry run again.";
			}
			if (report.isEmpty()) {
				return "Dry run, nothing was saved: run as it stands, the script would change nothing - no object"
					+ " matches its condition, or every object already has the new values." + returned;
			}
			return "Dry run, nothing was saved: the script " + report.summary() + ", and that was rolled back:\n"
				+ ReadFilter.marked("changeData", String.join("\n", report.lines()) + returned)
				+ "\nIf that is what the user asked for, call changeData again with the same script and"
				+ " without dryRun; the user is asked to approve it.";
		}
	}

	/**
	 * What the script returned, as a line to append; empty for nothing worth saying - no value, an
	 * empty text, or the list of nothings {@code foreach} returns.
	 */
	private static String result(Object value, ModelSecurity security) {
		Object visible = ReadFilter.forSecurity(security).visible(value);
		if (visible instanceof Collection<?> many) {
			List<Object> values = many.stream().filter(Objects::nonNull).collect(Collectors.toList());
			if (values.isEmpty()) {
				return "";
			}
			if (values.size() > MAX_RESULT_ENTRIES) {
				return "\nThe script returns a list of " + values.size() + " entries.";
			}
			visible = values;
		}
		if (visible == null || "".equals(visible)) {
			return "";
		}
		return "\nThe script returns: " + QueryTool.render(visible).replace("\n", "; ");
	}

	/**
	 * What {@link #check(String, boolean, ToolContext)} decided.
	 *
	 * @param refusal
	 *        Why the call is refused, or <code>null</code>.
	 * @param change
	 *        The compiled change, for a call that is not refused.
	 */
	private record Checked(String refusal, Change change) {

		static Checked refused(String refusal) {
			return new Checked(refusal, null);
		}
	}

	/**
	 * A change the model's constraints refuse, found before it is committed.
	 */
	private static final class BreaksRules extends RuntimeException {

		BreaksRules(List<String> violations) {
			super("The change breaks the model's rules, so it was not made:\n- " + String.join("\n- ", violations));
		}
	}

	/**
	 * What a change produced: the report and the script's own result.
	 */
	private record Answer(ChangeReport report, String result) {
		// Sole record.
	}

}
