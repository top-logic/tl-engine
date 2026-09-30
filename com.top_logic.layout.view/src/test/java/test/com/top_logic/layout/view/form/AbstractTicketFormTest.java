/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.base.locking.handler.LockHandler;
import com.top_logic.base.locking.handler.NoTokenHandling;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.element.meta.kbbased.storage.mappings.DirectMapping;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.form.AttributeFieldControl;
import com.top_logic.layout.view.form.FormControl;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLPrimitive.Kind;
import com.top_logic.model.access.StorageMapping;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.util.TLModelUtil;

/**
 * Base class for tests of a {@link FormControl} displaying a ticket with the fields {@link #TITLE}
 * and {@link #STATUS}, whose changes are delivered through a {@link RecordingScope}.
 *
 * <p>
 * The form is set up displayed ({@link FormControl#attach() attached}) and in view mode.
 * </p>
 */
@SuppressWarnings("javadoc")
public abstract class AbstractTicketFormTest extends BasicTestCase {

	/** Name of the attribute the user edits in the tests. */
	protected static final String TITLE = "title";

	/** Name of the attribute the user leaves alone in the tests. */
	protected static final String STATUS = "status";

	/** The message the form shows when it displays no object. */
	protected static final String NO_MODEL_MESSAGE = "no model";

	/** Key of the value an input shows, see {@link ReactFormFieldControl#VALUE}. */
	private static final String SHOWN_VALUE = "value";

	protected ReactContext _context;

	protected TLClass _ticketType;

	protected RecordingScope _scope;

	protected IdentifiedObject _ticket;

	protected FormControl _form;

	protected AttributeFieldControl _title;

	protected AttributeFieldControl _status;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		TLModelImpl model = new TLModelImpl();
		TLModule module = TLModelUtil.addModule(model, "test." + getClass().getSimpleName());
		TLPrimitive text =
			TLModelUtil.addDatatype(module, module, "Text", Kind.STRING, directMapping(String.class));
		_ticketType = TLModelUtil.addClass(module, "Ticket");
		TLModelUtil.addProperty(_ticketType, TITLE, text);
		TLModelUtil.addProperty(_ticketType, STATUS, text);

		_ticket = newTicket("Login fails", "open");

		_context = new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		_scope = new RecordingScope();

		_form = new FormControl(_context, _ticket, NO_MODEL_MESSAGE, lockHandler());
		_form.setModelScope(_scope);
		_title = field(TITLE);
		_status = field(STATUS);
		_form.attach();
	}

	@Override
	protected void tearDown() throws Exception {
		_form = null;
		_title = null;
		_status = null;
		_ticket = null;
		_scope = null;
		_context = null;

		super.tearDown();
	}

	/**
	 * The {@link LockHandler} the form locks the ticket with while editing it.
	 */
	protected LockHandler lockHandler() {
		return NoTokenHandling.INSTANCE;
	}

	/**
	 * A new ticket with the given values.
	 */
	protected IdentifiedObject newTicket(String title, String status) {
		IdentifiedObject result = new IdentifiedObject(_ticketType);
		result.tUpdateByName(TITLE, title);
		result.tUpdateByName(STATUS, status);
		return result;
	}

	/**
	 * Stores the given values to the displayed ticket, as a command of the same window does, and
	 * delivers the resulting change through the window's scope.
	 */
	protected void storeChange(String title, String status) {
		_ticket.tUpdateByName(TITLE, title);
		_ticket.tUpdateByName(STATUS, status);
		_scope.reportUpdate(_ticket);
	}

	/**
	 * Creates the field of the given attribute in {@link #_form}.
	 */
	protected AttributeFieldControl field(String attribute) {
		AttributeFieldControl result =
			new AttributeFieldControl(_context, _form, _form, attribute, null, false, null);
		result.createChromeControl();
		return result;
	}

	/**
	 * The value the input of the given field shows, as the client receives it.
	 */
	protected static Object shown(AttributeFieldControl field) {
		return field.getInnerControl().scriptingScalarState().get(SHOWN_VALUE);
	}

	/**
	 * The model of the input the given field is edited in, as the input control writes the user's
	 * entries to it.
	 */
	protected static AbstractFieldModel model(AttributeFieldControl field) {
		return (AbstractFieldModel) ((ReactFormFieldControl) field.getInnerControl()).getFieldModel();
	}

	/**
	 * A mapping storing the values as they are, as the core datatypes of that application type use
	 * it.
	 */
	private static StorageMapping<?> directMapping(Class<?> applicationType) throws ConfigurationException {
		PolymorphicConfiguration<?> config =
			TypedConfiguration.createConfigItemForImplementationClass(DirectMapping.class);
		config.update(config.descriptor().getProperty(DirectMapping.Config.APPLICATION_TYPE), applicationType);
		return (StorageMapping<?>) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
	}

}
