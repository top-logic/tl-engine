/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.instance.importer.schema;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.instance.importer.resolver.InstanceResolver;
import com.top_logic.model.util.TLModelPartRef;

/**
 * Collections of {@link ObjectConf objects to import}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ObjectsConf extends ConfigurationItem {

	/**
	 * Resolvers used to lookup stable object references stored in this configuration.
	 * 
	 * @see GlobalRefConf#getId()
	 */
	@Key(ResolverDef.TYPE)
	Map<TLModelPartRef, ResolverDef> getResolvers();

	/**
	 * Description of objects to import.
	 */
	@DefaultContainer
	List<ObjectConf> getObjects();

	/**
	 * Resolver entry associating an {@link InstanceResolver} with a {@link TLStructuredType}.
	 */
	interface ResolverDef extends ConfigurationItem {
		/**
		 * @see #getType()
		 */
		String TYPE = "type";

		/**
		 * @see #getImpl()
		 */
		String IMPL = "impl";

		/**
		 * The {@link TLStructuredType} for which the given {@link InstanceResolver} implementation
		 * should be used.
		 */
		@Name(TYPE)
		TLModelPartRef getType();

		/**
		 * @see #getType()
		 */
		void setType(TLModelPartRef ref);

		/**
		 * The {@link InstanceResolver} implementation to use for objects of the given
		 * {@link #getType()}.
		 */
		@Name(IMPL)
		PolymorphicConfiguration<? extends InstanceResolver> getImpl();

		/**
		 * @see #getImpl() 
		 */
		void setImpl(PolymorphicConfiguration<? extends InstanceResolver> value);

	}

}
