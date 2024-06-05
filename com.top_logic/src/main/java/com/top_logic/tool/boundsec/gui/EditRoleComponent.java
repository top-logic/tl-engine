/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.gui;

import java.util.Map;
import java.util.regex.Pattern;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.component.AbstractCreateCommandHandler;
import com.top_logic.layout.form.component.AbstractCreateComponent;
import com.top_logic.layout.form.component.AbstractDeleteCommandHandler;
import com.top_logic.layout.form.component.EditComponent;
import com.top_logic.layout.form.constraints.RegExpConstraint;
import com.top_logic.layout.form.constraints.StringLengthConstraint;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.HiddenField;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.annotate.security.RoleConfig;
import com.top_logic.tool.boundsec.BoundHelper;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.FormBinding;
import com.top_logic.util.FormFieldHelper;
import com.top_logic.util.error.TopLogicException;

/**
 * Component to edit, create and delete Bound(ed)Roles.
 *
 * @author <a href="mailto:kha@top-logic.com">kha</a>
 */
public class EditRoleComponent extends EditComponent {

    public static final String FIELD_NAME = BoundedRole.NAME_ATTRIBUTE;
    public static final String FIELD_DESC = BoundedRole.ATTRIBUTE_DESCRIPTION;
    public static final String FIELD_SYSTEM = BoundedRole.ROLE_SYSTEM;
    public static final String FIELD_MASTER = "roleMaster";

	/**
	 * Configuration of the {@link EditRoleComponent}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends EditComponent.Config {

		@Override
		@StringDefault(ApplyRoleCommand.COMMAND_ID)
		String getApplyCommand();

		@Override
		@StringDefault(DeleteRoleCommand.COMMAND_ID)
		String getDeleteCommand();

	}

	/**
	 * Creates a new instance of this class.
	 */
    public EditRoleComponent(InstantiationContext context, Config atts) throws ConfigurationException {
        super(context, atts);
    }

    @Override
    public FormContext createFormContext() {
        FormContext context = new FormContext(this);
        BoundedRole role = (BoundedRole)getModel();
        if (role != null) {
			MOClass roleType = (MOClass) role.tTable();

            FormFieldHelper fieldHelper = new FormFieldHelper(getResPrefix());

			{
				StringField field =
					fieldHelper.createStringField(FIELD_NAME, role.getName(), null, context, false, true);
				FormBinding.addSizeConstraint(field, roleType, BoundedRole.NAME_ATTRIBUTE);
				TLModule scope = getRoleScope();
				if (scope != null) {
					field.addConstraint(new RegExpConstraint(
						Pattern.quote(scope.getName() + ".") + RoleConfig.ROLE_NAME_PATTERN));
				}
			}

			{
				StringField field =
					fieldHelper.createStringField(FIELD_DESC, role.getValue(BoundedRole.ATTRIBUTE_DESCRIPTION), null,
						context, false, false);
				FormBinding.addSizeConstraint(field, roleType, BoundedRole.ATTRIBUTE_DESCRIPTION);
			}

            fieldHelper.createBooleanField(FIELD_SYSTEM, role.isSystem(), null, context, !BoundHelper.getInstance().isAllowChangeDeleteProtection(), false);
        }
        return context;
    }

    @Override
	protected boolean supportsInternalModel(Object anObject) {
        return anObject instanceof BoundedRole;
    }

	/**
	 * The {@link TLModule} defining the edited roles.
	 */
	protected TLModule getRoleScope() {
        return null;
    }

    /**
     * Apply command handler for Bounded roles.
     */
	public static class ApplyRoleCommand extends AbstractApplyCommandHandler {

        public static final String COMMAND_ID = "applyRole";

        public ApplyRoleCommand(InstantiationContext context, Config config) {
			super(context, config);
        }

        @Override
		protected boolean storeChanges(LayoutComponent component, FormContext formContext, Object model) {
            BoundedRole role = (BoundedRole)model;
            FormField field = FormFieldHelper.getField(formContext.getMember(FIELD_NAME));
            if (field.isChanged()) {
                role.setName((String)field.getValue());
            }
            field = FormFieldHelper.getField(formContext.getMember(FIELD_DESC));
            if (field.isChanged()) {
                role.setValue(BoundedRole.ATTRIBUTE_DESCRIPTION, field.getValue());
            }
            field = FormFieldHelper.getField(formContext.getMember(FIELD_SYSTEM));
            if (field.isChanged()) {
                role.setIsSystem(FormFieldHelper.getbooleanValue(field));
            }
            return true;
        }

        @Override
		@Deprecated
        public ExecutabilityRule createExecutabilityRule() {
            return super.createExecutabilityRule();
//            return new CombinedExecutabilityRule(super.getExecutabilityRule(), new ExecutabilityRule() {
//                public ExecutableState isExecutable(LayoutComponent aComponent, Map aSomeValues) {
//                    BoundedRole theRole = (BoundedRole) aComponent.getModel();
//                    if (theRole.isSystem()) {
//                        return ExecutableState.NOT_EXEC_DISABLED;
////                        return ExecutableState.createDisabledState("admin.role.edit.deleteRole.disabled.isSystem");
//                    }
//                    return ExecutableState.EXECUTABLE;
//                }
//            });
        }

    }



