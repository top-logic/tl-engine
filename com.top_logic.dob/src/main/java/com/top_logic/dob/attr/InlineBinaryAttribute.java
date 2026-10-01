/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.attr;

import java.io.IOException;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.blob.BlobUpload;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;

/**
 * Binary attribute storing its content in a BLOB column of its table.
 *
 * <p>
 * Besides the content, size, content type and name of the value are stored.
 * </p>
 *
 * @implNote Columns: {@link #SUFFIX_DATA}, {@link #SUFFIX_SIZE}, {@link #SUFFIX_CONTENT_TYPE},
 *           {@link #SUFFIX_NAME}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class InlineBinaryAttribute extends AbstractBinaryAttribute {

	/**
	 * Configuration of an {@link InlineBinaryAttribute}.
	 */
	@TagName(InlineBinaryAttribute.Config.TAG_NAME)
	public interface Config extends AbstractBinaryAttribute.Config {

		/** Tag name of an {@link InlineBinaryAttribute} in the schema. */
		String TAG_NAME = "binary-inline";

		@Override
		@ClassDefault(InlineBinaryAttribute.class)
		Class<? extends MOAttribute> getImplementationClass();

	}

	/**
	 * Creates an {@link InlineBinaryAttribute} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public InlineBinaryAttribute(InstantiationContext context, Config config) {
		super(context, config, true, false);
	}

	private InlineBinaryAttribute(String name, MetaObject type, InlineBinaryAttribute orig) {
		super(name, type, orig);
	}

	@Override
	public BinaryData toStoredValue(BinaryData value) throws IOException {
		return BlobUpload.withKnownSize(value);
	}

	@Override
	protected AbstractBinaryAttribute createCopy(String newName, MetaObject newType) {
		return new InlineBinaryAttribute(newName, newType, this);
	}

}
