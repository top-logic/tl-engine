/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.tool.export;

import java.io.IOException;
import java.io.OutputStream;

import junit.framework.Test;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.TestFactory;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.base.services.CurrencySystem;
import com.top_logic.basic.Logger;
import com.top_logic.basic.XMLProperties;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.WebFolderFactory;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.knowledge.wrap.currency.Currency;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.export.Export;
import com.top_logic.tool.export.ExportHandler;
import com.top_logic.tool.export.ExportHandlerRegistry;
import com.top_logic.tool.export.ExportRegistry;
import com.top_logic.tool.export.ExportRegistryFactory;
import com.top_logic.tool.export.ExportResult;
import com.top_logic.tool.export.ExportRun;
import com.top_logic.tool.export.InMemoryExportRegistry;
import com.top_logic.util.TLContext;

/**
 * Base class for test cases testing export.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractExportTest extends BasicTestCase {

    public static final String LONG_WORD_EXPORT_HANDLER_ID = "TestedLongWordHandler";
    public static final String SHORT_WORD_EXPORT_HANDLER_ID = "TestedShortWordHandler";
    public static final String SHORT_PPT_EXPORT_HANDLER_ID = "TestedShortPptHandler";
	public static final String EXPORT_HANDLER_ID         = "TestedExportHandler";
	public static final String WAITING_EXPORT_HANDLER_ID = "TestedWaitingExportHandler";
	public static final String LONG_WAITING_EXPORT_HANDLER_ID = "TestedLongWaitingExportHandler";
	public static final String FAILING_EXPORT_HANDLER_ID = "TestedFailingExportHandler";
	public static final String ERROR_EXPORT_HANDLER_ID = "TestedErrorExportHandler";
	public static final String USER_EXPORT_HANDLER_ID = "TestedUserExportHandler";

	private ExportRegistry reg;

	/**
	 * modules which are needed by all extending classes.
	 * <ol>
	 * <li>{@link WebFolderFactory} and {@link WrapperFactory} are needed by {@link ExportRun}</li>
	 * <li>{@link ExportHandlerRegistry} is needed to get handler for export</li>
	 * <li>{@link ExportRegistryFactory} is needed to get export registry</li>
	 * </ol>
	 */
	private static BasicRuntimeModule<?>[] additionalModules = new BasicRuntimeModule<?>[] {
		CurrencySystem.Module.INSTANCE,
		ExportHandlerRegistry.Module.INSTANCE,
		ExportRegistryFactory.Module.INSTANCE,
		WebFolderFactory.Module.INSTANCE
	};

	public static Test suite(Class<? extends AbstractExportTest> aClass) {
		return suite(aClass, (TestFactory) null);
	}
	
	public static Test suite(Class<? extends AbstractExportTest> aClass, TestFactory testFactory) {
		return suite(aClass, testFactory, BasicRuntimeModule.NO_MODULES);
	}

	public static Test suite(Class<? extends AbstractExportTest> aClass, BasicRuntimeModule<?>... modules) {
		return suite(aClass, null, modules);
	}

	public static Test suite(Class<? extends AbstractExportTest> aClass, TestFactory testFactory,
			BasicRuntimeModule<?>... modules) {
		return PersonManagerSetup.createPersonManagerSetup(aClass, createStarterFactory(testFactory, modules));
	}
	
	protected static TestFactory createStarterFactory(TestFactory innerFactory, BasicRuntimeModule<?>... modules) {
		BasicRuntimeModule<?>[] necessaryModules;
		if (modules.length == 0) {
			necessaryModules = additionalModules;
		} else {
			BasicRuntimeModule<?>[] extendedArray =
				new BasicRuntimeModule<?>[modules.length + additionalModules.length];
			// copy modules which are needed by all modules
			System.arraycopy(additionalModules, 0, extendedArray, 0, additionalModules.length);
			// copy modules needed by that specific class
			System.arraycopy(modules, 0, extendedArray, additionalModules.length, modules.length);
			necessaryModules = extendedArray;
		}
		if (innerFactory == null) {
			return ServiceTestSetup.createStarterFactoryForModules(necessaryModules);
		} else {
			return ServiceTestSetup.createStarterFactoryForModules(innerFactory, necessaryModules);
		}

	}

	@Override
	protected void setUp() throws Exception {
	    super.setUp();
	    XMLProperties.getInstance();
	}

	@Override
	protected void tearDown() throws Exception {
	    this.tearDownExports();
	    super.tearDown();
	}

	protected final ExportRegistry getExportRegistry() {
	    if (reg == null) {
	        reg = createExportRegistry();
	    }
	    return reg;
	}

	protected ExportRegistry createExportRegistry() {
	    return InMemoryExportRegistry.getInstance();
	}

	protected void tearDownExports() throws Exception {
	    ((InMemoryExportRegistry) getExportRegistry()).clear();
	}

	protected final Export getExport(String aHandlerID) throws Exception {
	    return this.getExportRegistry().getExport(aHandlerID, this.getModel());
	}

	protected final Wrapper getModel() throws Exception {

	    return Currency.getSystemCurrency();
	}

	protected final ExportHandler getExportHandler(String anID) {
		return ExportHandlerRegistry.getInstance().getHandler(anID);
	}

	public static class TestedLongWordHandler extends TestedExportHandler {
        public TestedLongWordHandler(String anID) {
			super(anID, 0, false, "TESTWORD", "LONG", false, false);
		}
    }

	public static class TestedShortWordHandler extends TestedExportHandler {
		public TestedShortWordHandler(String anID) {
			super(anID, 0, false, "TESTWORD", "SHORT", false, false);
		}
    }

	public static class TestedShortPptHandler extends TestedExportHandler {
		public TestedShortPptHandler(String anID) {
			super(anID, 0, false, "TESTPPT", "SHORT", false, false);
		}
    }

	public static class TestedLongWaitingExportHandler extends TestedExportHandler {
		public TestedLongWaitingExportHandler(String anID) {
			super(anID, 5000, false, false);
		}
	}

	public static class TestedWaitingExportHandler extends TestedExportHandler {
		public TestedWaitingExportHandler(String anID) {
			super(anID, 100, false, false);
		}
    }

    public static class TestedErrorExportHandler extends TestedExportHandler {
		public TestedErrorExportHandler(String anID) {
			super(anID, 100, false, true);
		}
    }

    public static class TestedFailingExportHandler extends TestedExportHandler {
		public TestedFailingExportHandler(String anID) {
			super(anID, 0, true, false);
		}
    }

    public static class TestedUserExportHandler extends TestedExportHandler {
		public TestedUserExportHandler(String anID) {
			super(anID, 0, false, "TESTWORD", "SHORT", false, true);
		}
    }

    public static class TestedExportHandler implements ExportHandler {

    	private final String  id;
    	private final long    wait;
		private final boolean shouldFail;
		private final boolean throwException;
		private final String  technology;
		private final String  duration;
		private final boolean expectUser;

		public TestedExportHandler(String anID) {
			this(anID, 0, false, false);
		}

		@Override
		public boolean isPersonalized() {
			return this.expectUser;
		}

		@Override
		public String getExportDuration() {
		    return this.duration;
		}

		@Override
		public String getExportTechnology() {
		    return this.technology;
		}

		public TestedExportHandler(String anID, long timeToWait, boolean shouldFail, boolean exception) {
			this(anID, timeToWait, shouldFail, "TESTWORD", "SHORT", exception, false);
		}

		public TestedExportHandler(String anID, long timeToWait, boolean shouldFail, String technology, String duration, boolean exception, boolean expectUser) {
            this.wait = timeToWait;
            this.shouldFail = shouldFail;
            this.technology = technology;
            this.duration   = duration;
            this.throwException = exception;
            this.expectUser = expectUser;
            this.id = anID;
        }

		@Override
		public boolean canExport(Object aModel) {
			return true;
		}

		@Override
		public String getExportHandlerID() {
			return this.id;
		}

		@Override
		public void exportObject(Object aModel, ExportResult aResult) {

			if (this.wait > 0) {
				try {
					Thread.sleep(wait);
				} catch (InterruptedException e) {
					if (Logger.isDebugEnabled(TestedExportHandler.class)) {
						Logger.debug("Interrupted while waiting", TestedExportHandler.class);
					}
				}
			}

			if (this.shouldFail) {
				aResult.setFailureKey(ResKey.forTest("test.export.failed"));
				return;
			}
			else if (this.throwException) {
				throw new RuntimeException("test.export.error");
			}
			else {
				try {
					aResult.setFileExtension("txt");
					aResult.setFileDisplaynameKey(ResKey.forTest("TestExportFile"));
					OutputStream theOut = aResult.getOutputStream();
					String theResult = "This is the successful export result!";
					theOut.write(theResult.getBytes());

					TLContext context = TLContext.getContext();
					if (this.expectUser) {
						assertSame(PersonManager.getManager().getRoot(), context.getCurrentPersonWrapper());
					}

				} catch (IOException e) {
					aResult.setFailureKey(ResKey.message(ResKey.forTest("test.export.failed"), e));
				}
			}
		}

		@Override
		public BoundCommandGroup getExportCommandGroup() {
			return SimpleBoundCommandGroup.EXPORT;
		}

		@Override
		public BoundCommandGroup getReadCommandGroup() {
			return SimpleBoundCommandGroup.EXPORT;
		}
	}
}
