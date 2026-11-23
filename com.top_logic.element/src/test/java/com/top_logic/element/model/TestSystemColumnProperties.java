/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model;

import java.util.List;

import junit.framework.Test;

import test.com.top_logic.element.meta.TestWithModelExtension;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.constraint.check.ConstraintChecker;
import com.top_logic.dob.meta.MOStructure;
import com.top_logic.element.config.AttributeConfig;
import com.top_logic.element.config.annotation.TLStorage;
import com.top_logic.element.layout.meta.TLPropertyFormBuilder;
import com.top_logic.element.layout.meta.TLStructuredTypePartFormBuilder.PartModel;
import com.top_logic.element.meta.kbbased.storage.I18NConstants;
import com.top_logic.element.meta.kbbased.storage.PrimitiveStorage;
import com.top_logic.element.model.diff.apply.ApplyModelPatch;
import com.top_logic.element.model.diff.config.CreateStructuredTypePart;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLType;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * Test that the model refuses a property that stores its values in a system column of each object,
 * and reports such a property of an existing model.
 */
@SuppressWarnings("javadoc")
public class TestSystemColumnProperties extends TestWithModelExtension {

	private static final String MODULE = TestSystemColumnProperties.class.getName();

	private static final String SYSTEM_COLUMN = "created";

	private TLModule _module;

	private TLClass _type;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_module = addModule(MODULE);
		_type = addClass(_module, "Holder");
	}

	@Override
	protected void tearDown() throws Exception {
		delete(_module);
		super.tearDown();
	}

	/**
	 * A model upgrade that adds a property with the name of a system column fails, and the
	 * message names the column.
	 */
	public void testUpgradeRefusesSystemColumnProperty() {
		try (Transaction tx = beginTX()) {
			applyCreateProperty(newProperty(SYSTEM_COLUMN));
			fail("A model upgrade must refuse a property with the name of the system column '" + SYSTEM_COLUMN + "'.");
		} catch (TopLogicException ex) {
			assertEquals(I18NConstants.ERROR_SYSTEM_COLUMN_NAME__COLUMN.fill(SYSTEM_COLUMN), ex.getErrorKey());
		}
		assertNull(_type.getPart(SYSTEM_COLUMN));
	}

	/**
	 * A property that names a system column as its explicit storage attribute uses this column on
	 * purpose and is accepted.
	 */
	public void testUpgradeAcceptsExplicitStorageAttribute() {
		try (Transaction tx = beginTX()) {
			AttributeConfig property = newProperty(SYSTEM_COLUMN);
			property.getAnnotations().add(explicitStorage(SYSTEM_COLUMN));
			applyCreateProperty(property);
			assertNotNull(_type.getPart(SYSTEM_COLUMN));
		}
	}

	/**
	 * A property with the name of a column that only an application table declares is accepted.
	 */
	public void testApplicationColumnAccepted() throws Exception {
		MOStructure applicationTable = (MOStructure) _kb.getMORepository().getMetaObject("StructuredElement");
		assertNotNull(applicationTable.getAttributeOrNull("name"));

		assertNull(PrimitiveStorage.systemColumnError("name"));
	}

	/**
	 * The load of a model that already has a property with the name of a system column reports one
	 * error that names the property and the column. A reference of such a name stores no value in
	 * the column and is not reported. The models of the engine report nothing.
	 */
	public void testLoadReportsSystemColumnProperty() {
		try (Transaction tx = beginTX()) {
			TLType stringType = TLModelUtil.findType("tl.core:String");
			TLModelUtil.addProperty(_type, SYSTEM_COLUMN, stringType);
			TLAssociationEnd end = TLModelUtil.addForwardReferenceEnd(_type, "modified", _type, false);
			TLModelUtil.addReference(_type, "modified", end);

			BufferingProtocol log = new BufferingProtocol();
			DynamicModelService.logSystemColumnProperties(log, ModelService.getApplicationModel());

			List<String> errors = log.getErrors();
			assertEquals(errors.toString(), 1, errors.size());
			String error = errors.get(0);
			assertTrue(error, error.contains("'" + TLModelUtil.qualifiedName(_type) + "#" + SYSTEM_COLUMN + "'"));
			assertTrue(error, error.contains("system column '" + SYSTEM_COLUMN + "'"));
		}
	}

	/**
	 * The editor of the classic model editor refuses a new property with the name of a system
	 * column.
	 */
	public void testClassicEditorRefusesSystemColumnProperty() {
		assertEquals(1, editorErrors(SYSTEM_COLUMN).size());
		assertEquals(List.of(), editorErrors("createdAt"));
	}

	private List<String> editorErrors(String name) {
		TLPropertyFormBuilder.EditModel editModel =
			TypedConfiguration.newConfigItem(TLPropertyFormBuilder.EditModel.class);
		editModel.setCreating(true);
		editModel.setContextType(_type);
		PartModel part = editModel.getPartModel();
		part.setName(name);
		part.setTypeSpec("tl.core:String");

		BufferingProtocol log = new BufferingProtocol();
		new ConstraintChecker().check(log, editModel);
		return log.getErrors();
	}

	private AttributeConfig newProperty(String name) {
		AttributeConfig property = TypedConfiguration.newConfigItem(AttributeConfig.class);
		property.setName(name);
		property.setTypeSpec("tl.core:String");
		return property;
	}

	private void applyCreateProperty(AttributeConfig property) {
		CreateStructuredTypePart create = TypedConfiguration.newConfigItem(CreateStructuredTypePart.class);
		create.setType(TLModelUtil.qualifiedName(_type));
		create.setPart(property);
		ApplyModelPatch.applyPatch(new BufferingProtocol(), ModelService.getApplicationModel(),
			ModelService.getInstance().getFactory(), List.of(create));
	}

	private static TLStorage explicitStorage(String column) {
		PrimitiveStorage.Config<?> storage = TypedConfiguration.newConfigItem(PrimitiveStorage.Config.class);
		storage.setStorageAttribute(column);
		TLStorage annotation = TypedConfiguration.newConfigItem(TLStorage.class);
		annotation.setImplementation(storage);
		return annotation;
	}

	public static Test suite() {
		return suite(TestSystemColumnProperties.class);
	}
}
