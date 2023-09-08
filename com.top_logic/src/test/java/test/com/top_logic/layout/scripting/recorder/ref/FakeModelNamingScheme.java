/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.scripting.recorder.ref;

import test.com.top_logic.layout.scripting.recorder.ref.FakeModelNamingScheme.FakeModelName;

import com.top_logic.layout.scripting.recorder.ref.AbstractModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.util.Utils;

/**
 * A {@link ModelNamingScheme} for tests.
 * <p>
 * It is registered for class {@link Object}, but {@link #isCompatibleModel(Object) supports} only
 * the model set via {@link #setNamedModel(Object)}. <br/>
 * </p>
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class FakeModelNamingScheme extends AbstractModelNamingScheme<Object, FakeModelName> {

	/** The {@link ModelName} for the {@link FakeModelNamingScheme}. */
	public interface FakeModelName extends ModelName {
		// Nothing needed in here
	}

	private static Object _model;

	@Override
	public Class<FakeModelName> getNameClass() {
		return FakeModelName.class;
	}

	@Override
	public Class<Object> getModelClass() {
		return Object.class;
	}

	@Override
	public Object locateModel(ActionContext context, FakeModelName name) {
		return _model;
	}

	@Override
	protected void initName(FakeModelName name, Object model) {
		// Nothing to do
	}

	@Override
	protected boolean isCompatibleModel(Object model) {
		return Utils.equals(model, _model);
	}

	/** Set the model that should be identified by the {@link FakeModelNamingScheme} */
	public static void setNamedModel(Object model) {
		_model = model;
	}

}
