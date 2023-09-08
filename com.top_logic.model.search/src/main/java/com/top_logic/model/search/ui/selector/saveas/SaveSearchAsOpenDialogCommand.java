/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.selector.saveas;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;

import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.search.ui.selector.SaveSearchCommand;
import com.top_logic.model.search.ui.selector.SearchSelectorComponent;
import com.top_logic.reporting.flex.search.SearchResultChartConfigComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.OpenModalDialogCommandHandler;

/**
 * {@link CommandHandler} to open the "save search as" dialog.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class SaveSearchAsOpenDialogCommand extends OpenModalDialogCommandHandler {

	/**
	 * The id under which this {@link CommandHandler} is registered at the
	 * {@link CommandHandlerFactory}.
	 */
	public static final String COMMAND_ID = "saveSearchAsOpenDialog";

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link SaveSearchAsOpenDialogCommand}.
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
	public SaveSearchAsOpenDialogCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext displayContext, LayoutComponent untypedComponent,
			Object model, Map<String, Object> arguments) {
		SearchSelectorComponent component = (SearchSelectorComponent) untypedComponent;
		HandlerResult errorResult = checkSearchAndReportFormContexts(component);
		if (errorResult != null) {
			return errorResult;
		}
		return super.handleCommand(displayContext, component, model, arguments);
	}

	/**
	 * {@link FormContext#checkAll() Checks} the {@link FormContext}s of the search and report
	 * component.
	 * 
	 * @param selectorComponent
	 *        The {@link SearchSelectorComponent} itself is not checked. Is not allowed to be null.
	 * @return Null, if there is no error.
	 */
	private HandlerResult checkSearchAndReportFormContexts(SearchSelectorComponent selectorComponent) {
		FormComponent searchExpressionEditor = (FormComponent) selectorComponent.getActiveSearchExpressionEditor();
		SearchResultChartConfigComponent reportComponent = selectorComponent.getReportComponent();
		return SaveSearchCommand.checkFormContexts(list(searchExpressionEditor, reportComponent));
	}

}
