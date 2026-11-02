/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.webauthn;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.json.JSON;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.ReactImages;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.ButtonAppearance;
import com.top_logic.layout.react.protocol.FunctionCall;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutableState;

/**
 * A button that runs a Web Authentication ceremony in the browser.
 *
 * <p>
 * On the client side, this control renders a {@code TLWebAuthn} React component. A click on its
 * button calls {@code navigator.credentials.create()} (for {@link WebAuthnCeremony#CREATE}) or
 * {@code navigator.credentials.get()} (for {@link WebAuthnCeremony#GET}) with the
 * {@link #setOptions(String) options} of this control, directly in the click handler, so the browser
 * sees the user's gesture. The resulting credential arrives with the {@link #CMD_CREDENTIAL} command,
 * a failure or cancellation with the {@link #CMD_FAILURE} command; both are handed to the
 * {@link WebAuthnListener} of the control.
 * </p>
 *
 * <p>
 * The client renders the button alone, as a plain button that takes its appearance from its
 * container like every other button. The control shows no error of its own: the
 * {@link WebAuthnListener} tells the user about a failure, through the messages of the window. A
 * reason why the button cannot be used is shown as its {@link #setTooltip(String) tooltip}.
 * </p>
 *
 * <p>
 * A page that cannot run a ceremony at all - one that is no secure context (served by {@code http}
 * from a host other than {@code localhost}), where browsers hide the Web Authentication API, or a
 * browser without that API - is detected by the client before any click: it shows the button
 * disabled and reports the cause once, when it is displayed, with the {@link #CMD_UNAVAILABLE}
 * command and the error name {@link #INSECURE_CONTEXT_ERROR} or {@link #WEB_AUTHN_UNAVAILABLE_ERROR}.
 * The command reaches {@link WebAuthnListener#onUnavailable(ReactContext, String, String)}, whose
 * owner explains the cause in the tooltip.
 * </p>
 *
 * <p>
 * When it is displayed, the client reports the origin of the page once with the {@link #CMD_ORIGIN}
 * command. The control keeps it as {@link #getPageOrigin()} and hands it to
 * {@link WebAuthnListener#onPageOrigin(ReactContext, String)}, whose owner checks it against the
 * origins its relying party accepts before offering a ceremony.
 * </p>
 *
 * <p>
 * The owner can tell the browser which credentials of its relying party are no longer valid, so
 * that the browser or the password manager removes or hides them:
 * {@link #signalUnknownCredential(String, String)} and
 * {@link #signalAllAcceptedCredentials(String, String, Collection)} call the corresponding
 * functions of the Web Authentication Signal API ({@code PublicKeyCredential.signalUnknownCredential()},
 * {@code PublicKeyCredential.signalAllAcceptedCredentials()}). A signal is a one-time
 * {@link FunctionCall} event to the client function {@link #CLIENT_FUNCTION}, not a part of the
 * state; it is sent whatever the state of the button, also while no ceremony runs. A browser
 * without the Signal API ignores it.
 * </p>
 *
 * <p>
 * Options with a {@code publicKey.timeout} expire: shortly before, while no ceremony runs, the
 * client asks for fresh ones with the {@link #CMD_REFRESH} command, so that a button left alone
 * for a long time still runs a ceremony the server accepts. The command reaches
 * {@link WebAuthnListener#onExpired(ReactContext)}, whose owner supplies fresh options with
 * {@link #setOptions(String)}.
 * </p>
 *
 * <p>
 * The control is generic: it neither creates the options nor verifies the credential, and keeps no
 * challenge. The options are the JSON a Web Authentication server library produces for the browser, an object
 * with the property {@code publicKey} holding the {@code PublicKeyCredentialCreationOptionsJSON} or
 * {@code PublicKeyCredentialRequestOptionsJSON} (binary values base64url-encoded). Further top-level
 * properties beside {@code publicKey} (e.g. {@code mediation}) are passed to the browser call as they
 * are.
 * </p>
 *
 * <p>
 * State keys: {@link #KIND}, {@link #OPTIONS}, {@link #LABEL}, {@link #IMAGE}, {@link #APPEARANCE},
 * {@link #AUTO_START}, {@link #DISABLED}, {@link #TOOLTIP}.
 * </p>
 *
 * <p>
 * The commands are not recorded by the script recorder: a ceremony answers a one-time challenge, so
 * a recorded credential can never be replayed.
 * </p>
 */
public class ReactWebAuthnControl extends ReactControl {

	/** The React component rendering this control. */
	private static final String REACT_MODULE = "TLWebAuthn";

	/**
	 * State key for the {@link WebAuthnCeremony#getExternalName() external name} of the ceremony
	 * kind.
	 */
	public static final String KIND = "kind";

