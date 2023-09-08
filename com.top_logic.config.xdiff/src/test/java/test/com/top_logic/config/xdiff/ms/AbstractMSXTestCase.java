/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.config.xdiff.ms;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.w3c.dom.Document;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.config.xdiff.ms.fixtures.Fixtures;

import com.top_logic.basic.Logger;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.config.xdiff.ms.MSXDiffSchema;

/**
 * Base class for test cases around {@link MSXDiffSchema}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractMSXTestCase extends TestCase {

	protected Document getExpected(String baseName) {
		return Fixtures.getDocument(getExpectedName(baseName));
	}

	protected Document getDiff(String baseName, String diffSuffix) {
		return Fixtures.getDocument(getDiffName(baseName, diffSuffix));
	}

	protected Document getBefore(String baseName) {
		return Fixtures.getDocument(getBeforeName(baseName));
	}

	protected com.top_logic.config.xdiff.model.Document getExpectedX(String baseName) {
		return Fixtures.getDocumentX(getExpectedName(baseName));
	}

	protected com.top_logic.config.xdiff.model.Document getDiffX(String baseName, String diffSuffix) {
		return Fixtures.getDocumentX(getDiffName(baseName, diffSuffix));
	}

	protected File getFile(String baseName, String suffix) {
		return Fixtures.getFile(getDiffName(baseName, suffix));
	}

	protected com.top_logic.config.xdiff.model.Document getBeforeX(String baseName) {
		return Fixtures.getDocumentX(getBeforeName(baseName));
	}

	private String getBeforeName(String baseName) {
		return baseName + "-before.xml";
	}

	private String getExpectedName(String baseName) {
		return baseName + "-expected.xml";
	}

	private String getDiffName(String baseName, String diffSuffix) {
		return baseName + "-" + diffSuffix + ".xml";
	}

	protected static void assertEqualsDocument(String message, Document expected, Document given) {
		boolean ok = false;
		try {
			BasicTestCase.assertEqualsDocument(message, expected, given);
			ok = true;
		} finally {
			if (!ok) {
				dumpDocument(given);
			}
		}
	}

	protected static void assertNotEqualsDocument(Document expected, Document given) {
		boolean ok = false;
		try {
			BasicTestCase.assertNotEqualsDocument(expected, given);
			ok = true;
		} finally {
			if (!ok) {
				dumpDocument(given);
			}
		}
	}

	private static void dumpDocument(Document given) {
		try {
			System.out.println();
			System.out.println("Document:");
			DOMUtil.serializeXMLDocument(System.out, false, given);
		} catch (IOException ex) {
			// Ignore inner exception.
			Logger.error("Cannot dump result.", ex, TestMSXApply.class);
		}
	}

}
