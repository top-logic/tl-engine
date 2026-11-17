/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta.form.discover;

import static com.top_logic.model.security.I18NConstants.ERROR_TARGET_NOT_DISCOVERABLE__OBJECT_ATTRIBUTE;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.element.util.ElementWebTestSetup;

import com.top_logic.base.security.device.TLSecurityDeviceManager;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.SimpleEditContext;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.meta.form.AttributeFormFactory;
import com.top_logic.element.meta.form.overlay.TLFormObject;
import com.top_logic.element.meta.gui.DefaultCreateAttributedCommandHandler;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.basic.DummyDisplayContext;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.SelectFieldUtils;
import com.top_logic.layout.form.model.utility.ListOptionModel;
import com.top_logic.layout.form.model.utility.MaskedValues;
import com.top_logic.layout.form.model.utility.OptionModel;
import com.top_logic.layout.provider.MetaResourceProvider;
import com.top_logic.layout.table.provider.generic.TableConfigModelService;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.security.ModelAccessRights;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.execution.ExecutabilityRuleManager;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * Tests that the forms of the classic UI respect the
 * {@link com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup#DISCOVER Discover} right: the
 * options of a reference offer only discoverable objects, a new undiscoverable target is refused
 * on save, and stored undiscoverable targets survive saving.
 *
 * <p>
 * Fixture in the model {@code TestDiscoverForm}: the user owns the holder and the targets
 * {@code own} and {@code ownChild}; the targets {@code hidden}, {@code hiddenStored} and
 * {@code hiddenParent} belong to another person, so the user may not discover them. The holder
 * references {@code [hiddenStored, own]} and, single-valued, {@code hiddenStored}. In the tree of
 * targets, {@code ownChild} is a child of {@code hiddenParent}.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestClassicDiscoverForm extends BasicTestCase {

	private static final String HOLDER = "TestDiscoverForm:Holder";

	private static final String TARGET = "TestDiscoverForm:Target";

	private static final String OWNER = "owner";

	private static final String NAME = "name";

	private static final String TARGETS = "targets";

	private static final String SINGLE_TARGET = "target";

	private static final String GENERATED = "generated";

	private static final String TREE = "tree";

	private final List<TLObject> _created = new ArrayList<>();

	private final List<Person> _createdPersons = new ArrayList<>();

	private Person _user;

	private Person _other;

	private TLObject _holder;

	private TLObject _own;

	private TLObject _ownChild;

	private TLObject _hidden;

	private TLObject _hiddenStored;

	private TLObject _hiddenParent;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_user = createPerson("classicDiscoverUser");
		_other = createPerson("classicDiscoverOther");
		inTransaction(() -> {
			_own = target("own", _user);
			_ownChild = target("ownChild", _user);
			_hidden = target("hidden", _other);
			_hiddenStored = target("hiddenStored", _other);
			_hiddenParent = target("hiddenParent", _other);
			_hiddenParent.tUpdateByName("children", List.of(_ownChild));

			_holder = create(HOLDER);
			_holder.tUpdateByName(OWNER, _user);
			_holder.tUpdateByName(NAME, "holder");
			_holder.tUpdateByName(TARGETS, List.of(_hiddenStored, _own));
			_holder.tUpdateByName(SINGLE_TARGET, _hiddenStored);
		});
	}

	@Override
	protected void tearDown() throws Exception {
		inTransaction(() -> {
			Collections.reverse(_created);
			for (TLObject object : _created) {
				if (object.tValid()) {
					object.tDelete();
				}
			}
			for (Person person : _createdPersons) {
				person.getRepresentativeGroup().tDelete();
				person.tDelete();
			}
		});
		_created.clear();
		_createdPersons.clear();
		super.tearDown();
	}

	public void testFixture() {
		asUser(() -> {
			assertTrue(isDiscoverable(_own));
			assertTrue(isDiscoverable(_holder));
			assertFalse(isDiscoverable(_hidden));
			assertFalse(isDiscoverable(_hiddenStored));
			assertFalse(isDiscoverable(_hiddenParent));
			return null;
		});
	}

	/**
	 * The options of a reference without option generator are the discoverable instances.
	 */
	public void testInstanceOptionsOnlyDiscoverable() {
		List<?> options = asUser(() -> listOptions(TARGETS));
		assertEquals(BasicTestCase.<Object> set(_own, _ownChild), new HashSet<>(options));
	}

	/**
	 * The options of a Java option generator, which does not check access itself, are restricted
	 * to the discoverable objects.
	 */
	public void testGeneratorOptionsOnlyDiscoverable() {
		List<?> options = asUser(() -> listOptions(GENERATED));
		assertEquals(BasicTestCase.<Object> set(_own, _ownChild), new HashSet<>(options));
	}

	/**
	 * A tree of options hides every undiscoverable node with its subtree, like a navigation tree.
	 */
	public void testTreeOptionsHideUndiscoverableNodes() {
		List<Object> options = asUser(() -> {
			List<Object> result = new ArrayList<>();
			for (Object option : AttributeOperations.allOptions(SimpleEditContext.createContext(_holder, part(TREE)))) {
				result.add(option);
			}
			return result;
		});
		assertEquals(List.of(_own), options);
	}

	/**
	 * Options adjusted to an attribute (e.g. those of a configured option provider) are restricted
	 * to the discoverable objects.
	 */
	public void testAdjustedOptionsOnlyDiscoverable() {
		List<?> options = asUser(() -> AttributeOperations.adjustOptions(
			SimpleEditContext.createContext(_holder, part(TARGETS)),
			List.of(_own, _hidden, _hiddenStored, "text")));
		assertEquals(List.of(_own, "text"), options);
	}

	/**
	 * An administrator sees all instances.
	 */
	public void testAdminSeesAllOptions() {
		List<?> options = inPersonContext(PersonManager.getManager().getRoot(), () -> listOptions(TARGETS));
		assertEquals(BasicTestCase.<Object> set(_own, _ownChild, _hidden, _hiddenStored, _hiddenParent), new HashSet<>(options));
	}

	/**
	 * Saving a form that adds an undiscoverable target to a reference is refused.
	 */
	public void testSaveNewUndiscoverableTargetDenied() {
		asUser(() -> {
			AttributeFormContext form = form();
			TLFormObject overlay = form.editObject(_holder);
			FormField targets = editField(form, overlay, TARGETS);
			targets.setValue(List.of(_hiddenStored, _own, _hidden));
			assertRefusedOnApply(form, targets);
			return null;
		});
		assertEquals(List.of(_hiddenStored, _own), _holder.tValueByName(TARGETS));
	}

	/**
	 * Setting a single-valued reference to an undiscoverable target is refused.
	 */
	public void testSaveNewUndiscoverableSingleTargetDenied() {
		asUser(() -> {
			AttributeFormContext form = form();
			TLFormObject overlay = form.editObject(_holder);
			FormField target = editField(form, overlay, SINGLE_TARGET);
			target.setValue(List.of(_hidden));
			assertRefusedOnApply(form, target);
			return null;
		});
		assertSame(_hiddenStored, _holder.tValueByName(SINGLE_TARGET));
	}

	/**
	 * Undiscoverable targets the references already hold survive saving a change of another
	 * attribute, also when their fields are part of the form.
	 */
	public void testStoredUndiscoverableKeptOnOtherChange() {
		asUser(() -> {
			AttributeFormContext form = form();
			TLFormObject overlay = form.editObject(_holder);
			FormField name = editField(form, overlay, NAME);
			FormField targets = editField(form, overlay, TARGETS);
			FormField target = editField(form, overlay, SINGLE_TARGET);
			assertEquals(List.of(_hiddenStored, _own), targets.getValue());
			assertEquals(List.of(_hiddenStored), target.getValue());
			name.setValue("changed");
			apply(form);
			return null;
		});
		assertEquals("changed", _holder.tValueByName(NAME));
		assertEquals(List.of(_hiddenStored, _own), _holder.tValueByName(TARGETS));
		assertSame(_hiddenStored, _holder.tValueByName(SINGLE_TARGET));
	}

	/**
	 * Adding a discoverable target keeps the stored undiscoverable one.
	 */
	public void testAddDiscoverableKeepsStoredUndiscoverable() {
		asUser(() -> {
			AttributeFormContext form = form();
			TLFormObject overlay = form.editObject(_holder);
			FormField targets = editField(form, overlay, TARGETS);
			targets.setValue(List.of(_hiddenStored, _own, _ownChild));
			apply(form);
			return null;
		});
		assertEquals(List.of(_hiddenStored, _own, _ownChild), _holder.tValueByName(TARGETS));
	}

	/**
	 * Creating an object with an undiscoverable target is refused, a discoverable target is
	 * accepted.
	 */
	public void testCreateChecksTargets() {
		asUser(() -> {
			AttributeFormContext form = form();
			TLFormObject overlay = form.createObject(type(HOLDER), null);
			FormField targets = createField(form, overlay, TARGETS);
			targets.setValue(List.of(_own, _hidden));
			assertRefusedOnApply(form, targets);
			return null;
		});

		TLObject created = asUser(() -> {
			AttributeFormContext form = form();
			TLFormObject overlay = form.createObject(type(HOLDER), null);
			FormField targets = createField(form, overlay, TARGETS);
			targets.setValue(List.of(_own));
			apply(form);
			return overlay.getEditedObject();
		});
		_created.add(created);
		assertEquals(List.of(_own), created.tValueByName(TARGETS));
	}

	/**
	 * The create handler writing the values of the form directly checks the new targets as well.
	 */
	public void testLegacyCreateHandlerChecksTargets() throws Exception {
		LegacyCreateHandler handler = new LegacyCreateHandler(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
			AbstractCommandHandler.createConfig(LegacyCreateHandler.class, "legacyCreate"));
		asUser(() -> {
			assertDenied(() -> inTransactionAsCurrentUser(() -> {
				TLObject holder = create(HOLDER);
				handler.save(Map.of(TARGETS, List.of(_hidden)), holder);
			}));
			inTransactionAsCurrentUser(() -> {
				TLObject holder = create(HOLDER);
				handler.save(Map.of(TARGETS, List.of(_own)), holder);
				assertEquals(List.of(_own), holder.tValueByName(TARGETS));
			});
			return null;
		});
	}

	/**
	 * A reference field shows an undiscoverable element by the placeholder, without tooltip, image
	 * and link, and a discoverable element by its label.
	 */
	public void testFieldMasksUndiscoverableElement() {
		asUser(() -> {
			AttributeFormContext form = form();
			SelectField targets = (SelectField) editField(form, form.editObject(_holder), TARGETS);
			assertTrue(SelectFieldUtils.isMaskUndiscoverable(targets));
			ResourceProvider labels = (ResourceProvider) SelectFieldUtils.getOptionLabelProvider(targets);
			assertEquals(MaskedValues.placeholder(), labels.getLabel(_hiddenStored));
			assertNull(labels.getTooltip(_hiddenStored));
			assertNull(labels.getImage(_hiddenStored, Flavor.DEFAULT));
			assertNull(labels.getLink(DummyDisplayContext.newInstance(), _hiddenStored));
			assertNull(labels.getCssClass(_hiddenStored));
			assertEquals(MetaResourceProvider.INSTANCE.getLabel(_own), labels.getLabel(_own));
			assertFalse(SelectFieldUtils.getOptionContextMenu(targets).hasContextMenu(_hiddenStored));
			return null;
		});
	}

	/**
	 * The read-only display of a field shows the placeholder instead of the label of an
	 * undiscoverable element.
	 */
	public void testReadOnlyTextMasked() {
		String text = asUser(() -> {
			AttributeFormContext form = form();
			FormField targets = editField(form, form.editObject(_holder), TARGETS);
			StringBuilder out = new StringBuilder();
			try {
				SelectFieldUtils.writeSelectionAsTextImmutable(out, targets);
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
			return out.toString();
		});
		assertTrue(text, text.contains(MaskedValues.placeholder()));
		assertFalse(text, text.contains("hiddenStored"));
		assertTrue(text, text.contains(MetaResourceProvider.INSTANCE.getLabel(_own)));
	}

	/**
	 * In an order by label, undiscoverable elements come after all others, so that their position
	 * does not disclose their label.
	 */
	public void testMaskedSortedLast() {
		asUser(() -> {
			AttributeFormContext form = form();
			FormField generated = editField(form, form.editObject(_holder), GENERATED);
			List<Object> values = new ArrayList<>(List.of(_hidden, _own, _hiddenStored, _ownChild));
			Collections.sort(values, SelectFieldUtils.getOptionComparator(generated));
			assertEquals(set(_own, _ownChild), new HashSet<>(values.subList(0, 2)));
			assertEquals(set(_hidden, _hiddenStored), new HashSet<>(values.subList(2, 4)));
			return null;
		});
	}

	/**
	 * Writing the selection of a field as text and parsing it back keeps all undiscoverable
	 * elements, although they are all shown by the same placeholder.
	 */
	public void testTextRoundTripKeepsMaskedElements() {
		inTransaction(() -> _holder.tUpdateByName(TARGETS, List.of(_hiddenStored, _own, _hidden)));
		asUser(() -> {
			AttributeFormContext form = form();
			FormField targets = editField(form, form.editObject(_holder), TARGETS);
			StringBuilder text = new StringBuilder();
			try {
				SelectFieldUtils.writeSelectionAsTextEditable(text, targets);
				targets.update(text.toString());
			} catch (IOException | VetoException ex) {
				throw new RuntimeException(ex);
			}
			assertFalse(targets.hasError() ? targets.getError() : null, targets.hasError());
			List<?> value = (List<?>) targets.getValue();
			assertEquals(3, value.size());
			assertEquals(set(_hiddenStored, _own, _hidden), new HashSet<>(value));
			return null;
		});
	}

	/**
	 * Exposes the storage of the values of {@link DefaultCreateAttributedCommandHandler}.
	 */
	public static class LegacyCreateHandler extends DefaultCreateAttributedCommandHandler {

		public LegacyCreateHandler(InstantiationContext context, Config config) throws ConfigurationException {
			super(context, config);
		}

		void save(Map<String, Object> values, TLObject object) {
			saveMetaAttributes(values, object);
		}

	}

	private List<?> listOptions(String attribute) {
		OptionModel<?> options = AttributeOperations.allOptions(SimpleEditContext.createContext(_holder, part(attribute)));
		return new ArrayList<>(((ListOptionModel<?>) options).getBaseModel());
	}

	private static boolean isDiscoverable(TLObject object) {
		return ModelAccessRights.getInstance().isDiscoverAllowed(object);
	}

	private static AttributeFormContext form() {
		return new AttributeFormContext(ResPrefix.forTest("TestClassicDiscoverForm"));
	}

	private static FormField editField(AttributeFormContext form, TLFormObject overlay, String attribute) {
		return field(form, overlay.newEditUpdateDefault(part(attribute), false));
	}

	private static FormField createField(AttributeFormContext form, TLFormObject overlay, String attribute) {
		return field(form, overlay.newCreateUpdate(part(attribute)));
	}

	private static FormField field(AttributeFormContext form, AttributeUpdate update) {
		return (FormField) AttributeFormFactory.getInstance().toFormMember(update, form.getAttributeUpdateContainer());
	}

	/**
	 * Applies the form like the apply command of the classic UI: checks all fields, transferring
	 * their values to the updates, and stores the updates.
	 */
	private static void apply(AttributeFormContext form) {
		assertTrue("The form is valid.", form.checkAll());
		storeInTransaction(form);
	}

	/**
	 * Expects that the check of the form refuses the value of the given field, and that storing the
	 * form without the check is refused as well.
	 */
	private static void assertRefusedOnApply(AttributeFormContext form, FormField field) {
		assertFalse("The check refuses the undiscoverable target.", form.checkAll());
		assertTrue("The field shows the refusal.", field.hasError());
		assertDenied(() -> storeInTransaction(form));
	}

	private static void storeInTransaction(AttributeFormContext form) {
		inTransactionAsCurrentUser(form::store);
	}

	private static void inTransactionAsCurrentUser(Runnable modification) {
		try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
			modification.run();
			tx.commit();
		}
	}

	private static void assertDenied(Runnable action) {
		try {
			action.run();
		} catch (RuntimeException ex) {
			for (Throwable cause = ex; cause != null; cause = cause.getCause()) {
				if (cause instanceof TopLogicException tlEx
					&& ERROR_TARGET_NOT_DISCOVERABLE__OBJECT_ATTRIBUTE.getKey().equals(tlEx.getErrorKey().getKey())) {
					return;
				}
			}
			throw new AssertionError("Expected the refusal of an undiscoverable target, got: " + ex, ex);
		}
		fail("An undiscoverable new target must be refused.");
	}

	private <T> T asUser(Supplier<T> job) {
		return inPersonContext(_user, job);
	}

	private static <T> T inPersonContext(Person person, Supplier<T> job) {
		return TLContextManager.<T, RuntimeException, RuntimeException> inPersonContext(person, job::get);
	}

	private TLObject target(String name, Person owner) {
		TLObject result = create(TARGET);
		result.tUpdateByName(NAME, name);
		result.tUpdateByName(OWNER, owner);
		return result;
	}

	private TLObject create(String qualifiedTypeName) {
		TLObject result = ModelService.getInstance().getFactory().createObject(type(qualifiedTypeName));
		_created.add(result);
		return result;
	}

	private Person createPerson(String name) {
		Person[] result = new Person[1];
		inTransaction(() -> {
			result[0] =
				Person.create(kb(), name, TLSecurityDeviceManager.getInstance().getAuthenticationDevice("dbSecurity"));
		});
		_createdPersons.add(result[0]);
		return result[0];
	}

	private static TLStructuredTypePart part(String attribute) {
		return type(HOLDER).getPart(attribute);
	}

	private static TLClass type(String qualifiedTypeName) {
		return (TLClass) TLModelUtil.findType(qualifiedTypeName);
	}

	private static KnowledgeBase kb() {
		return PersistencyLayer.getKnowledgeBase();
	}

	private static void inTransaction(Runnable modification) {
		ThreadContext.withoutSecurityChecks(() -> {
			try (Transaction tx = kb().beginTransaction(com.top_logic.knowledge.service.I18NConstants.NO_COMMIT_MESSAGE)) {
				modification.run();
				tx.commit();
			}
		});
	}

	/** Return the suite of tests to perform. */
	public static Test suite() {
		Test suite = ServiceTestSetup.createSetup(new TestSuite(TestClassicDiscoverForm.class),
			TLSecurityDeviceManager.Module.INSTANCE, PersonManager.Module.INSTANCE,
			TableConfigModelService.Module.INSTANCE, ExecutabilityRuleManager.Module.INSTANCE);
		return ElementWebTestSetup.createElementWebTestSetup(suite);
	}

}
