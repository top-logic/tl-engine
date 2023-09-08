/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util;

import static test.com.top_logic.util.I18NConstants.*;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import junit.extensions.ActiveTestSuite;
import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.TestUtils;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.DateUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Logger.LogEntry;
import com.top_logic.basic.ReloadableManager;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.logging.Level;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.basic.time.TimeZones;
import com.top_logic.basic.tools.CollectingLogListener;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.util.DefaultResourcesModule;
import com.top_logic.util.Resources;

/**
 * Test the {@link Resources}.
 *
 * @author     <a href="mailto:michanel.gaensler@top-logic.com">Michael Gänsler</a>
 */
@SuppressWarnings("javadoc")
public class TestResources extends BasicTestCase { 

	/** Some static array of Objects for {@link #testGetMessage()} */
    protected static final Object[] VALUES0 = {
		Boolean.FALSE, Integer.valueOf(0), Double.valueOf(Math.E), "Blah"
    };

    /** Some static array of Objects for {@link #testGetMessage()} */
    protected static final Object[] VALUES1 = {
		Boolean.TRUE, Integer.valueOf(77), Double.valueOf(Math.PI), "Blub"
    };

    /** Some static array of Objects for <code>testGetMessage</ocde> */
    protected static final Object[] VALUES2 = {
		null, Integer.valueOf(Integer.MIN_VALUE), Double.valueOf(Double.MIN_VALUE), null
    };

	private Locale _testLocale;

