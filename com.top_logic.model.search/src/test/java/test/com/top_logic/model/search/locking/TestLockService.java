/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.locking;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.TestFactory;
import test.com.top_logic.basic.ThreadContextSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.base.locking.Lock;
import com.top_logic.base.locking.LockService;
import com.top_logic.base.locking.service.ConfiguredLockService;
import com.top_logic.base.locking.strategy.LockStrategy;
import com.top_logic.base.locking.token.I18NConstants;
import com.top_logic.base.locking.token.Token;
import com.top_logic.base.locking.token.Token.Kind;
import com.top_logic.base.locking.token.TokenService;
import com.top_logic.basic.DateUtil;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;
import com.top_logic.util.model.ModelService;

/**
 * Test case for {@link ConfiguredLockService}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestLockService extends BasicTestCase {

	private static final String OPERATION_EDIT_VALUES = "editValues";

	private static final String OPERATION_EDIT_STRUCTURE = "editStructure";

	private static Map<String, ConfigurationDescriptor> descriptors() {
		return Collections.singletonMap("service",
			TypedConfiguration.getConfigurationDescriptor(ConfiguredLockService.Config.class));
	}

	private LockService _lockService;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		@SuppressWarnings("unchecked")
		PolymorphicConfiguration<LockService> config =
			(PolymorphicConfiguration<LockService>) ConfigurationReader.readContent(new AssertProtocol(),
				descriptors(),
				content("TestLockService.config.xml"));

		InstantiationContext context = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY;
		_lockService = context.getInstance(config);
	}

	@Override
	protected void tearDown() throws Exception {
		_lockService = null;
		super.tearDown();
	}

	public void testLockA() throws ConfigurationException {
		TLObject obj = createA();

		Lock lock = _lockService.acquireLock(OPERATION_EDIT_VALUES, obj);
		assertNotNull(lock);
		assertTrue(lock.isStateAcquired());
		assertTrue(lock.check());
		assertTrue(lock.renew(1000));

		try {
			_lockService.acquireLock(OPERATION_EDIT_VALUES, obj);
			fail("Expected failure.");
		} catch (TopLogicException ex) {
			assertEquals(I18NConstants.ERROR_LOCK_CONFLICT, ex.getErrorKey());
		}

		lock.unlock();
		assertFalse(lock.isStateAcquired());
		assertFalse(lock.check());

		Lock other = _lockService.acquireLock(OPERATION_EDIT_VALUES, obj);
		assertTrue(other.isStateAcquired());
		assertTrue(other.check());
		other.unlock();
	}

	public void testDefaultTimeout() throws ConfigurationException {
		assertEquals(10 * DateUtil.SECOND_MILLIS, _lockService.getLockTimeout());
		
		TLObject s = createS();
		Lock lock = _lockService.createLock(OPERATION_EDIT_VALUES, s);
		assertEquals(10 * DateUtil.SECOND_MILLIS, lock.getLockTimeout());
	}

	public void testTimeout() throws ConfigurationException, InterruptedException {
		TLObject obj = createA();

		Lock lock = _lockService.acquireLock(OPERATION_EDIT_VALUES, obj);
		assertNotNull(lock);
		assertTrue(lock.isStateAcquired());
		assertTrue(lock.check());
		assertTrue(lock.renew());
		assertTrue(lock.check());

		Thread.sleep(1075);

		assertFalse(lock.check());
		assertFalse(lock.renew(500));

		assertEquals(500, lock.getLockTimeout());
	}

	private TLObject createA() throws ConfigurationException {
		return create(type("TestLockService:A"));
	}

	public void testLockB() throws ConfigurationException {
		TLObject obj = create(type("TestLockService:B"));

		Lock lock = _lockService.acquireLock(OPERATION_EDIT_VALUES, obj);
		assertNotNull(lock);
		assertTrue(lock.isStateAcquired());
		assertTrue(lock.check());
		lock.unlock();
	}

	public void testLockTree() throws ConfigurationException {
		TLObject root = createS();
		TLObject n1 = createS(root);
		TLObject n2 = createS(root);
		TLObject n21 = createS(n2);

		Lock lock1 = _lockService.acquireLock(OPERATION_EDIT_VALUES, n1);
		assertNotNull(lock1);
		assertTrue(lock1.isStateAcquired());
		assertTrue(lock1.check());

		Lock lock2 = _lockService.acquireLock(OPERATION_EDIT_VALUES, n2);
		assertNotNull(lock2);
		assertTrue(lock2.isStateAcquired());
		assertTrue(lock2.check());

		Lock lock3 = _lockService.acquireLock(OPERATION_EDIT_VALUES, n21);
		assertNotNull(lock3);
		assertTrue(lock3.isStateAcquired());
		assertTrue(lock3.check());

		lock2.unlock();

		Lock lock4 = _lockService.createLock(OPERATION_EDIT_STRUCTURE, n2);
		assertNotNull(lock4);
		assertFalse(lock4.tryLock());

		lock3.unlock();

		assertTrue(lock4.tryLock());

		assertFalse(lock3.tryLock());

		Lock lock5 = _lockService.createLock(OPERATION_EDIT_STRUCTURE, n21);
		assertFalse(lock5.tryLock());

		Lock lock6 = _lockService.createLock(OPERATION_EDIT_VALUES, root);
		assertNotNull(lock6);
		assertTrue(lock6.tryLock());

		lock6.unlock();

		Lock lock7 = _lockService.createLock(OPERATION_EDIT_STRUCTURE, root);
		assertNotNull(lock7);
		assertFalse(lock7.tryLock());

		lock1.unlock();

		Lock lock8 = _lockService.createLock(OPERATION_EDIT_STRUCTURE, n1);
		assertNotNull(lock8);
		assertTrue(lock8.tryLock());

		lock1.unlock();
		lock2.unlock();
		lock3.unlock();
		lock4.unlock();
		lock5.unlock();
		lock6.unlock();
		lock7.unlock();
		lock8.unlock();
	}

	private TLObject createS(TLObject parent) throws ConfigurationException {
		TLObject result = createS();
		Collection<?> oldChildren = (Collection<?>) parent.tValueByName(StructuredElement.CHILDREN_ATTR);
		List<Object> newChildren = oldChildren == null ? new ArrayList<>() : new ArrayList<>(oldChildren);
		newChildren.add(result);
		parent.tUpdateByName(StructuredElement.CHILDREN_ATTR, newChildren);
		return result;
	}

	private TLObject createS() throws ConfigurationException {
		return create(type("TestLockService:S"));
	}

	private TLObject create(TLClass type) {
		return ModelService.getInstance().getFactory().createObject(type);
	}

	private TLClass type(String name) throws ConfigurationException {
		return (TLClass) TLModelUtil.findType(name);
	}

	static ClassRelativeBinaryContent content(String name) {
		return new ClassRelativeBinaryContent(TestLockService.class, name);
	}

	private static ThreadContextSetup setupModel(Test test) {
		return new ThreadContextSetup(test) {
			@Override
			protected void doSetUp() throws Exception {
				try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
					DynamicModelService.extendModel(new AssertProtocol(), ModelService.getApplicationModel(),
						ModelService.getInstance().getFactory(),
						content("TestLockService.model.xml"));
					tx.commit();
				}
			}
	
			@Override
			protected void doTearDown() throws Exception {
				// Ignore.
			}
		};
	}

	public static class CustomObject {

		private final String tokenName;

		/**
		 * Creates a {@link CustomObject}.
		 */
		public CustomObject(String tokenName) {
			super();
			this.tokenName = tokenName;
		}

		public String getTokenName() {
			return tokenName;
		}

	}

	public static class CustomStrategy implements LockStrategy<CustomObject> {

		@Override
		public List<Token> createTokens(CustomObject model) {
			return Collections.singletonList(Token.newGlobalToken(Kind.EXCLUSIVE, model.getTokenName()));
		}

	}

	public static Test suite() {
		return KBSetup.getKBTest(TestLockService.class, new TestFactory() {

			@Override
			public Test createSuite(Class<? extends TestCase> testCase, String suiteName) {
				TestSuite suite = new TestSuite(testCase);
				suite.setName(suiteName);
				return ServiceTestSetup.createSetup(
					setupModel(suite),
					ModelService.Module.INSTANCE, TokenService.Module.INSTANCE);
			}
		});
	}
}
