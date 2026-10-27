/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin.system;

import java.util.List;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.credential.SecretReference;
import com.top_logic.basic.credential.SecretReferences;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.provider.label.I18NClassNameProvider;
import com.top_logic.model.search.expr.config.operations.AdminOnly;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;
import com.top_logic.util.Resources;

/**
 * TL-Script functions about the secrets the service configurations of the application refer to:
 * what a view checking the secrets of an installation displays of them.
 *
 * <p>
 * {@link #references()} reads the secrets and checks each of them once, the other functions
 * describe one of these {@link SecretReference}s, e.g. the row of a table. None of them reveals a
 * secret. Every function describing a reference accepts no reference at all and answers with
 * nothing.
 * </p>
 *
 * @implNote Every public static method here is a TL-Script function named by the
 *           {@link ScriptPrefix} of this class followed by the capitalized method name, so
 *           {@link #references()} is called <code>secretReferences</code>. The functions read the
 *           configuration of the services, which only an administrator may inspect from a script
 *           entered interactively.
 */
@ScriptPrefix(SecretFunctions.PREFIX)
@AdminOnly
public class SecretFunctions extends TLScriptFunctions {

	/** Prefix all function names of this class start with. */
	public static final String PREFIX = "secret";

	/**
	 * The secrets in the configurations of all running services, each checked now whether it is
	 * available.
	 *
	 * @return The references to the secrets, a snapshot: the status of a reference is the one
	 *         found when it was read.
	 *
	 * @see SecretReferences#ofServices()
	 */
	@Label("Secrets of the services")
	public static List<SecretReference> references() {
		return SecretReferences.ofServices();
	}

	/**
	 * The label of the service in whose configuration the given secret is configured.
	 *
	 * @param reference
	 *        The reference to the secret.
	 * @return The label of the service, <code>null</code> without a reference.
	 */
	@Label("Service of a secret")
	public static String service(SecretReference reference) {
		if (reference == null) {
			return null;
		}
		BasicRuntimeModule<?> service = reference.getService();
		return service == null ? null : ServiceSnapshot.label(service);
	}

	/**
	 * The path of the property holding the given secret inside the configuration of its service.
	 *
	 * @param reference
	 *        The reference to the secret.
	 * @return The property names leading to the secret, <code>null</code> without a reference.
	 *
	 * @see SecretReference#getPath()
	 */
	@Label("Property of a secret")
	public static String path(SecretReference reference) {
		return reference == null ? null : reference.getPath();
	}

	/**
	 * The kind of source delivering the given secret.
	 *
	 * @param reference
	 *        The reference to the secret.
	 * @return The label of the implementation of the secret source, <code>null</code> without a
	 *         reference.
	 */
	@Label("Source of a secret")
	public static String kind(SecretReference reference) {
		return reference == null ? null : I18NClassNameProvider.INSTANCE.getLabel(reference.getKind());
	}

	/**
	 * How the source of the given secret identifies it, e.g. the name of the secret, the variable
	 * or the file.
	 *
	 * @param reference
	 *        The reference to the secret.
	 * @return The description, <code>null</code> without a reference or if the source cannot tell.
	 *
	 * @see SecretReference#getReference()
	 */
	@Label("Reference of a secret")
	public static String reference(SecretReference reference) {
		return reference == null ? null : text(reference.getReference());
	}

	/**
	 * What delivers the given secret given by its name: an override in the secret storage service,
	 * or the store of the installation.
	 *
	 * @param reference
	 *        The reference to the secret.
	 * @return The description, <code>null</code> without a reference or for a secret not given by
	 *         its name.
	 *
	 * @see SecretReference#getResolvedBy()
	 */
	@Label("What resolves a secret")
	public static String resolvedBy(SecretReference reference) {
		return reference == null ? null : text(reference.getResolvedBy());
	}

	/**
	 * Whether the given secret was available when it was read.
	 *
	 * @param reference
	 *        The reference to the secret.
	 * @return What the user reads for the status, <code>null</code> without a reference.
	 *
	 * @see SecretReference#getStatus()
	 */
	@Label("Status of a secret")
	public static String status(SecretReference reference) {
		return reference == null ? null : text(ResKey.forEnum(reference.getStatus()));
	}

	/**
	 * Why the given secret is not available.
	 *
	 * @param reference
	 *        The reference to the secret.
	 * @return The message, <code>null</code> for an available secret and without a reference.
	 *
	 * @see SecretReference#getMessage()
	 */
	@Label("Problem of a secret")
	public static String message(SecretReference reference) {
		return reference == null ? null : text(reference.getMessage());
	}

	/**
	 * Whether the given secret was available when it was read.
	 *
	 * @param reference
	 *        The reference to the secret.
	 * @return Whether the status is {@link SecretReference.Status#AVAILABLE}, <code>false</code>
	 *         without a reference.
	 */
	@Label("Whether a secret is available")
	public static boolean available(SecretReference reference) {
		return reference != null && reference.getStatus() == SecretReference.Status.AVAILABLE;
	}

	/**
	 * Whether the given secret was absent from its source when it was read.
	 *
	 * @param reference
	 *        The reference to the secret.
	 * @return Whether the status is {@link SecretReference.Status#MISSING}, <code>false</code>
	 *         without a reference.
	 */
	@Label("Whether a secret is missing")
	public static boolean missing(SecretReference reference) {
		return reference != null && reference.getStatus() == SecretReference.Status.MISSING;
	}

	private static String text(ResKey key) {
		return key == null ? null : Resources.getInstance().getString(key);
	}

}
