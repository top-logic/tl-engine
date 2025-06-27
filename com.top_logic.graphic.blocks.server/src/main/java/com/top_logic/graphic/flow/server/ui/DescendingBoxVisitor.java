/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.server.ui;

import java.util.Arrays;
import java.util.function.BinaryOperator;

import com.top_logic.graphic.flow.data.Align;
import com.top_logic.graphic.flow.data.Border;
import com.top_logic.graphic.flow.data.Box;
import com.top_logic.graphic.flow.data.Box.Visitor;
import com.top_logic.graphic.flow.data.ClickTarget;
import com.top_logic.graphic.flow.data.CompassLayout;
import com.top_logic.graphic.flow.data.Empty;
import com.top_logic.graphic.flow.data.Fill;
import com.top_logic.graphic.flow.data.FloatingLayout;
import com.top_logic.graphic.flow.data.GridLayout;
import com.top_logic.graphic.flow.data.HorizontalLayout;
import com.top_logic.graphic.flow.data.Image;
import com.top_logic.graphic.flow.data.Padding;
import com.top_logic.graphic.flow.data.SelectableBox;
import com.top_logic.graphic.flow.data.Stack;
import com.top_logic.graphic.flow.data.Text;
import com.top_logic.graphic.flow.data.Tooltip;
import com.top_logic.graphic.flow.data.VerticalLayout;
import com.top_logic.graphic.flow.data.Widget;

/**
 * {@link Visitor} that descends through all elements of a diagram.
 */
public abstract class DescendingBoxVisitor<R, A> implements Box.Visitor<R, A, RuntimeException>, BinaryOperator<R> {

	/**
	 * Result to produce for leaf nodes.
	 */
	protected abstract R none();

	/**
	 * Callback that is informed of each generic {@link Widget} that is being visited.
	 * 
	 * @param self
	 *        The visited generic node.
	 * @param arg
	 *        The visit argument.
	 */
	protected R visitBox(Widget self, A arg) {
		return none();
	}

	@Override
	public R visit(Empty self, A arg) throws RuntimeException {
		return visitBox(self, arg);
	}

	@Override
	public R visit(Image self, A arg) throws RuntimeException {
		return visitBox(self, arg);
	}

	@Override
	public R visit(Text self, A arg) throws RuntimeException {
		return visitBox(self, arg);
	}

	@Override
	public R visit(SelectableBox self, A arg) throws RuntimeException {
		return apply(visitBox(self, arg), self.getContent().visit(this, arg));
	}

	@Override
	public R visit(ClickTarget self, A arg) throws RuntimeException {
		return apply(visitBox(self, arg), self.getContent().visit(this, arg));
	}

	@Override
	public R visit(Tooltip self, A arg) throws RuntimeException {
		return apply(visitBox(self, arg), self.getContent().visit(this, arg));
	}

	@Override
	public R visit(Align self, A arg) throws RuntimeException {
		return apply(visitBox(self, arg), self.getContent().visit(this, arg));
	}

	@Override
	public R visit(Border self, A arg) throws RuntimeException {
		return apply(visitBox(self, arg), self.getContent().visit(this, arg));
	}

	@Override
	public R visit(Fill self, A arg) throws RuntimeException {
		return apply(visitBox(self, arg), self.getContent().visit(this, arg));
	}

	@Override
	public R visit(Padding self, A arg) throws RuntimeException {
		return apply(visitBox(self, arg), self.getContent().visit(this, arg));
	}

	@Override
	public R visit(GridLayout self, A arg) throws RuntimeException {
		return apply(visitBox(self, arg),
			self.getContents().stream().map(b -> b.visit(this, arg)).reduce(this).orElse(null));
	}

	@Override
	public R visit(HorizontalLayout self, A arg) throws RuntimeException {
		return apply(visitBox(self, arg),
			self.getContents().stream().map(b -> b.visit(this, arg)).reduce(this).orElse(null));
	}

	@Override
	public R visit(VerticalLayout self, A arg) throws RuntimeException {
		return apply(visitBox(self, arg),
			self.getContents().stream().map(b -> b.visit(this, arg)).reduce(this).orElse(null));
	}

	@Override
	public R visit(FloatingLayout self, A arg) throws RuntimeException {
		return apply(visitBox(self, arg),
			self.getNodes().stream().map(b -> b.visit(this, arg)).reduce(this).orElse(null));
	}

	@Override
	public R visit(Stack self, A arg) throws RuntimeException {
		return apply(visitBox(self, arg),
			self.getContents().stream().map(b -> b.visit(this, arg)).reduce(this).orElse(null));
	}

	@Override
	public R visit(CompassLayout self, A arg) throws RuntimeException {
		return apply(visitBox(self, arg),
			Arrays.asList(self.getNorth(), self.getWest(), self.getEast(), self.getSouth(), self.getCenter())
				.stream().map(b -> b.visit(this, arg)).reduce(this).orElse(null));
	}

}
