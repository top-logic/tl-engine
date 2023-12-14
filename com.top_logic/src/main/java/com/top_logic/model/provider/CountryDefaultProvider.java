/*
 * Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.model.provider;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;
import com.top_logic.util.Country;

/**
 * {@link DefaultProvider} delivering a configured {@link Country}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@InApp(priority = 100)
@TargetType(value = TLTypeKind.CUSTOM, name = "tl.util:Country")
public class CountryDefaultProvider extends ConfiguredConstantDefaultProvider {

	/**
	 * Configuration of a {@link CountryDefaultProvider}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@TagName("country")
	public interface Config extends ConfiguredConstantDefaultProvider.Config<BooleanDefaultProvider> {

		@Override
		Country getValue();

	}

	/**
	 * Create a {@link CountryDefaultProvider}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public CountryDefaultProvider(InstantiationContext context, Config config) {
		super(context, config);
	}

}