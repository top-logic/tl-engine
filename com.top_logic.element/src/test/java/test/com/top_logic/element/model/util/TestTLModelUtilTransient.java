/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.model.util;

import junit.framework.Test;

import com.top_logic.model.TLModel;
import com.top_logic.model.factory.TLFactory;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link Test}s for {@link TLModelUtil} with the {@link TLModelImpl transient TLModel}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public class TestTLModelUtilTransient extends AbstractTestTLModelUtil {

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
		return suiteTransient(TestTLModelUtilTransient.class);
	}

}