    /**
     * Handler to create new roles.
     */
    public static class NewRoleCommand extends AbstractCreateCommandHandler {

        /** The ID of this command. */
        public static final String COMMAND_ID = "newRole";

        public NewRoleCommand(InstantiationContext context, Config config) {
            super(context, config);
        }

        @Override
		public Object createObject(LayoutComponent component, Object createContext, FormContainer formContainer,
				Map<String, Object> arguments) {
            String name = FormFieldHelper.getStringValue(formContainer.getField(FIELD_NAME));
			TLModule master = (TLModule) FormFieldHelper.getProperValue(formContainer.getField(FIELD_MASTER));
            return createRole(master, name, false);
        }


        /**
		 * Creates a new role.
		 *
		 * @param scope
		 *        The scope to bind the created role to, may be <code>null</code>.
		 * @param aName
		 *        The name of the new role, must not be empty or <code>null</code>.
		 * @param isSystem
		 *        Flag, if the role is a system role.
		 * @return The newly created role.
		 * @throws IllegalArgumentException
		 *         If given name is empty or <code>null</code>.
		 */
		public static BoundedRole createRole(TLModule scope, String aName, boolean isSystem) {
            if (StringServices.isEmpty(aName)) {
                throw new IllegalArgumentException("No name defined for new role!");
            }
			String qualifiedName;
			if (scope != null) {
				String structureName = scope.getName();
				qualifiedName = structureName + "." + aName;
			} else {
				qualifiedName = aName;
			}

			BoundedRole theRole = BoundedRole.createBoundedRole(qualifiedName);
            theRole.setIsSystem(isSystem);
			if (scope != null) {
				theRole.bind(scope);
            }
            return theRole;
        }
    }



    /**
     * DeleteHandler for roles.
     */
    public static class DeleteRoleCommand extends AbstractDeleteCommandHandler {

        public static final String COMMAND_ID = "deleteRole";

        public DeleteRoleCommand(InstantiationContext context, Config config) {
			super(context, config);
        }

        @Override
        protected void deleteObject(LayoutComponent component, Object model, Map<String, Object> arguments) {
			checkRole(model);
			((BoundedRole) model).tDelete();
		}

		@Override
		protected void deleteObjects(LayoutComponent component, Iterable<?> elements, Map<String, Object> arguments) {
			elements.forEach(this::checkRole);
			@SuppressWarnings("unchecked")
			Iterable<? extends TLObject> roles = (Iterable<? extends TLObject>) elements;
			KBUtils.deleteAll(roles);
		}

		private void checkRole(Object model) {
			if (!(model instanceof BoundedRole)) {
				throw new TopLogicException(I18NConstants.ERROR_NOT_A_ROLE__ELEMENT.fill(model));
			}
		}

        @Override
		@Deprecated
        public ExecutabilityRule createExecutabilityRule() {
            return CombinedExecutabilityRule.combine(super.createExecutabilityRule(), new ExecutabilityRule() {
                @Override
				public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> aSomeValues) {
					BoundedRole theRole = (BoundedRole) model;
					if (theRole != null && theRole.isSystem()) {
                        return ExecutableState.createDisabledState(I18NConstants.ERROR_SYSTEM_ROLE_CANNOT_BE_DETETED);
                    }
                    return ExecutableState.EXECUTABLE;
                }
            });
        }
    }



    /**
     * Component to create new roles.
     */
    public static class NewRoleComponent extends AbstractCreateComponent {

		/**
		 * Configuration for the {@link NewRoleComponent}.
		 * 
		 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
		 */
		public interface Config extends AbstractCreateComponent.Config {

			@StringDefault(NewRoleCommand.COMMAND_ID)
			@Override
			String getCreateHandler();

		}

        /**
         * Creates a new instance of this class.
         */
        public NewRoleComponent(InstantiationContext context, Config someAttrs) throws ConfigurationException {
            super(context, someAttrs);
        }

        @Override
        public FormContext createFormContext() {
            FormContext context = new FormContext(this);
            FormFieldHelper fieldHelper = new FormFieldHelper(getResPrefix());
            StringField field = fieldHelper.createStringField(FIELD_NAME, null, null, context, false, true);
            field.addConstraint(new StringLengthConstraint(1, 128));
			field.addConstraint(new RegExpConstraint(RoleConfig.ROLE_NAME_PATTERN));

			TLModule scope = null;
            LayoutComponent parent = this.getDialogParent();
            if (parent instanceof EditRoleComponent) {
				scope = ((EditRoleComponent) parent).getRoleScope();
            }

            HiddenField hiddenField = FormFactory.newHiddenField(FIELD_MASTER);
			hiddenField.initializeField(scope);
            context.addMember(hiddenField);

            return context;
        }
    }

}
