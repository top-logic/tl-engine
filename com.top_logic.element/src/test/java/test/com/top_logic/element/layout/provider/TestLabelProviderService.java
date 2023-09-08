/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.layout.provider;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.element.structured.model.ANode;
import test.com.top_logic.element.structured.model.TestTypesFactory;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.mig.html.DefaultResourceProvider;
import com.top_logic.util.model.ModelService;

/**
 * Test case for {@link LabelProviderService}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestLabelProviderService extends BasicTestCase {

	public void testHistoricObjectLabels() {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		if (!kb.getHistoryManager().hasHistory()) {
			return;
		}

		LabelProviderService service = LabelProviderService.getInstance();
		Revision r1;
		ANode obj;
		try (Transaction tx = kb.beginTransaction()) {
			ANode root = TestTypesFactory.getInstance().getRootSingleton();
			obj = (ANode) root.createChild("TestLabelProviderService", ANode.A_NODE_TYPE);

			LabelProvider providerTransient = service.getLabelProvider(obj);
			assertInstanceof(providerTransient, LabelsForA.class);

			tx.commit();

			LabelProvider providerPersistent = service.getLabelProvider(obj);
			assertInstanceof(providerPersistent, LabelsForA.class);

			r1 = tx.getCommitRevision();
			ANode historic = WrapperHistoryUtils.getWrapper(r1, obj);

			LabelProvider providerHistoric = service.getLabelProvider(historic);
			assertInstanceof(providerHistoric, LabelsForA.class);
		}

		HistoryManager hm = kb.getHistoryManager();
		Branch b1 = hm.createBranch(hm.getContextBranch(), r1, null);

		ANode objB1 = HistoryUtils.getKnowledgeItem(b1, obj.tHandle()).getWrapper();

		LabelProvider providerBranch = service.getLabelProvider(objB1);
		assertInstanceof(providerBranch, LabelsForA.class);
	}

	public static class LabelsForA extends DefaultResourceProvider {

		@Override
		public String getLabel(Object object) {
			return ((ANode) object).getName();
		}

	}

	public static Test suite() {
		return KBSetup.getKBTest(
			ServiceTestSetup.createSetup(TestLabelProviderService.class,
				ModelService.Module.INSTANCE, LabelProviderService.Module.INSTANCE),
			KBSetup.DEFAULT_KB);
	}

}
