/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.model.diff;

import java.util.Collection;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.ErrorIgnoringProtocol;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.equal.ConfigEquality;
import com.top_logic.element.config.ModelConfig;
import com.top_logic.element.config.annotation.ConfigType;
import com.top_logic.element.model.ModelCopy;
import com.top_logic.element.model.ModelResolver;
import com.top_logic.element.model.diff.apply.ApplyModelPatch;
import com.top_logic.element.model.diff.compare.CreateModelPatch;
import com.top_logic.element.model.diff.config.AddAnnotations;
import com.top_logic.element.model.diff.config.DiffElement;
import com.top_logic.element.model.export.ModelConfigExtractor;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLType;
import com.top_logic.model.access.IdentityMapping;
import com.top_logic.model.config.ModelPartConfig;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.util.TLModelUtil;

/**
 * Test for {@link CreateModelPatch} and {@link ApplyModelPatch}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestModelPatch extends AbstractModelPatchTest {

	public void testPatch() {
		TLModel left;
		try (Transaction tx = kb().beginTransaction()) {
			left = loadModel("test1-left.model.xml");
			tx.commit();
		}

		TLModel right = loadModelTransient("test1-right.model.xml");

		try (Transaction tx = kb().beginTransaction()) {
			applyDiff(left, right);
			tx.commit();
		}

		assertEmpty(createPatch(left, right));

		assertEqualsConfig(right, left);
	}

	public void testPatchTransient() {
		doTestTransient("test1-left.model.xml", "test1-right.model.xml");
	}

	public void testPatchOverrideTransient() {
		doTestTransient("test-override-left.model.xml", "test-override-right.model.xml");
	}

	public void testPatchRefDeleteTransient() {
		doTestTransient("test-refdelete-left.model.xml", "test-refdelete-right.model.xml");
	}

	private void doTestTransient(String leftFixture, String rightFixture) {
		TLModel left = loadModelTransient(leftFixture);
		TLModel right = loadModelTransient(rightFixture);

		applyDiff(left, right);

		assertEmpty(createPatch(left, right));
		assertEqualsConfig(right, left);
	}

	public void testCopy() {
		assertCopyWithoutDiff("test1-left.model.xml");
		assertCopyWithoutDiff("test1-right.model.xml");
		assertCopyWithoutDiff("test-override-left.model.xml");
	}

	private void assertCopyWithoutDiff(String fixture) {
		TLModel model = loadModelTransient(fixture);
		TLModel copy = ModelCopy.copy(model);
		TLModel copy2 = copyDumpLoad(model);

		assertEmpty(createPatch(model, copy));
		assertEqualsConfig(model, copy);

		assertEmpty(createPatch(model, copy2));
		assertEqualsConfig(model, copy2);
	}

	private TLModel copyDumpLoad(TLModel model) {
		TLModelImpl result = new TLModelImpl();

		ModelConfig config = (ModelConfig) model.visit(new ModelConfigExtractor(), null);
		ModelResolver modelResolver = new ModelResolver(new ErrorIgnoringProtocol(), result, null);
		modelResolver.createModel(config);
		modelResolver.complete();

		return result;
	}

	private void assertEqualsConfig(TLModel expected, TLModel actual) {
		assertEqualConfig(config(expected), config(actual));
	}

	private void assertEqualConfig(ModelPartConfig expected, ModelPartConfig actual) {
		boolean equals = ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(expected, actual);
		if (!equals) {
			assertEquals(expected.toString(), actual.toString());
			fail("Expected configuration '" + expected + "' but was '" + actual + "'.");
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

	public void testAnnotationUpdate() {
		TLModelImpl leftModel = annotationUpdateModel();
		
		TLModelImpl rightModel = annotationUpdateModel();
		TLType type = rightModel.getModule("m1").getType("c1");
		ConfigType configType = TypedConfiguration.newConfigItem(ConfigType.class);
		configType.setValue("C1_TYPE");
		type.setAnnotation(configType);
		
		AddAnnotations addAnnotations = TypedConfiguration.newConfigItem(AddAnnotations.class);
		addAnnotations.setPart("m1:c1");
		addAnnotations.getAnnotations().add(TypedConfiguration.copy(configType));

		List<DiffElement> patch = createPatch(leftModel, rightModel);
		assertConfigEquals(list(addAnnotations), patch);
	}

	private TLModelImpl annotationUpdateModel() {
		TLModelImpl model = new TLModelImpl();
		TLModule m1 = TLModelUtil.addModule(model, "m1");
		TLModelUtil.addDatatype(m1, "c1", IdentityMapping.INSTANCE);
		return model;
	}
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestModelPatch.class);
	}

}
