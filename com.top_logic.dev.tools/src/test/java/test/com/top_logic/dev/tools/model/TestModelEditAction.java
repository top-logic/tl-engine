/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.dev.tools.model;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.dev.tools.model.I18NConstants;
import com.top_logic.dev.tools.model.ModelEditAction;
import com.top_logic.dev.tools.model.ModelEditAction.Mode;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLProperty;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.DynamicVisibility;
import com.top_logic.model.annotate.ModeSelector;
import com.top_logic.model.annotate.util.AttributeSettings;
import com.top_logic.model.form.definition.FormVisibility;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.CompatibilityService;
import com.top_logic.util.model.ModelService;

/**
 * Test of {@link ModelEditAction} creating references of a given kind with an opposite reference,
 * refusing an attribute with the name of a system column, adding and removing generalizations
 * of a class (also by deleting a generalization link), deleting model parts, and of the
 * visibility of the reference kind and opposite fields of the "new attribute" dialog.
 */
@SuppressWarnings("javadoc")
public class TestModelEditAction extends TestCase {

	private static final String NEW_ATTRIBUTE = "tl.devtools:NewAttribute";

	private static final String NEW_GENERALIZATION = "tl.devtools:NewGeneralization";

	private static final String GENERALIZATION_LINK = "tl.devtools:Generalization";

	private static final String SPECIALIZATION = "specialization";

	private static final String REFERENCE_KIND = "tl.devtools:ReferenceKind";

	private static final String NAME = "name";

	private static final String TYPE = "type";

	private static final String KIND = "referenceKind";

	private static final String OPPOSITE = "opposite";

	private static final String GENERALIZATION = "generalization";

	private static final String KIND_NORMAL = "normal";

	private static final String KIND_COMPOSITION = "composition";

	private static final String KIND_AGGREGATION = "aggregation";

	/** Name of the channel holding the container of the action. */
	private static final String CONTAINER = "container";

	private static final String TEST_MODULE = "test.devtools.edit";

	private static final String REF = "ref";

	private static final String BACK = "back";

	public void testCreateNormalReference() {
		inTestModule((owner, target, enumeration) -> {
			TLReference reference = createReference(owner, target, KIND_NORMAL, null);
			assertFalse(reference.isComposite());
			assertFalse(reference.getEnd().isAggregate());
			assertFalse(ownerEnd(reference).isAggregate());
			assertTrue(reference.getEnd().canNavigate());
			assertNull(ownerEnd(reference).getReference());
		});
	}

	public void testCreateComposition() {
		inTestModule((owner, target, enumeration) -> {
			TLReference reference = createReference(owner, target, KIND_COMPOSITION, null);
			assertTrue(reference.getEnd().isComposite());
			assertTrue(ownerEnd(reference).isAggregate());
			assertFalse(ownerEnd(reference).isMultiple());
		});
	}

	public void testCreateAggregation() {
		inTestModule((owner, target, enumeration) -> {
			TLReference reference = createReference(owner, target, KIND_AGGREGATION, null);
			assertFalse(reference.getEnd().isComposite());
			assertFalse(reference.getEnd().isAggregate());
			assertTrue(ownerEnd(reference).isAggregate());
			assertTrue(ownerEnd(reference).isMultiple());
		});
	}

	public void testCreateOpposite() {
		inTestModule((owner, target, enumeration) -> {
			TLReference reference = createReference(owner, target, KIND_NORMAL, BACK);
			TLReference back = (TLReference) target.getPart(BACK);
			assertNotNull(back);
			assertSame(ownerEnd(reference), back.getEnd());
			assertTrue(back.getEnd().canNavigate());
			assertSame(owner, back.getType());
			assertSame(back.getEnd(), TLModelUtil.getOtherEnd(reference.getEnd()));
		});
	}

