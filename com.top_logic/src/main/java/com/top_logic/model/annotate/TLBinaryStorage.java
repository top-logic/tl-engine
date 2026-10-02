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
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
import com.top_logic.basic.config.constraint.annotation.Bound;
import com.top_logic.basic.config.constraint.annotation.Comparision;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.constraint.impl.NonNegative;
import com.top_logic.basic.config.format.MemorySizeFormat;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.io.blob.BlobStoreNames;
import com.top_logic.basic.io.blob.BlobUpload;
import com.top_logic.dob.attr.BinaryAttributeKind;
import com.top_logic.knowledge.service.BinaryStorageFieldModes;
import com.top_logic.knowledge.service.FlexDataManagerFactory;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.annotate.persistency.BinaryStorageAnnotationPolicy;

/**
 * Annotation choosing where the content of a binary attribute is stored.
 *
 * <p>
 * The {@link #getKind()} decides between storing content in the database and uploading it to the
 * blob store {@link #getStore()}: all content in the database, all content in the blob store, or
 * content smaller than the {@link #getThreshold()} in the database and larger content in the blob
 * store. Settings that are not given are taken from the configuration of the dynamic attribute
 * storage, see {@link FlexDataManagerFactory}.
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
@DisplayOrder({ TLBinaryStorage.KIND, TLBinaryStorage.STORE, TLBinaryStorage.THRESHOLD })
@TargetType(TLTypeKind.BINARY)
@InApp
public interface TLBinaryStorage extends TLAttributeAnnotation {

	/** Tag name of a {@link TLBinaryStorage} annotation. */
	String TAG_NAME = "binary-storage";

	/** Configuration name of {@link #getKind()}. */
	String KIND = "kind";

	/** Configuration name of {@link #getStore()}. */
	String STORE = "store";

	/** Configuration name of {@link #getThreshold()}. */
	String THRESHOLD = "threshold";

	/**
	 * Where the content of the attribute is stored.
	 *
	 * <p>
	 * {@link BinaryAttributeKind#INLINE} stores all content in the database, independent of its
	 * size. {@link BinaryAttributeKind#REF} stores all content in the blob store.
	 * {@link BinaryAttributeKind#HYBRID} stores content smaller than the threshold in the database
	 * and larger content in the blob store. If no kind is given, the kind configured for dynamic
	 * binary attributes is used. Changing the kind does not move content that is already stored.
	 * </p>
	 */
	@Name(KIND)
	@Nullable
	@NullDefault
	BinaryAttributeKind getKind();

	/**
	 * @see #getKind()
	 */
	void setKind(BinaryAttributeKind value);

	/**
	 * The name of the blob store receiving the content stored externally.
	 *
	 * <p>
	 * The name refers to a store configured in the blob store service. Only relevant if the kind
	 * stores content in a blob store. If no store is given, the store configured for dynamic
	 * binary attributes is used. Changing the store does not move content that is already stored.
	 * </p>
	 */
	@Name(STORE)
	@Nullable
	@Options(fun = BlobStoreNames.class)
	@DynamicMode(fun = BinaryStorageFieldModes.StoreMode.class, args = @Ref(KIND))
	String getStore();

	/**
	 * @see #getStore()
	 */
	void setStore(String value);

	/**
	 * The size from which on content is stored in the blob store.
	 *
	 * <p>
	 * Only relevant for the kind {@link BinaryAttributeKind#HYBRID}. The size is given in bytes,
	 * optionally with a unit, e.g. <code>64KB</code> or <code>1MB</code>. A threshold of
	 * <code>0</code> stores all content in the blob store. The threshold must not exceed
	 * 2147483639 bytes (2 GB minus 8 bytes), since content of unknown size is buffered in memory up
	 * to the threshold to decide. If no threshold is given, the threshold configured for dynamic
	 * binary attributes is used.
	 * </p>
	 *
	 * @implNote The upper bound is {@link BlobUpload#MAX_BUFFERED_THRESHOLD}.
	 */
	@Name(THRESHOLD)
	@Format(MemorySizeFormat.class)
	@Nullable
	@NullDefault
	@Constraint(NonNegative.class)
	@Bound(comparison = Comparision.SMALLER_OR_EQUAL, value = BlobUpload.MAX_BUFFERED_THRESHOLD)
	@DynamicMode(fun = BinaryStorageFieldModes.ThresholdMode.class, args = @Ref(KIND))
	Long getThreshold();

	/**
	 * @see #getThreshold()
	 */
	void setThreshold(Long value);

}
