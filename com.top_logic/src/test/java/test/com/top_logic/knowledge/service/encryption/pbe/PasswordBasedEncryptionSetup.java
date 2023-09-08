/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.encryption.pbe;

import java.sql.SQLException;

import junit.extensions.TestSetup;
import junit.framework.Test;

import com.top_logic.basic.encryption.EncryptionService;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.util.DBOperation;
import com.top_logic.knowledge.service.encryption.pbe.ApplicationPasswordUtil;
import com.top_logic.knowledge.service.encryption.pbe.InvalidPasswordException;
import com.top_logic.knowledge.service.encryption.pbe.PasswordBasedEncryptionService;
import com.top_logic.util.TLContext;

/**
 * {@link TestSetup} that starts the {@link PasswordBasedEncryptionService} with a dummy password.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PasswordBasedEncryptionSetup extends TestSetup {

	/**
	 * Wraps the given test with a {@link PasswordBasedEncryptionSetup}.
	 */
	public static PasswordBasedEncryptionSetup setup(Test test) {
		return new PasswordBasedEncryptionSetup(test);
	}

	private PasswordBasedEncryptionSetup(Test test) {
		super(test);
	}

	@Override
	protected void setUp() throws Exception {
		final String password = "test123";
		if (!ApplicationPasswordUtil.hasPassword()) {
			TLContext.inSystemContext(ApplicationPasswordUtil.class, new DBOperation() {
				@Override
				protected void update(PooledConnection connection) throws ModuleException, InvalidPasswordException,
						SQLException {
					ApplicationPasswordUtil.setupPassword(connection, password);
				}
			}).reportProblem();

		}
		PasswordBasedEncryptionService.startUp(password.toCharArray());
	}

	@Override
	protected void tearDown() throws Exception {
		EncryptionService.stop();
	}

}