	public void testCreateCompositionWithOpposite() {
		inTestModule((owner, target, enumeration) -> {
			TLReference reference = createReference(owner, target, KIND_COMPOSITION, BACK);
			TLReference back = (TLReference) target.getPart(BACK);
			assertTrue(reference.isComposite());
			assertFalse(back.isMultiple());
		});
	}

	public void testCompositionToEnumerationRefused() {
		inTestModule((owner, target, enumeration) -> {
			assertRefused(I18NConstants.ERROR_KIND_REQUIRES_CLASS__TYPE,
				() -> createReference(owner, enumeration, KIND_COMPOSITION, null));
			assertNull(owner.getPart(REF));
		});
	}

	public void testOppositeAtEnumerationRefused() {
		inTestModule((owner, target, enumeration) -> {
			assertRefused(I18NConstants.ERROR_REFERENCE_NOT_SUPPORTED__OWNER,
				() -> createReference(owner, enumeration, KIND_NORMAL, BACK));
			assertNull(owner.getPart(REF));
		});
	}

	public void testExistingOppositeRefused() {
		inTestModule((owner, target, enumeration) -> {
			TLModelUtil.addForwardReference(target, BACK, owner);
			int targetParts = target.getLocalParts().size();
			assertRefused(I18NConstants.ERROR_PART_EXISTS__TYPE_NAME,
				() -> createReference(owner, target, KIND_NORMAL, BACK));
			assertNull(owner.getPart(REF));
			assertEquals(targetParts, target.getLocalParts().size());
		});
	}

	public void testSelfOppositeWithSameNameRefused() {
		inTestModule((owner, target, enumeration) -> {
			assertRefused(I18NConstants.ERROR_PART_EXISTS__TYPE_NAME,
				() -> createReference(owner, owner, KIND_NORMAL, REF));
			assertNull(owner.getPart(REF));
		});
	}

	public void testExistingNameRefused() {
		inTestModule((owner, target, enumeration) -> {
			createReference(owner, target, KIND_NORMAL, null);
			assertRefused(I18NConstants.ERROR_PART_EXISTS__TYPE_NAME,
				() -> createReference(owner, target, KIND_NORMAL, null));
		});
	}

	public void testSystemColumnNameRefused() {
		inTestModule((owner, target, enumeration) -> {
			TLObject input =
				TransientObjectFactory.INSTANCE.createObject((TLClass) TLModelUtil.findType(NEW_ATTRIBUTE));
			input.tUpdateByName(NAME, "created");
			input.tUpdateByName(TYPE, TLModelUtil.findType("tl.core:String"));
			assertRefused(
				com.top_logic.element.meta.kbbased.storage.I18NConstants.ERROR_SYSTEM_COLUMN_NAME__COLUMN,
				() -> execute(Mode.CREATE_ATTRIBUTE, owner, input));
			assertNull(owner.getPart("created"));
		});
	}

	public void testAddAndRemoveGeneralization() {
		inTestModule((owner, target, enumeration) -> {
			assertSame(owner, addGeneralization(owner, target));
			assertEquals(List.of(target), owner.getGeneralizations());

			// Adding again does not duplicate.
			addGeneralization(owner, target);
			assertEquals(List.of(target), owner.getGeneralizations());

			assertSame(owner, execute(Mode.REMOVE_GENERALIZATION, owner, target));
			assertTrue(owner.getGeneralizations().isEmpty());
		});
	}

	public void testDeleteGeneralizationLink() {
		inTestModule((owner, target, enumeration) -> {
			TLClass other = TLModelUtil.addClass(owner.getModule(), "Other");
			addGeneralization(owner, target);
			addGeneralization(owner, other);

			assertSame("The specialization is what the link belonged to.", owner,
				execute(Mode.DELETE, null, generalizationLink(owner, target)));
			assertEquals(List.of(other), owner.getGeneralizations());
			assertNotNull("Only the link is removed, not the generalized class.",
				owner.getModule().getType("Target"));
		});
	}

