/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.person;

import java.util.Properties;

import com.top_logic.base.locking.Lock;
import com.top_logic.basic.DebugHelper;
import com.top_logic.basic.Logger;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.col.Provider;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.util.Computation;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.util.sched.task.Task;
import com.top_logic.util.sched.task.impl.TaskImpl;
import com.top_logic.util.sched.task.result.TaskResult.ResultType;

/**
 * {@link Task} that calls {@link PersonManager#initUserMap() updates} cache in the
 * {@link PersonManager}.
 * 
 * @author <a href="mailto:tri@top-logic.com">Thomas Richter</a>
 */
@InApp
public class RefreshUsersTask<C extends RefreshUsersTask.Config<?>> extends TaskImpl<C> {

	/**
	 * Configuration options for {@link RefreshUsersTask}.
	 */
	public interface Config<I extends RefreshUsersTask<?>> extends TaskImpl.Config<I> {

		/**
		 * @see #getKnowledgeBaseProvider()
		 */
		static final String KNOWLEDGE_BASE_PROPERTY = "knowledge-base";

		/**
		 * @see #getPersonManagerProvider()
		 */
		static final String PERSON_MANAGER_PROPERTY = "person-manager";

		/**
		 * {@link PersonManager}, which shall by updated by this task
		 */
		@InstanceDefault(DefaultPersonManagerProvider.class)
		@InstanceFormat
		@Name(PERSON_MANAGER_PROPERTY)
		Provider<PersonManager> getPersonManagerProvider();

		/**
		 * @see #getPersonManagerProvider()
		 */
		void setPersonManagerProvider(Provider<PersonManager> value);

		/**
		 * {@link KnowledgeBase}, to which changes, made by this task, will be committed to
		 */
		@InstanceDefault(DefaultKnowledgeBaseProvider.class)
		@InstanceFormat
		@Name(KNOWLEDGE_BASE_PROPERTY)
		Provider<KnowledgeBase> getKnowledgeBaseProvider();

		/**
		 * @see #getKnowledgeBaseProvider()
		 */
		void setKnowledgeBaseProvider(Provider<KnowledgeBase> value);
	}

	private PersonManager personManager;
	private KnowledgeBase knowledgeBase;

	/**
	 * @param prop
	 *        the task scheduling properties
	 */
	public RefreshUsersTask(Properties prop) {
		super(prop);
		logInfo();
	}

	private void logInfo() {
		Logger.info("RefreshUsersTask loaded.", this);
	}

	public RefreshUsersTask(InstantiationContext context, C config) {
		super(context, config);
		this.knowledgeBase = config.getKnowledgeBaseProvider().get();
		this.personManager = config.getPersonManagerProvider().get();

		logInfo();
	}

	/**
	 * Entry to start this tasks thread. Re initialize the user map.
	 */
	@Override
	public synchronized void run() {
		super.run();

		getLog().taskStarted();

		ThreadContext.inSystemContext(RefreshUsersTask.class, new Computation<Void>() {

			@Override
			public Void run() {
				KnowledgeBase theKB = knowledgeBase;
				try {
					long start = System.currentTimeMillis();

					Transaction tx = theKB.beginTransaction();
					try {
						Lock theTokenCtxt = personManager.initUserMap();
						try {
							if (theTokenCtxt != null && theTokenCtxt.isStateAcquired() && theTokenCtxt.check()) {
								try {
									tx.commit();
								} catch (KnowledgeBaseException ex) {
									getLog().taskEnded(ResultType.ERROR, I18NConstants.REFRESH_USERS_COMMIT_FAILED, ex);

									Logger.error("Failed to refresh users (commit failed)", ex, this);
									return null;
								}
							}
						} finally {
							if (theTokenCtxt != null) {
								try {
									theTokenCtxt.unlock();
								} catch (Exception ex) {
									// Ignore.
								}
							} else {
								Logger.warn("Unable to get token for user refresh. Skipping this cycle", this);
							}
						}
					} finally {
						tx.rollback();
					}
					long delta = System.currentTimeMillis() - start;
					String duration = DebugHelper.getTime(delta);

					if (delta > 3000) {
						Logger.info("run() needed " + duration, this);
					}

					ResKey i18nMessage =
						I18NConstants.REFRESH_USERS_SUCCESS__DURATION.fill(duration);
					getLog().taskEnded(ResultType.SUCCESS, i18nMessage);
				} catch (Exception ex) {
					Logger.error("run(): Unexpected Error during refreshing of Users:", ex, this);
					getLog().taskEnded(ResultType.ERROR, ResultType.ERROR.getMessageI18N(), ex);
				}
				return null;
			}
		});
	}

	/**
     * Don't interrupt work...
	 */
	@Override
	public boolean signalStopHook() {
		//make sure we are not shut down by force during a cycle 
		//as this could cause corrupted database
		return false;
	}

}
