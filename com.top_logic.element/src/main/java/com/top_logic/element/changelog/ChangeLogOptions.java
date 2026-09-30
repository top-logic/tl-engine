/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.changelog;

import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.format.MillisFormat;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.util.TLModelPartRef;

/**
 * Common options selecting the change sets that make up a model change log.
 *
 * <p>
 * These options are shared by all configurations that inspect the change log, e.g. the change
 * overview table and the undo/redo commands. Aligning their settings guarantees that a command
 * operating on the change log only considers changes that a correspondingly configured overview
 * would display.
 * </p>
 *
 * @see ChangeLogBuilder#applyOptions(ChangeLogOptions)
 */
public interface ChangeLogOptions extends ConfigurationItem {

	/** Configuration name for {@link #getAllUsers()}. */
	String ALL_USERS = "all-users";

	/** Configuration name for {@link #getIncludeTechnicalChanges()}. */
	String INCLUDE_TECHNICAL_CHANGES = "include-technical-changes";

	/** Configuration name for {@link #getMaxTime()}. */
	String MAX_TIME = "max-time";

	/** Configuration name for {@link #getExcludedModules()}. */
	String EXCLUDED_MODULES = "excluded-modules";

	/**
	 * Whether to consider changes of all users instead of only those of the currently logged-in
	 * user.
	 */
	@Name(ALL_USERS)
	boolean getAllUsers();

	/**
	 * Whether to include technical changes.
	 */
	@Name(INCLUDE_TECHNICAL_CHANGES)
	boolean getIncludeTechnicalChanges();

	/**
	 * How long to look into the past.
	 *
	 * <p>
	 * A value of <code>0</code> means unlimited.
	 * </p>
	 */
	@FormattedDefault("7d")
	@Format(MillisFormat.class)
	@Name(MAX_TIME)
	long getMaxTime();

	/**
	 * Modules in this list are not observed. I.e. no changes are reported for elements whose
	 * type is part of one of this module.
	 */
	@Format(TLModelPartRef.CommaSeparatedTLModelPartRefs.class)
	@Options(fun = TLModelPartRef.AllModules.class, mapping = TLModelPartRef.PartMapping.class)
	@Name(EXCLUDED_MODULES)
	List<TLModelPartRef> getExcludedModules();

}
