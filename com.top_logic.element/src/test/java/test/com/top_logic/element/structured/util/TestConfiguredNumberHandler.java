/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.structured.util;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Test;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ExpectedFailure;
import test.com.top_logic.basic.ExpectedFailureProtocol;
import test.com.top_logic.basic.TestFactory;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.constraint.check.ConstraintChecker;
import com.top_logic.basic.exception.I18NFailure;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.structured.util.ConfiguredNumberHandler;
import com.top_logic.element.structured.util.ConfiguredNumberHandler.Config;
import com.top_logic.element.structured.util.ContextAwareSequenceName;
import com.top_logic.element.structured.util.GenerateNumberException;
import com.top_logic.element.structured.util.I18NConstants;
import com.top_logic.element.structured.util.NumberHandler;
import com.top_logic.element.structured.util.NumberHandlerFactory;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.Transaction;

/**
 * Test case for {@link ConfiguredNumberHandler}
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestConfiguredNumberHandler extends BasicTestCase {

	/**
	 * Test concurrent number generation on new number sequences.
	 */
	public void testConcurrent() throws InterruptedException {
		final KnowledgeBase kb = KnowledgeBaseFactory.getInstance().getDefaultKnowledgeBase();
		
		NumberHandlerFactory factory = NumberHandlerFactory.getInstance();
		final NumberHandler handler = factory.getNumberHandler("TestConfiguredNumberHandler_testConcurrent");

		for (int n = 0; n < 4; n++) {
			// Sleep until the next full second starts to maximize clash probability.
			GregorianCalendar cal = new GregorianCalendar();
			int currentMilli = cal.get(Calendar.MILLISECOND);
			Thread.sleep(1000 - currentMilli);
			
			final int parallelTests = 2;
			final Set<Object> numbers = Collections.synchronizedSet(CollectionUtil.newSet(parallelTests));
			parallelTest(parallelTests, 15*1000, new ExecutionFactory() {
				@Override
				public Execution createExecution(final int id) {
					return new Execution() {
						@Override
						public void run() throws Exception {
							{
								Transaction tx = kb.beginTransaction();
								numbers.add(handler.generateId(null));
								
								// Simulate long running transaction.
								Thread.sleep(1000);
								tx.commit();
							}
						}
					};
				}
			});
			assertEquals("Not all tests generate different numbers", parallelTests, numbers.size());
		}
	}
	
	public void testBrokenConfiguration() {
	    try {
			NumberHandlerFactory.getInstance().getNumberHandler("HANDLER.DOES.NOT.EXISTS");
	        fail("Expected an Exception during instantiation");
	    } catch (Exception e) {
	        // expected
	    }
		Config noNumberPatterb = newConfig();

		// Missing %NUMBER%
		noNumberPatterb.setPattern("%DATE%");

		noNumberPatterb.setNumberPattern("#");
		noNumberPatterb.setDatePattern("yyyy-MM-dd");
		try {
			expectFailure(noNumberPatterb);
			fail("No number pattern");
		} catch (ExpectedFailure ex) {
			assertFailure((ResKey) I18NConstants.ERROR_NUMBER_HANDLER_NO_NUMBER_PATTERN, ex);
		}

	    try {
	        NumberHandlerFactory.getInstance().getNumberHandler("TestConfiguredNumberHandler.BrokenB");
	        fail("Expected an Exception during instantiation");
	    } catch (Exception e) {
	        // expected
	    }
	}

	private static void assertFailure(ResKey expected, ExpectedFailure ex) {
		ResKey error = ((I18NFailure) ex.getCause()).getErrorKey();
		if (!contains(expected, error)) {
			throw ex;
		}
	}

	private static boolean contains(ResKey expected, ResKey error) {
		if (error.plain() == expected) {
			return true;
		}
		for (Object arg : error.arguments()) {
			if (arg instanceof ResKey) {
				if (contains(expected, (ResKey) arg)) {
					return true;
				}
			}
		}
		return false;
	}

	private static void expectFailure(PolymorphicConfiguration<?> config) {
		getInstance(new ExpectedFailureProtocol(), config);
	}

	private static <T> T getInstance(PolymorphicConfiguration<T> config) {
		return getInstance(new AssertProtocol(), config);
	}

	private static <T> T getInstance(Protocol protocol, PolymorphicConfiguration<T> config) {
		config.check(protocol);
		ConstraintChecker checker = new ConstraintChecker();
		checker.check(protocol, config);
		protocol.checkErrors();

		T result = new DefaultInstantiationContext(protocol).getInstance(config);
		protocol.checkErrors();
		return result;
	}

	public void testCustomHandler() {
		NumberHandler customHandler =
			NumberHandlerFactory.getInstance().getNumberHandler("TestConfiguredNumberHandler.Custom");
		assertNotNull(customHandler);
		assertInstanceof(customHandler, NumberHandlerForTest.class);
	}

	public void testDefaultConfig() throws Exception {
		ConfiguredNumberHandler.Config config = newConfig();

		NumberHandler handler = getInstance(config);
		assertEquals("1", handler.generateId(null));
		assertEquals("2", handler.generateId("ABC"));
	}

	public void testContextSensitive() throws Exception {
		ConfiguredNumberHandler.Config config = newConfig();
		config.setDynamicSequenceName(
			TypedConfiguration.createConfigItemForImplementationClass(ContextAwareSequenceName.class));
		
		NumberHandler handler = getInstance(config);
		assertEquals("1", handler.generateId("A"));
		assertEquals("2", handler.generateId("A"));
		
		assertEquals("1", handler.generateId("B"));
		assertEquals("2", handler.generateId("B"));
	}
	
	public void testGeneratePlainNumber() throws GenerateNumberException {
		ConfiguredNumberHandler.Config config = newConfig();
		config.setPattern("%NUMBER%");
		config.setNumberPattern("0000");

		NumberHandler handler = getInstance(config);
	    assertEquals("0001", handler.generateId(null));
        assertEquals("0002", handler.generateId("ABC"));
	}

	public void testGenerateDateNumber() throws Exception {
		ConfiguredNumberHandler.Config config = newConfig();
		config.setPattern("%DATE%/%NUMBER%");
		config.setNumberPattern("0000");
		config.setDatePattern("yyyy-MM-dd");

		NumberHandler handler = getInstance(config);
		String datePart = CalendarUtil.newSimpleDateFormat(config.getDatePattern()).format(new Date());
        // date/number handler
        assertEquals(datePart + "/0001", handler.generateId(null));
        assertEquals(datePart + "/0002", handler.generateId("ABC"));
	}

	public void testGenerateContextNumber() throws Exception {
		ConfiguredNumberHandler.Config config = newConfig();
		config.setPattern("%OBJECT%/%DATE%/%NUMBER%");
		config.setNumberPattern("100000");
		config.setDatePattern("yyyy-MM-dd");
		config.setDynamicSequenceName(
			TypedConfiguration.createConfigItemForImplementationClass(ContextAwareSequenceName.class));

		NumberHandler handler = getInstance(config);
		String datePart = CalendarUtil.newSimpleDateFormat(config.getDatePattern()).format(new Date());
		assertEquals("ABC/" + datePart + "/100001", handler.generateId("ABC"));
		assertEquals("ABC/" + datePart + "/100002", handler.generateId("ABC"));
        
		assertEquals("DEF/" + datePart + "/100001", handler.generateId("DEF"));
		assertEquals("DEF/" + datePart + "/100002", handler.generateId("DEF"));
	}
	
	private Config newConfig() {
		Config result = TypedConfiguration.newConfigItem(ConfiguredNumberHandler.Config.class);
		result.setName("test");
		return result;
	}

	/**
	 * Suite of tests.
	 */
    public static Test suite () {
		TestFactory factory = ServiceTestSetup.createStarterFactory(NumberHandlerFactory.Module.INSTANCE);
		return KBSetup.getKBTest(TestConfiguredNumberHandler.class, factory);
    }
	
	public static class NumberHandlerForTest implements NumberHandler {

		private static final AtomicInteger COUNTER = new AtomicInteger(0);

		@Override
		public Object generateId(Object context) throws GenerateNumberException {
			return Integer.valueOf(COUNTER.getAndIncrement());
		}

	}
}
