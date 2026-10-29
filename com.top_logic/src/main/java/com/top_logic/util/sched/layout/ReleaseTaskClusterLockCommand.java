/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.layout;

import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.MessageBox.MessageType;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.conditional.CommandStep;
import com.top_logic.tool.boundsec.conditional.Failure;
import com.top_logic.tool.boundsec.conditional.PreconditionCommandHandler;
import com.top_logic.tool.boundsec.conditional.Success;
import com.top_logic.util.Resources;
import com.top_logic.util.sched.Scheduler;
import com.top_logic.util.sched.task.Task;

/**
 * {@link CommandHandler} for manually releasing the
 * {@link Scheduler#getClusterLockHolder(Task) cluster lock} of a {@link Task}.
 * <p>
 * <b>Be careful! If the task is still running or about to start, releasing the lock can cause
 * duplicate task runs.</b>
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class ReleaseTaskClusterLockCommand extends PreconditionCommandHandler {

	/**
	 * {@link TypedConfiguration} interface for {@link ReleaseTaskClusterLockCommand}.
	 */
	public interface Config extends AbstractCommandHandler.Config {
		/* Nothing, yet. Introduced for easier changes in the future: Introducing a config interface
		 * later is an API change that requires a code migration marker on the ticket, as there
		 * might be subclasses in some project with their own subclasses of the configuration that
		 * would need to be adapted. */
	}

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link ReleaseTaskClusterLockCommand}.
	 * <p>
	 * <b>Don't call directly.</b> Use
	 * {@link InstantiationContext#getInstance(com.top_logic.basic.config.PolymorphicConfiguration)}
	 * instead.
	 * </p>
	 * 
	 * @param context
	 *        For error reporting and instantiation of dependent configured objects.
	 * @param config
	 *        The configuration for the new instance.
	 */
	@CalledByReflection
	public ReleaseTaskClusterLockCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected CommandStep prepare(LayoutComponent component, Object model, Map<String, Object> arguments) {
		if (model == null) {
			return new Failure(I18NConstants.MANUAL_LOCK_RELEASE_TASK_IS_NULL);
		}
		if (!(model instanceof Task)) {
			return new Failure(
				I18NConstants.MANUAL_LOCK_RELEASE_SELECTION_IS_NO_TASK__SELECTION.fill(model.toString()));
		}
		Task task = (Task) model;
		ResKey problem = Scheduler.getClusterLockReleaseProblem(task);
		if (problem != null) {
			return new Failure(problem);
		}

		return new Success() {
			@Override
			protected void doExecute(DisplayContext context) {
				clearLock(task);
				createSuccessMessageBox(context, task);
			}
		};
	}

	/** Creates the {@link MessageBox} shown after releasing the cluster lock. */
	protected HandlerResult createSuccessMessageBox(DisplayContext displayContext, Task task) {
		String message = translate(I18NConstants.MANUAL_LOCK_RELEASE_DONE__TASK.fill(task.getName()));
		return MessageBox.confirm(displayContext, MessageType.INFO, message, MessageBox.button(ButtonType.OK));
	}

	/**
	 * Clears the cluster lock for the given {@link Task}.
	 *
	 * @see Scheduler#releaseClusterLock(Task)
	 */
	protected void clearLock(Task task) {
		Scheduler.releaseClusterLock(task);
	}

	/**
	 * Short-cut for {@link Resources#getMessage(ResKey, Object...)}.
	 * 
	 * @param arguments
	 *        This parameter is of type 'String...' and not 'Object...' as only a very tiny number
	 *        of types is actually supported and everything else is silently converted to "null".
	 *        Therefore, this method requires it callers to convert the arguments to type String to
	 *        avoid that "feature".
	 */
	protected static String translate(ResKey i18n, String... arguments) {
		return Resources.getInstance().getMessage(i18n, (Object[]) arguments);
	}

}
