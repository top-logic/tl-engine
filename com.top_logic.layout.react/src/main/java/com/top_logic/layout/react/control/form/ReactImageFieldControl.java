/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.Part;

import com.top_logic.basic.Logger;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.model.FieldModel;
import com.top_logic.layout.react.DataProvider;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.UploadHandler;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.upload.UploadSupport;
import com.top_logic.layout.react.state.FieldState;
import com.top_logic.util.Resources;

import de.haumacher.msgbuf.data.DataObject;

/**
 * A field holding one image or several, each shown as a preview.
 *
 * <p>
 * A field holding a {@link #isMultiple() single image} shows its preview and, while the field is
 * {@link FieldModel#isEditable() editable}, a button removing it; without an image it is an area an
 * image is dropped on or that opens the file picker. A dropped or picked file replaces the image. A
 * field holding {@link #isMultiple() several images} shows a row of such tiles, one per image,
 * followed by the area adding further images. Each tile replaces its image by a file dropped on it or
 * picked through its button, removes it, and moves it within the row where the
 * {@link #isOrdered() order} is part of the value. A field that is only displayed shows the previews
 * alone.
 * </p>
 *
 * <p>
 * The field model holds a single value for a single image, the collection of them for several;
 * <code>null</code> and the empty collection both mean no image. What a value is - the image file
 * itself, an image of a gallery - its {@link ImageValues} say: the image a value shows, and the value
 * an uploaded file becomes. The images never enter
 * the client state: the control serves the data endpoint as a {@link DataProvider}, delivering the
 * image of the tile whose key is requested, and the upload endpoint as an {@link UploadHandler},
 * taking every {@link UploadSupport#FILE_PART file part} of the request. An upload carrying a
 * {@link #REPLACE_PART} replaces the image of the tile it names by the first file.
 * </p>
 *
 * <p>
 * The state carries the {@link #IMAGES}, whether the field holds {@link #MULTIPLE several} images,
 * whether they are {@link #ORDERED arranged} by the user, the file types the file picker offers
 * under {@link UploadSupport#ACCEPT}, the {@link #maxUploadSize() upload limit} under
 * {@link UploadSupport#MAX_UPLOAD_SIZE}, the {@link #setMaxCount(int) maximum number of images}
 * under {@link ValueCountLimit#MAX_COUNT}, at which the client offers no further image, and the
 * message of a failed upload under {@link UploadSupport#ERROR}, cleared by the next successful
 * upload.
 * </p>
 *
 * @see ReactBinaryFieldControl
 * @see ReactFileChipsControl
 */
public class ReactImageFieldControl extends ReactFormFieldControl implements UploadHandler, DataProvider {

	private static final String REACT_MODULE = "TLImageField";

	/**
	 * Command sent by the client to remove one of the images, see {@link RemoveChipArguments}: the
	 * key names the tile.
	 */
	public static final String CMD_REMOVE_IMAGE = "removeImage";

	/**
	 * Command sent by the client to move one of the images to another position, see
	 * {@link MoveElementArguments}.
	 */
	public static final String CMD_MOVE_IMAGE = "moveImage";

	/**
	 * Name of the multipart part of an upload naming the tile whose image the uploaded file replaces;
	 * its content is the {@link #IMAGE_KEY key} of the tile.
	 */
	public static final String REPLACE_PART = "replace";

	/** State key for the tiles, one per image and in image order. */
	public static final String IMAGES = "images";

	/** State key for whether the field holds several images rather than one. */
	public static final String MULTIPLE = "multiple";

	/** State key for whether the user arranges the order of the images. */
	public static final String ORDERED = "ordered";

	/**
	 * Tile property for the key identifying the tile, see {@link RemoveChipArguments#KEY}; it is the
	 * key the data endpoint delivers the image of the tile under.
	 */
	public static final String IMAGE_KEY = RemoveChipArguments.KEY;

	/** Tile property for the name of the image file. */
	public static final String IMAGE_NAME = "name";

	private final ImageValues _imageValues;

	private final boolean _multiple;

	private final boolean _ordered;

	/** The values of the field model, in the order of {@link #_keys}. */
	private List<Object> _values = List.of();

	/** The tile key of each of the {@link #_values}, stable for a value as long as it is displayed. */
	private List<String> _keys = List.of();

	private int _nextKey;

	/** The size limit of one uploaded file, see {@link #maxUploadSize()}. */
	private long _maxUploadSize;

	/** The maximum number of images, see {@link #setMaxCount(int)}. */
	private ValueCountLimit _maxCount = ValueCountLimit.NONE;

