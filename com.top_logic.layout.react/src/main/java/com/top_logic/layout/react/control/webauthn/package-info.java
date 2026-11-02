/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

/**
 * Web Authentication ceremonies for the React integration.
 *
 * <p>
 * Provides {@link com.top_logic.layout.react.control.webauthn.ReactWebAuthnControl}, a button that
 * runs a {@code navigator.credentials.create()} or {@code navigator.credentials.get()} ceremony in
 * the browser with options supplied by the server, and hands the resulting credential (or the
 * failure) to a {@link com.top_logic.layout.react.control.webauthn.WebAuthnListener}. The control
 * knows nothing about what the credential is used for; creating the options and verifying the
 * credential is the job of its owner.
 * </p>
 */
package com.top_logic.layout.react.control.webauthn;
