/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.transform;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.Protocol;
import com.top_logic.model.TLModel;

/**
 * {@link ModelTransformation} that executes multiple
 * {@link ModelTransformation}s given by their {@link Class}es.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class GenericModelTransformer extends ModelTransformation {

	private final List<Class<? extends ModelTransformation>> transformationClasses;

	private GenericModelTransformer(Protocol protocol, TLModel index, List<Class<? extends ModelTransformation>> transformationClasses) {
		super(protocol, index);
		
		this.transformationClasses = transformationClasses;
	}

	@Override
	public void transform() {
		try {
			for (Class<? extends ModelTransformation> transformerClass : transformationClasses) {
				Constructor<? extends ModelTransformation> transformationConstructor = 
					transformerClass.getConstructor(Protocol.class, TLModel.class);
				ModelTransformation transformation = transformationConstructor.newInstance(log, index);
				transformation.transform();
				
				if (log.hasErrors()) {
					return;
				}
			}
		} catch (NoSuchMethodException ex) {
			log.error("Transformation failed.", ex);
		} catch (InstantiationException ex) {
			log.error("Transformation failed.", ex);
		} catch (IllegalAccessException ex) {
			log.error("Transformation failed.", ex);
		} catch (InvocationTargetException ex) {
			log.error("Transformation failed.", ex);
		}
	}

	/**
	 * Parses the given comma separated class names into a list of
	 * {@link ModelTransformation} classes.
	 * 
	 * @param classNames
	 *        The comma separated list of class names to parse.
	 * @return A list of {@link ModelTransformation} classes.
	 * 
	 * @throws IllegalArgumentException
	 *         If parsing fails.
	 */
	public static List<Class<? extends ModelTransformation>> parseTransformerClasses(String classNames) throws IllegalArgumentException {
		String[] classNamesArray = classNames.trim().split("[\\s\\r\\n;,]+");
		return parseTransformerClasses(classNamesArray);
	}

	/**
	 * Parses the given class names into a list of {@link ModelTransformation}
	 * classes.
	 * 
	 * @param classNames
	 *        The class names to parse.
	 * @return A list of {@link ModelTransformation} classes.
	 * 
	 * @throws IllegalArgumentException
	 *         If parsing fails.
	 */
	public static List<Class<? extends ModelTransformation>> parseTransformerClasses(String[] classNames) throws IllegalArgumentException {
		try {
			List<Class<? extends ModelTransformation>> tranformerClasses = new ArrayList<>();
			for (String className : classNames) {
				Class<?> transformerClass = Class.forName(className);
				if (! ModelTransformation.class.isAssignableFrom(transformerClass)) {
					throw new IllegalArgumentException("Transformer '" + transformerClass.getName() + "' not a subclass of '" + ModelTransformation.class.getName() + "'.");
				}
				tranformerClasses.add((Class<? extends ModelTransformation>) transformerClass);
			}
			
			return tranformerClasses;
		} catch (ClassNotFoundException ex) {
			throw new IllegalArgumentException("Transformer class not found.", ex);
		}
	}

	/**
	 * Creates a {@link GenericModelTransformer} from the given
	 * {@link ModelTransformation} classes.
	 * 
	 * @param log
	 *        The protocol for the transformation.
	 * @param index
	 *        The indexed model to transform.
	 * @param transformationClasses
	 *        the {@link ModelTransformation} classes.
	 * @return A new {@link GenericModelTransformer} that executes all given
	 *         transformations.
	 */
	public static GenericModelTransformer createGenericModelTransformer(Protocol log, TLModel index, List<Class<? extends ModelTransformation>> transformationClasses) {
		return new GenericModelTransformer(log, index, transformationClasses);
	}

	/**
	 * Creates a {@link GenericModelTransformer} from the given
	 * {@link ModelTransformation} classes.
	 * 
	 * @param log
	 *        The protocol for the transformation.
	 * @param index
	 *        The indexed model to transform.
	 * @param transformationClasseNames
	 *        the {@link ModelTransformation} class names.
	 * @return A new {@link GenericModelTransformer} that executes all given
	 *         transformations.
	 * 
	 * @throws IllegalArgumentException
	 *         If parsing transformation class names fails.
	 */
	public static GenericModelTransformer createGenericModelTransformer(Protocol log, TLModel index, String[] transformationClasseNames) throws IllegalArgumentException {
		return new GenericModelTransformer(log, index, parseTransformerClasses(transformationClasseNames));
	}

	/**
	 * Creates a {@link GenericModelTransformer} from the given
	 * {@link ModelTransformation} classes.
	 * 
	 * @param log
	 *        The protocol for the transformation.
	 * @param index
	 *        The indexed model to transform.
	 * @param transformationClasseNames
	 *        the {@link ModelTransformation} class names.
	 * @return A new {@link GenericModelTransformer} that executes all given
	 *         transformations.
	 * 
	 * @throws IllegalArgumentException
	 *         If parsing transformation class names fails.
	 */
	public static GenericModelTransformer createGenericModelTransformer(Protocol log, TLModel index, String transformationClasseNames) throws IllegalArgumentException {
		return new GenericModelTransformer(log, index, parseTransformerClasses(transformationClasseNames));
	}
	
}
