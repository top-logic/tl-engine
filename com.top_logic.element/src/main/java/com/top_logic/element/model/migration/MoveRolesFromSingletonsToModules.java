/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration;

import java.util.Collection;
import java.util.Iterator;

import com.top_logic.basic.Log;
import com.top_logic.element.model.PersistentModuleSingletons;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.db2.DBKnowledgeAssociation;
import com.top_logic.knowledge.service.migration.MigrationPostProcessor;
import com.top_logic.model.impl.generated.TlModelFactory;
import com.top_logic.tool.boundsec.wrap.BoundedRole;

/**
 * {@link MigrationPostProcessor} that changes {@link BoundedRole} scopes from module singletons to
 * the module that defines the singleton.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MoveRolesFromSingletonsToModules implements MigrationPostProcessor {

	@Override
	public void afterMigration(Log log, KnowledgeBase kb) {
		Collection<KnowledgeObject> roles = kb.getAllKnowledgeObjects(BoundedRole.OBJECT_NAME);
		for (KnowledgeObject role : roles) {
			Iterator<KnowledgeAssociation> links = role.getIncomingAssociations(BoundedRole.DEFINES_ROLE_ASSOCIATION);
			if (links.hasNext()) {
				KnowledgeAssociation link = links.next();
				KnowledgeObject singleton = link.getSourceObject();
				if (TlModelFactory.KO_NAME_TL_MODULE.equals(singleton.tTable().getName())) {
					log.info("Role '" + role + "' already assigned to module '" + singleton + "'.");
					continue;
				}
				
				Iterator<KnowledgeItem> singletonLinks =
					kb.getObjectsByAttribute(PersistentModuleSingletons.OBJECT_NAME,
						PersistentModuleSingletons.DEST_ATTR, singleton);
				
				if (singletonLinks.hasNext()) {
					KnowledgeItem singletonLink = singletonLinks.next();

					KnowledgeObject module =
						(KnowledgeObject) singletonLink.getAttributeValue(PersistentModuleSingletons.SOURCE_ATTR);

					if (module != null) {
						link.setAttributeValue(DBKnowledgeAssociation.REFERENCE_SOURCE_NAME, module);

						log.info("Updated scope of '" + role + "' from '" + singleton + "' to '" + module + "'.");
					}

					while (singletonLinks.hasNext()) {
						KnowledgeItem clashLink = singletonLinks.next();

						KnowledgeObject clashModule =
							(KnowledgeObject) singletonLink.getAttributeValue(PersistentModuleSingletons.SOURCE_ATTR);

						log.info("Removed duplicate singleton link for singleton '" + singleton
							+ " (binding the singleton to module '" + clashModule + "' instead of '" + module + "')'.",
							Log.WARN);
						clashLink.delete();
					}
				} else {
					log.info("Found role '" + role + "' not defined by a singleton, but '" + singleton + "'.",
						Log.WARN);
				}

				while (links.hasNext()) {
					KnowledgeAssociation clashLink = links.next();

					KnowledgeObject clashSingleton = clashLink.getSourceObject();

					log.info(
						"Removed duplicate binding for role '" + role + "' (binding to the singleton '" + clashSingleton
							+ "' instead of '" + singleton + "')'.",
						Log.WARN);
					clashLink.delete();
				}
			}
		}
	}

}
