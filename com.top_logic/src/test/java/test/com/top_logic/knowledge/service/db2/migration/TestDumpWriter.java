/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2.migration;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.dob.meta.DeferredMetaObject;
import com.top_logic.knowledge.service.db2.migration.DumpSchemaConstants;
import com.top_logic.knowledge.service.db2.migration.DumpWriter;

/**
 * Test for the {@link DumpWriter}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestDumpWriter extends BasicTestCase {

	public void testNotFailOnErrors() throws IOException {
		StringWriter out = new StringWriter();
		try (DumpWriter dumpWriter = new DumpWriter(new TagWriter(out))) {
			dumpWriter.startDocument();

			dumpWriter.beginChangeSets();
			// Null triggers error
			dumpWriter.writeChangeSet(null);
			dumpWriter.endChangeSets();

			dumpWriter.beginTypes();
			dumpWriter.writeUnversionedType(new DeferredMetaObject("mo1"), Collections.emptyIterator());
			// Null triggers error
			dumpWriter.writeUnversionedType(new DeferredMetaObject("mo2"), null);
			dumpWriter.endTypes();

			dumpWriter.beginTables();
			dumpWriter.writeTable("TABLE1", Collections.emptyList());
			// Null triggers error
			dumpWriter.writeTable("TABLE2", Collections.singletonList(null));
			dumpWriter.endTables();

			dumpWriter.endDocument();
			dumpWriter.close();

			assertEquals(3, dumpWriter.errors().size());
		}
		// content contains an error for each failure and a summary error at the end of the content.
		assertEquals(4, StringServices.count(out.toString(), "<" + DumpSchemaConstants.ERROR_TAG + ">"));
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestDumpWriter}.
	 */
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestDumpWriter.class);
	}

}

