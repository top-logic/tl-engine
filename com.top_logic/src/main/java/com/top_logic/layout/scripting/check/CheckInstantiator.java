/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.check;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.scripting.check.AbstractFileEqualityCheck.FileMatchConfig;
import com.top_logic.layout.scripting.check.EqualsCheck.EqualsCheckConfig;
import com.top_logic.layout.scripting.check.NullCheck.NullCheckConfig;
import com.top_logic.layout.scripting.recorder.ref.DataItemValue;
import com.top_logic.layout.scripting.recorder.ref.ModelName;

/**
 * Contains methods for instantiating {@link ValueCheck}s.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class CheckInstantiator {

	public static EqualsCheckConfig equalsCheck(ModelName expectedValue) {
		EqualsCheckConfig equalsCheck = TypedConfiguration.newConfigItem(EqualsCheck.EqualsCheckConfig.class);
		equalsCheck.setImplementationClass(EqualsCheck.class);
		equalsCheck.setExpectedValue(expectedValue);
		return equalsCheck;
	}

	/**
	 * Convenience method for creating an filling a {@link NullCheckConfig}.
	 * 
	 * @param isNull
	 *        Has the value to be <code>null</code>, or must it be not <code>null</code>?
	 * @return Never <code>null</code>.
	 */
	public static NullCheckConfig nullCheck(boolean isNull) {
		NullCheckConfig equalsCheck = TypedConfiguration.newConfigItem(NullCheckConfig.class);
		equalsCheck.setNull(isNull);
		return equalsCheck;
	}

	public static FileMatchConfig binaryFileEqualityCheck(DataItemValue expectedValue) {
		FileMatchConfig equalsCheck = TypedConfiguration.newConfigItem(AbstractFileEqualityCheck.FileMatchConfig.class);
		equalsCheck.setImplementationClass(BinaryFileEqualityCheck.class);
		equalsCheck.setExpectedValue(expectedValue);
		return equalsCheck;
	}

}
