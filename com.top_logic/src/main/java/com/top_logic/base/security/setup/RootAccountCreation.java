/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.security.setup;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.base.security.device.interfaces.AuthenticationDevice;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.dob.DataObject;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.knowledge.wrap.person.Person;

/**
 * Creation of the root account.
 */
@ServiceDependencies(TLSecurityDeviceManager.Module.class)
public class RootAccountCreation extends ManagedClass {

	@Override
	protected void startUp() {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();

		DataObject existingAccount = kb.getObjectByAttribute(Person.OBJECT_NAME, Person.NAME, "root");
		if (existingAccount != null) {
			return;
		}

		try (Transaction tx = kb.beginTransaction()) {
			KnowledgeObject item = kb.createKnowledgeObject(Person.OBJECT_NAME);
			item.setAttributeValue(AbstractWrapper.NAME_ATTRIBUTE, "root");
			Person root = item.getWrapper();
			root.setAuthenticationDeviceID("dbSecurity");
			root.setAdmin(true);

			AuthenticationDevice device = TLSecurityDeviceManager.getInstance().getAuthenticationDevice("dbSecurity");
			device.setPassword(root, "root1234".toCharArray());
			tx.commit();
		}
	}

	public static final class Module extends TypedRuntimeModule<RootAccountCreation> {

		/** Singleton {@link Module} instance. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// singleton instance
		}

		@Override
		public Class<RootAccountCreation> getImplementation() {
			return RootAccountCreation.class;
		}

	}

}
