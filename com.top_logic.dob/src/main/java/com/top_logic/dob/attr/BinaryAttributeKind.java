/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.attr;

import java.io.IOException;

import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.blob.BlobStore;
import com.top_logic.basic.io.blob.BlobStoreService;
import com.top_logic.basic.io.blob.BlobUpload;
import com.top_logic.dob.MOAttribute;

/**
 * The kinds of storing the content of binary values: inline in the database, in a blob store, or
 * depending on the size of the content.
 *
 * <p>
 * Each kind is the kind of a binary attribute in the kbase schema; its external name is the tag
 * name of the attribute in the schema. For binary values of dynamic attributes, the kind is chosen
 * per value.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public enum BinaryAttributeKind implements ExternallyNamed {

	/**
	 * All content stored inline in the database, see {@link InlineBinaryAttribute}.
	 */
	INLINE(InlineBinaryAttribute.Config.TAG_NAME, InlineBinaryAttribute.Config.class),

	/**
	 * All content stored in a blob store, see {@link RefBinaryAttribute}.
	 */
	REF(RefBinaryAttribute.Config.TAG_NAME, RefBinaryAttribute.Config.class),

	/**
	 * Small content inline, large content in a blob store, see {@link HybridBinaryAttribute}.
	 */
	HYBRID(HybridBinaryAttribute.Config.TAG_NAME, HybridBinaryAttribute.Config.class);

	private final String _externalName;

	private final Class<? extends AbstractBinaryAttribute.Config> _configType;

	private BinaryAttributeKind(String externalName, Class<? extends AbstractBinaryAttribute.Config> configType) {
		_externalName = externalName;
		_configType = configType;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}

	/**
	 * The configuration interface of an attribute of this kind.
	 */
	public Class<? extends AbstractBinaryAttribute.Config> getConfigType() {
		return _configType;
	}

	/**
	 * Whether attributes of this kind store content in a blob store.
	 */
	public boolean isExternal() {
		return this != INLINE;
	}

	/**
	 * Whether attributes of this kind store content inline.
	 */
	public boolean isInline() {
		return this != REF;
	}

	/**
	 * Prepares the given content for being stored according to this kind.
	 *
	 * <p>
	 * Content stored in a blob store is uploaded by this method.
	 * </p>
	 *
	 * @param storeName
	 *        The name of the {@link BlobStore} in the {@link BlobStoreService} receiving external
	 *        content, <code>null</code> for the default store. Ignored for {@link #INLINE}.
	 * @param threshold
	 *        The size from which on content is stored in the blob store, only relevant for
	 *        {@link #HYBRID}.
	 * @param data
	 *        The data to store, or <code>null</code>.
	 * @return A {@link BinaryData} with known size to store inline, or the reference to the
	 *         uploaded content.
	 */
	public BinaryData toStoredValue(String storeName, long threshold, BinaryData data) throws IOException {
		if (data == null) {
			return null;
		}
		switch (this) {
			case INLINE:
				return BlobUpload.inline(data);
			case REF:
				return BlobUpload.upload(storeName, data);
			case HYBRID:
				return BlobUpload.uploadAboveThreshold(storeName, threshold, data);
		}
		throw new IllegalStateException("Unknown kind: " + this);
	}

	/**
	 * The kind of the given attribute, <code>null</code> if the given attribute is no
	 * {@link AbstractBinaryAttribute}.
	 */
	public static BinaryAttributeKind of(MOAttribute attribute) {
		if (attribute instanceof HybridBinaryAttribute) {
			return HYBRID;
		}
		if (attribute instanceof RefBinaryAttribute) {
			return REF;
		}
		if (attribute instanceof InlineBinaryAttribute) {
			return INLINE;
		}
		return null;
	}

}
