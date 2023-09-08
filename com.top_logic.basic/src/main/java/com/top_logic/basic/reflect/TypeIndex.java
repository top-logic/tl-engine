/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.reflect;

import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.io.rw.ReaderR;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.xref.model.AnnotationInfo;
import com.top_logic.xref.model.IndexFile;
import com.top_logic.xref.model.StringValue;

/**
 * Service for looking up specializations of certain types.
 * 
 * <p>
 * This service depends on <code>META-INF/com.top_logic.basic.reflect.TypeIndex.json</code>
 * available on the class path. This information is generated during compilation by annotation
 * processing.
 * </p>
 * 
 * @see "Module tl-build_processor"
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TypeIndex extends ManagedClass {

	private final Map<String, ClassInfo> _types = new HashMap<>();

	/**
	 * Creates a {@link TypeIndex}.
	 */
	@CalledByReflection
	public TypeIndex() {
		super();
	}

	/**
	 * Looks all public specializations of a given {@link Class}.
	 * 
	 * @param type
	 *        The base type.
	 * @param transitive
	 *        whether to report specializations of specializations.
	 * @param onlyInterfaces
	 *        Whether to restrict the result to interfaces.
	 * @param includeAbstract
	 *        Whether to report abstract types. If the search is not restricted to interfaces, a
	 *        type is considered abstract if the type is an abstract class or interface. If the
	 *        search is restricted to interfaces, a type is considered abstract, if the interface is
	 *        annotated {@link Abstract}.
	 * @return Types specializing the given base type.
	 */
	public Collection<Class<?>> getSpecializations(Class<?> type, boolean transitive,
			boolean onlyInterfaces, boolean includeAbstract) {
		TypeDescriptor info = getDescriptor(type);
		if (info == null) {
			return Collections.emptyList();
		}
		Collection<Class<?>> result = new ArrayList<>();
		if (transitive) {
			loadClasses(result, new HashSet<>(), onlyInterfaces, includeAbstract, info);
		} else {
			for (TypeDescriptor specialization : info.getSpecializations()) {
				loadClass(result, onlyInterfaces, includeAbstract, specialization);
			}
		}
		return result;
	}

	/**
	 * A descriptor for the given type.
	 */
	public TypeDescriptor getDescriptor(Class<?> type) {
		return getDescriptor(type.getName());
	}

	/**
	 * A descriptor the the type with the given name.
	 * 
	 * @param typeName
	 *        The fully qualified name of a Java class.
	 */
	public TypeDescriptor getDescriptor(String typeName) {
		return _types.get(typeName);
	}

	private void loadClasses(Collection<Class<?>> result, Set<TypeDescriptor> seen, boolean onlyInterfaces,
			boolean includeAbstract, TypeDescriptor info) {
		if (!seen.add(info)) {
			// This branch has already been visited, avoid duplicates.
			return;
		}

		loadClass(result, onlyInterfaces, includeAbstract, info);
		for (TypeDescriptor specialization : info.getSpecializations()) {
			loadClasses(result, seen, onlyInterfaces, includeAbstract, specialization);
		}
	}

	private void loadClass(Collection<Class<?>> result, boolean onlyInterfaces, boolean includeAbstract,
			TypeDescriptor info) {
		if (!info.isPublic()) {
			return;
		}

		if (onlyInterfaces) {
			if (!info.isInterface()) {
				return;
			}
			if (!includeAbstract) {
				if (info.isAbstract()) {
					return;
				}
			}
		} else {
			if (!includeAbstract) {
				if (info.isInterface()) {
					return;
				}
				if (info.isAbstract()) {
					return;
				}
			}
		}

		try {
			Class<?> clazz = Class.forName(info.getClassName());
			result.add(clazz);
		} catch (NoClassDefFoundError | ClassNotFoundException ex) {
			Logger.error("Indexed class '" + info.getClassName() + "' not found.", ex, TypeIndex.class);
		}
	}

	/**
	 * All configuration interface types by their {@link TagName tag name} for the given
	 * implementation type.
	 *
	 * @param instanceType
	 *        The configured instance type.
	 * @return Map of tag name to the configuration type for a specialized implementation of the
	 *         given instance type.
	 */
	public Map<String, Class<?>> implTagNames(Class<?> instanceType) {
		String instanceName = instanceType.getName();
		ClassInfo instanceInfo = _types.get(instanceName);
		if (instanceInfo == null) {
			return Collections.emptyMap();
		}
	
		HashMap<String, Class<?>> result = new HashMap<>();
	
		reflexiveTransitiveSpecializations(instanceInfo, specialization -> {
			for (ClassInfo config : specialization.getConfigurationOptions()) {
				String tagName = config.getTagName();
				if (tagName != null) {
					Class<?> configClass;
					try {
						configClass = Class.forName(config.getClassName());
					} catch (ClassNotFoundException ex) {
						Logger.error("Configuration class '" + config.getClassName()
							+ "' cannot be resolved when searching for configuration options for " + instanceName
							+ ".", TypeIndex.class);
						continue;
					}
	
					Class<?> clash = result.put(tagName, configClass);
					if (clash != null) {
						Logger.error("Duplicate tag name '" + tagName + "' for " + clash.getName() + " and "
							+ config.getClassName() + " when searching for configuration options for " + instanceName
							+ ".", TypeIndex.class);
					}
				}
			}
		});
	
		return result;
	}

	/**
	 * All specialized configuration interface types by their {@link TagName tag name} for the given
	 * configuration interface.
	 *
	 * @param configType
	 *        The configuration interface type to search specialized configuration with tag name
	 *        for.
	 * @return Map of tag name to the specialized configuration type.
	 */
	public Map<String, Class<?>> configTagNames(Class<?> configType) {
		String configName = configType.getName();
		ClassInfo configInfo = _types.get(configName);
		if (configInfo == null) {
			return Collections.emptyMap();
		}
	
		TagMapBuilder builder = new TagMapBuilder(configName);
		reflexiveTransitiveSpecializations(configInfo, builder);
		return builder.getResult();
	}

	private void reflexiveTransitiveSpecializations(ClassInfo type, Consumer<? super ClassInfo> consumer) {
		reflexiveTransitiveSpecializations(new HashSet<>(), type, consumer);
	}

	private void reflexiveTransitiveSpecializations(Set<ClassInfo> seen, ClassInfo type,
			Consumer<? super ClassInfo> consumer) {
		if (!seen.add(type)) {
			return;
		}
		consumer.accept(type);
		for (ClassInfo specialization : type.getSpecializations()) {
			reflexiveTransitiveSpecializations(seen, specialization, consumer);
		}
	}

	@Override
	protected void startUp() {
		super.startUp();

		try {
			Enumeration<URL> resources =
				Thread.currentThread().getContextClassLoader()
					.getResources("META-INF/com.top_logic.basic.reflect.TypeIndex.json");
			int cnt = 0;
			while (resources.hasMoreElements()) {
				cnt++;
				URL resource = resources.nextElement();
				try (InputStream in = resource.openStream(); Reader r = new InputStreamReader(in, "utf-8")) {
					IndexFile typeInfos = IndexFile.readIndexFile(new JsonReader(new ReaderR(r)));

					for (Entry<String, com.top_logic.xref.model.TypeInfo> entry : typeInfos.getTypes().entrySet()) {
						String className = entry.getKey();
						com.top_logic.xref.model.TypeInfo infoSpec = entry.getValue();

						internalParse(mkInfo(className), infoSpec);
					}
				}
			}
			if (cnt == 0) {
				Logger.error("No type index specifications are found, " +
					"check whether annotation processing is enabled during compilation", TypeIndex.class);
			} else if (cnt == 1) {
				Logger.error("Only a single type index specifications was found. " +
					"This means that something is wrong with the class loader.", TypeIndex.class);
			}
			for (ClassInfo classInfo : _types.values()) {
				classInfo.optimize();
			}
		} catch (IOException ex) {
			throw new IOError(ex);
		}
	}

	private ClassInfo mkInfo(String className) {
		ClassInfo info = _types.get(className);
		if (info == null) {
			info = new ClassInfo(className);
			_types.put(className, info);
		}
		return info;
	}

	private void internalParse(ClassInfo classInfo, com.top_logic.xref.model.TypeInfo infoSpec) {
		classInfo.setPublic(infoSpec.isPublic());
		classInfo.setAbstract(infoSpec.isAbstract());
		classInfo.setInterface(infoSpec.isInterface());

		for (String generalization : infoSpec.getGeneralizations()) {
			linkTo(classInfo, generalization);
		}
		
		String configuration = infoSpec.getConfiguration();
		if (!StringServices.isEmpty(configuration)) {
			classInfo.setConfiguration(mkInfo(configuration));
		}
		
		String implementation = infoSpec.getImplementation();
		if (!StringServices.isEmpty(implementation)) {
			mkInfo(implementation).addConfigurationOption(classInfo);
		}

		AnnotationInfo annotationInfo = infoSpec.getAnnotations().get(TagName.class.getName());
		if (annotationInfo != null) {
			StringValue value = (StringValue) annotationInfo.getProperties().get(TagName.VALUE);
			classInfo.setTagName(value.getValue());
		}
	}

	private void linkTo(ClassInfo classInfo, String generalization) {
		mkInfo(generalization).addSpecialization(classInfo);
	}

	/**
	 * The singleton {@link TypeIndex} instance.
	 */
	public static TypeIndex getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	private static final class TagMapBuilder implements Consumer<ClassInfo> {
		private final Map<String, Class<?>> _result = new HashMap<>();

		private final String _configName;

		private boolean _clash;

		/**
		 * Creates a {@link TagMapBuilder}.
		 */
		public TagMapBuilder(String configName) {
			_configName = configName;
		}

		@Override
		public void accept(ClassInfo specialization) {
			String tagName = specialization.getTagName();
			if (tagName == null) {
				return;
			}

			String specializationName = specialization.getClassName();
			try {
				Class<?> specializationClass = Class.forName(specializationName);
				Class<?> clash = _result.put(tagName, specializationClass);
				if (clash != null && clash != Void.class) {
					Logger.error("Duplicate tag name '" + tagName + "' for " + clash.getName() + " and "
						+ specializationName + " when searching for options for " + _configName + ".", TypeIndex.class);

					// Mark clash.
					_result.put(tagName, Void.class);
					_clash = true;
				}
			} catch (ClassNotFoundException ex) {
				Logger.error("Configuration class '" + specializationName
					+ "' cannot be resolved when searching for options for " + _configName + ".", TypeIndex.class);
			}
		}

		public Map<String, Class<?>> getResult() {
			if (_clash) {
				removeClashes();
			}
			return _result;
		}

		private void removeClashes() {
			for (Iterator<Entry<String, Class<?>>> it = _result.entrySet().iterator(); it.hasNext();) {
				Entry<String, Class<?>> entry = it.next();
				if (entry.getValue() == Void.class) {
					it.remove();
				}
			}
		}
	}

	/**
	 * Singleton reference of {@link TypeIndex}.
	 */
	public static final class Module extends BasicRuntimeModule<TypeIndex> {

		/**
		 * Singleton {@link TypeIndex.Module} instance.
		 */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<TypeIndex> getImplementation() {
			return TypeIndex.class;
		}

		@Override
		public Collection<? extends Class<? extends BasicRuntimeModule<?>>> getDependencies() {
			return Collections.emptyList();
		}

		@Override
		protected TypeIndex newImplementationInstance() throws ModuleException {
			return new TypeIndex();
		}

	}

}
