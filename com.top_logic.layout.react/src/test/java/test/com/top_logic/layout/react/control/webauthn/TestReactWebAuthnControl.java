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

import com.top_logic.basic.json.JSON;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.webauthn.ReactWebAuthnControl;
import com.top_logic.layout.react.control.webauthn.WebAuthnCeremony;
import com.top_logic.layout.react.control.webauthn.WebAuthnCredentialArguments;
import com.top_logic.layout.react.control.webauthn.WebAuthnFailureArguments;
import com.top_logic.layout.react.control.webauthn.WebAuthnListener;
import com.top_logic.layout.react.control.webauthn.WebAuthnOriginArguments;
import com.top_logic.layout.react.protocol.FunctionCall;
import com.top_logic.layout.react.protocol.SSEEvent;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Tests for {@link ReactWebAuthnControl}: that the commands of the client reach the
 * {@link WebAuthnListener} with their arguments, that a control that is not offered refuses them,
 * and that the signals of the owner reach the client as calls of its signal function.
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

		@Override
		public void onExpired(ReactContext context) {
			assertNotNull(context);
			_calls.add("expired");
		}

		@Override
		public void onUnavailable(ReactContext context, String errorName, String message) {
			assertNotNull(context);
			_calls.add("unavailable:" + errorName + ":" + message);
		}

		@Override
		public void onPageOrigin(ReactContext context, String origin) {
			assertNotNull(context);
			_calls.add("origin:" + origin);
		}
	}

	/**
	 * Keeps the events handed to the queue, as the client would receive them.
	 */
	private static final class RecordingQueue extends SSEUpdateQueue {

		final List<SSEEvent> _events = new ArrayList<>();

		@Override
		public void enqueue(SSEEvent event) {
			_events.add(event);
			super.enqueue(event);
		}

		/** The function calls among the events. */
		List<FunctionCall> functionCalls() {
			List<FunctionCall> result = new ArrayList<>();
			for (SSEEvent event : _events) {
				if (event instanceof FunctionCall call) {
					result.add(call);
				}
			}
			return result;
		}
	}

	private RecordingQueue _queue;

	private Trace _trace;

	private ReactWebAuthnControl _control;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_queue = new RecordingQueue();
		ReactContext context =
			new DefaultReactContext("", "test", _queue, new ReactWindowRegistry("test"));
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

	private HandlerResult sendUnavailable(String errorName) {
		return _control.executeClientCommand(ReactWebAuthnControl.CMD_UNAVAILABLE,
			Map.of(WebAuthnFailureArguments.ERROR_NAME, errorName));
	}

	private HandlerResult sendOrigin(String origin) {
		return _control.executeClientCommand(ReactWebAuthnControl.CMD_ORIGIN,
			Map.of(WebAuthnOriginArguments.ORIGIN, origin));
	}

	/**
	 * The single function call the owner's signal produced, as the arguments the client function
	 * {@link ReactWebAuthnControl#CLIENT_FUNCTION} receives.
	 */
	private List<?> signalArguments() throws JSON.ParseException {
		List<FunctionCall> calls = _queue.functionCalls();
		assertEquals(1, calls.size());
		FunctionCall call = calls.get(0);
		assertEquals(ReactWebAuthnControl.CLIENT_NAMESPACE, call.getFunctionRef());
		assertEquals(ReactWebAuthnControl.CLIENT_FUNCTION, call.getFunctionName());
		return (List<?>) JSON.fromString(call.getArguments());
	}

	private HandlerResult sendRefresh() {
		return _control.executeClientCommand(ReactWebAuthnControl.CMD_REFRESH, Map.of());
	}

	/** The request of the client for fresh options reaches the listener. */
	public void testRefreshReachesTheListener() {
		HandlerResult result = sendRefresh();

		assertTrue(result.isSuccess());
		assertEquals(List.of("expired"), _trace._calls);
	}

	/** A listener that does not handle expiry ignores the request for fresh options. */
	public void testRefreshIgnoredByDefault() {
		WebAuthnListener listener = new WebAuthnListener() {
			@Override
			public void onCredential(ReactContext context, String credentialJson) {
				fail("No credential sent.");
			}

			@Override
			public void onFailure(ReactContext context, String errorName, String message) {
				fail("No failure sent.");
			}
		};
		ReactWebAuthnControl control = new ReactWebAuthnControl(
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test")),
			WebAuthnCeremony.GET, OPTIONS, "Sign in", listener);

		assertTrue(control.executeClientCommand(ReactWebAuthnControl.CMD_REFRESH, Map.of()).isSuccess());
		assertEquals(OPTIONS, control.getOptions());
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
		assertFalse(sendRefresh().isSuccess());
		assertEquals(List.of(), _trace._calls);
	}

	/** Neither does a hidden one. */
	public void testHiddenControlRefusesCommands() {
		_control.setHidden(true);

		assertFalse(sendCredential().isSuccess());
		assertFalse(sendRefresh().isSuccess());
		assertEquals(List.of(), _trace._calls);
	}

	/** Enabled again, the control accepts the outcome again. */
	public void testReenabledControlAcceptsCommands() {
		_control.setDisabled(true);
		_control.setDisabled(false);

		assertTrue(sendCredential().isSuccess());
		assertEquals(1, _trace._calls.size());
	}

	/** A page that cannot run a ceremony reaches the listener, also while the control is disabled. */
	public void testUnavailableReachesTheListener() {
		_control.setDisabled(true);

		assertTrue(sendUnavailable(ReactWebAuthnControl.INSECURE_CONTEXT_ERROR).isSuccess());
		assertEquals(List.of("unavailable:" + ReactWebAuthnControl.INSECURE_CONTEXT_ERROR + ":"), _trace._calls);
	}

	/** The owner replaces the options and explains the button in its tooltip; there is no error state. */
	public void testOwnerSuppliesFreshOptionsAndTooltip() {
		assertEquals(OPTIONS, _control.getOptions());

		String fresh = "{\"publicKey\":{\"challenge\":\"Ag\"}}";
		_control.setOptions(fresh);
		_control.setTooltip("Open via HTTPS.");

		assertEquals(fresh, _control.getOptions());
		assertEquals("Open via HTTPS.", _control.getTooltip());
		String state = _control.stateAsJSON();
		assertTrue(state, state.contains(ReactWebAuthnControl.TOOLTIP));
		assertTrue(state, state.contains("Open via HTTPS."));
	}

	/** The origin of the page reaches the listener and stays available, also on a disabled control. */
	public void testOriginReachesTheListener() {
		assertNull(_control.getPageOrigin());
		_control.setDisabled(true);

		assertTrue(sendOrigin("https://app.example.com").isSuccess());

		assertEquals(List.of("origin:https://app.example.com"), _trace._calls);
		assertEquals("https://app.example.com", _control.getPageOrigin());
	}

	/** A listener that does not check the origin ignores it; the control keeps it anyway. */
	public void testOriginIgnoredByDefault() {
		WebAuthnListener listener = new WebAuthnListener() {
			@Override
			public void onCredential(ReactContext context, String credentialJson) {
				fail("No credential sent.");
			}

			@Override
			public void onFailure(ReactContext context, String errorName, String message) {
				fail("No failure sent.");
			}
		};
		ReactWebAuthnControl control = new ReactWebAuthnControl(
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test")),
			WebAuthnCeremony.GET, OPTIONS, "Sign in", listener);
		control.setHidden(true);

		assertTrue(control.executeClientCommand(ReactWebAuthnControl.CMD_ORIGIN,
			Map.of(WebAuthnOriginArguments.ORIGIN, "http://localhost:8080")).isSuccess());
		assertEquals("http://localhost:8080", control.getPageOrigin());
	}

	/** An unknown credential is signalled as a call of the client function, not as state. */
	public void testSignalUnknownCredential() throws JSON.ParseException {
		String stateBefore = _control.stateAsJSON();

		_control.signalUnknownCredential("app.example.com", "AQID");

		assertEquals(List.of(ReactWebAuthnControl.SIGNAL_UNKNOWN_CREDENTIAL,
			Map.of(ReactWebAuthnControl.RP_ID, "app.example.com", ReactWebAuthnControl.CREDENTIAL_ID, "AQID")),
			signalArguments());
		assertEquals(stateBefore, _control.stateAsJSON());
	}

	/** The accepted credentials of a user are signalled with all their IDs, also on a disabled button. */
	public void testSignalAllAcceptedCredentials() throws JSON.ParseException {
		_control.setDisabled(true);

		_control.signalAllAcceptedCredentials("app.example.com", "dXNlcg", List.of("AQ", "Ag"));

		assertEquals(List.of(ReactWebAuthnControl.SIGNAL_ALL_ACCEPTED_CREDENTIALS,
			Map.of(ReactWebAuthnControl.RP_ID, "app.example.com", ReactWebAuthnControl.USER_ID, "dXNlcg",
				ReactWebAuthnControl.ALL_ACCEPTED_CREDENTIAL_IDS, List.of("AQ", "Ag"))),
			signalArguments());
	}

	/** Each signal is sent once: a second signal is a second call, the first is not repeated. */
	public void testSignalsAreOneShot() {
		_control.signalUnknownCredential("app.example.com", "AQ");
		_control.signalUnknownCredential("app.example.com", "Ag");

		List<FunctionCall> calls = _queue.functionCalls();
		assertEquals(2, calls.size());
		assertTrue(calls.get(0).getArguments(), calls.get(0).getArguments().contains("\"AQ\""));
		assertTrue(calls.get(1).getArguments(), calls.get(1).getArguments().contains("\"Ag\""));
	}

	/** A ceremony answers a one-time challenge, so the recorder captures none of its commands. */
	public void testCommandsAreNotRecorded() {
		assertFalse(_control.isRecordable(ReactWebAuthnControl.CMD_CREDENTIAL));
		assertFalse(_control.isRecordable(ReactWebAuthnControl.CMD_FAILURE));
		assertFalse(_control.isRecordable(ReactWebAuthnControl.CMD_REFRESH));
		assertFalse(_control.isRecordable(ReactWebAuthnControl.CMD_UNAVAILABLE));
		assertFalse(_control.isRecordable(ReactWebAuthnControl.CMD_ORIGIN));
	}

}
