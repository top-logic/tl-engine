/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.db.schema;

import java.util.Iterator;
import java.util.List;

import junit.framework.Test;

import test.com.top_logic.basic.DecoratedTestSetup;
import test.com.top_logic.basic.ModuleContextDecorator;
import test.com.top_logic.basic.RearrangableTestSetup;
import test.com.top_logic.basic.TestSetupDecorator;
import test.com.top_logic.basic.ThreadContextDecorator;

import com.top_logic.basic.col.MutableInteger;
import com.top_logic.basic.col.TupleFactory;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.Decision;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.ResourceDeclaration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.db.model.DBSchema;
import com.top_logic.basic.db.model.util.DBSchemaUtils;
import com.top_logic.basic.db.schema.setup.SchemaSetup;
import com.top_logic.basic.db.schema.setup.config.ApplicationTypes;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.ConnectionPoolRegistry;
import com.top_logic.dsa.DataAccessService;

/**
 * Setup creating all schemas in the type system.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SchemaTestSetup extends RearrangableTestSetup {

	private static final String SINGLE_BRANCH_META = "webinf://kbase/singleBranchMeta.xml";

	private static final MultipleSetupCounter SETUP_CNT = newMultipleCounter();

	/** Whether the {@link SchemaConfiguration} must support multiple branches. */
	protected final Decision _multipleBranches;

	/**
	 * Creates a new {@link SchemaTestSetup}.
	 * 
	 * @param test
	 *        The actual test.
	 * @param multipleBranches
	 *        whether the {@link SchemaConfiguration} shall have
	 *        {@link SchemaConfiguration#hasMultipleBranches() multiple branches}.
	 */
	public SchemaTestSetup(Test test, Decision multipleBranches) {
		this(test, multipleBranches, SETUP_CNT.getCounterFor(multipleBranches));
	}

	@Override
	public Object configKey() {
		return TupleFactory.newTuple(super.configKey(), _multipleBranches);
	}

	/**
	 * Creates a new {@link SchemaTestSetup} with given setup counter.
	 */
	protected SchemaTestSetup(Test test, Decision multipleBranches, MutableInteger setupCnt) {
		super(createDecorator(), test, setupCnt);
		_multipleBranches = multipleBranches;
	}

	private static TestSetupDecorator createDecorator() {
		TestSetupDecorator setupDAService =
			ModuleContextDecorator.createMultiModulesDecorator(DataAccessService.Module.INSTANCE,
				ConnectionPoolRegistry.Module.INSTANCE);
		return DecoratedTestSetup.join(ThreadContextDecorator.INSTANCE, setupDAService);
	}

	@Override
	protected void doSetUp() throws Exception {
		ApplicationTypes types = ApplicationConfig.getInstance().getConfig(ApplicationTypes.class);
		SchemaConfiguration schemaConfig = types.getTypeSystem(SchemaConfiguration.DEFAULT_NAME).getConfig();
		schemaConfig = updateMultipleBranches(schemaConfig, _multipleBranches);

		List<? extends ResourceDeclaration> declarations = getSchemaDeclarations(schemaConfig);

		DBSchema schema = SchemaSetup.newDBSchema(declarations);
		// Allocate tables locally in the current schema.
		schema.setName(null);

		ConnectionPool pool = ConnectionPoolRegistry.getDefaultConnectionPool();
		DBSchemaUtils.recreateTables(pool, schema);
	}

	/**
	 * Returns a {@link SchemaConfiguration} where the value of
	 * {@link SchemaConfiguration#hasMultipleBranches()} is updated with the value of the given
	 * {@link Decision}.
	 * 
	 * @param schemaConfig
	 *        The {@link SchemaConfiguration} to update.
	 * @param multipleBranches
	 *        New value of {@link SchemaConfiguration#hasMultipleBranches()}. If
	 *        {@link Decision#DEFAULT} the call is noop.
	 * @return Eventually a the same or a new {@link SchemaConfiguration} with desired value.
	 */
	public static SchemaConfiguration updateMultipleBranches(SchemaConfiguration schemaConfig, Decision multipleBranches) {
		PropertyDescriptor multipleBranchesProperty;
		switch (multipleBranches) {
			case TRUE:
				/* Copy the configuration because this is the "static" one from the application
				 * configuration, which should not be modified. */
				schemaConfig = TypedConfiguration.copy(schemaConfig);
				multipleBranchesProperty =
					schemaConfig.descriptor().getProperty(SchemaConfiguration.MULTIPLE_BRANCHES_ATTRIBUTE);
				schemaConfig.update(multipleBranchesProperty, true);
				for (Iterator<ResourceDeclaration> it = schemaConfig.getDeclarations().iterator(); it.hasNext();) {
					ResourceDeclaration resourceDecl = it.next();
					if (SINGLE_BRANCH_META.equals(resourceDecl.getResource())) {
						it.remove();
					}
				}
				break;
			case FALSE:
				/* Copy the configuration because this is the "static" one from the application
				 * configuration, which should not be modified. */
				schemaConfig = TypedConfiguration.copy(schemaConfig);
				multipleBranchesProperty =
					schemaConfig.descriptor().getProperty(SchemaConfiguration.MULTIPLE_BRANCHES_ATTRIBUTE);
				schemaConfig.update(multipleBranchesProperty, false);
				schemaConfig.getDeclarations().add(resourceDeclaration(SINGLE_BRANCH_META));
				break;
			default:
				break;
		}
		return schemaConfig;
	}

	private static ResourceDeclaration resourceDeclaration(String resource) {
		ResourceDeclaration decl = TypedConfiguration.newConfigItem(ResourceDeclaration.class);
		decl.setResource(resource);
		return decl;
	}

	/**
	 * Returns the list of {@link ResourceDeclaration} containing the schema configurations.
	 */
	protected List<? extends ResourceDeclaration> getSchemaDeclarations(SchemaConfiguration schemaConfig) {
		return schemaConfig.getSchemas();
	}

	@Override
	protected void doTearDown() throws Exception {
		// Ignore.
	}

}
