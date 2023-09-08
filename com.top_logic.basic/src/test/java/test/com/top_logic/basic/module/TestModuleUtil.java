/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.module;

import static test.com.top_logic.basic.BasicTestCase.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ExpectedFailure;
import test.com.top_logic.basic.ReflectionUtils;

import com.top_logic.basic.Logger;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.module.ModuleUtil.ModuleContext;
import com.top_logic.basic.module.RestartException;
import com.top_logic.basic.module.RuntimeModule;
import com.top_logic.basic.thread.ThreadContextManager;

/**
 * Test case for {@link ModuleUtil}.
 * 
 * Implemented Modules
 * 
 * Illegal dependency:
 * <ul>
 * <li>A cyclic dependency : A -&gt; A</li> 
 * <li>B depends on cyclic dependency: B -&gt; A</li>
 * <li>C,D cyclic indirect dependency: C &lt;-&gt; D</li> 
 * <li>E depends on indirect cyclic dependency : E -&gt; D</li>
 * </ul>
 * 
 * Legal dependency:
 * 
 * 			H -> G -> F
 * 			H ------> F
 * 	   I -> H
 *     I -----------> F	
 *     J ------> G	
 * 
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestModuleUtil extends AbstractModuleTest {
	
	/**
	 * Base class for testing {@link ManagedClass}es.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static abstract class TestingManagedClass extends ManagedClass {
		
		private Object state;
		
		protected TestingManagedClass() {
			super();
			this.active = true;
		}

		private boolean active;

		@Override
		protected void startUp() {
			assertFalse(isStarted());
			super.startUp();
			assertFalse(isStarted());
		}

		@Override
		protected void shutDown() {
			assertFalse(isStarted());
			setState(null);
			this.active = false;
			super.shutDown();
			assertFalse(isStarted());
		}
		
		/**
		 * Whether the test implementation has been started.
		 */
		public boolean isActive() {
			return active;
		}

		public void setState(Object state) {
			if (!isActive()) {
				throw new IllegalStateException("not active");
			}
			this.state = state;
		}

		public Object getState() {
			return state;
		}
		
	}
	
	public static abstract class TestingRuntimeModule<M extends ManagedClass> extends BasicRuntimeModule<M> {
		// Pure base class.
	}
	
	/**
	 * {@link ManagedClass} with cyclic dependency on itself.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static final class A extends TestingManagedClass {
		protected A() {
		}

		/** {@link RuntimeModule} for {@link TestModuleUtil.A}. */
		public static class Module extends TestingRuntimeModule<A> {
			/** Singleton instance of {@link TestModuleUtil.A.Module}. */
			public static final A.Module INSTANCE = new Module();
			
			private static final Collection<? extends Class<? extends BasicRuntimeModule<?>>> DEPENDENCIES =
				Arrays.asList(A.Module.class);

			@Override
			public Collection<? extends Class<? extends BasicRuntimeModule<?>>> getDependencies() {
				return DEPENDENCIES;
			}

			@Override
			public Class<A> getImplementation() {
				return A.class;
			}

			@Override
			protected A newImplementationInstance() throws ModuleException {
				return new A();
			}
		}
	}
	
	/**
	 * {@link ManagedClass} with dependency on broken class {@link TestModuleUtil.A}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static final class B extends TestingManagedClass {
		protected B() {
		}

		/** {@link RuntimeModule} for {@link TestModuleUtil.B}. */
		public static class Module extends TestingRuntimeModule<B> {
			/** Singleton instance of {@link TestModuleUtil.B.Module}. */
			public static final B.Module INSTANCE = new Module();
			
			private static final Collection<? extends Class<? extends BasicRuntimeModule<?>>> DEPENDENCIES =
				Arrays.asList(A.Module.class);

			@Override
			public Collection<? extends Class<? extends BasicRuntimeModule<?>>> getDependencies() {
				return DEPENDENCIES;
			}

			@Override
			public Class<B> getImplementation() {
				return B.class;
			}

			@Override
			protected B newImplementationInstance() throws ModuleException {
				return new B();
			}
		}
	}
	
	/**
	 * {@link ManagedClass} with cyclic dependency on {@link TestModuleUtil.D}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static final class C extends TestingManagedClass {
		protected C() {
		}

		/** {@link RuntimeModule} for {@link TestModuleUtil.C}. */
		public static class Module extends TestingRuntimeModule<C> {
			/** Singleton instance of {@link TestModuleUtil.C.Module}. */
			public static final C.Module INSTANCE = new Module();
			
			private static final Collection<? extends Class<? extends BasicRuntimeModule<?>>> DEPENDENCIES =
				Arrays.asList(D.Module.class);

			@Override
			public Collection<? extends Class<? extends BasicRuntimeModule<?>>> getDependencies() {
				return DEPENDENCIES;
			}

			@Override
			public Class<C> getImplementation() {
				return C.class;
			}

			@Override
			protected C newImplementationInstance() throws ModuleException {
				return new C();
			}
		}
	}
	
	/**
	 * {@link ManagedClass} with cyclic dependency on {@link TestModuleUtil.C}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static final class D extends TestingManagedClass {
		protected D() {
		}

		/** {@link RuntimeModule} for {@link TestModuleUtil.D}. */
		public static class Module extends TestingRuntimeModule<D> {
			/** Singleton instance of {@link TestModuleUtil.D.Module}. */
			public static final D.Module INSTANCE = new Module();
			
			private static final Collection<? extends Class<? extends BasicRuntimeModule<?>>> DEPENDENCIES =
				Arrays.asList(C.Module.class);

			@Override
			public Collection<? extends Class<? extends BasicRuntimeModule<?>>> getDependencies() {
				return DEPENDENCIES;
			}

			@Override
			public Class<D> getImplementation() {
				return D.class;
			}

			@Override
			protected D newImplementationInstance() throws ModuleException {
				return new D();
			}
		}
	}
	
	/**
	 * {@link ManagedClass} with dependency on class with cyclic dependency {@link TestModuleUtil.D}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static final class E extends TestingManagedClass {
		protected E() {
		}

		/** {@link RuntimeModule} for {@link TestModuleUtil.E}. */
		public static class Module extends TestingRuntimeModule<E> {
			/** Singleton instance of {@link TestModuleUtil.E.Module}. */
			public static final E.Module INSTANCE = new Module();
			
			private static final Collection<? extends Class<? extends BasicRuntimeModule<?>>> DEPENDENCIES =
				Arrays.asList(D.Module.class);

			@Override
			public Collection<? extends Class<? extends BasicRuntimeModule<?>>> getDependencies() {
				return DEPENDENCIES;
			}

			@Override
			public Class<E> getImplementation() {
				return E.class;
			}

			@Override
			protected E newImplementationInstance() throws ModuleException {
				return new E();
			}
		}
	}
	
	/**
	 * {@link ManagedClass} with dependency {@link ThreadContextManager} .
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static final class K extends TestingManagedClass {

		boolean _createInteraction;

		boolean _startupInteraction;

		boolean _shutdownInteraction;

		protected K() {
			assertTrue("Module depends on thread context", ThreadContextManager.Module.INSTANCE.isActive());
			_createInteraction = ThreadContextManager.getInteraction() != null;
		}

		@Override
		protected void startUp() {
			super.startUp();
			_startupInteraction = ThreadContextManager.getInteraction() != null;
		}

		@Override
		protected void shutDown() {
			_shutdownInteraction = ThreadContextManager.getInteraction() != null;
			super.shutDown();
		}

		/** {@link RuntimeModule} for {@link TestModuleUtil.K}. */
		public static class Module extends TestingRuntimeModule<K> {
			/** Singleton instance of {@link TestModuleUtil.K.Module}. */
			public static final K.Module INSTANCE = new Module();

			private static final Collection<? extends Class<? extends BasicRuntimeModule<?>>> DEPENDENCIES =
				Arrays.asList(ThreadContextManager.Module.class);

			@Override
			public Collection<? extends Class<? extends BasicRuntimeModule<?>>> getDependencies() {
				return DEPENDENCIES;
			}

			@Override
			public Class<K> getImplementation() {
				return K.class;
			}

			@Override
			protected K newImplementationInstance() throws ModuleException {
				return new K();
			}
		}

		/**
		 * Module depending on {@link TestModuleUtil.K.Module} to test inherited dependency of
		 * {@link ThreadContextManager}.
		 */
		public static class Module2 extends TestingRuntimeModule<K> {
			/** Singleton instance of {@link TestModuleUtil.K.Module2}. */
			public static final K.Module2 INSTANCE = new Module2();

			private static final Collection<? extends Class<? extends BasicRuntimeModule<?>>> DEPENDENCIES =
				Arrays.asList(K.Module.class);

			@Override
			public Collection<? extends Class<? extends BasicRuntimeModule<?>>> getDependencies() {
				return DEPENDENCIES;
			}

			@Override
			public Class<K> getImplementation() {
				return K.class;
			}

			@Override
			protected K newImplementationInstance() throws ModuleException {
				return new K();
			}
		}
	}

	public void testThreadContextDependency() throws IllegalArgumentException, ModuleException {
		if (ThreadContextManager.Module.INSTANCE.isActive()) {
			assertNull("Test does not with existing interaction.", ThreadContextManager.getInteraction());
		}
		moduleUtil.startUp(K.Module.INSTANCE);
		K implementation = K.Module.INSTANCE.getImplementationInstance();
		assertTrue(implementation._createInteraction);
		assertTrue(implementation._startupInteraction);
		assertFalse(implementation._shutdownInteraction);
		moduleUtil.shutDown(K.Module.INSTANCE);
		assertTrue(implementation._shutdownInteraction);

		moduleUtil.startUp(K.Module2.INSTANCE);
		K implementationOfInheritedModule = K.Module2.INSTANCE.getImplementationInstance();
		assertTrue(implementationOfInheritedModule._createInteraction);
		assertTrue(implementationOfInheritedModule._startupInteraction);
		assertFalse(implementationOfInheritedModule._shutdownInteraction);
		moduleUtil.shutDown(K.Module2.INSTANCE);
		assertTrue(implementationOfInheritedModule._shutdownInteraction);
	}

	/**
	 * Test detecting dependency cycle in {@link TestModuleUtil.A}.
	 */
	public void testCyclicDependency1() throws ModuleException {
		try {
			moduleUtil.startUp(A.Module.INSTANCE);
			fail("Must not start due to cyclic dependency.");
		} catch (IllegalArgumentException ex) {
			// Expected.
			Logger.info(ex.getMessage(), TestModuleUtil.class);
		}
	}		
	
	/**
	 * Test detecting dependency cycle in {@link TestModuleUtil.B}.
	 */
	public void testCyclicDependency2() throws ModuleException {
		try {
			moduleUtil.startUp(B.Module.INSTANCE);
			fail("Must not start due to cyclic dependency.");
		} catch (IllegalArgumentException ex) {
			// Expected.
			Logger.info(ex.getMessage(), TestModuleUtil.class);
			assertFalse("B is not part of the cycle: " + ex.getMessage(), 
				ex.getMessage().contains(B.class.getName()));
		}
	}
	
	/**
	 * Test detecting dependency cycle in {@link TestModuleUtil.C}.
	 */
	public void testCyclicDependency3() throws ModuleException {
		try {
			moduleUtil.startUp(C.Module.INSTANCE);
			fail("Must not start due to cyclic dependency.");
		} catch (IllegalArgumentException ex) {
			// Expected.
			Logger.info(ex.getMessage(), TestModuleUtil.class);
		}
	}
	
	/**
	 * Test detecting dependency cycle in {@link TestModuleUtil.E}.
	 */
	public void testCyclicDependency4() throws ModuleException {
		try {
			moduleUtil.startUp(E.Module.INSTANCE);
			fail("Must not start due to cyclic dependency.");
		} catch (IllegalArgumentException ex) {
			// Expected.
			Logger.info(ex.getMessage(), TestModuleUtil.class);
			assertFalse("E is not part of the cycle: " + ex.getMessage(), 
				ex.getMessage().contains(E.class.getName()));
		}
	}
	
	public static final class G extends TestingManagedClass {
		
		protected G() {
		}

		public static final class Module extends TestingRuntimeModule<G> {
			
			public static final G.Module INSTANCE = new G.Module();

			private static final Collection<? extends Class<? extends BasicRuntimeModule<?>>> DEPENDENCIES =
				Arrays.asList(F.Module.class);
				
			@Override
			public Collection<? extends Class<? extends BasicRuntimeModule<?>>> getDependencies() {
				return DEPENDENCIES;
			}

			@Override
			public Class<G> getImplementation() {
				return G.class;
			}

			@Override
			protected G newImplementationInstance() throws ModuleException {
				return new G();
			}
			
		}
	}
	
	public static final class J extends TestingManagedClass {
		
		public static final class Module extends TestingRuntimeModule<J> {
			
			public static final J.Module INSTANCE = new J.Module();
			private static final Collection<? extends Class<? extends BasicRuntimeModule<?>>> DEPENDENCIES =
				Arrays.asList(G.Module.class);
			
			@Override
			public Collection<? extends Class<? extends BasicRuntimeModule<?>>> getDependencies() {
				return DEPENDENCIES;
			}
			
			@Override
			public Class<J> getImplementation() {
				return J.class;
			}
			
			@Override
			protected J newImplementationInstance() throws ModuleException {
				return new J();
			}
			
		}
	}
	
	public static final class I extends TestingManagedClass {
		
		protected I() {
		}

		public static final class Module extends TestingRuntimeModule<I> {
			
			public static final I.Module INSTANCE = new I.Module();
			private static final Collection<? extends Class<? extends BasicRuntimeModule<?>>> DEPENDENCIES =
				Arrays.asList(H.Module.class, F.Module.class);
			
			@Override
			public Collection<? extends Class<? extends BasicRuntimeModule<?>>> getDependencies() {
				return DEPENDENCIES;
			}
			
			@Override
			public Class<I> getImplementation() {
				return I.class;
			}
			
			@Override
			protected I newImplementationInstance() throws ModuleException {
				return new I();
			}
			
		}
	}
	
	public static final class H extends TestingManagedClass {
		
		protected H() {
		}

		public static final class Module extends TestingRuntimeModule<H> {
			public static final H.Module INSTANCE = new H.Module();
			
			private static final Collection<? extends Class<? extends BasicRuntimeModule<?>>> DEPENDENCIES =
				Arrays.asList(G.Module.class, F.Module.class);
				
			@Override
			public Collection<? extends Class<? extends BasicRuntimeModule<?>>> getDependencies() {
				return DEPENDENCIES;
			}
			
			@Override
			public Class<H> getImplementation() {
				return H.class;
			}
			
			@Override
			protected H newImplementationInstance() throws ModuleException {
				return new H();
			}
			
		}
	}
	
	public static final class F extends TestingManagedClass {
		
		protected F() {
		}

		public static final class Module extends TestingRuntimeModule<F> {
			public static final F.Module INSTANCE = new F.Module();

			@Override
			public Collection<? extends Class<? extends BasicRuntimeModule<?>>> getDependencies() {
				return RuntimeModule.NO_DEPENDENCIES;
			}
			
			@Override
			public Class<F> getImplementation() {
				return F.class;
			}
			
			@Override
			protected F newImplementationInstance() throws ModuleException {
				return new F();
			}
			
		}
	}
	
	public static final class FExtension extends TestingManagedClass {

		protected FExtension() {
		}

		public static final class Module extends TestingRuntimeModule<FExtension> {
			public static final FExtension.Module INSTANCE = new FExtension.Module();

			@Override
			public Collection<? extends Class<? extends BasicRuntimeModule<?>>> getDependencies() {
				return NO_DEPENDENCIES;
			}

			@Override
			public Class<? extends BasicRuntimeModule<?>> getExtendedService() {
				return F.Module.class;
			}

			@Override
			public Class<FExtension> getImplementation() {
				return FExtension.class;
			}

			@Override
			protected FExtension newImplementationInstance() throws ModuleException {
				return new FExtension();
			}

		}
	}

	public static final class L extends TestingManagedClass {

		protected L() {
		}

		public static final class Module extends TestingRuntimeModule<L> {
			public static final L.Module INSTANCE = new L.Module();

			Collection<? extends Class<? extends BasicRuntimeModule<?>>> DEPENDENCIES = NO_DEPENDENCIES;

			@Override
			public Collection<? extends Class<? extends BasicRuntimeModule<?>>> getDependencies() {
				return DEPENDENCIES;
			}

			@Override
			public Class<L> getImplementation() {
				return L.class;
			}

			@Override
			protected L newImplementationInstance() throws ModuleException {
				return new L();
			}

		}
	}

	public void testDependencies() {
		Iterator<? extends BasicRuntimeModule<?>> dependenciesOfI =
			moduleUtil.getDependencies(I.Module.INSTANCE).iterator();
		assertSame(dependenciesOfI.next(), F.Module.INSTANCE);
		assertSame(dependenciesOfI.next(), G.Module.INSTANCE);
		assertSame(dependenciesOfI.next(), H.Module.INSTANCE);
		assertSame(dependenciesOfI.next(), I.Module.INSTANCE);
		assertFalse(dependenciesOfI.hasNext());
		
		Iterator<? extends BasicRuntimeModule<?>> dependenciesOfH = moduleUtil.getDependencies(H.Module.INSTANCE).iterator();
		assertSame(dependenciesOfH.next(), F.Module.INSTANCE);
		assertSame(dependenciesOfH.next(), G.Module.INSTANCE);
		assertSame(dependenciesOfH.next(), H.Module.INSTANCE);
		assertFalse(dependenciesOfH.hasNext());
		
		Iterator<? extends BasicRuntimeModule<?>> dependenciesOfG = moduleUtil.getDependencies(G.Module.INSTANCE).iterator();
		assertSame(dependenciesOfG.next(), F.Module.INSTANCE);
		assertSame(dependenciesOfG.next(), G.Module.INSTANCE);
		assertFalse(dependenciesOfG.hasNext());
		
		Iterator<? extends BasicRuntimeModule<?>> dependenciesOfF = moduleUtil.getDependencies(F.Module.INSTANCE).iterator();
		assertSame(dependenciesOfF.next(), F.Module.INSTANCE);
		assertFalse(dependenciesOfF.hasNext());
	}
	
	/**
	 * Test for Ticket #3947
	 */
	public void testRestart_3947() throws IllegalArgumentException, ModuleException {
		moduleUtil.startUp(G.Module.INSTANCE);
		assertTrue(G.Module.INSTANCE.isActive());
		assertTrue(F.Module.INSTANCE.isActive());
		
		moduleUtil.shutDown(G.Module.INSTANCE);
		assertFalse(G.Module.INSTANCE.isActive());
		
		moduleUtil.restart(F.Module.INSTANCE, null);
		assertTrue("Ticket #3947: module not restarted",F.Module.INSTANCE.isActive());
	}
	
	/**
	 * Test for Ticket #21112
	 */
	public void testRestartFailed() throws IllegalArgumentException, ModuleException {
		moduleUtil.startUp(G.Module.INSTANCE);
		assertTrue(G.Module.INSTANCE.isActive());
		assertTrue(F.Module.INSTANCE.isActive());

		try {
			moduleUtil.restart(F.Module.INSTANCE, new Runnable() {
				@Override
				public void run() {
					throw new ExpectedFailure("Test failing callback.");
				}
			});

			fail("Restart not possible");
		} catch (RestartException ex) {
			assertEquals("Test failing callback.", ex.getCause().getMessage());

			// Test restart pattern in XMLProperties#startXMLProperties(XMLPropertiesConfig)
			for (BasicRuntimeModule<?> dependent : ex.getCurrentlyStartedDependents()) {
				ModuleUtil.INSTANCE.startUp(dependent);
			}
		}
		assertTrue("Ticket #21112: Module not restarted.", F.Module.INSTANCE.isActive());
	}

	public void testShutdown() throws IllegalArgumentException, ModuleException {
		moduleUtil.startUp(F.Module.INSTANCE);
		assertTrue(F.Module.INSTANCE.isActive());
		F service = F.Module.INSTANCE.getImplementationInstance();
		assertTrue(service.isActive());
		
		moduleUtil.shutDown(F.Module.INSTANCE);
		assertFalse(F.Module.INSTANCE.isActive());
		assertFalse(service.isActive());
	}
	
	public void testShutdownInherited() throws IllegalArgumentException, ModuleException {
		moduleUtil.startUp(H.Module.INSTANCE);
		assertTrue(H.Module.INSTANCE.isActive());
		assertTrue(G.Module.INSTANCE.isActive());
		assertTrue(F.Module.INSTANCE.isActive());
		
		moduleUtil.shutDown(F.Module.INSTANCE);
		assertFalse(F.Module.INSTANCE.isActive());
		assertFalse(G.Module.INSTANCE.isActive());
		assertFalse(H.Module.INSTANCE.isActive());
	}
	
	public void testShutdownNoDependanAffect() throws IllegalArgumentException, ModuleException {
		moduleUtil.startUp(G.Module.INSTANCE);
		assertTrue(G.Module.INSTANCE.isActive());
		assertTrue(F.Module.INSTANCE.isActive());
		
		moduleUtil.shutDown(G.Module.INSTANCE);
		assertFalse(G.Module.INSTANCE.isActive());
		assertTrue(F.Module.INSTANCE.isActive());
	}
	
	public void testGetDirectDependents() throws IllegalArgumentException, ModuleException {
		moduleUtil.startUp(F.Module.INSTANCE);
		moduleUtil.startUp(G.Module.INSTANCE);
		moduleUtil.startUp(H.Module.INSTANCE);

		assertEquals(Collections.singleton(F.Module.INSTANCE), toSet(moduleUtil.getDirectDependents(null)));
		assertEquals(set(G.Module.INSTANCE, H.Module.INSTANCE),
				toSet(moduleUtil.getDirectDependents(F.Module.INSTANCE)));
		assertEquals(Collections.singleton(H.Module.INSTANCE), toSet(moduleUtil.getDirectDependents(G.Module.INSTANCE)));
		assertEquals(Collections.emptySet(), toSet(moduleUtil.getDirectDependents(H.Module.INSTANCE)));
	}
	
	public void testGetIndependant() throws IllegalArgumentException, ModuleException {
		moduleUtil.startUp(F.Module.INSTANCE);
		moduleUtil.startUp(G.Module.INSTANCE);
		moduleUtil.startUp(H.Module.INSTANCE);
		moduleUtil.startUp(I.Module.INSTANCE);
		moduleUtil.startUp(J.Module.INSTANCE);

		assertEmpty(true, moduleUtil.getIndependant(Collections.<BasicRuntimeModule<?>> emptySet()));
		assertEquals(set(F.Module.INSTANCE),
				moduleUtil.getIndependant(set(F.Module.INSTANCE, G.Module.INSTANCE, H.Module.INSTANCE)));
		assertEquals(set(G.Module.INSTANCE), moduleUtil.getIndependant(set(I.Module.INSTANCE, G.Module.INSTANCE)));
		assertEquals(set(G.Module.INSTANCE),
			moduleUtil.getIndependant(set(I.Module.INSTANCE, G.Module.INSTANCE, H.Module.INSTANCE)));
		assertEquals(set(F.Module.INSTANCE),
			moduleUtil.getIndependant(set(I.Module.INSTANCE, G.Module.INSTANCE, F.Module.INSTANCE)));
		assertEquals(set(G.Module.INSTANCE),
			moduleUtil.getIndependant(set(I.Module.INSTANCE, G.Module.INSTANCE)));
		
		assertEquals(set(H.Module.INSTANCE, J.Module.INSTANCE),
				moduleUtil.getIndependant(set(H.Module.INSTANCE, J.Module.INSTANCE)));
		assertEquals(set(G.Module.INSTANCE),
				moduleUtil.getIndependant(set(H.Module.INSTANCE, J.Module.INSTANCE, G.Module.INSTANCE)));
	}
	
	public void testContext() throws IllegalArgumentException, ModuleException {
		moduleUtil.startUp(F.Module.INSTANCE);
		assertTrue("F was started before context.", F.Module.INSTANCE.isActive());
		assertTrue(
			"Test needs that F would be started by starting G, otherwise the check that already started modules are not shut down is senseless.",
			G.Module.INSTANCE.getDependencies().contains(F.Module.class));

		try (ModuleContext context = moduleUtil.begin()) {
			assertTrue("F was started before.", F.Module.INSTANCE.isActive());
			moduleUtil.startUp(H.Module.INSTANCE);
			assertTrue("H was started.", H.Module.INSTANCE.isActive());
			assertTrue("G was started as dependency of H.", G.Module.INSTANCE.isActive());
			{
				assertTrue(
					"Test needs that H would be started by starting I, otherwise the check that already started modules are not shut down is senseless.",
					I.Module.INSTANCE.getDependencies().contains(H.Module.class));
				// Nesting
				ModuleContext nestedContext = moduleUtil.begin();
				moduleUtil.startUp(I.Module.INSTANCE);
				assertTrue("I was started.", I.Module.INSTANCE.isActive());
				nestedContext.close();
				assertFalse("Context was closed.", I.Module.INSTANCE.isActive());
				assertTrue("H is not shut down because it was started before creating context.",
					H.Module.INSTANCE.isActive());
			}
		}
		assertFalse("H was shut down by closing context.", H.Module.INSTANCE.isActive());
		assertFalse("G was shut down as it was started because H was started in same context.",
			G.Module.INSTANCE.isActive());
		assertTrue("F was started before context.", F.Module.INSTANCE.isActive());
	}

	public void testAddAllDependencies() throws IllegalArgumentException, ModuleException {
		assertFalse(H.Module.INSTANCE.isActive());
		assertFalse(F.Module.INSTANCE.isActive());
		assertFalse(G.Module.INSTANCE.isActive());

		// H -> F, G
		ModuleUtil.INSTANCE.startUp(H.Module.INSTANCE);
		assertTrue(F.Module.INSTANCE.isActive());
		assertTrue(G.Module.INSTANCE.isActive());

		// G -> F
		Collection<BasicRuntimeModule<?>> fDep = ModuleUtil.INSTANCE.getAllDependents(F.Module.INSTANCE);
		assertTrue(fDep.contains(F.Module.INSTANCE));
		assertTrue(fDep.contains(G.Module.INSTANCE));
		assertTrue(fDep.contains(H.Module.INSTANCE));
	}

	public void testExtension() {
		test.com.top_logic.basic.module.TestModuleUtil.L.Module module = L.Module.INSTANCE;

		// order of dependencies don't care because extension wins.
		module.DEPENDENCIES = Arrays.asList(G.Module.class, FExtension.Module.class);
		Iterator<? extends BasicRuntimeModule<?>> dependencies = moduleUtil.getDependencies(module).iterator();
		assertSame(F.Module.INSTANCE, dependencies.next());
		assertSame(FExtension.Module.INSTANCE, dependencies.next());
		assertSame(G.Module.INSTANCE, dependencies.next());
		assertSame(module, dependencies.next());
		assertFalse(dependencies.hasNext());

		// order of dependencies don't care because extension wins.
		module.DEPENDENCIES = Arrays.asList(FExtension.Module.class, G.Module.class);
		dependencies = moduleUtil.getDependencies(module).iterator();
		assertSame(F.Module.INSTANCE, dependencies.next());
		assertSame(FExtension.Module.INSTANCE, dependencies.next());
		assertSame(G.Module.INSTANCE, dependencies.next());
		assertSame(module, dependencies.next());
		assertFalse(dependencies.hasNext());
	}

	/**
	 * Tests of this suite.
	 */
	@SuppressWarnings("unused")
	public static Test suite() {
		Test test;
		if (false) {
			TestModuleUtil testModuleUtil = new TestModuleUtil();
			testModuleUtil.setName("");
			test = testModuleUtil;
		} else {
			test = new TestSuite(TestModuleUtil.class);
		}
		return wrap(test);
	}
	
	
	@SuppressWarnings("unchecked")
	public static <M extends ManagedClass> M installNewInstance(BasicRuntimeModule<M> module, M newInstance) {
		final Object oldInstance = ReflectionUtils.setValue(module, "impl", newInstance);
		if (newInstance == null && oldInstance != null) {
			ReflectionUtils.getValue(ModuleUtil.INSTANCE, "activeModules", Collection.class).remove(module);
		}
		if (oldInstance == null && newInstance != null) {
			ReflectionUtils.getValue(ModuleUtil.INSTANCE, "activeModules", Collection.class).add(module);
		}
		return (M) oldInstance;
	}
	
}
