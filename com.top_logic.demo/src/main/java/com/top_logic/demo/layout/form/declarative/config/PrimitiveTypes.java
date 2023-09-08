/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.declarative.config;

import java.awt.Paint;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Binding;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.MapBinding;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.format.MillisFormat;
import com.top_logic.basic.config.format.XMLFragmentString;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.values.MultiLineText;
import com.top_logic.layout.form.values.edit.annotation.AcceptedTypes;
import com.top_logic.layout.form.values.edit.annotation.ControlProvider;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay;
import com.top_logic.layout.form.values.edit.annotation.ItemDisplay.ItemDisplayType;
import com.top_logic.layout.form.values.edit.annotation.PropertyEditor;
import com.top_logic.layout.form.values.edit.annotation.RenderWholeLine;
import com.top_logic.layout.form.values.edit.editor.BinaryDataEditor;
import com.top_logic.layout.wysiwyg.ui.StructuredTextControlProvider;
import com.top_logic.reporting.flex.chart.config.color.HexEncodedPaint;

/**
 * {@link TypeDemos} part for demonstrating how primitive values are displayed .
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
@DisplayOrder({
	PrimitiveTypes.BOOLEAN_PRIMITIVE,
	PrimitiveTypes.BYTE_PRIMITIVE,
	PrimitiveTypes.SHORT_PRIMITIVE,
	PrimitiveTypes.CHARACTER_PRIMITIVE,
	PrimitiveTypes.INTEGER_PRIMITIVE,
	PrimitiveTypes.LONG_PRIMITIVE,
	PrimitiveTypes.FLOAT_PRIMITIVE,
	PrimitiveTypes.DOUBLE_PRIMITIVE,
	PrimitiveTypes.PAINT_PRIMITIVE,
	PrimitiveTypes.TIME_PRIMITIVE,
	PrimitiveTypes.LIST_PRIMITIVE,
	PrimitiveTypes.MAP_PRIMITIVE,
	PrimitiveTypes.XML_PRIMITIVE,
	PrimitiveTypes.BINARY_PRIMITIVE,
	PrimitiveTypes.BINARIES,
	PrimitiveTypes.ICON_PRIMITIVE,
	PrimitiveTypes.RESKEY_PRIMITIVE,
	PrimitiveTypes.RESKEY_MULTILINE_PRIMITIVE,
	PrimitiveTypes.RESKEY_HTML_PRIMITIVE,
})
public interface PrimitiveTypes extends ConfigurationItem {

	/** @see #getBooleanPrimitive() */
	String BOOLEAN_PRIMITIVE = "boolean-primitive";

	/** @see #getBytePrimitive() */
	String BYTE_PRIMITIVE = "byte-primitive";

	/** @see #getShortPrimitive() */
	String SHORT_PRIMITIVE = "short-primitive";

	/** @see #getIntegerPrimitive() */
	String INTEGER_PRIMITIVE = "integer-primitive";

	/** @see #getLongPrimitive() */
	String LONG_PRIMITIVE = "long-primitive";

	/** @see #getCharacterPrimitive() */
	String CHARACTER_PRIMITIVE = "character-primitive";

	/** @see #getFloatPrimitive() */
	String FLOAT_PRIMITIVE = "float-primitive";

	/** @see #getDoublePrimitive() */
	String DOUBLE_PRIMITIVE = "double-primitive";

	/** @see #getColorPrimmitive() */
	String PAINT_PRIMITIVE = "paint-primitive";

	/** @see #getTimePrimitive() */
	String TIME_PRIMITIVE = "time-primitive";

	/** @see #getListPrimitive() */
	String LIST_PRIMITIVE = "list-primitive";

	/** @see #getMapPrimitive() */
	String MAP_PRIMITIVE = "map-primitive";

	/** @see #getXmlPrimitive() */
	String XML_PRIMITIVE = "xml-primitive";

	/** @see #getBinaryPrimitive() */
	String BINARY_PRIMITIVE = "binary-primitive";

	/** @see #getBinaries() */
	String BINARIES = "binaries";
	
	/** @see #getIconPrimitive() */
	String ICON_PRIMITIVE = "icon-primitive";

	/** @see #getResKeyPrimitive() */
	String RESKEY_PRIMITIVE = "reskey-primitive";

	/** @see #getResKeyMultilinePrimitive() */
	String RESKEY_MULTILINE_PRIMITIVE = "reskey-multiline-primitive";

	/** @see #getResKeyHtmlPrimitive() */
	String RESKEY_HTML_PRIMITIVE = "reskey-html-primitive";

	@Name(BOOLEAN_PRIMITIVE)
	boolean getBooleanPrimitive();

	@Name(BYTE_PRIMITIVE)
	byte getBytePrimitive();

	@Name(SHORT_PRIMITIVE)
	short getShortPrimitive();

	@Name(INTEGER_PRIMITIVE)
	int getIntegerPrimitive();

	@Name(LONG_PRIMITIVE)
	long getLongPrimitive();

	@Name(CHARACTER_PRIMITIVE)
	char getCharacterPrimitive();

	@Name(FLOAT_PRIMITIVE)
	float getFloatPrimitive();

	@Name(DOUBLE_PRIMITIVE)
	double getDoublePrimitive();

	/**
	 * Demo for a custom primitive type that is entered as raw configuration value.
	 */
	@Name(PAINT_PRIMITIVE)
	@Format(HexEncodedPaint.class)
	Paint getColorPrimmitive();

	/**
	 * Specialized long property that is entered in a time format "1h 15min".
	 */
	@Name(TIME_PRIMITIVE)
	@Format(MillisFormat.class)
	@LongDefault(0L)
	long getTimePrimitive();

	/**
	 * A list of simple strings.
	 */
	@Name(LIST_PRIMITIVE)
	@ListBinding
	List<String> getListPrimitive();

	/**
	 * A map of string to integer.
	 */
	@Name(MAP_PRIMITIVE)
	@MapBinding
	Map<String, Integer> getMapPrimitive();

	/**
	 * An XML fragment in a string property.
	 */
	@Name(XML_PRIMITIVE)
	@Binding(XMLFragmentString.class)
	String getXmlPrimitive();

	/**
	 * A binary data upload.
	 */
	@Name(BINARY_PRIMITIVE)
	BinaryData getBinaryPrimitive();
	
	/**
	 * Upload of multiple XML files.
	 */
	@Name(BINARIES)
	@InstanceFormat
	@ItemDisplay(ItemDisplayType.VALUE)
	@PropertyEditor(BinaryDataEditor.class)
	@AcceptedTypes(value = { "text/xml", "application/xml", ".xml" })
	@RenderWholeLine
	List<BinaryData> getBinaries();

	/**
	 * An icon property.
	 */
	@Name(ICON_PRIMITIVE)
	ThemeImage getIconPrimitive();

	/**
	 * An internationalized string.
	 */
	@Name(RESKEY_PRIMITIVE)
	@ItemDisplay(ItemDisplayType.MONOMORPHIC)
	ResKey getResKeyPrimitive();

	/**
	 * An internationalized multi-line text.
	 */
	@Name(RESKEY_MULTILINE_PRIMITIVE)
	@ItemDisplay(ItemDisplayType.MONOMORPHIC)
	@ControlProvider(MultiLineText.class)
	ResKey getResKeyMultilinePrimitive();

	/**
	 * An internationalized structured text.
	 */
	@Name(RESKEY_HTML_PRIMITIVE)
	@ItemDisplay(ItemDisplayType.MONOMORPHIC)
	@ControlProvider(StructuredTextControlProvider.class)
	ResKey getResKeyHtmlPrimitive();

}
