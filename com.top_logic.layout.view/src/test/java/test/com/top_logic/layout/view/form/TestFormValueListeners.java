/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.view.form.AttributeFieldControl;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.TLObjectOverlay;
import com.top_logic.layout.view.form.ValueListenerDispatch;
import com.top_logic.model.TLFormObjectBase;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.AttributeValueListener;
import com.top_logic.model.annotate.TLValueListeners;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Tests that a {@link FormControl} calls the {@link AttributeValueListener}s annotated to an
 * attribute (see {@link TLValueListeners}) when its value changes, and that the form follows the
 * values the listeners write.
 *
 * <p>
 * The listeners form a chain: a change of the {@link #STATUS} resets the {@link #BRANCH}, a change
 * of the {@link #BRANCH} resets the {@link #MODULE}, and a change of the {@link #MODULE} reopens
 * the {@link #STATUS}, which closes a cycle. A change of the {@link #TITLE} writes the
 * {@link #TITLE} itself and the {@link #LOG}, which has no field in the form.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestFormValueListeners extends AbstractTicketFormTest {

	private static final String BRANCH = "branch";

	private static final String MODULE = "module";

	private static final String LOG = "log";

	private static final String REOPENED = "reopened";

	private static final String NO_MODULE = "none";

	private static final String FIXED_TITLE = "fixed";

	private static final String LOGGED = "logged";

	/**
	 * The calls of the {@link AssignValue} listeners, in the order they happened.
	 */
	static final List<String> CALLS = new ArrayList<>();

	private AttributeFieldControl _branch;

	private AttributeFieldControl _module;

	@Override
	protected void setUp() throws Exception {
		CALLS.clear();
		super.setUp();
		_branch = field(BRANCH);
		_module = field(MODULE);
	}

	@Override
	protected void tearDown() throws Exception {
		_branch = null;
		_module = null;
		CALLS.clear();
		super.tearDown();
	}

	@Override
	protected void addAttributes(TLModule module, TLPrimitive text) {
		TLModelUtil.addProperty(_ticketType, BRANCH, text);
		TLModelUtil.addProperty(_ticketType, MODULE, text);
		TLModelUtil.addProperty(_ticketType, LOG, text);

		part(STATUS).setAnnotation(listeners(assign(BRANCH, null)));
		part(BRANCH).setAnnotation(listeners(assign(MODULE, NO_MODULE)));
		part(MODULE).setAnnotation(listeners(assign(STATUS, REOPENED)));
		part(TITLE).setAnnotation(listeners(assign(TITLE, FIXED_TITLE), assign(LOG, LOGGED)));
	}

	@Override
	protected IdentifiedObject newTicket(String title, String status) {
		IdentifiedObject result = super.newTicket(title, status);
		result.tUpdateByName(BRANCH, "main");
		result.tUpdateByName(MODULE, "core");
		return result;
	}

	/**
	 * Entering and leaving edit mode calls no listener.
	 */
	public void testLoadAndCancelCallNoListener() {
		_form.enterEditMode();
		assertEquals(List.of(), CALLS);

		_form.executeCancel();
		assertEquals(List.of(), CALLS);
	}

	/**
	 * A change of the user calls the listeners of the attribute, a value written by a listener
	 * calls the listeners of the written attribute, and the fields show the written values as
	 * changes. The cycle back to the edited attribute ends there.
	 */
	public void testUserChangeCascades() {
		_form.enterEditMode();

		model(_status).setValue("closed");

		assertEquals(List.of(
			"status:open->closed=>branch",
			"branch:main->null=>module",
			"module:core->none=>status"), CALLS);
		assertNull("The reset is shown in the field.", model(_branch).getValue());
		assertTrue("The reset is a change to save.", _branch.isDirty());
		assertEquals(NO_MODULE, shown(_module));
		assertTrue(_module.isDirty());
		assertEquals("The field of the edited attribute shows the value written by the cycle.",
			REOPENED, shown(_status));
		assertEquals(REOPENED, _form.getOverlay().tValue(part(STATUS)));
		assertTrue(_form.isDirty());
		assertEquals("Nothing is stored before the save.", "main", _ticket.tValueByName(BRANCH));
	}

	/**
	 * A listener writing the attribute it listens to is not called again for its own write.
	 */
	public void testSelfWrite() {
		_form.enterEditMode();

		model(_title).setValue("Logout fails");

		assertEquals(List.of(
			"title:Login fails->Logout fails=>title",
			"title:Login fails->Logout fails=>log"), CALLS);
		assertEquals(FIXED_TITLE, shown(_title));
		assertEquals(FIXED_TITLE, _form.getOverlay().tValue(part(TITLE)));
		assertEquals(LOGGED, _form.getOverlay().tValue(part(LOG)));
	}

	/**
	 * A value written to an attribute without a field is announced by the form.
	 */
	public void testAttributeWithoutFieldIsAnnounced() {
		_form.enterEditMode();
		List<TLStructuredTypePart> changed = new ArrayList<>();
		_form.addFieldChangeListener(changed::add);

		model(_title).setValue("Logout fails");

		assertTrue(changed.contains(part(LOG)));
		assertTrue(changed.contains(part(TITLE)));
	}

	/**
	 * A value written directly to the overlay is shown by its field and calls its listeners; a
	 * discarded change calls none.
	 */
	public void testOverlayWriteAndRevert() {
		_form.enterEditMode();
		TLObjectOverlay overlay = _form.getOverlay();

		overlay.tUpdate(part(BRANCH), "dev");

		assertEquals(List.of(
			"branch:main->dev=>module",
			"module:core->none=>status",
			"status:open->reopened=>branch"), CALLS);
		assertEquals(NO_MODULE, shown(_module));
		assertTrue(_module.isDirty());
		assertEquals(REOPENED, shown(_status));
		assertTrue(_status.isDirty());
		assertNull("The cycle writes the branch again, without calling its listener again.",
			model(_branch).getValue());
		CALLS.clear();

		overlay.revert(part(MODULE));

		assertEquals("A discarded change calls no listener.", List.of(), CALLS);
		assertEquals("core", shown(_module));
		assertFalse(_module.isDirty());
	}

	/**
	 * Values an overlay receives from an overlay over it that ends its edit - a dialog storing into
	 * the buffer of a row - call no listener: they ran where the values were entered.
	 */
	public void testTransferredValuesCallNoListener() {
		TLObjectOverlay buffer = new TLObjectOverlay(_ticket);
		ValueListenerDispatch dispatch = new ValueListenerDispatch();
		buffer.addValueChangeListener(dispatch::valueChanged);
		TLObjectOverlay dialog = new TLObjectOverlay(buffer);
		dialog.tUpdate(part(STATUS), "closed");

		dialog.applyToBuffer();

		assertEquals(List.of(), CALLS);
		assertEquals("closed", buffer.tValue(part(STATUS)));
		assertFalse(buffer.isReceivingTransfer());

		buffer.tUpdate(part(BRANCH), "dev");

		assertEquals("A direct write calls the listeners.", List.of(
			"branch:main->dev=>module",
			"module:core->none=>status",
			"status:closed->reopened=>branch"), CALLS);
	}

	/**
	 * Storing the changes calls no listener.
	 */
	public void testSaveCallsNoListener() {
		_form.enterEditMode();
		model(_status).setValue("closed");
		CALLS.clear();

		_form.executeSave();

		assertEquals(List.of(), CALLS);
		assertFalse(_form.isEditMode());
		assertEquals(REOPENED, _ticket.tValueByName(STATUS));
		assertNull(_ticket.tValueByName(BRANCH));
		assertEquals(NO_MODULE, _ticket.tValueByName(MODULE));
	}

	private TLStructuredTypePart part(String name) {
		return _ticketType.getPart(name);
	}

	@SafeVarargs
	private static TLValueListeners listeners(PolymorphicConfiguration<? extends AttributeValueListener>... listeners) {
		TLValueListeners result = TypedConfiguration.newConfigItem(TLValueListeners.class);
		result.getListeners().addAll(List.of(listeners));
		return result;
	}

	private static PolymorphicConfiguration<? extends AttributeValueListener> assign(String target, String value) {
		AssignValue.Config config = TypedConfiguration.newConfigItem(AssignValue.Config.class);
		config.setTarget(target);
		config.setValue(value);
		return config;
	}

	/**
	 * {@link AttributeValueListener} writing a fixed value to an attribute of the edited object and
	 * recording its call in {@link TestFormValueListeners#CALLS}.
	 */
	public static class AssignValue extends AbstractConfiguredInstance<AssignValue.Config>
			implements AttributeValueListener {

		/**
		 * Configuration options for {@link AssignValue}.
		 */
		public interface Config extends PolymorphicConfiguration<AssignValue> {

			@Override
			@ClassDefault(AssignValue.class)
			Class<? extends AssignValue> getImplementationClass();

			/**
			 * The name of the attribute to write.
			 */
			@Name("target")
			String getTarget();

			/**
			 * @see #getTarget()
			 */
			void setTarget(String value);

			/**
			 * The value to write.
			 */
			@Name("value")
			@Nullable
			String getValue();

			/**
			 * @see #getValue()
			 */
			void setValue(String value);
		}

		/**
		 * Creates an {@link AssignValue} from configuration.
		 */
		public AssignValue(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public void valueChanged(TLFormObjectBase object, TLStructuredTypePart attribute, Object oldValue,
				Object newValue) {
			String target = getConfig().getTarget();
			CALLS.add(attribute.getName() + ":" + oldValue + "->" + newValue + "=>" + target);
			object.tUpdate(object.tType().getPart(target), getConfig().getValue());
		}
	}

	/**
	 * Test suite requiring a knowledge base for the save, and the {@link FieldControlService}
	 * building the inputs of the fields.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestFormValueListeners.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, ModelService.Module.INSTANCE, FieldControlService.Module.INSTANCE));
	}
}
