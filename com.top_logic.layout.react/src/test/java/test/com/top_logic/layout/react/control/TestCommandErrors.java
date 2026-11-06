/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.exception.ErrorSeverity;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DummyControlScope;
import com.top_logic.layout.basic.DummyDisplayContext;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.control.CommandErrors;
import com.top_logic.layout.react.control.ErrorSink;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.Resources;
import com.top_logic.util.error.ErrorHandlingHelper;
import com.top_logic.util.error.TopLogicException;

/**
 * Tests how {@link CommandErrors#show(ErrorSink, Throwable)} reports a failed command, and the
 * {@link CommandErrors#notExecutable(ExecutableState) refusal} of a command that is not
 * executable.
 *
 * <p>
 * The {@link I18NRuntimeException#getSeverity() severity} of an exception decides which kind of
 * message the user sees, so that a command refused by its executability rule is a warning with
 * the rule's reason and not an internal error.
 * </p>
 */
public class TestCommandErrors extends TestCase {

	/** The reason an executability rule gives in the tests. */
	private static final String REASON = "No object selected in the test.";

	/** The kind of message an {@link ErrorSink} was asked to show. */
	private enum Kind {
		/** {@link ErrorSink#showError(HTMLFragment)}. */
		ERROR,
		/** {@link ErrorSink#showWarning(HTMLFragment)}. */
		WARNING,
		/** {@link ErrorSink#showInfo(HTMLFragment)}. */
		INFO;
	}

	/** One message shown to an {@link ErrorSink}. */
	private record Shown(Kind kind, HTMLFragment content) {
		// Pure value.
	}

	/** An {@link ErrorSink} recording what it is asked to show. */
	private static final class RecordingSink implements ErrorSink {

		private final List<Shown> _shown = new ArrayList<>();

		@Override
		public void showError(HTMLFragment content) {
			_shown.add(new Shown(Kind.ERROR, content));
		}

		@Override
		public void showWarning(HTMLFragment content) {
			_shown.add(new Shown(Kind.WARNING, content));
		}

		@Override
		public void showInfo(HTMLFragment content) {
			_shown.add(new Shown(Kind.INFO, content));
		}

		/** The only message shown, failing if there is not exactly one. */
		Shown single() {
			assertEquals("Exactly one message is shown.", 1, _shown.size());
			return _shown.get(0);
		}
	}

	/**
	 * Tests that a refusal with a reason is a warning, and names the refusal and the reason.
	 */
	public void testNotExecutableWithReason() {
		ResKey reason = ResKey.text(REASON);
		TopLogicException refusal = CommandErrors.notExecutable(ExecutableState.createDisabledState(reason));

		assertEquals(ErrorSeverity.WARNING, refusal.getSeverity());
		assertEquals(com.top_logic.layout.basic.I18NConstants.ERROR_COMMAND_NOT_EXECUTABLE, refusal.getErrorKey());
		assertEquals(reason, refusal.getDetails());
	}

	/**
	 * Tests that a refusal without a reason still carries the refusal title, not the
	 * internal-error fallback.
	 */
	public void testNotExecutableWithoutReason() {
		TopLogicException refusal = CommandErrors.notExecutable(
			new ExecutableState(ExecutableState.CommandVisibility.DISABLED, ResKey.NONE));

		assertEquals(ErrorSeverity.WARNING, refusal.getSeverity());
		assertEquals(com.top_logic.layout.basic.I18NConstants.ERROR_COMMAND_NOT_EXECUTABLE, refusal.getErrorKey());
		assertFalse("A refusal is no internal error.", ErrorHandlingHelper.isInternalError(refusal));
	}

	/**
	 * Tests that a refused command is shown as warning with the refusal title and the rule's
	 * reason, once, and without the internal-error title.
	 */
	public void testNotExecutableShownAsWarning() throws IOException {
		RecordingSink sink = new RecordingSink();

		CommandErrors.show(sink,
			CommandErrors.notExecutable(ExecutableState.createDisabledState(ResKey.text(REASON))));

		Shown shown = sink.single();
		assertEquals(Kind.WARNING, shown.kind());
		String html = render(shown.content());
		assertContainsOnce(html, text(com.top_logic.layout.basic.I18NConstants.ERROR_COMMAND_NOT_EXECUTABLE));
		assertContainsOnce(html, REASON);
		assertFalse("A refusal is no internal error: " + html,
			html.contains(text(com.top_logic.util.I18NConstants.INTERNAL_ERROR)));
	}

	/**
	 * Tests that the generic reasons of a hidden or disabled state count as no reason: they only
	 * repeat the title.
	 */
	public void testGenericReasonsAreNoDetail() {
		for (ExecutableState state : new ExecutableState[] { ExecutableState.NOT_EXEC_HIDDEN,
			ExecutableState.NOT_EXEC_DISABLED }) {
			TopLogicException refusal = CommandErrors.notExecutable(state);

			assertEquals(ErrorSeverity.WARNING, refusal.getSeverity());
			assertEquals(com.top_logic.layout.basic.I18NConstants.ERROR_COMMAND_NOT_EXECUTABLE,
				refusal.getErrorKey());
			assertNotSame(state.getI18NReasonKey(), refusal.getDetails());
		}
	}