	public void testDeleteReturnsWhatTheDeletedPartBelongedTo() {
		inTestModule((owner, target, enumeration) -> {
			TLModule module = owner.getModule();
			TLProperty property = TLModelUtil.addProperty(owner, "prop", TLModelUtil.findType("tl.core:String"));
			TLClassifier classifier = TLModelUtil.addClassifier(enumeration, "first");

			assertSame("The owner of an attribute.", owner, execute(Mode.DELETE, null, property));
			assertSame("The enumeration of a classifier.", enumeration, execute(Mode.DELETE, null, classifier));
			assertSame("The module of a type.", module, execute(Mode.DELETE, null, target));
			assertNull("Nothing for a module.", execute(Mode.DELETE, null, module));
		});
	}

	public void testCyclicGeneralizationRefused() {
		inTestModule((owner, target, enumeration) -> {
			addGeneralization(owner, target);
			assertRefused(I18NConstants.ERROR_CYCLIC_GENERALIZATION__CLASS_GENERALIZATION,
				() -> addGeneralization(target, owner));
			assertTrue(target.getGeneralizations().isEmpty());
		});
	}

	public void testTransitiveCyclicGeneralizationRefused() {
		inTestModule((owner, target, enumeration) -> {
			TLClass middle = TLModelUtil.addClass(owner.getModule(), "Middle");
			addGeneralization(owner, middle);
			addGeneralization(middle, target);
			assertRefused(I18NConstants.ERROR_CYCLIC_GENERALIZATION__CLASS_GENERALIZATION,
				() -> addGeneralization(target, owner));
			assertTrue(target.getGeneralizations().isEmpty());
		});
	}

	public void testSelfGeneralizationRefused() {
		inTestModule((owner, target, enumeration) -> {
			assertRefused(I18NConstants.ERROR_CYCLIC_GENERALIZATION__CLASS_GENERALIZATION,
				() -> addGeneralization(owner, owner));
			assertTrue(owner.getGeneralizations().isEmpty());
		});
	}

	public void testFinalGeneralizationRefused() {
		inTestModule((owner, target, enumeration) -> {
			target.setFinal(true);
			assertRefused(I18NConstants.ERROR_FINAL_GENERALIZATION__CLASS,
				() -> addGeneralization(owner, target));
			assertTrue(owner.getGeneralizations().isEmpty());
		});
	}

	public void testReferenceFieldsShownForClass() {
		inTestModule((owner, target, enumeration) -> {
			TLObject input = newAttribute(target, KIND_NORMAL, null);
			assertEquals(FormVisibility.DEFAULT, mode(input, KIND));
			assertEquals(FormVisibility.DEFAULT, mode(input, OPPOSITE));
		});
	}

	public void testReferenceFieldsHiddenForEnumeration() {
		inTestModule((owner, target, enumeration) -> {
			TLObject input = newAttribute(enumeration, KIND_NORMAL, null);
			assertEquals(FormVisibility.HIDDEN, mode(input, KIND));
			assertEquals(FormVisibility.HIDDEN, mode(input, OPPOSITE));
		});
	}

	public void testReferenceFieldsHiddenWithoutType() {
		inTestModule((owner, target, enumeration) -> {
			TLObject input = newAttribute(null, KIND_NORMAL, "");
			assertEquals(FormVisibility.HIDDEN, mode(input, KIND));
			assertEquals(FormVisibility.HIDDEN, mode(input, OPPOSITE));
		});
	}

	public void testReferenceFieldsWithValueStayVisible() {
		inTestModule((owner, target, enumeration) -> {
			// A value entered for a class stays visible, so it can be reset.
			TLObject input = newAttribute(enumeration, KIND_COMPOSITION, BACK);
			assertEquals(FormVisibility.DEFAULT, mode(input, KIND));
			assertEquals(FormVisibility.DEFAULT, mode(input, OPPOSITE));
		});
	}

	private static FormVisibility mode(TLObject input, String attribute) {
		TLStructuredTypePart part = input.tType().getPartOrFail(attribute);
		ModeSelector selector = DynamicVisibility.modeSelector(part);
		assertNotNull(selector);
		return selector.getMode(input, part, true);
	}

