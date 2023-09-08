/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.form.values.edit.TemplateProvider;
import com.top_logic.layout.form.values.edit.annotation.UseTemplate;
import com.top_logic.model.search.ModelBasedSearch;
import com.top_logic.model.search.ui.model.options.PredefinedSearchParameters;
import com.top_logic.model.search.ui.model.parameters.PredefinedSearchParameter;
import com.top_logic.model.search.ui.model.structure.SearchPart;

/**
 * Top-level search UI model.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@UseTemplate(Search.Display.class)
public interface Search extends UnionSearch {

	/**
	 * Version of the search expression types.
	 * <p>
	 * For detecting old persisted search expressions and for example converting them.
	 * </p>
	 */
	String VERSION = "0.1";

	/** Property name of {@link #getPredefinedParameters()}. */
	String PREDEFINED_PARAMETERS = "predefined-parameters";

	/**
	 * See: {@link SearchPart#getConfigName()}
	 * <p>
	 * All other {@link SearchPart}s navigate to their container to get the value of this property.
	 * But this is the top-level {@link SearchPart}, it has no container. Therefore, the value has
	 * to be set explicitly.
	 * </p>
	 */
	@Hidden
	@Mandatory
	@Override
	String getConfigName();

	/** @see #getConfigName() */
	void setConfigName(String configName);

	/**
	 * A {@link List} of predefined parameters.
	 * <p>
	 * These parameters are the same for every query.
	 * </p>
	 */
	@Name(PREDEFINED_PARAMETERS)
	@Hidden
	@Derived(fun = PredefinedSearchParameters.class, args = @Ref(CONFIG_NAME))
	List<PredefinedSearchParameter> getPredefinedParameters();

	/**
	 * {@link TemplateProvider} for {@link Search} display.
	 */
	public class Display extends TemplateProvider {

		/**
		 * The CSS class for the {@link ModelBasedSearch}.
		 */
		public static final String CSS_CLASS_SEARCH = "modelBasedSearch";

		@Override
		public HTMLTemplateFragment get(ConfigurationItem model) {
			return div(css(CSS_CLASS_SEARCH),
				member(UNIONS));
		}
	}
}
