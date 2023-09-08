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
 * The class {@link TestReferenceById} tests {@link KIReference} that holds the identifier of an
 * object instead of the object itself.
 * 
 *          test.com.top_logic.knowledge.service.db2.AbstractKIReferenceTest
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestReferenceById extends AbstractKIReferenceTest {

	@Override
	protected String refererTypeName() {
		return E_NAME;
	}

	@Override
	protected MOReference newReferenceAttribute(String name, MetaObject targetType) {
		return KIReference.referenceById(name, targetType);
	}

	public static Test suite() {
		if (false) {
			return runOneTest(TestReferenceById.class, "testFetchHistoricObjectWithCurrentReference");
		}
		return suite(TestReferenceById.class);
	}

}

