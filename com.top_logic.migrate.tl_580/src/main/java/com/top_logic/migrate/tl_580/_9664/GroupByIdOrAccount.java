/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.migrate.tl_580._9664;

import java.util.Collections;

import com.google.inject.Inject;

import com.top_logic.basic.StringID;
import com.top_logic.basic.TLID;
import com.top_logic.basic.col.Mapping;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.db2.DBKnowledgeAssociation;
import com.top_logic.knowledge.service.db2.migration.converters.AbstractReferenceConversion;
import com.top_logic.knowledge.service.db2.migration.converters.ReferenceConversion;
import com.top_logic.knowledge.service.db2.migration.rewriters.Indexer;
import com.top_logic.knowledge.service.db2.migration.rewriters.Indexer.Index;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tool.boundsec.wrap.Group;

/**
 * {@link ReferenceConversion} searching for a group as representative group of an account
 * identified by name or as direct group reference.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class GroupByIdOrAccount extends AbstractReferenceConversion {

	private static final Mapping<Object, String> TO_LOWER_CASE_MAPPING = new LowerCaseMapping();

	private Indexer _indexer;

	private Indexer.Index _accountIdByName;

	private Index _definesGroupDestBySource;

	private MetaObject _groupType;

	/**
	 * Initialises this {@link GroupByIdOrAccount} with the {@link Indexer} to use.
	 */
	@Inject
	public void initIndexer(Indexer indexer) {
		_indexer = indexer;

		_accountIdByName =
			_indexer.register(Person.OBJECT_NAME,
				Collections.singletonList(TO_LOWER_CASE_MAPPING),
				Collections.singletonList(Person.NAME_ATTRIBUTE),
				Collections.singletonList(Indexer.SELF_ATTRIBUTE));
		_definesGroupDestBySource =
			_indexer.register(Group.DEFINES_GROUP_ASSOCIATION,
				Collections.singletonList(DBKnowledgeAssociation.REFERENCE_SOURCE_NAME),
				Collections.singletonList(DBKnowledgeAssociation.REFERENCE_DEST_NAME));
	}

	/**
	 * Initialises this {@link GroupByIdOrAccount} with the {@link MORepository} to use.
	 */
	@Inject
	public void initMORepository(MORepository types) {
		try {
			_groupType = types.getType(Group.OBJECT_NAME);
		} catch (UnknownTypeException ex) {
			throw new KnowledgeBaseRuntimeException("Unknown type " + Group.OBJECT_NAME, ex);
		}
	}

	@Override
	public ObjectKey convertReference(ItemChange event, String dumpValue) {
		ObjectKey accountId = (ObjectKey) _accountIdByName.getValue(TO_LOWER_CASE_MAPPING.map(dumpValue));
		if (accountId != null) {
			ObjectKey groupId = (ObjectKey) _definesGroupDestBySource.getValue(accountId);
			if (groupId != null) {
				return groupId;
			} else {
				migration().error(
					"Person " + dumpValue + " without representative group referenced in bounded role association: "
						+ event);
			}
		}

		TLID groupId = StringID.valueOf(dumpValue);
		ObjectBranchId selfKey = event.getObjectId();
		return new DefaultObjectKey(selfKey.getBranchId(), Revision.CURRENT_REV, _groupType, groupId);
	}

}
