/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.demo;

import java.util.Date;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.component.model.ModelProvider;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.component.EditComponent;
import com.top_logic.layout.form.component.Editor;
import com.top_logic.layout.form.constraints.ComparisonDependency;
import com.top_logic.layout.form.constraints.IntRangeConstraint;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandlerFactory;

/**
 * {@link LayoutComponent} testing basic AJAX submissions.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestAJAXEditComponent extends EditComponent implements ModelProvider {

	private static final String YYYY_MM_DD_DATE_FORMAT_STRING = "yyyy-MM-dd";

	private static final String SEARCH_NAME = "search";

	private static final String DATE_NAME = "date";
	private static final String BEGIN_NAME = "begin";
	private static final String END_NAME = "end";

	private static final String DAYS_NAME = "days";
	private static final String MIN_NAME = "min";
	private static final String MAX_NAME = "max";

	/**
	 * Configuration options for {@link TestAJAXEditComponent}.
	 */
	public interface Config extends EditComponent.Config {
		@Override
		@NullDefault
		String getLockOperation();

		@Override
		@StringDefault(ApplyCommand.ID)
		String getApplyCommand();

		@Override
		@StringDefault(SaveCommand.ID)
		String getSaveCommand();

	}

	public TestAJAXEditComponent(InstantiationContext context, Config someAttrs) throws ConfigurationException {
		super(context, someAttrs);
	}

	@Override
	public FormContext createFormContext() {
		FormField begin, end, min, max;
		
		ResPrefix resPrefix = ResPrefix.legacyClass(this.getClass());
		FormContext formContext = 
			new FormContext(SEARCH_NAME, resPrefix, new FormMember[] {
				new FormGroup(DATE_NAME, resPrefix, new FormMember[] {
					begin = FormFactory.newComplexField(BEGIN_NAME,
						CalendarUtil.newSimpleDateFormat(YYYY_MM_DD_DATE_FORMAT_STRING)),
					end = FormFactory.newComplexField(END_NAME,
						CalendarUtil.newSimpleDateFormat(YYYY_MM_DD_DATE_FORMAT_STRING))
				}),
				
				new FormGroup(DAYS_NAME, resPrefix, new FormMember[] {
					min = FormFactory.newIntField(MIN_NAME, Integer.valueOf(7), false, false, new IntRangeConstraint(1, 90)),
					max = FormFactory.newIntField(MAX_NAME, Integer.valueOf(14), false, false, new IntRangeConstraint(1, 90))
				})
			});
		
		// Add constraints that enforce a non-empty range
		begin.addConstraint(new ComparisonDependency(ComparisonDependency.LOWER_TYPE, end));
		end.addConstraint(new ComparisonDependency(ComparisonDependency.GREATER_TYPE, begin));
		
		// Add constraints that enforce a (possibly empty) range
		min.addConstraint(new ComparisonDependency(ComparisonDependency.LOWER_OR_EQUALS_TYPE, max));
		max.addConstraint(new ComparisonDependency(ComparisonDependency.GREATER_OR_EQUALS_TYPE, min));
		
		loadModel(formContext, (SearchModel) getModel());
		
		return formContext;
	}

	@Override
	public Object getBusinessModel(LayoutComponent businessComponent) {
		return new SearchModel();
	}

	@Override
	protected boolean supportsInternalModel(Object anObject) {
		return anObject instanceof SearchModel;
	}

	/**
	 * Demo model class, where inputs from the form are stored.
	 */
	static class SearchModel {
		Date beginDate;
		Date endDate;
		Integer maxDays;
		Integer minDays;
	}

	/**
	 * "Configuration" frontend for {@link TestAJAXEditComponent.StoreCommand}
	 * that makes it an "apply command".
	 * 
	 * Is configured in top-logic.local.xml
	 */
	public static class ApplyCommand extends StoreCommand {
		private static final String ID = "TestAJAXEditComponent_Apply";

		public ApplyCommand(InstantiationContext context, Config config) {
			super(context, config);
		}
	}

	/**
	 * "Configuration" frontend for {@link TestAJAXEditComponent.StoreCommand}
	 * that makes it an "save command".
	 * 
	 * Is configured in top-logic.local.xml
	 */
	public static class SaveCommand extends StoreCommand {

		private static final String ID = "TestAJAXEditComponent_Save";

		public SaveCommand(InstantiationContext context, Config config) {
			super(context, config);
		}

	    @Override
		protected void updateComponent(LayoutComponent component, FormContext formContext, Object model) {
			super.updateComponent(component, formContext, model);

			((Editor) component).setEditMode(false);
	    }
	}
	
	/**
	 * Note: This class must be public, because it is created by
	 * {@link CommandHandlerFactory} using Java reflection.
	 */
	public abstract static class StoreCommand extends AbstractApplyCommandHandler {
		
		public StoreCommand(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		protected boolean storeChanges(LayoutComponent component, FormContext formContext, Object model) {
			SearchModel searchModel = (SearchModel) model;
			
			FormContainer dateContainer = formContext.getContainer(DATE_NAME);
			searchModel.beginDate = (Date) dateContainer.getField(BEGIN_NAME).getValue();
			searchModel.endDate = (Date) dateContainer.getField(END_NAME).getValue();
			
			FormContainer daysContainer = formContext.getContainer(DAYS_NAME);
			searchModel.minDays = (Integer) daysContainer.getField(MIN_NAME).getValue();
			searchModel.maxDays = (Integer) daysContainer.getField(MAX_NAME).getValue();

			return true;
		}
	}

	protected static void loadModel(FormContext context, SearchModel model) {
		FormContainer dateContainer = context.getContainer(DATE_NAME);
		dateContainer.getField(BEGIN_NAME).setValue(model.beginDate);
		dateContainer.getField(END_NAME).setValue(model.endDate);
		
		FormContainer daysContainer = context.getContainer(DAYS_NAME);
		daysContainer.getField(MIN_NAME).setValue(model.minDays);
		daysContainer.getField(MAX_NAME).setValue(model.maxDays);
	}

}
