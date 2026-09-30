/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.security;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.layout.view.security.SecurityScopeService;
import com.top_logic.tool.boundsec.config.AccessConfigurationSetupService;

/**
 * Test for the startup order of the {@link AccessConfigurationSetupService} and the
 * {@link SecurityScopeService}.
 *
 * <p>
 * The access configuration (<code>WEB-INF/conf/security.xml</code>) grants roles on view security
 * scopes. A scope can be granted only after the {@link SecurityScopeService} has materialized it as
 * a security object, so the import service depends on the scope service. The dependency is declared
 * in the configuration of this module, since {@link AccessConfigurationSetupService} resides in a
 * layer below the view layer and cannot reference the {@link SecurityScopeService} in Java.
 * </p>
 *
 * @see ModuleUtil#getDependencies(BasicRuntimeModule)
 */
public class TestSecurityScopeStartupOrder extends TestCase {

	/**
	 * The access configuration import starts after the view security scopes exist.
	 */
	public void testImportDependsOnScopes() {
		assertTrue("The access configuration import must start after the view security scopes.",
			dependencies(AccessConfigurationSetupService.Module.INSTANCE)
				.contains(SecurityScopeService.Module.INSTANCE));
	}

	/**
	 * The dependency comes from the configuration, not from the
	 * {@link ServiceDependencies} annotation of the service.
	 */
	public void testDependencyIsDeclaredInConfiguration() {
		List<Class<? extends BasicRuntimeModule<?>>> annotated = programmaticDependencies();

		assertFalse("The service in the lower layer cannot name the view layer scope service.",
			annotated.contains(SecurityScopeService.Module.class));
		assertFalse("The annotation of the service under test must be readable.", annotated.isEmpty());
	}

	/**
	 * The scope service itself does not wait for the access configuration import, so that the
	 * dependency stays acyclic.
	 */
	public void testScopesDoNotDependOnImport() {
		assertFalse("The scope service must not wait for the access configuration import.",
			dependencies(SecurityScopeService.Module.INSTANCE)
				.contains(AccessConfigurationSetupService.Module.INSTANCE));
	}

	private List<Class<? extends BasicRuntimeModule<?>>> programmaticDependencies() {
		ServiceDependencies annotation =
			AccessConfigurationSetupService.class.getAnnotation(ServiceDependencies.class);
		return annotation == null ? List.of() : Arrays.asList(annotation.value());
	}

	private Set<BasicRuntimeModule<?>> dependencies(BasicRuntimeModule<?> module) {
		Set<BasicRuntimeModule<?>> result = new LinkedHashSet<>();
		for (BasicRuntimeModule<?> dependency : ModuleUtil.INSTANCE.getDependencies(module)) {
			result.add(dependency);
		}
		return result;
	}

	/**
	 * Test suite requiring the application configuration the dependencies are read from.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(TestSecurityScopeStartupOrder.class);
	}

}
