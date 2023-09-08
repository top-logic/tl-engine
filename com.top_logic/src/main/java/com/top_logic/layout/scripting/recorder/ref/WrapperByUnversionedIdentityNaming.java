/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref;

import java.util.Collections;
import java.util.Map;

import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;

/**
 * A {@link ValueNamingScheme} that recognizes a given {@link Wrapper} irrespective of its revision.
 * <p>
 * This is useful for example if a table displays {@link Wrapper}s in a historic version. In that
 * situation this {@link ValueNamingScheme} can be used to remove the need to identify that
 * revision. This works only if every {@link Wrapper} is displayed only once in the table, of
 * course.
 * </p>
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class WrapperByUnversionedIdentityNaming extends ValueNamingScheme<Wrapper> {

	/**
	 * The key under which the Wrapper is stored in the map of parts.
	 */
	private static final String WRAPPER = "wrapper";

	@Override
	public boolean isRecorded() {
		return false;
	}

	@Override
	public Class<Wrapper> getModelClass() {
		return Wrapper.class;
	}

	@Override
	public Map<String, Object> getName(Wrapper model) {
		return Collections.<String, Object> singletonMap(WRAPPER, model);
	}

	@Override
	public boolean matches(Map<String, Object> name, Wrapper model) {
		Wrapper wrongRevisionWrapper = (Wrapper) name.get(WRAPPER);
		return WrapperHistoryUtils.equalsUnversioned(wrongRevisionWrapper, model);
	}

}
