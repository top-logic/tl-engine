/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.selector.saveas;

import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.element.layout.meta.expression.NewExpressionCommand;
import com.top_logic.element.layout.meta.expression.SaveExpressionCommand;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.search.persistency.expressions.SearchExpression;
import com.top_logic.model.search.persistency.expressions.SearchExpressionStructureFactory;
import com.top_logic.model.search.ui.GUISearchExpressionEditor;
import com.top_logic.model.search.ui.model.Search;
import com.top_logic.model.search.ui.selector.SearchAndReportConfig;
import com.top_logic.model.search.ui.selector.SearchSelectorComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;

/**
 * The {@link SaveExpressionCommand} that saves the search configured in the given
 * {@link GUISearchExpressionEditor} under a new name.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class SaveSearchAsCommand extends NewExpressionCommand {

	/**
	 * The id under which this {@link CommandHandler} is registered at the
	 * {@link CommandHandlerFactory}.
	 */
	public static final String COMMAND_ID = "saveModelSearchAs";

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link SaveSearchAsCommand}.
	 * <p>
	 * <b>Don't call directly.</b> Use
	 * {@link InstantiationContext#getInstance(PolymorphicConfiguration)} instead.
	 * </p>
	 * 
	 * @param context
	 *        For error reporting and instantiation of dependent configured objects.
	 * @param config
	 *        The configuration for the new instance.
	 */
	@CalledByReflection
	public SaveSearchAsCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Object createObject(LayoutComponent component, Object model, FormContainer formContainer,
			Map<String, Object> arguments) {
		SearchSelectorComponent searchComponent = (SearchSelectorComponent) component.getDialogParent();
		SearchAndReportConfig searchExpression = searchComponent.getExpression();
		String searchExpressionString = TypedConfiguration.toString(searchExpression);
		SearchExpression expressionWrapper = createSearchExpressionImpl();
		expressionWrapper.setName(getSearchName(formContainer));
		expressionWrapper.setExpression(searchExpressionString);
		expressionWrapper.setVersion(Search.VERSION);
		storeOwner(expressionWrapper);
		publish(expressionWrapper, formContainer);
		return expressionWrapper;
	}

	private SearchExpression createSearchExpressionImpl() {
		return SearchExpressionStructureFactory.getInstance().newSearchExpressionNode();
	}

	private String getSearchName(FormContainer formContainer) {
		String fieldName = SaveSearchAsComponent.FIELD_NAME_NAME;
		StringField nameField = (StringField) formContainer.getMember(fieldName);
		return nameField.getAsString();
	}

	@Override
	protected void afterCommit(LayoutComponent component, Object expressionWrapper) {
		super.afterCommit(component, expressionWrapper);
		SearchSelectorComponent searchComponent = (SearchSelectorComponent) component.getDialogParent();
		searchComponent.getSelectorField().setAsSingleSelection(expressionWrapper);
	}

}
