/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.webauthn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.webauthn.ReactWebAuthnControl;
import com.top_logic.layout.react.control.webauthn.WebAuthnCeremony;
import com.top_logic.layout.react.control.webauthn.WebAuthnCredentialArguments;
import com.top_logic.layout.react.control.webauthn.WebAuthnFailureArguments;
import com.top_logic.layout.react.control.webauthn.WebAuthnListener;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Tests for {@link ReactWebAuthnControl}: that the commands of the client reach the
 * {@link WebAuthnListener} with their arguments, and that a control that is not offered refuses
 * them.
 */
public class TestReactWebAuthnControl extends TestCase {

	private static final String OPTIONS = "{\"publicKey\":{\"challenge\":\"AQ\"}}";

	private static final String CREDENTIAL = "{\"id\":\"AQ\",\"type\":\"public-key\"}";

	/**
	 * Records the calls of the listener.
	 */
	private static final class Trace implements WebAuthnListener {

		final List<String> _calls = new ArrayList<>();

		@Override
		public void onCredential(ReactContext context, String credentialJson) {
			assertNotNull(context);
			_calls.add("credential:" + credentialJson);
		}

		@Override
		public void onFailure(ReactContext context, String errorName, String message) {
			assertNotNull(context);
			_calls.add("failure:" + errorName + ":" + message);
		}
	}

	private Trace _trace;

	private ReactWebAuthnControl _control;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		ReactContext context =
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		_trace = new Trace();
		_control = new ReactWebAuthnControl(context, WebAuthnCeremony.GET, OPTIONS, "Sign in", _trace);
	}

	private HandlerResult sendCredential() {
		return _control.executeClientCommand(ReactWebAuthnControl.CMD_CREDENTIAL,
			Map.of(WebAuthnCredentialArguments.CREDENTIAL, CREDENTIAL));
	}

	private HandlerResult sendFailure(String errorName, String message) {
		Map<String, Object> arguments = new HashMap<>();
		arguments.put(WebAuthnFailureArguments.ERROR_NAME, errorName);
		if (message != null) {
			arguments.put(WebAuthnFailureArguments.MESSAGE, message);
		}
		return _control.executeClientCommand(ReactWebAuthnControl.CMD_FAILURE, arguments);
	}

	/** The credential of a completed ceremony reaches the listener unchanged. */
	public void testCredentialReachesTheListener() {
		HandlerResult result = sendCredential();

		assertTrue(result.isSuccess());
		assertEquals(List.of("credential:" + CREDENTIAL), _trace._calls);
	}

	/** A failure reaches the listener with the name and the message of the browser's error. */
	public void testFailureReachesTheListener() {
		HandlerResult result = sendFailure("NotAllowedError", "The operation was cancelled.");

		assertTrue(result.isSuccess());
		assertEquals(List.of("failure:NotAllowedError:The operation was cancelled."), _trace._calls);
	}

	/** A failure without a message reaches the listener with an empty one. */
	public void testFailureWithoutMessage() {
		sendFailure("NotSupportedError", null);

		assertEquals(List.of("failure:NotSupportedError:"), _trace._calls);
	}

	/** A disabled control accepts no outcome of a ceremony. */
	public void testDisabledControlRefusesCommands() {
		_control.setDisabled(true);

		assertFalse(sendCredential().isSuccess());
		assertFalse(sendFailure("NotAllowedError", "").isSuccess());
		assertEquals(List.of(), _trace._calls);
	}

	/** Neither does a hidden one. */
	public void testHiddenControlRefusesCommands() {
		_control.setHidden(true);

		assertFalse(sendCredential().isSuccess());
		assertEquals(List.of(), _trace._calls);
	}

	/** Enabled again, the control accepts the outcome again. */
	public void testReenabledControlAcceptsCommands() {
		_control.setDisabled(true);
		_control.setDisabled(false);

		assertTrue(sendCredential().isSuccess());
		assertEquals(1, _trace._calls.size());
	}

	/** The owner replaces the options and sets the error message for the next attempt. */
	public void testOwnerSuppliesFreshOptionsAndError() {
		assertEquals(OPTIONS, _control.getOptions());

		String fresh = "{\"publicKey\":{\"challenge\":\"Ag\"}}";
		_control.setOptions(fresh);
		_control.setError("Try again.");

		assertEquals(fresh, _control.getOptions());
		assertEquals("Try again.", _control.getError());
		assertTrue(_control.stateAsJSON().contains("Try again."));
	}

	/** A ceremony answers a one-time challenge, so the recorder captures none of its commands. */
	public void testCommandsAreNotRecorded() {
		assertFalse(_control.isRecordable(ReactWebAuthnControl.CMD_CREDENTIAL));
		assertFalse(_control.isRecordable(ReactWebAuthnControl.CMD_FAILURE));
	}

}
