/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased;

import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.WeakHashMap;

import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.MOAttribute;
import com.top_logic.element.config.annotation.TLOptions;
import com.top_logic.element.meta.kbbased.filtergen.Generator;

/**
 * {@link AnnotationsBasedCacheValueFactory} creating a {@link Generator} from the {@link TLOptions}
 * annotation.
 * 
 * <p>
 * This factory is used for defining the derived attribute <code>options</code> in the
 * persistency-layer of model attributes.
 * </p>
 * 
 * @see "com.top_logic.element.meta.AttributeOperations.getOptions(TLStructuredTypePart)"
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TLOptionsFactory extends AnnotationsBasedCacheValueFactory {

	/**
	 * Singleton {@link TLOptionsFactory} instance.
	 */
	public static final TLOptionsFactory INSTANCE = new TLOptionsFactory();

	/**
	 * {@link Generator}s of annotations that are not stored at the attribute they apply to.
	 * 
	 * <p>
	 * Keys are compared by identity (configuration items do not override
	 * {@link Object#equals(Object)}), so a changed annotation yields a fresh generator. A generator
	 * may reference its configuration and thus its key, therefore it is held softly to keep the
	 * entry collectable once the annotation is no longer used.
	 * </p>
	 */
	private static final Map<TLOptions, SoftReference<Generator>> GENERATORS = new WeakHashMap<>();

	private TLOptionsFactory() {
		// Singleton constructor.
	}

	@Override
	public Object getCacheValue(MOAttribute attribute, DataObject item, Object[] storage) {
		TLOptions tlAnnotation = getAnnotation(item, storage, TLOptions.class);
		if (tlAnnotation == null) {
			return null;
		}
		return createGenerator(tlAnnotation);
	}

	/**
	 * The {@link Generator} of the given annotation, shared by all callers asking for the same
	 * annotation instance.
	 * 
	 * <p>
	 * Used for an annotation that is not stored at the attribute it applies to (e.g. an annotation
	 * of the attribute's value type), where the attribute's own cache cannot hold the generator.
	 * </p>
	 * 
	 * @param annotation
	 *        The annotation to get the generator for.
	 * @return The generator configured in the given annotation.
	 */
	public static Generator getGenerator(TLOptions annotation) {
		synchronized (GENERATORS) {
			SoftReference<Generator> reference = GENERATORS.get(annotation);
			Generator generator = reference == null ? null : reference.get();
			if (generator == null) {
				generator = createGenerator(annotation);
				GENERATORS.put(annotation, new SoftReference<>(generator));
			}
			return generator;
		}
	}

	private static Generator createGenerator(TLOptions annotation) {
		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(annotation.getGenerator());
	}

}

