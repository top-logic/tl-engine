/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.config.i18n;

import java.lang.reflect.Array;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey.Builder;
import com.top_logic.basic.util.ResourceTransaction;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.util.Resources;

/**
 * A utility class for storing interactively entered {@link ResKey} values.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class InternationalizedUtil {

	/**
	 * Creates a new {@link ResKey} from a random {@link UUID} with a constant prefix.
	 * 
	 * @see UUID#randomUUID()
	 * @see I18NConstants#DYNAMIC
	 */
	public static ResKey newKey() {
		return I18NConstants.DYNAMIC.key(StringServices.randomUUID());
	}

	/**
	 * Stores all literal {@link ResKey}s referenced in the given {@link ConfigurationItem} and
	 * replaces them with plain keys referencing the stored internationalization values.
	 * 
	 * @param config
	 *        The item to transform.
	 * @return Reference to the same instance as given as parameter for call chaining.
	 */
	public static <T extends ConfigurationItem> T storeI18N(T config) {
		return storeI18N(config, false);
	}

	/**
	 * Stores all literal {@link ResKey}s referenced in the given {@link ConfigurationItem} and
	 * replaces them with plain keys referencing the stored internationalization values.
	 * 
	 * @param config
	 *        The item to transform.
	 * @param ignoreKeys
	 *        Whether to ignore keys given in the transformed configuration. During configuration
	 *        new keys are generated for each internationalization literal.
	 * @return Reference to the same instance as given as parameter for call chaining.
	 */
	public static <T extends ConfigurationItem> T storeI18N(T config, boolean ignoreKeys) {
		try (ResourceTransaction tx = ResourcesModule.getInstance().startResourceTransaction()) {
			T result = new StoreI18N(tx, ignoreKeys).transform(config);
			tx.commit();
			return result;
		}
	}

	static class StoreI18N extends I18NTransform {

		private boolean _ignoreKeys;

		private ResourceTransaction _tx;

		/**
		 * Creates a {@link StoreI18N}.
		 *
		 * @param updateExistingKeys
		 *        Whether to ignore keys given in the transformed configuration. During
		 *        configuration new keys are generated for each internationalization literal.
		 */
		public StoreI18N(ResourceTransaction tx, boolean updateExistingKeys) {
			_tx = tx;
			_ignoreKeys = updateExistingKeys;
		}

		@Override
		protected void processResKeyProperty(ConfigurationItem config, PropertyDescriptor property) {
			ResKey reskey = (ResKey) config.value(property);
			if (reskey != null && reskey.isLiteral()) {
				config.update(property, saveLiteralKey(reskey));
			}
		}
	
		private ResKey saveLiteralKey(ResKey literal) {
			assert literal.isLiteral();
	
			ResKey plain;
			if (_ignoreKeys || !literal.hasKey()) {
				plain = newKey();
			} else {
				plain = ResKey.internalCreate(literal.getKey());
			}
	
			for (Locale locale : ResourcesModule.getInstance().getSupportedLocales()) {
				String localizedValue = Resources.getInstance(locale).getString(literal);
				_tx.saveI18N(locale, plain, localizedValue);
			}
	
			return plain;
		}
	
	}

	/**
	 * Transforms the given item by replacing all {@link ResKey} values in properties with
	 * {@link ResKey#literal(com.top_logic.basic.util.ResKey.LangString...) literals} containing the
	 * current translations in the {@link ResourcesModule#getSupportedLocaleNames() supported
	 * languages}.
	 * 
	 * <p>
	 * Note: The item is transformed inline, the returned value the same as the given one for
	 * call-chaining.
	 * </p>
	 *
	 * @param <T>
	 *        The configuration type.
	 * @param config
	 *        The {@link ConfigurationItem} to manipulate.
	 * @return The given item after transformation.
	 */
	public static <T extends ConfigurationItem> T fillLiteralI18N(T config) {
		return fillLiteralI18N(config, false);
	}

	/**
	 * Transforms the given item by replacing all {@link ResKey} values in properties with
	 * {@link ResKey#literal(com.top_logic.basic.util.ResKey.LangString...) literals} containing the
	 * current translations in the {@link ResourcesModule#getSupportedLocaleNames() supported
	 * languages}.
	 * 
	 * <p>
	 * Note: The item is transformed inline, the returned value the same as the given one for
	 * call-chaining.
	 * </p>
	 *
	 * @param <T>
	 *        The configuration type.
	 * @param config
	 *        The {@link ConfigurationItem} to manipulate.
	 * @param dropKeys
	 *        Whether to create {@link ResKey} literals that do no longer refer to the original
	 *        keys. For the resulting {@link ResKey} literals, {@link ResKey#hasKey()} is
	 *        <code>false</code>.
	 * @return The given item after transformation.
	 */
	public static <T extends ConfigurationItem> T fillLiteralI18N(T config, boolean dropKeys) {
		return new FillI18N(dropKeys).transform(config);
	}

	static class FillI18N extends I18NTransform {

		private boolean _dropKeys;

		/**
		 * Creates a {@link FillI18N}.
		 *
		 * @param dropKeys
		 *        Whether to drop keys when transforming resource keys to resource literals.
		 */
		public FillI18N(boolean dropKeys) {
			_dropKeys = dropKeys;
		}

		@Override
		protected void processResKeyProperty(ConfigurationItem config, PropertyDescriptor property) {
			ResKey reskey = (ResKey) config.value(property);
			if (reskey != null && !reskey.isLiteral()) {
				config.update(property, loadLiteralKey(reskey));
			}
		}

		private ResKey loadLiteralKey(ResKey resKey) {
			assert !resKey.isLiteral();

			Builder builder = ResKey.builder(_dropKeys || !resKey.hasKey() ? null : resKey.getKey());
			for (Locale locale : ResourcesModule.getInstance().getSupportedLocales()) {
				String localizedValue = Resources.getInstance(locale).getString(resKey);
				builder.add(locale, localizedValue);
			}

			return builder.build();
		}

	}

	static abstract class I18NTransform {
		public <T extends ConfigurationItem> T transform(T config) {
			internaltransform(config);
			return config;
		}

		private void internaltransform(ConfigurationItem config) {
			for (PropertyDescriptor property : config.descriptor().getProperties()) {
				if (property.getType() == ResKey.class) {
					processResKeyProperty(config, property);
				} else {
					descendProperty(config, property);
				}
			}
		}

		protected abstract void processResKeyProperty(ConfigurationItem config, PropertyDescriptor property);

		private <T extends ConfigurationItem> void descendProperty(T config, PropertyDescriptor property) {
			switch (property.kind()) {
				case ITEM:
				case LIST:
				case ARRAY:
				case MAP:
					descendValue(config.value(property));
					break;

				default:
					break;
			}
		}

		private void descendValue(Object value) {
			if (value instanceof ConfigurationItem) {
				internaltransform((ConfigurationItem) value);
			} else if (value instanceof Iterable<?>) {
				for (Object entry : (Iterable<?>) value) {
					descendValue(entry);
				}
			} else if (value instanceof Map<?, ?>) {
				for (Object entry : ((Map<?, ?>) value).values()) {
					descendValue(entry);
				}
			} else if (value == null) {
				// Ignore.
			} else if (value.getClass().isArray()) {
				for (int n = 0, cnt = Array.getLength(value); n < cnt; n++) {
					descendValue(Array.get(value, n));
				}
			}
		}
	}

}