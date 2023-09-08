/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref.value;

/**
 * A generic {@link Enum} value.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 * 
 * @deprecated Use {@link EnumNaming}.
 */
@Deprecated
public interface EnumValue extends ValueRef {

	/**
	 * The reference enum constant.
	 */
	Enum<?> getEnum();

	/** @see #getEnum() */
	void setEnum(Enum<?> value);
	
}
