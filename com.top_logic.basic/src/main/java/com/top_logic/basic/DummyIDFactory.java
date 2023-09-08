/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;


/**
 * Factory for {@link TLID} that are valid for testing without a knowledge base only.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DummyIDFactory {

	private static long nextId = 1;

	/**
	 * Creates a unique ID.
	 */
	public static TLID createId() {
		if (IdentifierUtil.SHORT_IDS) {
			synchronized (DummyIDFactory.class) {
				return LongID.valueOf(nextId++);
			}
		} else {
			return StringID.createRandomID();
		}
	}

}