	/** State key for the options JSON text, see {@link #setOptions(String)}. */
	public static final String OPTIONS = "options";

	/** State key for the label of the button. */
	public static final String LABEL = "label";

	/** State key for the encoded icon of the button. */
	public static final String IMAGE = "image";

	/** State key for the {@link ButtonAppearance#getExternalName() appearance} of the button. */
	public static final String APPEARANCE = "appearance";

	/** State key for whether the ceremony starts as soon as the control is displayed. */
	public static final String AUTO_START = "autoStart";

	/** State key for whether the button is disabled, e.g. while the owner processes a credential. */
	public static final String DISABLED = "disabled";

	/** State key for the tooltip of the button, see {@link #setTooltip(String)}. */
	public static final String TOOLTIP = "tooltip";

	/** Command sent with the credential of a completed ceremony, see {@link WebAuthnCredentialArguments}. */
	public static final String CMD_CREDENTIAL = "credential";

	/** Command sent for a failed or cancelled ceremony, see {@link WebAuthnFailureArguments}. */
	public static final String CMD_FAILURE = "failure";

	/**
	 * Command sent once when the client is displayed on a page that cannot run a ceremony, see
	 * {@link WebAuthnFailureArguments}.
	 */
	public static final String CMD_UNAVAILABLE = "unavailable";

	/**
	 * Error name of {@link #CMD_UNAVAILABLE} and {@link #CMD_FAILURE} for a page that is no secure
	 * context: browsers offer Web Authentication only on pages served by {@code https} or from
	 * {@code localhost}.
	 */
	public static final String INSECURE_CONTEXT_ERROR = "InsecureContextError";

	/**
	 * Error name of {@link #CMD_UNAVAILABLE} and {@link #CMD_FAILURE} for a browser that offers no
	 * Web Authentication API on a secure page.
	 */
	public static final String WEB_AUTHN_UNAVAILABLE_ERROR = "WebAuthnUnavailableError";

	/** Command sent shortly before the {@link #setOptions(String) options} expire, without arguments. */
	public static final String CMD_REFRESH = "refresh";

	/**
	 * Command sent once when the client is displayed, with the origin of its page, see
	 * {@link WebAuthnOriginArguments}.
	 */
	public static final String CMD_ORIGIN = "origin";

	/** The global object of the client holding {@link #CLIENT_FUNCTION}. */
	public static final String CLIENT_NAMESPACE = "TLReact";

	/**
	 * The client function receiving a signal: called with the name of the signal (e.g.
	 * {@link #SIGNAL_UNKNOWN_CREDENTIAL}) and its options object.
	 */
	public static final String CLIENT_FUNCTION = "webAuthnSignal";

	/**
	 * Name of the signal that one credential is unknown to the relying party, the name of the
	 * function {@code PublicKeyCredential.signalUnknownCredential()}; options {@link #RP_ID} and
	 * {@link #CREDENTIAL_ID}.
	 *
	 * @see #signalUnknownCredential(String, String)
	 */
	public static final String SIGNAL_UNKNOWN_CREDENTIAL = "signalUnknownCredential";

	/**
	 * Name of the signal listing all credentials the relying party accepts for a user, the name of
	 * the function {@code PublicKeyCredential.signalAllAcceptedCredentials()}; options
	 * {@link #RP_ID}, {@link #USER_ID} and {@link #ALL_ACCEPTED_CREDENTIAL_IDS}.
	 *
	 * @see #signalAllAcceptedCredentials(String, String, Collection)
	 */
	public static final String SIGNAL_ALL_ACCEPTED_CREDENTIALS = "signalAllAcceptedCredentials";

	/** Option of a signal: the ID of the relying party. */
	public static final String RP_ID = "rpId";

	/** Option of {@link #SIGNAL_UNKNOWN_CREDENTIAL}: the base64url-encoded credential ID. */
	public static final String CREDENTIAL_ID = "credentialId";

	/** Option of {@link #SIGNAL_ALL_ACCEPTED_CREDENTIALS}: the base64url-encoded user handle. */
	public static final String USER_ID = "userId";

	/**
	 * Option of {@link #SIGNAL_ALL_ACCEPTED_CREDENTIALS}: the list of the base64url-encoded IDs of
	 * all credentials the relying party accepts for the user.
	 */
	public static final String ALL_ACCEPTED_CREDENTIAL_IDS = "allAcceptedCredentialIds";

	private final WebAuthnListener _listener;

	/** The origin the client reported with {@link #CMD_ORIGIN}, {@code null} before. */
	private String _pageOrigin;