	/**
	 * Tests that a hidden or disabled refusal is shown with the title only, without a detail line.
	 */
	public void testGenericReasonShownAsTitleOnly() throws IOException {
		for (ExecutableState state : new ExecutableState[] { ExecutableState.NOT_EXEC_HIDDEN,
			ExecutableState.NOT_EXEC_DISABLED }) {
			RecordingSink sink = new RecordingSink();

			CommandErrors.show(sink, CommandErrors.notExecutable(state));

			Shown shown = sink.single();
			assertEquals(Kind.WARNING, shown.kind());
			String html = render(shown.content());
			assertContainsOnce(html, text(com.top_logic.layout.basic.I18NConstants.ERROR_COMMAND_NOT_EXECUTABLE));
			assertFalse("No detail line for a generic reason: " + html, html.contains("<li"));
		}
	}

	/**
	 * Tests that a specific reason is shown as detail line below the title.
	 */
	public void testSpecificReasonShownAsDetail() throws IOException {
		RecordingSink sink = new RecordingSink();

		CommandErrors.show(sink, CommandErrors.notExecutable(ExecutableState.NO_EXEC_NO_MODEL));

		String html = render(sink.single().content());
		assertTrue("Detail line expected: " + html, html.contains("<li"));
		assertContainsOnce(html, text(ExecutableState.NO_EXEC_NO_MODEL.getI18NReasonKey()));
	}

	/**
	 * Tests that a failure with details is an error, not an internal one, and lists each detail
	 * once below its title.
	 */
	public void testErrorWithDetails() throws IOException {
		TopLogicException error = CommandErrors.error(ResKey.text("Refused."),
			List.of(ResKey.text("First problem."), ResKey.text("Second problem.")));
		assertEquals(ErrorSeverity.ERROR, error.getSeverity());
		assertFalse("A refusal is no internal error.", ErrorHandlingHelper.isInternalError(error));

		RecordingSink sink = new RecordingSink();
		CommandErrors.show(sink, error);

		Shown shown = sink.single();
		assertEquals(Kind.ERROR, shown.kind());
		String html = render(shown.content());
		assertContainsOnce(html, "Refused.");
		assertContainsOnce(html, "First problem.");
		assertContainsOnce(html, "Second problem.");
		assertTrue("The first detail is listed before the second: " + html,
			html.indexOf("First problem.") < html.indexOf("Second problem."));
	}

	/**
	 * Tests that an exception without a message for the user is shown as error under the generic
	 * title, with its technical message as detail.
	 */
	public void testInternalErrorShownWithGenericTitle() throws IOException {
		RecordingSink sink = new RecordingSink();
		CommandErrors.show(sink, new IllegalStateException("Broken invariant."));

		Shown shown = sink.single();
		assertEquals(Kind.ERROR, shown.kind());
		String html = render(shown.content());
		assertContainsOnce(html, text(I18NConstants.ERROR_COMMAND_FAILED));
		assertContainsOnce(html, "Broken invariant.");
	}

	/**
	 * Tests that an exception of {@link ErrorSeverity#INFO} severity is shown as info.
	 */
	public void testInfoShownAsInfo() {
		assertEquals(Kind.INFO, shownKind(ErrorSeverity.INFO));
	}

	/**
	 * Tests that an exception of {@link ErrorSeverity#WARNING} severity is shown as warning.
	 */
	public void testWarningShownAsWarning() {
		assertEquals(Kind.WARNING, shownKind(ErrorSeverity.WARNING));
	}

	/**
	 * Tests that a plain failure, which has {@link ErrorSeverity#ERROR} severity, is shown as
	 * error.
	 */
	public void testErrorShownAsError() {
		TopLogicException error = new TopLogicException(ResKey.text("Failed."));
		assertEquals(ErrorSeverity.ERROR, error.getSeverity());

		RecordingSink sink = new RecordingSink();
		CommandErrors.show(sink, error);

		assertEquals(Kind.ERROR, sink.single().kind());
	}

	/**
	 * Tests that a {@link ErrorSeverity#SYSTEM_FAILURE} is shown as error.
	 */
	public void testSystemFailureShownAsError() {
		assertEquals(Kind.ERROR, shownKind(ErrorSeverity.SYSTEM_FAILURE));
	}

	/**
	 * The kind of message a failure of the given severity is shown as.
	 */
	private static Kind shownKind(ErrorSeverity severity) {
		TopLogicException error = new TopLogicException(ResKey.text("Failed."));
		error.initSeverity(severity);

		RecordingSink sink = new RecordingSink();
		CommandErrors.show(sink, error);
		return sink.single().kind();
	}

	private static String text(ResKey key) {
		return Resources.getInstance().getString(key);
	}

	private static void assertContainsOnce(String html, String text) {
		int first = html.indexOf(text);
		assertTrue("Missing '" + text + "' in: " + html, first >= 0);
		assertEquals("'" + text + "' is shown more than once in: " + html, -1,
			html.indexOf(text, first + text.length()));
	}

	private static String render(HTMLFragment fragment) throws IOException {
		TagWriter out = new TagWriter();
		fragment.write(DummyDisplayContext.forScope(new DummyControlScope()), out);
		return out.toString();
	}

	/** Suite providing the resources the messages are resolved with. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestCommandErrors.class, ResourcesModule.Module.INSTANCE));
	}

}
