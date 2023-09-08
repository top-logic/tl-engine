/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout;

import junit.framework.Test;
import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.base.services.simpleajax.JSSnipplet;
import com.top_logic.layout.DefaultUpdateQueue;

/**
 * The class {@link TestDefaultUpdateQueue} tests the {@link DefaultUpdateQueue}.
 * 
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestDefaultUpdateQueue extends BasicTestCase {
	
	public void testAdd() {
		DefaultUpdateQueue updateQueue = new DefaultUpdateQueue();
		JSSnipplet action = JSSnipplet.createAlert("TESTE");
		updateQueue.add(action);
		assertTrue(updateQueue.getStorage().contains(action));
		
		int currentSize = updateQueue.getStorage().size();
		updateQueue.add(null);
		assertEquals(currentSize, updateQueue.getStorage().size());
	}
	
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestDefaultUpdateQueue.class);
	}


}

