/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.form.component.PostCreateAction;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.tool.boundsec.CommandHandler.ConfirmConfig.VisibleIf;

/**
 * Configuration for drop targets that are fully configured by model queries.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
@Abstract
public interface DropTargetByExpressionConfig extends ConfigurationItem {

	/**
	 * Name of {@link #getHandleDrop()}.
	 */
	public static final String HANDLE_DROP = "handleDrop";

	/**
	 * Name of {@link #getPostCreateActions()}.
	 */
	public static final String POST_CREATE_ACTIONS = "postCreateActions";

	/**
	 * Name of {@link #getCanDrop()}.
	 */
	public static final String CAN_DROP = "canDrop";

	/**
	 * Name of {@link #getInTransaction()}.
	 */
	public static final String IN_TRANSACTION = "in-transaction";

	/**
	 * Name of {@link #getCommitMessage()}.
	 */
	public static final String COMMIT_MESSAGE = "commit-message";

	/**
	 * Operation executing a drop in the context of a referenced element.
	 * 
	 * <p>
	 * The arguments depend on the concrete handler sub class.
	 * </p>
	 * 
	 * <p>
	 * The value returned from the function is passed to potential {@link #getPostCreateActions()
	 * post-drop actions}.
	 * </p>
	 */
	@Name(HANDLE_DROP)
	@ItemDefault(Expr.Null.class)
	Expr getHandleDrop();

	/**
	 * Actions to be executed after the dragged element is dropped.
	 * 
	 * <p>
	 * These actions get as model the object which is returned by the configured drop functionality.
	 * </p>
	 */
	@Name(POST_CREATE_ACTIONS)
	@Options(fun = AllInAppImplementations.class)
	@Label("Post-drop actions")
	List<PolymorphicConfiguration<PostCreateAction>> getPostCreateActions();

	/**
	 * Function checking whether a drop in the context of a referenced element can be performed.
	 */
	@Name(CAN_DROP)
	@ItemDefault(Expr.True.class)
	Expr getCanDrop();

	/**
	 * Whether the drop operation should be executed in a {@link Transaction transaction}.
	 */
	@Name(IN_TRANSACTION)
	@BooleanDefault(true)
	boolean getInTransaction();

	/**
	 * Function computing the message to annotate to the change performed by the drop.
	 * 
	 * <p>
	 * The function receives the arguments of the {@link #getHandleDrop() drop operation} followed
	 * by the model of the component the drop happens in. The result is either a string or an
	 * internationalized text.
	 * </p>
	 * 
	 * <p>
	 * If not set, or if the function returns nothing, a default message is used that names the
	 * dropped objects and the component the objects are dropped into.
	 * </p>
	 */
	@Name(COMMIT_MESSAGE)
	@DynamicMode(fun = VisibleIf.class, args = @Ref(IN_TRANSACTION))
	Expr getCommitMessage();

}
