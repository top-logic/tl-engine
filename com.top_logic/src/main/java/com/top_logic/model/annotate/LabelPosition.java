/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.format.I18NConstants;
import com.top_logic.model.form.ReactiveFormCSS;

/**
 * Possible positions of a label of a value in a form.
 * 
 * @see LabelPositionAnnotation
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Format(LabelPosition.PositionFormat.class)
public enum LabelPosition implements ExternallyNamed {

	/**
	 * The label is rendered in a column before the input if the form is wide enough and wrapped
	 * into a separate line, if there is not enough space.
	 */
	@Label("Automatic")
	// Note: The classifier name is for backwards compatibility, see PositionFormat.
	DEFAULT("default"),

	/**
	 * Label is rendered in a separate line above the value.
	 * 
	 * <p>
	 * This is useful for multi-line values, because the otherwise emty space in the label column
	 * can be used for the value, too.
	 * </p>
	 */
	@Label("Above value")
	// Note: The classifier name is for backwards compatibility, see PositionFormat.
	ABOVE("above-value"),

	/**
	 * Places the label in a separate line above the input element in edit mode but use default
	 * placement before the value in view mode.
	 */
	@Label("Above input")
	ABOVE_INPUT("above-input"),

	/**
	 * The label is rendered strictly before the input, even if causes scrolling due to space
	 * constraints.
	 */
	@Label("Before value")
	// Note: The classifier name is for backwards compatibility, see PositionFormat.
	INLINE("before-value"),

	/**
	 * There is a single cell for label and value, and the label is rendered after the corresponding
	 * value.
	 * 
	 * <p>
	 * This is useful for very short input elements such as checkboxes.
	 * </p>
	 */
	@Label("After value")
	AFTER_VALUE("after-value"),

	/** The label is not displayed at all. */
	@Label("Hidden")
	HIDE_LABEL("hide-label"),

	;
	
	private String _externalName;

	/**
	 * Creates a new {@link LabelPosition}.
	 * 
	 */
	private LabelPosition(String externalName) {
		_externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}

	/**
	 * Method to break control flow when receiving an unexpected {@link LabelPosition}.
	 */
	public static RuntimeException noSuchPosition(LabelPosition labelPosition) {
		throw new IllegalArgumentException("Unhandled label position: " + labelPosition);
	}

	/**
	 * CSS class representing this {@link LabelPosition}.
	 * 
	 * @return CSS class, or <code>null</code>, if this {@link LabelPosition} is not expressed by a
	 *         CSS class.
	 */
	public String cssClass(boolean inEditMode) {
		switch (this) {
			case ABOVE:
				return ReactiveFormCSS.RF_LABEL_ABOVE;
			case ABOVE_INPUT:
				return inEditMode ? ReactiveFormCSS.RF_LABEL_ABOVE : null;
			case INLINE:
				return ReactiveFormCSS.RF_LABEL_INLINE;
			case AFTER_VALUE:
				return ReactiveFormCSS.RF_LABEL_AFTER;
			default:
				return null;
		}
	}

	/**
	 * Whether the label is rendered above its value.
	 */
	public boolean isAbove() {
		return switch (this) {
			case DEFAULT -> com.top_logic.layout.form.boxes.reactive_tag.Icons.LABEL_ABOVE.get();
			case ABOVE -> true;
			default -> false;
		};
	}

	/**
	 * Whether the label should be strictly kept on the same line as its value.
	 */
	public boolean isInline() {
		return switch (this) {
			case INLINE -> true;
			default -> false;
		};
	}

	/**
	 * Format for {@link LabelPosition} values that provides backwards compatibility with old-style
	 * configuration values.
	 */
	public static class PositionFormat extends AbstractConfigurationValueProvider<LabelPosition> {
		/**
		 * Creates a {@link PositionFormat}.
		 */
		public PositionFormat() {
			super(LabelPosition.class);
		}

		@Override
		protected LabelPosition getValueNonEmpty(String propertyName, CharSequence propertyValue)
				throws ConfigurationException {
			String name = propertyValue.toString();
			
			for (LabelPosition choice : LabelPosition.values()) {
				// Note: Using the classifier name in configuration is for backwards compatibility.
				if (name.equals(choice.name()) || name.equals(choice.getExternalName())) {
					return choice;
				}
			}
			
			throw new ConfigurationException(
				I18NConstants.ERROR_CANNOT_RESOLVE_ENUM_CONSTANT__VALUE_DETAIL.fill(name,
					Stream.of(LabelPosition.values()).map(c -> c.getExternalName()).collect(Collectors.joining(", "))),
				propertyName, propertyValue);
		}

		@Override
		protected String getSpecificationNonNull(LabelPosition configValue) {
			// Always serialize in new-style format.
			return configValue.getExternalName();
		}

	}

	/**
	 * Converts <code>null</code> to {@link LabelPosition#DEFAULT}.
	 */
	public static LabelPosition nonNull(LabelPosition labelPositionOrNull) {
		return labelPositionOrNull == null ? LabelPosition.DEFAULT : labelPositionOrNull;
	}
}
