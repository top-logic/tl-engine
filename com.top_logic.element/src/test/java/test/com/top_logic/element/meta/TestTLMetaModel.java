/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta;

import java.util.Collection;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ThreadContextSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLModuleSingleton;
import com.top_logic.model.TLNamedPart;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLProperty;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.annotate.ui.ClassificationDisplay;
import com.top_logic.model.annotate.ui.ClassificationDisplay.ClassificationPresentation;
import com.top_logic.model.config.annotation.SystemEnum;
import com.top_logic.model.impl.generated.TlModelFactory;
import com.top_logic.util.model.ModelService;

/**
 * Tests for the TL Meta model, i.e. the model of the model.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestTLMetaModel extends TestPersistentModelPart {

	private static class MetaModelTestSetup extends ThreadContextSetup {
		public MetaModelTestSetup(Test test) {
			super(test);
		}

		@Override
		protected void doSetUp() throws Exception {
			extendApplicationModel(PersistencyLayer.getKnowledgeBase(), TestTLMetaModel.class, "model.xml");
		}

		@Override
		protected void doTearDown() throws Exception {
			Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction();
			TLModule module = ModelService.getApplicationModel().getModule("test.com.top_logic.element.meta.TestTLMetaModel");
			for (TLModuleSingleton singleton : module.getSingletons()) {
				// Must delete instances of classes before the TLClass can be deleted.
				singleton.getSingleton().tDelete();
			}
			module.tDelete();
			tx.commit();
		}

	}

	private TLModule _module;

	private TLPrimitive _primitive;

	private TLClass _typeRoot;

	private TLClass _typeA;

	private TLClass _typeB;

	private TLProperty _prop1;

	private TLReference _reference;

	private TLAssociationEnd _assEnd;

	private TLAssociation _association;

	private TLModel _applicationModel;

	private TLEnumeration _enum;

	private TLClassifier _classifier1;

	private TLClassifier _classifier2;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_applicationModel = ModelService.getApplicationModel();
		_module = _applicationModel.getModule("test.com.top_logic.element.meta.TestTLMetaModel");
		_primitive = (TLPrimitive) _module.getType("dataType1");
		_typeRoot = (TLClass) _module.getType("Root");
		_typeA = (TLClass) _module.getType("A");
		_typeB = (TLClass) _module.getType("B");
		_prop1 = (TLProperty) _typeA.getPartOrFail("prop1");
		_reference = (TLReference) _typeA.getPartOrFail("ref");
		_enum = (TLEnumeration) _module.getType("enum1");
		_classifier1 = _enum.getClassifier("classifier1");
		_classifier2 = _enum.getClassifier("classifier2");
		_assEnd = _reference.getEnd();
		_association = _assEnd.getOwner();
	}

	public void testCorrectMetaModelTypes() {
		assertCorrectType(TlModelFactory.getTLClassType(), TlModelFactory.getTLClassType());
		assertCorrectType(TlModelFactory.getTLClassType(), TlModelFactory.getTLModuleType());
		assertCorrectType(TlModelFactory.getTLClassType(), TlModelFactory.getTLModelType());
		assertCorrectType(TlModelFactory.getTLClassType(), TlModelFactory.getTLAssociationType());
		assertCorrectType(TlModelFactory.getTLClassType(), TlModelFactory.getTLEnumerationType());
		assertCorrectType(TlModelFactory.getTLClassType(), TlModelFactory.getTLClassifierType());
		assertCorrectType(TlModelFactory.getTLClassType(), TlModelFactory.getTLReferenceType());
		assertCorrectType(TlModelFactory.getTLClassType(), TlModelFactory.getTLPrimitiveType());
	}

	public void testCorrectModelTypes() {
		assertCorrectType(TlModelFactory.getTLClassType(), _typeA);
		assertCorrectType(TlModelFactory.getTLPropertyType(), _prop1);
		assertCorrectType(TlModelFactory.getTLModuleType(), _module);
		assertCorrectType(TlModelFactory.getTLModelType(), _applicationModel);
		assertCorrectType(TlModelFactory.getTLAssociationType(), _association);
		assertCorrectType(TlModelFactory.getTLEnumerationType(), _enum);
		assertCorrectType(TlModelFactory.getTLClassifierType(), _classifier1);
		assertCorrectType(TlModelFactory.getTLReferenceType(), _reference);
		assertCorrectType(TlModelFactory.getTLPrimitiveType(), _primitive);
	}

	private void assertCorrectType(TLClass expectedType, TLObject item) {
		assertEquals(expectedType, item.tType());
		assertEquals(expectedType, item.tValueByName(TLObject.T_TYPE_ATTR));
	}

	public void testPrimitives() {
		testPrimitive(_primitive);
	}

	public void testClass() {
		testClass(_typeA);
		testClass(_typeRoot);
		testClass(_typeB);
	}

	public void testAssociation() {
		testAssociation(_association);
	}

	public void testAssociationEnd() {
		testStructuredTypePart(_assEnd);
		assertEquals(_assEnd.getReference(), _assEnd.tValueByName(TLAssociationEnd.REFERENCE_ATTR));
		assertEquals(_assEnd.isComposite(), _assEnd.tValueByName(TLAssociationEnd.COMPOSITE_ATTR));
		assertEquals(_assEnd.isAggregate(), _assEnd.tValueByName(TLAssociationEnd.AGGREGATE_ATTR));
		assertEquals(_assEnd.canNavigate(), _assEnd.tValueByName(TLAssociationEnd.NAVIGATE_ATTR));
	}

	public void testModule() {
		testNamedModelPart(_module);
		assertEquals(_module.getSingletons(), _module.tValueByName(TLModule.SINGLETONS_ATTR));
		_module.getSingletons().forEach(this::testSingleton);
		assertEquals(toSet(_module.getTypes()), _module.tValueByName(TLModule.TYPES_ATTR));
	}

	public void testModel() {
		testModelPart(_applicationModel);
		assertEquals(_applicationModel.getModules(), _applicationModel.tValueByName(TLModel.MODULES_ATTR));
	}

	public void testClassifier() {
		testTypePart(_classifier1);
		assertFalse(_classifier1.isDefault());
		assertEquals(_classifier1.isDefault(), _classifier1.tValueByName(TLClassifier.DEFAULT_ATTR));
		assertTrue(_classifier2.isDefault());
		assertEquals(_classifier2.isDefault(), _classifier2.tValueByName(TLClassifier.DEFAULT_ATTR));
	}

	public void testEnumeration() {
		testStructuredType(_enum);
		assertEquals(toSet(_enum.getClassifiers()), _enum.tValueByName(TLEnumeration.CLASSIFIERS_ATTR));
	}

	public void testProperty() {
		testStructuredTypePart(_prop1);
	}

	public void testReference() {
		testStructuredTypePart(_reference);
		assertEquals(_reference.getEnd(), _reference.tValueByName(TLReference.END_ATTR));
	}

	private void testStructuredTypePart(TLStructuredTypePart structuredTypePart) {
		testTypePart(structuredTypePart);
		assertEquals(structuredTypePart.isMandatory(),
			structuredTypePart.tValueByName(TLStructuredTypePart.MANDATORY_ATTR));
		assertEquals(structuredTypePart.isMultiple(),
			structuredTypePart.tValueByName(TLStructuredTypePart.MULTIPLE_ATTR));
		assertEquals(structuredTypePart.isOrdered(),
			structuredTypePart.tValueByName(TLStructuredTypePart.ORDERED_ATTR));
		assertEquals(structuredTypePart.isBag(),
			structuredTypePart.tValueByName(TLStructuredTypePart.BAG_ATTR));
	}

	private void testSingleton(TLModuleSingleton singleton) {
		assertEquals(singleton.getName(), singleton.tValueByName(TLModuleSingleton.NAME_ATTR));
		assertEquals(singleton.getModule(), singleton.tValueByName(TLModuleSingleton.MODULE_ATTR));
		assertEquals(singleton.getSingleton(), singleton.tValueByName(TLModuleSingleton.SINGLETON_ATTR));
	}

	private void testTypePart(TLTypePart typePart) {
		testNamedModelPart(typePart);
		assertEquals(typePart.getType(), typePart.tValueByName(TLTypePart.TYPE_ATTR));
		assertEquals(typePart.getOwner(), typePart.tValueByName(TLTypePart.OWNER_ATTR));
	}

	private void testNamedModelPart(TLNamedPart namedPart) {
		testModelPart(namedPart);
		assertEquals(namedPart.getName(), namedPart.tValueByName(TLNamedPart.NAME_ATTR));
	}

	private void testModelPart(TLModelPart modelPart) {
		assertEquals(modelPart.getModel(), modelPart.tValueByName(TLModelPart.MODEL_ATTR));
		assertEquals(_applicationModel, modelPart.getModel());
	}

	private void testClass(TLClass type) {
		testStructuredType(type);
		assertEquals(toSet(type.getLocalParts()), type.tValueByName(TLClass.LOCAL_PARTS_ATTR));
		assertEquals(type.isAbstract(), type.tValueByName(TLClass.ABSTRACT_ATTR));
		assertEquals(type.isFinal(), type.tValueByName(TLClass.FINAL_ATTR));
		// Concrete Collection type of specializations is not defined.
		assertEquals(toSet(type.getSpecializations()),
			toSet((Collection<?>) type.tValueByName(TLClass.SPECIALIZATIONS_ATTR)));
		assertEquals(type.getGeneralizations(), type.tValueByName(TLClass.GENERALIZATIONS_ATTR));
	}

	private void testAssociation(TLAssociation type) {
		testStructuredType(type);
		assertEquals(toSet(type.getLocalParts()), type.tValueByName(TLAssociation.LOCAL_PARTS_ATTR));
	}

	private void testStructuredType(TLStructuredType type) {
		testType(type);
	}

	private void testPrimitive(TLPrimitive type) {
		testType(type);
	}

	private void testType(TLType type) {
		testNamedModelPart(type);
		assertEquals(type.getModule(), type.tValueByName(TLType.MODULE_ATTR));
		assertEquals(type.getScope(), type.tValueByName(TLType.SCOPE_ATTR));
	}

	public void testAnnotationsAccess() {
		SystemEnum systemEnum = TypedConfiguration.newConfigItem(SystemEnum.class);
		systemEnum.setValue(false);
		ClassificationDisplay classificationDisplay = TypedConfiguration.newConfigItem(ClassificationDisplay.class);
		classificationDisplay.setValue(ClassificationPresentation.RADIO_INLINE);
		List<? extends TLAnnotation> expectedAnnotations = list(classificationDisplay, systemEnum);
		List<? extends TLAnnotation> annotationsByAPI = toList(_enum.getAnnotations());
		assertConfigEquals(expectedAnnotations, annotationsByAPI);
		@SuppressWarnings("unchecked")
		List<? extends TLAnnotation> annotationsByReflection =
			toList((Collection<? extends TLAnnotation>) _enum.tValueByName(TLModelPart.ANNOTATIONS_ATTR));
		assertConfigEquals(expectedAnnotations, annotationsByReflection);
	}

	public void testReferers() {
		assertEquals(
			"`test.com.top_logic.element.meta.TestTLMetaModel:A#ref` has `test.com.top_logic.element.meta.TestTLMetaModel:B` as target type",
			_typeB, _reference.tValue(TlModelFactory.getTypeTLReferenceAttr()));
		assertEquals(
			"`test.com.top_logic.element.meta.TestTLMetaModel:A#ref` has `test.com.top_logic.element.meta.TestTLMetaModel:B` as target type",
			set(_reference), _typeB.tReferers(TlModelFactory.getTypeTLReferenceAttr()));
	}

	/**
	 * @return a cumulative {@link Test} for all Tests in {@link TestTLMetaModel}.
	 */
	public static Test suite() {
		return suite(new MetaModelTestSetup(new TestSuite(TestTLMetaModel.class)));
	}

}