	/**
	 * Creates a {@link ReactWebAuthnControl}.
	 *
	 * @param context
	 *        The context of the control.
	 * @param ceremony
	 *        The kind of ceremony the button runs.
	 * @param optionsJson
	 *        The options of the ceremony, see {@link #setOptions(String)}; {@code null} if they are
	 *        supplied later.
	 * @param label
	 *        The label of the button.
	 * @param listener
	 *        Receives the outcome of the ceremony.
	 */
	public ReactWebAuthnControl(ReactContext context, WebAuthnCeremony ceremony, String optionsJson, String label,
			WebAuthnListener listener) {
		super(context, null, REACT_MODULE);
		_listener = listener;
		putState(KIND, ceremony.getExternalName());
		putState(OPTIONS, optionsJson);
		putState(LABEL, label);
	}

	/**
	 * Supplies the options of the next ceremony, e.g. fresh options with a new challenge after a
	 * failed attempt.
	 *
	 * @param optionsJson
	 *        The JSON text of an object with the property {@code publicKey}, as produced by a
	 *        Web Authentication server library for the browser; {@code null} disables the ceremony until options
	 *        are supplied.
	 */
	public void setOptions(String optionsJson) {
		putState(OPTIONS, optionsJson);
	}

	/**
	 * The options of the next ceremony, see {@link #setOptions(String)}.
	 */
	public String getOptions() {
		return (String) getState(OPTIONS);
	}

	/**
	 * Sets the label of the button.
	 */
	public void setLabel(String label) {
		putState(LABEL, label);
	}

	/**
	 * Sets the icon of the button; {@code null} for a button without icon.
	 */
	public void setImage(ThemeImage image) {
		putState(IMAGE, image == null ? null : ReactImages.encode(getReactContext(), image));
	}

	/**
	 * Sets the appearance of the button; {@code null} for the default the container suggests.
	 */
	public void setAppearance(ButtonAppearance appearance) {
		putState(APPEARANCE,
			appearance == null || appearance == ButtonAppearance.DEFAULT ? null : appearance.getExternalName());
	}

	/**
	 * Whether the ceremony starts as soon as the control is displayed, without a click.
	 *
	 * <p>
	 * Only the first display starts it, not a later {@link #setOptions(String) change of the options},
	 * so a failed automatic attempt is not repeated without the user. Browsers that require a user
	 * gesture for the ceremony refuse the automatic start with {@code NotAllowedError}, which arrives
	 * as {@link #CMD_FAILURE}; the button stays available for a manual start.
	 * </p>
	 */
	public void setAutoStart(boolean autoStart) {
		putState(AUTO_START, autoStart ? Boolean.TRUE : null);
	}

	/**
	 * Disables the button, e.g. while the owner processes a credential.
	 *
	 * <p>
	 * A disabled control refuses the {@link #CMD_CREDENTIAL} and {@link #CMD_FAILURE} commands.
	 * </p>
	 */
	public void setDisabled(boolean disabled) {
		putState(DISABLED, disabled ? Boolean.TRUE : null);
	}

	/**
	 * Whether the button is {@link #setDisabled(boolean) disabled}.
	 */
	public boolean isDisabled() {
		return Boolean.TRUE.equals(getState(DISABLED));
	}

	/**
	 * Sets the tooltip of the button, e.g. why it cannot be used; {@code null} for none.
	 *
	 * @param tooltip
	 *        The localized text for the user.
	 */
	public void setTooltip(String tooltip) {
		putState(TOOLTIP, tooltip);
	}

	/**
	 * The tooltip of the button, see {@link #setTooltip(String)}.
	 */
	public String getTooltip() {
		return (String) getState(TOOLTIP);
	}

