/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.util.HashSet;

import com.top_logic.basic.SubSessionContext;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.model.ExecutabilityPolling;
import com.top_logic.util.TLContext;
import com.top_logic.util.ToBeValidated;

/**
 * The class {@link CommandModelRegistry} is a registry for command models which base on things
 * which are not observable. The registry depends on the {@link TLContext#getContext() context}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CommandModelRegistry implements ToBeValidated {

	private static final Property<CommandModelRegistry> COMMAND_MODELS =
		TypedAnnotatable.property(CommandModelRegistry.class, "commandModels");

	private HashSet<CommandModel> commandModels = new HashSet<>();

	protected CommandModelRegistry() {
	}

	/**
	 * This method returns a registry to register the {@link CommandModel}s on and deregister the
	 * {@link CommandModel}s from, resp..
	 */
	public static CommandModelRegistry getRegistry() {
		SubSessionContext context = ThreadContextManager.getSubSession();
		assert context != null : "Method is called in a thread which is neither used for rendering nor for command execution!";
		CommandModelRegistry registry = context.get(COMMAND_MODELS);
		if (registry == null) {
			synchronized (context) {
				if (context.get(COMMAND_MODELS) == null) {
					registry = new CommandModelRegistry();
					context.set(COMMAND_MODELS, registry);
				}
			}
		}
		return registry;
	}

	/**
	 * This method updates all registered {@link CommandModel}s. This method is not synchronized so
	 * it can only be used during command execution and not during rendering.
	 */
	public void updateCommandModels() {
		/*
		 * must iterate over a copy of the CommandModel storage. This is necessary due to Ticket
		 * #528. It is assumed that the described ConcurrentModification occurs, since during update
		 * some CommandModel some CommandModel wants to deregister
		 */
		final int size = commandModels.size();
		if (size == 0) {
			return;
		}
		CommandModel[] copiedModels = commandModels.toArray(new CommandModel[size]);

		for (int index = 0; index < size; index++) {
			updateCommandModel(copiedModels[index]);
		}
	}

	/**
	 * Updates the given command model if it is an instance of
	 * {@link CommandField} or {@link ComponentCommandModel}
	 * 
	 * @param model
	 *        the model to update
	 */
	private static void updateCommandModel(Command model) {
		if (model instanceof ExecutabilityPolling) {
			((ExecutabilityPolling) model).updateExecutabilityState();
		} else if (model instanceof WrappedCommandModel) {
			updateCommandModel(((WrappedCommandModel) model).unwrap());
		}
	}

	/**
	 * This method registers a {@link CommandModel} for being updated during
	 * next revalidation process. This method must be synchronized to avoid
	 * concurrent modification, because it is called during rendering so it is
	 * possible that this method is called from different threads.
	 * 
	 * If the given model was not registered before it will also be updated
	 * directly.
	 */
	public synchronized boolean registerCommandModel(CommandModel model) {
		return commandModels.add(model);
	}

	/**
	 * This method deregisteres the given {@link CommandModel} from being updated next revalidation.
	 * This method must be synchronized to avoid concurrent modification, because it is called
	 * during rendering so it is possible that this method is called from different threads.
	 */
	public synchronized boolean deregisterCommandModel(CommandModel model) {
		return commandModels.remove(model);
	}

	@Override
	public void validate(DisplayContext context) {
		updateCommandModels();
	}
}
