/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import com.top_logic.basic.Base64AESEncoder;
import com.top_logic.basic.Base64Encoder;

/**
 * Test class for {@link Base64AESEncoder}.
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestBase64AESEncoder extends TestBase64Encoder {

	@Override
	protected Base64Encoder newBase64Encoder(String key) {
		return Base64Encoder.newBase64AESEncoder(key);
	}

}

