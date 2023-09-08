/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.util.List;

import junit.framework.Test;

import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOReference.HistoryType;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.search.SetExpression;
import com.top_logic.knowledge.service.Transaction;

/**
 * Test for override of {@link MOAttribute} in {@link MOClass}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestOverrideAttribute extends AbstractDBKnowledgeBaseTest {

	public void testReverseSearch() throws DataObjectException {
		Transaction createTX = begin();
		KnowledgeObject referer = newDOverride("d1");
		KnowledgeObject reference = newDOverride("d1");
		String referenceAttr = getReferenceAttr(!MONOMORPHIC, HistoryType.CURRENT, BRANCH_GLOBAL);
		referer.setAttributeValue(referenceAttr, reference);
		commit(createTX);
		
		SetExpression search = filter(anyOf(D_NAME), eqBinary(reference(D_NAME, referenceAttr), literal(reference)));
		List<KnowledgeObject> result = kb().search(queryUnresolved(search));
		assertEquals(list(referer), result);
	}

	protected KnowledgeObject newDOverride(String a1) throws DataObjectException {
		KnowledgeObject result = kb().createKnowledgeObject(D_OVERRIDE_NAME);
		setA1(result, a1);
		return result;
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestOverrideAttribute}.
	 */
	public static Test suite() {
		return suiteDefaultDB(TestOverrideAttribute.class);
	}
}
