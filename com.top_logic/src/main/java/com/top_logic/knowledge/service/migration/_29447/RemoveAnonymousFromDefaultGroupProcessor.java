/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration._29447;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.Log;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.module.ManagedClass.ServiceConfiguration;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.knowledge.objects.KnowledgeAssociation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.migration.MigrationPostProcessor;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.knowledge.wrap.exceptions.WrapperRuntimeException;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.tool.boundsec.wrap.Group;

/**
 * {@link MigrationPostProcessor} removing the anonymous account from the default group (Ticket
 * #29447).
 *
 * <p>
 * The anonymous account represents a visitor that did not log in, while the default group is the
 * group that all accounts have and therefore typically carries the roles of an ordinary user. With
 * model-based access rights, an anonymous visitor would inherit whatever those roles grant. New
 * accounts stay out of the default group since {@link com.top_logic.knowledge.wrap.person.PersonGroupsInitializer}
 * skips it for the anonymous account; existing databases still hold the membership created before.
 * </p>
 *
 * @implNote Operates on the persistency layer only: the model service is not available in a
 *           {@link MigrationPostProcessor}. Besides the account itself, its
 *           {@link Group#isRepresentativeGroup() representative group} was added to the default
 *           group as well (see {@link Group#addMember(com.top_logic.model.TLObject)}), so both
 *           memberships are removed.
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class RemoveAnonymousFromDefaultGroupProcessor implements MigrationPostProcessor {

	@Override
	public void afterMigration(Log log, KnowledgeBase kb) {
		String anonymousName = anonymousUserName(log);
		if (anonymousName == null) {
			return;
		}

		KnowledgeObject account = accountByName(kb, anonymousName);
		if (account == null) {
			log.info("No account '" + anonymousName + "' exists, nothing to remove from the default group.");
			return;
		}

		Set<KnowledgeItem> anonymousMembers = new HashSet<>();
		anonymousMembers.add(account);
		anonymousMembers.addAll(representativeGroups(log, account));

		int removed = 0;
		for (KnowledgeObject group : kb.getAllKnowledgeObjects(Group.OBJECT_NAME)) {
			if (!isDefaultGroup(log, group)) {
				continue;
			}
			for (KnowledgeItem member : anonymousMembers) {
				removed += removeMembership(group, member);
			}
		}
		if (removed == 0) {
			log.info("The account '" + anonymousName + "' is not a member of a default group.");
		} else {
			log.info("Removed the account '" + anonymousName + "' from the default group ("
				+ removed + " membership(s)).");
		}
	}

	/**
	 * The configured name of the anonymous account, or <code>null</code>, if it cannot be
	 * determined.
	 */
	private String anonymousUserName(Log log) {
		try {
			ServiceConfiguration<PersonManager> serviceConfig =
				ApplicationConfig.getInstance().getServiceConfiguration(PersonManager.class);
			return Person.normalizeName(((PersonManager.Config) serviceConfig).getAnonymousUserName());
		} catch (ConfigurationException ex) {
			log.error("Unable to determine the name of the anonymous account.", ex);
			return null;
		}
	}

	private KnowledgeObject accountByName(KnowledgeBase kb, String name) {
		return (KnowledgeObject) kb.getObjectByAttribute(Person.OBJECT_NAME, AbstractWrapper.NAME_ATTRIBUTE, name);
	}

	/**
	 * The groups the given account defines, i.e. its representative group.
	 */
	private Set<KnowledgeItem> representativeGroups(Log log, KnowledgeObject account) {
		Set<KnowledgeItem> result = new HashSet<>();
		Iterator<KnowledgeAssociation> links = account.getOutgoingAssociations(Group.DEFINES_GROUP_ASSOCIATION);
		while (links.hasNext()) {
			try {
				result.add(links.next().getDestinationObject());
			} catch (WrapperRuntimeException ex) {
				log.error("Unable to resolve the representative group of the anonymous account.", ex);
			}
		}
		return result;
	}

	private boolean isDefaultGroup(Log log, KnowledgeObject group) {
		try {
			return Boolean.TRUE.equals(group.getAttributeValue(Group.GROUP_DEFAULT));
		} catch (NoSuchAttributeException ex) {
			log.error("No '" + Group.GROUP_DEFAULT + "' attribute in table '" + Group.OBJECT_NAME + "'.", ex);
			return false;
		}
	}

	/**
	 * Deletes the membership links from the given group to the given member.
	 *
	 * @return The number of deleted links.
	 */
	private int removeMembership(KnowledgeObject group, KnowledgeItem member) {
		List<KnowledgeAssociation> links = new ArrayList<>();
		Iterator<KnowledgeAssociation> it =
			group.getOutgoingAssociations(Group.GROUP_MEMBERS_ASSOCIATION, (KnowledgeObject) member);
		while (it.hasNext()) {
			links.add(it.next());
		}
		KBUtils.deleteAllKI(links.iterator());
		return links.size();
	}

}
