/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.LongID;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.json.JSON;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOClassImpl;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactValueColor;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.element.TextElement;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.annotate.ui.AnnotationValueColorProvider;
import com.top_logic.model.annotate.ui.TLColor;
import com.top_logic.model.annotate.ui.TLDynamicColor;
import com.top_logic.model.annotate.ui.ValueColor;
import com.top_logic.model.annotate.ui.ValueColorProvider;
import com.top_logic.model.annotate.util.AttributeSettings;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.impl.TransientTLObjectImpl;
import com.top_logic.model.listen.ModelChangeEvent;
import com.top_logic.model.listen.ModelListener;
import com.top_logic.model.listen.ModelScope;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.CompatibilityService;

/**
 * Tests that a {@code <text>} over a channel holding an object follows that object: the pill of a
 * ticket whose status was stored is drawn in the color of the new status, although the channel keeps
 * pointing to the same ticket.
 *
 * <p>
 * The object is displayed by a type whose {@link TLDynamicColor} annotation takes the color from an
 * attribute, and the change is delivered by the {@link ModelScope} of the view context: what the
 * control shows here is therefore what it shows in an application session, where the scope is the
 * one of the browser window.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestTextElementObservation extends BasicTestCase {

	/** Name of the attribute the color of a ticket is taken from. */
	static final String STATUS_ATTRIBUTE = "status";

	/** Design token coloring an open ticket. */
	private static final String OPEN_TOKEN = "support-warning";

	/** Design token coloring a closed ticket. */
	private static final String CLOSED_TOKEN = "support-success";

	/** Name of the channel the displayed ticket is held on. */
	private static final String TICKET_CHANNEL = "ticket";

	private TLEnumeration _status;

	private TLClassifier _open;

	private TLClassifier _closed;

	private TLClass _ticketType;

	private ViewChannel _ticket;

	private RecordingScope _scope;

	private ViewContext _context;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		TLModelImpl model = new TLModelImpl();
		model.addCoreModule();
		TLModule module = TLModelUtil.addModule(model, "test");

		_status = TLModelUtil.addEnumeration(module, "Status");
		_open = TLModelUtil.addClassifier(_status, "open");
		_open.setAnnotation(tokenColor(OPEN_TOKEN));
		_closed = TLModelUtil.addClassifier(_status, "closed");
		_closed.setAnnotation(tokenColor(CLOSED_TOKEN));

		_ticketType = TLModelUtil.addClass(module, "Ticket");
		TLModelUtil.addProperty(_ticketType, STATUS_ATTRIBUTE, _status);
		_ticketType.setAnnotation(dynamicColor());

		_scope = new RecordingScope();
		_ticket = new DefaultViewChannel(TICKET_CHANNEL);
		_context = new DefaultViewContext(new ObservingReactContext(_scope));
		_context.registerChannel(TICKET_CHANNEL, _ticket);
	}

	@Override
	protected void tearDown() throws Exception {
		_context = null;
		_ticket = null;
		_scope = null;

		super.tearDown();
	}

	/**
	 * Tests that storing the attribute the color is taken from recolors the pill, although the
	 * channel keeps holding the same object.
	 */
	public void testObjectChangeRecomputesTheDisplay() {
		TLObject ticket = ticket(_open);
		_ticket.set(ticket);
		ReactTextControl control = attachedControl();

		assertEquals("The pill is drawn in the color of the status the ticket has.",
			color(OPEN_TOKEN), displayedColor(control));

		ticket.tUpdateByName(STATUS_ATTRIBUTE, _closed);
		_scope.reportUpdate(ticket);

		assertEquals("The pill follows the status stored on the displayed ticket.",
			color(CLOSED_TOKEN), displayedColor(control));
	}

	/**
	 * Tests that a detached control is not updated: it is off screen, so what it would show is
	 * nobody's concern until it is displayed again.
	 */
	public void testDetachedControlIsNotUpdated() {
		TLObject ticket = ticket(_open);
		_ticket.set(ticket);
		ReactTextControl control = attachedControl();
		control.detach();

		ticket.tUpdateByName(STATUS_ATTRIBUTE, _closed);
		_scope.reportUpdate(ticket);

		assertEquals("A detached display keeps the color it was left with.",
			color(OPEN_TOKEN), displayedColor(control));
	}

	/**
	 * Tests that a change of an object the channel never held leaves the display alone.
	 */
	public void testUnrelatedObjectChangeDoesNotRecompute() {
		TLObject ticket = ticket(_open);
		TLObject other = ticket(_open);
		_ticket.set(ticket);
		ReactTextControl control = attachedControl();

		other.tUpdateByName(STATUS_ATTRIBUTE, _closed);
		_scope.reportUpdate(other);

		assertEquals("An object nobody displays does not reach the display.",
			color(OPEN_TOKEN), displayedColor(control));
	}

	/**
	 * Tests that the observation follows the channel value: the object dropped from the channel no
	 * longer recomputes the display, the one taken up does.
	 */
	public void testObservationFollowsTheChannelValue() {
		TLObject ticket1 = ticket(_open);
		TLObject ticket2 = ticket(_open);
		_ticket.set(ticket1);
		ReactTextControl control = attachedControl();

		_ticket.set(ticket2);

		ticket1.tUpdateByName(STATUS_ATTRIBUTE, _closed);
		_scope.reportUpdate(ticket1);
		assertEquals("The object dropped from the channel is not observed any more.",
			color(OPEN_TOKEN), displayedColor(control));

		ticket2.tUpdateByName(STATUS_ATTRIBUTE, _closed);
		_scope.reportUpdate(ticket2);
		assertEquals("The object now on the channel is observed.",
			color(CLOSED_TOKEN), displayedColor(control));
	}

	/**
	 * Tests that a new channel value is displayed with the color of the object it names.
	 */
	public void testNewChannelValueIsDisplayed() {
		_ticket.set(ticket(_open));
		ReactTextControl control = attachedControl();

		_ticket.set(ticket(_closed));

		assertEquals(color(CLOSED_TOKEN), displayedColor(control));
	}

	/**
	 * The control of a {@code <text>} over {@link #TICKET_CHANNEL}, attached as a displayed control
	 * is.
	 */
	private ReactTextControl attachedControl() {
		TextElement.Config config = TypedConfiguration.newConfigItem(TextElement.Config.class);
		config.update(config.descriptor().getProperty(TextElement.Config.INPUT), new ChannelRef(TICKET_CHANNEL));

		IReactControl control = new TextElement(null, config).createControl(_context);
		ReactTextControl result = (ReactTextControl) control;
		result.attach();
		return result;
	}

	/**
	 * The color the given control currently displays its text with, as the client receives it.
	 */
	private static String displayedColor(ReactTextControl control) {
		Object state;
		try {
			state = JSON.fromString(control.stateAsJSON());
		} catch (JSON.ParseException ex) {
			throw new AssertionError("Not the JSON state of a control: " + control.stateAsJSON(), ex);
		}
		return (String) ((Map<?, ?>) state).get(ReactValueColor.COLOR);
	}

	/**
	 * The CSS of the color the given design token names.
	 */
	private static String color(String token) {
		return ValueColor.themeToken(token).cssValue();
	}

	/**
	 * A ticket with the given status, identified so that it can be observed.
	 */
	private TLObject ticket(TLClassifier status) {
		IdentifiedObject result = new IdentifiedObject(_ticketType);
		result.tUpdateByName(STATUS_ATTRIBUTE, status);
		return result;
	}

	private static TLColor tokenColor(String token) {
		TLColor result = TypedConfiguration.newConfigItem(TLColor.class);
		result.setToken(token);
		return result;
	}

	private static TLDynamicColor dynamicColor() throws ConfigurationException {
		TLDynamicColor result = TypedConfiguration.newConfigItem(TLDynamicColor.class);
		result.setColorProvider(TypedConfiguration.createConfigItemForImplementationClass(StatusColor.class));
		return result;
	}

	/**
	 * {@link ValueColorProvider} taking the color of a ticket from its status.
	 */
	public static class StatusColor implements ValueColorProvider {
		@Override
		public ValueColor colorOf(Object value) {
			Object status = ((TLObject) value).tValueByName(STATUS_ATTRIBUTE);
			return AnnotationValueColorProvider.INSTANCE.colorOf(status);
		}
	}

	/**
	 * Transient object with an identity, which is what an observation registers a listener for.
	 */
	private static class IdentifiedObject extends TransientTLObjectImpl {

		private static final MOClassImpl TABLE = new MOClassImpl("test");

		private static long _nextId = 1;

		private final ObjectKey _id =
			new DefaultObjectKey(1, Revision.CURRENT_REV, TABLE, LongID.valueOf(_nextId++));

		IdentifiedObject(TLStructuredType type) {
			super(type, null);
		}

		@Override
		public ObjectKey tId() {
			return _id;
		}
	}

	/**
	 * {@link ModelScope} the test delivers object changes through, standing in for the scope of a
	 * browser window.
	 */
	private static class RecordingScope implements ModelScope {

		private final Map<ObjectKey, Set<ModelListener>> _objectListeners = new LinkedHashMap<>();

		private final Map<TLStructuredType, Set<ModelListener>> _typeListeners = new LinkedHashMap<>();

		private final Set<ModelListener> _globalListeners = new LinkedHashSet<>();

		@Override
		public boolean addModelListener(ModelListener listener) {
			return _globalListeners.add(listener);
		}

		@Override
		public boolean addModelListener(TLStructuredType type, ModelListener listener) {
			return _typeListeners.computeIfAbsent(type, x -> new LinkedHashSet<>()).add(listener);
		}

		@Override
		public boolean addModelListener(TLObject object, ModelListener listener) {
			return _objectListeners.computeIfAbsent(object.tId(), x -> new LinkedHashSet<>()).add(listener);
		}

		@Override
		public boolean removeModelListener(ModelListener listener) {
			return _globalListeners.remove(listener);
		}

		@Override
		public boolean removeModelListener(TLStructuredType type, ModelListener listener) {
			Set<ModelListener> listeners = _typeListeners.get(type);
			return listeners != null && listeners.remove(listener);
		}

		@Override
		public boolean removeModelListener(TLObject object, ModelListener listener) {
			Set<ModelListener> listeners = _objectListeners.get(object.tId());
			return listeners != null && listeners.remove(listener);
		}

		/**
		 * Reports the given object as updated to everybody listening for it.
		 */
		void reportUpdate(TLObject object) {
			ModelChangeEvent event = new UpdateOf(object);
			for (ModelListener listener : Set.copyOf(listeners(object))) {
				listener.notifyChange(event);
			}
			for (ModelListener listener : Set.copyOf(_globalListeners)) {
				listener.notifyChange(event);
			}
		}

		private Set<ModelListener> listeners(TLObject object) {
			Set<ModelListener> result = _objectListeners.get(object.tId());
			return result == null ? Set.of() : result;
		}
	}

	/**
	 * The change of a single object, as a {@link ModelScope} reports it.
	 *
	 * @param object
	 *        The object that was updated.
	 */
	private record UpdateOf(TLObject object) implements ModelChangeEvent {

		@Override
		public ChangeType getChange(TLObject existingObject) {
			return existingObject == object ? ChangeType.UPDATED : ChangeType.NONE;
		}

		@Override
		public Stream<? extends TLObject> getUpdated() {
			return Stream.of(object);
		}

		@Override
		public Stream<? extends TLObject> getUpdated(TLStructuredType type) {
			return object.tType() == type ? getUpdated() : Stream.empty();
		}

		@Override
		public Stream<? extends TLObject> getCreated() {
			return Stream.empty();
		}

		@Override
		public Stream<? extends TLObject> getCreated(TLStructuredType type) {
			return Stream.empty();
		}

		@Override
		public Stream<? extends TLObject> getDeleted() {
			return Stream.empty();
		}

		@Override
		public Stream<? extends TLObject> getDeleted(TLStructuredType type) {
			return Stream.empty();
		}
	}

	/**
	 * React context whose {@link ModelScope} is the one the test reports changes through.
	 */
	private static final class ObservingReactContext extends DefaultReactContext {

		private final ModelScope _scope;

		ObservingReactContext(ModelScope scope) {
			super("", "test", new SSEUpdateQueue());
			_scope = scope;
		}

		@Override
		public ModelScope getModelScope() {
			return _scope;
		}
	}

	/**
	 * The suite of tests.
	 */
	public static Test suite() {
		Test test = new TestSuite(TestTextElementObservation.class);
		test = ServiceTestSetup.createSetup(test, CompatibilityService.Module.INSTANCE,
			AttributeSettings.Module.INSTANCE);
		return TLTestSetup.createTLTestSetup(test);
	}

}
