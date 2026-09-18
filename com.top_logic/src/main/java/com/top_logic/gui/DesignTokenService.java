/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.gui;

import java.util.Collection;
import java.util.Collections;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;

/**
 * The design tokens the user interface emits as CSS custom properties.
 *
 * <p>
 * The tokens are the vocabulary a configuration names a color, a length, or another design value
 * from. A configuration that names a token is checked against this vocabulary, so a name the user
 * interface does not emit is reported where it is written instead of silently displaying nothing.
 * </p>
 *
 * <p>
 * The names come from the registry of custom properties the user interface writes into its
 * stylesheets. {@link ThemeDesignTokens} answers those of the theme settings; a user interface
 * layer holding a registry of its own takes the place of that implementation.
 * </p>
 *
 * <p>
 * The service answering no names for a {@link DesignTokenKind} means that the application declares
 * no token vocabulary of that kind. Nothing is then checked: every name is accepted, because there
 * is nothing to tell a valid name from an invalid one. This is the answer of this implementation
 * for every kind, and the answer of every implementation for a kind no registry supplies.
 * </p>
 */
@Label("Design tokens")
public class DesignTokenService extends ManagedClass {

	/**
	 * Creates a {@link DesignTokenService} from the given configuration.
	 *
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link DesignTokenService}.
	 */
	public DesignTokenService(InstantiationContext context, ManagedClass.ServiceConfiguration<?> config) {
		super(context, config);
	}

	/**
	 * The names of the design tokens of the given kind that the user interface emits.
	 *
	 * @param kind
	 *        The kind of value the tokens hold.
	 * @return The token names, each without the leading <code>--</code> of the CSS custom property
	 *         the token is emitted as. Empty, if no vocabulary of the given kind is declared.
	 */
	public Collection<String> getTokenNames(DesignTokenKind kind) {
		return Collections.emptySet();
	}

	/**
	 * The {@link #getTokenNames(DesignTokenKind)} of the started {@link DesignTokenService}.
	 *
	 * <p>
	 * Answers no names while the service is not started, which is the state of an application whose
	 * user interface emits no design tokens.
	 * </p>
	 *
	 * @param kind
	 *        The kind of value the tokens hold.
	 * @return The token names, empty if no vocabulary of the given kind is declared.
	 */
	public static Collection<String> tokenNames(DesignTokenKind kind) {
		Module module = Module.INSTANCE;
		if (!module.isActive()) {
			return Collections.emptySet();
		}
		return module.getImplementationInstance().getTokenNames(kind);
	}

	/**
	 * Module definition for the {@link DesignTokenService}.
	 */
	public static class Module extends TypedRuntimeModule<DesignTokenService> {

		/** Singleton for this module. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<DesignTokenService> getImplementation() {
			return DesignTokenService.class;
		}

	}

}
