/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
import com.top_logic.basic.config.format.MemorySizeFormat;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.model.annotate.persistency.BinaryStorageAnnotationPolicy;

/**
 * Annotation choosing where the content of a binary attribute is stored.
 *
 * <p>
 * Content smaller than the {@link #getThreshold()} is stored in the database, larger content is
 * uploaded to the blob store {@link #getStore()}. Settings that are not given are taken
 * from the configuration of the dynamic attribute storage.
 * </p>
 *
 * <p>
 * The annotation applies to binary attributes stored in the table shared by all dynamic binary
 * attributes, i.e. attributes without a column of their own in the table of their type. On an
 * attribute stored in a declared column, it is ignored: such a column declares its blob store and
 * threshold in the schema of its table.
 * </p>
 *
 * @see BinaryStorageAnnotationPolicy
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName(TLBinaryStorage.TAG_NAME)
@DisplayOrder({ TLBinaryStorage.STORE, TLBinaryStorage.THRESHOLD })
@TargetType(TLTypeKind.BINARY)
@InApp
public interface TLBinaryStorage extends TLAttributeAnnotation {

	/** Tag name of a {@link TLBinaryStorage} annotation. */
	String TAG_NAME = "binary-storage";

	/** Configuration name of {@link #getStore()}. */
	String STORE = "store";

	/** Configuration name of {@link #getThreshold()}. */
	String THRESHOLD = "threshold";

	/**
	 * The name of the blob store receiving content of at least the threshold size.
	 *
	 * <p>
	 * The name refers to a store configured in the blob store service. If no store is given, the
	 * store configured for dynamic binary attributes is used. Changing the store does not move
	 * content that is already stored.
	 * </p>
	 */
	@Name(STORE)
	@Nullable
	String getStore();

	/**
	 * @see #getStore()
	 */
	void setStore(String value);

	/**
	 * The size from which on content is stored in the blob store.
	 *
	 * <p>
	 * The size is given in bytes, optionally with a unit, e.g. <code>64KB</code> or
	 * <code>1MB</code>. A threshold of <code>0</code> stores all content in the blob store. If no
	 * threshold is given, the threshold configured for dynamic binary attributes is used.
	 * </p>
	 */
	@Name(THRESHOLD)
	@Format(MemorySizeFormat.class)
	@Nullable
	@NullDefault
	Long getThreshold();

	/**
	 * @see #getThreshold()
	 */
	void setThreshold(Long value);

}
