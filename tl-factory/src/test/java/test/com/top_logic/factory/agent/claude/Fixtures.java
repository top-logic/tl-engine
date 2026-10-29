/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.agent.claude;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * The recorded stream-json output of Claude Code in the directory {@value #DIR}.
 *
 * <p>
 * Recorded by {@link TestClaudeCodeIntegration} from the Claude Code version pinned in the
 * workspace image, one file per message of a conversation:
 * </p>
 * <ul>
 * <li>{@value #OK}: "Antworte nur mit OK." - the first message of a new process.</li>
 * <li>{@value #TOOL}: a message answered with a Bash tool call reading pom.xml.</li>
 * <li>{@value #INTERRUPT}: a long answer interrupted by a control request after its first text.</li>
 * <li>{@value #RESUME}: the first message of a new process resuming the conversation.</li>
 * <li>{@value #LOGIN_FAILED}: a message to a process whose token the workspace proxy refuses.</li>
 * <li>{@value #SESSION_NOT_FOUND}: a message to a process resuming a conversation Claude Code does
 * not have; the process ends after this line.</li>
 * <li>{@value #THINKING}: a question the default model thinks about, its thinking displayed as
 * summary.</li>
 * </ul>
 */
public final class Fixtures {

	/** Directory of the fixtures, relative to this class. */
	public static final String DIR = "fixtures/";

	/** Fixture of a plain answer. */
	public static final String OK = "ok.jsonl";

	/** Fixture of an answer with a tool call. */
	public static final String TOOL = "tool.jsonl";

	/** Fixture of an interrupted answer. */
	public static final String INTERRUPT = "interrupt.jsonl";

	/** Fixture of the first answer of a resumed conversation. */
	public static final String RESUME = "resume.jsonl";

	/** Fixture of a message to a process the workspace proxy refuses. */
	public static final String LOGIN_FAILED = "login-failed.jsonl";

	/** Fixture of a message to a process resuming a conversation Claude Code does not have. */
	public static final String SESSION_NOT_FOUND = "session-not-found.jsonl";

	/** Fixture of an answer with the summary of the thinking of the model. */
	public static final String THINKING = "thinking.jsonl";

	private Fixtures() {
		// Static access only.
	}

	/**
	 * The lines of the given fixture.
	 */
	public static List<String> lines(String name) {
		try (InputStream in = Fixtures.class.getResourceAsStream(DIR + name)) {
			if (in == null) {
				throw new IllegalArgumentException("No fixture " + name + ".");
			}
			return new String(in.readAllBytes(), StandardCharsets.UTF_8).lines().filter(line -> !line.isBlank())
				.toList();
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

}