    public TestResources (String aName) {
        super (aName);
    }

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_testLocale = new Locale("xx", "XX", "TEST");
	}

	@Override
	protected void tearDown() throws Exception {
		_testLocale = null;
		super.tearDown();
	}

    /**
     * Reload existing Resources to avoid problems with sticky configurations.
     */
    public void doReload () {
        ReloadableManager.getInstance().reload(DefaultResourcesModule.RELOADABLE_KEY);
    }

    public void testGetStringVariant () {
		Resources resources = Resources.getInstance(_testLocale);
		String theString = resources.getString(OID);
        assertEquals("TEST_OID",theString);

		theString = resources.getString(TEST);
        assertEquals("123",theString);
    }

	public void testResolve() {
		Resources bundle = Resources.getInstance(_testLocale);

		String value1 = "value1";
		String value2 = "value2";
		String value3 = "value3";

		assertResolve(bundle, value1, EXISTING_1);
		assertResolve(bundle, value2, EXISTING_2);
		assertResolve(bundle, value3, EXISTING_3);

		assertNotExists(bundle, UNDEFINED_1);
		assertNotExists(bundle, UNDEFINED_2);

		assertSame(EXISTING_1, ResKey.fallback(null, EXISTING_1));
		assertSame(EXISTING_1, ResKey.fallback(EXISTING_1, null));
		assertNull(ResKey.fallback(null, null));

		assertExists(bundle, ResKey.fallback(EXISTING_1, UNDEFINED_1));
		assertExists(bundle, ResKey.fallback(UNDEFINED_1, EXISTING_1));
		assertNotExists(bundle, ResKey.fallback(UNDEFINED_1, UNDEFINED_2));

		assertExists(bundle,
			ResKey.fallback(UNDEFINED_1,
				ResKey.fallback(UNDEFINED_2, EXISTING_1)));

		assertExists(bundle, EXISTING_1, null);
		assertNotExists(bundle, UNDEFINED_1, null);

		assertExists(bundle, ResKey.fallback(EXISTING_1, UNDEFINED_1), null);
		assertExists(bundle, ResKey.fallback(UNDEFINED_1, EXISTING_1), null);
		assertNotExists(bundle, ResKey.fallback(UNDEFINED_1, UNDEFINED_2), null);

		assertExists(bundle,
			ResKey.fallback(UNDEFINED_1,
				ResKey.fallback(UNDEFINED_2,
					EXISTING_1)), null);

		assertResolve(bundle, value1, ResKey.fallback(EXISTING_1, EXISTING_2));
		assertResolve(bundle, value2, ResKey.fallback(EXISTING_2, EXISTING_1));

		assertResolve(bundle, value1,
			ResKey.fallback(
				EXISTING_1,
				EXISTING_2),
			EXISTING_3);

		assertResolve(bundle, value1,
			ResKey.fallback(
				UNDEFINED_1,
				EXISTING_1),
			EXISTING_3);

		assertResolve(bundle, value3,
			ResKey.fallback(
				UNDEFINED_1,
				UNDEFINED_2),
			EXISTING_3);
		
		assertResolve(bundle, value3,
			ResKey.fallback(
				ResKey.fallback(
					UNDEFINED_1,
					UNDEFINED_2),
				EXISTING_3));

		assertResolve(bundle, value3,
			ResKey.fallback(
				UNDEFINED_1,
				ResKey.fallback(
					UNDEFINED_2,
					EXISTING_3)));
		
		assertResolve(bundle, value1,
			ResKey.fallback(
				ResKey.fallback(
					UNDEFINED_1,
					EXISTING_1),
				ResKey.fallback(
					EXISTING_2,
					EXISTING_3)));
	}

	public void testResolveDeprecated() {
		Resources bundle = Resources.getInstance(_testLocale);

		String value1 = "value1";
		String value2 = "value2";
		String value3 = "value3";

		assertResolve(bundle, value1, EXISTING_1);
		assertResolve(bundle, value2, EXISTING_2);
		assertResolve(bundle, value3, EXISTING_3);

		CollectingLogListener listener = new CollectingLogListener(set(Level.ERROR), true);

		assertResolve(bundle, value1,
			ResKey.fallback(
				ResKey.fallback(
					UNDEFINED_1,
					ResKey.deprecated(EXISTING_1)),
				ResKey.fallback(
					ResKey.deprecated(EXISTING_2),
					ResKey.deprecated(EXISTING_3))));

		List<LogEntry> logs1 = listener.getAndClearLogEntries();
		assertEquals(1, logs1.size());
		assertEquals("Deprecated resource key [" + EXISTING_1.getKey() + "] was resolved, [" + UNDEFINED_1.getKey()
			+ "] should be used instead.",
			logs1.get(0).getMessage());

		listener.deactivate();
	}

	private void assertExists(Resources resources, ResKey key) {
		assertValidResource(resources, resources.getString(key));
	}

	private void assertResolve(Resources resources, String expected, ResKey key) {
		assertEquals(expected, resources.getString(key));
	}

	private void assertExists(Resources resources, ResKey key, ResKey defaultKey) {
		assertValidResource(resources, resources.getStringWithDefaultKey(key, defaultKey));
	}

	private void assertResolve(Resources resources, String expected, ResKey key, ResKey defaultKey) {
		assertEquals(expected, resources.getStringWithDefaultKey(key, defaultKey));
	}

	private void assertValidResource(Resources resources, String resource) {
		assertNotNull(resource);
		assertFalse(resources +
			", '" + resource + "' should not start with '['", StringServices.startsWithChar(resource, '['));
	}

	private void assertNotExists(Resources resources, ResKey key) {
		assertInvalidResource(resources, resources.getString(key));
	}

	private void assertNotExists(Resources resources, ResKey key, ResKey defaultKey) {
		assertInvalidResource(resources, resources.getStringWithDefaultKey(key, defaultKey));
	}

	private void assertInvalidResource(Resources resources, String resource) {
		assertNotNull(resource);
        assertTrue (resources + 
			", '" + resource + "' should start with '['", StringServices.startsWithChar(resource, '['));
	}

	/**
	 * Test, how the method handles an empty parameter, this should not leed to a problem. The
	 * defined behavior for an empty parameter is the return of the string "[no key specified]".
	 */
    public void testGetString_NULL () {
        Resources resources = Resources.getInstance ();
		assertEquals(StringServices.EMPTY_STRING, resources.getString(ResKey.forTest(null)));
		assertEquals(StringServices.EMPTY_STRING, resources.getString(null));
    }

	public void testGetMessageUnknownKey() {
		Resources resources = Resources.getInstance();
		String message = resources.getMessage(ResKey.forTestUnknown("xyz"), "param1", "param2");
		assertTrue("I18N for unkown key starts with '['.", StringServices.startsWithChar(message, '['));
		assertTrue("I18N for unkown message should contain parameter: " + message, message.contains("param1"));
		assertTrue("I18N for unkown message should contain parameter: " + message, message.contains("param2"));
	}

    /**
     * Test, how the method handles an empty parameter, this should not
     * leed to a problem. The defined behavior for an empty parameter is
     * the return of the string "[no key specified]".
     */
    public void testInstances () {
        
        Resources de1 = Resources.getInstance (Locale.GERMAN);
        Resources de2 = Resources.getInstance (Locale.GERMAN);
        assertSame(de1,de2);
        
        Resources us1 = Resources.getInstance (Locale.US);
        Resources us2 = Resources.getInstance (Locale.US);
        assertSame(us1,us2);

        de1 = Resources.getInstance ("de_DE");
        de2 = Resources.getInstance ("de_DE");
        assertSame(de1,de2);
        
        de1 = Resources.getInstance ("de");
        de2 = Resources.getInstance ("de");
        assertSame(de1,de2);
    }

    /**
     * Do an exhaustive Test over all Locales.
     */
    public void testAll () {
        Locale[] all = Locale.getAvailableLocales();
        int      len = all.length;
        for (int i=0; i < len; i++)  {
            Locale    l = all[i];        
            Resources.getInstance(l);
        }
    }
    
	/**
	 * Ensure that regular plain keys equal their encoded form.
	 */
	public void testDecodeCompatibility() {
		assertEquals("foo.bar", ResKey.encode(ResKey.forTest("foo.bar")));
	}

	public void testDecodeMessageEmpty() {
		Resources resources = Resources.getInstance(_testLocale);

		assertEquals(StringServices.EMPTY_STRING, resources.getString(ResKey.decode("")));
	}

	public void testDecodeMessageNoArg() {
		Resources resources = Resources.getInstance(_testLocale);

		assertDecode(resources, "Keine Nachricht", I18NConstants.NO_MESSAGE.getKey());
	}

	public void testDecodeMessageNoEncode() {
		Resources resources = Resources.getInstance(_testLocale);

		assertEquals("Keine Nachricht",
			resources.getString(ResKey.decode(I18NConstants.NO_MESSAGE.getKey())));
	}

