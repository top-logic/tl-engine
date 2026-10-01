/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.attr;

import java.io.IOException;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.format.MemorySizeFormat;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.blob.BlobUpload;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;

/**
 * Binary attribute storing small content in a BLOB column of its table and large content in a
 * blob store.
 *
 * <p>
 * Each row has either inline content or the key and SHA-256 hash of a blob, plus size, content
 * type and name. The decision is taken per value when it is assigned: content smaller than the
 * configured threshold is stored inline, larger content is uploaded to the blob store.
 * </p>
 *
 * @implNote Columns: {@link #SUFFIX_DATA}, {@link #SUFFIX_KEY}, {@link #SUFFIX_HASH},
 *           {@link #SUFFIX_SIZE}, {@link #SUFFIX_CONTENT_TYPE}, {@link #SUFFIX_NAME}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class HybridBinaryAttribute extends AbstractBinaryAttribute implements BlobReferenceAttribute {

	/** Default value of {@link Config#getThreshold()}: 64 KB. */
	public static final long DEFAULT_THRESHOLD = 64 * 1024;

	/**
	 * Configuration of a {@link HybridBinaryAttribute}.
	 */
	@TagName(HybridBinaryAttribute.Config.TAG_NAME)
	public interface Config extends AbstractBinaryAttribute.ExternalConfig {

		/** Tag name of a {@link HybridBinaryAttribute} in the schema. */
		String TAG_NAME = "binary-hybrid";

		/** Configuration name of {@link #getThreshold()}. */
		String THRESHOLD = "threshold";

		/**
		 * The size from which on content is stored in the blob store.
		 *
		 * <p>
		 * Content smaller than this size is stored in the database table, content of this size or
		 * larger in the blob store. The size is given in bytes, optionally with a unit, e.g.
		 * <code>64KB</code> or <code>1MB</code>.
		 * </p>
		 */
		@Name(THRESHOLD)
		@Format(MemorySizeFormat.class)
		@LongDefault(DEFAULT_THRESHOLD)
		long getThreshold();

		/**
		 * @see #getThreshold()
		 */
		void setThreshold(long value);

		@Override
		@ClassDefault(HybridBinaryAttribute.class)
		Class<? extends MOAttribute> getImplementationClass();

	}

	private final String _storeName;

	private final long _threshold;

	/**
	 * Creates a {@link HybridBinaryAttribute} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public HybridBinaryAttribute(InstantiationContext context, Config config) {
		super(context, config, true, true);
		_storeName = StringServices.nonEmpty(config.getStore());
		long threshold = config.getThreshold();
		if (threshold <= 0 || threshold > Integer.MAX_VALUE - 8) {
			context.error("Invalid threshold " + threshold + " of binary attribute '" + config.getAttributeName()
				+ "', using default " + DEFAULT_THRESHOLD + ".");
			threshold = DEFAULT_THRESHOLD;
		}
		_threshold = threshold;
	}

	private HybridBinaryAttribute(String name, MetaObject type, HybridBinaryAttribute orig) {
		super(name, type, orig);
		_storeName = orig._storeName;
		_threshold = orig._threshold;
	}

	@Override
	public String getStoreName() {
		return _storeName;
	}

	/**
	 * The size in bytes from which on content is stored in the blob store.
	 */
	public long getThreshold() {
		return _threshold;
	}

	@Override
	public BinaryData toStoredValue(BinaryData value) throws IOException {
		return BlobUpload.uploadAboveThreshold(_storeName, _threshold, value);
	}

	@Override
	protected AbstractBinaryAttribute createCopy(String newName, MetaObject newType) {
		return new HybridBinaryAttribute(newName, newType, this);
	}

}
