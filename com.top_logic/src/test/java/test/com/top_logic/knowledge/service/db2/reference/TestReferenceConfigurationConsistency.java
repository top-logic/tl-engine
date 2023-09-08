/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2.reference;

import junit.framework.Test;
import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.DeletionPolicy;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.knowledge.KIReference;

/**
 * The class {@link TestReferenceConfigurationConsistency} tests configuration of
 * {@link KIReference}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestReferenceConfigurationConsistency extends BasicTestCase {

	private MOReference _reference;

	public TestReferenceConfigurationConsistency(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		MetaObject referenceType = MOPrimitive.STRING;
		_reference = KIReference.referenceById("reference", referenceType);
	}

	@Override
	protected void tearDown() throws Exception {
		_reference = null;
		super.tearDown();
	}

	public void testContainmentHistoric() {
		_reference.setHistoryType(HistoryType.HISTORIC);
		try {
			_reference.setContainer(true);
			fail("Containment is useless if reference is filled with historic object");
		} catch (ConfigurationError ex) {
			// expected
		}
		_reference.setHistoryType(HistoryType.CURRENT);
		_reference.setContainer(true);
		try {
			_reference.setHistoryType(HistoryType.HISTORIC);
			fail("Containment is useless if reference is filled with historic object");
		} catch (ConfigurationError ex) {
			// expected
		}
	}

	public void testContainmentMixed() {
		_reference.setHistoryType(HistoryType.MIXED);
		try {
			_reference.setContainer(true);
			fail("Containment is useless if reference is currently filled with historic object");
		} catch (ConfigurationError ex) {
			// expected
		}
		_reference.setHistoryType(HistoryType.CURRENT);
		_reference.setContainer(true);
		try {
			_reference.setHistoryType(HistoryType.MIXED);
			fail("Containment is useless if reference is filled with historic object");
		} catch (ConfigurationError ex) {
			// expected
		}
	}

	public void testCurrentNotStabilise() {
		_reference.setHistoryType(HistoryType.CURRENT);
		try {
			_reference.setDeletionPolicy(DeletionPolicy.STABILISE_REFERENCE);
			fail("Current references can not be stable");
		} catch (ConfigurationError ex) {
			// expected
		}
	}

	public void testStabiliseNotCurrent() {
		// first made possible to set stabilise reference
		_reference.setHistoryType(HistoryType.MIXED);
		_reference.setDeletionPolicy(DeletionPolicy.STABILISE_REFERENCE);
		try {
			_reference.setHistoryType(HistoryType.CURRENT);
			fail("Current references can not be stable");
		} catch (ConfigurationError ex) {
			// expected
		}
	}

	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestReferenceConfigurationConsistency.class);
	}
}

