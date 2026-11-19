/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.form;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
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
 * A field holding one image or several, showing one image at a time as large as the form allows.
 *
 * <p>
 * The field shows the image it is at on a stage as wide as the field, at most as high as the theme
 * or the {@link #setDimensions(String, String) dimensions} of the field allow; with
 * {@link #isMultiple() several images}, buttons and the arrow keys step through them and a counter
 * tells where the field is. A click on the image opens it in a view filling the page. A field that
 * is {@link FieldModel#isEditable() edited} has buttons below the stage adding images after the
 * image shown, replacing it, removing it and, with several images, opening an editor covering the
 * page: a tile per image that replaces its image by a file dropped on it or picked through its
 * button, removes it, and moves it among the others where the {@link #isOrdered() order} is part of
 * the value, followed by an area adding further images. A file dropped on the stage of a field
 * holding a single image replaces it; dropped on the stage of a field holding several, it is
 * inserted after the image shown. Without an image, the stage is an area an image is dropped on or
 * that opens the file picker. The control takes a {@link #prefersWholeLine() whole row} of a form.
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
 * {@link #REPLACE_PART} replaces the image of the tile it names by the first file, an upload carrying
 * an {@link #AFTER_PART} inserts the files after the image of the tile it names.
 * </p>
 *
 * <p>
 * The state carries the {@link #IMAGES}, whether the field holds {@link #MULTIPLE several} images,
 * whether they are {@link #ORDERED arranged} by the user, the file types the file picker offers
 * under {@link UploadSupport#ACCEPT}, the largest {@link #WIDTH width} and {@link #HEIGHT height} of
 * the stage, the {@link #maxUploadSize() upload limit} under
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

	/**
	 * Name of the multipart part of an upload naming the tile after whose image the uploaded files
	 * are inserted; its content is the {@link #IMAGE_KEY key} of the tile.
	 */
	public static final String AFTER_PART = "after";

	/**
	 * State key for the CSS width the stage shows an image at most, absent for the width of the
	 * field.
	 */
	public static final String WIDTH = "width";

	/**
	 * State key for the CSS height the stage shows an image at most, absent for the height of the
	 * theme.
	 */
	public static final String HEIGHT = "height";

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
	 * than there is room for is refused as a whole, see {@link ValueCountLimit}. Replacing or
	 * removing an image stays possible.
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

	/**
	 * Limits the size the stage shows an image at.
	 *
	 * @param width
	 *        The CSS width an image is shown at most, <code>null</code> for the width of the field.
	 * @param height
	 *        The CSS height an image is shown at most, <code>null</code> for the height of the
	 *        theme.
	 */
	public void setDimensions(String width, String height) {
		putState(WIDTH, width);
		putState(HEIGHT, height);
	}

	@Override
	public boolean prefersWholeLine() {
		return true;
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
	 * cache of an earlier image. Values are matched by equality, because reading the value of an
	 * attribute again may answer new but equal objects, e.g. the files of a multi-valued binary
	 * attribute; each previous key is taken by one value only, so two equal images are two tiles.
	 * </p>
	 */
	private void updateImages() {
		Map<Object, Deque<String>> previousKeys = new HashMap<>();
		for (int n = 0, cnt = _values.size(); n < cnt; n++) {
			previousKeys.computeIfAbsent(_values.get(n), x -> new ArrayDeque<>()).add(_keys.get(n));
		}

		List<Object> values = images(getFieldModel().getValue());
		List<String> keys = new ArrayList<>(values.size());
		List<Map<String, Object>> tiles = new ArrayList<>(values.size());
		for (Object value : values) {
			// Taken, so that the same value occurring twice gets two keys.
			Deque<String> candidates = previousKeys.get(value);
			String key = candidates == null ? null : candidates.poll();
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
	 * inserts every file, in the order the client sent them, after the image of the tile an
	 * {@link #AFTER_PART} names, or appends them where the upload names no tile still shown.
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
		String afterKey;
		try {
			files = UploadSupport.toBinaryData(parts);
			replacedKey = partText(parts, REPLACE_PART);
			afterKey = partText(parts, AFTER_PART);
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
			int after = afterKey == null ? -1 : _keys.indexOf(afterKey);
			int position = after >= 0 ? after + 1 : values.size();
			for (BinaryData file : files) {
				values.add(position++, _imageValues.valueOf(file));
			}
		}
		getFieldModel().setValue(values);
		return null;
	}

	/**
	 * The text of the part of the given upload with the given name, the key of a tile, or
	 * <code>null</code> if the upload has no such part.
	 */
	private static String partText(Collection<Part> parts, String name) throws IOException {
		for (Part part : parts) {
			if (name.equals(part.getName())) {
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
