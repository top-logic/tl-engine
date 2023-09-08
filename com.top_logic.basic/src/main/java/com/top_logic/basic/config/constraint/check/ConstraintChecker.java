/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.constraint.check;

import static java.util.Objects.*;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.Log;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.NamePath;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyDescriptorImpl;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.constraint.ConstraintAnnotation;
import com.top_logic.basic.config.constraint.ConstraintFactory;
import com.top_logic.basic.config.constraint.ConstraintSpec;
import com.top_logic.basic.config.constraint.algorithm.ConstraintAlgorithm;
import com.top_logic.basic.config.constraint.algorithm.DefaultPropertyModel;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.constraint.annotation.OverrideConstraints;
import com.top_logic.basic.config.misc.PropertyValue;
import com.top_logic.basic.i18n.log.I18NLog;
import com.top_logic.basic.logging.Level;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKeyUtil;
import com.top_logic.basic.util.ResourcesModule;

/**
 * Algorithm checking {@link Constraint}s annotated to typed configurations.
 * 
 * @see #check(ConfigurationItem)
 * @see #getFailures()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ConstraintChecker {

	private List<ConstraintFailure> _errors = new ArrayList<>();

	private Annotation _currentAnnotation;

	private PropertyDescriptor _currentProperty;

	/**
	 * Creates a {@link ConstraintChecker}.
	 * 
	 * <p>
	 * Note: The resulting instance is not thread-safe.
	 * </p>
	 */
	public ConstraintChecker() {
		super();
	}

	/**
	 * Checks the given {@link ConfigurationItem} for constraint violations.
	 * <p>
	 * The check is recursive: The inner items of the given item are checked, too.
	 * </p>
	 * <p>
	 * The failures are reported to the given {@link Log}. They are not thrown as exceptions.
	 * </p>
	 * 
	 * @param log
	 *        Is not allowed to be null.
	 * @param item
	 *        If null, nothing is done.
	 * 
	 * @see #check(I18NLog, ConfigurationItem)
	 */
	public void check(Log log, ConfigurationItem item) {
		requireNonNull(log);
		try {
			check(item);
		} catch (ConfigurationException exception) {
			log.error(exception.getMessage(), exception);
		} finally {
			getFailures().forEach(failure -> handleFailure(log, failure));
			getFailures().clear();
		}
	}

	private void handleFailure(Log log, ConstraintFailure failure) {
		if (failure.isWarning()) {
			log.info(ResKeyUtil.getTranslation(failure.getMessage(), ResourcesModule.getLogLocale()), Protocol.WARN);
		} else {
			log.error("Constraint violation.", new ConfigurationError(failure.getMessage()));
		}
	}

	/**
	 * Checks the given {@link ConfigurationItem} for constraint violations.
	 * <p>
	 * The check is recursive: The inner items of the given item are checked, too.
	 * </p>
	 * <p>
	 * The failures are reported to the given {@link I18NLog}. They are not thrown as exceptions.
	 * </p>
	 * 
	 * @param log
	 *        Is not allowed to be null.
	 * @param item
	 *        If null, nothing is done.
	 * 
	 * @see #check(Log, ConfigurationItem)
	 */
	public void check(I18NLog log, ConfigurationItem item) {
		requireNonNull(log);
		try {
			check(item);
		} catch (ConfigurationException exception) {
			log.error(exception.getErrorKey());
		} finally {
			getFailures().forEach(failure -> handleFailure(log, failure));
			getFailures().clear();
		}
	}

	private void handleFailure(I18NLog log, ConstraintFailure failure) {
		if (failure.isWarning()) {
			log.log(Level.WARN, failure.getMessage());
		} else {
			log.error(failure.getMessage());
		}
	}

	/**
	 * Starts the check on the given top-level configuration.
	 * 
	 * @param config
	 *        The configuration to check (including all its parts).
	 * @throws ConfigurationException
	 *         If errors exist in the syntax of constraint annotations.
	 */
	public void check(ConfigurationItem config) throws ConfigurationException {
		if (config == null) {
			return;
		}

		for (PropertyDescriptor property : config.descriptor().getProperties()) {
			processProperty(config, property, property.getDescriptor(), property);
		}

		descend(config);
	}

	/**
	 * The list of constraint violations found.
	 */
	public List<ConstraintFailure> getFailures() {
		return _errors;
	}

	private void processProperty(ConfigurationItem config, PropertyDescriptor property,
			ConfigurationDescriptor annotatedDescriptor, PropertyDescriptor annotatedProperty)
			throws ConfigurationException {
		boolean override = processLocalAnnotations(config, property, annotatedProperty.getLocalAnnotations());
		if (!override) {
			for (ConfigurationDescriptor superDescriptor : annotatedDescriptor.getSuperDescriptors()) {
				PropertyDescriptor superProperty = superDescriptor.getProperty(property.getPropertyName());
				if (superProperty == null) {
					continue;
				}

				processProperty(config, property, superDescriptor, superProperty);
			}
		}
	}

	private boolean processLocalAnnotations(ConfigurationItem config, PropertyDescriptor property,
			Annotation[] localAnnotations) throws ConfigurationException {
		boolean override = false;
		for (Annotation annotation : localAnnotations) {
			Class<? extends Annotation> annotationType = annotation.annotationType();
			if (annotationType == OverrideConstraints.class) {
				override = true;
				continue;
			}
			ConstraintAnnotation metaAnnotation = annotationType.getAnnotation(ConstraintAnnotation.class);
			if (metaAnnotation == null) {
				continue;
			}

			checkAnnotation(config, property, annotation, metaAnnotation);
		}
		return override;
	}

	private void checkAnnotation(ConfigurationItem config, PropertyDescriptor property,
			Annotation annotation, ConstraintAnnotation metaAnnotation) throws ConfigurationException {
		_currentProperty = property;
		_currentAnnotation = annotation;

		@SuppressWarnings("unchecked")
		ConstraintFactory<Annotation> factory = ConfigUtil.getInstance(metaAnnotation.value());

		for (ConstraintSpec spec : factory.createConstraint(annotation)) {
			checkConstraint(config, property, spec);
		}

		_currentAnnotation = null;
		_currentProperty = null;
	}

	private void checkConstraint(ConfigurationItem config, PropertyDescriptor property, ConstraintSpec spec) {
		ConstraintAlgorithm algorithm = spec.getAlgorithm();
		Ref[] argSpecs = spec.getRelated();
		DefaultPropertyModel<?>[] models = resolveArguments(config, property, argSpecs);
		if (models != null) {
			algorithm.check(models);
			checkResult(config, spec, models);
		}
	}

	private DefaultPropertyModel<?>[] resolveArguments(ConfigurationItem config, PropertyDescriptor property,
			Ref[] argSpecs) {
		DefaultPropertyModel<?>[] models = new DefaultPropertyModel<?>[argSpecs.length + 1];
		models[0] = createModel(config, property);
		for (int n = 0, cnt = argSpecs.length; n < cnt; n++) {
			DefaultPropertyModel<?> arg = resolveArg(config, argSpecs[n]);
			if (arg == null) {
				return null;
			}
			models[1 + n] = arg;
		}
		return models;
	}

	private DefaultPropertyModel<?> resolveArg(ConfigurationItem config, Ref ref) {
		PropertyValue value = NamePath.resolve(ref, config);
		if (value == null) {
			return null;
		}
		return createModel(value.getItem(), value.getProperty());
	}

	private void checkResult(ConfigurationItem checkedItem, ConstraintSpec spec, DefaultPropertyModel<?>[] models) {
		for (DefaultPropertyModel<?> model : models) {
			ResKey problemDescription = model.getProblemDescription();
			if (problemDescription == null) {
				continue;
			}

			_errors.add(new ConstraintFailure(checkedItem, spec.asWarning(), model.getProperty(), problemDescription));
		}
	}


	private DefaultPropertyModel<Object> createModel(ConfigurationItem config, PropertyDescriptor property) {
		return new DefaultPropertyModel<>(config, property, getPropertyNameKey(property));
	}

	/**
	 * Constructs a {@link ResKey} describing the given property.
	 */
	protected ResKey getPropertyNameKey(PropertyDescriptor property) {
		return ResKey.text(property.getPropertyName());
	}

	private String context() {
		return "While checking constraint '" + _currentAnnotation + "' of property '" + _currentProperty + "': ";
	}

	private void descend(ConfigurationItem config) throws ConfigurationException {
		for (PropertyDescriptor property : config.descriptor().getProperties()) {
			switch (property.kind()) {
				case ITEM: {
					checkItem(config, property);
					break;
				}
				case ARRAY: {
					checkArray(config, property);
					break;
				}
				case LIST: {
					checkList(config, property);
					break;
				}
				case MAP: {
					checkMap(config, property);
					break;
				}
				default:
					break;
			}
		}
	}

	private void checkMap(ConfigurationItem config, PropertyDescriptor property) throws ConfigurationException {
		Map<?, ?> values = (Map<?, ?>) config.value(property);
		if (values == null) {
			return;
		}
		checkIterable(values.values());
	}

	private void checkArray(ConfigurationItem config, PropertyDescriptor property) throws ConfigurationException {
		List<?> values = PropertyDescriptorImpl.arrayAsList(config.value(property));
		if (values == null) {
			return;
		}
		checkIterable(values);
	}

	private void checkList(ConfigurationItem config, PropertyDescriptor property) throws ConfigurationException {
		List<?> values = (List<?>) config.value(property);
		if (values == null) {
			return;
		}
		checkIterable(values);
	}

	private void checkItem(ConfigurationItem config, PropertyDescriptor property) throws ConfigurationException {
		Object value = config.value(property);
		if (value == null) {
			return;
		}
		check(getConfig(value));
	}

	private void checkIterable(Iterable<?> values) throws ConfigurationException {
		for (Object value : values) {
			check(getConfig(value));
		}
	}

	private ConfigurationItem getConfig(Object value) {
		if (!(value instanceof ConfigurationItem)) {
			/* It makes no sense to check a value that is not a ConfigurationItem. Even if the value
			 * is ConfiguredInstance, the object was already built such that everything actually
			 * worked. */
			return null;
		}
		return (ConfigurationItem) value;
	}

}
