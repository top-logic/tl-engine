/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.util.List;

import junit.framework.Test;

import test.com.top_logic.knowledge.wrap.SimpleWrapperFactoryTestScenario.EObj;

import com.top_logic.knowledge.service.AssociationQuery;
import com.top_logic.knowledge.service.db2.OrderedLinkQuery;
import com.top_logic.knowledge.util.OrderedLinkUtil;

/**
 * Test case for {@link OrderedLinkQuery} with stable result.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestAssociationCacheOrderedStable extends TestAssociationCacheOrdered {

	private static final OrderedLinkQuery<EObj> ORDERED = AssociationQuery.createOrderedLinkQuery("ordered",
		EObj.class, E_NAME, REFERENCE_POLY_CUR_LOCAL_NAME, E1_NAME);

	@Override
	protected OrderedLinkQuery<EObj> query() {
		return ORDERED;
	}

	@Override
	protected void updateIndices(List<EObj> list, String attributeName) {
		OrderedLinkUtil.updateIndices(list, attributeName);
	}

	public static Test suite() {
		return suite(TestAssociationCacheOrderedStable.class);
	}

}
