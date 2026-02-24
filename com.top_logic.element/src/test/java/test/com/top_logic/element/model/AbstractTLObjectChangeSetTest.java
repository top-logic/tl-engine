/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package test.com.top_logic.element.model;

import java.util.function.Consumer;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.CustomPropertiesDecorator;
import test.com.top_logic.basic.CustomPropertiesSetup;
import test.com.top_logic.basic.TestUtils;
import test.com.top_logic.element.knowledge.TestingElementContextFactory;
import test.com.top_logic.element.meta.TestWithModelExtension;

import com.top_logic.basic.io.FileUtilities;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.model.cs.TLObjectChangeSet;

/**
 * Abstract superclass to test {@link TLObjectChangeSet}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractTLObjectChangeSetTest extends TestWithModelExtension {

	/**
	 * Executes the given installer during the next commit with the created
	 * {@link TLObjectChangeSet}.
	 */
	protected void testNextCommit(Consumer<? super TLObjectChangeSet> installer) {
		DisplayContext dc = DefaultDisplayContext.getDisplayContext();
		dc.set(TestingElementContextFactory.INSTALLER, change -> {
			installer.accept(change);
			dc.reset(TestingElementContextFactory.INSTALLER);
		});
	}

	private static Test createCustomConfigSetup(Class<?> testClass, Test innerSetup) {
		String configFileName = testClass.getSimpleName() + FileUtilities.XML_FILE_ENDING;
		String createFilePath = CustomPropertiesDecorator.createFileName(testClass, configFileName);
		return TestUtils.doNotMerge(new CustomPropertiesSetup(innerSetup, createFilePath, true));
	}

	/**
	 * Creates a {@link Test} for the given {@link AbstractTLObjectChangeSetTest}.
	 */
	protected static Test suiteForChangeSet(Class<? extends AbstractTLObjectChangeSetTest> testClass) {
		Test test = new TestSuite(testClass);
		test = createCustomConfigSetup(AbstractTLObjectChangeSetTest.class, test);
		return TestWithModelExtension.suite(test);
	}

}

