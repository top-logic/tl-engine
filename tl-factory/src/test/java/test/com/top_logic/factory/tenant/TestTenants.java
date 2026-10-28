/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.tenant;

import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.factory.project.FactoryModel;
import com.top_logic.model.TLObject;
import com.top_logic.util.model.ModelService;

/**
 * Tenants and projects for the tests of the control application.
 */
public class TestTenants {

	/**
	 * Name of the tenant owning the projects of {@link #newProject()}.
	 */
	public static final String TEST_TENANT = "Test tenant";

	private TestTenants() {
		// Static helpers only.
	}

	/**
	 * Creates a project owned by the {@link #testTenant() test tenant} in the current transaction.
	 */
	public static TLObject newProject() {
		return newProject(testTenant());
	}

	/**
	 * Creates a project owned by the given tenant in the current transaction.
	 */
	public static TLObject newProject(TLObject tenant) {
		TLObject result = ModelService.getInstance().getFactory().createObject(FactoryModel.projectType());
		result.tUpdateByName(FactoryModel.PROJECT_TENANT, tenant);
		return result;
	}

	/**
	 * Creates a tenant with the given name in the current transaction.
	 */
	public static TLObject newTenant(String name) {
		TLObject result = ModelService.getInstance().getFactory().createObject(FactoryModel.tenantType());
		result.tUpdateByName(FactoryModel.TENANT_NAME, name);
		return result;
	}

	/**
	 * The tenant named {@link #TEST_TENANT}, created in the current transaction if it does not
	 * exist. It is kept for the following tests.
	 */
	public static TLObject testTenant() {
		for (TLObject tenant : MetaElementUtil.getAllInstancesOf(FactoryModel.tenantType(), TLObject.class)) {
			if (TEST_TENANT.equals(tenant.tValueByName(FactoryModel.TENANT_NAME))) {
				return tenant;
			}
		}
		return newTenant(TEST_TENANT);
	}

}
