/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import org.apache.commons.collections4.BidiMap;

import com.top_logic.basic.col.BidiHashMap;
import com.top_logic.basic.col.InverseBidiMap;

/**
 * Test case for {@link InverseBidiMap}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestInverseBidiMap extends AbstractTestBidiMap {

	@Override
	public BidiMap makeEmptyBidiMap() {
		return new InverseBidiMap<>(new BidiHashMap<>());
	}

	@Override
	public void testBidiInverse() {
		// Cannot be guaranteed due to test setup.
	}

	@Override
	public void testBidiModifyEntrySet() {
		// Not supported.
	}

	@Override
	public void testBidiMapIteratorSet() {
		// Not supported.
	}

}
