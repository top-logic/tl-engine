/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration;

import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.db2.migration.formats.ValueType;

/**
 * XML entity names of a knowledge base dump file.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface DumpSchemaConstants {

	/**
	 * The top-level element of a dump.
	 */
	String DATA = "data";

	/**
	 * Model section of the dump (currently unused).
	 */
	String MODEL = "model";

	/**
	 * The version descriptor of the dump.
	 */
	String VERSION = "version";

	/**
	 * Descriptor of a module version within {@link #VERSION}.
	 */
	String MODULE = "module";

	/**
	 * Module name attribute of {@link #MODULE}.
	 */
	String MODULE_NAME_ATTR = "name";

	/**
	 * Module version attribute of {@link #MODULE}.
	 */
	String MODULE_VERSION_ATTR = "version";

	/**
	 * Collection of {@link #CHANGE_SET} elements.
	 */
	String CHANGE_SETS = "changesets";

	/**
	 * All changes in a revision.
	 * 
	 * @see #CREATION
	 * @see #DELETION
	 * @see #UPDATE
	 */
	String CHANGE_SET = "changeset";

	/**
	 * The revision of a {@link #CHANGE_SET}.
	 */
	String REVISION_ATTR = "rev";

	/**
	 * The author of a {@link #CHANGE_SET}.
	 */
	String AUTHOR_ATTR = "author";

	/**
	 * The commit date of a {@link #CHANGE_SET}.
	 */
	String DATE_ATTR = "date";

	/**
	 * The commit message of a {@link #CHANGE_SET}.
	 */
	String MESSAGE_ATTR = "message";

	/**
	 * A creation event in a {@link #CHANGE_SET}.
	 */
	String CREATION = "add";

	/**
	 * A deletion event in a {@link #CHANGE_SET}.
	 */
	String DELETION = "delete";

	/**
	 * An update event in a {@link #CHANGE_SET}.
	 */
	String UPDATE = "update";

	/**
	 * The object type of a {@link #CREATION}, {@link #DELETION}, or {@link #UPDATE} event.
	 */
	String TYPE_ATTR = "type";

	/**
	 * The object identifier in the format <code>branch/id</code> of a {@link #CREATION},
	 * {@link #DELETION}, or {@link #UPDATE} event.
	 */
	String ID_ATTR = "id";

	/**
	 * A single property set in a {@link #CREATION}, or {@link #UPDATE} event.
	 */
	String PROPERTY = "prop";

	/**
	 * The name of the property.
	 * 
	 * @see #PROPERTY
	 */
	String NAME_ATTR = "name";

	/**
	 * The new value of the {@link #PROPERTY}.
	 */
	String VALUE_ATTR = "value";

	/**
	 * The {@link ValueType} of the overwritten value of a {@link #PROPERTY} in an {@link #UPDATE}
	 * event.
	 */
	String OLD_TYPE_ATTR = "old-type";

	/**
	 * The overwritten value of a {@link #PROPERTY} in an {@link #UPDATE} event.
	 */
	String OLD_VALUE_ATTR = "old-value";

	/**
	 * A branch event in a {@link #CHANGE_SET}.
	 */
	String BRANCH = "branch";

	/**
	 * The ID of the newly created branch in a {@link #BRANCH} event.
	 */
	String BRANCH_ID_ATTR = "id";

	/**
	 * The ID of the base branch in a {@link #BRANCH} event.
	 */
	String BASE_BRANCH_ID_ATTR = "base-branch";

	/**
	 * The base revision that was branched in a {@link #BRANCH} event.
	 */
	String BASE_REV_ATTR = "base-ref";

	/**
	 * Branched type in {@link #BRANCH}.
	 */
	String TYPE = "type";

	/**
	 * The name of the type.
	 * 
	 * @see #TYPE
	 */
	String TYPE_NAME_ATTR = "name";

	/** Tag containing all non {@link KnowledgeBase} tables. */
	String TABLES = "tables";

	/** Tag containing values for a {@link KnowledgeBase} tables. */
	String TABLE = "table";

	/**
	 * The name of a non {@link KnowledgeBase} table.
	 * 
	 * @see #TABLE
	 */
	String TABLE_NAME = "name";

	/**
	 * Tag containing the values of a row in a non {@link KnowledgeBase} table.
	 * 
	 * @see #TABLE
	 */
	String ROW = "row";

	/** Tag containing all unversioned {@link KnowledgeBase} types. */
	String UNVERSIONED_TYPES = "types";

	/** Tag containing values for an unversioned {@link KnowledgeBase} type. */
	String UNVERSIONED_TYPE = "type";

	/** Tag containing values for an unversioned {@link KnowledgeItem}. */
	String UNVERSIONED_ITEM = "item";

	/** Tag containing an error that occur when the previous sibling was produced. */
	String ERROR_TAG = "error";

}
