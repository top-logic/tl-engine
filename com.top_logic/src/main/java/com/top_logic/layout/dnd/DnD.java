/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.dnd;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.scripting.recorder.ScriptingRecorder;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.layout.scripting.recorder.ref.value.ListNaming;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutComponentScope;

/**
 * Utilities for implementing drag'n drop in controls.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DnD {

	private static final Function<Object, ModelName> ERROR_ON_CALL_SCRIPTING_NOT_ENABLED = dragSource -> {
		throw new IllegalStateException("Function must only be called when scripting is enabled.");
	};

	/**
	 * Prefix of a drag'n drop data url.
	 * 
	 * <p>
	 * A data URL is composed on the client during a drag'n drop operation. After the drop, it is
	 * transmitted to the server-side representation of the drop target. By parsing this URL, the
	 * drop target is able to find the drag source and identify the dragged object.
	 * </p>
	 */
	public static final String DND_DATA_PREFIX = "dnd://";

	private static final char DND_DATA_SEPARATOR = '/';

	/**
	 * Control command parameter that receives the drag data URL.
	 * 
	 * @see #DND_DATA_PREFIX
	 */
	public static final String DATA_PARAM = "data";

	/**
	 * Looks up the drag data parameter from the arguments map and parses its contens.
	 * 
	 * @param context
	 *        The command {@link DisplayContext}.
	 * @param arguments
	 *        The control command arguments.
	 * @return The {@link DndData} that identifies the dragged object.
	 */
	public static DndData getDndData(DisplayContext context, Map<String, ?> arguments) {
		String dataUrl = (String) arguments.get(DnD.DATA_PARAM);
		if (!dataUrl.startsWith(DnD.DND_DATA_PREFIX)) {
			return null;
		}
		return parse(context, dataUrl);
	}

	private static DndData parse(DisplayContext context, String dataUrl) {
		int scopeStartIndex = DnD.DND_DATA_PREFIX.length();
		int scopeEndIndex = dataUrl.indexOf(DnD.DND_DATA_SEPARATOR, scopeStartIndex);
		int controlStartIndex = scopeEndIndex + 1;
		int controlEndIndex = dataUrl.indexOf(DnD.DND_DATA_SEPARATOR, controlStartIndex);
		int refStartIndex = controlEndIndex + 1;

		String scopeName = dataUrl.substring(scopeStartIndex, scopeEndIndex);
		String controlId = dataUrl.substring(controlStartIndex, controlEndIndex);
		String references = dataUrl.substring(refStartIndex);

		LayoutComponent scope;
		try {
			scope = context.getLayoutContext().getMainLayout()
				.getComponentByName(ComponentName.newConfiguredName(DATA_PARAM, scopeName));
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(ex);
		}
		LayoutComponentScope frameScope = scope.getEnclosingFrameScope();
		DragSourceSPI source = (DragSourceSPI) frameScope.getCommandListener(controlId);
		if (source == null) {
			// Control was removed in the meanwhile.
			return null;
		}
		String[] referenceIDs = references.split(",");
		return new DndData(source, getDragData(referenceIDs, source), getDragDataName(referenceIDs, source));
	}

	private static Collection<Object> getDragData(String[] referenceIDs, DragSourceSPI source) {
		return Arrays.stream(referenceIDs).map(source::getDragData).collect(Collectors.toList());
	}

	private static Function<Object, ModelName> getDragDataName(String[] referenceIDs, DragSourceSPI source) {
		if (ScriptingRecorder.isRecordingActive()) {
			return dragViewName -> {
				List<ModelName> modelNames = Arrays.stream(referenceIDs)
					.map(id -> createNameForDragData(id, source, dragViewName))
					.collect(Collectors.toList());
				ListNaming.Name name = TypedConfiguration.newConfigItem(ListNaming.Name.class);
				name.getValues().addAll(modelNames);
				return name;
			};
		} else {
			return ERROR_ON_CALL_SCRIPTING_NOT_ENABLED;
		}
	}

	private static ModelName createNameForDragData(String id, DragSourceSPI source, Object dragView) {
		Maybe<? extends ModelName> specialName = source.getDragDataName(dragView, id);
		if (specialName.hasValue()) {
			return specialName.get();
		}
		/* Try to build a name for the concrete business object that is dragged. */
		return ModelResolver.buildModelName(dragView, source.getDragData(id));
	}
}