	/**
	 * Creates a {@link ReactImageFieldControl}.
	 *
	 * @param context
	 *        The React context for ID allocation and SSE registration.
	 * @param model
	 *        Holds a single value, or the collection of them if the field holds several images.
	 * @param imageValues
	 *        What the values of the field are: the image each of them shows, the value an uploaded
	 *        file becomes.
	 * @param multiple
	 *        Whether the field holds several images rather than one.
	 * @param ordered
	 *        Whether the user arranges the order of several images; ignored for a single image.
	 */
	public ReactImageFieldControl(ReactContext context, FieldModel model, ImageValues imageValues, boolean multiple,
			boolean ordered) {
		super(context, model, REACT_MODULE);
		_imageValues = imageValues;
		_multiple = multiple;
		_ordered = multiple && ordered;
		// The images are displayed as previews; the value itself is nothing the client draws.
		putState(FieldState.VALUE__PROP, null);
		putState(MULTIPLE, Boolean.valueOf(_multiple));
		putState(ORDERED, Boolean.valueOf(_ordered));
		setMaxUploadSize(0);
		setMaxCount(0);
		updateImages();
	}

	/**
	 * Limits the number of images of a field holding {@link #isMultiple() several} of them.
	 *
	 * <p>
	 * The client offers no further image once the field holds that many; an upload of more images
	 * than there is room for is refused as a whole, see {@link ValueCountLimit}. Replacing an image
	 * does not change the number.
	 * </p>
	 *
	 * @param maxCount
	 *        The maximum number of images, <code>0</code> for no limit.
	 */
	public void setMaxCount(int maxCount) {
		_maxCount = ValueCountLimit.of(maxCount);
		putState(ValueCountLimit.MAX_COUNT, Integer.valueOf(_maxCount.getMax()));
	}

	/**
	 * Whether the field holds several images rather than one.
	 */
	public boolean isMultiple() {
		return _multiple;
	}

	/**
	 * Whether the user arranges the order of the images.
	 */
	public boolean isOrdered() {
		return _ordered;
	}

	/**
	 * Limits the size of each uploaded file to the given value, capped by the application-wide
	 * {@link UploadSupport#maxUploadSize() limit}.
	 *
	 * @param fieldLimit
	 *        The maximum size in bytes of one file the field accepts, <code>0</code> for no limit of
	 *        its own.
	 *
	 * @see UploadSupport#effectiveMaxUploadSize(long)
	 */
	public void setMaxUploadSize(long fieldLimit) {
		_maxUploadSize = UploadSupport.effectiveMaxUploadSize(fieldLimit);
		putState(UploadSupport.MAX_UPLOAD_SIZE, Long.valueOf(_maxUploadSize));
	}

	@Override
	public long maxUploadSize() {
		return _maxUploadSize;
	}

	/**
	 * Restricts the files the file picker offers to the given types.
	 *
	 * @param acceptedTypes
	 *        Comma separated list of MIME types like <code>image/*</code> and file patterns like
	 *        <code>*.png</code>, see {@link UploadSupport#acceptAttribute(String)}; <code>null</code>
	 *        to offer every file.
	 */
	public void setAcceptedTypes(String acceptedTypes) {
		putState(UploadSupport.ACCEPT, UploadSupport.acceptAttribute(acceptedTypes));
	}

	@Override
	protected void handleModelValueChanged(FieldModel source, Object oldValue, Object newValue) {
		updateImages();
	}

	/**
	 * Rebuilds the tiles from the value the field model now holds.
	 *
	 * <p>
	 * A value that was displayed before keeps the key of its tile, so the client keeps the identity
	 * of the tile and its loaded preview while images are added, removed and moved around it. A new
	 * value gets a key never used before in this control, so its preview is never taken from the
	 * cache of an earlier image. Values are told apart by identity, not by equality: two equal
	 * images are two tiles.
	 * </p>
	 */
	private void updateImages() {
		Map<Object, String> previousKeys = new IdentityHashMap<>();
		for (int n = 0, cnt = _values.size(); n < cnt; n++) {
			previousKeys.put(_values.get(n), _keys.get(n));
		}

		List<Object> values = images(getFieldModel().getValue());
		List<String> keys = new ArrayList<>(values.size());
		List<Map<String, Object>> tiles = new ArrayList<>(values.size());
		for (Object value : values) {
			// Removed, so that the same value occurring twice gets two keys.
			String key = previousKeys.remove(value);
			if (key == null) {
				key = String.valueOf(_nextKey++);
			}
			keys.add(key);
			tiles.add(tile(key, value));
		}
		_values = values;
		_keys = keys;
		putState(IMAGES, tiles);
	}

