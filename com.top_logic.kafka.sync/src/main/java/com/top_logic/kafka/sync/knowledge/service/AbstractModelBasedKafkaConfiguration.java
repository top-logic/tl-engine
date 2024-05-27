/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.sync.knowledge.service;

import static com.top_logic.basic.config.misc.TypedConfigUtil.*;
import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;
import static com.top_logic.model.util.TLModelUtil.*;
import static java.util.Collections.*;
import static java.util.Optional.*;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Log;
import com.top_logic.basic.col.TupleFactory.Pair;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.TypeSystem;
import com.top_logic.kafka.sync.knowledge.service.exporter.KafkaExportConfiguration;
import com.top_logic.kafka.sync.knowledge.service.importer.KafkaImportConfiguration;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.HasModelPartChanged;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.knowledge.service.UpdateListener;
import com.top_logic.knowledge.service.db2.WeakUpdateListener;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassPart;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.util.TLAttributeAnnotationPropagation;
import com.top_logic.model.annotate.util.TLClassAnnotationPropagation;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.ModelService;

/**
 * Superclass for {@link KafkaImportConfiguration} and {@link KafkaExportConfiguration} based on the
 * {@link TLModel} of the {@link KnowledgeBase}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractModelBasedKafkaConfiguration implements KafkaSyncConfig, UpdateListener {

	/**
	 * Typed configuration interface definition for {@link AbstractModelBasedKafkaConfiguration}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends PolymorphicConfiguration<AbstractModelBasedKafkaConfiguration> {
		// configuration interface definition
	}

	private final Map<ObjectKey, Map<String, Function<Object, ?>>> _valueMappings = map();

	private TLClassAnnotationPropagation _classAnnotationCache;

	private final Map<TLClassPart, TLAttributeAnnotationPropagation> _attributeAnnotationCache = map();

	private final KnowledgeBase _kb;

	/**
	 * Create a {@link AbstractModelBasedKafkaConfiguration}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public AbstractModelBasedKafkaConfiguration(InstantiationContext context, Config config) {
		_kb = PersistencyLayer.getKnowledgeBase();
	}

	/**
	 * Attaches this class as listener. Must not be called twice. Should be called at end of
	 * constructor.
	 */
	protected final void attachAsListener() {
		kb().addUpdateListener(new WeakUpdateListener(this));
	}

	/**
	 * The {@link KnowledgeBase} to use.
	 */
	public final KnowledgeBase kb() {
		return _kb;
	}

	@Override
	public synchronized void notifyUpdate(KnowledgeBase sender, UpdateEvent event) {
		if (sender != kb()) {
			throw new IllegalArgumentException("Attached to foreign " + KnowledgeBase.class.getName());
		}

		if (modelPartChanged(event.getChanges())) {
			// Model has changed, rebuild completely
			handleModelPartChanged();
		}

	}

	/**
	 * All global classes, topologically sorted.
	 */
	protected List<TLClass> getGlobalClassesSorted() {
		Collection<TLClass> globalTypes = TLModelUtil.getAllGlobalClasses(ModelService.getApplicationModel());
		return CollectionUtil.topsort(TLModelUtil.GET_GENERALIZATIONS, globalTypes, false);
	}

	/**
	 * Notify any part of the model has changed.
	 * 
	 */
	protected void handleModelPartChanged() {
		getValueMappings().clear();
		initClassAnnotationCache();
	}

	private boolean modelPartChanged(ChangeSet changes) {
		return HasModelPartChanged.INSTANCE.hasModelPartChanged(changes);
	}

	/**
	 * Has to be called after the subclass has finished the analysis of the type system.
	 * <p>
	 * Clears caches that are used for the type system analysis.
	 * </p>
	 */
	protected void afterTypeSystemAnalysis() {
		clearClassAnnotationCache();
		getAttributeAnnotationCache().clear();
	}

	/**
	 * The subtype of the {@link TLSynced} annotation used by the concrete subclass.
	 * 
	 * @return Either {@link TLExported} or {@link TLImported}.
	 */
	protected abstract Class<? extends TLSynced> getAnnotationType();

	/**
	 * {@link TypeSystem} of the {@link #kb()}.
	 */
	protected TypeSystem typeSystem() {
		return (TypeSystem) kb().getMORepository();
	}

	/**
	 * All concrete subtypes of {@link ApplicationObjectUtil#WRAPPER_ATTRIBUTE_ASSOCIATION_BASE}.
	 */
	protected Set<MetaObject> findStaticAssociationTypes(Log log) {
		MetaObject baseType;
		try {
			baseType = typeSystem().getType(ApplicationObjectUtil.WRAPPER_ATTRIBUTE_ASSOCIATION_BASE);
		} catch (UnknownTypeException ex) {
			log.error("No table found containing wrapper attribute values.", ex);
			return Collections.emptySet();
		}
		return new HashSet<>(typeSystem().getConcreteSubtypes(baseType));
	}

	@Override
	public synchronized Function<Object, ?> getValueMapping(ObjectKey ownerTypeId, String tlAttribute) {
		return getValueMappings().getOrDefault(ownerTypeId, emptyMap()).get(tlAttribute);
	}

	/**
	 * Retrieve the "effective" {@link TLSynced} annotation for this {@link TLClass}.
	 * <p>
	 * "Effective" means: Either locally annotated or inherited.
	 * </p>
	 * 
	 * @param type
	 *        Is not allowed to be null.
	 * @return Null, if there is no {@link TLSynced} annotation.
	 */
	@SuppressWarnings("unchecked")
	protected <T extends TLSynced> T getAnnotation(TLClass type) {
		return (T) getClassAnnotationCache().getProperty(type);
	}

	/**
	 * Retrieve the "effective" {@link TLSynced} annotation for this attribute.
	 * <p>
	 * "Effective" means: Either locally annotated or inherited.
	 * </p>
	 * 
	 * @param owner
	 *        Is not allowed to be null.
	 * @param name
	 *        Is not allowed to be null.
	 * @return Null, if there is no (indirectly inherited) annotation.
	 */
	@SuppressWarnings("unchecked")
	protected <T extends TLSynced> T getAnnotation(TLClass owner, String name) {
		TLClassPart tlClassPart = (TLClassPart) owner.getPart(name);
		TLClassPart definition = (TLClassPart) tlClassPart.getDefinition();
		if (owner.equals(definition.getOwner())) {
			if (!getOverridingParts(definition).isEmpty()) {
				/* Create the annotation cache only if it is worth the effort, i.e. if there are
				 * overriding parts. */
				cacheAttributeAnnotation(definition);
			}
			return (T) tlClassPart.getAnnotation(getAnnotationType());
		}
		TLAttributeAnnotationPropagation cacheEntry = getAttributeAnnotationCache().get(definition);
		if (cacheEntry == null) {
			/* This happens if the definition of the attribute is not annotated, but an override
			 * is. */
			cacheAttributeAnnotation(definition);
			cacheEntry = getAttributeAnnotationCache().get(definition);
		}
		return getAttributeAnnotationCached(cacheEntry, owner, name);
	}

	/**
	 * Has to be called for every annotated {@link TLStructuredTypePart}.
	 * <p>
	 * Can be called for non-annotated {@link TLStructuredTypePart}s, too. They are filtered out.
	 * </p>
	 * <p>
	 * Don't use a {@link TLClassPart} object. They exist only for definitions and overrides, but
	 * not for inherited attributes. The {@link TLClassPart} of the definition would be used
	 * instead. But the owner of the inherited attribute would be the definition's owner, not the
	 * inheriting owner.
	 * </p>
	 * 
	 * @param ownerType
	 *        Is not allowed to be null.
	 * @param attributeName
	 *        Is not allowed to be null.
	 * @param annotation
	 *        The {@link TLSynced} annotation. If it is null, nothing happens.
	 */
	protected void handleTypePartAnnotation(TLClass ownerType, String attributeName, TLSynced annotation) {
		getValueMapping(annotation).ifPresent(mapping -> addValueMapping(ownerType, attributeName, mapping));
	}

	private Optional<Function<Object, ?>> getValueMapping(TLSynced annotation) {
		if (annotation == null) {
			return empty();
		}
		if (annotation.getValueMapping() == null) {
			return empty();
		}
		return Optional.of(createInstance(annotation.getValueMapping()));
	}

	private void addValueMapping(TLClass ownerType, String attributeName, Function<Object, ?> mapping) {
		if (isValueMappingMissingOnDefinition(ownerType, attributeName, mapping)) {
			throw new RuntimeException(getErrorMessageValueMappingMissingOnDefinition(ownerType, attributeName));
		}
		getValueMappings()
			.computeIfAbsent(ownerType.tId(), key -> new HashMap<>())
			.put(attributeName, mapping);
	}

	private boolean isValueMappingMissingOnDefinition(TLClass ownerType, String attributeName,
			Function<Object, ?> mapping) {
		TLStructuredTypePart attribute = ownerType.getPart(attributeName);
		TLStructuredTypePart definition = attribute.getDefinition();
		if (attribute.equals(definition)) {
			return false;
		}
		Map<String, Function<Object, ?>> mappingsPerAttribute = getValueMappings().get(definition.getOwner().tId());
		if (mappingsPerAttribute == null) {
			return true;
		}
		Function<Object, ?> definitionValueMapping = mappingsPerAttribute.get(attributeName);
		return !Objects.equals(definitionValueMapping, mapping);
	}

	private String getErrorMessageValueMappingMissingOnDefinition(TLClass ownerType, String attributeName) {
		TLStructuredTypePart attribute = ownerType.getPart(attributeName);
		return "Value mappings on overrides are not supported and have no effect."
			+ " Move the value mapping to the definition of the attribute. Attribute: "
			+ qualifiedName(attribute) + ". Definition: " + qualifiedName(attribute.getDefinition());
	}

	private Map<ObjectKey, Map<String, Function<Object, ?>>> getValueMappings() {
		return _valueMappings;
	}

	private TLClassAnnotationPropagation getClassAnnotationCache() {
		return _classAnnotationCache;
	}

	private void initClassAnnotationCache() {
		_classAnnotationCache = new TLClassAnnotationPropagation(getAnnotationType());
	}

	private void clearClassAnnotationCache() {
		_classAnnotationCache = null;
	}

	private <T extends TLSynced> TLAttributeAnnotationPropagation cacheAttributeAnnotation(TLClassPart definition) {
		TLAttributeAnnotationPropagation attributeAnnotations =
			new TLAttributeAnnotationPropagation(getAnnotationType(), definition);
		return getAttributeAnnotationCache().put(definition, attributeAnnotations);
	}

	@SuppressWarnings("unchecked")
	private <T extends TLSynced> T getAttributeAnnotationCached(TLAttributeAnnotationPropagation cacheEntry,
			TLClass owner, String name) {
		return (T) cacheEntry.getProperty(new Pair<>(owner, name));
	}

	private Map<TLClassPart, TLAttributeAnnotationPropagation> getAttributeAnnotationCache() {
		return _attributeAnnotationCache;
	}

}
