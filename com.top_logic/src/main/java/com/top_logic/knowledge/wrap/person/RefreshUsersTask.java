/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.person;

import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.base.security.device.interfaces.PersonDataAccessDevice;
import com.top_logic.base.user.UserInterface;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.util.sched.task.Task;
import com.top_logic.util.sched.task.impl.StateHandlingTask;

/**
 * {@link Task} to synchronized accounts with external systems such as LDAP.
 * 
 * @author <a href="mailto:tri@top-logic.com">Thomas Richter</a>
 */
@InApp
public class RefreshUsersTask<C extends RefreshUsersTask.Config<?>> extends StateHandlingTask<C> {

	/**
	 * Configuration options for {@link RefreshUsersTask}.
	 */
	public interface Config<I extends RefreshUsersTask<?>> extends StateHandlingTask.Config<I> {
		/**
		 * The maximum number of accounts to delete during a synchronization run.
		 * 
		 * <p>
		 * If more accounts should be removed from the system in a synchronization run as given in
		 * this limit, this is interpreted as invalid configuration in the remote system. In that
		 * case, no accounts are removed at all, but the task reports an error.
		 * </p>
		 * 
		 * <p>
		 * The special value of <code>-1</code> means that no limit should be enforced.
		 * </p>
		 * 
		 * <p>
		 * A value of <code>0</code> means that accounts are created during synchronization, but
		 * never removed from this system. An account removal in a remote system is silently
		 * ignored, without producing an error during synchronization.
		 * </p>
		 */
		@IntDefault(-1)
		int getAccountCleanupLimit();
	}

	/**
	 * Creates a {@link RefreshUsersTask} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public RefreshUsersTask(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	protected void runHook() {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		Transaction tx = kb.beginTransaction();
		try {
			TLSecurityDeviceManager deviceManager = TLSecurityDeviceManager.getInstance();
			Set<String> deviceIds = deviceManager.getConfiguredDataAccessDeviceIDs();
			Set<Person> deletedRemoteAccounts =
				Person.all().stream().filter(p -> deviceIds.contains(p.getDataDeviceId())).collect(Collectors.toSet());

			for (String deviceId : deviceIds) {
				PersonDataAccessDevice device = deviceManager.getDataAccessDevice(deviceId);
				String authenticationDeviceID = device.getAuthenticationDeviceID();

				for (UserInterface user : device.getAllUserData()) {
					String userName = user.getUserName();
					if (StringServices.isEmpty(userName)) {
						Logger.warn("Encountered empty username in '" + deviceId + "' - entry ignored.", this);
						continue;
					}

					Person account = Person.byName(userName);
					if (account != null) {
						deletedRemoteAccounts.remove(account);
					} else {
						account =
							Person.create(PersistencyLayer.getKnowledgeBase(), userName, authenticationDeviceID);
						account.setDataDeviceId(deviceId);
					}

					account.getUser();
				}
			}

			int cleanupLimit = getConfig().getAccountCleanupLimit();
			if (cleanupLimit > 0 && deletedRemoteAccounts.size() > cleanupLimit) {
				// No deletion for safety reasons.
				getLog().getCurrentResult()
					.addWarning("Skipped excessive account deletion: " + deletedRemoteAccounts.size());
			} else if (cleanupLimit == 0) {
				// Ignore deletion.
			} else {
				// Delete accounts no longer found in remote systems.
				for (Person account : deletedRemoteAccounts) {
					// Account was imported but does no longer exist in the remote system.
					account.tDelete();
				}
			}

			tx.commit();
		} finally {
			tx.rollback();
		}
	}

	@Override
	public boolean isNodeLocal() {
		return false;
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