	/**
	 * Handles the credential of a completed ceremony.
	 */
	@ReactCommandHandler(CMD_CREDENTIAL)
	HandlerResult handleCredential(ReactContext context, WebAuthnCredentialArguments args) {
		ExecutableState offered = offeredState();
		if (!offered.isExecutable()) {
			return HandlerResult.notExecutable(offered);
		}
		_listener.onCredential(context, args.getCredential());
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * The origin of the page that displays this control, as the client reported it with
	 * {@link #CMD_ORIGIN}; {@code null} as long as the client has not reported it.
	 *
	 * @see WebAuthnListener#onPageOrigin(ReactContext, String)
	 */
	public String getPageOrigin() {
		return _pageOrigin;
	}

	/**
	 * Tells the browser that a credential is unknown to the relying party, e.g. because it was
	 * deleted on the server or its registration was refused after the authenticator had stored it.
	 *
	 * <p>
	 * The browser calls {@code PublicKeyCredential.signalUnknownCredential()}, so that the
	 * authenticator or password manager may remove the credential. A browser without that function
	 * ignores the signal.
	 * </p>
	 *
	 * @param rpId
	 *        The ID of the relying party the credential belongs to; it must match the page.
	 * @param credentialId
	 *        The base64url-encoded ID of the credential.
	 */
	public void signalUnknownCredential(String rpId, String credentialId) {
		Map<String, Object> options = new LinkedHashMap<>();
		options.put(RP_ID, rpId);
		options.put(CREDENTIAL_ID, credentialId);
		signal(SIGNAL_UNKNOWN_CREDENTIAL, options);
	}

	/**
	 * Tells the browser which credentials the relying party accepts for a user.
	 *
	 * <p>
	 * The browser calls {@code PublicKeyCredential.signalAllAcceptedCredentials()}, so that the
	 * authenticator or password manager may remove or hide the credentials of this user that are
	 * not listed. A browser without that function ignores the signal.
	 * </p>
	 *
	 * @param rpId
	 *        The ID of the relying party; it must match the page.
	 * @param userId
	 *        The base64url-encoded user handle the credentials were registered for.
	 * @param allAcceptedCredentialIds
	 *        The base64url-encoded IDs of all credentials of the user the relying party accepts.
	 */
	public void signalAllAcceptedCredentials(String rpId, String userId, Collection<String> allAcceptedCredentialIds) {
		Map<String, Object> options = new LinkedHashMap<>();
		options.put(RP_ID, rpId);
		options.put(USER_ID, userId);
		options.put(ALL_ACCEPTED_CREDENTIAL_IDS, new ArrayList<>(allAcceptedCredentialIds));
		signal(SIGNAL_ALL_ACCEPTED_CREDENTIALS, options);
	}

	/**
	 * Sends a one-time signal to the browser of this control's window: a call of the client
	 * function {@link #CLIENT_FUNCTION} with the name of the signal and its options.
	 */
	private void signal(String name, Map<String, Object> options) {
		SSEUpdateQueue queue = getReactContext().getSSEQueue();
		if (queue == null) {
			return;
		}
		queue.enqueue(FunctionCall.create()
			.setFunctionRef(CLIENT_NAMESPACE)
			.setFunctionName(CLIENT_FUNCTION)
			.setArguments(JSON.toString(List.of(name, options))));
	}

	/**
	 * Handles the report of the client of the origin of its page.
	 *
	 * <p>
	 * Accepted also while the control is {@link #setDisabled(boolean) disabled} or hidden, since it
	 * describes the page, not an answer to a ceremony.
	 * </p>
	 */
	@ReactCommandHandler(value = CMD_ORIGIN, technical = true)
	HandlerResult handleOrigin(ReactContext context, WebAuthnOriginArguments args) {
		_pageOrigin = args.getOrigin();
		_listener.onPageOrigin(context, _pageOrigin);
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Handles a failed or cancelled ceremony.
	 */
	@ReactCommandHandler(CMD_FAILURE)
	HandlerResult handleFailure(ReactContext context, WebAuthnFailureArguments args) {
		ExecutableState offered = offeredState();
		if (!offered.isExecutable()) {
			return HandlerResult.notExecutable(offered);
		}
		String message = args.getMessage();
		_listener.onFailure(context, args.getErrorName(), message == null ? "" : message);
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Handles the report of the client that the page cannot run a ceremony.
	 *
	 * <p>
	 * Accepted also while the control is {@link #setDisabled(boolean) disabled}, since it describes
	 * the page, not an answer to a ceremony.
	 * </p>
	 */
	@ReactCommandHandler(value = CMD_UNAVAILABLE, technical = true)
	HandlerResult handleUnavailable(ReactContext context, WebAuthnFailureArguments args) {
		String message = args.getMessage();
		_listener.onUnavailable(context, args.getErrorName(), message == null ? "" : message);
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Handles the request of the client for fresh options, because the current ones are about to
	 * expire.
	 */
	@ReactCommandHandler(value = CMD_REFRESH, technical = true)
	HandlerResult handleRefresh(ReactContext context) {
		ExecutableState offered = offeredState();
		if (!offered.isExecutable()) {
			return HandlerResult.notExecutable(offered);
		}
		_listener.onExpired(context);
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Whether the button is offered to the user, and if not, why: a hidden or disabled control
	 * accepts no outcome of a ceremony, whatever the client sends.
	 */
	private ExecutableState offeredState() {
		if (isHidden()) {
			return ExecutableState.NOT_EXEC_HIDDEN;
		}
		if (isDisabled()) {
			return ExecutableState.NOT_EXEC_DISABLED;
		}
		return ExecutableState.EXECUTABLE;
	}

	@Override
	public Set<String> nonRecordableCommands() {
		Set<String> result = new LinkedHashSet<>(super.nonRecordableCommands());
		result.add(CMD_CREDENTIAL);
		result.add(CMD_FAILURE);
		result.add(CMD_REFRESH);
		result.add(CMD_UNAVAILABLE);
		result.add(CMD_ORIGIN);
		return result;
	}

}