// Does not work.
//
//	public void testDecodeMessageLongString() {
//		Resources resources = Resources.getInstance(_testLocale);
//		
//		assertDecode(resources, "Nachricht '1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890 1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890 1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890'", I18NConstants.MESSAGE__X, "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890 1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890 1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890");
//	}
	
	public void testDecodeMessageDate() {
		Resources resources = Resources.getInstance(_testLocale);

		Date date = DateUtil.createDate(CalendarUtil.createCalendar(TimeZones.UTC), 2014, Calendar.MAY, 19);
		ThreadContext threadContext = ThreadContext.getThreadContext();
		threadContext.setCurrentTimeZone(TimeZones.UTC);
		assertEncodeDecode(resources, "Nachricht '2014-05-19 00:00'", I18NConstants.MESSAGE__X, date);
		threadContext.setCurrentTimeZone(getTimeZoneBerlin());
		assertEncodeDecode(resources, "Nachricht '2014-05-19 02:00'", I18NConstants.MESSAGE__X, date);
		threadContext.setCurrentTimeZone(getTimeZoneLosAngeles());
		assertEncodeDecode(resources, "Nachricht '2014-05-18 17:00'", I18NConstants.MESSAGE__X, date);
	}

	public void testDecodeMessageStringWithSpecials() {
		Resources resources = Resources.getInstance(_testLocale);

		assertEncodeDecode(resources, "Nachricht '/>'&\"/'\"</' und '/<'&\"/'\">/'", I18NConstants.MESSAGE__X_Y,
			"/>'&\"/'\"</", "/<'&\"/'\">/");
	}

	public void testDecodeMessageStringLegacyEncoding() {
		Resources resources = Resources.getInstance(_testLocale);

		assertDecode(resources, "Nachricht '/some/string/' und '/other/string/'", I18NConstants.MESSAGE__X_Y + "/s"
			+ "//some//string//" + "/s" + "//other//string//");
	}

	public void testDecodeMessageMissingArgument() {
		Resources resources = Resources.getInstance(_testLocale);
		assertDecode(resources, "Nachricht 'null'", I18NConstants.MESSAGE__X + "/");
	}

	public void testDecodeMessageMissingSecondArgument() {
		Resources resources = Resources.getInstance(_testLocale);
		assertDecode(resources, "Nachricht 'String' und 'null'", I18NConstants.MESSAGE__X_Y + "/s" + "String" + "/");
	}

	public void testDecodeMessageEmptyArguments() {
		Resources resources = Resources.getInstance(_testLocale);
		assertDecode(resources, "Nachricht '' und ''", I18NConstants.MESSAGE__X_Y + "/s" + "" + "/s" + "");
	}

	public void testDecodeMessage() {
		Resources resources = Resources.getInstance(_testLocale);

		assertEncodeDecode(resources, "Nachricht 'true'", I18NConstants.MESSAGE__X, true);
		assertEncodeDecode(resources, "Nachricht 'false'", I18NConstants.MESSAGE__X, false);
		assertEncodeDecode(resources, "Nachricht '42'", I18NConstants.MESSAGE__X, 42);
		assertEncodeDecode(resources, "Nachricht '13.13'", I18NConstants.MESSAGE__X, 13.13f);
		assertEncodeDecode(resources, "Nachricht '12,345,678,900'", I18NConstants.MESSAGE__X, 12345678900L);
		assertEncodeDecode(resources, "Nachricht '13.131'", I18NConstants.MESSAGE__X, 13.1313131313131313D);
		assertEncodeDecode(resources, "Nachricht '/Some/string/'", I18NConstants.MESSAGE__X, "/Some/string/");
		assertEncodeDecode(resources, "Nachricht 'Ending with an exclamation mark!'", I18NConstants.MESSAGE__X,
			"Ending with an exclamation mark!");
	}

	private void assertEncodeDecode(Resources resources, String expected, ResKey1 key, Object argument) {
		assertDecode(resources, expected, ResKey.encode(key.fill(argument)));
	}

	private void assertEncodeDecode(Resources resources, String expected, ResKey2 key, Object arg1, Object arg2) {
		assertDecode(resources, expected, ResKey.encode(key.fill(arg1, arg2)));
	}

	private void assertDecode(Resources resources, String expected, String encodedArguments) {
		assertEquals(expected, resources.getString(ResKey.decode(encodedArguments)));
	}

    /**
     * Test the getMessage(String, Object[]) method.
     * 
     * This should be done in an Active TestSuite to check
     * that it is Thread safe.
     */
    public void testGetMessage () {
                
		Resources res = Resources.getInstance(_testLocale);
        
        for (int i=0; i < 10; i++) {
        	
        	/*
        	// Attention: While using our fantasy-test-locale, the MessageFormat-Class falls back to system locale
        	// formating the numbers. This is the case why the following lines fail in others than the german locale...
            assertEquals("No message with a false Boolean , some Integer 000,000E000, a Double 2,718, and the String \"Blah\". The Integer is zero",
                         res.getMessage(ResKey.forTest("test.message0"), VALUES0));    
            
            assertEquals("One message with a true Boolean , some Integer 770,000E-001, a Double 3,142, and the String \"Blub\". The Integer is positiv",
                         res.getMessage(ResKey.forTest("test.message1"), VALUES1));    
     
            assertEquals("Two messages with a null Boolean , some Integer -214,748E007, a Double 0, and the String \"null\". The Integer is negative",
                         res.getMessage(ResKey.forTest("test.message2"), VALUES2));
            */
        	// ...test around the language-depended number-parts... 
            String message = res.getMessage(FOR_TEST, VALUES0);
			if (message.charAt(0) == '[') {
                System.out.println(message);
				res = Resources.getInstance(_testLocale);
            }
            
			assertContains("No message with a false Boolean , some Integer 000", message);
			assertContains(", a Double 2", message);
			assertContains("718, and the String \"Blah\". The Integer is zero", message);

            String message2 = res.getMessage(FOR_TEST_1, VALUES1);
			assertContains("One message with a true Boolean , some Integer 77", message2);
			assertContains(", a Double 3", message2);
			assertContains("142, and the String \"Blub\". The Integer is positiv", message2);

			String message3 = res.getMessage(FOR_TEST_2, VALUES2);
			assertContains("Two messages with a null Boolean , some Integer -214", message3);
			assertContains(", a Double 0, and the String \"null\". The Integer is negative", message3);
        }
    }

	public void testLabelProviderInMessage() {
		Person root = PersonManager.getManager().getRoot();
		String label = MetaLabelProvider.INSTANCE.getLabel(root);
		ResKey msgKey = I18NConstants.LABELS_FOR_BUSINESS_OBJECTS.fill(root);
		assertEquals("Label EN: " + label, Resources.getInstance(Locale.ENGLISH).getString(msgKey));
		assertEquals("Label DE: " + label, Resources.getInstance(Locale.GERMAN).getString(msgKey));
	}

    public static Test suite () {
        TestSuite mainSuite = new TestSuite (TestResources.class);
        
        ActiveTestSuite paralelSuite = new ActiveTestSuite();
		paralelSuite.addTest(TestUtils.tryEnrichTestnames(new TestResources("testGetMessage"), "ActiveTestSuite 1"));
		paralelSuite.addTest(TestUtils.tryEnrichTestnames(new TestResources("testGetMessage"), "ActiveTestSuite 2"));
        paralelSuite.addTest(mainSuite);
		paralelSuite.addTest(TestUtils.tryEnrichTestnames(new TestResources("testGetMessage"), "ActiveTestSuite"));
        
        TestSuite outerSuite = new TestSuite (TestResources.class.getName());
		outerSuite.addTest(TestUtils.tryEnrichTestnames(new TestResources("doReload"), "Test 1"));
        outerSuite.addTest(paralelSuite);
		outerSuite.addTest(TestUtils.tryEnrichTestnames(new TestResources("doReload"), "Test 2"));

		return PersonManagerSetup
			.createPersonManagerSetup(ServiceTestSetup.createSetup(outerSuite, LabelProviderService.Module.INSTANCE));
    }

    public static void main (String[] args) {
        
        Logger.configureStdout();
        junit.textui.TestRunner.run (suite ());
    }

}
