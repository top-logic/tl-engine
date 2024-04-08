/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.html.template;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.TemplateVariable;
import com.top_logic.layout.template.WithProperties;
import com.top_logic.layout.template.WithPropertiesBase;

/**
 * A template expanding a {@link #getContent()} multiple times in the context of a bound loop
 * variable.
 */
public class ForeachTemplate extends ScopeTemplate implements RawTemplateFragment {

	private String _iteration;

	/**
	 * Creates a {@link ForeachTemplate}.
	 *
	 * @param var
	 *        See {@link #getVar()}.
	 * @param expression
	 *        See {@link #getExpression()}.
	 * @param content
	 *        See {@link #getContent()}
	 */
	public ForeachTemplate(String var, String interator, TemplateExpression expression, HTMLTemplateFragment content) {
		super(var, expression, content);

		_iteration = interator;
	}

	/**
	 * The optional iterator variable giving access to the current position of the iteration.
	 * 
	 * <p>
	 * The iterator variable has the following properties:
	 * </p>
	 * 
	 * <dl>
	 * <dt>index</dt>
	 * <dd>The current iteration index, starting with 0.</dd>
	 * 
	 * <dt>count</dt>
	 * <dd>The number of the current iteration, starting with 1.</dd>
	 * 
	 * <dt>size</dt>
	 * <dd>The total amount of elements in the iterated variable.</dd>
	 * 
	 * <dt>current</dt>
	 * <dd>The current iteration value.</dd>
	 * 
	 * <dt>even/odd</dt>
	 * <dd>Whether the current iteration count is even or odd. The first iteration is odd, the
	 * second is even, and so on.</dd>
	 * 
	 * <dt>first</dt>
	 * <dd>Whether the current iteration is the first one.</dd>
	 * 
	 * <dt>last</dt>
	 * <dd>Whether the current iteration is the last one.</dd>
	 * </dl>
	 */
	public String getIteration() {
		return _iteration;
	}

	@Override
	public void write(DisplayContext context, TagWriter out, WithProperties properties) throws IOException {
		Collection<?> elements = asCollection(getExpression().eval(context, properties));
		if (elements.isEmpty()) {
			return;
		}

		if (_iteration == null) {
			LocalVariable localVariable = new LocalVariable(getVar(), properties);

			for (Object x : elements) {
				localVariable.setValue(x);
				getContent().write(context, out, localVariable);
			}
		} else {
			LocalVariable iterationVar = new LocalVariable(_iteration, properties);
			LocalVariable localVariable = new LocalVariable(getVar(), iterationVar);

			Iteration iteration = new Iteration(elements.size());
			iterationVar.setValue(iteration);

			for (Object x : elements) {
				localVariable.setValue(x);
				iteration.next(x);

				getContent().write(context, out, localVariable);
			}
		}
	}

	/**
	 * The optional iteration object of a foreach loop.
	 */
	public static final class Iteration extends WithPropertiesBase {
		private final int _size;

		private int _index = -1;

		private Object _current = null;

		/**
		 * Creates a {@link ForeachTemplate.Iteration}.
		 */
		public Iteration(int size) {
			_size = size;
		}

		/**
		 * Advances the iteration to the next element.
		 * 
		 * <p>
		 * Is called before rendering each iteration value.
		 * </p>
		 */
		public void next(Object current) {
			_index++;
			_current = current;
		}

		/**
		 * The number of total elements to iterate.
		 */
		@TemplateVariable("size")
		public int getSize() {
			return _size;
		}
		
		/**
		 * The current iteration index (zero-based).
		 */
		@TemplateVariable("index")
		public int getIndex() {
			return _index;
		}

		/**
		 * The current iteration index (one-based).
		 */
		@TemplateVariable("count")
		public int getCount() {
			return _index + 1;
		}

		/**
		 * The current value.
		 */
		@TemplateVariable("current")
		public Object getCurrent() {
			return _current;
		}

		/**
		 * Whether the current iteration index is even.
		 */
		@TemplateVariable("even")
		public boolean isEven() {
			return getCount() % 2 == 0;
		}

		/**
		 * Whether the current iteration index is even.
		 */
		@TemplateVariable("odd")
		public boolean isOdd() {
			return getCount() % 2 == 1;
		}

		/**
		 * Whether the current iteration index is zero.
		 */
		@TemplateVariable("first")
		public boolean isFirst() {
			return _index == 0;
		}

		/**
		 * Whether the current iteration index is the last one.
		 */
		@TemplateVariable("last")
		public boolean isLast() {
			return _index == _size - 1;
		}
	}

	private Collection<?> asCollection(Object obj) {
		return obj == null ? Collections.emptyList()
			: obj instanceof Collection ? (Collection<?>) obj : Collections.singletonList(obj);
	}

	@Override
	public <R, A> R visit(Visitor<R, A> visitor, A arg) {
		return visitor.visit(this, arg);
	}

}
