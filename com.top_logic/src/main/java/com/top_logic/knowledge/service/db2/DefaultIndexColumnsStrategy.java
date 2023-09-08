/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.schema.config.annotation.IndexColumnsStrategy;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.dob.sql.DBIndex;
import com.top_logic.knowledge.service.BasicTypes;

/**
 * Configurable {@link IndexColumnsStrategy}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultIndexColumnsStrategy extends AbstractIndexColumnsStrategy {

	/**
	 * Configuration for {@link DefaultIndexColumnsStrategy}.
	 */
	public interface Config extends PolymorphicConfiguration<IndexColumnsStrategy> {

		/**
		 * Whether the branch column should be excluded from non-unique indices.
		 * 
		 * <p>
		 * For unique indices, the branch is required in the index to enforce uniqueness only within
		 * a branch. Without the branch column in the index, the branch operation is guaranteed to
		 * fail, because the uniqueness constraint is violated.
		 * </p>
		 * 
		 * <p>
		 * Since most queries are branch-local, the branch must even be added for non unique indices
		 * to search efficiently.
		 * </p>
		 */
		boolean getExcludeBranchFromNonUniqueIndices();
		
		/**
		 * Whether the revision column should be excluded from non-unique indices.
		 * 
		 * <p>
		 * For unique indices, a revision column is required in the index to enforce uniqueness only
		 * within a single time-slice in history. Without a revision column in the index, the
		 * updating a versioned object is guaranteed to fail, if not all properties included in the
		 * index are changed.
		 * </p>
		 * 
		 * <p>
		 * Even for non-unique indices, a revision column can speed up searches in the current
		 * revision (or near current revisions), since less versions of a long-lived object must be
		 * skipped.
		 * </p>
		 */
		boolean getExcludeRevisionFromNonUniqueIndices();

		/**
		 * Whether the branch column should be appended to the custom columns instead of prepended
		 * to them.
		 * 
		 * <p>
		 * Since most searches are branch-local, the branch column is always known in searches using
		 * the index. Therefore, it is most likely, that the index can be used for the search.
		 * However, for branch-global searches, the branch column in front of the custom columns
		 * prevents the index from being used. In systems with few branches, the DBMS may also
		 * decide not to use an index with a column in front of the index that does not have many
		 * different values.
		 * </p>
		 */
		boolean getBranchAfterCustomColumns();
		
	}

	/**
	 * Creates a {@link DefaultIndexColumnsStrategy} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DefaultIndexColumnsStrategy(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected List<DBAttribute> internalCreateIndexColumns(MOClass type, DBIndex index) {
		List<DBAttribute> attributes = index.getKeyAttributes();
		boolean unique = index.isUnique();
		boolean excludeBranch;
		if (type.getDBMapping().multipleBranches()) {
			excludeBranch = config().getExcludeBranchFromNonUniqueIndices() && (!unique);
		} else {
			excludeBranch = true;
		}
		boolean excludeRevision = config().getExcludeRevisionFromNonUniqueIndices() && (!unique);
		boolean branchAfterCustomColumns = config().getBranchAfterCustomColumns();

		ArrayList<DBAttribute> result = new ArrayList<>(attributes.size() + 2);

		if (!excludeBranch && !branchAfterCustomColumns) {
			Collections.addAll(result, type.getAttributeOrNull(BasicTypes.BRANCH_ATTRIBUTE_NAME).getDbMapping());
		}

		result.addAll(attributes);

		if (!excludeBranch && branchAfterCustomColumns) {
			Collections.addAll(result, type.getAttributeOrNull(BasicTypes.BRANCH_ATTRIBUTE_NAME).getDbMapping());
		}

		if (!excludeRevision) {
			// The uniqueness must only be ensured in the current revision. This
			// is sufficient to ensure that the constraint holds in all stable
			// revisions. Without this column in the index, uniqueness of values
			// would be enforced throughout history and updating an attribute to
			// a value it already had would fail.
			Collections.addAll(result, type.getAttributeOrNull(BasicTypes.REV_MAX_ATTRIBUTE_NAME).getDbMapping());
		}

		return result;
	}

	private Config config() {
		return (Config) getConfig();
	}

	static DefaultIndexColumnsStrategy defaultInstance() {
		Config config = TypedConfiguration.newConfigItem(DefaultIndexColumnsStrategy.Config.class);
		config.setImplementationClass(DefaultIndexColumnsStrategy.class);
		return new DefaultIndexColumnsStrategy(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config);
	}

}
