/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.tool.boundsec.commandhandlers;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.dummy.DummyWrapper;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.StringID;
import com.top_logic.basic.TLID;
import com.top_logic.dob.meta.MOClassImpl;
import com.top_logic.mig.util.net.URLUtilities;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.commandhandlers.DefaultBookmarkHandler;
import com.top_logic.tool.boundsec.commandhandlers.TechnicalBookmark;

/**
 * Test case for {@link DefaultBookmarkHandler}
 * 
 * @author     <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class TestBookmarkHandler extends BasicTestCase {

	public static Test suite() {
		return TLTestSetup.createTLTestSetup(ServiceTestSetup.createSetup(TestBookmarkHandler.class,
			CommandHandlerFactory.Module.INSTANCE));
	}

	public void testAppendIdentificationArguments() throws Exception {
		TechnicalBookmark handler = new TechnicalBookmark();
		
		StringBuilder urlString = new StringBuilder("www.example.com/application");
		TLID id = StringID.createRandomID();
		handler.appendIdentificationArguments(urlString, true, DummyWrapper.obj(id, new MOClassImpl("myClass")));
		
		assertEquals("www.example.com/application?id=" + URLUtilities.urlEncode(IdentifierUtil.toExternalForm(id))
			+ "&branch=1&type=myClass", urlString.toString());
	}

}
