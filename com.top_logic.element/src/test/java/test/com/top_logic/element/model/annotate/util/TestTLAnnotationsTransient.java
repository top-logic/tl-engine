/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.model.annotate.util;

import junit.framework.Test;

import com.top_logic.model.TLModel;
import com.top_logic.model.factory.TLFactory;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.impl.TransientObjectFactory;

/**
 * {@link TestTLAnnotations} for the transient model.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestTLAnnotationsTransient extends TestTLAnnotations {

	@Override
	protected TLModel setUpModel() {
		return new TLModelImpl();
	}

	@Override
	protected TLFactory setUpFactory() {
		return TransientObjectFactory.INSTANCE;
	}

	@Override
	protected void tearDownModel() {
		// Nothing to do.
	}

	public static Test suite() {
		return suiteTransient(TestTLAnnotationsTransient.class);
	}

}

