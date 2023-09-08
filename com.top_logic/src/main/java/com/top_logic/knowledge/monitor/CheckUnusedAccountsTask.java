/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.monitor;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.util.Computation;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.util.sched.task.impl.TaskImpl;

/**
 * Task checking all accounts whether they are unused.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
@InApp
public class CheckUnusedAccountsTask<C extends CheckUnusedAccountsTask.Config<?>> extends TaskImpl<C> {

	/** The name of this Task. */
	public static final String TASK_NAME = "CheckUnusedAccountsTask";

	/**
	 * Configuration for the {@link CheckUnusedAccountsTask}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config<I extends CheckUnusedAccountsTask<?>> extends TaskImpl.Config<I> {

		@StringDefault(TASK_NAME)
		@Override
		String getName();

		/**
		 * Configuration of the actual checker.
		 */
		@Mandatory
		PolymorphicConfiguration<UnusedAccountCheck> getChecker();

	}

	final UnusedAccountCheck _unusedAccountsChecker;

	/**
	 * Creates a {@link CheckUnusedAccountsTask} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public CheckUnusedAccountsTask(InstantiationContext context, C config) {
		super(context, config);
		_unusedAccountsChecker = context.getInstance(config.getChecker());
	}

    @Override
    public void run() {
        super.run(); // as wished by super class
        ThreadContext.inSystemContext(CheckUnusedAccountsTask.class, new Computation<Void>() {
            @Override
			public Void run() {
                Logger.info("Checking unused accounts...", CheckUnusedAccountsTask.class);
				try (Transaction transaction = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
					int[] result = _unusedAccountsChecker.checkUnusedAccounts(false);
                    if (result[0] > 0 || result[1] > 0 || result[2] > 0) {
                        transaction.commit();
                    }
                    String message = "Check done. Notified " + result[1] + " user and deleted " + result[2] + " acounts.";
                    Logger.info(message, CheckUnusedAccountsTask.class);
                }
                catch (KnowledgeBaseException ex) {
                    Logger.error("Failed to commit changes while checking unused accounts.", ex, CheckUnusedAccountsTask.class);
                }
                return null;
            }
        });
    }

}
