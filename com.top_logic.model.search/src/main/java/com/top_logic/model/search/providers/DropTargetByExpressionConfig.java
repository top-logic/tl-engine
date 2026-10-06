/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.form.component.PostCreateAction;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.DynamicMode;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.provider.MetaLabelProvider;
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

	/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
	Lookup LOOKUP = MethodHandles.lookup();

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
	 * The message to annotate to the change performed by the drop.
	 * 
	 * <p>
	 * The message may contain the placeholder '{0}' that is replaced with the labels of the dropped
	 * objects, and the placeholder '{1}' that is replaced with the label of the object the objects
	 * are dropped onto.
	 * </p>
	 * 
	 * <p>
	 * If not set, a default message is derived from the dropped objects and the drop target.
	 * </p>
	 */
	@Name(COMMIT_MESSAGE)
	@DynamicMode(fun = VisibleIf.class, args = @Ref(IN_TRANSACTION))
	ResKey2 getCommitMessage();

	/**
	 * Builds the commit message for a drop.
	 * 
	 * @param droppedObjects
	 *        The objects being dropped.
	 * @param target
	 *        The object the objects are dropped onto, <code>null</code> if the drop has no target
	 *        object.
	 * @return The message to annotate to the change performed by the drop.
	 * 
	 * @implNote Without a configured {@link #getCommitMessage()}, the message is
	 *           {@link I18NConstants#DROPPED__OBJECTS_TARGET}, or
	 *           {@link I18NConstants#DROPPED__OBJECTS} if there is no target.
	 */
	default ResKey buildCommitMessage(Collection<?> droppedObjects, Object target) {
		String droppedLabels = droppedObjects.stream()
			.map(MetaLabelProvider.INSTANCE::getLabel)
			.collect(Collectors.joining(", "));

		ResKey2 customMessage = getCommitMessage();
		if (customMessage != null) {
			return customMessage.fill(droppedLabels, MetaLabelProvider.INSTANCE.getLabel(target));
		}
		if (target == null) {
			return I18NConstants.DROPPED__OBJECTS.fill(droppedLabels);
		}
		return I18NConstants.DROPPED__OBJECTS_TARGET.fill(droppedLabels, MetaLabelProvider.INSTANCE.getLabel(target));
	}

}
