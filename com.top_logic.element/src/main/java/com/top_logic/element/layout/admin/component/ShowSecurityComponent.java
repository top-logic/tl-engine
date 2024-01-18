/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.admin.component;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.top_logic.base.services.simpleajax.AJAXCommandHandler;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.DebugHelper;
import com.top_logic.basic.Logger;
import com.top_logic.basic.col.FilterUtil;
import com.top_logic.basic.col.LazyListUnmodifyable;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.util.StopWatch;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOClassImpl;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.gui.WrapperResourceProvider;
import com.top_logic.knowledge.gui.layout.person.PersonResourceProvider;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LabelComparator;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.constraints.CollectionSizeConstraint;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.TableField;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.model.ExportConfig;
import com.top_logic.layout.table.model.ObjectTableModel;
import com.top_logic.layout.table.model.TableConfiguration;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.securityObjectProvider.SecurityRootObjectProvider;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.FormFieldHelper;
import com.top_logic.util.model.ModelService;

/**
 * Shows security information for the selected elements.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class ShowSecurityComponent extends FormComponent {

	/**
	 * Configuration of the {@link ShowSecurityComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends FormComponent.Config, ExportConfig {
		
		/** Configuration name for {@link #getTableName()}. */
		public static final String TABLE_NAME = "tableName";

		/**
		 * Name of the table that is displayed separate.
		 */
		@Mandatory
		@Name(TABLE_NAME)
		String getTableName();

		@Override
		@BooleanDefault(false)
		Boolean getUseChangeHandling();

	}

	/** Input field for a person. */
    public static final String FIELD_PERSONS = "field_persons";
	/** Input field for some business object types. */
    public static final String FIELD_TYPES = "field_types";
	/** Input field for some business objects. */
    public static final String FIELD_OBJECTS = "field_objects";
	/** Input field for some roles. */
    public static final String FIELD_ROLES = "field_roles";
	/** Input field for displaying raw data. */
    public static final String FIELD_SIMPLE = "field_simple";

    /** Output field for some component messages. */
    public static final String FIELD_MESSAGE = "field_message";
	/** Output field for the search result. */
    public static final String FIELD_RESULT = "field_result";

    /**
	 * Creates a new {@link ShowSecurityComponent}.
	 */
	public ShowSecurityComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	public Config getConfig() {
		return (Config) super.getConfig();
	}

	@Override
	public boolean validateModel(DisplayContext context) {
		if (getModel() == null) {
			setModel(securityRoot());
		}
		return super.validateModel(context);
	}

	private BoundObject securityRoot() {
		return SecurityRootObjectProvider.INSTANCE.getSecurityRoot();
	}

    @Override
	protected boolean supportsInternalModel(Object anObject) {
		return anObject == null || anObject == securityRoot();
    }

    @Override
    protected boolean receiveModelCreatedEvent(Object aModel, Object changedBy) {
        if (!hasFormContext()) return false;
        FormContext context = getFormContext();
        boolean result = false;

        SelectField typesField = (SelectField)context.getField(FIELD_TYPES);
        if (aModel instanceof Person) {
            result |= addOption((SelectField)context.getField(FIELD_PERSONS), aModel);
        }
        if (aModel instanceof MetaObject) {
            if ((!(aModel instanceof MOClassImpl) || !((MOClassImpl)aModel).isAbstract())) {
                result |= addOption(typesField, aModel);
            }
        }
        if (aModel instanceof TLClass) {
            result |= addOption(typesField, aModel);
        }
        if (aModel instanceof BoundedRole) {
            result |= addOption((SelectField)context.getField(FIELD_ROLES), aModel);
        }

        Object type = FormFieldHelper.getSingleProperValue(typesField);
        if (type instanceof MetaObject && aModel instanceof Wrapper) {
            if (isAssignable(((Wrapper)aModel).tTable(), (MetaObject)type)) {
                result |= addOption((SelectField)context.getField(FIELD_OBJECTS), aModel);
            }
        }
        else if (type instanceof TLClass && aModel instanceof Wrapper) {
			if (isAssignable(((Wrapper) aModel).tType(), (TLClass) type)) {
                result |= addOption((SelectField)context.getField(FIELD_OBJECTS), aModel);
            }
        }

        return result;
    }

	@Override
	protected boolean observeAllTypes() {
		return true;
	}

    @Override
    protected boolean receiveModelDeletedEvent(Set<TLObject> models, Object changedBy) {
        if (!hasFormContext()) return super.receiveModelDeletedEvent(models, changedBy);
        FormContext context = getFormContext();
        boolean result = false;

        SelectField typesField = (SelectField)context.getField(FIELD_TYPES);
		SelectField personField = (SelectField) context.getField(FIELD_PERSONS);
		SelectField rolesField = (SelectField) context.getField(FIELD_ROLES);
		SelectField objectsField = (SelectField) context.getField(FIELD_OBJECTS);
		for (TLObject model : models) {
			if (model instanceof Person) {
				result |= removeOption(personField, model);
			}
			if (model instanceof MetaObject || model instanceof TLClass) {
				result |= removeOption(typesField, model);
			}
			if (model instanceof BoundedRole) {
				result |= removeOption(rolesField, model);
			}
			result |= removeOption(objectsField, model);
		}

        return super.receiveModelDeletedEvent(models, changedBy) || result;
    }

    @Override
	public FormContext createFormContext() {
        FormContext theContext = new FormContext(this);

        theContext.addMember(this.createPersonSelectField());
        theContext.addMember(this.createTypeSelectField());
        theContext.addMember(this.createBusinessObjectSelectField());
        theContext.addMember(this.createRoleSelectField());
        theContext.addMember(FormFactory.newBooleanField(FIELD_SIMPLE));
        theContext.addMember(FormFactory.newStringField(FIELD_MESSAGE, IMMUTABLE));
		theContext.addMember(this.createResultTable());

        return theContext;
    }

	/**
	 * Create the {@link TableField} for the {@link Config#getTableName()}.
	 * 
	 * This table will have no rows, they will be injected later on.
	 */
	protected FormMember createResultTable() {
		String fieldName = getConfig().getTableName();
		TableField table = FormFactory.newTableField(fieldName);
		TableConfigurationProvider customConfig = lookupTableConfigurationBuilder(fieldName);

		TableConfiguration tableConfig = TableConfigurationFactory.build(customConfig);
		List<String> columnNames = tableConfig.getDefaultColumns();
		ObjectTableModel objectTableModel = new ObjectTableModel(columnNames, tableConfig, Collections.emptyList());
		table.getTableData().setTableModel(objectTableModel);

		return table;
	}

	/** 
	 * Create the select field for the {@link #FIELD_ROLES}.
	 * 
	 * @see #createPossibleRolesList()
	 */
	protected SelectField createRoleSelectField() {
		SelectField theField = FormFactory.newSelectField(FIELD_ROLES, this.createPossibleRolesList(), MULTI_SELECT, !IMMUTABLE);

        theField.setOptionLabelProvider(WrapperResourceProvider.INSTANCE);
        theField.setOptionComparator(LabelComparator.newCachingInstance(WrapperResourceProvider.INSTANCE));

        return theField;
	}

	/** 
	 * Create the select field for the {@link #FIELD_OBJECTS}.
	 * 
	 * This field will have no values, they will be injected later on.
	 */
	protected SelectField createBusinessObjectSelectField() {
		SelectField theField = FormFactory.newSelectField(FIELD_OBJECTS, Collections.EMPTY_LIST, MULTI_SELECT, !IMMUTABLE);

		// constraint is necessary because DB query will fail if it is too long
        theField.addConstraint(new CollectionSizeConstraint(-1, 128));
        theField.setOptionComparator(LabelComparator.newCachingInstance());

        return theField;
	}

	/** 
	 * Create the select field for the {@link #FIELD_TYPES}.
	 * 
	 * @see #createPossibleTypeList()
	 */
	protected SelectField createTypeSelectField() {
		SelectField   theField    = FormFactory.newSelectField(FIELD_TYPES, this.createPossibleTypeList(), !MULTI_SELECT, !IMMUTABLE);
		LabelProvider theProvider = this.createTypeFieldLabelProvider();

		theField.setOptionLabelProvider(theProvider);

		final LabelComparator labelComparator = LabelComparator.newCachingInstance();
		theField.setOptionComparator(new Comparator<>() {
            @Override
            public int compare(Object value1, Object value2) {
                boolean v1MO = value1 instanceof MetaObject;
                boolean v2MO = value2 instanceof MetaObject;
				if (v1MO && v2MO) {
					return labelComparator.compare(value1, value2);
				}
                if (v1MO) return -1;
                if (v2MO) return 1;
				return labelComparator.compare(value1, value2);
            }
        });
        theField.addValueListener(new ValueListener() {
    		@Override
    		public void valueChanged(FormField aField, Object aOldValue, Object aNewValue) {
    			updateObjectsField(aField.getParent(), CollectionUtil.getFirst(aNewValue));
    		}
        });

        return theField;
	}

	/** 
	 * Create a label provider for the type field.
	 * 
	 * @return    The requested label provider.
	 */
	protected LabelProvider createTypeFieldLabelProvider() {
		return new LabelProvider() {
	        @Override
			public String getLabel(Object anObject) {
	            if (anObject instanceof MetaObject) { 
	            	return ((MetaObject) anObject).getName();
	            }
				else if (anObject instanceof TLClass) {
					return "ME: " + ((TLClass) anObject).getName();
				}
				else {
					return anObject.toString();
				}
	        }
	    };
	}

	/** 
	 * Create the select field for the {@link #FIELD_PERSONS}.
	 * 
	 * @see #createPossiblePersonList()
	 */
	protected SelectField createPersonSelectField() {
		SelectField            theField    = FormFactory.newSelectField(FIELD_PERSONS, this.createPossiblePersonList(), !MULTI_SELECT, !IMMUTABLE);
		PersonResourceProvider theProvider = TypedConfigUtil.createInstance(PersonResourceProvider.createConfig(true));

        theField.setOptionLabelProvider(theProvider);
		theField.setOptionComparator(LabelComparator.newCachingInstance(theProvider));

        return theField;
	}

	/** 
	 * Create the lazy list of possible persons.
	 * 
	 * @return    The list of possible persons, never <code>null</code>.
	 */
	protected List<Person> createPossiblePersonList() {
		return new LazyListUnmodifyable<>() {

			@Override
			protected List<? extends Person> initInstance() {
				return PersonManager.getManager().getAllAlivePersons();
			}
		};
	}

	/** 
	 * Create the lazy list of possible roles.
	 * 
	 * @return    The list of possible roles, never <code>null</code>.
	 */
	protected List<BoundedRole> createPossibleRolesList() {
		return new LazyListUnmodifyable<>() {

			@Override
			protected List<? extends BoundedRole> initInstance() {
				return CollectionUtil.dynamicCastView(BoundedRole.class, BoundedRole.getAll());
			}
		};
	}

	/** 
	 * Create the lazy list of possible business object types.
	 * 
	 * @return    The list of possible meta elements and meta objects, never <code>null</code>.
	 */
	protected List<Object> createPossibleTypeList() {
		return new LazyListUnmodifyable<>() {

			@Override
			protected List<? extends Object> initInstance() {
				Collection<TLClass> allGlobalClasses = TLModelUtil.getAllGlobalClasses(ModelService.getApplicationModel());
				return FilterUtil.filterList(TLModelUtil.IS_CONCRETE, allGlobalClasses);
			}
		};
	}

	/** 
	 * Create the lazy list of possible business objects.
	 * 
	 * @param    aType    The requested type of objects (instance of {@link TLClass} or {@link MetaObject}).
	 * @return   The lazy list of possible business objects, never <code>null</code>.
	 */
	protected List<Wrapper> createPossibleBusinessObjectsList(final Object aType) {
		return new LazyListUnmodifyable<>() {

			@Override
			protected List<? extends Wrapper> initInstance() {
				if (aType instanceof TLClass) {
					return CollectionUtil.toList(MetaElementUtil.getAllInstancesOf((TLClass) aType, Wrapper.class));
				} else {
					return Collections.emptyList();
				}
			}
		};
	}

	/** 
	 * Convert the given list to a modifiable one.
	 */
	protected List<Object> getFieldList(List<?> aList) {
		return CollectionUtil.<Object> modifiableList(aList);
	}

	/** 
	 * Update the business object field contained in the given form container.
	 * 
	 * @param    aContainer    The container to get the field from, must not be <code>null</code>.
	 * @param    aType         The requested type of objects (instance of {@link TLClass} or {@link MetaObject}).
	 */
	protected void updateObjectsField(FormContainer aContainer, Object aType) {
		SelectField typeField = (SelectField) aContainer.getField(FIELD_TYPES);
		SelectField theField = (SelectField) aContainer.getField(FIELD_OBJECTS);

		theField.setMandatory(typeField.hasValue() && !typeField.getSelection().isEmpty());
		theField.setValue(null);
		theField.setOptions(this.createPossibleBusinessObjectsList(aType));
	}

    /**
     * Adds the given option to the given SelectField if the field doesn't contain it.
     */
	private boolean addOption(SelectField aField, Object aValue) {
		List<Object> theOptions = this.getFieldList(aField.getOptions());

		if (!theOptions.contains(aValue)) {
			Object theValue = aField.getValue();

			theOptions.add(aValue);
			aField.setOptions(theOptions);
            // required because setting options will clear selection
			aField.setValue(theValue);

			return true;
		}
		else {
			return false;
        }
    }

    /**
     * Removes the given option from the given SelectField if the field contains it.
     */
	private boolean removeOption(SelectField aField, Object aValue) {
        boolean      theResult    = false;
		List<Object> theOptions   = this.getFieldList(aField.getOptions());
		List<Object> theSelection = this.getFieldList(aField.getSelection());

		if (theOptions.contains(aValue)) {
			theOptions.remove(aValue);
			aField.setOptions(theOptions);
            theResult = true;
        }
		if (theSelection.contains(aValue)) {
			theSelection.remove(aValue);
            theResult = true;
        }
        if (theResult) {
			aField.setAsSelection(theSelection);
        }
        return theResult;
    }

	private boolean isAssignable(TLStructuredType aConcreteType, TLStructuredType aSuperType) {
        return aConcreteType != null && aSuperType != null && MetaElementUtil.isSuperType(aSuperType, aConcreteType);
    }

    private boolean isAssignable(MetaObject aConcreteType, MetaObject aSuperType) {
        return aConcreteType != null && aSuperType != null && aConcreteType.isSubtypeOf(aSuperType);
    }

    /**
     * Calculates the result table.
     * 
     * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
     */
	public static class RefreshCommand extends AJAXCommandHandler {

		/**
		 * Configuration of the {@link RefreshCommand}.
		 * 
		 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
		 */
		public interface Config extends AJAXCommandHandler.Config {

			/** Configuration name for {@link #getTableName()}. */
			public static final String TABLE_NAME = "tableName";

			/**
			 * Name of the table that is displayed separate.
			 */
			@Mandatory
			@Name(TABLE_NAME)
			String getTableName();

		}

		/**
		 * Creates a {@link RefreshCommand}.
		 */
		public RefreshCommand(InstantiationContext context, Config config) {
			super(context, config);
		}

		private Config config() {
			return (Config) getConfig();
		}

        @Override
		public HandlerResult handleCommand(DisplayContext aCommandContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
            FormContext   theContext = ((FormComponent) aComponent).getFormContext();
            HandlerResult theResult  = new HandlerResult();

            if (theContext.checkAll()) {
                try {
					this.refresh(theContext, aComponent);
                }
                catch (Exception e) {
                    Logger.error("Failed to compute roles.", e, RefreshCommand.class);
					theResult.addErrorText(aComponent.getResString("failedUpdate") + ": " + e.getMessage());
                }
            }
            else {
				AbstractApplyCommandHandler.fillHandlerResultWithErrors(theContext, theResult);
            }

            return theResult;
        }

        // Public methods

        /** 
         * Extract the user input from the context, query for the security rows and
         * inject them to the result field.
         * 
         * @param    aContext    The context to get the values from.
         */
        public void refresh(FormContext aContext, LayoutComponent component) {
        	StopWatch         theWatch   = StopWatch.createStartedWatch();
			List<Object[]> theRawData = this.computeRoles(aContext);
            List<SecurityRow> theResult  = this.convertSearchResult(theRawData, aContext);
			TableField theTable = (TableField) aContext.getField(config().getTableName());

			((ObjectTableModel) theTable.getTableModel()).setRowObjects(theResult);

            int theSize = theResult.size();

			aContext.getField(FIELD_MESSAGE).setValue(
				component.getResMessage(theSize == 1 ? "resultMessage1" : "resultMessage", Integer.valueOf(theSize),
					DebugHelper.getTime(theWatch.getElapsedMillis())));
        }

        // Protected methods

        /** 
    	 * Convert the given raw data to objects understood by the UI.
    	 */
    	protected List<SecurityRow> convertSearchResult(List<Object[]> someRawData, FormContext aContext) {
    		boolean doSimple = FormFieldHelper.getbooleanValue(aContext.getField(ShowSecurityComponent.FIELD_SIMPLE));

    		return new ShowSecurityResultConverter().convert(someRawData, doSimple);
    	}

        /** 
         * Calculate the raw data by inspecting the form context.
         */
        protected List<Object[]> computeRoles(FormContext aContext) {
            Person            thePerson  = (Person) FormFieldHelper.getSingleProperValue(aContext.getField(FIELD_PERSONS));
            Object            theType    = FormFieldHelper.getSingleProperValue(aContext.getField(FIELD_TYPES));
            List<Wrapper>     theObjects = this.getListValueFromContext(aContext, FIELD_OBJECTS);
            List<BoundedRole> theRoles   = this.getListValueFromContext(aContext, FIELD_ROLES);

            return new ShowSecurityQuery().query(thePerson, theType, theObjects, theRoles);
        }

        // Private methods

    	private <T> List<T> getListValueFromContext(FormContext aContext, String aFieldName) {
        	FormField theField = aContext.getField(aFieldName);
        	Object    theValue = theField.getValue();

			return (theValue instanceof Collection) ? CollectionUtil.toList((Collection) theValue) : Collections
				.<T> emptyList();
        }
    }


    /**
	 * {@link ExecutabilityRule} computation for the export of the {@link ShowSecurityComponent}.
	 */
	public static class ShowSecurityExecutability implements Function<TableData, ExecutableState> {

		private static final int MAXIMUM_EXPORT_ROWS = 65534;

		@Override
		public ExecutableState apply(TableData t) {
			if (t.getTableModel().getRowCount() > MAXIMUM_EXPORT_ROWS) {
				return ExecutableState.createDisabledState(I18NConstants.EXPORT_ERROR_TOO_MANY_ROWS);
			}
			return ExecutableState.EXECUTABLE;
		}

	}

}
