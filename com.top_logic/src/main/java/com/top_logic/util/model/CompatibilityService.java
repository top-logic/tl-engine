/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.model;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.util.ResKey;
import com.top_logic.dob.MetaObject;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.model.DerivedTLTypePart;
import com.top_logic.model.StorageDetail;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.TLI18NKey;
import com.top_logic.model.export.EmptyPreloadContribution;
import com.top_logic.model.export.PreloadContribution;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link ManagedClass} used to be able to provide APIs in top logic for methods and classes that
 * are introduced in element.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CompatibilityService extends ManagedClass {
	
	/**
	 * Creates a new {@link CompatibilityService} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link CompatibilityService}.
	 */
	public CompatibilityService(InstantiationContext context, ManagedClass.ServiceConfiguration<?> config) {
		super(context, config);
	}

	public String getTypeName(Wrapper attributed) {
		throw new UnsupportedOperationException();
	}

	public boolean isAttributed(Wrapper target) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Returns an I18N for the given {@link TLModelPart}.
	 * 
	 * @return may be <code>null</code> if no I18N could be found.
	 */
	public ResKey i18nKey(TLModelPart modelPart) {
		ResKey i18nKey;
		TLI18NKey i18nKeyAnnotation = modelPart.getAnnotation(TLI18NKey.class);
		if (i18nKeyAnnotation != null) {
			i18nKey = i18nKeyAnnotation.getValue();
		} else {
			i18nKey = null;
		}
		return i18nKey;
	}

	/**
	 * @param part
	 *        The part to get {@link PreloadContribution} for.
	 * 
	 * @return The {@link PreloadContribution} to preload the values for the given part in base
	 *         objects.
	 */
	public PreloadContribution preloadContribution(TLStructuredTypePart part) {
		return EmptyPreloadContribution.INSTANCE;
	}

	/**
	 * Determines the name of the {@link MetaObject} that holds the data for the given
	 * {@link TLClass}.
	 */
	public String getTableFor(TLStructuredType tlClass) {
		String msg = "Unable to determine table for class " + TLModelUtil.qualifiedName(tlClass);
		throw new UnsupportedOperationException(msg);
	}

	/**
	 * Creates the {@link TLStructuredTypePart#getStorageImplementation()} for the given part.
	 * <p>
	 * This method is a workaround for the fact that {@link DerivedTLTypePart#isDerived()} and other
	 * information (see: {@link StorageDetail}) depend on the storage implementation which lives in
	 * com.top_logic.element.
	 * </p>
	 * 
	 * @param part
	 *        The {@link TLStructuredTypePart} for which the {@link StorageDetail} should be
	 *        created.
	 */
	public StorageDetail createStorage(TLStructuredTypePart part) {
		/* Dummy implementation in "com.top_logic", as the actual implementation refers to
		 * "com.top_logic.element" code and therefore has to be there, too. */
		return null;
	}

	/**
	 * The singleton {@link CompatibilityService}.
	 */
	public static final CompatibilityService getInstance() {
		return CompatibilityService.Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Module definition for the {@link CompatibilityService}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class Module extends TypedRuntimeModule<CompatibilityService> {

		// Constants

		/** Singleton for this module. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<CompatibilityService> getImplementation() {
			return CompatibilityService.class;
		}

	}

}

