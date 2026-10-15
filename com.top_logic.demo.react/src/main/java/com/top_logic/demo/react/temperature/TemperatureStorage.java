/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.react.temperature;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.element.meta.AttributeException;
import com.top_logic.element.meta.kbbased.storage.PrimitiveStorage;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * Storage of an attribute of type <code>demo.temperature:Temperature</code> that attaches the unit
 * of the owning object to the value it reads.
 *
 * <p>
 * The value is stored like any other primitive value, by the {@link TemperatureMapping} of the
 * type. Reading it, the storage determines the {@link #getUnit(TLObject) unit} of the object and
 * hands out the {@link Temperature} displayed in that unit. The unit is computed by the function
 * of the {@link TemperatureUnitAnnotation} of the attribute.
 * </p>
 *
 * <p>
 * The storage is chosen for every attribute of the type by the storage annotation of the type
 * itself:
 * </p>
 *
 * <pre>
 * &lt;datatype name="Temperature" db_type="double" kind="Custom"&gt;
 *   &lt;annotations&gt;
 *     &lt;storage-algorithm&gt;
 *       &lt;temperature-storage/&gt;
 *     &lt;/storage-algorithm&gt;
 *   &lt;/annotations&gt;
 *   &lt;temperature-mapping/&gt;
 * &lt;/datatype&gt;
 * </pre>
 */
public class TemperatureStorage<C extends TemperatureStorage.Config<?>> extends PrimitiveStorage<C> {

	/**
	 * Configuration options for {@link TemperatureStorage}.
	 */
	@TagName("temperature-storage")
	public interface Config<I extends TemperatureStorage<?>> extends PrimitiveStorage.Config<I> {
		// No additional options.
	}

	private QueryExecutor _unitFunction;

	/**
	 * Creates a {@link TemperatureStorage} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TemperatureStorage(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public void init(TLStructuredTypePart attribute) {
		super.init(attribute);

		TemperatureUnitAnnotation annotation = attribute.getAnnotation(TemperatureUnitAnnotation.class);
		if (annotation != null) {
			_unitFunction = QueryExecutor.compile(annotation.getUnit());
			_unitFunction.disableSecurity();
		}
	}

	@Override
	public Object getAttributeValue(TLObject object, TLStructuredTypePart attribute) throws AttributeException {
		Object value = super.getAttributeValue(object, attribute);
		if (value instanceof Temperature temperature) {
			return temperature.withUnit(getUnit(object));
		}
		return value;
	}

	/**
	 * The unit in which the attribute of this storage is displayed and entered for the given
	 * object.
	 *
	 * @param object
	 *        The object owning the attribute, or <code>null</code>.
	 * @return The unit the {@link TemperatureUnitAnnotation} of the attribute computes for the
	 *         object, {@link TemperatureUnit#CELSIUS} if it computes none or there is no such
	 *         annotation.
	 */
	public TemperatureUnit getUnit(TLObject object) {
		if (_unitFunction == null || object == null) {
			return TemperatureUnit.CELSIUS;
		}
		Object result = _unitFunction.execute(object);
		TemperatureUnit unit = result instanceof TLClassifier classifier ? TemperatureUnit.fromClassifier(classifier) : null;
		return unit == null ? TemperatureUnit.CELSIUS : unit;
	}

}
