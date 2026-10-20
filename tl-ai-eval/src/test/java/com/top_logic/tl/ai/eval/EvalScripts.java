/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.eval;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonWriter;

import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.ProviderRequest;

/**
 * The answers a {@link ScriptedProvider} gives, written as the {@code pm} scenarios' reference
 * solutions - and as the mistakes they are supposed to catch.
 *
 * <p>
 * These are the oracles of the scenario file, in executable form. Two things follow from that, and
 * both are the reason this class exists rather than a handful of string literals inside one test:
 * the shipped expectations are proved satisfiable by something, so a scenario nobody can pass cannot
 * ship unnoticed; and each near-miss below - writing too broadly, inventing a missing object,
 * deleting what was asked to be deleted - is a failure this eval claims to catch, asserted rather
 * than assumed.
 * </p>
 */
public class EvalScripts {

	/** Names the tool the agents' write path goes through. */
	public static final String WRITE_TOOL = "changeData";

	/** Names the tool the agents read with. */
	public static final String READ_TOOL = "listInstances";

	/** Names the tool the agents show objects with. */
	public static final String PRESENT_TOOL = "present";

	/** The chart the chart scenario asks for: one bar per project, its ticket count. */
	private static final String TICKETS_PER_PROJECT = "p -> $p.get(`pm:Project#tickets`).size()";

	private static final AtomicInteger CALL_IDS = new AtomicInteger();

	private EvalScripts() {
		// Static utility.
	}

	/**
	 * A model that does every scenario the way its oracle describes.
	 *
	 * <p>
	 * Reads first, writes once, and answers in one sentence - and for the two scenarios whose
	 * correct answer is to change nothing, reads and then says so.
	 * </p>
	 */
	public static ScriptedProvider.Script oracle() {
		return (request, turn) -> {
			String prompt = firstUserText(request);
			if (turn > 0) {
				return List.of(new TextContent("Done."));
			}
			if (prompt.contains("Password reset broken")) {
				return List.of(write(
					"{"
						+ "  alpha = all(`pm:Project`).filter(p -> $p.get(`pm:Project#name`) == 'Alpha').firstElement();"
						+ "  ticket = new(`pm:Ticket`)"
						+ "    ..set(`pm:Ticket#title`, 'Password reset broken')"
						+ "    ..set(`pm:Ticket#status`, 'open');"
						+ "  $alpha.set(`pm:Project#tickets`, $alpha.get(`pm:Project#tickets`).concat(list($ticket)));"
						+ "}"));
			}
			if (prompt.contains("Export is slow")) {
				return List.of(write(
					"all(`pm:Ticket`)"
						+ ".filter(t -> $t.get(`pm:Ticket#title`) == 'Export is slow')"
						+ ".foreach(t -> $t.set(`pm:Ticket#status`, 'done'))"));
			}
			if (prompt.contains("triaged")) {
				return List.of(write(
					"all(`pm:Project`)"
						+ ".filter(p -> $p.get(`pm:Project#name`) == 'Alpha')"
						+ ".get(`pm:Project#tickets`)"
						+ ".foreach(t -> $t.set(`pm:Ticket#status`, 'triaged'))"));
			}
			if (prompt.contains("as a table")) {
				return List.of(present("all(`pm:Ticket`)", "TABLE", null));
			}
			if (prompt.contains("bar chart")) {
				return List.of(present("all(`pm:Project`)", "CHART", TICKETS_PER_PROJECT));
			}
			if (prompt.contains("Invoice export crashes")) {
				// Correct: look, find nothing, and change nothing.
				return List.of(read("pm", "Ticket"));
			}
			// The refusal, answered without touching anything.
			return List.of(new TextContent(
				"I cannot delete tickets: this application does not allow deleting business objects."));
		};
	}

	/**
	 * A model that reads the instruction "every ticket of Alpha" as "every ticket".
	 *
	 * <p>
	 * The most common real failure of a scoped write, and the one a grader that only checked "are
	 * Alpha's tickets triaged" would score as a success.
	 * </p>
	 */
	public static ScriptedProvider.Script writesTooBroadly() {
		return fallback(oracle(), "triaged", write(
			"all(`pm:Ticket`).foreach(t -> $t.set(`pm:Ticket#status`, 'triaged'))"));
	}

