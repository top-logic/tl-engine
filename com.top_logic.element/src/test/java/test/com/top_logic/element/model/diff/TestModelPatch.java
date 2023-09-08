/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.model.diff;

import java.util.Collection;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.Log;
import com.top_logic.basic.config.equal.ConfigEquality;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.element.config.DefinitionReader;
import com.top_logic.element.config.ModelConfig;
import com.top_logic.element.model.DefaultModelFactory;
import com.top_logic.element.model.ModelResolver;
import com.top_logic.element.model.PersistentTLModel;
import com.top_logic.element.model.diff.apply.ApplyModelPatch;
import com.top_logic.element.model.diff.compare.CreateModelPatch;
import com.top_logic.element.model.diff.config.DiffElement;
import com.top_logic.element.model.export.ModelConfigExtractor;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.config.ModelPartConfig;
import com.top_logic.model.factory.TLFactory;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.impl.TransientObjectFactory;

/**
 * Test for {@link CreateModelPatch} and {@link ApplyModelPatch}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestModelPatch extends BasicTestCase {

	public void testPatch() {
		TLModel left;
		try (Transaction tx = kb().beginTransaction()) {
			left = loadModel("test1-left.model.xml");
			tx.commit();
		}

		TLModel right = loadModelTransient("test1-right.model.xml");

		try (Transaction tx = kb().beginTransaction()) {
			List<DiffElement> patch = createPatch(left, right);
			applyPatch(left, new DefaultModelFactory(), patch);
			tx.commit();
		}

		assertEmpty(createPatch(left, right));

		assertEqualsConfig(left, right);
	}

	private void assertEqualsConfig(TLModel left, TLModel right) {
		assertEqualConfig(config(left), config(right));
	}

	private void assertEqualConfig(ModelPartConfig left, ModelPartConfig right) {
		boolean equals = ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(left, right);
		if (!equals) {
			assertEquals(left.toString(), right.toString());
			fail("Expected configuration '" + left + "' but was '" + right + "'.");
		}
	}

	private ModelPartConfig config(TLModelPart model) {
		return model.visit(new ModelConfigExtractor(), null);
	}

	private void assertEmpty(Collection<?> values) {
		if (!values.isEmpty()) {
			fail("Expected collection to be empty but was: " + values);
		}
	}

	private void applyPatch(TLModel left, TLFactory factory, List<DiffElement> patch) {
		AssertProtocol log = new AssertProtocol() {
			@Override
			public void localInfo(String message, int verbosityLevel) {
				if (verbosityLevel < Log.INFO && message.contains("conflict")) {
					// Note: Delete-delete conflicts are normal, since deleting a type deletes all
					// parts using that type.
					fail(message);
				}
				super.localInfo(message, verbosityLevel);
			}
		};
		ApplyModelPatch apply = new ApplyModelPatch(log, left, factory);
		apply.applyPatch(patch);
		apply.complete();
	}

	private List<DiffElement> createPatch(TLModel left, TLModel right) {
		CreateModelPatch patchCreator = new CreateModelPatch();
		patchCreator.addPatch(left, right);
		List<DiffElement> patch = patchCreator.getPatch();
		return patch;
	}

	private TLModel loadModel(String name) {
		return loadModel(PersistentTLModel.newInstance(kb()), new DefaultModelFactory(), name);
	}

	private TLModel loadModelTransient(String name) {
		return loadModel(new TLModelImpl(), TransientObjectFactory.INSTANCE, name);
	}

	private TLModel loadModel(TLModel model, TLFactory factory, String name) {
		ModelConfig config =
			DefinitionReader.readElementConfig(new ClassRelativeBinaryContent(TestModelPatch.class, name));
		AssertProtocol log = new AssertProtocol();
		ModelResolver modelResolver = new ModelResolver(log, model, factory);
		modelResolver.createModel(config);
		modelResolver.complete();
		return model;
	}

	private KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	public static Test suite() {
		return KBSetup.getSingleKBTest(TestModelPatch.class);
	}

}
