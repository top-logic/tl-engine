/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.db.schema.setup;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.db.schema.setup.SchemaSetup;
import com.top_logic.basic.db.schema.setup.config.SchemaConfiguration;
import com.top_logic.basic.db.schema.setup.config.TypeProvider;
import com.top_logic.dob.MOFactory;
import com.top_logic.dob.meta.MORepository;

/**
 * Extension of {@link SchemaSetup} which allows adding additional {@link TypeProvider}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SchemaSetupForTest extends SchemaSetup {

	private List<TypeProvider> _testProviders = new ArrayList<>();

	/**
	 * Creates a {@link SchemaSetupForTest} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public SchemaSetupForTest(InstantiationContext context, SchemaConfiguration config) {
		super(context, config);
	}

	@Override
	protected void internalCreateTypes(InstantiationContext context, MORepository repository, MOFactory typeFactory) {
		super.internalCreateTypes(context, repository, typeFactory);
		for (TypeProvider provider : _testProviders) {
			provider.createTypes(context, typeFactory, repository);
		}
	}

	@Override
	protected SchemaSetup internalResolve(InstantiationContext context) throws ConfigurationException {
		SchemaSetup result = super.internalResolve(context);
		((SchemaSetupForTest) result)._testProviders.addAll(_testProviders);
		return result;
	}

	/**
	 * Add the given {@link TypeProvider} to create types in
	 * {@link #createTypes(InstantiationContext, MORepository, MOFactory)}.
	 */
	public void addTypeProvider(TypeProvider provider) {
		_testProviders.add(provider);
	}

}

