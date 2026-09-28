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
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DummyControlScope;
import com.top_logic.layout.basic.DummyDisplayContext;
import com.top_logic.layout.react.control.CommandErrors;
import com.top_logic.layout.react.control.ErrorSink;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.Resources;

/**
 * Tests how {@link CommandErrors#show(ErrorSink, HandlerResult)} reports a failed command, and the
 * {@link HandlerResult#notExecutable(ExecutableState) refusal} of a command that is not
 * executable.
 *
 * <p>
 * The {@link HandlerResult#getErrorSeverity() severity} of a result decides which kind of message
 * the user sees, so that a command refused by its executability rule is a warning with the
 * rule's reason and not an internal error.
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
	 * Tests that a refusal with a reason fails, is a warning, and names the refusal and the
	 * reason.
	 */
	public void testNotExecutableWithReason() {
		ResKey reason = ResKey.text(REASON);
		HandlerResult result = HandlerResult.notExecutable(ExecutableState.createDisabledState(reason));

		assertFalse("A refused command did not succeed.", result.isSuccess());
		assertEquals(ErrorSeverity.WARNING, result.getErrorSeverity());
		assertEquals(com.top_logic.layout.basic.I18NConstants.ERROR_COMMAND_NOT_EXECUTABLE, result.getErrorTitle());
		assertEquals(reason, result.getErrorMessage());
	}

	/**
	 * Tests that a refusal without a reason still fails and still carries the refusal title, not
	 * the internal-error fallback.
	 */
	public void testNotExecutableWithoutReason() {
		HandlerResult result =
			HandlerResult.notExecutable(new ExecutableState(ExecutableState.CommandVisibility.DISABLED, ResKey.NONE));

		assertFalse("A refused command did not succeed.", result.isSuccess());
		assertEquals(ErrorSeverity.WARNING, result.getErrorSeverity());
		assertEquals(com.top_logic.layout.basic.I18NConstants.ERROR_COMMAND_NOT_EXECUTABLE, result.getErrorTitle());
		assertNotSame(com.top_logic.util.I18NConstants.INTERNAL_ERROR, result.getErrorTitle());
	}

	/**
	 * Tests that a refused command is shown as warning with the refusal title and the rule's
	 * reason, once, and without the internal-error title.
	 */
	public void testNotExecutableShownAsWarning() throws IOException {
		RecordingSink sink = new RecordingSink();

		CommandErrors.show(sink,
			HandlerResult.notExecutable(ExecutableState.createDisabledState(ResKey.text(REASON))));

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
			HandlerResult result = HandlerResult.notExecutable(state);

			assertFalse("A refused command did not succeed.", result.isSuccess());
			assertEquals(ErrorSeverity.WARNING, result.getErrorSeverity());
			assertEquals(com.top_logic.layout.basic.I18NConstants.ERROR_COMMAND_NOT_EXECUTABLE,
				result.getErrorTitle());
			assertNotSame(state.getI18NReasonKey(), result.getErrorMessage());
		}
	}

	/**
	 * Tests that a hidden or disabled refusal is shown with the title only, without a detail line.
	 */
	public void testGenericReasonShownAsTitleOnly() throws IOException {
		for (ExecutableState state : new ExecutableState[] { ExecutableState.NOT_EXEC_HIDDEN,
			ExecutableState.NOT_EXEC_DISABLED }) {
			RecordingSink sink = new RecordingSink();

			CommandErrors.show(sink, HandlerResult.notExecutable(state));

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

		CommandErrors.show(sink, HandlerResult.notExecutable(ExecutableState.NO_EXEC_NO_MODEL));

		String html = render(sink.single().content());
		assertTrue("Detail line expected: " + html, html.contains("<li"));
		assertContainsOnce(html, text(ExecutableState.NO_EXEC_NO_MODEL.getI18NReasonKey()));
	}

	/**
	 * Tests that a result of {@link ErrorSeverity#INFO} severity is shown as info.
	 */
	public void testInfoShownAsInfo() {
		assertEquals(Kind.INFO, shownKind(ErrorSeverity.INFO));
	}

	/**
	 * Tests that a result of {@link ErrorSeverity#WARNING} severity is shown as warning.
	 */
	public void testWarningShownAsWarning() {
		assertEquals(Kind.WARNING, shownKind(ErrorSeverity.WARNING));
	}

	/**
	 * Tests that a plain failure, which has {@link ErrorSeverity#ERROR} severity, is still shown as
	 * error.
	 */
	public void testErrorShownAsError() {
		HandlerResult result = HandlerResult.error(ResKey.text("Failed."));
		assertEquals(ErrorSeverity.ERROR, result.getErrorSeverity());

		RecordingSink sink = new RecordingSink();
		CommandErrors.show(sink, result);

		assertEquals(Kind.ERROR, sink.single().kind());
	}

	/**
	 * Tests that a {@link ErrorSeverity#SYSTEM_FAILURE} is shown as error.
	 */
	public void testSystemFailureShownAsError() {
		assertEquals(Kind.ERROR, shownKind(ErrorSeverity.SYSTEM_FAILURE));
	}

	/**
	 * The kind of message a failed result of the given severity is shown as.
	 */
	private static Kind shownKind(ErrorSeverity severity) {
		HandlerResult result = HandlerResult.error(ResKey.text("Failed."));
		result.setErrorSeverity(severity);

		RecordingSink sink = new RecordingSink();
		CommandErrors.show(sink, result);
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
