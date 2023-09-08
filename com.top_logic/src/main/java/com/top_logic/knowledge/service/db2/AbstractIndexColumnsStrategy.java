/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.schema.config.annotation.IndexColumnsStrategy;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.dob.sql.DBIndex;
import com.top_logic.knowledge.service.BasicTypes;

/**
 * Base class for {@link IndexColumnsStrategy} implementations.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractIndexColumnsStrategy implements IndexColumnsStrategy,
		ConfiguredInstance<PolymorphicConfiguration<IndexColumnsStrategy>> {

	private final PolymorphicConfiguration<IndexColumnsStrategy> _config;

	/**
	 * Creates a {@link AbstractIndexColumnsStrategy} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AbstractIndexColumnsStrategy(InstantiationContext context, PolymorphicConfiguration<IndexColumnsStrategy> config) {
		super();
		_config = config;
	}

	@Override
	public PolymorphicConfiguration<IndexColumnsStrategy> getConfig() {
		return _config;
	}

	@Override
	public final List<DBAttribute> createIndexColumns(MOClass type, DBIndex index) {
		if (index.isCustom()) {
			if (index.isUnique()) {
				if (!hasBranchColumn(index) && type.getDBMapping().multipleBranches()) {
					throw new IllegalArgumentException("Unique index '" + index.getName()
						+ "' must include the '" + BasicTypes.BRANCH_ATTRIBUTE_NAME + "' column.");
				}

				if (!hasVersionColumn(index)) {
					throw new IllegalArgumentException("Unique index '" + index.getName()
						+ "' in a versioned type must include the '" + BasicTypes.REV_MAX_ATTRIBUTE_NAME
						+ "' column.");
				}
			}

			return index.getKeyAttributes();
		}
		
		return internalCreateIndexColumns(type, index);
	}

	private boolean hasVersionColumn(DBIndex index) {
		// Note: Only REV_MAX can be used as version column for ensuring the index uniqueness
		// property. REV_MIN would neither make the index unique, nor would it prevent the
		// application from inserting non-unique custom values: With REV_MIN in a unique index, the
		// same value could be set to different objects, when the change occurs in different
		// commits, since REV_MIN carries the information when the change happened.
		return hasColumn(BasicTypes.REV_MAX_ATTRIBUTE_NAME, index);
	}

	private boolean hasBranchColumn(DBIndex index) {
		return hasColumn(BasicTypes.BRANCH_ATTRIBUTE_NAME, index);
	}

	/**
	 * Whether a column with the given name is among the columns in the given index.
	 */
	protected static boolean hasColumn(String expectedColumnName, DBIndex index) {
		for (MOAttribute attribute : index.getAttributes()) {
			if (attribute.getName().equals(expectedColumnName)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Custom implementation of {@link #createIndexColumns(MOClass, DBIndex)}.
	 */
	protected abstract List<DBAttribute> internalCreateIndexColumns(MOClass type, DBIndex index);

}
