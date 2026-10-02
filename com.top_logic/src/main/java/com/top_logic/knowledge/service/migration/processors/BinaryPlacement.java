/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration.processors;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.blob.BlobStoreService;
import com.top_logic.dob.attr.AbstractBinaryAttribute;
import com.top_logic.dob.attr.BinaryAttributeKind;
import com.top_logic.dob.attr.HybridBinaryAttribute;

/**
 * Decision where the content of a binary value is stored: inline in the database or in a blob
 * store.
 *
 * <p>
 * The decision follows the rules of the {@link BinaryAttributeKind}: {@link BinaryAttributeKind#INLINE}
 * keeps all content inline, {@link BinaryAttributeKind#REF} stores all content in the
 * {@link #getStoreName() store}, {@link BinaryAttributeKind#HYBRID} stores content from the
 * {@link #getThreshold() threshold} on in the store and smaller content inline.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class BinaryPlacement {

	private final BinaryAttributeKind _kind;

	private final String _storeName;

	private final long _threshold;

	/**
	 * Creates a {@link BinaryPlacement}.
	 *
	 * @param kind
	 *        The rule for deciding between inline and external content.
	 * @param storeName
	 *        The store for external content, <code>null</code> for the default store.
	 * @param threshold
	 *        The size from which on content is stored externally, only relevant for
	 *        {@link BinaryAttributeKind#HYBRID}.
	 */
	public BinaryPlacement(BinaryAttributeKind kind, String storeName, long threshold) {
		_kind = kind;
		_storeName = StringServices.nonEmpty(storeName);
		_threshold = threshold;
	}

	/**
	 * The placement of the content of the given attribute.
	 */
	public static BinaryPlacement of(AbstractBinaryAttribute attribute) {
		BinaryAttributeKind kind = BinaryAttributeKind.of(attribute);
		long threshold =
			attribute instanceof HybridBinaryAttribute hybrid ? hybrid.getThreshold() : HybridBinaryAttribute.DEFAULT_THRESHOLD;
		return new BinaryPlacement(kind, attribute.getStoreName(), threshold);
	}

	/**
	 * The rule for deciding between inline and external content.
	 */
	public BinaryAttributeKind getKind() {
		return _kind;
	}

	/**
	 * The store for external content, <code>null</code> for the default store.
	 */
	public String getStoreName() {
		return _storeName;
	}

	/**
	 * The size from which on content is stored externally by a {@link BinaryAttributeKind#HYBRID}
	 * placement.
	 */
	public long getThreshold() {
		return _threshold;
	}

	/**
	 * Whether content of the given size is stored in the {@link #getStoreName() store}.
	 */
	public boolean isExternal(long size) {
		switch (_kind) {
			case INLINE:
				return false;
			case REF:
				return true;
			case HYBRID:
				return size >= _threshold;
		}
		throw new IllegalStateException("Unknown kind: " + _kind);
	}

	/**
	 * Whether the given store name refers to the {@link #getStoreName() store} of this placement.
	 *
	 * @param storeName
	 *        A store name, <code>null</code> or empty for the default store.
	 */
	public boolean isTargetStore(String storeName) {
		BlobStoreService service = BlobStoreService.getInstance();
		return service.getStore(storeName) == service.getStore(_storeName);
	}

	@Override
	public String toString() {
		return _kind.getExternalName() + (_kind.isExternal() ? " store=" + _storeName : "")
			+ (_kind == BinaryAttributeKind.HYBRID ? " threshold=" + _threshold : "");
	}

}
