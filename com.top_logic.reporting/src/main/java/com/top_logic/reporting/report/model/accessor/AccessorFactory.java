/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.report.model.accessor;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.knowledge.wrap.WrapperAccessor;
import com.top_logic.layout.Accessor;
import com.top_logic.reporting.report.exception.ReportingException;

/**
 * This factory produces {@link Accessor}s for (data) object.
 * 
 * @author <a href="mailto:fsc@top-logic.com">Friedemann Schneider</a>
 */
@Deprecated
public class AccessorFactory extends AbstractConfigurationValueProvider<Accessor<?>> {

	/** singleton */
	public static final AccessorFactory	INSTANCE		     	= new AccessorFactory();

	public static final String			WRAPPER_ACCESSOR	    = "wrapper";
	public static final String			DEFAULT_ACCESSOR	    = "default";
	public static final String			PAYMENT_ACCESSOR	    = "paymentplan";

	public HashMap						accessors;

	private AccessorFactory() {
		super(Accessor.class);
		this.accessors = new HashMap();
		this.accessors.put(WRAPPER_ACCESSOR, WrapperAccessor.INSTANCE);
		this.accessors.put(DEFAULT_ACCESSOR, WrapperAccessor.INSTANCE);
		this.accessors.put(PAYMENT_ACCESSOR, WrapperAccessor.INSTANCE);
	}

	public Accessor getAccessor(String anAccessorName, boolean returnDefaultAccessor) {

		// return cached accessor
		if (this.accessors.containsKey(anAccessorName.toLowerCase())) {
			return (Accessor) this.accessors.get(anAccessorName.toLowerCase());
		}
		else {
			// try to create a accessor (implicit assuming anAccessorName to be a full qualified
			// classname)
			Accessor theAccessor = createAccessor(anAccessorName);
			if (theAccessor != null) {
				this.accessors.put(anAccessorName, theAccessor); // cache the instance, should be a
																	// singleton
				return theAccessor;
			}

			// return the default accessor, if wanted
			if (returnDefaultAccessor) {
				return (Accessor) this.accessors.get(DEFAULT_ACCESSOR);
			}
			else {
				throw new ReportingException(AccessorFactory.class, "No accessor found for "
						+ anAccessorName);
			}
		}
	}

	private Accessor createAccessor(String aClassName) {
		try {
			Class theClass = Class.forName(aClassName);
			return createAccessorInstance(theClass);
		}
		catch (ClassNotFoundException cnfx) {
			Logger.info("Unable to instanciate accessor from " + aClassName, cnfx, this);
		}
		return null;
	}

	private Accessor createAccessorInstance(Class aClass) {
		try {
			Accessor theAccessor = (Accessor) aClass.getDeclaredConstructor().newInstance();
			return theAccessor;
		}
		catch (IllegalAccessException iax) {
			// ok
			Logger.info("Unable to instanciate accessor from " + aClass.getName(), iax, this);
		}
		catch (InstantiationException iex) {
			// ok
			Logger.info("Unable to instanciate accessor from " + aClass.getName(), iex, this);
		} catch (InvocationTargetException iex) {
			Logger.info("Unable to instanciate accessor from " + aClass.getName(), iex, this);
		} catch (NoSuchMethodException iex) {
			Logger.info("Unable to instanciate accessor from " + aClass.getName(), iex, this);
		}
		return null;
	}

	@Override
	public String getSpecificationNonNull(Accessor<?> aConfigValue) {
	    return DEFAULT_ACCESSOR;
	}
	
	@Override
	public Accessor<?> getValueNonEmpty(String aPropertyName, CharSequence aPropertyValue) throws ConfigurationException {
	    return WrapperAccessor.INSTANCE;
	}
	
}
