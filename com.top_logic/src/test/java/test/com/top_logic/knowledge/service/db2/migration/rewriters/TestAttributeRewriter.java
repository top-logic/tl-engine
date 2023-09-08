/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2.migration.rewriters;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.LongID;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.StringContent;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.dob.meta.MOClassImpl;
import com.top_logic.knowledge.event.CachingEventWriter;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.objects.identifier.ObjectBranchId;
import com.top_logic.knowledge.service.db2.migration.rewriters.AttributeRewriter;
import com.top_logic.knowledge.service.db2.migration.rewriters.Rewriter;

/**
 * Test case for {@link AttributeRewriter}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestAttributeRewriter extends TestCase {

	public void testRewriteToNull() throws ConfigurationException {
		PolymorphicConfiguration<?> config = TypedConfiguration.parse("rewriter", PolymorphicConfiguration.class,
			new StringContent(
			"<rewriter class=\"com.top_logic.knowledge.service.db2.migration.rewriters.AttributeRewriter\"\r\n" +
					"	types=\"MyType\"\r\n" +
				">\r\n" +
				"	<algorithm class=\"com.top_logic.knowledge.service.db2.migration.rewriters.ChangeValue\"\r\n" +
					"		attribute=\"foo\"\r\n" +
				"		new-value=\"NULL:\"\r\n" +
				"	>\r\n" +
				"	</algorithm>\r\n" +
				"</rewriter>\r\n"));

		MOClassImpl myType = new MOClassImpl("MyType");

		Rewriter rewriter = (Rewriter) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);

		ObjectCreation evt = new ObjectCreation(42, new ObjectBranchId(1, myType, LongID.valueOf(1)));
		evt.setValue("foo", null, "bar");
		evt.setValue("bar", null, "bar");

		CachingEventWriter result = new CachingEventWriter();
		rewriter.rewrite(new ChangeSet(42).add(evt), result);

		ChangeSet firstChange = result.getAllEvents().get(0);
		ObjectCreation firstCreatiion = firstChange.getCreations().get(0);
		assertEquals("bar", firstCreatiion.getValues().get("bar"));
		assertNull(firstCreatiion.getValues().get("foo"));
	}
	
	public void testRewriteXML() throws ConfigurationException {
		PolymorphicConfiguration<?> config = TypedConfiguration.parse("rewriter", PolymorphicConfiguration.class,
			new StringContent(
				"<rewriter class=\"com.top_logic.knowledge.service.db2.migration.rewriters.AttributeRewriter\"\r\n" + 
				"	types=\"MyType\"\r\n" + 
				">\r\n" + 
				"	<algorithm class=\"com.top_logic.knowledge.service.db2.migration.rewriters.XsltValueTransform\"\r\n" + 
					"		attribute=\"attr\"\r\n" +
					"		transform=\"/WEB-INF/test/TestAttributeRewriter-rewriteXml.xsl\"\r\n"
					+
				"	>\r\n" + 
				"	</algorithm>\r\n" + 
				"</rewriter>\r\n" + 
				""));
		
		MOClassImpl myType = new MOClassImpl("MyType");
		
		Rewriter rewriter = (Rewriter) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
		
		ObjectCreation evt = new ObjectCreation(42, new ObjectBranchId(1, myType, LongID.valueOf(1)));
		evt.setValue("attr", null, "<foo>test</foo>");
		
		CachingEventWriter result = new CachingEventWriter();
		rewriter.rewrite(new ChangeSet(42).add(evt), result);
		
		ChangeSet firstChange = result.getAllEvents().get(0);
		ObjectCreation firstCreatiion = firstChange.getCreations().get(0);
		assertEquals(
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?><bar>test</bar>",
			firstCreatiion.getValues().get("attr"));
	}

	public static Test suite() {
		return ModuleLicenceTestSetup
			.setupModule(ServiceTestSetup.createSetup(TestAttributeRewriter.class, TypeIndex.Module.INSTANCE));
	}
}
