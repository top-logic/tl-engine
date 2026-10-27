/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.credential;

import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.util.ResKey;

/**
 * A property in the configuration of a service that holds a {@link SecretSource}, together with
 * the result of checking whether its secret is available.
 *
 * <p>
 * A reference describes the secret without revealing it: where it is configured, which kind of
 * source delivers it and how that source identifies it. The {@link #getStatus() status} is
 * determined once when the reference is created, so that a list of references is a snapshot of
 * the secrets of the application at one point in time. The secret itself is read for the check and
 * discarded; no property of a reference contains it.
 * </p>
 *
 * @see SecretReferences
 */
public final class SecretReference {

	/**
	 * Whether the secret of a {@link SecretReference} is available.
	 */
	public enum Status {

		/** The secret is provided by its source. */
		AVAILABLE,

		/**
		 * The secret is absent from its source: an undefined variable, a file that does not exist,
		 * a name the secret store does not hold.
		 */
		MISSING,

		/** The secret is present but cannot be read, or the source cannot be asked for it. */
		ERROR;

	}

	private final BasicRuntimeModule<?> _service;

	private final String _path;

	private final Class<?> _kind;

	private final ResKey _reference;

	private final ResKey _resolvedBy;

	private final Status _status;

	private final ResKey _message;

	SecretReference(BasicRuntimeModule<?> service, String path, Class<?> kind, ResKey reference,
			ResKey resolvedBy, Status status, ResKey message) {
		_service = service;
		_path = path;
		_kind = kind;
		_reference = reference;
		_resolvedBy = resolvedBy;
		_status = status;
		_message = message;
	}

	/**
	 * The service in whose configuration the secret is configured, <code>null</code> for a
	 * configuration that belongs to no service.
	 */
	public BasicRuntimeModule<?> getService() {
		return _service;
	}

	/**
	 * The path of the secret property inside the configuration of the {@link #getService()
	 * service}.
	 *
	 * <p>
	 * The path consists of the names of the properties leading to the secret, separated by
	 * {@link SecretReferences#PATH_SEPARATOR}. An entry of a map or of a list is given by its key
	 * in brackets after the name of its property, an entry of a list without keys by its index,
	 * e.g. <code>providers[anthropic] / credentials / secret</code>.
	 * </p>
	 */
	public String getPath() {
		return _path;
	}

	/**
	 * The implementation class of the {@link SecretSource} delivering the secret.
	 */
	public Class<?> getKind() {
		return _kind;
	}

	/**
	 * How the source identifies the secret, e.g. the name of a secret or variable, or the path of
	 * a file.
	 *
	 * @return The description, <code>null</code> if the source cannot tell or cannot be created.
	 *
	 * @see SecretSource#reference()
	 */
	public ResKey getReference() {
		return _reference;
	}

	/**
	 * What delivers a secret given by its name: the override for the name in the
	 * {@link SecretStoreService}, or the store of the installation.
	 *
	 * @return The description, <code>null</code> for a source that is not a
	 *         {@link NamedSecretSource}, or while the {@link SecretStoreService} is not started.
	 */
	public ResKey getResolvedBy() {
		return _resolvedBy;
	}

	/**
	 * Whether the secret was available when this reference was created.
	 */
	public Status getStatus() {
		return _status;
	}

	/**
	 * Why the secret is not {@link Status#AVAILABLE available}.
	 *
	 * @return The message, <code>null</code> for an available secret. It never contains the
	 *         secret.
	 */
	public ResKey getMessage() {
		return _message;
	}

	@Override
	public String toString() {
		return "SecretReference(" + (_service == null ? "" : _service.getImplementation().getName() + ": ")
			+ _path + ", " + _kind.getName() + ", " + _status + ")";
	}

}
