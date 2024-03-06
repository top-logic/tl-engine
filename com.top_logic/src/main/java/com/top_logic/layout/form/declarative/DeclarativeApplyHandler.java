/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.declarative;

import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * Apply {@link CommandHandler} that stores form values build with {@link DeclarativeFormBuilder}s.
 * 
 * @see DeclarativeFormBuilder
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class DeclarativeApplyHandler<E extends ConfigurationItem, M> extends AbstractCommandHandler {

	/** Config interface for {@link DeclarativeApplyHandler}. */
	public interface Config extends AbstractCommandHandler.Config {

		@Override
		@BooleanDefault(false)
		boolean getConfirm();

		@Override
		@FormattedDefault(SimpleBoundCommandGroup.WRITE_NAME)
		CommandGroupReference getGroup();

	}

	/**
	 * Creates a {@link DeclarativeApplyHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DeclarativeApplyHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext displayContext, LayoutComponent component, Object model,
			Map<String, Object> arguments) {
		FormComponent formHandler = (FormComponent) component;
		FormContext formContext = formHandler.getFormContext();

		if (!formContext.checkAll()) {
			return AbstractApplyCommandHandler.createErrorResult(formContext);
		}

		doTransaction(formHandler, formContext, model);

		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Applies changes after the {@link FormContext} has been validated.
	 * 
	 * @param formHandler
	 *        The context component.
	 * @param formContext
	 *        The valid {@link FormContext}.
	 * @param model
	 *        The business model to apply changes to.
	 */
	protected void doTransaction(FormComponent formHandler, FormContext formContext, Object model) {
		Transaction tx;
		if (model instanceof TLObject) {
			TLObject modelObject = (TLObject) model;
			KnowledgeBase kb = modelObject.tHandle().getKnowledgeBase();
			tx = kb.beginTransaction();
		} else {
			tx = null;
		}
		try {
			@SuppressWarnings("unchecked")
			E editModel = (E) EditorFactory.getModel(formContext);

			@SuppressWarnings("unchecked")
			M typedModel = (M) model;

			storeValues(formHandler, editModel, typedModel);

			if (tx != null) {
				tx.commit();
			}

			afterCommit(formHandler, editModel, typedModel);
		} catch (KnowledgeBaseException ex) {
			throw new KnowledgeBaseRuntimeException(ex);
		} finally {
			if (tx != null) {
				tx.rollback();
			}
		}
	}

	/**
	 * Business logic for storing values from the given edit model to the given typed business
	 * model.
	 * 
	 * @param formHandler
	 *        The context component.
	 * @param editModel
	 *        The edit model.
	 * @param typedModel
	 *        The typed business model.
	 */
	protected abstract void storeValues(FormComponent formHandler, E editModel, M typedModel);

	/**
	 * Business logic for cleaning up after successfully commiting the apply.
	 * 
	 * @param formHandler
	 *        The context component.
	 * @param editModel
	 *        The edit model.
	 * @param typedModel
	 *        The typed business model.
	 */
	protected void afterCommit(FormComponent formHandler, E editModel, M typedModel) {
		formHandler.removeFormContext();
	}

}