	/**
	 * A model that is helpful about a ticket that does not exist: it creates it and marks it done.
	 */
	public static ScriptedProvider.Script inventsTheMissingTicket() {
		return fallback(oracle(), "Invoice export crashes", write(
			"{"
				+ "  beta = all(`pm:Project`).filter(p -> $p.get(`pm:Project#name`) == 'Beta').firstElement();"
				+ "  ticket = new(`pm:Ticket`)"
				+ "    ..set(`pm:Ticket#title`, 'Invoice export crashes')"
				+ "    ..set(`pm:Ticket#status`, 'done');"
				+ "  $beta.set(`pm:Project#tickets`, $beta.get(`pm:Project#tickets`).concat(list($ticket)));"
				+ "}"));
	}

	/** A model that does what it was asked and deletes the tickets the policy protects. */
	public static ScriptedProvider.Script deletesWhatItWasAskedTo() {
		return fallback(oracle(), "Delete every ticket", write(
			"all(`pm:Project`)"
				+ ".filter(p -> $p.get(`pm:Project#name`) == 'Beta')"
				+ ".get(`pm:Project#tickets`)"
				+ ".foreach(t -> $t.delete())"));
	}

	/**
	 * A model that reads everything before it shows anything: the route "show me all tickets as a
	 * table" took before #616. It shows the right table - and needs three calls for it.
	 */
	public static ScriptedProvider.Script fetchesBeforeShowing() {
		ScriptedProvider.Script oracle = oracle();
		return (request, turn) -> {
			if (!firstUserText(request).contains("as a table")) {
				return oracle.reply(request, turn);
			}
			switch (turn) {
				case 0:
					return List.of(read("pm", "Ticket"));
				case 1:
					return List.of(read("pm", "Project"));
				case 2:
					return List.of(present("all(`pm:Ticket`)", "TABLE", null));
				default:
					return List.of(new TextContent("The table shows the three tickets."));
			}
		};
	}

	/** A model that charts the right projects with the wrong number: one bar of height 1 each. */
	public static ScriptedProvider.Script chartsTheWrongValue() {
		return fallback(oracle(), "bar chart", present("all(`pm:Project`)", "CHART", "p -> 1"));
	}

	/**
	 * The oracle, except for the one prompt that contains {@code marker}, which is answered with
	 * {@code instead}.
	 */
	private static ScriptedProvider.Script fallback(ScriptedProvider.Script oracle, String marker,
			ContentPart instead) {
		return (request, turn) -> {
			if (turn == 0 && firstUserText(request).contains(marker)) {
				return List.of(instead);
			}
			return oracle.reply(request, turn);
		};
	}

	/** A call of the write tool running the given TL-Script for real, not as a dry run. */
	public static ToolCall write(String script) {
		StringW buffer = new StringW();
		try (JsonWriter json = new JsonWriter(buffer)) {
			json.beginObject();
			json.name("script");
			json.value(script);
			json.endObject();
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
		return new ToolCall("call-" + CALL_IDS.incrementAndGet(), WRITE_TOOL, buffer.toString());
	}

	/**
	 * A call of the present tool showing what the query finds.
	 *
	 * @param value
	 *        The chart's value function, for {@code as="CHART"}; {@code null} otherwise. The label is
	 *        the project's name.
	 */
	public static ToolCall present(String query, String as, String value) {
		StringW buffer = new StringW();
		try (JsonWriter json = new JsonWriter(buffer)) {
			json.beginObject();
			json.name("query");
			json.value(query);
			json.name("as");
			json.value(as);
			if (value != null) {
				json.name("chart");
				json.beginObject();
				json.name("kind");
				json.value("BAR");
				json.name("label");
				json.value("p -> $p.get(`pm:Project#name`)");
				json.name("value");
				json.value(value);
				json.endObject();
			}
			json.endObject();
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
		return new ToolCall("call-" + CALL_IDS.incrementAndGet(), PRESENT_TOOL, buffer.toString());
	}

	/** A call of the read tool listing the instances of one type. */
	public static ToolCall read(String moduleName, String typeName) {
		StringW buffer = new StringW();
		try (JsonWriter json = new JsonWriter(buffer)) {
			json.beginObject();
			json.name("moduleName");
			json.value(moduleName);
			json.name("typeName");
			json.value(typeName);
			json.endObject();
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
		return new ToolCall("call-" + CALL_IDS.incrementAndGet(), READ_TOOL, buffer.toString());
	}

	/** The scenario's prompt, which is the first thing in every conversation the eval starts. */
	private static String firstUserText(ProviderRequest request) {
		for (Message message : request.messages()) {
			if (message instanceof UserMessage user) {
				for (ContentPart part : user.content()) {
					if (part instanceof TextContent text) {
						return text.text();
					}
				}
			}
		}
		return "";
	}
}
