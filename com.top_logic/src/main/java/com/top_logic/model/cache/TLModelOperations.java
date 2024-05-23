/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.cache;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.collections4.map.ListOrderedMap;

import com.google.common.collect.ImmutableSet;

import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.layout.provider.icon.IconProvider;
import com.top_logic.layout.provider.icon.ProxyIconProvider;
import com.top_logic.layout.provider.icon.StaticIconProvider;
import com.top_logic.model.ModelKind;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassPart;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.annotate.InstancePresentation;
import com.top_logic.model.annotate.TLSortOrder;
import com.top_logic.model.annotate.persistency.CompositionStorage;
import com.top_logic.model.annotate.persistency.CompositionStorage.InSourceTable;
import com.top_logic.model.annotate.persistency.CompositionStorage.InTargetTable;
import com.top_logic.model.annotate.persistency.CompositionStorage.LinkTable;
import com.top_logic.model.annotate.persistency.LinkTables;
import com.top_logic.model.annotate.ui.TLDynamicIcon;
import com.top_logic.model.annotate.util.TLAnnotations;
import com.top_logic.model.util.TLModelUtil;

/**
 * Performs operations on a {@link TLModel} like calculating of subclasses of a given
 * {@link TLClass}.
 * <p>
 * This class computes the data every time anew. The subclass {@link TLModelCacheEntry} is used by
 * the {@link TLModelCacheService} to cache the data.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TLModelOperations {

	/** The {@link TLModelOperations} instance. */
	public static final TLModelOperations INSTANCE = new TLModelOperations();

	/**
	 * The recursive super classes.
	 * <p>
	 * Includes the given {@link TLClass} itself.
	 * </p>
	 * <p>
	 * The result is depth-first sorted. Only the first occurrence of a {@link TLClass} in the super
	 * class graph is relevant.
	 * </p>
	 * 
	 * @return The {@link Set} can be unmodifiable, if it was returned from the cache.
	 */
	public Set<TLClass> getSuperClasses(TLClass tlClass) {
		return computeSuperClasses(tlClass);
	}

	/** Computes the data for {@link #getSuperClasses(TLClass)}. */
	protected LinkedHashSet<TLClass> computeSuperClasses(TLClass tlClass) {
		LinkedHashSet<TLClass> result = linkedSet();
		result.add(tlClass);
		for (TLClass superClass : tlClass.getGeneralizations()) {
			result.addAll(getSuperClasses(superClass));
		}
		return result;
	}

	/**
	 * The recursive subclasses.
	 * <p>
	 * Includes the given {@link TLClass} itself.
	 * </p>
	 * <p>
	 * The result is unsorted as subclasses have no order.
	 * </p>
	 * 
	 * @return The {@link Set} can be unmodifiable, if it was returned from the cache.
	 */
	public Set<TLClass> getSubClasses(TLClass tlClass) {
		return computeSubClasses(tlClass);
	}

	/** Computes the data for {@link #getSubClasses(TLClass)}. */
	protected Set<TLClass> computeSubClasses(TLClass tlClass) {
		Set<TLClass> result = set();
		result.add(tlClass);
		return addSpecializationsRecursively(result, tlClass);
	}

	private <T extends Set<TLClass>> T addSpecializationsRecursively(T result, TLClass tlClass) {
		for (TLClass subClass : tlClass.getSpecializations()) {
			result.addAll(getSubClasses(subClass));
		}
		return result;
	}

	/**
	 * The tables in which instances of the given {@link TLClass} can be stored.
	 * <p>
	 * This includes tables in which instances of subclasses are stored.
	 * </p>
	 * <p>
	 * The result is unsorted, as the tables are computed from the subclasses. And as subclasses
	 * have no order their tables too have no order.
	 * </p>
	 * 
	 * @return The {@link Map} can be unmodifiable, if it was returned from the cache.
	 */
	public Map<String, ? extends Set<TLClass>> getPotentialTables(TLClass tlClass) {
		return computePotentialTables(tlClass);
	}

	/** Computes the data for {@link #getPotentialTables(TLClass)}. */
	protected Map<String, Set<TLClass>> computePotentialTables(TLClass tlClass) {
		return TLModelUtil.potentialTablesUncached(tlClass, false);
	}

	/**
	 * The {@link TLStructuredTypePart} of the {@link TLClass} with the given
	 * {@link TLStructuredType#getName() name}.
	 * <p>
	 * Searches in the super classes, too, recursively.
	 * </p>
	 * 
	 * @return The requested part, or <code>null</code> if neither this type nor one of its super
	 *         classes has a part with that name.
	 */
	public TLStructuredTypePart getAttribute(TLClass tlClass, String name) {
		/* Don't use "getAllAttributes(TLClass)" here. This way the method returns early without
		 * having to collect every attribute first. */
		for (TLClass superClass : getSuperClasses(tlClass)) {
			for (TLStructuredTypePart attribute : superClass.getLocalParts()) {
				if (attribute.getName().equals(name)) {
					return attribute;
				}
			}
		}
		return null;
	}

	/**
	 * The {@link TLStructuredTypePart}s of the given {@link TLClass} and its super classes,
	 * recursively.
	 * 
	 * @return The {@link List} is ordered by the {@link TLSortOrder}. The {@link List} can be
	 *         unmodifiable, if it was returned from the cache.
	 */
	public List<? extends TLStructuredTypePart> getAllAttributes(TLClass tlClass) {
		if (tlClass.getModelKind() == ModelKind.CLASS) {
			return TLModelUtil.calcAllPartsUncached(tlClass);
		}
		return tlClass.getLocalClassParts();
	}

	/**
	 * Computes the data for {@link #getAllAttributes(TLClass)} and
	 * {@link #getAttribute(TLClass, String)}.
	 */
	protected ListOrderedMap<String, TLStructuredTypePart> computeAllAttributes(TLClass tlClass) {
		if (tlClass.getModelKind() == ModelKind.CLASS) {
			return toMap(TLModelUtil.calcAllPartsUncached(tlClass));
		}
		return toMap(tlClass.getLocalClassParts());
	}

	private ListOrderedMap<String, TLStructuredTypePart> toMap(Iterable<TLClassPart> metaAttributes) {
		ListOrderedMap<String, TLStructuredTypePart> map = new ListOrderedMap<>();
		for (TLStructuredTypePart attribute : metaAttributes) {
			map.put(attribute.getName(), attribute);
		}
		return map;
	}

	/**
	 * The {@link TLClassPart}s of subclasses.
	 * <p>
	 * Does not include the attributes from the {@link TLClass} itself.
	 * </p>
	 * <p>
	 * The result is unsorted, as subclasses have no order, and therefore their attributes have no
	 * order, too.
	 * </p>
	 * 
	 * @return The {@link Set} can be unmodifiable, if it was returned from the cache.
	 */
	public Set<TLClassPart> getAttributesOfSubClasses(TLClass tlClass) {
		return computeAttributesOfSubClasses(tlClass);
	}

	/** Computes the data for {@link #getAttributesOfSubClasses(TLClass)}. */
	protected Set<TLClassPart> computeAttributesOfSubClasses(TLClass tlClass) {
		Set<TLClassPart> result = set();
		Set<TLClass> subClasses = addSpecializationsRecursively(set(), tlClass);
		for (TLClass subClass : subClasses) {
			result.addAll(subClass.getLocalClassParts());
		}
		return result;
	}

	/**
	 * The global {@link TLClass}es in the given {@link TLModel}.
	 * <p>
	 * "Global" means, it is either defined directly in the scope of a {@link TLModule} or
	 * {@link TLModule#getSingletons() singleton}.
	 * </p>
	 * 
	 * @return The {@link Set} can be unmodifiable, if it was returned from the cache.
	 */
	public Set<TLClass> getGlobalClasses(TLModel tlModel) {
		return computeGlobalClasses(tlModel);
	}

	/** Computes the data for {@link #getGlobalClasses(TLModel)}. */
	protected Set<TLClass> computeGlobalClasses(TLModel tlModel) {
		return TLModelUtil.getAllGlobalClassesUncached(tlModel);
	}

	/**
	 * Retrieves the {@link IconProvider} for a given {@link TLType}.
	 * 
	 * @see TLDynamicIcon#getIconProvider()
	 */
	public IconProvider getIconProvider(TLType type) {
		return computeIconProvider(type);
	}

	/**
	 * Looks up the first {@link TLDynamicIcon} annotation in the primary generalization hierarchy
	 * and builds an {@link IconProvider} for the given type.
	 * 
	 * @see #getIconProvider(TLType)
	 */
	protected IconProvider computeIconProvider(TLType type) {
		while (type != null) {
			TLDynamicIcon dynamicPresentation = type.getAnnotation(TLDynamicIcon.class);
			if (dynamicPresentation != null) {
				IconProvider dynamic = TypedConfigUtil.createInstance(dynamicPresentation.getIconProvider());
				IconProvider fallback = StaticIconProvider.getInstance(getStaticPresentation(type));
				return new ProxyIconProvider(dynamic, fallback);
			}
			InstancePresentation staticPresentation = type.getAnnotation(InstancePresentation.class);
			if (staticPresentation != null) {
				// Defining static icons on a sub-class overrides a potential dynamic annotation on
				// a super class.
				return StaticIconProvider.getInstance(staticPresentation);
			}

			type = TLModelUtil.getPrimaryGeneralization(type);
		}

		return IconProvider.NONE;
	}

	private InstancePresentation getStaticPresentation(TLType type) {
		while (type != null) {
			InstancePresentation annotation = type.getAnnotation(InstancePresentation.class);
			if (annotation != null) {
				return annotation;
			}

			type = TLModelUtil.getPrimaryGeneralization(type);
		}
		return null;
	}

	/**
	 * Determines which storage strategies exists for composition references with the given target
	 * type.
	 */
	public CompositionStorages getCompositionStorages(TLClass type) {
		Set<CompositionStorage.InTargetTable> targets = new HashSet<>();
		Set<InSource> sources = new HashSet<>();
		Set<CompositionStorage.LinkTable> links = new HashSet<>();
		doForAllCompositionReferences(type.getModel(), reference -> {
			TLType targetType = reference.getType();
			if (!TLModelUtil.isCompatibleType(targetType, type)) {
				return;
			}
			CompositionStorage compositionStorage = TLAnnotations.getCompositionStorage(reference);
			CompositionStorage.Storage storage;
			if (compositionStorage != null) {
				storage = compositionStorage.getStorage();
			} else {
				storage = CompositionStorage.defaultCompositionLinkStorage();
			}
			if (storage instanceof CompositionStorage.InTargetTable) {
				targets.add((InTargetTable) storage);
			} else if (storage instanceof CompositionStorage.InSourceTable) {
				InSourceTable inSourceStorage = (InSourceTable) storage;
				sources.add(new InSource(reference, inSourceStorage.getPart()));
			} else {
				links.add((LinkTable) storage);
			}
		});
		return CompositionStoragesImpl.newInstance(links, sources, targets);

	}

	/**
	 * Navigates through the given {@link TLModel} and executes the given callback for all composite
	 * references of non abstract {@link TLClass}.
	 *
	 * @param model
	 *        The {@link TLModel} to navigate.
	 * @param callback
	 *        Handler for the visited {@link TLReference}.
	 */
	protected void doForAllCompositionReferences(TLModel model, Consumer<TLReference> callback) {
		for (TLModule module : model.getModules()) {
			for (TLType type : module.getTypes()) {
				if (type.getModelKind() != ModelKind.CLASS) {
					continue;
				}
				TLClass tlClass = (TLClass) type;
				if (tlClass.isAbstract()) {
					continue;
				}
				for (TLModelPart part : tlClass.getAllParts()) {
					if (part.getModelKind() != ModelKind.REFERENCE) {
						continue;
					}
					TLReference reference = (TLReference) part;
					if (!reference.getEnd().isComposite()) {
						continue;
					}
					callback.accept(reference);
				}
			}
		}
	}

	/**
	 * Determines the first overrides of the given abstract part which are not abstract.
	 *
	 * @param <T>
	 *        implementation type of the part.
	 * @param part
	 *        Abstract {@link TLStructuredTypePart} to get overrides for.
	 * 
	 * @throws IllegalArgumentException
	 *         if the given part is not {@link TLStructuredTypePart#isAbstract()}.
	 */
	public <T extends TLStructuredTypePart> Set<T> getDirectConcreteOverrides(T part) {
		if (!part.isAbstract()) {
			throw new IllegalArgumentException(
				"Direct overrides just exist for abstract parts: " + TLModelUtil.qualifiedName(part));
		}
		TLStructuredType owner = part.getOwner();
		if (owner instanceof TLClass) {
			Set<TLStructuredTypePart> result = new HashSet<>();
			collectDirectConcreteOverrides(result::add, (TLClass) owner, part.getName());
			@SuppressWarnings("unchecked") // All overrides are of the same type.
			Set<T> typeSafe = (Set<T>) result;
			return typeSafe;
		} else {
			return Collections.emptySet();
		}
	}

	/**
	 * Navigates (recursively) throw the specialization hierarchy of the given type and adds the
	 * first non-abstract parts with the given name to the given sink.
	 */
	protected void collectDirectConcreteOverrides(Consumer<? super TLStructuredTypePart> sink, TLClass type,
			String name) {
		for (TLClass specialization : type.getSpecializations()) {
			TLStructuredTypePart part = specialization.getPart(name);
			if (part.getOwner() == specialization) {
				// locally overridden
				if (part.isAbstract()) {
					collectDirectConcreteOverrides(sink, specialization, name);
				} else {
					sink.accept(part);
				}
			} else {
				// not locally defined;
				collectDirectConcreteOverrides(sink, specialization, name);
			}
		}
	}

	/**
	 * Collection of exiting storage strategies for a composition references.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface CompositionStorages {

		/**
		 * All {@link InTargetTable} strategies.
		 */
		Set<CompositionStorage.InTargetTable> storedInTarget();

		/**
		 * All {@link InSourceTable} strategies.
		 */
		Set<InSource> storedInSource();

		/**
		 * All {@link LinkTables} strategies.
		 */
		Set<CompositionStorage.LinkTable> storedInLink();

	}

	/**
	 * Representation of a composition storage algorithm that stores the connection between
	 * container and part in the table of the container.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class InSource {

		private final String _table;

		private final String _partAttr;

		private final String _referenceName;

		/**
		 * Creates a {@link TLModelOperations.InSource}.
		 */
		public InSource(TLReference reference, String partAttr) {
			_table = TLAnnotations.getTable(reference.getOwner());
			_partAttr = partAttr;
			_referenceName = reference.getName();
		}

		@Override
		public int hashCode() {
			return Objects.hash(_partAttr, _referenceName, _table);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			InSource other = (InSource) obj;
			return Objects.equals(_partAttr, other._partAttr)
					&& Objects.equals(_referenceName, other._referenceName)
					&& Objects.equals(_table, other._table);
		}

		/**
		 * The table of the container.
		 */
		public String getTable() {
			return _table;
		}

		/**
		 * {@link MOReference} holding the part.
		 */
		public String getPartAttribute() {
			return _partAttr;
		}

		/**
		 * Name of the composition {@link TLReference}.
		 */
		public String getReferenceName() {
			return _referenceName;
		}
	}

	/**
	 * Simple implementation of {@link CompositionStorage}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	protected static class CompositionStoragesImpl implements CompositionStorages {

		private final Set<LinkTable> _links;

		private final Set<InSource> _sources;

		private final Set<InTargetTable> _targets;

		/**
		 * Creates a {@link CompositionStoragesImpl} with empty {@link #storedInLink()},
		 * {@link #storedInSource()} and {@link #storedInTarget()}.
		 */
		public CompositionStoragesImpl() {
			this(new HashSet<>(), new HashSet<>(), new HashSet<>());
		}

		/**
		 * Creates a {@link CompositionStoragesImpl} with the given values for
		 * {@link #storedInLink()}, {@link #storedInSource()} and {@link #storedInTarget()}.
		 */
		public CompositionStoragesImpl(Set<LinkTable> links, Set<InSource> sources, Set<InTargetTable> targets) {
			_links = links;
			_sources = sources;
			_targets = targets;
		}

		/**
		 * Creates a new {@link CompositionStorages} instance with unmodifiable versions of the
		 * given values.
		 */
		public static CompositionStorages newInstance(Set<LinkTable> links, Set<InSource> sources,
				Set<InTargetTable> targets) {
			return new CompositionStoragesImpl(minimizeAndStabilize(links), minimizeAndStabilize(sources),
				minimizeAndStabilize(targets));
		}

		private static <T> Set<T> minimizeAndStabilize(Set<T> in) {
			switch (in.size()) {
				case 0:
					return Collections.emptySet();
				case 1:
					return Collections.singleton(in.iterator().next());
				default:
					return ImmutableSet.copyOf(in);
			}
		}

		@Override
		public Set<InTargetTable> storedInTarget() {
			return _targets;
		}

		@Override
		public Set<InSource> storedInSource() {
			return _sources;
		}

		@Override
		public Set<LinkTable> storedInLink() {
			return _links;
		}

	}

}
