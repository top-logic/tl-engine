/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import java.io.StringWriter;


/**
 * Testing {@link StringWriter} that does not accept <code>null</code> in
 * {@link #write(String)} and {@link #append(CharSequence)}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StringWriterNonNull extends StringWriter {
	
	@Override
	public void write(String str) {
		BasicTestCase.assertNotNull(str);
		super.write(str);
	}

	@Override
	public StringWriter append(CharSequence csq) {
		BasicTestCase.assertNotNull(csq);
		return super.append(csq);
	}
}