/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotationContainer;

/**
 * Test case for {@link TypedAnnotationContainer}
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestTypedAnnotationContainer extends AbstractTypedAnnotationTest {

	@Override
	protected TypedAnnotatable newAnnotatable() {
		return new TypedAnnotationContainer();
	}

}
