/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import junit.framework.Test;

import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.knowledge.KIReference;

/**
 * The class {@link TestReferenceByValue} tests {@link KIReference} that hold the complete
 * referenced value instead of the only the identifier.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestReferenceByValue extends AbstractKIReferenceTest {

	@Override
	protected String refererTypeName() {
		return G_NAME;
	}

	@Override
	protected MOReference newReferenceAttribute(String name, MetaObject targetType) {
		return KIReference.referenceByValue(name, targetType);
	}


	public static Test suite() {
		if (false) {
			return runOneTest(TestReferenceByValue.class, "testFetchHistoricObjectWithCurrentReference");
		}
		return suite(TestReferenceByValue.class);
	}

}

