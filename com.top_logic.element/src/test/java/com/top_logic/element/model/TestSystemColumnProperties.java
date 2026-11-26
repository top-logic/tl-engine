/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model;

import java.util.ArrayList;
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
import com.top_logic.element.meta.AttributeWithFallbackStorage;
import com.top_logic.element.meta.kbbased.storage.I18NConstants;
import com.top_logic.element.meta.kbbased.storage.PrimitiveStorage;
import com.top_logic.element.model.diff.apply.ApplyModelPatch;
import com.top_logic.element.model.diff.config.CreateStructuredTypePart;
import com.top_logic.element.model.diff.config.DiffElement;
import com.top_logic.element.model.diff.config.RenamePart;
import com.top_logic.element.model.diff.config.UpdatePartType;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.config.annotation.TableName;
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

	private static final String STRING = "tl.core:String";

	private static final String I18N_STRING = "tl.model.i18n:I18NString";

	private static final String APPLICATION_TABLE = "StructuredElement";

	private static final String APPLICATION_COLUMN = "name";

	private static final String LINK_TABLE = "hasWrapperAttValue";

	private static final String OBJECT_COLUMN = "tType";

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
			applyCreateProperty(_type, newProperty(SYSTEM_COLUMN, STRING));
			fail("A model upgrade must refuse a property with the name of the system column '" + SYSTEM_COLUMN + "'.");
		} catch (TopLogicException ex) {
			assertEquals(I18NConstants.ERROR_SYSTEM_COLUMN_NAME__COLUMN.fill(SYSTEM_COLUMN), ex.getErrorKey());
		}
		assertNull(_type.getPart(SYSTEM_COLUMN));
	}

	/**
	 * A model upgrade that renames a property to the name of a system column fails.
	 */
	public void testUpgradeRefusesRenameToSystemColumn() {
		try (Transaction tx = beginTX()) {
			TLModelUtil.addProperty(_type, "createdNote", TLModelUtil.findType(STRING));
			tx.commit();
		}
		RenamePart rename = TypedConfiguration.newConfigItem(RenamePart.class);
		rename.setPart(TLModelUtil.qualifiedName(_type) + "#createdNote");
		rename.setNewName(SYSTEM_COLUMN);
		try (Transaction tx = beginTX()) {
			applyPatch(rename);
			fail("A model upgrade must refuse to rename a property to the system column '" + SYSTEM_COLUMN + "'.");
		} catch (TopLogicException ex) {
			assertEquals(I18NConstants.ERROR_SYSTEM_COLUMN_NAME__COLUMN.fill(SYSTEM_COLUMN), ex.getErrorKey());
		}
	}

	/**
	 * A model upgrade that gives a property with the name of a system column a value type without a
	 * storage of its own fails.
	 */
	public void testUpgradeRefusesTypeChangeToSystemColumn() {
		try (Transaction tx = beginTX()) {
			TLModelUtil.addProperty(_type, SYSTEM_COLUMN, TLModelUtil.findType(I18N_STRING));
			tx.commit();
		}
		UpdatePartType typeChange = TypedConfiguration.newConfigItem(UpdatePartType.class);
		typeChange.setPart(TLModelUtil.qualifiedName(_type) + "#" + SYSTEM_COLUMN);
		typeChange.setTypeSpec(STRING);
		try (Transaction tx = beginTX()) {
			applyPatch(typeChange);
			fail("A model upgrade must refuse the value type '" + STRING + "' for the property '" + SYSTEM_COLUMN
				+ "'.");
		} catch (TopLogicException ex) {
			assertEquals(I18NConstants.ERROR_SYSTEM_COLUMN_NAME__COLUMN.fill(SYSTEM_COLUMN), ex.getErrorKey());
		}
	}

	/**
	 * A property that stored its values in a system column before the upgrade does not stop the
	 * upgrade: the model load only reports it.
	 */
	public void testUpgradeKeepsExistingSystemColumnProperty() {
		try (Transaction tx = beginTX()) {
			TLModelUtil.addProperty(_type, SYSTEM_COLUMN, TLModelUtil.findType(STRING));
			tx.commit();
		}
		try (Transaction tx = beginTX()) {
			applyCreateProperty(_type, newProperty("createdNote", STRING));
			assertNotNull(_type.getPart("createdNote"));
		}
	}

	/**
	 * A property of a link type is checked against the columns of its link table: a column of each
	 * object that the link table does not have is no system column there.
	 */
	public void testLinkTableHasItsOwnColumns() throws Exception {
		MOStructure linkTable = (MOStructure) _kb.getMORepository().getMetaObject(LINK_TABLE);
		assertNull(linkTable.getAttributeOrNull(OBJECT_COLUMN));
		TLAssociation association;
		try (Transaction tx = beginTX()) {
			association = TLModelUtil.addAssociation(_module, "Link");
			TableName table = TypedConfiguration.newConfigItem(TableName.class);
			table.setName(LINK_TABLE);
			association.setAnnotation(table);
			tx.commit();
		}
		TLType string = TLModelUtil.findType(STRING);
		assertNull(PrimitiveStorage.systemColumnError(association, OBJECT_COLUMN, string, false, null));
		assertEquals(I18NConstants.ERROR_SYSTEM_COLUMN_NAME__COLUMN.fill(SYSTEM_COLUMN),
			PrimitiveStorage.systemColumnError(association, SYSTEM_COLUMN, string, false, null));
	}

	/**
	 * A property that names a system column as its explicit storage attribute uses this column on
	 * purpose and is accepted.
	 */
	public void testUpgradeAcceptsExplicitStorageAttribute() {
		try (Transaction tx = beginTX()) {
			AttributeConfig property = newProperty(SYSTEM_COLUMN, STRING);
			property.getAnnotations().add(explicitStorage(SYSTEM_COLUMN));
			applyCreateProperty(_type, property);
			assertNotNull(_type.getPart(SYSTEM_COLUMN));
		}
	}

	/**
	 * A property with the name of a column that only an application table declares stores its
	 * values in this column and is accepted.
	 */
	public void testApplicationColumnAccepted() throws Exception {
		MOStructure applicationTable = (MOStructure) _kb.getMORepository().getMetaObject(APPLICATION_TABLE);
		assertNotNull(applicationTable.getAttributeOrNull(APPLICATION_COLUMN));
		TLClass type;
		try (Transaction tx = beginTX()) {
			type = TLModelUtil.addClass(_module, "Element");
			TableName table = TypedConfiguration.newConfigItem(TableName.class);
			table.setName(APPLICATION_TABLE);
			type.setAnnotation(table);
			tx.commit();
		}
		try (Transaction tx = beginTX()) {
			applyCreateProperty(type, newProperty(APPLICATION_COLUMN, STRING));
			TLStructuredTypePart property = type.getPart(APPLICATION_COLUMN);
			assertNotNull(property);
			assertEquals(APPLICATION_COLUMN,
				((PrimitiveStorage<?>) property.getStorageImplementation()).getStorageAttribute());
		}
	}

	/**
	 * A property with the name of a system column, whose value type has a storage of its own,
	 * stores no value in the column and is accepted.
	 */
	public void testOwnStorageOfValueTypeAccepted() {
		TLType i18nString = TLModelUtil.findType(I18N_STRING);
		assertNull(PrimitiveStorage.systemColumnError(_type, SYSTEM_COLUMN, i18nString, false, null));
		try (Transaction tx = beginTX()) {
			applyCreateProperty(_type, newProperty(SYSTEM_COLUMN, I18N_STRING));
			assertNotNull(_type.getPart(SYSTEM_COLUMN));
		}
	}

	/**
	 * A model upgrade creates the parts of a type one after the other. The check of a new property
	 * must not initialize its storage: the storage of a property with a fallback refers to parts
	 * that the upgrade creates later.
	 */
	public void testUpgradeCreatesPropertyReferringToLaterParts() {
		AttributeConfig value = newProperty("value", STRING);
		value.getAnnotations().add(fallbackStorage("storage", "fallback"));
		try (Transaction tx = beginTX()) {
			applyCreateProperties(_type, value, newProperty("storage", STRING), newProperty("fallback", STRING));
			assertNotNull(_type.getPart("value"));
			assertNotNull(_type.getPart("fallback"));
		}
	}

	/**
	 * The load of a model that already has a property with the name of a system column reports one
	 * error that names the property and the column. A reference of such a name stores no value in
	 * the column and is not reported. The models of the engine report nothing.
	 */
	public void testLoadReportsSystemColumnProperty() {
		try (Transaction tx = beginTX()) {
			TLType stringType = TLModelUtil.findType(STRING);
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
	 * column, unless the form binds the column on purpose with an explicit storage attribute.
	 */
	public void testClassicEditorRefusesSystemColumnProperty() {
		assertEquals(1, editorErrors(SYSTEM_COLUMN).size());
		assertEquals(List.of(), editorErrors("createdAt"));
		assertEquals(List.of(), editorErrors(SYSTEM_COLUMN, explicitStorage(SYSTEM_COLUMN)));
	}

	private List<String> editorErrors(String name, TLStorage... annotations) {
		TLPropertyFormBuilder.EditModel editModel =
			TypedConfiguration.newConfigItem(TLPropertyFormBuilder.EditModel.class);
		editModel.setCreating(true);
		editModel.setContextType(_type);
		PartModel part = editModel.getPartModel();
		part.setName(name);
		part.setTypeSpec(STRING);
		for (TLStorage annotation : annotations) {
			part.getAnnotations().add(annotation);
		}

		BufferingProtocol log = new BufferingProtocol();
		new ConstraintChecker().check(log, editModel);
		return log.getErrors();
	}

	private static AttributeConfig newProperty(String name, String typeSpec) {
		AttributeConfig property = TypedConfiguration.newConfigItem(AttributeConfig.class);
		property.setName(name);
		property.setTypeSpec(typeSpec);
		return property;
	}

	private static void applyCreateProperty(TLClass type, AttributeConfig property) {
		applyCreateProperties(type, property);
	}

	private static void applyCreateProperties(TLClass type, AttributeConfig... properties) {
		List<DiffElement> patch = new ArrayList<>();
		for (AttributeConfig property : properties) {
			CreateStructuredTypePart create = TypedConfiguration.newConfigItem(CreateStructuredTypePart.class);
			create.setType(TLModelUtil.qualifiedName(type));
			create.setPart(property);
			patch.add(create);
		}
		applyPatch(patch.toArray(DiffElement[]::new));
	}

	private static void applyPatch(DiffElement... patch) {
		ApplyModelPatch.applyPatch(new BufferingProtocol(), ModelService.getApplicationModel(),
			ModelService.getInstance().getFactory(), List.of(patch));
	}

	private static TLStorage fallbackStorage(String storageAttribute, String fallbackAttribute) {
		AttributeWithFallbackStorage.Config<?> storage =
			TypedConfiguration.newConfigItem(AttributeWithFallbackStorage.Config.class);
		storage.update(storage.descriptor().getProperty(AttributeWithFallbackStorage.Config.STORAGE_ATTRIBUTE),
			storageAttribute);
		storage.update(storage.descriptor().getProperty(AttributeWithFallbackStorage.Config.FALLBACK_ATTRIBUTE),
			fallbackAttribute);
		TLStorage annotation = TypedConfiguration.newConfigItem(TLStorage.class);
		annotation.setImplementation(storage);
		return annotation;
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
