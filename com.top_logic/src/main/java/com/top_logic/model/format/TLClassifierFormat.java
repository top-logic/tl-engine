/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.format;

import java.util.stream.Collectors;

import org.apache.commons.collections4.BidiMap;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.BidiHashMap;
import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.knowledge.gui.layout.list.FastListElementLabelProvider;
import com.top_logic.knowledge.wrap.list.FastList;
import com.top_logic.knowledge.wrap.list.FastListElement;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.util.AllEnumerations;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.model.util.TLModelPartRef.PartMapping;
import com.top_logic.util.Resources;

/**
 * Configurable {@link ConfigurationValueProvider} that reads classifiers of {@link TLEnumeration}
 * model elements based on their label or technical name.
 * 
 * <p>
 * Note: Since this implementation caches classifiers, it is designed to be used in short-lived
 * scenarios such as an importer that creates the format, imports the data and then discards the
 * format. By no way, an instance of this format must be used as member of a long-lived
 * configuration (e.g. as an {@link InstanceFormat} value of a layout configuration).
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TLClassifierFormat extends AbstractConfigurationValueProvider<FastListElement>
		implements ConfiguredInstance<TLClassifierFormat.Config<?>> {

	/**
	 * Configuration options for {@link TLClassifierFormat}.
	 */
	public interface Config<I extends TLClassifierFormat> extends PolymorphicConfiguration<I> {
		/**
		 * The fully qualified name of the {@link TLEnumeration}.
		 */
		@Name("type")
		@Mandatory
		@Options(fun = AllEnumerations.class, mapping = PartMapping.class)
		TLModelPartRef getType();

		/**
		 * Whether to use the labels of the classifiers for identification.
		 */
		@Name("by-label")
		boolean getByLabel();

		/**
		 * Whether to ignore upper/lower-case of the identifiers.
		 */
		@Name("ignore-case")
		boolean getIgnoreCase();

		/**
		 * The language to use for looking up classifiers, if {@link #getByLabel()} is set.
		 */
		@Name("language")
		@StringDefault("en")
		String getLanguage();
	}

	private final Config<?> _config;

	@Deprecated
	private final String _typeName;

	private final boolean _byLabel;

	private final boolean _ignoreCase;

	private final String _lang;

	private BidiMap<String, TLClassifier> _classifierByKey;

	/**
	 * Creates a {@link TLClassifierFormat} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TLClassifierFormat(InstantiationContext context, Config<?> config) {
		super(TLClassifier.class);

		_config = config;
		_typeName = null;

		_byLabel = config.getByLabel();
		_ignoreCase = config.getIgnoreCase();
		_lang = config.getLanguage();
	}

	/**
	 * Creates a {@link TLClassifierFormat}.
	 * 
	 * @param listName
	 *        The name of the fast list to be used, must not be <code>null</code>.
	 * 
	 * @deprecated Use {@link TLClassifierFormat#TLClassifierFormat(InstantiationContext, Config)}
	 */
	@Deprecated
	protected TLClassifierFormat(String listName) {
		super(TLClassifier.class);

		_config = null;
		_typeName = listName;

		_byLabel = true;
		_ignoreCase = true;
		_lang = "de_DE";
	}

	@Override
	public Config<?> getConfig() {
		assert _config != null : "Must not access the configuration, if not created as configured instance.";
		return _config;
	}

	@Override
	public FastListElement getValueNonEmpty(String aPropertyName, CharSequence elementName)
			throws ConfigurationException {
		initCache();
		FastListElement result = (FastListElement) _classifierByKey.get(key(elementName.toString()));
		if (result == null) {
			throw new ConfigurationException(
				I18NConstants.ERROR_NO_SUCH_CLASSIFIER__NAME_OPTIONS.fill(elementName,
					_classifierByKey.keySet().stream().map(key -> "\"" + key + "\"").collect(Collectors.joining(", "))),
				aPropertyName, aPropertyName);
		}
		return result;
	}

	@Override
	public String getSpecificationNonNull(FastListElement element) {
		initCache();
		return _classifierByKey.getKey(element);
	}

	private void initCache() {
		if (_classifierByKey == null) {
			_classifierByKey = createClassifierMap();
		}
	}

	private BidiMap<String, TLClassifier> createClassifierMap() {
		BidiMap<String, TLClassifier> result = new BidiHashMap<>();
		TLEnumeration enumeration = lookupType();
		for (TLClassifier classifier : enumeration.getClassifiers()) {
			result.put(key(classifier), classifier);
		}
		return result;
	}

	private String key(TLClassifier classifier) {
		String name = _byLabel ? getResources().getString(FastListElementLabelProvider.labelKey(classifier))
			: classifier.getName();
		return key(name);
	}

	private String key(String value) {
		return _ignoreCase ? value.toLowerCase() : value;
	}

	private TLEnumeration lookupType() {
		if (_typeName != null) {
			// For legacy compatibility.
			return FastList.getFastList(_typeName);
		}
		return (TLEnumeration) getConfig().getType().resolveType();
	}

	/**
	 * The {@link Resources} used to get the name of the {@link FastListElement}s.
	 * <p>
	 * This method is a hook for subclasses, which want to use other {@link Resources}. <br/>
	 * The default is defined in {@link #getDefaultResource()}.
	 * </p>
	 */
	protected Resources getResources() {
		return getDefaultResource();
	}

	/**
	 * The default {@link Resources} (<code>"de_DE"</code>), which are used if
	 * {@link #getResources()} is not overridden in subclasses.
	 */
	protected final Resources getDefaultResource() {
		return Resources.getInstance(_lang);
	}

}
