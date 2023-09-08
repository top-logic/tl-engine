/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.scripting.recorder.ref;

import java.util.Collections;
import java.util.Map;

import com.top_logic.layout.scripting.recorder.ref.ValueNamingScheme;
import com.top_logic.util.Utils;

/**
 * A {@link ValueNamingScheme} for tests.
 * <p>
 * It is registered for class {@link Object}, but {@link #supportsModel(Object) supports} only the
 * value set via {@link #setNamedValue(Object)}.
 * </p>
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class FakeValueNamingScheme extends ValueNamingScheme<Object> {

	private static Object _value;

	@Override
	public Class<Object> getModelClass() {
		return Object.class;
	}

	@Override
	public Map<String, Object> getName(Object model) {
		return Collections.emptyMap();
	}

	@Override
	public boolean matches(Map<String, Object> name, Object model) {
		return Utils.equals(model, _value);
	}

	/** Set the value that should be identified by the {@link FakeValueNamingScheme} */
	public static void setNamedValue(Object namedValue) {
		_value = namedValue;
	}

	@Override
	public boolean supportsModel(Object model) {
		return Utils.equals(model, _value);
	}

}
