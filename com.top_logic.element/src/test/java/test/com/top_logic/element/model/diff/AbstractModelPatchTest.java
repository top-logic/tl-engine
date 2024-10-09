/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.model.diff;

import java.util.List;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.Log;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.element.config.DefinitionReader;
import com.top_logic.element.config.ModelConfig;
import com.top_logic.element.model.DefaultModelFactory;
import com.top_logic.element.model.ModelResolver;
import com.top_logic.element.model.PersistentTLModel;
import com.top_logic.element.model.diff.apply.ApplyModelPatch;
import com.top_logic.element.model.diff.compare.CreateModelPatch;
import com.top_logic.element.model.diff.config.DiffElement;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.model.TLModel;
import com.top_logic.model.factory.TLFactory;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.impl.TransientObjectFactory;

/**
 * Abstract superclass for tests that test incremental model updates.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public abstract class AbstractModelPatchTest extends BasicTestCase {

	protected KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	protected TLModel loadModel(String name) {
		return loadModel(PersistentTLModel.newInstance(kb()), new DefaultModelFactory(), name);
	}

	protected TLModel loadModelTransient(String name) {
		return loadModel(new TLModelImpl(), TransientObjectFactory.INSTANCE, name);
	}

	protected TLModel loadModel(TLModel model, TLFactory factory, String name) {
		ModelConfig config =
			DefinitionReader.readElementConfig(new ClassRelativeBinaryContent(getClass(), name));
		AssertProtocol log = new AssertProtocol();
		ModelResolver modelResolver = new ModelResolver(log, model, factory);
		modelResolver.createModel(config);
		modelResolver.complete();
		return model;
	}

	protected List<DiffElement> createPatch(TLModel left, TLModel right) {
		CreateModelPatch patchCreator = new CreateModelPatch();
		patchCreator.addPatch(left, right);
		List<DiffElement> patch = patchCreator.getPatch();
		return patch;
	}

	protected void applyPatch(TLModel left, TLFactory factory, List<DiffElement> patch) {
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
		ApplyModelPatch.applyPatch(log, left, factory, patch);
	}

	protected void applyDiff(TLModel left, TLModel right) {
		List<DiffElement> patch = createPatch(left, right);
		applyPatch(left, new DefaultModelFactory(), patch);
	}

}
