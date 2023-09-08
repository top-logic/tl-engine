/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.Log;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ResourceDeclaration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.db.schema.io.MORepositoryBuilder;
import com.top_logic.basic.db.schema.setup.config.TypeProvider;
import com.top_logic.dob.MOFactory;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;

/**
 * {@link KnowledgeBaseTestScenario} that loads a Meta-XML file.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ConfiguredScenario implements KnowledgeBaseTestScenario, TypeProvider {

	private final String _resource;

	/**
	 * Creates a {@link ConfiguredScenario}.
	 *
	 * @param resource
	 *        The meta XML file resource to load.
	 */
	public ConfiguredScenario(String resource) {
		_resource = resource;
	}

	@Override
	public List<TypeProvider> getTestTypes() {
		return Collections.<TypeProvider> singletonList(this);
	}

	@Override
	public void createTypes(Log log, MOFactory typeFactory, MORepository typeRepository) {
		ResourceDeclaration declaration = TypedConfiguration.newConfigItem(ResourceDeclaration.class);
		declaration.setResource(_resource);
		List<ResourceDeclaration> declarations = Collections.singletonList(declaration);
		try {
			MORepositoryBuilder.buildRepository(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, typeFactory,
				declarations, typeRepository);
		} catch (ConfigurationException ex) {
			throw new KnowledgeBaseRuntimeException(ex);
		}

	}

}
