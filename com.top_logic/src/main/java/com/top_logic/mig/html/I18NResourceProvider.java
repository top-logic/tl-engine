/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Internationalizable;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.util.Resources;

/**
 * Special {@link ResourceProvider} creating internationalized labels for
 * {@link Internationalizable}, {@link ResKey} or string-encoded {@link ResKey}s.
 * 
 * <p>
 * This is especially useful to directly display {@link ResKey} instances,
 * {@link Internationalizable} objects, or {@link ResKey#encode(ResKey) string-encoded resource
 * keys}).
 * </p>
 * 
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 */
public class I18NResourceProvider extends DefaultResourceProvider
		implements ConfiguredInstance<I18NResourceProvider.Config> {

	/**
	 * Configuration options for {@link I18NResourceProvider}.
	 */
	public interface Config extends PolymorphicConfiguration<ResourceProvider> {
		/**
		 * The optional resource prefix used as context to resolve suffixes in.
		 */
		@Name(RES_PREFIX_PROPERTY)
		@InstanceFormat
		ResPrefix getResPrefix();

		/**
		 * @see #getResPrefix()
		 */
		void setResPrefix(ResPrefix value);

		/**
		 * The fully qualified key to use, if <code>null</code> is displayed, or the encoded form is
		 * empty.
		 */
		@Name(EMPTY_KEY_PROPERTY)
		@InstanceFormat
		ResKey getEmptyKey();

		/**
		 * @see #getEmptyKey()
		 */
		void setEmptyKey(ResKey value);
	}

	/**
	 * Configuration option for {@link #_emptyKey}.
	 */
    private static final String EMPTY_KEY_PROPERTY = "emptyKey";

    /**
     * Configuration option for {@link #_resPrefix}
     */
	private static final String RES_PREFIX_PROPERTY = "resPrefix";


	/** Instance of {@link I18NResourceProvider} without a resource key prefix. */
    public static final I18NResourceProvider INSTANCE = new I18NResourceProvider();

	private static final ResKey defaultEmptyKey = ResKey.text("");

    /** The resource key prefix. */
	private final ResPrefix _resPrefix;
    
	private final ResKey _emptyKey;

    /**
     * Creates a new I18NResourceProvider without a resource key prefix.
     */
    public I18NResourceProvider() {
		this(null);
    }

    /**
	 * Creates a {@link I18NResourceProvider} with the given resource prefix.
	 * 
	 * @param resPrefix
	 *        The resource prefix to use, <code>null</code> to resolve global keys.
	 */
	public I18NResourceProvider(ResPrefix resPrefix) {
		this(resPrefix, null);
    }

    /**
	 * Creates a {@link I18NResourceProvider} with the given resource prefix and empty key.
	 * 
	 * @param resPrefix
	 *        The resource prefix to use, <code>null</code> to resolve global keys.
	 * @param emptyKey
	 *        The key to resolve for <code>null</code> values and those not providing any key.
	 */
	public I18NResourceProvider(ResPrefix resPrefix, ResKey emptyKey) {
		_resPrefix = resPrefix;
		_emptyKey = emptyKey == null ? defaultEmptyKey : emptyKey;
    }

	/**
	 * Creates a {@link I18NResourceProvider} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public I18NResourceProvider(InstantiationContext context, Config config) {
		this(config.getResPrefix(), config.getEmptyKey());
	}
    
	@Override
	public Config getConfig() {
		Config result = TypedConfiguration.newConfigItem(Config.class);
		result.setImplementationClass(getClass());
		result.setResPrefix(_resPrefix);
		result.setEmptyKey(_emptyKey);
		return result;
	}

    /**
     * Translates all non-empty keys using with the configured resource prefix.
     */
    @Override
	public String getLabel(Object aObject) {
		ResKey key = toKey(aObject);
		if (key == null) {
			return getEmptyLabel();
		}
		return resolve(key);
    }

	private String getEmptyLabel() {
		return resolve(_emptyKey);
	}

	@Override
	public String getTooltip(Object aObject) {
		ResKey key = toKey(aObject);
		if (key == null) {
			return null;
		}

		return resolveOptional(key.tooltip());
	}

	private ResKey toKey(Object aObject) {
		if (aObject == null) {
			return null;
		} else if (aObject instanceof ResKey) {
			return ((ResKey) aObject);
		} else if (aObject instanceof Internationalizable) {
			return ((Internationalizable) aObject).getI18NKey(_resPrefix);
		} else {
			return decodeKey(aObject);
		}
	}

	/**
	 * Resolve a {@link ResKey} from a given non-<code>null</code> value.
	 * 
	 * @param value
	 *        The value to internationalize.
	 * @return The {@link ResKey} for displaying the given value.
	 */
	protected ResKey decodeKey(Object value) {
		String encoding = value.toString();
		if (StringServices.isEmpty(encoding)) {
			return null;
		}
		if (_resPrefix == null) {
			return ResKey.decode(encoding);
		} else {
			return _resPrefix.key(encoding);
		}
	}

	private static String resolve(ResKey aKey) {
		return Resources.getInstance().getString(aKey);
	}

	private static String resolveOptional(ResKey aKey) {
		return Resources.getInstance().getString(aKey, null);
	}

}
