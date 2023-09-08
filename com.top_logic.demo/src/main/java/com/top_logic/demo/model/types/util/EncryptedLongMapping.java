/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.model.types.util;

import org.apache.commons.codec.binary.Base64;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.internal.AbstractConfiguredStorageMapping;

/**
 * The class {@link EncryptedLongMapping} encoded long values using {@link Base64} encoding of them.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class EncryptedLongMapping extends AbstractConfiguredStorageMapping<EncryptedLongMapping.Config, Long> {

	/**
	 * Configuration options for {@link EncryptedLongMapping}.
	 */
	@TagName("demo-encrypted-long-mapping")
	public interface Config extends PolymorphicConfiguration<EncryptedLongMapping> {
		// Pure marker interface.
	}

	/**
	 * Creates a {@link EncryptedLongMapping} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public EncryptedLongMapping(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Class<Long> getApplicationType() {
		return Long.class;
	}

	@Override
	public Long getBusinessObject(Object aStorageObject) {
		if (aStorageObject == null) {
			return null;
		}
		String decodedString = new String(Base64.decodeBase64((String) aStorageObject), StringServices.CHARSET_UTF_8);
		return Long.valueOf(decodedString);
	}

	@Override
	public Object getStorageObject(Object aBusinessObject) {
		if (aBusinessObject == null) {
			return null;
		}
		String message = Long.toString(((Long)aBusinessObject).longValue());
		return Base64.encodeBase64String(message.getBytes(StringServices.CHARSET_UTF_8));
	}

	@Override
	public boolean isCompatible(Object businessObject) {
		return businessObject == null || businessObject instanceof Long;
	}
}

