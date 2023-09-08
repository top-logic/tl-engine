/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.listener.EventType;
import com.top_logic.basic.shared.collection.CollectionUtilShared;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMemberVisitor;
import com.top_logic.mig.html.HTMLConstants;

/**
 * The class {@link DataField} accepts {@link BinaryData} as model
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DataField extends AbstractFormField {
	
	/**
	 * Property is fired when the 'read only' property has changed
	 * 
	 * @see #isReadOnly()
	 * @see ReadOnlyListener
	 */
	public static final EventType<ReadOnlyListener, DataField, Boolean> READ_ONLY_PROPERTY =
		new EventType<>("readOnly") {

			@Override
			public Bubble dispatch(ReadOnlyListener listener, DataField sender, Boolean oldValue, Boolean newValue) {
				return listener.handleReadOnlyChanged(sender, oldValue, newValue);
			}

		};

	/**
	 * Property which is used to set an I18N key for the text to display when the {@link DataField}
	 * has currently no value.
	 * 
	 * @see #setNoValueResourceKey(ResKey)
	 */
	public static final Property<ResKey> NO_VALUE_I18N_KEY_PROPERTY = TypedAnnotatable.property(ResKey.class, "noValueKey");

	private Constraint fileNameConstraint;

	/**
	 * Constraint that wraps a Constraint which checks potential file names.
	 * That wrapping constraint is added as ordinary constraint to this field
	 * and dispatches to the wrapped constraint using the name of the dataItem.
	 * to check.
	 */
	private DelegateConstraint fileNameConstraintDelegate;

	/**
	 * #see {@link #isReadOnly()}
	 */
	private boolean readOnly;

	/** #see {@link #isMultiple()} */
	private boolean _multiple;

	/** #see {@link #isDownload()} */
	private boolean _isDownload = true;

	/**
	 * Accepted file types for file upload dialogs.
	 * 
	 * @see #getAcceptedTypes()
	 */
	private String _acceptedTypes;

	private long _maxUploadSize;

	DataField(String name, boolean isMultiple) {
		super(name, !MANDATORY, !IMMUTABLE, NORMALIZE, NO_CONSTRAINT);

		_multiple = isMultiple;
	}

	@Override
	protected Object narrowValue(Object aValue) throws IllegalArgumentException, ClassCastException {
		if (aValue == null) {
			return null;
		} else if (aValue instanceof List) {
			return castList((List<?>) aValue);
		} else {
			return cast(aValue);
		}
	}

	private static List<BinaryData> castList(List<?> list) {
		ArrayList<BinaryData> result = new ArrayList<>(list.size());
		for (Object obj : list) {
			result.add(cast(obj));
		}
		return result;
	}

	private static BinaryData cast(Object aValue) {
		return BinaryData.cast(aValue);
	}

	/**
	 * This method changes the read only mode of this {@link DataField}.
	 * 
	 * @param readOnly
	 *        Whether the field should be read only.
	 * 
	 * @see DataField#READ_ONLY_PROPERTY
	 */
	public void setReadOnly(boolean readOnly) {
		if (this.readOnly != readOnly) {
			this.readOnly = readOnly;
			this.firePropertyChanged(READ_ONLY_PROPERTY, self(), !readOnly, readOnly);
		}
	}

	/**
	 * States whether this {@link DataField} is read only. In read only mode the
	 * user has no chance to change the value of the field directly.
	 */
	public boolean isReadOnly() {
		return this.readOnly;
	}

	/**
	 * Whether this {@link DataField} accepts multiple files to be uploaded at once.
	 */
	public final boolean isMultiple() {
		return _multiple;
	}

	/**
	 * Whether this {@link DataField} allows to download uploaded values.
	 */
	public final boolean isDownload() {
		return _isDownload;
	}

	/**
	 * See {@link #isDownload()}.
	 * 
	 * <p>
	 * Note: This property must only be altered directly after construction, since it is not
	 * observed by the display.
	 * </p>
	 */
	public void setDownload(boolean canDownload) {
		_isDownload = canDownload;
	}

	/**
	 * Raw value is also application value
	 * 
	 * @see AbstractFormField#parseRawValue(Object)
	 */
	@Override
	protected Object parseRawValue(Object aRawValue) throws CheckException {
		return aRawValue;
	}

	/**
	 * Application value is also raw value
	 * 
	 * @see AbstractFormField#unparseValue(Object)
	 */
	@Override
	protected Object unparseValue(Object aValue) {
		return aValue;
	}

	@Override
	public Object visit(FormMemberVisitor v, Object arg) {
		return v.visitDataField(this, arg);
	}

	/**
	 * Type safe getter for the value of this field.
	 * 
	 * <p>
	 * Note: This method must only be called for non-{@link #isMultiple() multiple} fields. For
	 * safety reasons, it is always better to call {@link #getDataItems()}.
	 * </p>
	 * 
	 * @see #getDataItems()
	 * 
	 * @return The uploaded data, or <code>null</code>, if this field does not contain any data.
	 */
	public final BinaryData getDataItem() {
		if (isMultiple()) {
			throw new IllegalStateException("Field is multiple, please call getDataItems() instead of getDataItem()!");
		} else {
			Object value = this.getValue();
			if (value instanceof Collection<?>) {
				return cast(CollectionUtil.getSingleValueFrom(value));
			} else {
				return cast(value);
			}
		}
	}

	/**
	 * Type safe getter for the {@link #getValue() value of this field}.
	 * 
	 * @return The uploaded contents, or the empty list, if this field does not contain any data.
	 * 
	 * @see #getValue()
	 */
	public final List<BinaryData> getDataItems() {
		return toItems(getValue());
	}

	/**
	 * Converts a generic field value to a typed {@link DataField} item list.
	 * 
	 * @see #getDataItems()
	 */
	public static List<BinaryData> toItems(Object value) {
		if (value instanceof List<?>) {
			return castList((List<?>) value);
		} else {
			return CollectionUtilShared.singletonOrEmptyList(cast(value));
		}
	}

	/**
	 * The constraint to check upload file names with.
	 */
	public Constraint getFileNameConstraint() {
		return fileNameConstraint;
	}

	/**
	 * Sets a constraint which checks the name of the file to store in this {@link DataField}. This
	 * is a designed as separate constraint as it is also checked <b>before</b> the file is
	 * uploaded. The constraint must be able to check {@link String}.
	 * 
	 * @param fileNameConstraint
	 *        the constraint to check. may be <code>null</code> if no special constraint for the
	 *        file name should be set.
	 * 
	 * @see DataField#checkFileName(String)
	 */
	public void setFileNameConstraint(Constraint fileNameConstraint) {
		if (this.fileNameConstraint != null) {
			removeConstraint(fileNameConstraintDelegate);
		}
		this.fileNameConstraint = fileNameConstraint;
		if (this.fileNameConstraint != null) {
			if (fileNameConstraintDelegate == null) {
				fileNameConstraintDelegate = new DelegateConstraint();
			}
			fileNameConstraintDelegate.setDelegate(this.fileNameConstraint);
			addConstraint(fileNameConstraintDelegate);
		}
	}

	/**
	 * checks whether the given fileName can be the {@link BinaryData#getName()
	 * name} of some {@link BinaryData} to store in this {@link DataField}.
	 * 
	 * @param fileName
	 *        the fileName to check
	 * 
	 * @throws CheckException
	 *         when the given filename does not fulfill the constraints for a
	 *         filename of the data of this filed.
	 * 
	 * @see Constraint#check(Object)
	 */
	public void checkFileName(String fileName) throws CheckException {
		if (fileNameConstraint != null) {
			fileNameConstraint.check(fileName);
		}
	}

	/**
	 * The maximum size of a single file to upload.
	 * 
	 * <p>
	 * A value of <code>0</code> means no limit.
	 * </p>
	 */
	public long getMaxUploadSize() {
		return _maxUploadSize;
	}

	/**
	 * @see #getMaxUploadSize()
	 */
	public void setMaxUploadSize(long maxUploadSize) {
		_maxUploadSize = maxUploadSize;
	}

	/**
	 * Sets the resource key of the message which may be displayed when this
	 * field has currently no value.
	 * 
	 * @param resKey
	 *        the I18N key. may be <code>null</code>.
	 * 
	 * @return the previous I18N key or <code>null</code> if it was never set.
	 */
	public ResKey setNoValueResourceKey(ResKey resKey) {
		return set(NO_VALUE_I18N_KEY_PROPERTY, resKey);
	}

	/**
	 * Constraint which delegates the check of some non <code>null</code>
	 * {@link BinaryData} to an inner constraint check of the
	 * {@link BinaryData#getName() item name}.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	/*package protected*/ static class DelegateConstraint implements Constraint {

		private Constraint delegate;

		@Override
		public boolean check(Object value) throws CheckException {
			if (value instanceof List) {
				for (BinaryDataSource theData : CollectionUtil.dynamicCastView(BinaryDataSource.class,
					(List<?>) value)) {
					if (!delegate.check(theData.getName())) {
						return false;
					}
				}
			}
			else if (value instanceof BinaryDataSource) {
				return delegate.check(((BinaryDataSource) value).getName());
			}

			return true;
		}

		@Override
		public Collection<FormField> reportDependencies() {
			return delegate.reportDependencies();
		}

		public void setDelegate(Constraint delegate) {
			this.delegate = delegate;
		}

	}

	@Override
	protected DataField self() {
		return this;
	}

	/**
	 * @see DataField#getAcceptedTypes()
	 */
	public void setAcceptedTypes(String acceptedTypes) {
		_acceptedTypes = acceptedTypes;
	}

	/**
	 * Accepted file types for file upload dialogs.
	 * <p>
	 * The values are written to the {@link HTMLConstants#ACCEPT_ATTR accept} attribute without
	 * further processing.
	 * </p>
	 * <p>
	 * See <a href=
	 * "https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input/file#accept">https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input/file#accept</a>
	 * and <a href=
	 * "https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input/file#Unique_file_type_specifiers">https://developer.mozilla.org/en-US/docs/Web/HTML/Element/input/file#Unique_file_type_specifiers</a>.
	 * </p>
	 * 
	 * @return Accepted types separated by comma. Null, if they have not been set.
	 */
	public String getAcceptedTypes() {
		return _acceptedTypes;
	}

}
