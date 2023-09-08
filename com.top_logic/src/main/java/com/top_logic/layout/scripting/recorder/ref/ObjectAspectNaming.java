/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Arrays;
import java.util.Objects;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;

/**
 * Generic {@link UnrecordableNamingScheme} to access an aspect of a given base object.
 * 
 * @see ObjectAspectName
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ObjectAspectNaming extends UnrecordableNamingScheme<Object, ObjectAspectNaming.ObjectAspectName> {

	/**
	 * {@link ModelName} representing an aspect of a {@link ObjectAspectName base object}.
	 * 
	 * <p>
	 * This {@link ModelName} consists of a {@link #getBaseObject() base object}, from which the
	 * aspect is requested, and the {@link #getArguments() arguments} which are needed to get the
	 * required aspect.
	 * </p>
	 * 
	 * <p>
	 * For the simple case that the aspect can be derived from the base object with no, one, or two
	 * arguments, the developer should subclass {@link ObjectAspectName0},
	 * {@link ObjectAspectName1}, or {@link ObjectAspectName2} to bind type parameter.
	 * </p>
	 * 
	 * @see ObjectAspectName0
	 * @see ObjectAspectName1
	 * @see ObjectAspectName2
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@Abstract
	public interface ObjectAspectName extends ModelName {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		/**
		 * The name of the base object to get aspect from.
		 */
		@Mandatory
		ModelName getBaseObject();

		/**
		 * Setter for {@link #getBaseObject()}
		 */
		void setBaseObject(ModelName baseObject);

		/**
		 * Arguments that are needed to get the aspect from {@link #getBaseObject()}.
		 */
		ModelName[] getArguments();

		/**
		 * Setter for {@link #getArguments()}.
		 */
		void setArguments(ModelName[] arguments);

		/**
		 * Generic method to fetch the required aspect.
		 * 
		 * @param baseObject
		 *        The resolved {@link #getBaseObject()}.
		 * @param arguments
		 *        The resolved {@link #getArguments()}.
		 * @throws Exception
		 *         May be thrown when an error occurs.
		 */
		default Object invoke(Object baseObject, Object[] arguments) throws Exception {
			throw new UnsupportedOperationException();
		}

	}

	/**
	 * {@link ObjectAspectName} for an aspect which needs no arguments.
	 * 
	 * <p>
	 * The developer should implement {@link #getAspect(Object)}.
	 * </p>
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@Abstract
	public interface ObjectAspectName0<R, M> extends ObjectAspectName {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		@Override
		@SuppressWarnings("unchecked")
		default Object invoke(Object baseObject, Object[] args) throws Exception {
			return getAspect((M) baseObject);
		}

		/**
		 * Fetches the required aspect from the given base object.
		 * 
		 * @param baseObject
		 *        The Base object to get aspect from.
		 * @return The required aspect.
		 * @throws Exception
		 *         In case an error occurred.
		 */
		default R getAspect(M baseObject) throws Exception {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * {@link ObjectAspectName} for an aspect which needs one argument.
	 * 
	 * <p>
	 * The developer should implement {@link #getAspect(Object, Object)}.
	 * </p>
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@Abstract
	public interface ObjectAspectName1<R, M, A1> extends ObjectAspectName {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		@Override
		@SuppressWarnings("unchecked")
		default Object invoke(Object baseObject, Object[] arguments) throws Exception {
			return getAspect((M) baseObject, (A1) arguments[0]);
		}

		/**
		 * Fetches the required aspect from the given base object.
		 * 
		 * @param baseObject
		 *        The Base object to get aspect from.
		 * @param argument1
		 *        The argument needed to get aspect.
		 * @return The required aspect.
		 * @throws Exception
		 *         In case an error occurred.
		 */
		default R getAspect(M baseObject, A1 argument1) throws Exception {
			throw new UnsupportedOperationException();
		}

	}

	/**
	 * {@link ObjectAspectName} for an aspect which needs two arguments.
	 * 
	 * <p>
	 * The developer should implement {@link #getAspect(Object, Object,Object)}.
	 * </p>
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@Abstract
	public interface ObjectAspectName2<R, M, A1, A2> extends ObjectAspectName {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		@Override
		@SuppressWarnings("unchecked")
		default Object invoke(Object baseObject, Object[] arguments) throws Exception {
			return getAspect((M) baseObject, (A1) arguments[0], (A2) arguments[1]);
		}

		/**
		 * Fetches the required aspect from the given base object.
		 * 
		 * @param baseObject
		 *        The Base object to get aspect from.
		 * @param argument1
		 *        The first argument needed to get aspect.
		 * @param argument2
		 *        The second argument needed to get aspect.
		 * @return The required aspect.
		 * @throws Exception
		 *         In case an error occurred.
		 */
		default R getAspect(M baseObject, A1 argument1, A2 argument2) throws Exception {
			throw new UnsupportedOperationException();
		}

	}

	/**
	 * Creates a new {@link ObjectAspectNaming}.
	 */
	public ObjectAspectNaming() {
		super(Object.class, ObjectAspectName.class);
	}

	@Override
	public Object locateModel(ActionContext context, ObjectAspectName name) {
		Object baseObject = context.resolve(name.getBaseObject());
		ModelName[] argumentNames = name.getArguments();
		Object[] arguments = new Object[argumentNames.length];
		for (int i = 0; i < argumentNames.length; i++) {
			arguments[i] = context.resolve(argumentNames[i]);
		}
		try {
			return name.invoke(baseObject, arguments);
		} catch (Exception ex) {
			throw ApplicationAssertions.fail(name, "Unable to get aspect.", ex);
		}
	}

	/**
	 * Builds an {@link ObjectAspectName} of the given type.
	 * 
	 * @param nameClass
	 *        The concrete {@link ObjectAspectName name type}.
	 * @param baseObject
	 *        The object to get aspect from.
	 * @param arguments
	 *        Arguments that are needed to get the aspect.
	 */
	public static <T extends ObjectAspectName> T buildName(Class<T> nameClass, Object baseObject,
			Object... arguments) {
		/* Build name without context, because the ObjectAspectNaming also ignores the context
		 * during location. Therefore, if building the name would need a context, resolving would
		 * fail. */
		ModelName modelName = ModelResolver.buildModelName(baseObject);
		ModelName[] argNames = new ModelName[arguments.length];
		for (int i = 0; i < arguments.length; i++) {
			argNames[i] = ModelResolver.buildModelName(arguments[i]);
		}
		return buildNameFromModelNames(nameClass, modelName, argNames);
	}

	/**
	 * /** Builds an {@link ObjectAspectName} of the given type.
	 * 
	 * @param nameClass
	 *        The concrete {@link ObjectAspectName name type}.
	 * @param baseObject
	 *        The model name of the object to get aspect from.
	 * @param arguments
	 *        Model names of the arguments that are needed to get the aspect.
	 */
	public static <T extends ObjectAspectName> T buildNameFromModelNames(Class<T> nameClass, ModelName baseObject,
			ModelName... arguments) {
		requireNonNull(baseObject, arguments);

		T newName = TypedConfiguration.newConfigItem(nameClass);
		newName.setBaseObject(baseObject);
		newName.setArguments(arguments);
		return newName;
	}

	private static void requireNonNull(ModelName baseObject, ModelName... arguments) {
		Objects.requireNonNull(baseObject);
		Objects.requireNonNull(arguments);
		for (ModelName argument : arguments) {
			if (argument == null) {
				throw new NullPointerException("Null as argument is not allowed: " + Arrays.toString(arguments));
			}
		}
	}

}
