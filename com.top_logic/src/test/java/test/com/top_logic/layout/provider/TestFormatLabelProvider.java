/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.provider;

import java.text.Format;
import java.util.Date;

import junit.framework.TestCase;

import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.layout.provider.FormatLabelProvider;

/**
 * {@link TestCase} for {@link FormatLabelProvider}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestFormatLabelProvider extends TestCase {

	public void testLabel() {
		Date d = new Date(987654321012L);
		Format f = CalendarUtil.getDateTimeInstance();
		FormatLabelProvider labelProvider = new FormatLabelProvider(f);
		assertEquals(f.format(d), labelProvider.getLabel(d));
		assertEquals(null, labelProvider.getLabel(null));
	}

}

