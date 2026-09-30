/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.security.ModelAccessRule;
import com.top_logic.model.search.expr.DeleteObject;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * {@link ViewAction} deleting the object it receives.
 *
 * <p>
 * The object is deleted the way the TL-Script function {@code delete()} deletes it, including the
 * parts of its compositions, in a transaction of its own (a transaction nested in an enclosing
 * {@link WithTransactionAction} commits together with that one). The current user must hold the
 * right to delete the object; a refusal aborts the chain with the message of {@code delete()}.
 * The action results in {@code null}, so that a following {@link WriteChannelAction} clears the
 * channel that held the deleted object.
 * </p>
 *
 * <p>
 * The action brings its executability: a command deleting its input is offered only where the
 * user may delete that input. See {@link ModelAccessRule} for how a refusal is displayed.
 * </p>
 *
 * <p>
 * Example: delete the selected project.
 * </p>
 *
 * <pre>
 * &lt;generic-command input="project"&gt;
 *   &lt;executability&gt;
 *     &lt;null-input-disabled/&gt;
 *   &lt;/executability&gt;
 *   &lt;delete-object/&gt;
 *   &lt;write-channel name="project"/&gt;
 * &lt;/generic-command&gt;
 * </pre>
 *
 * @implNote Deletion and the permission check are {@link DeleteObject#delete(Object, boolean)}.
 */
@InApp
public class DeleteObjectAction implements ViewAction {

	/**
	 * Configuration for {@link DeleteObjectAction}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<DeleteObjectAction> {

		/** Tag name of a {@link DeleteObjectAction} in an action chain. */
		String TAG_NAME = "delete-object";

		@Override
		@ClassDefault(DeleteObjectAction.class)
		Class<? extends DeleteObjectAction> getImplementationClass();
	}

	/**
	 * Creates a {@link DeleteObjectAction} from configuration.
	 */
	@CalledByReflection
	public DeleteObjectAction(InstantiationContext context, Config config) {
		// No configuration.
	}

	/**
	 * The deletion right on the command input.
	 */
	@Override
	public ViewExecutabilityRule getIntrinsicRule() {
		return ModelAccessRule.onInput(SimpleBoundCommandGroup.DELETE);
	}

	@Override
	public Object execute(ReactContext context, Object input) {
		if (input == null) {
			return null;
		}
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
			DeleteObject.delete(input, true);
			tx.commit();
		}
		return null;
	}
}
