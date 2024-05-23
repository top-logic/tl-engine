/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.util;

import java.util.List;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassifier;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.annotate.AnnotationLookup;
import com.top_logic.model.annotate.ExportColumns;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.annotate.persistency.CompositionStorage;
import com.top_logic.model.annotate.ui.ClassificationDisplay;
import com.top_logic.model.annotate.ui.ClassificationDisplay.ClassificationPresentation;
import com.top_logic.model.config.TLTypeAnnotation;
import com.top_logic.model.config.TypeConfig;
import com.top_logic.model.config.annotation.MultiSelect;
import com.top_logic.model.config.annotation.TableName;
import com.top_logic.model.form.EditContextBase;
import com.top_logic.model.util.TLModelUtil;

/**
 * Utility class to read and handle {@link TLAnnotation}s.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TLAnnotations {

	/**
	 * Name of the default database table, when nothing is configured for a given {@link TLType}.
	 * 
	 * @see #getTable(TLType)
	 */
	public static final String GENERIC_TABLE_NAME = "GenericObject";

	/**
	 * Lookup an annotation of the given annotation type in the given {@link TypeConfig}.
	 * 
	 * @param config
	 *        The annotated {@link TypeConfig}.
	 * @param annotationType
	 *        The annotation type to look up.
	 * @return The annotation of the given type, or <code>null</code>, if no such annotation exists.
	 */
	public static <T extends TLAnnotation> T getAnnotation(AnnotationLookup config, Class<T> annotationType) {
		return config.getAnnotation(annotationType);
	}

	/**
	 * Lookup an annotation of the given annotation type in the given {@link TypeConfig}.
	 * 
	 * @param editContext
	 *        The edit location.
	 * @param annotationType
	 *        The annotation type to look up.
	 * @return The annotation of the given type, or <code>null</code>, if no such annotation exists.
	 */
	public static <T extends TLTypeAnnotation> T getAnnotation(EditContextBase editContext, Class<T> annotationType) {
		return editContext.getAnnotation(annotationType);
	}

	/**
	 * Lookup an annotation of the given annotation type in the given modelpart.
	 * 
	 * @param modelPart
	 *        The annotated {@link TLModelPart}.
	 * @param annotationType
	 *        The annotation type to look up.
	 * @return The annotation of the given type, or <code>null</code>, if no such annotation exists.
	 */
	public static <T extends TLAnnotation> T getAnnotation(TLModelPart modelPart, Class<T> annotationType) {
		return modelPart.getAnnotation(annotationType);
	}

	/**
	 * Determines how the {@link TLClassifier} of the {@link TLEnumeration value type} at the given
	 * edit location shall be presented.
	 */
	public static ClassificationPresentation getClassificationPresentation(TLStructuredTypePart part) {
		return getClassificationPresentation(getAnnotation(part, ClassificationDisplay.class), part.isMultiple());
	}

	/**
	 * Determines how the {@link TLClassifier} of the {@link TLEnumeration target type} of the given
	 * part shall be presented.
	 */
	public static ClassificationPresentation getClassificationPresentation(EditContextBase part) {
		return getClassificationPresentation(getAnnotation(part, ClassificationDisplay.class), part.isMultiple());
	}

	/**
	 * Determines how the {@link TLClassifier} of an {@link TLEnumeration} shall be presented.
	 * 
	 * @param annotation
	 *        The annotation determining the display. If <code>null</code>, a default is applied.
	 * @param multiple
	 *        Whether multiple values are allowed in the current context.
	 */
	public static ClassificationPresentation getClassificationPresentation(ClassificationDisplay annotation,
			boolean multiple) {
		if (annotation == null) {
			if (multiple) {
				return ClassificationPresentation.POP_UP;
			} else {
				return ClassificationPresentation.DROP_DOWN;
			}
		}
		return annotation.getValue();
	}

	/**
	 * Setter for {@link #getTable(TLType)}.
	 */
	public static void setTable(TLType type, String tableName) {
		if (StringServices.isEmpty(tableName)) {
			type.removeAnnotation(TableName.class);
		} else {
			TableName existingAnnotation = type.getAnnotation(TableName.class);
			if (existingAnnotation != null) {
				if (existingAnnotation.getName().equals(tableName)) {
					return;
				}
			}
			type.setAnnotation(newTableAnnotation(tableName));
		}
	}

	/**
	 * Creates a new {@link TableName} for the given table name.
	 * 
	 * @param tableName
	 *        The table to create a {@link TableName} for.
	 */
	public static TableName newTableAnnotation(String tableName) {
		TableName tableAnnotation = TypedConfiguration.newConfigItem(TableName.class);
		PropertyDescriptor tableNameProp = tableAnnotation.descriptor().getProperty(TableName.NAME);
		tableAnnotation.update(tableNameProp, tableName);
		return tableAnnotation;
	}

	/**
	 * The name of the table used to store instances of the given type.
	 * 
	 * @param type
	 *        The type to get table for.
	 * 
	 * @return Table where instances of the given type are stored. Not <code>null</code>.
	 */
	public static String getTable(TLType type) {
		TableName tableAnnotation = getTableName(type);
		if (tableAnnotation != null) {
			return tableAnnotation.getName();
		}
		return GENERIC_TABLE_NAME;
	}

	/**
	 * {@link TableName} annotation of the given type, if there is one. Otherwise the
	 * {@link TableName} annotation of the primary generalisation (recursively).
	 * 
	 * @return May be <code>null</code> if no primary generalisation (recursively) has a
	 *         {@link TableName} annotation.
	 */
	public static TableName getTableName(TLType type) {
		TableName tableAnnotation = type.getAnnotation(TableName.class);
		if (tableAnnotation != null) {
			return tableAnnotation;
		}
		TLClass primaryGeneralization = TLModelUtil.getPrimaryGeneralization(type);
		if (primaryGeneralization == null) {
			return null;
		}
		return getTableName(primaryGeneralization);
	}

	/**
	 * Whether the given enumeration is a "multiple" enumeration by default.
	 */
	public static boolean isMultiSelectDefault(TLEnumeration enumeration) {
		MultiSelect annotation = enumeration.getAnnotation(MultiSelect.class);
		if (annotation == null) {
			return false;
		}
		return annotation.getValue();
	}

	/**
	 * The annotated column names to export.
	 * 
	 * @param part
	 *        {@link TLStructuredTypePart} which has values that are displayed as table.
	 * @return The name of the table columns that must be exported. May be <code>null</code> when
	 *         nothing is configured.
	 */
	public static List<String> getExportColumns(TLStructuredTypePart part) {
		ExportColumns configuredColumns = getAnnotation(part, ExportColumns.class);
		if (configuredColumns != null) {
			return configuredColumns.getValue();
		}
		return getExportColumns(part.tType());
	}

	/**
	 * The annotated column names to export.
	 * 
	 * @param type
	 *        {@link TLStructuredType} whose values are displayed as table.
	 * @return The name of the table columns that must be exported. May be <code>null</code> when
	 *         nothing is configured.
	 */
	public static List<String> getExportColumns(TLStructuredType type) {
		ExportColumns configuredColumns = getAnnotation(type, ExportColumns.class);
		if (configuredColumns != null) {
			return configuredColumns.getValue();
		}
		TLClass primaryGeneralization = TLModelUtil.getPrimaryGeneralization(type);
		if (primaryGeneralization == null) {
			return null;
		}
		return getExportColumns(primaryGeneralization);
	}

	/**
	 * The annotated {@link CompositionStorage} name for a composition reference.
	 * 
	 * @param reference
	 *        The reference to get {@link CompositionStorage} annotation for.
	 */
	public static CompositionStorage getCompositionStorage(TLReference reference) {
		CompositionStorage annotation = getAnnotation(reference, CompositionStorage.class);
		if (annotation != null) {
			return annotation;
		}
		TLTypePart definition = reference.getDefinition();
		if (definition == reference) {
			return null;
		}
		// search parent
		for (TLClass generalization : reference.getOwner().getGeneralizations()) {
			TLReference overriddenReference = (TLReference) generalization.getPart(reference.getName());
			if (overriddenReference == null) {
				// reference is not defined in generalization.
				continue;
			}
			CompositionStorage storage = getCompositionStorage(overriddenReference);
			if (storage != null) {
				return storage;
			}
		}
		return null;

	}

}
