/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.filtergen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.top_logic.element.meta.form.EditContext;
import com.top_logic.knowledge.wrap.list.FastList;
import com.top_logic.knowledge.wrap.list.FastListElement;

/**
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ClassificationGeneratorSpi implements GeneratorSpi {
	
	private static class ClassificationGenerator extends ListGeneratorAdaptor {

		private final String _classification;

		public ClassificationGenerator(String classification) {
			this._classification = classification;
		}

		@Override
		public List<?> generateList(EditContext editContext) {
			FastList fastList = FastList.getFastList(_classification);
			if (fastList == null) {
				return handleClassificationNotFound();
			}
			{
				List<FastListElement> elements = fastList.elements();
				// list is unmodifiable but caller tries to sort
				return new ArrayList<>(elements);
			}
		}

		private <T> List<T> handleClassificationNotFound() {
			return Collections.emptyList();
		}

	}

	@Override
	public Generator bind(String... config) {
		String classificationList = checkConfiguration(config);
		return new ClassificationGenerator(classificationList);
	}

	private String checkConfiguration(String... config) {
		if (config.length != 1) {
			throw new IllegalArgumentException("Expected exactly one classification list is given but was: " +  Arrays.toString(config));
		}
		String classificationList = config[0];
		if (classificationList == null) {
			throw new IllegalArgumentException("No classification list given");
		}
		return classificationList;
	}

}


