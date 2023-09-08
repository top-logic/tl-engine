/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.scripting.template.gui;

import java.io.File;
import java.io.IOException;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.layout.scripting.action.ActionFactory;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.runtime.ActionReader;
import com.top_logic.layout.scripting.template.gui.ScriptContainer;
import com.top_logic.layout.scripting.template.gui.ScriptContainer.PersistableScriptContainer;

/**
 * Test for {@link ScriptContainer}.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestScriptContainer extends BasicTestCase {

	public void testCorrectPersistEncoding() throws IOException {
		String comment = "öäüßµ€";
		ApplicationAction action = ActionFactory.actionChain();
		action.setComment(comment);
		File file = File.createTempFile(TestScriptContainer.class.getSimpleName(), ActionReader.FILE_ENDING);
		PersistableScriptContainer scriptContainer = ScriptContainer.createPersistable(action, file);
		scriptContainer.persist();
		ApplicationAction readAction;
		try {
			readAction = ActionReader.INSTANCE.readAction(file);
		} catch (RuntimeException ex) {
			throw fail("Ticket #18782: ", ex);
		}
		assertEquals("Ticket #18782: Wrong write or read encoding?", comment, readAction.getComment());
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestScriptContainer}.
	 */
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestScriptContainer.class);
	}

}