	/**
	 * The values of the given field value that show an image, in their order.
	 */
	private List<Object> images(Object value) {
		List<Object> result = ListElementFieldModel.elementsOfValue(value);
		result.removeIf(element -> _imageValues.imageOf(element) == null);
		return result;
	}

	private Map<String, Object> tile(String key, Object value) {
		Map<String, Object> tile = new LinkedHashMap<>();
		tile.put(IMAGE_KEY, key);
		tile.put(IMAGE_NAME, _imageValues.imageOf(value).getName());
		return tile;
	}

	@Override
	public BinaryData getDownloadData(String key) {
		int index = _keys.indexOf(key);
		if (index < 0) {
			return null;
		}
		return BinaryData.cast(_imageValues.imageOf(_values.get(index)));
	}

	/**
	 * Takes the uploaded files into the value, each as the value its {@link ImageValues} make of it.
	 *
	 * <p>
	 * A file uploaded for the tile a {@link #REPLACE_PART} names replaces the image of that tile. A
	 * field holding a single image takes the first file as its image. A field holding several images
	 * appends every file, in the order the client sent them.
	 * </p>
	 *
	 * <p>
	 * A failed upload leaves the value unchanged and reports {@link I18NConstants#FILE_UPLOAD_FAILED}
	 * under {@link UploadSupport#ERROR}, as does an upload adding more images than the
	 * {@link #setMaxCount(int) maximum} leaves room for, with the problem of
	 * {@link ValueCountLimit#checkAdd(int, int)}; a successful upload clears it.
	 * </p>
	 */
	@Override
	public DataObject handleUpload(DisplayContext context, Collection<Part> parts) {
		if (!acceptsClientValue()) {
			return null;
		}
		List<BinaryData> files;
		String replacedKey;
		try {
			files = UploadSupport.toBinaryData(parts);
			replacedKey = replacedKey(parts);
		} catch (IOException ex) {
			Logger.error("Failed to process image upload.", ex, ReactImageFieldControl.class);
			putState(UploadSupport.ERROR, Resources.getInstance().getString(I18NConstants.FILE_UPLOAD_FAILED));
			return null;
		}
		if (files.isEmpty()) {
			putState(UploadSupport.ERROR, null);
			return null;
		}
		if (!_multiple) {
			putState(UploadSupport.ERROR, null);
			getFieldModel().setValue(_imageValues.valueOf(files.get(0)));
			return null;
		}
		List<Object> values = new ArrayList<>(_values);
		int replaced = replacedKey == null ? -1 : _keys.indexOf(replacedKey);
		if (replaced < 0) {
			ResKey tooMany = _maxCount.checkAdd(values.size(), files.size());
			if (tooMany != null) {
				putState(UploadSupport.ERROR, Resources.getInstance().getString(tooMany));
				return null;
			}
		}
		putState(UploadSupport.ERROR, null);
		if (replaced >= 0) {
			values.set(replaced, _imageValues.valueOf(files.get(0)));
		} else {
			for (BinaryData file : files) {
				values.add(_imageValues.valueOf(file));
			}
		}
		getFieldModel().setValue(values);
		return null;
	}

	/**
	 * The key of the tile the {@link #REPLACE_PART} of the given upload names, <code>null</code> if
	 * it names none.
	 */
	private static String replacedKey(Collection<Part> parts) throws IOException {
		for (Part part : parts) {
			if (REPLACE_PART.equals(part.getName())) {
				return new String(part.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
			}
		}
		return null;
	}

	/**
	 * Removes the image of the tile the client names.
	 */
	@ReactCommandHandler(CMD_REMOVE_IMAGE)
	final void handleRemoveImage(RemoveChipArguments args) {
		if (!acceptsClientValue()) {
			return;
		}
		int index = _keys.indexOf(args.getKey());
		if (index < 0) {
			return;
		}
		if (!_multiple) {
			getFieldModel().setValue(null);
			return;
		}
		List<Object> values = new ArrayList<>(_values);
		values.remove(index);
		getFieldModel().setValue(values);
	}

	/**
	 * Moves the image the client names to the position it names, shifting the images in between.
	 */
	@ReactCommandHandler(CMD_MOVE_IMAGE)
	final void handleMoveImage(MoveElementArguments args) {
		if (!acceptsClientValue() || !_ordered) {
			return;
		}
		int index = args.getIndex();
		int targetIndex = args.getTargetIndex();
		int count = _values.size();
		if (index == targetIndex || index < 0 || index >= count || targetIndex < 0 || targetIndex >= count) {
			return;
		}
		List<Object> values = new ArrayList<>(_values);
		values.add(targetIndex, values.remove(index));
		getFieldModel().setValue(values);
	}

}
