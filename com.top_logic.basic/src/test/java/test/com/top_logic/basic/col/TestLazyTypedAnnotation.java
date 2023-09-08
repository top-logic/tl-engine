/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import com.top_logic.basic.col.LazyTypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable;

/**
 * Test for {@link LazyTypedAnnotatable}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestLazyTypedAnnotation extends AbstractTypedAnnotationTest {

	@Override
	protected TypedAnnotatable newAnnotatable() {
		return new LazyTypedAnnotatable();
	}

}

