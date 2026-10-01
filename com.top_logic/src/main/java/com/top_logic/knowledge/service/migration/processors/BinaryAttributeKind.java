/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration.processors;

import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.attr.AbstractBinaryAttribute;
import com.top_logic.dob.attr.HybridBinaryAttribute;
import com.top_logic.dob.attr.InlineBinaryAttribute;
import com.top_logic.dob.attr.RefBinaryAttribute;

/**
 * The kinds of binary attributes in the kbase schema.
 *
 * <p>
 * The external name of each kind is the tag name of the attribute in the schema.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public enum BinaryAttributeKind implements ExternallyNamed {

	/**
	 * Content stored inline in a BLOB column, see {@link InlineBinaryAttribute}.
	 */
	INLINE(InlineBinaryAttribute.Config.TAG_NAME, InlineBinaryAttribute.Config.class),

	/**
	 * Content stored in a blob store, see {@link RefBinaryAttribute}.
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
