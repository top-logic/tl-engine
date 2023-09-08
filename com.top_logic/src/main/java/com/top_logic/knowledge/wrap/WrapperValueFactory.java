/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap;

import java.util.Map;

import com.top_logic.basic.json.JSON.DefaultValueFactory;
import com.top_logic.basic.json.JSON.ValueFactory;

/**
 * {@link DefaultValueFactory} that post modifies a {@link Map} formerly created with
 * {@link WrapperValueAnalyzer} to a {@link Wrapper}.
 * 
 * @see WrapperValueAnalyzer
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class WrapperValueFactory extends DefaultValueFactory {

	/** Default instance of {@link WrapperValueFactory}. */
	public static final ValueFactory WRAPPER_INSTANCE = new WrapperValueFactory();

	@Override
	public Object finishMap(Object result) {
		// result is created in #createMapValue()
		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>) result;
		if (WrapperValueAnalyzer.isWrapperMap(map)) {
			return WrapperValueAnalyzer.mapToWrapper(map);
		}
		return super.finishMap(result);
	}
}
