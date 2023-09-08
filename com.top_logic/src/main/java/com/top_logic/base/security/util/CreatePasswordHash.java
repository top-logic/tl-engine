/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.util;

import static com.top_logic.basic.shared.string.StringServicesShared.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.top_logic.basic.Logger;
import com.top_logic.basic.XMain;
import com.top_logic.basic.encryption.EncryptionService;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.knowledge.service.encryption.SecurityService;
import com.top_logic.knowledge.service.encryption.pbe.PasswordBasedEncryptionService;

/**
 * Program to create a password hash that can be manually written to the database, e.g. if the root
 * password was lost.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CreatePasswordHash extends XMain {

	@Override
	protected void doActualPerformance() throws Exception {
		initFileManager();
		initXMLProperties();

		setupModuleContext(SignatureService.Module.INSTANCE);

		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		if (EncryptionService.Module.INSTANCE.isActive()) {
			EncryptionService encryptionService = EncryptionService.getInstance();
			if (encryptionService instanceof PasswordBasedEncryptionService) {
				ModuleUtil.INSTANCE.startUp(SecurityService.Module.INSTANCE);
				System.out.print("Application password: ");
				String masterPassword = in.readLine();
				PasswordBasedEncryptionService.usePassword((PasswordBasedEncryptionService) encryptionService,
					nonNull(masterPassword).toCharArray());
			}
		}

		SignatureService signatureService = SignatureService.getInstance();

		System.out.print("User password: ");
		String password = in.readLine();

		System.out.print("Hash: ");
		System.out.print(signatureService.sign(password));
	}

	public static void main(String[] args) throws Exception {
		Logger.configureStdout();
		CreatePasswordHash tool = new CreatePasswordHash();
		tool.interactive = false;
		tool.runMainCommandLine(args);
	}

}
