/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.table.filter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.table.filter.ConfiguredFilter;
import com.top_logic.layout.table.filter.FilterConfiguration;
import com.top_logic.layout.table.filter.FilterViewControl;

/**
 * Dummy for {@link TestCase}s
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public final class DummyConfiguredFilter implements ConfiguredFilter {
	
	DummyConfiguredFilter() {}
	
	@Override
	public boolean accept(Object anObject) {
		throw new UnsupportedOperationException("Method is not implemented");
	}

	@Override
	public void setFilterConfiguration(FilterConfiguration filterConfig) {
		throw new UnsupportedOperationException("Method is not implemented");
	}

	@Override
	public boolean isActive() {
		throw new UnsupportedOperationException("Method is not implemented");
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	public List<Class<?>> getSupportedObjectTypes() {
		return Collections.<Class<?>> singletonList(Object.class);
	}

	@Override
	public FilterConfiguration getFilterConfiguration() {
		throw new UnsupportedOperationException("Method is not implemented");
	}

	@Override
	public FilterViewControl<?> getDisplayControl(DisplayContext context, FormGroup form, int filterControlId) {
		return new FilterViewControl<>(null, form) {

			@Override
			protected boolean internalApplyFilterSettings() {
				// TODO STS Automatically created
				throw new UnsupportedOperationException();

			}

			@Override
			public void resetFilterSettings() {
				// TODO STS Automatically created
				if (true)
					throw new UnsupportedOperationException();

			}

			@Override
			protected void internalValueChanged() {
				// TODO STS Automatically created
				if (true)
					throw new UnsupportedOperationException();

			}

			@Override
			protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
				// Do nothing
			}
		};
	}

	@Override
	public void clearFilterConfiguration() {
		throw new UnsupportedOperationException("Method is not implemented");
	}

	@Override
	public void startFilterRevalidation(boolean countableRevalidation) {
		throw new UnsupportedOperationException();

	}

	@Override
	public void stopFilterRevalidation() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void count(Object value) {
		throw new UnsupportedOperationException();
	}
}