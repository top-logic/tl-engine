/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.form;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.element.meta.kbbased.storage.mappings.DirectMapping;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLPrimitive.Kind;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TransientObject;
import com.top_logic.model.access.StorageMapping;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.util.TLModelUtil;

/**
 * A model with a single class holding a text attribute, and transient objects of it, so that a
 * form can display real fields without a persistent model.
 */
public class ItemFixture {

	/** The name of the text attribute of {@link #getType()}. */
	public static final String NAME = "name";

	private final TLClass _type;

	/**
	 * Creates a model holding the class under the given module name.
	 *
	 * @param moduleName
	 *        The name of the module of the class, distinct per test to keep the models apart.
	 */
	public ItemFixture(String moduleName) {
		TLModelImpl model = new TLModelImpl();
		TLModule module = TLModelUtil.addModule(model, moduleName);
		TLPrimitive text = TLModelUtil.addDatatype(module, module, "Text", Kind.STRING, directMapping(String.class));
		_type = TLModelUtil.addClass(module, "Item");
		TLModelUtil.addProperty(_type, NAME, text);
	}

	/**
	 * The class of the objects {@link #newItem(String)} creates.
	 */
	public TLClass getType() {
		return _type;
	}

	/**
	 * A new transient object of {@link #getType()} with the given {@link #NAME}.
	 */
	public TLObject newItem(String name) {
		Item result = new Item(_type);
		result.tUpdate(_type.getPart(NAME), name);
		return result;
	}

	private static StorageMapping<?> directMapping(Class<?> applicationType) {
		try {
			PolymorphicConfiguration<?> config =
				TypedConfiguration.createConfigItemForImplementationClass(DirectMapping.class);
			config.update(config.descriptor().getProperty(DirectMapping.Config.APPLICATION_TYPE), applicationType);
			return (StorageMapping<?>) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
		} catch (ConfigurationException ex) {
			throw new AssertionError("Cannot create the storage mapping of the text attribute.", ex);
		}
	}

	/**
	 * Transient object of the fixture class, holding its values in memory.
	 */
	private static class Item extends TransientObject {

		private final TLStructuredType _itemType;

		private final Map<TLStructuredTypePart, Object> _values = new HashMap<>();

		Item(TLStructuredType type) {
			_itemType = type;
		}

		@Override
		public TLStructuredType tType() {
			return _itemType;
		}

		@Override
		public Object tValue(TLStructuredTypePart part) {
			return _values.get(part);
		}

		@Override
		public void tUpdate(TLStructuredTypePart part, Object value) {
			_values.put(part, value);
		}
	}

}
