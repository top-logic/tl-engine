/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.channel;

import java.util.Collections;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelFactory;
import com.top_logic.layout.view.channel.ValueChannelConfig;
import com.top_logic.layout.view.channel.ValueChannelFactory;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModule;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * Tests a {@code <channel>} that remembers its value per user
 * ({@link ValueChannelConfig#isPersonalize()}).
 *
 * <p>
 * A session of a user is a {@link TLContextManager#inPersonContext(Person, com.top_logic.basic.thread.InContext)
 * person context}: it starts with the personal configuration stored for the user and stores it
 * when it ends, as a logout does.
 * </p>
 */
public class TestPersonalizedChannel extends BasicTestCase {

	private static final String CHANNEL = "structure";

	private static final String INIT_VALUE = "initial";

	private static final String EXPLICIT_KEY = "test.channel.explicit";

	private static final String MODULE_NAME = "test.PersonalizedChannel";

	private static final String PERSONALIZED =
		"<channel name='" + CHANNEL + "' init='\"" + INIT_VALUE + "\"' " + ValueChannelConfig.PERSONALIZE + "='true'/>";

	private ViewContext _context;

	private String _defaultKey;

	private Person _alice;

	private Person _bob;

	private TLModule _module;

	private TLClassifier _red;

	private TLClassifier _green;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_context = new DefaultViewContext(null);
		_defaultKey = _context.getPersonalizationKey() + "." + ValueChannelFactory.CHANNEL_SEGMENT + "." + CHANNEL;
		_alice = TestPerson.createPerson("personalizedChannelAlice");
		_bob = TestPerson.createPerson("personalizedChannelBob");

		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			_module = TLModelUtil.addModule(ModelService.getApplicationModel(), MODULE_NAME);
			TLEnumeration colors = TLModelUtil.addEnumeration(_module, "Color");
			_red = TLModelUtil.addClassifier(colors, "red");
			_green = TLModelUtil.addClassifier(colors, "green");
			tx.commit();
		}
	}

	@Override
	protected void tearDown() throws Exception {
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			TLModelUtil.deleteRecursive(_module);
			tx.commit();
		}
		TestPerson.deletePersonAndUser(_alice);
		TestPerson.deletePersonAndUser(_bob);
		_module = null;
		_red = null;
		_green = null;
		_alice = null;
		_bob = null;
		_context = null;
		super.tearDown();
	}

	/**
	 * Tests that a channel starts with its initial value as long as nothing is remembered, and
	 * that the value written is remembered for the next session of the same user only.
	 */
	public void testRestoredInNextSessionOfSameUser() throws Exception {
		ChannelFactory factory = factory(PERSONALIZED);

		inSession(_alice, () -> {
			ViewChannel channel = factory.createChannel(_context);
			assertEquals("Nothing remembered yet: the initial value applies.", INIT_VALUE, channel.get());
			assertNull("Filling the channel initially remembers nothing.", stored(_defaultKey));

			channel.set("chosen");
			assertEquals("chosen", stored(_defaultKey));
		});

		inSession(_alice, () -> {
			assertEquals("The remembered value takes precedence over the initial value.", "chosen",
				factory.createChannel(_context).get());
		});

		inSession(_bob, () -> {
			assertEquals("Another user does not see the value remembered for the first.", INIT_VALUE,
				factory.createChannel(_context).get());
			assertNull(stored(_defaultKey));
		});
	}

	/**
	 * Tests that writing an empty value forgets the remembered value, so that the initial value
	 * applies in the next session.
	 */
	public void testEmptyValueForgets() throws Exception {
		ChannelFactory factory = factory(PERSONALIZED);

		inSession(_alice, () -> factory.createChannel(_context).set("chosen"));

		inSession(_alice, () -> {
			ViewChannel channel = factory.createChannel(_context);
			assertEquals("chosen", channel.get());
			channel.set(null);
			assertNull("An empty value removes the entry.", stored(_defaultKey));
		});

		inSession(_alice, () -> assertEquals(INIT_VALUE, factory.createChannel(_context).get()));
	}

	/**
	 * Tests that a classifier is remembered by its name, and that a remembered classifier that was
	 * removed is forgotten in favor of the initial value.
	 */
	public void testClassifier() throws Exception {
		ChannelFactory factory = factory(PERSONALIZED);

		inSession(_alice, () -> factory.createChannel(_context).set(_red));
		inSession(_alice, () -> assertSame(_red, factory.createChannel(_context).get()));

		inSession(_alice, () -> factory.createChannel(_context).set(_green));
		try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			TLModelUtil.deleteRecursive(_green);
			tx.commit();
		}

		inSession(_alice, () -> {
			assertNotNull("Precondition: the removed classifier is remembered.", stored(_defaultKey));
			assertEquals("A remembered value that no longer resolves gives way to the initial value.", INIT_VALUE,
				factory.createChannel(_context).get());
			assertNull("A remembered value that no longer resolves is forgotten.", stored(_defaultKey));
		});
	}

	/**
	 * Tests that a value that cannot be named stably is not remembered, and that writing it does
	 * not fail.
	 */
	public void testNotStorableValue() throws Exception {
		ChannelFactory factory = factory(PERSONALIZED);

		inSession(_alice, () -> {
			ViewChannel channel = factory.createChannel(_context);
			channel.set("chosen");
			assertEquals("chosen", stored(_defaultKey));

			StringBuilder notStorable = new StringBuilder("transient");
			channel.set(notStorable);
			assertSame(notStorable, channel.get());
			assertNull("A value that cannot be remembered removes the entry.", stored(_defaultKey));

			channel.set(new StringBuilder("another"));
			assertNull(stored(_defaultKey));
		});

		inSession(_alice, () -> assertEquals(INIT_VALUE, factory.createChannel(_context).get()));
	}

	/**
	 * Tests that a channel that is not personalized remembers nothing.
	 */
	public void testNotPersonalized() throws Exception {
		ChannelFactory factory = factory("<channel name='" + CHANNEL + "' init='\"" + INIT_VALUE + "\"'/>");

		inSession(_alice, () -> {
			factory.createChannel(_context).set("chosen");
			assertNull(stored(_defaultKey));
		});

		inSession(_alice, () -> assertEquals(INIT_VALUE, factory.createChannel(_context).get()));
	}

	/**
	 * Tests that a channel without initialization function starts empty and is restored as well.
	 */
	public void testWithoutInit() throws Exception {
		ChannelFactory factory =
			factory("<channel name='" + CHANNEL + "' " + ValueChannelConfig.PERSONALIZE + "='true'/>");

		inSession(_alice, () -> {
			ViewChannel channel = factory.createChannel(_context);
			assertNull(channel.get());
			channel.set("chosen");
		});

		inSession(_alice, () -> assertEquals("chosen", factory.createChannel(_context).get()));
	}

	/**
	 * Tests that the configured personalization key is used as key.
	 */
	public void testExplicitKey() throws Exception {
		ChannelFactory factory = factory("<channel name='" + CHANNEL + "' init='\"" + INIT_VALUE + "\"' "
			+ ValueChannelConfig.PERSONALIZE + "='true' " + ValueChannelConfig.PERSONALIZATION_KEY + "='"
			+ EXPLICIT_KEY + "'/>");

		inSession(_alice, () -> {
			factory.createChannel(_context).set("chosen");
			assertEquals("chosen", stored(EXPLICIT_KEY));
			assertNull(stored(_defaultKey));
		});

		inSession(_alice, () -> assertEquals("chosen", factory.createChannel(_context).get()));
	}

	/**
	 * Tests that a personalization key without personalization is reported as configuration
	 * error.
	 */
	public void testKeyWithoutPersonalizeFails() throws Exception {
		ValueChannelConfig config = parse("<channel name='" + CHANNEL + "' "
			+ ValueChannelConfig.PERSONALIZATION_KEY + "='" + EXPLICIT_KEY + "'/>");

		BufferingProtocol log = new BufferingProtocol();
		new DefaultInstantiationContext(log).getInstance(config);
		assertTrue("A personalization key without personalization must be reported.", log.hasErrors());
	}

	/**
	 * Runs the given code in a session of the given user, which starts with the personal
	 * configuration stored for the user and stores it at its end.
	 */
	private static void inSession(Person user, Runnable code) {
		TLContextManager.inPersonContext(user, () -> {
			code.run();
			PersonalConfiguration.storePersonalConfiguration();
		});
	}

	private static Object stored(String key) {
		return PersonalConfiguration.getPersonalConfiguration().getJSONValue(key);
	}

	private static ChannelFactory factory(String xml) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestPersonalizedChannel.class);
		ChannelFactory result = context.getInstance(parse(xml));
		context.checkErrors();
		return result;
	}

	private static ValueChannelConfig parse(String xml) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestPersonalizedChannel.class);
		ConfigurationReader reader = new ConfigurationReader(context, Collections.singletonMap("channel",
			TypedConfiguration.getConfigurationDescriptor(ValueChannelConfig.class)));
		reader.setSource(CharacterContents.newContent(xml));
		ValueChannelConfig result = (ValueChannelConfig) reader.read();
		context.checkErrors();
		return result;
	}

	/**
	 * The suite of tests.
	 *
	 * @implNote The initialization functions are compiled and evaluated with the
	 *           {@link SearchBuilder}, classifiers are resolved in the application model, and the
	 *           personal configurations belong to accounts of the {@link PersonManager}.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestPersonalizedChannel.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, SearchBuilder.Module.INSTANCE, ModelService.Module.INSTANCE,
				PersonManager.Module.INSTANCE));
	}
}
