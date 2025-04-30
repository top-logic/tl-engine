/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.excel.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.base.office.PoiTypeHandler;
import com.top_logic.base.office.excel.CellSizeFormular;
import com.top_logic.base.office.excel.ExcelLocalLink;
import com.top_logic.base.office.excel.Formula;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;

/**
 * Manage {@link PoiTypeHandler} for different kinds of objects.
 * 
 * <p>The {@link PoiTypeHandler} can be used to set values into an excel sheet.</p>
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class POITypeProvider extends ManagedClass {

	/**
	 * Configuration options for {@link POITypeProvider}.
	 */
	public interface Config<I extends POITypeProvider> extends ServiceConfiguration<I> {

		/**
		 * Custom {@link PoiTypeHandler}s for exporting values of application-defined types.
		 */
		@Name("type-handlers")
		List<PolymorphicConfiguration<? extends PoiTypeHandler>> getTypeHandlers();

	}

	/** Cache for the helper when setting values to the POI cell. */
	private final Map<Class<?>, PoiTypeHandler> typeHandlerMap;

	/** Default {@link PoiTypeHandler}. */
	private final DefaultPOIType _defaultType;

	/**
	 * Creates a {@link POITypeProvider} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public POITypeProvider(InstantiationContext context, Config<?> config) {
		super(context, config);

        this.typeHandlerMap = new HashMap<>();
		_defaultType = this.createDefaultType();

        this.registerTypeHandler(new AmountPOIType());
        this.registerTypeHandler(new BinaryDataPOIType());
        this.registerTypeHandler(new BooleanPOIType());
        this.registerTypeHandler(new CalendarPOIType());
        this.registerTypeHandler(new CharacterPOIType());
        this.registerTypeHandler(new DataAccessProxyPOIType());
        this.registerTypeHandler(new DatePOIType());
        this.registerTypeHandler(new FilePOIType());
        this.registerTypeHandler(new FormulaPOIType(Formula.class));
        this.registerTypeHandler(new FormulaPOIType(CellSizeFormular.class));
        this.registerTypeHandler(new FormulaPOIType(ExcelLocalLink.class));
        this.registerTypeHandler(new IntegerPOIType());
        this.registerTypeHandler(new LongPOIType());
        this.registerTypeHandler(new NumberPOIType());
        this.registerTypeHandler(new RichTextStringPOIType());
        this.registerTypeHandler(new StringPOIType());

		for (PolymorphicConfiguration<? extends PoiTypeHandler> handlerConf : config.getTypeHandlers()) {
			registerTypeHandler(context.getInstance(handlerConf));
		}

		this.registerTypeHandler(_defaultType);
	}

	/**
	 * Return the matching {@link PoiTypeHandler} for the given object.
	 * 
	 * <p>
	 * If the given value is <code>null</code> or there is no matching handler for the class or one of
	 * its super classes, the {@link #createDefaultType()} will be returned!
	 * </p>
	 * 
	 * @param aValue
	 *        The value to get the handler for, may be <code>null</code>.
	 * @return The requested handler, never <code>null</code>.
	 */
	public PoiTypeHandler getPOITypeHandler(Object aValue) {
		if (aValue != null) {
			Class<? extends Object> theClass = aValue.getClass();

			do {
				PoiTypeHandler theHandler = this.typeHandlerMap.get(theClass);

				if (theHandler != null) {
					return theHandler;
				}
				else {
					theHandler = this.findByInterface(theClass.getInterfaces());

					if (theHandler != null) {
						return theHandler;
					}
					else { 
						theClass = theClass.getSuperclass();
					}
				}
			} while (theClass != Object.class);
		}

		return _defaultType;
	}

	/** 
     * Registers a new type handler.
     * 
     * @param    aHandler    The new handler, must not be <code>null</code>.
	 * @throws   IllegalArgumentException will be throws if the parameter is <code>null</code>
     */
	public void registerTypeHandler(PoiTypeHandler aHandler) {
		if (aHandler != null) {
			this.typeHandlerMap.put(aHandler.getHandlerClass(), aHandler);
		}
		else {
			throw new IllegalArgumentException("Parameter handler must not be null");
		}
	}

	/**
	 * Create the default value provider.
	 * 
	 * @return The requested value provider.
	 */
	protected DefaultPOIType createDefaultType() {
		return new DefaultPOIType();
	}

	private PoiTypeHandler findByInterface(Class<?>[] someInterfaces) {
		for (Class<?> theInterface : someInterfaces) {
			PoiTypeHandler theHandler = this.typeHandlerMap.get(theInterface);

			if (theHandler != null) {
				return theHandler;
			}

			// Find super interfaces.
			theHandler = this.findByInterface(theInterface.getInterfaces());

			if (theHandler != null) {
				return theHandler;
			}
		}

		return null;
	}

	/**
	 * The singleton {@link POITypeProvider}.
	 */
	public static POITypeProvider getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Module for {@link POITypeProvider}.
	 */
	public static final class Module extends TypedRuntimeModule<POITypeProvider> {

		/** Singleton {@link POITypeProvider.Module} instance. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// singleton instance
		}

		@Override
		public Class<POITypeProvider> getImplementation() {
			return POITypeProvider.class;
		}

	}

}

