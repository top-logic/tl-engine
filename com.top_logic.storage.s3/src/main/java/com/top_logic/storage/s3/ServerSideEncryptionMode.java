/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.storage.s3;

import software.amazon.awssdk.services.s3.model.ServerSideEncryption;

import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.basic.config.annotation.Label;

/**
 * Server-side encryption applied by the storage to the blobs of a {@link S3BlobStore}.
 *
 * <p>
 * The content is encrypted and decrypted by the storage, transparently for the application. Range
 * reads keep working.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public enum ServerSideEncryptionMode implements ExternallyNamed {

	/**
	 * No encryption is requested; the default encryption settings of the bucket apply.
	 */
	NONE("none", null),

	/**
	 * Encryption with keys managed by the storage (SSE-S3, AES-256).
	 */
	@Label("SSE-S3")
	SSE_S3("sse-s3", ServerSideEncryption.AES256),

	/**
	 * Encryption with a key of the key management service of the storage (SSE-KMS).
	 *
	 * <p>
	 * The key is configured in the store; without a key, the default key of the key management
	 * service is used.
	 * </p>
	 */
	@Label("SSE-KMS")
	SSE_KMS("sse-kms", ServerSideEncryption.AWS_KMS);

	private final String _externalName;

	private final ServerSideEncryption _sdkValue;

	private ServerSideEncryptionMode(String externalName, ServerSideEncryption sdkValue) {
		_externalName = externalName;
		_sdkValue = sdkValue;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}

	/**
	 * The encryption algorithm requested from the storage, or <code>null</code> if no encryption
	 * is requested.
	 */
	public ServerSideEncryption sdkValue() {
		return _sdkValue;
	}

}
