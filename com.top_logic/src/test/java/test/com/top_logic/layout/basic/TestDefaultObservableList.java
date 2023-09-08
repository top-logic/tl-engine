/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.basic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.Test;
import test.com.top_logic.TLTestSetup;

import com.top_logic.layout.basic.DefaultObservableList;
import com.top_logic.layout.basic.ObservableList;

/**
 * The class {@link TestDefaultObservableList} tests the {@link DefaultObservableList}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestDefaultObservableList extends TestObservableList {

	@Override
	protected ObservableList createObservableList() {
		return new DefaultObservableList();
	}
	
	public void testGetModel() {
		DefaultObservableList observableList = (DefaultObservableList) createObservableList(Collections.singleton(modelChangeListener));
		List shadowStorage = new ArrayList();
		Object o1 = new Object();
		Object o2 = new Object();
		Object o3 = new Object();
		observableList.add(o1);
		shadowStorage.add(o1);
		observableList.add(o2);
		shadowStorage.add(o2);
		observableList.add(o3);
		shadowStorage.add(o3);
		
		assertEquals(shadowStorage, observableList.getModel());
	}

	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestDefaultObservableList.class);
	}

}
