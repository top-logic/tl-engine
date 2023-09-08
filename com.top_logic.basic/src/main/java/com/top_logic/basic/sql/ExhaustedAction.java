/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sql;

import org.apache.commons.pool.impl.GenericObjectPool;

/**
 * {@link Enum} representing possible values from the
 * {@link org.apache.commons.pool.impl.GenericObjectPool.Config#whenExhaustedAction} property.
 * 
 * @see #fail
 * @see #block
 * @see #grow
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public enum ExhaustedAction {

	/**
	 * @see GenericObjectPool#WHEN_EXHAUSTED_FAIL
	 */
	fail,

	/**
	 * @see GenericObjectPool#WHEN_EXHAUSTED_BLOCK
	 */
	block,
	
	/**
	 * @see GenericObjectPool#WHEN_EXHAUSTED_GROW
	 */
	grow
}