	private static TLAssociationEnd ownerEnd(TLReference reference) {
		return TLModelUtil.getOtherEnd(reference.getEnd());
	}

	private static void assertRefused(Object expected, Runnable change) {
		try {
			change.run();
			fail("Change must be refused with " + expected + ".");
		} catch (TopLogicException ex) {
			assertEquals(expected, ex.getErrorKey().plain());
		}
	}

	private static TLReference createReference(TLClass owner, Object target, String kind, String opposite) {
		return (TLReference) execute(Mode.CREATE_ATTRIBUTE, owner, newAttribute(target, kind, opposite));
	}

	private static TLObject newAttribute(Object target, String kind, String opposite) {
		TLObject input = TransientObjectFactory.INSTANCE.createObject((TLClass) TLModelUtil.findType(NEW_ATTRIBUTE));
		input.tUpdateByName(NAME, REF);
		input.tUpdateByName(TYPE, target);
		input.tUpdateByName(KIND,
			((TLEnumeration) TLModelUtil.findType(REFERENCE_KIND)).getClassifier(kind));
		input.tUpdateByName(OPPOSITE, opposite);
		return input;
	}

	private static Object addGeneralization(TLClass specialization, TLClass generalization) {
		TLObject input =
			TransientObjectFactory.INSTANCE.createObject((TLClass) TLModelUtil.findType(NEW_GENERALIZATION));
		input.tUpdateByName(GENERALIZATION, generalization);
		return execute(Mode.ADD_GENERALIZATION, specialization, input);
	}

	private static TLObject generalizationLink(TLClass specialization, TLClass generalization) {
		TLObject link =
			TransientObjectFactory.INSTANCE.createObject((TLClass) TLModelUtil.findType(GENERALIZATION_LINK));
		link.tUpdateByName(SPECIALIZATION, specialization);
		link.tUpdateByName(GENERALIZATION, generalization);
		return link;
	}

	private static Object execute(Mode mode, Object container, Object input) {
		ConfigurationDescriptor descriptor = TypedConfiguration.getConfigurationDescriptor(ModelEditAction.Config.class);
		ModelEditAction.Config config = TypedConfiguration.newConfigItem(ModelEditAction.Config.class);
		config.update(descriptor.getProperty(ModelEditAction.Config.MODE), mode);
		config.update(descriptor.getProperty(ModelEditAction.Config.CONTAINER), CONTAINER);
		ModelEditAction action = new ModelEditAction(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, config);

		ViewContext context = new DefaultViewContext(null);
		DefaultViewChannel channel = new DefaultViewChannel(CONTAINER);
		context.registerChannel(CONTAINER, channel);
		channel.set(container);
		return action.execute(context, input);
	}

	private interface ModuleTest {
		void run(TLClass owner, TLClass target, TLEnumeration enumeration);
	}

	/**
	 * Runs the given test with two classes and an enumeration of a fresh module in a transaction
	 * that is rolled back afterwards.
	 */
	private static void inTestModule(ModuleTest test) {
		ThreadContext.inSystemContext(TestModelEditAction.class, () -> {
			try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
				TLModule module = TLModelUtil.addModule(ModelService.getApplicationModel(), TEST_MODULE);
				test.run(TLModelUtil.addClass(module, "Owner"), TLModelUtil.addClass(module, "Target"),
					TLModelUtil.addEnumeration(module, "Choice"));
				tx.rollback();
			}
		});
	}

	/**
	 * Test suite starting the model with the developer tools types.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestModelEditAction.class,
			ServiceTestSetup.createStarterFactoryForModules(
				ThreadContextManager.Module.INSTANCE, TypeIndex.Module.INSTANCE,
				CompatibilityService.Module.INSTANCE, AttributeSettings.Module.INSTANCE,
				ModelService.Module.INSTANCE));
	}
}
