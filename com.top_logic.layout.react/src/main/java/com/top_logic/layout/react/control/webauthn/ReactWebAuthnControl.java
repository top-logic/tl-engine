/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.webauthn;

import java.util.LinkedHashSet;
import java.util.Set;

import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.ReactImages;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.ButtonAppearance;
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
 * {@link #AUTO_START}, {@link #DISABLED}, {@link #ERROR}.
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

	/** State key for the error message shown below the button. */
	public static final String ERROR = "error";

	/** Command sent with the credential of a completed ceremony, see {@link WebAuthnCredentialArguments}. */
	public static final String CMD_CREDENTIAL = "credential";

	/** Command sent for a failed or cancelled ceremony, see {@link WebAuthnFailureArguments}. */
	public static final String CMD_FAILURE = "failure";

	private final WebAuthnListener _listener;

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
	 * Sets the error message shown below the button; {@code null} to remove it.
	 *
	 * @param message
	 *        The localized message for the user.
	 */
	public void setError(String message) {
		putState(ERROR, message);
	}

	/**
	 * The error message shown below the button, {@code null} if there is none.
	 */
	public String getError() {
		return (String) getState(ERROR);
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
		return result;
	}

}
