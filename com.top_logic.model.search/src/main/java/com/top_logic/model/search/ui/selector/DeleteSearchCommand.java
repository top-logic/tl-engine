/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.selector;

import java.util.Map;

import com.top_logic.base.services.simpleajax.AJAXCommandHandler;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.element.layout.meta.search.QueryUtils;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.util.WrapperUtil;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.search.persistency.expressions.SearchExpression;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * The {@link Command} that deletes the selected {@link SearchExpression}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class DeleteSearchCommand extends AJAXCommandHandler {

	/**
	 * The id under which this {@link CommandHandler} is registered at the
	 * {@link CommandHandlerFactory}.
	 */
	public static final String COMMAND_ID = "deleteModelSearch";

	/**
	 * Configuration for {@link DeleteSearchCommand}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AJAXCommandHandler.Config {

		@FormattedDefault(QueryUtils.OWNER_WRITE_NAME)
		@Override
		CommandGroupReference getGroup();

	}

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link DeleteSearchCommand}.
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
	public DeleteSearchCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext displayContext, LayoutComponent untypedComponent,
			Object model, Map<String, Object> arguments) {
		SearchSelectorComponent component = (SearchSelectorComponent) untypedComponent;
		SearchExpression expression = component.getSelected();
		delete(expression);
		return HandlerResult.DEFAULT_RESULT;
	}

	private void delete(SearchExpression expression) {
		String searchName = expression.getName();
		Transaction transaction = WrapperUtil.getKnowledgeBase(expression).beginTransaction();
		try {
			expression.tDelete();
			transaction.commit();
		} catch (KnowledgeBaseException ex) {
			throw new RuntimeException("Failed to delete search expression '" + searchName + "'. Cause: "
				+ ex.getMessage(), ex);
		} finally {
			transaction.rollback();
		}
	}

}
