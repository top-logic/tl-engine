/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.scripting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.element.layout.meta.search.AttributedSearchResultSet;
import com.top_logic.layout.scripting.recorder.ref.AbstractModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;

/**
 * {@link AbstractModelNamingScheme} for {@link AttributedSearchResultSet}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SearchResultNamingScheme
		extends AbstractModelNamingScheme<AttributedSearchResultSet, SearchResultNamingScheme.Name> {

	/**
	 * {@link Name} for an {@link AttributedSearchResultSet}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Name extends ModelName {

		/**
		 * Names for the types of an {@link AttributedSearchResultSet}.
		 */
		List<ModelName> getTypes();

		/**
		 * Names for the results of an {@link AttributedSearchResultSet}.
		 */
		List<ModelName> getResults();

		/**
		 * Result column names.
		 */
		@Format(CommaSeparatedStrings.class)
		List<String> getResultColumns();

		/**
		 * Setter for {@link #getResultColumns()}.
		 */
		void setResultColumns(List<String> value);

	}

	/**
	 * Creates a {@link SearchResultNamingScheme}.
	 *
	 */
	public SearchResultNamingScheme() {
		super(AttributedSearchResultSet.class, Name.class);
	}

	@Override
	protected void initName(Name name, AttributedSearchResultSet model) {
		for (TLClass type : model.getTypes()) {
			ModelName typeName = ModelResolver.buildModelNameIfAvailable(type).getElse(null);
			if (typeName != null) {
				name.getTypes().add(typeName);
			}
		}
		for (TLObject result : model.getResultObjects()) {
			ModelName resultName = ModelResolver.buildModelNameIfAvailable(result).getElse(null);
			if (resultName != null) {
				name.getResults().add(resultName);
			}
		}
		name.setResultColumns(model.getResultColumns());
	}

	@Override
	public AttributedSearchResultSet locateModel(ActionContext context, Name name) {
		Collection<TLObject> result = new ArrayList<>();
		for (ModelName resultName : name.getResults()) {
			result.add((TLObject) ModelResolver.locateModel(context, resultName));
		}
		Set<TLClass> types = new HashSet<>();
		for (ModelName typeName : name.getTypes()) {
			types.add((TLClass) ModelResolver.locateModel(context, typeName));
		}
		List<String> columns = name.getResultColumns();
		return new AttributedSearchResultSet(result, types, columns, Collections.emptyList());
	}

}
