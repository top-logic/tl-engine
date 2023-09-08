/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.server.commands;

import java.util.Map;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.util.ResKey;
import com.top_logic.graph.common.model.GraphPart;
import com.top_logic.graph.diagramjs.server.DiagramJSGraphControl;
import com.top_logic.graph.diagramjs.server.I18NConstants;
import com.top_logic.graph.diagramjs.server.util.layout.Bounds;
import com.top_logic.graph.diagramjs.server.util.layout.Dimension;
import com.top_logic.graph.diagramjs.server.util.layout.Position;
import com.top_logic.graph.diagramjs.util.DiagramJSGraphControlCommon;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link ControlCommand} for creating a class.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class CreateClassCommand extends ControlCommand {

	/**
	 * Property for {@link Bounds}.
	 */
	public static final String BOUNDS_PROPERTY_NAME = "bounds";

	private static final String HEIGHT = "height";

	private static final String WIDTH = "width";

	private static final String Y = "y";

	private static final String X = "x";

	/**
	 * Stores {@link GraphPart} bound layout informations.
	 */
	public static final Property<Bounds> BOUNDS = TypedAnnotatable.property(Bounds.class, BOUNDS_PROPERTY_NAME);

	/**
	 * Singleton instance of {@link CreateClassCommand}.
	 */
	public static final CreateClassCommand INSTANCE = new CreateClassCommand();

	private CreateClassCommand() {
		super(DiagramJSGraphControlCommon.CREATE_CLASS_COMMAND);
	}

	@Override
	public ResKey getI18NKey() {
		return I18NConstants.CREATE_CLASS_COMMAND;
	}

	@Override
	protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
		DiagramJSGraphControl graphControl = (DiagramJSGraphControl) control;

		Bounds bounds = createBounds(arguments);

		graphControl.createClass(bounds);

		return HandlerResult.DEFAULT_RESULT;
	}

	private double getLeftUpperYCoordinate(double y, double height) {
		return (y - height / 2);
	}

	private double getLeftUpperXCoordinate(double x, double width) {
		return (x - width / 2);
	}

	private Bounds createBounds(Map<String, Object> arguments) {
		double width = Double.valueOf((int) arguments.get(WIDTH));
		double height = Double.valueOf((int) arguments.get(HEIGHT));
		double x = getLeftUpperXCoordinate(Double.valueOf((int) arguments.get(X)), width);
		double y = getLeftUpperYCoordinate(Double.valueOf((int) arguments.get(Y)), height);

		return new Bounds(new Position(x, y), new Dimension(width, height));
	}

}
