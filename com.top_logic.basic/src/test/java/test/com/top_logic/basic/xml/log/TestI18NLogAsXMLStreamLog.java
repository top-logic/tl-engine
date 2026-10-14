/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.xml.log;

import java.util.Locale;

import javax.xml.stream.Location;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.basic.xml.log.I18NLogAsXMLStreamLog;

/**
 * Test case for {@link I18NLogAsXMLStreamLog#withLocation(Location, ResKey)} resolved through the
 * application resource bundles.
 * 
 * <p>
 * Line and column of a location are shown as plain numbers, without the grouping separator of the
 * locale.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestI18NLogAsXMLStreamLog extends TestCase {

	private static final int LINE = 4433;

	private static final int COLUMN = 183;

	/**
	 * Location with a system ID in English.
	 */
	public void testFileLocationEnglish() {
		assertPlainNumbers(Locale.ENGLISH, "file:/data/import.xml");
	}

	/**
	 * Location with a system ID in German.
	 */
	public void testFileLocationGerman() {
		assertPlainNumbers(Locale.GERMAN, "file:/data/import.xml");
	}

	/**
	 * Location without a system ID in English.
	 */
	public void testLocationEnglish() {
		assertPlainNumbers(Locale.ENGLISH, null);
	}

	/**
	 * Location without a system ID in German.
	 */
	public void testLocationGerman() {
		assertPlainNumbers(Locale.GERMAN, null);
	}

	private void assertPlainNumbers(Locale locale, String systemId) {
		ResKey key = I18NLogAsXMLStreamLog.withLocation(location(systemId), ResKey.text("Problem."));
		String text = ResourcesModule.getInstance().getBundle(locale).getString(key);

		assertTrue("Line missing: " + text, text.contains(Integer.toString(LINE)));
		assertTrue("Column missing: " + text, text.contains(Integer.toString(COLUMN)));
		assertFalse("Grouped line: " + text, text.contains("4,433"));
		assertFalse("Grouped line: " + text, text.contains("4.433"));
		if (systemId != null) {
			assertTrue("System ID missing: " + text, text.contains(systemId));
		}
		assertTrue("Detail missing: " + text, text.contains("Problem."));
	}

	private static Location location(String systemId) {
		return new Location() {
			@Override
			public int getLineNumber() {
				return LINE;
			}

			@Override
			public int getColumnNumber() {
				return COLUMN;
			}

			@Override
			public int getCharacterOffset() {
				return -1;
			}

			@Override
			public String getPublicId() {
				return null;
			}

			@Override
			public String getSystemId() {
				return systemId;
			}
		};
	}

	/**
	 * Suite of {@link TestI18NLogAsXMLStreamLog} with the resources module started.
	 */
	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(
			ServiceTestSetup.createSetup(new TestSuite(TestI18NLogAsXMLStreamLog.class),
				ResourcesModule.Module.INSTANCE));
	}

}
