/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.filters;

import java.util.Collections;

import com.google.inject.Inject;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.filter.typed.FilterResult;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ItemChange;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.service.db2.migration.rewriters.Indexer;
import com.top_logic.knowledge.service.db2.migration.rewriters.Indexer.Index;

/**
 * {@link DelegatingMigrationFilter} checking creations of objects which are set as reference in the
 * same {@link ChangeSet}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class NavigateReferenceFilter extends DelegatingMigrationFilter<NavigateReferenceFilter.Config> {

	private final String _reference;

	private final String _type;

	private Index _index;

	/**
	 * Configuration of a {@link NavigateReferenceFilter}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends DelegatingMigrationFilter.Config<NavigateReferenceFilter> {

		/** Name of property {@link #getType}. */
		String TYPE = "type";

		/** Name of property {@link #getReference}. */
		String REFERENCE = "reference";

		/**
		 * Name of a reference attribute.
		 * 
		 * <p>
		 * When an object was changed and the value of this attribute is a newly created object,
		 * that {@link ObjectCreation} is checked.
		 * </p>
		 */
		@Name(REFERENCE)
		String getReference();

		/**
		 * Name of a {@link MetaObject}.
		 * 
		 * <p>
		 * When an object with that type was changed and the value of {@link #getReference()} is a
		 * newly created object, that {@link ObjectCreation} is checked.
		 * </p>
		 */
		@Name(TYPE)
		String getType();

	}

	/**
	 * Creates a {@link NavigateReferenceFilter} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public NavigateReferenceFilter(InstantiationContext context, Config config) {
		super(context, config);

		_reference = config.getReference();
		_type = config.getType();
	}

	/**
	 * Initialised the index of this {@link NavigateReferenceFilter}.
	 */
	@Inject
	public void initIndexer(Indexer indexer) {
		_index = indexer.register(_type,
			Collections.singletonList(Indexer.SELF_ATTRIBUTE),
			Collections.singletonList(_reference));
	}

	@Override
	protected FilterResult matchesTypesafe(ItemChange event) {
		ObjectKey targetKey = (ObjectKey) _index.getValue(event.getObjectId().toCurrentObjectKey());
		if (targetKey == null) {
			return FilterResult.FALSE;
		}

		MetaObject targetType = targetKey.getObjectType();
		ChangeSet cs = migration().current();
		for (ObjectCreation creation : cs.getCreations()) {
			if (!targetType.equals(creation.getObjectType())) {
				continue;
			}
			
			if (!creation.getObjectId().toCurrentObjectKey().equals(targetKey)) {
				continue;
			}

			return filter().matches(creation);
		}
		return FilterResult.FALSE;
	}

}