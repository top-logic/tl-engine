/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.check;

import static com.top_logic.layout.scripting.runtime.action.ApplicationAssertions.*;

import java.io.IOException;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.layout.scripting.recorder.ref.DataItemValue;
import com.top_logic.layout.scripting.runtime.ActionContext;

/**
 * Base class for {@link ValueCheck}s on {@link BinaryData}s
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractFileEqualityCheck extends ValueCheck<AbstractFileEqualityCheck.FileMatchConfig> {

	/**
	 * Configuration of {@link AbstractFileEqualityCheck}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public interface FileMatchConfig extends ValueCheck.ValueCheckConfig {

		/**
		 * Description of the expected value.
		 */
		DataItemValue getExpectedValue();

		/** @see #getExpectedValue() */
		void setExpectedValue(DataItemValue value);

	}

	/**
	 * Create a {@link AbstractFileEqualityCheck} from configuration.
	 */
	public AbstractFileEqualityCheck(InstantiationContext context, FileMatchConfig config) {
		super(context, config);
	}

	@Override
	public void check(ActionContext context, Object value) {
		try {
			BinaryData actual = BinaryData.cast(value);
			DataItemValue expectedValue = _config.getExpectedValue();
			assertEquals(_config, "Unexpected file name.", expectedValue.getName(), actual.getName());
			assertEquals(_config, "Unexpected file content type.", expectedValue.getContentType(), actual.getContentType());

			BinaryData expected = BinaryData.cast(context.resolve(expectedValue));
			checkContents(actual, expected);
		} catch (IOException ex) {
			fail(_config, "Error reading file", ex);
		}
	}

	/**
	 * Performs the actual check.
	 * 
	 * @param actual
	 *        The actual value.
	 * @param expected
	 *        The expected value.
	 * @throws IOException
	 *         If accessing the {@link BinaryData}s fails.
	 */
	protected abstract void checkContents(BinaryData actual, BinaryData expected) throws IOException;

}
