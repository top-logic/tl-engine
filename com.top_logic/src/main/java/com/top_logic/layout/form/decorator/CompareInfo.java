/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.decorator;

import java.awt.Color;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.tools.NameBuilder;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.Utils;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.provider.LabelProviderService;

/**
 * Information object containing the differences of two form fields.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class CompareInfo implements DecorateInfo {

	private static Mapping<Object, Object> IDENTIFIER_MAPPING = new Mapping<>() {
		@Override
		public Object map(Object input) {
			Mapping<Object, ?> mapping = (Mapping<Object, ?>) LabelProviderService.getInstance().getMapping(input);
			return mapping.map(input);
		}
	};

	private final Object _baseValue;

	private final Object _changeValue;

	private final LabelProvider _labels;

	private ChangeInfo _changeInfo;

	/**
	 * Creates a new {@link CompareInfo}.
	 */
	public CompareInfo(Object baseValue, Object changeValue, LabelProvider labels) {
		_baseValue = baseValue;
		_changeValue = changeValue;
		_labels = labels;
	}

	@Override
	public String toString() {
		return new NameBuilder(this)
			.add("change", _changeInfo)
			.add("baseValue", _baseValue)
			.add("changeValue", _changeValue)
			.build();
	}

	/**
	 * Return the information about the content change.
	 * 
	 * @return The requested change information.
	 */
	public ChangeInfo getChangeInfo() {
		if (_changeInfo == null) {
			_changeInfo = createChangeInfo();
		}

		return _changeInfo;
	}

	/**
	 * Return the CSS class describing the change in this info object.
	 * 
	 * @return The requested CSS class.
	 */
	public String getChangedCSSClass() {
		return getChangeInfo().getCSSClass();
	}

	/**
	 * Return the tool tip describing the change in this info object.
	 * 
	 * @return The requested tooltip key.
	 */
	public ResKey getTooltip() {
		return ResKey.message(getI18NKey().tooltip(), getBaseValueTooltip(), getChangeValueTooltip());
	}

	/**
	 * Return the I18N key describing the change in this info object.
	 * 
	 * @return The requested I18N key.
	 */
	public ResKey getI18NKey() {
		return getChangeInfo().getI18NKey();
	}

	/**
	 * Return the {@link Color} used in excel for this info object.
	 * 
	 * @return The requested excel color.
	 */
	public Color getExcelColor() {
		return getChangeInfo().getExcelColor();
	}

	/**
	 * Return the image describing the change in this info object.
	 * 
	 * @return The requested image. May be <code>null</code>
	 */
	public ThemeImage getChangedIcon() {
		return getChangeInfo().getImage();
	}

	/**
	 * Return the value of the base object.
	 * 
	 * @return The requested value. May be <code>null</code>.
	 */
	public Object getBaseValue() {
		return _baseValue;
	}

	/**
	 * Return the value of the change object.
	 * 
	 * @return The requested value. May be <code>null</code>.
	 */
	public Object getChangeValue() {
		return _changeValue;
	}

	/**
	 * The {@link LabelProvider} used to get {@link #getTooltip()}.
	 */
	public LabelProvider getLabels() {
		return _labels;
	}

	/**
	 * Return the tooltip for the base value.
	 * 
	 * @return The requested tooltip.
	 */
	public String getBaseValueTooltip() {
		return getTooltip(_baseValue);
	}

	/**
	 * Return the tool tip for the changed value.
	 * 
	 * @return The requested tooltip.
	 */
	public String getChangeValueTooltip() {
		return getTooltip(_changeValue);
	}

	/**
	 * Create a tooltip for the given object.
	 * 
	 * @param aValue
	 *        The value to get the tooltip for.
	 * 
	 * @return The requested tooltip.
	 */
	protected String getTooltip(Object aValue) {
		if (_labels instanceof ResourceProvider) {
			String tooltip = ((ResourceProvider) _labels).getTooltip(aValue);
			/* Check whether tooltip is useful. It may be that the ResourceProvider is derived from
			 * a LabelProvider. In such case the label is a better tooltip. */
			if (!StringServices.isEmpty(tooltip)) {
				return tooltip;
			}
		}
		return _labels.getLabel(aValue);
	}

	/**
	 * Create the change information of this instance.
	 * 
	 * @return The requested change info, never <code>null</code>.
	 */
	protected ChangeInfo createChangeInfo() {
		if (StringServices.isEmpty(_baseValue)) {
			if (StringServices.isEmpty(_changeValue)) {
				return ChangeInfo.NO_CHANGE;
			} else {
				return ChangeInfo.CREATED;
			}
		}

		if (StringServices.isEmpty(_changeValue)) {
			return ChangeInfo.REMOVED;
		}

		return compareObject(_baseValue, _changeValue);
	}

	private ChangeInfo compareObject(Object baseValue, Object changeValue) {
		if (Utils.equals(baseValue, changeValue)) {
			return ChangeInfo.NO_CHANGE;
		}
		if (!Utils.equals(identifier(baseValue), identifier(changeValue))) {
			return ChangeInfo.CHANGED;
		}
		String changeValueTooltip = getChangeValueTooltip();
		String baseValueTooltip = getBaseValueTooltip();
		if (!Utils.equals(changeValueTooltip, baseValueTooltip)) {
			return ChangeInfo.DEEP_CHANGED;
		}
		return ChangeInfo.NO_CHANGE;
	}

	/** 
	 * The identifier of the given object, that is used to determine whether the value has changed or not.
	 */
	public static Object identifier(Object o) {
		return CompareInfo.identifierMapping().map(o);
	}

	/**
	 * Mapping of an object to its identifying key, i.e. to the key that identifies objects in a
	 * comparison mode.
	 */
	public static final Mapping<Object, Object> identifierMapping() {
		return IDENTIFIER_MAPPING;
	}

	/**
	 * The object of this {@link CompareInfo} which is displayed.
	 * 
	 * @see #getOtherObject() The corresponding method to get not displayed object.
	 */
	public Object getDisplayedObject() {
		if (getChangeInfo() == ChangeInfo.REMOVED) {
			return getBaseValue();
		}
		return getChangeValue();
	}

	/**
	 * The object of this {@link CompareInfo} which is <b>not</b> displayed.
	 * 
	 * @see #getDisplayedObject() The corresponding method to get displayed object.
	 */
	public Object getOtherObject() {
		if (getChangeInfo() == ChangeInfo.REMOVED) {
			return getChangeValue();
		}
		return getBaseValue();
	}

}
