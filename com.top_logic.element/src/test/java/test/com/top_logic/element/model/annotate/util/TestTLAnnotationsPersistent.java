/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.model.annotate.util;

import junit.framework.Test;

import test.com.top_logic.basic.TestUtils;
import test.com.top_logic.element.model.util.TLModelTestUtil;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.element.model.DefaultModelFactory;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.model.TLModel;
import com.top_logic.model.factory.TLFactory;

/**
 * {@link TestTLAnnotations} for the persistent model.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestTLAnnotationsPersistent extends TestTLAnnotations {

	@Override
	protected TLModel setUpModel() {
		return TLModelTestUtil.createTLModelInTransaction(getKnowledgeBase());
	}

	@Override
	protected TLFactory setUpFactory() {
		return new DefaultModelFactory();
	}

	@Override
	protected void tearDownModel() {
		/* nothing to do. Reverting the changes in the KnowledgeBase does not work, as reverting
		 * type-creations is not possible. Additionally, reverting slows down the test from 2
		 * seconds to 30 seconds and causes NPEs during the teardown, caused by MetaElements without
		 * scope. */
	}

	private KnowledgeBase getKnowledgeBase() {
		return PersistencyLayer.getKnowledgeBase();
	}

	public static Test suite() {
		return TestUtils.doNotMerge(KBSetup.getSingleKBTest(TestTLAnnotationsPersistent.class));
	}

}

