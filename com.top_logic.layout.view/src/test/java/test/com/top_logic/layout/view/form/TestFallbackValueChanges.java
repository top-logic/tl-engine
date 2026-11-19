/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.layout.view.security.AbstractModelAccessTest;

import com.top_logic.base.locking.handler.NoTokenHandling;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.element.config.annotation.TLStorage;
import com.top_logic.element.meta.AttributeWithFallbackStorage;
import com.top_logic.element.meta.StorageImplementation;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.FieldState;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.form.AttributeFieldControl;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.layout.view.form.TLObjectOverlay;
import com.top_logic.layout.view.form.ValueListenerDispatch;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassProperty;
import com.top_logic.model.TLFormObjectBase;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLPrimitive.Kind;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.AttributeValueListener;
import com.top_logic.model.annotate.TLValueListeners;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.util.TLModelUtil;

/**
 * Tests that a {@link TLObjectOverlay} reports a change of an attribute with an
 * {@link AttributeWithFallbackStorage} like a change of any other attribute: as value change event
 * of the attribute, as {@link TLObjectOverlay#isChanged(TLStructuredTypePart) changed} attribute,
 * to the {@link AttributeValueListener}s of the attribute, and to its field.
 *
 * <p>
 * The schedule has a {@link #FORECAST} whose explicit value is kept in {@link #FORECAST_STORED}
 * and whose fallback is the {@link #PLAN}. A change of the {@link #PLAN} resets the
 * {@link #FORECAST}. The {@link #NOTE} is an attribute without fallback. Every change of one of
 * these attributes is recorded in {@link #CALLS} by a {@link RecordValue} listener.
 * </p>
 *
 * <p>
 * Each case runs for a schedule of a transient type and of a persistent type
 * (<code>TestViewFallbackStorage.model.xml</code>). The field of a form follows the change for the
 * persistent type here, for a transient type in {@link TestFallbackPlaceholder}: entering the edit
 * mode of a form checks the command approval, which needs a persistent type in this test setup.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestFallbackValueChanges extends AbstractModelAccessTest {

	/** The attribute with the fallback. */
	private static final String FORECAST = "forecast";

	/** The attribute storing the explicit value of the {@link #FORECAST}. */
	private static final String FORECAST_STORED = "forecastStored";

	/** The attribute providing the fallback of the {@link #FORECAST}. */
	private static final String PLAN = "plan";

	/** An attribute without fallback. */
	private static final String NOTE = "note";

	/** The persistent schedule type. */
	private static final String SCHEDULE = "TestViewFallbackStorage:Schedule";

	/**
	 * The calls of the {@link RecordValue} listeners, in the order they happened.
	 */
	static final List<String> CALLS = new ArrayList<>();

	/**
	 * The value change events of the overlay under test, in the order they happened.
	 */
	private final List<String> _events = new ArrayList<>();

	private ReactContext _reactContext;

	private TLClass _transientType;

	@Override
	protected void setUp() throws Exception {
		CALLS.clear();
		super.setUp();
		_reactContext = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		_transientType = createTransientType();
	}

	@Override
	protected void tearDown() throws Exception {
		_reactContext = null;
		_transientType = null;
		_events.clear();
		CALLS.clear();
		super.tearDown();
	}

	/**
	 * The tests are about the reported changes, not about the access rights.
	 */
	@Override
	protected boolean checksSecurity() {
		return false;
	}

	/** (a) An entry into the attribute with fallback is a change of the attribute. */
	public void testInputTransient() {
		checkInput(newTransient("p1", null));
	}

	/** (a) An entry into the attribute with fallback is a change of the attribute. */
	public void testInputPersistent() {
		checkInput(newPersistent("p1", null));
	}

	private void checkInput(TLObject schedule) {
		TLObjectOverlay overlay = observedOverlay(schedule);

		overlay.tUpdate(part(overlay, FORECAST), "f1");

		assertEquals(List.of("forecastStored:null->f1", "forecast:p1->f1"), _events);
		assertEquals(List.of("forecast:p1->f1"), CALLS);
		assertTrue(overlay.isChanged(part(overlay, FORECAST)));
		assertTrue(overlay.isChanged(part(overlay, FORECAST_STORED)));
		assertNull("Nothing is stored.", schedule.tValueByName(FORECAST_STORED));
	}

	/** (b) Resetting the attribute with fallback is a change to the fallback value. */
	public void testResetTransient() {
		checkReset(newTransient("p1", "own"));
	}

	/** (b) Resetting the attribute with fallback is a change to the fallback value. */
	public void testResetPersistent() {
		checkReset(newPersistent("p1", "own"));
	}

	private void checkReset(TLObject schedule) {
		TLObjectOverlay overlay = observedOverlay(schedule);

		overlay.tUpdate(part(overlay, FORECAST), null);

		assertEquals(List.of("forecastStored:own->null", "forecast:own->p1"), _events);
		assertEquals("The listener receives the fallback as new value.", List.of("forecast:own->p1"), CALLS);
		assertTrue(overlay.isChanged(part(overlay, FORECAST)));
		assertEquals("p1", overlay.tValue(part(overlay, FORECAST)));
	}

	/**
	 * (c) A listener of another attribute resetting the attribute with fallback changes the
	 * attribute, which its field shows.
	 */
	public void testListenerResetShownPersistent() {
		checkListenerResetShown(newPersistent("p1", "own"));
	}

	private void checkListenerResetShown(TLObject schedule) {
		FormControl form = new FormControl(_reactContext, schedule, "no model", NoTokenHandling.INSTANCE);
		form.setModelScope(new RecordingScope());
		AttributeFieldControl plan = field(form, PLAN);
		AttributeFieldControl forecast = field(form, FORECAST);
		form.attach();
		assertTrue(form.enterEditMode());
		assertEquals("own", shown(forecast));
		TLObjectOverlay overlay = form.getOverlay();
		overlay.addValueChangeListener(this::record);

		model(plan).setValue("p2");

		assertEquals(List.of("plan:p1->p2", "forecast:own->p2"), CALLS);
		assertTrue(_events.contains("forecast:own->p2"));
		assertTrue(overlay.isChanged(part(overlay, FORECAST)));
		assertNull("The field shows the reset immediately.", shown(forecast));
		assertEquals("p2", placeholder(forecast));
		assertTrue(forecast.isDirty());
		assertEquals("Nothing is stored before the save.", "own", schedule.tValueByName(FORECAST_STORED));
	}

	/** (d) Writing the storage attribute directly is a change of the attribute with fallback. */
	public void testStorageWriteTransient() {
		checkStorageWrite(newTransient("p1", null));
	}

	/** (d) Writing the storage attribute directly is a change of the attribute with fallback. */
	public void testStorageWritePersistent() {
		checkStorageWrite(newPersistent("p1", null));
	}

	private void checkStorageWrite(TLObject schedule) {
		TLObjectOverlay overlay = observedOverlay(schedule);

		overlay.tUpdate(part(overlay, FORECAST_STORED), "f1");

		assertEquals(List.of("forecastStored:null->f1", "forecast:p1->f1"), _events);
		assertEquals(List.of("forecast:p1->f1"), CALLS);
		assertTrue(overlay.isChanged(part(overlay, FORECAST)));
		_events.clear();
		CALLS.clear();

		overlay.revert(part(overlay, FORECAST));

		assertEquals("Discarding the change of the attribute discards the change of the storage attribute.",
			List.of("forecastStored:f1->null", "forecast:f1->p1"), _events);
		assertEquals("A discarded change calls no listener.", List.of(), CALLS);
		assertFalse(overlay.isChanged(part(overlay, FORECAST)));
		assertFalse(overlay.isChanged(part(overlay, FORECAST_STORED)));
	}

	/** (e) An attribute without fallback is reported as before. */
	public void testWithoutFallbackTransient() {
		checkWithoutFallback(newTransient("p1", null));
	}

	/** (e) An attribute without fallback is reported as before. */
	public void testWithoutFallbackPersistent() {
		checkWithoutFallback(newPersistent("p1", null));
	}

	private void checkWithoutFallback(TLObject schedule) {
		TLObjectOverlay overlay = observedOverlay(schedule);

		overlay.tUpdate(part(overlay, NOTE), "n1");

		assertEquals(List.of("note:null->n1"), _events);
		assertEquals(List.of("note:null->n1"), CALLS);
		assertTrue(overlay.isChanged(part(overlay, NOTE)));
		assertFalse(overlay.isChanged(part(overlay, FORECAST)));
		_events.clear();
		CALLS.clear();

		overlay.revert(part(overlay, NOTE));

		assertEquals(List.of("note:n1->null"), _events);
		assertEquals(List.of(), CALLS);
		assertFalse(overlay.isChanged(part(overlay, NOTE)));
	}

	/**
	 * An overlay over the given schedule whose value changes are recorded in {@link #_events} and
	 * dispatched to the {@link AttributeValueListener}s.
	 */
	private TLObjectOverlay observedOverlay(TLObject schedule) {
		TLObjectOverlay overlay = new TLObjectOverlay(schedule);
		overlay.addValueChangeListener(this::record);
		overlay.addValueChangeListener(new ValueListenerDispatch()::valueChanged);
		return overlay;
	}

	private void record(TLObjectOverlay overlay, TLStructuredTypePart part, Object oldValue, Object newValue) {
		_events.add(part.getName() + ":" + oldValue + "->" + newValue);
	}

	private TLObject newTransient(String plan, String explicitForecast) {
		IdentifiedObject result = new IdentifiedObject(_transientType);
		result.tUpdateByName(PLAN, plan);
		result.tUpdateByName(FORECAST_STORED, explicitForecast);
		return result;
	}

	private TLObject newPersistent(String plan, String explicitForecast) {
		TLClass type = (TLClass) TLModelUtil.findType(model(), SCHEDULE);
		try (Transaction tx = kb().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			TLObject result = DynamicModelService.getFactoryFor(type.getModule().getName()).createObject(type);
			result.tUpdateByName(PLAN, plan);
			result.tUpdateByName(FORECAST_STORED, explicitForecast);
			tx.commit();
			return result;
		}
	}

	/**
	 * A transient schedule type, built like the persistent one.
	 */
	private static TLClass createTransientType() throws Exception {
		TLModelImpl model = new TLModelImpl();
		TLModule module = TLModelUtil.addModule(model, "test.TestFallbackValueChanges");
		TLPrimitive text = TLModelUtil.addDatatype(module, module, "Text", Kind.STRING,
			AbstractTicketFormTest.directMapping(String.class));
		TLClass type = TLModelUtil.addClass(module, "Schedule");
		TLModelUtil.addProperty(type, PLAN, text).setAnnotation(listeners(record(FORECAST)));
		TLModelUtil.addProperty(type, FORECAST_STORED, text);
		TLClassProperty forecast = TLModelUtil.addProperty(type, FORECAST, text);
		forecast.setAnnotation(fallbackStorage(FORECAST_STORED, PLAN));
		forecast.setAnnotation(listeners(record(null)));
		// A part of a transient model does not initialize its storage, which the fallback storage
		// needs to resolve the attributes it reads.
		((StorageImplementation) forecast.getStorageImplementation()).init(forecast);
		TLModelUtil.addProperty(type, NOTE, text).setAnnotation(listeners(record(null)));
		return type;
	}

	private static TLStructuredTypePart part(TLObject object, String name) {
		return object.tType().getPartOrFail(name);
	}

	private AttributeFieldControl field(FormControl form, String attribute) {
		AttributeFieldControl result =
			new AttributeFieldControl(_reactContext, form, form, attribute, null, false, null);
		result.createChromeControl();
		return result;
	}

	private static AbstractFieldModel model(AttributeFieldControl field) {
		return (AbstractFieldModel) ((ReactFormFieldControl) field.getInnerControl()).getFieldModel();
	}

	private static Object shown(AttributeFieldControl field) {
		return field.getInnerControl().scriptingScalarState().get(FieldState.VALUE__PROP);
	}

	private static Object placeholder(AttributeFieldControl field) {
		return field.getInnerControl().scriptingScalarState().get(FieldState.PLACEHOLDER__PROP);
	}

	private static TLStorage fallbackStorage(String storageAttribute, String fallbackAttribute) {
		AttributeWithFallbackStorage.Config<?> storageConfig =
			TypedConfiguration.newConfigItem(AttributeWithFallbackStorage.Config.class);
		storageConfig.update(
			storageConfig.descriptor().getProperty(AttributeWithFallbackStorage.Config.STORAGE_ATTRIBUTE),
			storageAttribute);
		storageConfig.update(
			storageConfig.descriptor().getProperty(AttributeWithFallbackStorage.Config.FALLBACK_ATTRIBUTE),
			fallbackAttribute);

		TLStorage annotation = TypedConfiguration.newConfigItem(TLStorage.class);
		annotation.setImplementation(storageConfig);
		return annotation;
	}

	private static TLValueListeners listeners(PolymorphicConfiguration<? extends AttributeValueListener> listener) {
		TLValueListeners result = TypedConfiguration.newConfigItem(TLValueListeners.class);
		result.getListeners().add(listener);
		return result;
	}

	private static PolymorphicConfiguration<? extends AttributeValueListener> record(String target) {
		RecordValue.Config config = TypedConfiguration.newConfigItem(RecordValue.Config.class);
		config.setTarget(target);
		return config;
	}

	/**
	 * {@link AttributeValueListener} recording its call in {@link TestFallbackValueChanges#CALLS}
	 * and resetting the {@link Config#getTarget() target attribute}, if one is given.
	 */
	public static class RecordValue extends AbstractConfiguredInstance<RecordValue.Config>
			implements AttributeValueListener {

		/**
		 * Configuration options for {@link RecordValue}.
		 */
		public interface Config extends PolymorphicConfiguration<RecordValue> {

			@Override
			@ClassDefault(RecordValue.class)
			Class<? extends RecordValue> getImplementationClass();

			/**
			 * The name of the attribute to reset, <code>null</code> for none.
			 */
			@Name("target")
			@Nullable
			String getTarget();

			/**
			 * @see #getTarget()
			 */
			void setTarget(String value);
		}

		/**
		 * Creates a {@link RecordValue} from configuration.
		 */
		public RecordValue(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public void valueChanged(TLFormObjectBase object, TLStructuredTypePart attribute, Object oldValue,
				Object newValue) {
			CALLS.add(attribute.getName() + ":" + oldValue + "->" + newValue);
			String target = getConfig().getTarget();
			if (target != null) {
				object.tUpdate(object.tType().getPartOrFail(target), null);
			}
		}
	}

	/**
	 * The test suite, starting the services building the fields.
	 */
	public static Test suite() {
		return suiteWith(TestFallbackValueChanges.class, TypeIndex.Module.INSTANCE,
			ThemeFactory.Module.INSTANCE, ResourcesModule.Module.INSTANCE, FieldControlService.Module.INSTANCE,
			LabelProviderService.Module.INSTANCE, SearchBuilder.Module.INSTANCE);
	}

}
