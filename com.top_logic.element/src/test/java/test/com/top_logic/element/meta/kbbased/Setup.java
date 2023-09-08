/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta.kbbased;

import junit.framework.Test;

import test.com.top_logic.basic.ThreadContextSetup;

import com.top_logic.basic.col.NameValueBuffer;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.dob.ex.DuplicateAttributeException;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.element.config.ReferenceConfig;
import com.top_logic.element.config.annotation.TLStorage;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.element.meta.kbbased.PersistentReference;
import com.top_logic.element.meta.kbbased.WrapperMetaAttributeUtil;
import com.top_logic.element.meta.kbbased.storage.LinkStorage.Config;
import com.top_logic.element.meta.kbbased.storage.ListStorage;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.service.db2.PersistentObject;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.util.Messages;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * {@link ThreadContextSetup} to build {@link TLClass} for tests
 * 
 * @since 5.8.0
 * 
 *          test.com.top_logic.element.meta.kbbased.TestKBBasedMetaAttributes$Setup
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class Setup extends ThreadContextSetup {

	/** Name of an multiple reference to any object */
	public static final String LIST_1_ATTR = "list1";
	
	/** Name of an multiple reference to any object */
	static final String LIST_2_ATTR = "list2";
	
	public static final String STRUCTURED_ELEMENT_TABLE_NAME = "StructuredElement";

	public static final String GENERIC_OBJECT_TABLE_NAME = "GenericObject";

	static final String STRUCTURE_NAME = Setup.class.getName().replace('.', '_') + "_structure";

	static final String NODE_NAME = Setup.class.getName().replace('.', '_') + "_node";
	
	/** Name of the association table used by {@link #LIST_2_ATTR} to store their values. */
	/* Defined in DemoMeta.xml */
	static final String LIST_2_ATTR_TABLE = "otherHasWrapperAttValue";
	
	static TLClass metaElement;

	private TLModule _module;

	public Setup(Test test) {
		super(test);
	}
	
	@Override
	protected void doSetUp() throws Exception {
		checkTableExistence();
		createMetaElement();
	}

	private void checkTableExistence() throws UnknownTypeException {
		try {
			kb().getMORepository().getMetaObject(LIST_2_ATTR_TABLE);
		} catch (UnknownTypeException ex) {
			StringBuilder error = new StringBuilder();
			error.append(Setup.class.getName());
			error.append(" expects that '");
			error.append(LIST_2_ATTR_TABLE);
			error.append("' exists, because it is used by '");
			error.append(LIST_2_ATTR);
			error.append("' to store its data.");
			throw new UnknownTypeException(error.toString(), ex);
		}
	}

	private void createMetaElement() throws DuplicateAttributeException, ConfigurationException, KnowledgeBaseException {
		Transaction tx = kb().beginTransaction();
		TLModel tlModel = ModelService.getApplicationModel();
		_module = TLModelUtil.makeModule(tlModel, STRUCTURE_NAME);
		metaElement = TLModelUtil.addClass(_module, NODE_NAME);
		
		createListAttr(Setup.LIST_1_ATTR);
		createListAttr(Setup.LIST_2_ATTR, LIST_2_ATTR_TABLE);

		tx.commit();
	}

	private KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	private void createListAttr(String name) throws DuplicateAttributeException, ConfigurationException {
		createListAttr(name, WrapperMetaAttributeUtil.WRAPPER_ATTRIBUTE_ASSOCIATION);
	}

	private void createListAttr(String name, String associationName) throws DuplicateAttributeException,
			ConfigurationException {
		ReferenceConfig destRefConfig = TypedConfiguration.newConfigItem(ReferenceConfig.class);
		destRefConfig.setName(name);
		destRefConfig.setMultiple(true);
		destRefConfig.setBag(true);
		destRefConfig.setOrdered(true);
		destRefConfig.setTypeSpec(TLModelUtil.qualifiedName(metaElement));
		PersistentReference reference =
			(PersistentReference) TestKBBasedMetaAttributes.createReference(metaElement, destRefConfig);
		Config linkStorageConfig = TypedConfiguration.newConfigItem(ListStorage.Config.class);
		linkStorageConfig.setImplementationClass(ListStorage.class);
		linkStorageConfig.setTable(associationName);
		TLStorage storage = TypedConfiguration.newConfigItem(TLStorage.class);
		storage.setImplementation(linkStorageConfig);
		reference.setAnnotation(storage);
	}

	@Override
	protected void doTearDown() throws Exception {
		removeModule();
	}

	private void removeModule() {
		KnowledgeBase kb = _module.tKnowledgeBase();
		Transaction tx =
			kb.beginTransaction(
				Messages.DELETED_MODEL_PART_RECURSIVELY__PART_NAME.fill(TLModelUtil.qualifiedName(_module)));
		try {
			TLModelUtil.deleteRecursive(_module);

			tx.commit();
		} finally {
			tx.rollback();
		}
	}

	/**
	 * Creates an Object with {@link #metaElement} as type and {@link #STRUCTURE_NAME} as structure,
	 * stored in {@value #STRUCTURED_ELEMENT_TABLE_NAME}.
	 * 
	 * @param name
	 *        Name of the resulting item
	 * 
	 * @see #newGenericObject(String)
	 */
	public static Wrapper newStructuredElementObject(String name) {
		return newObject(name, STRUCTURED_ELEMENT_TABLE_NAME);
	}

	/**
	 * Creates an Object with {@link #metaElement} as type and {@link #STRUCTURE_NAME} as structure,
	 * stored in {@value #GENERIC_OBJECT_TABLE_NAME}.
	 * 
	 * @param name
	 *        Name of the resulting item
	 * 
	 * @see #newStructuredElementObject(String)
	 */
	public static Wrapper newGenericObject(String name) {
		return newObject(name, GENERIC_OBJECT_TABLE_NAME);
	}

	private static Wrapper newObject(String name, String table) {
		NameValueBuffer initialValues = new NameValueBuffer();
		initialValues.put(AbstractWrapper.NAME_ATTRIBUTE, name);
		initialValues.put(PersistentObject.TYPE_REF, ((Wrapper) metaElement).tHandle());
		KnowledgeObject ko = TestWrapperListMetaAttribute.kb().createKnowledgeObject(table, initialValues);
		
		return (Wrapper) WrapperFactory.getWrapper(ko);
	}

	/**
	 * Returns the {@link TLStructuredTypePart} {@link #LIST_1_ATTR}
	 */
	public static TLStructuredTypePart list1Attr() {
		try {
			return MetaElementUtil.getMetaAttribute(metaElement, LIST_1_ATTR);
		} catch (NoSuchAttributeException ex) {
			throw new RuntimeException(LIST_1_ATTR + " is defined at " + metaElement, ex);
		}
	}

	/**
	 * Returns the {@link TLStructuredTypePart} {@link #LIST_2_ATTR}
	 */
	public static TLStructuredTypePart list2Attr() {
		try {
			return MetaElementUtil.getMetaAttribute(metaElement, LIST_2_ATTR);
		} catch (NoSuchAttributeException ex) {
			throw new RuntimeException(LIST_2_ATTR + " is defined at " + metaElement, ex);
		}
	}
	
}
