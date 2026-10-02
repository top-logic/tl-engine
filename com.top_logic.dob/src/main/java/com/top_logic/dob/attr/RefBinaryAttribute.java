/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.attr;

import java.io.IOException;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;

/**
 * Binary attribute storing its content in a blob store.
 *
 * <p>
 * The table stores the key of the blob, the SHA-256 hash of the content, size, content type and
 * name. The content is uploaded when a value is assigned to the attribute, not when the transaction
 * is committed. Reading a value needs no access to LOB columns; the content is streamed from the
 * store when it is read, and a complete read checks the content against the stored hash.
 * </p>
 *
 * @implNote Columns: {@link #SUFFIX_KEY}, {@link #SUFFIX_HASH}, {@link #SUFFIX_SIZE},
 *           {@link #SUFFIX_CONTENT_TYPE}, {@link #SUFFIX_NAME}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class RefBinaryAttribute extends AbstractBinaryAttribute implements BlobReferenceAttribute {

	/**
	 * Configuration of a {@link RefBinaryAttribute}.
	 */
	@TagName(RefBinaryAttribute.Config.TAG_NAME)
	public interface Config extends AbstractBinaryAttribute.ExternalConfig {

		/** Tag name of a {@link RefBinaryAttribute} in the schema. */
		String TAG_NAME = "binary-ref";

		@Override
		@ClassDefault(RefBinaryAttribute.class)
		Class<? extends MOAttribute> getImplementationClass();

	}

	private final String _storeName;

	/**
	 * Creates a {@link RefBinaryAttribute} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public RefBinaryAttribute(InstantiationContext context, Config config) {
		super(context, config, false, true);
		_storeName = StringServices.nonEmpty(config.getStore());
	}

	private RefBinaryAttribute(String name, MetaObject type, RefBinaryAttribute orig) {
		super(name, type, orig);
		_storeName = orig._storeName;
	}

	@Override
	public String getStoreName() {
		return _storeName;
	}

	@Override
	public BinaryData toStoredValue(BinaryData value) throws IOException {
		return BinaryAttributeKind.REF.toStoredValue(_storeName, 0, value);
	}

	@Override
	protected AbstractBinaryAttribute createCopy(String newName, MetaObject newType) {
		return new RefBinaryAttribute(newName, newType, this);
	}

}
