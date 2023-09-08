/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.datasource;


import static com.top_logic.layout.form.template.model.Templates.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import com.top_logic.basic.DateUtil;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperHistoryUtils;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.model.ComplexField;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.reporting.flex.chart.config.gui.ChartContextObserver;
import com.top_logic.reporting.flex.chart.config.gui.InteractiveBuilder;
import com.top_logic.reporting.flex.chart.config.gui.InteractiveBuilderUtil;

/**
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
public class InteractiveRevisonTransformer implements InteractiveBuilder<Transformer, ChartContextObserver>,
		Transformer, ConfiguredInstance<InteractiveRevisonTransformer.Config> {

	/**
	 * Config-interface for {@link InteractiveRevisonTransformer}.
	 */
	public interface Config extends PolymorphicConfiguration<InteractiveRevisonTransformer> {

		/**
		 * <code>REVISION</code> Attribute name for revision-property
		 */
		String REVISION = "revision";

		/**
		 * the configured revision to transform the input into
		 */
		@InstanceFormat
		@Name(REVISION)
		Revision getRevision();

		/**
		 * see {@link #getRevision()}
		 */
		void setRevision(Revision rev);

	}

	private static final String DATE = "date";

	/**
	 * Constant used to annotate the currently selected {@link Revision} to
	 * the {@link ChartContextObserver}
	 */
	public static final Property<Revision> REVISION_NC = TypedAnnotatable.property(Revision.class, "revision");

	private final Config _config;

	/**
	 * Config-Constructor for {@link InteractiveRevisonTransformer}.
	 * 
	 * @param context
	 *        - default config-constructor
	 * @param config
	 *        - default config-constructor
	 */
	public InteractiveRevisonTransformer(InstantiationContext context, Config config) {
		_config = config;
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	/**
	 * shortcut to {@link Config#getRevision()}
	 */
	protected Revision getRevision() {
		return getConfig().getRevision();
	}

	/**
	 * shortcut to {@link Config#setRevision(Revision)}
	 */
	protected void setRevision(Revision rev) {
		getConfig().setRevision(rev);
	}

	@Override
	public Collection<Wrapper> transform(Collection<Wrapper> wrappers) {

		Revision revision = getRevision();
		if (revision == null || revision.isCurrent()) {
			return wrappers;
		} else {
			Collection<Wrapper> historic = new ArrayList<>(wrappers.size());

			for (Wrapper wrapper : wrappers) {
				Wrapper old = WrapperHistoryUtils.getWrapper(revision, wrapper);
				if (old != null) {
					historic.add(old);
				}
			}
			return historic;
		}
	}

	@Override
	public void createUI(FormContainer container, ChartContextObserver observer) {
		FormContainer revGroup = InteractiveBuilderUtil.addContainer(container, Config.REVISION, getConfig());

		ResPrefix resPrefix = ResPrefix.legacyString(getClass().getSimpleName());

		ComplexField dateField = FormFactory.newDateField(DATE, null, false);
		revGroup.addMember(dateField);

		template(revGroup, div(
			fieldsetBox(
				resource(I18NConstants.POINT_IN_TIME),
				fieldBox(DATE),
				ConfigKey.field(revGroup))));
	}

	@Override
	public Transformer build(FormContainer container) {
		Revision rev = getRevision(container);
		ChartContextObserver observer = ChartContextObserver.getObserver(container.getFormContext());
		updateRevision(rev, observer);
		return this;
	}

	/**
	 * Updates the given Revision in the {@link Config} and {@link ChartContextObserver}
	 */
	protected void updateRevision(Revision rev, ChartContextObserver observer) {
		setRevision(rev);
		observer.set(REVISION_NC, rev);
	}

	/**
	 * @param container
	 *        the FormContainer that has been filled in
	 *        {@link #createUI(FormContainer, ChartContextObserver)}
	 * @return the selected {@link Revision} retrieved from the container
	 */
	protected Revision getRevision(FormContainer container) {
		FormContainer revGroup = container.getContainer(Config.REVISION);
		ComplexField dateField = (ComplexField) revGroup.getMember(DATE);
		Object value = dateField.getValue();
		Revision rev = Revision.CURRENT;
		if (value instanceof Date) {
			Date date = (Date) value;
			date = DateUtil.adjustToDayEnd(date);
			rev = HistoryUtils.getRevisionAt(date.getTime());
		}
		return rev;
	}

}
