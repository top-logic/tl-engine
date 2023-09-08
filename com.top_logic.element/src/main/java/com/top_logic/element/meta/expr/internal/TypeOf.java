/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.expr.internal;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.element.meta.kbbased.filtergen.AttributeValueLocator;
import com.top_logic.element.meta.kbbased.filtergen.CustomSingleSourceValueLocator;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.config.TypeRefMandatory;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link AttributeValueLocator} that filters source objects depending on their type.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TypeOf extends CustomSingleSourceValueLocator implements ConfiguredInstance<TypeOf.Config> {

	/**
	 * Configuration options for {@link TypeOf}.
	 */
	@TagName("type-of")
	public interface Config extends PolymorphicConfiguration<TypeOf>, TypeRefMandatory {
		// Pure sum interface.
	}

	/**
	 * Creates a {@link TypeOf} configuration.
	 * 
	 * @param module
	 *        See {@link Config#getTypeSpec()}.
	 * @param typeName
	 *        See {@link Config#getTypeSpec()}.
	 */
	public static PolymorphicConfiguration<? extends AttributeValueLocator> newInstance(String module, String typeName) {
		Config config = TypedConfiguration.newConfigItem(Config.class);
		config.setTypeSpec(TLModelUtil.qualifiedName(module, typeName));
		return config;
	}

	private final String _typeSpec;

	private final Config _config;

	/**
	 * Creates a {@link TypeOf} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TypeOf(InstantiationContext context, Config config) {
		_config = config;
		_typeSpec = config.getTypeSpec();
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public Object internalLocateAttributeValue(Object obj) {
		if (!(obj instanceof TLObject)) {
			return null;
		}

		TLObject baseObject = (TLObject) obj;
		if (!TLModelUtil.isCompatibleInstance(getType(baseObject), baseObject)) {
			return null;
		}

		return obj;
	}

	private TLClass getType(TLObject context) {
		return Operation.type(context, _typeSpec);
	}

}
