/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import java.text.Format;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;

import com.top_logic.basic.DateUtil;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormMemberVisitor;
import com.top_logic.layout.form.I18NConstants;
import com.top_logic.layout.form.constraints.DateRangeConstraint;
import com.top_logic.layout.form.constraints.RangeConstraint;
import com.top_logic.util.Resources;

/**
 * An input field that accepts a single string as input and transformes this
 * input into an application object.
 *
 * <p>
 * The transformation algorithm from {@link String}-valued input to application
 * objects and vice versa is customizable using a {@link Format} object. This
 * format encapsulates the parsing and unparsing algorithm of this field.
 * </p>
 *
 * <p>
 * To be able to report user-readable internationalized error messages from a
 * custom {@link Format} implementation, this field passes a
 * {@link DescriptiveParsePosition} object to the format's
 * {@link Format#parseObject(String, java.text.ParsePosition)} method (instead
 * of a regular {@link ParsePosition}). If no
 * {@link DescriptiveParsePosition#setErrorDescription(String, String) error description}
 * is provided by a format, a generic error message is constructed from the
 * index at which the error occurred.
 * </p>
 *
 * <p>
 * This class is not intended to be sub-classed, but must be
 * {@link #setFormat(Format) configured with an appropriate format} instead.
 * </p>
 *
 * @see Format
 * @see DescriptiveParsePosition
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class ComplexField extends AbstractSingleValueField {

	/** Constant to use in {@link #setWhiteSpaceIgnored(boolean)} */
	public static boolean IGNORE_WHITE_SPACE = true;

	/**
	 * Configuration section name to configure global field settings.
	 */
	private static final String FIELD_MANAGEMENT_SECTION = "FieldManagement";

	/**
	 * Configuration property to set a minimum year for {@link #GLOBAL_DATE_CONSTRAINT}.
	 */
	private static final String MAX_YEAR_PROPERTY = "ComplexField.date.year.max";

	/**
	 * Configuration property to set a maximum year for {@link #GLOBAL_DATE_CONSTRAINT}.
	 */
	private static final String MIN_YEAR_PROPERTY = "ComplexField.date.year.min";

	/**
	 * The global range constraint.
	 * 
	 * <p>
	 * All complex field that are constructed by one of the
	 * {@link FormFactory#newDateField(String, Object, boolean)},
	 * {@link FormFactory#newDateField(String, Object, boolean, boolean, boolean, Constraint)}
	 * methods get this global date constraint.
	 * </p>
	 * 
	 * <p>
	 * The global date constraint is configured through the properties
	 * {@link #MIN_YEAR_PROPERTY} and {@link #MAX_YEAR_PROPERTY} in the section
	 * {@link #FIELD_MANAGEMENT_SECTION}.
	 * </p>
	 */
	static Constraint GLOBAL_DATE_CONSTRAINT;

	/**
	 * @see #isWhiteSpaceIgnored()
	 */
	boolean ignoreWhiteSpace;

	/**
	 * @see #getFormat()
	 */
	Format format;

	/**
	 * @see #getParser()
	 */
	Format parser;

	/**
	 * @see #getFormat()
	 * @see #isWhiteSpaceIgnored()
	 * @see AbstractFormField#AbstractFormField(String, boolean, boolean, boolean, Constraint)
	 */
	protected ComplexField(String name, Format format, boolean ignoreWhiteSpace,
			boolean mandatory, boolean immutable,
			Constraint constraint)
	{
		super(name, mandatory, immutable, constraint);
		init(format, ignoreWhiteSpace);
	}

	/**
	 * Constructor fragment initializing the {@link #format} and
	 * {@link #ignoreWhiteSpace} properties to the given values.
	 */
	private void init(Format aFormat, boolean ignoreWhite) {
		assert aFormat != null;
		this.format = aFormat;
		this.parser = aFormat;
		this.ignoreWhiteSpace = ignoreWhite;
	}

	/**
	 * The underlying format of this field.
	 *
	 * <p>
	 * This format is used to render this field's values.
	 * </p>
	 *
	 * @see #getParser() for the format that is responsible to parse values
	 *      entered by the user.
	 */
	public Format getFormat() {
		return format;
	}

	/**
	 * Sets the format of this field by preserving its {@link #getValue() value}.
	 * 
	 * <p>
	 * This field's parser (see {@link #setParser(Format)}) may also have to be
	 * updated, because it is initialized to the initial format of this field
	 * and is not updated by this method.
	 * </p>
	 * 
	 * <p>
	 * Note: This method must only be used, if the new format can still display
	 * the old {@link #getValue()} of this field.
	 * </p>
	 * 
	 * @see #getFormat()
	 * @see #setParser(Format) for updating this field's parser.
	 * 
	 * @return this field. This can be used to chain multiple set into a
	 *         single expression.
	 */
	public ComplexField setFormat(Format format) {
		assert format != null;
		this.format = format;

		// Update the cached raw value that is displayed at the UI.
		if (hasValue()) {
			updateRawValue();
		}

		return this;
	}

	/**
	 * The {@link Format} that is used to parse input values entered by the
	 * user.
	 *
	 * <p>
	 * By having a different format for parsing and displaying information, one
	 * can allow to enter values in an abbreviated form, but display them fully
	 * expanded.
	 * </p>
	 *
	 * <p>
	 * By default, a field's parser is initialized to the field's
	 * {@link #getFormat() format}.
	 * </p>
	 */
	public Format getParser() {
		return parser;
	}

	/**
	 * Sets the format for parsing.
	 * 
	 * <p>
	 * Note: The current user input of the field is parsed again and the fields
	 * {@link #getValue()} might be updated accordingly.
	 * </p>
	 * 
	 * @see #getParser()
	 */
	public void setParser(Format parser) {
		assert parser != null;
		this.parser = parser;
		reparse();
	}

	/**
	 * If white space is ignored, parsing of enterd values succeeds, even if the
	 * entered value is surrounded with white space and the underlying
	 * {@link #getFormat() format} does not accept white space as part of the
	 * parsed value.
	 *
	 * If not explicitly specified, the default value of this property is
	 * <code>true</code>.
	 */
	public boolean isWhiteSpaceIgnored() {
		return ignoreWhiteSpace;
	}

	/**
	 * this field. This can be used to chain multipe settes into a
	 *     single expression.
	 *
	 * @see #isWhiteSpaceIgnored()
	 */
	public ComplexField setWhiteSpaceIgnored(boolean ignoreWhiteSpace) {
		this.ignoreWhiteSpace = ignoreWhiteSpace;
		return this;
	}

	/**
	 * Atomically sets the format and the parser of this field by preserving the
	 * current user input.
	 * 
	 * <p>
	 * Note: If the current user input cannot be parsed with the given format,
	 * the field is marked as having an error.
	 * </p>
	 * 
	 * @param format
	 *        the format to set as format and parser.
	 * @return this field. This can be used to chain multiple set into a
	 *         single expression.
	 */
    public ComplexField setFormatAndParser(Format format) {
    	// Note: Parser and format must be changed atomically, because a change
		// to an incompatible format would otherwise fail, since the format is
		// used as fall-back for parsing, if the parser fails.
        assert format != null;
        this.format = format;
        this.parser = format;

		// Re-parse first to preserve as much of the current user input as possible.
		reparse();

		// Update the cached raw value that is displayed at the UI.
		if (hasValue()) {
			updateRawValue();
		}

        return this;
    }

    @Override
	protected Object parseString(String aRawValue) throws CheckException {
		int startPos = 0;
		int stopPos = aRawValue.length();
		if (ignoreWhiteSpace) {
			// Do not use String.trim(), because this would loose the
			// information about the error position relative to the user-entered
			// value (if this value starts with white space characters).
			while ((startPos < stopPos) && Character.isWhitespace(aRawValue.charAt(startPos))) {
				startPos++;
			}
			while ((stopPos > startPos) && Character.isWhitespace(aRawValue.charAt(stopPos - 1))) {
				stopPos--;
			}
		}

		if (startPos == stopPos) {
			// Empty input (possibly with whitespace)
			return EMPTY_INPUT;
		}

		DescriptiveParsePosition pos = new DescriptiveParsePosition(startPos);

		Object result = parser.parseObject(aRawValue, pos);

		if ((pos.getErrorIndex() < 0) && (pos.getIndex() < stopPos)) {
			// Check, whether all input has been consumed. If not, there is some
			// (invalid) input at the end of the parsed string.
			pos.setErrorIndex(pos.getIndex());
		}

		if (pos.getErrorIndex() >= 0) {
			// If parsing does not succeed and did not provide a result,
			// fall-back to use the display format for parsing. This is
			// required, if the parse format is not compatible with the display
			// format. This is e.g. the case for the short and long English date
			// format.
			pos.reset();
			result = format.parseObject(aRawValue, pos);

			if ((pos.getErrorIndex() < 0) && (pos.getIndex() < stopPos)) {
				// Check, whether all input has been consumed. If not, there is some
				// (invalid) input at the end of the parsed string.
				pos.setErrorIndex(pos.getIndex());
			}
		}

		if (pos.getErrorIndex() >= 0) {
			if (pos.hasErrorMessage()) {
				// If the format did provide some detail about the error, use
				// this description, because it is most preceise.
				throw new CheckException(pos.getErrorMessage());
			}

			Object example = getExampleValue();

			// Create a generic error message that gives as much detail about
			// the problem as possible (without knowing any details about the
			// format).
			if (pos.getErrorIndex() == aRawValue.length()) {
				if (example != null) {
					throw new CheckException(
						Resources.getInstance()
							.getString(I18NConstants.MORE_INPUT_EXPECTED__VALUE_EXAMPLE.fill(aRawValue,
								format.format(example))));
				} else {
					throw new CheckException(
						Resources.getInstance().getString(I18NConstants.MORE_INPUT_EXPECTED__VALUE.fill(aRawValue)));
				}
			} else {
				if (example != null) {
					throw new CheckException(
						Resources.getInstance().getString(I18NConstants.FORMAT_INVALID__VALUE_POSITION_EXAMPLE.fill(
							aRawValue, Integer.valueOf(pos.getErrorIndex() + 1), format.format(example))));
				} else {
					throw new CheckException(
						Resources.getInstance().getString(I18NConstants.FORMAT_INVALID__VALUE_POSITION.fill(aRawValue,
							Integer.valueOf(pos.getErrorIndex() + 1))));
				}
			}
		}

		return result;
	}

	@Override
	protected String unparseString(Object aValue) {
		if (aValue == EMPTY_INPUT) return "";

		return format.format(aValue);
	}

	@Override
	protected Object narrowValue(Object aValue) {
		if (aValue == EMPTY_INPUT) return aValue;

		// Prevent illegal values from being stored programmatically into this
		// field.
		checkFormat(aValue);

		return aValue;
	}

	/**
	 * Checks that the {@link #getFormat()} of the field is able to display the new value of the
	 * field.
	 * 
	 * <p>
	 * The only way to check, whether a given value is compatible with the format of this field, is
	 * to actually format the value.
	 * </p>
	 * 
	 * <p>
	 * Formatting ensures that the given value is of an appropriate type, which is compatible with
	 * the format of this field. The result is not used. Only the IllegalArgumentException that is
	 * thrown by the format, if the type of the given value does not match the expected type of the
	 * format, prevents illegal value from being stored into this field.
	 * </p>
	 * 
	 * @see #narrowValue(Object)
	 */
	private void checkFormat(Object value) throws IllegalArgumentException {
		format.format(value);
	}


    @Override
	public Object visit(FormMemberVisitor v, Object arg) {
		return v.visitComplexField(this, arg);
	}

	// Static block to init the global date constraint for complex fields that are
	// constructed by one of the newDateInstance methods.
    static {
		int minYear = ApplicationConfig.getInstance().getConfig(FieldManagement.class).getMinYear();
		int maxYear = ApplicationConfig.getInstance().getConfig(FieldManagement.class).getMaxYear();

		boolean rangeActive = (minYear != 0) || (maxYear != 0);

        if (rangeActive) {
        	GLOBAL_DATE_CONSTRAINT = createDateRangeConstraint(minYear, maxYear);
        } else {
        	GLOBAL_DATE_CONSTRAINT = null;
        }
    }

	public static RangeConstraint createDateRangeConstraint(int minYear, int maxYear) {
		Date lowerDate = minYear != 0 ? DateUtil.adjustToDayBegin(DateUtil.createDate(minYear, Calendar.JANUARY, 1)) : null;
		Date upperDate = maxYear != 0 ? DateUtil.adjustToDayEnd(DateUtil.createDate(maxYear, Calendar.DECEMBER, 31)) : null;
		DateRangeConstraint rangeConstraint = new DateRangeConstraint(lowerDate, upperDate);
		return rangeConstraint;
	}

	@Override
	protected boolean canRecordRawValue() {
		return true;
	}
}
