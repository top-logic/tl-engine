/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.tool.boundsec;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.tool.boundsec.simple.SimpleBoundChecker;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundHelper;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.DispatchingSecurityObjectProvider;
import com.top_logic.tool.boundsec.DispatchingSecurityObjectProvider.ProviderConfig;
import com.top_logic.tool.boundsec.SecurityObjectProvider;
import com.top_logic.tool.boundsec.SecurityObjectProviderManager;
import com.top_logic.tool.boundsec.securityObjectProvider.SecurityRootObjectProvider;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.simple.SimpleBoundObject;

/**
 * Test for {@link DispatchingSecurityObjectProvider}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestDispatchingSecurtyObjectProvider extends BasicTestCase {

	public static class Test1SecurityObjectProvider implements SecurityObjectProvider {
		static BoundObject SECURITY_OBJECT = new SimpleBoundObject();

		@Override
		public BoundObject getSecurityObject(BoundChecker aChecker, Object model, BoundCommandGroup aCommandGroup) {
			return SECURITY_OBJECT;
		}

	}

	public static class Test2SecurityObjectProvider implements SecurityObjectProvider {
		static BoundObject SECURITY_OBJECT = new SimpleBoundObject();

		@Override
		public BoundObject getSecurityObject(BoundChecker aChecker, Object model, BoundCommandGroup aCommandGroup) {
			return SECURITY_OBJECT;
		}

	}

	public void testDispatch() throws ConfigurationException {
		Map<BoundCommandGroup, Class<? extends SecurityObjectProvider>> providers = new HashMap<>();
		providers.put(SimpleBoundCommandGroup.READ, Test1SecurityObjectProvider.class);
		providers.put(SimpleBoundCommandGroup.WRITE, Test2SecurityObjectProvider.class);
		SecurityObjectProvider dispatchingProvider = newInstance(SecurityRootObjectProvider.class, providers);
		BoundChecker checker = new SimpleBoundChecker("checker", null);
		Object model = new Object();
		assertEquals(Test1SecurityObjectProvider.SECURITY_OBJECT,
			dispatchingProvider.getSecurityObject(checker, model, SimpleBoundCommandGroup.READ));
		assertEquals(Test2SecurityObjectProvider.SECURITY_OBJECT,
			dispatchingProvider.getSecurityObject(checker, model, SimpleBoundCommandGroup.WRITE));
		assertEquals(SecurityRootObjectProvider.INSTANCE.getSecurityRoot(),
			dispatchingProvider.getSecurityObject(checker, model, SimpleBoundCommandGroup.CREATE));
	}

	@SuppressWarnings("unchecked")
	private DispatchingSecurityObjectProvider newInstance(
			Class<? extends SecurityObjectProvider> defaultProvider,
			Map<? extends BoundCommandGroup, Class<? extends SecurityObjectProvider>> providers)
			throws ConfigurationException {
		DispatchingSecurityObjectProvider.Config config =
			TypedConfiguration.newConfigItem(DispatchingSecurityObjectProvider.Config.class);
		config.setDefault((PolymorphicConfiguration<SecurityObjectProvider>) TypedConfiguration
			.createConfigItemForImplementationClass(defaultProvider));
		for (Entry<? extends BoundCommandGroup, Class<? extends SecurityObjectProvider>> providerConfig : providers
			.entrySet()) {
			PolymorphicConfiguration<SecurityObjectProvider> provider =
				(PolymorphicConfiguration<SecurityObjectProvider>) TypedConfiguration
					.createConfigItemForImplementationClass(providerConfig.getValue());
			String commandGroup = providerConfig.getKey().getID();
			ProviderConfig providerConf =
				TypedConfiguration.newConfigItem(DispatchingSecurityObjectProvider.ProviderConfig.class);
			providerConf.setCommandGroup(commandGroup);
			providerConf.setImpl(provider);
			config.getProviders().put(providerConf.getCommandGroup(), providerConf);
		}
		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
	}

	/**
	 * a cumulative {@link Test} for all Tests in
	 *         {@link TestDispatchingSecurtyObjectProvider}.
	 */
	public static Test suite() {
		Test test = ServiceTestSetup.createSetup(TestDispatchingSecurtyObjectProvider.class,
			SecurityObjectProviderManager.Module.INSTANCE,
			BoundHelper.Module.INSTANCE);
		return TLTestSetup.createTLTestSetup(test);
	}

}
