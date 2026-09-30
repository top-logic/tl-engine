/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta.search;

import com.top_logic.basic.i18n.CustomKey;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * Internationalization constants for this package.
 *
 * @see ResPrefix
 * @see ResKey
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	@CustomKey("tl.search.thread.columns.get")
	public static ResKey PHASE_BUILDING_RESULT_COLUMNS;

	@CustomKey("element.search.query.create")
	public static ResKey CREATE_QUERY;

	@CustomKey("element.search.query.delete")
	public static ResKey DELETE_QUERY;

	@CustomKey("tl.search.thread.elements.get")
	public static ResKey PHASE_LOADING_DATA;

	@CustomKey("element.search.execute")
	public static ResKey START_SEARCH;

	@CustomKey("tl.search.thread.search.execute.filter")
	public static ResKey2 PHASE_FILTER__POS_SIZE;

	@CustomKey("tl.search.thread.filters.get")
	public static ResKey PHASE_LOADING_FILTERS;

	@CustomKey("element.search.invalid")
	public static ResKey ERROR_INVALID_INPUT;

	@CustomKey("tl.search.thread.metaElement.get")
	public static ResKey PHASE_RESOLVING_SEARCH_TYPE;

	@CustomKey("element.search.query.save.query.notSelected")
	public static ResKey ERROR_NO_QUERY_SELECTED;

	@CustomKey("element.search.query.save.query.failed")
	public static ResKey ERROR_QUERY_FAILED;

	@CustomKey("element.search.reset")
	public static ResKey RESET_SEARCH;

	@CustomKey("element.search.query.save")
	public static ResKey SAVE_QUERY;

	@CustomKey("tl.search.thread.search.execute")
	public static ResKey PHASE_EXECUTING_SEARCH;

	@CustomKey("element.search.query.switch.toExtended")
	public static ResKey SWITCH_TO_EXTENDED_MODE;

	@CustomKey("element.search.query.switch.toNormal")
	public static ResKey SWITCH_TO_NORMAL_MODE;

	/**
	 * @en No search result available
	 */
	public static ResKey NOT_EXECUTABLE_NO_SEARCH_RESULT;

	/**
	 * @en No search has been performed
	 */
	public static ResKey NOT_EXECUTABLE_NO_SEARCH_EXECUTED;

	static {
		initConstants(I18NConstants.class);
	}
}
