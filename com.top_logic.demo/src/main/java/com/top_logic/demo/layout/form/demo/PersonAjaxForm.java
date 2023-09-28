/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.form.demo;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.base.user.UserInterface;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.knowledge.gui.layout.person.PersonResourceProvider;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.component.EditComponent;
import com.top_logic.layout.form.constraints.NotEmptyConstraint;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.model.SelectFieldUtils;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.commandhandlers.GotoHandler;
import com.top_logic.util.TLContext;

/**
 * Allow {@link GotoHandler} from Person via this component.
 * 
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public class PersonAjaxForm extends EditComponent {

	/** Name of the filed containing the person to go to */
	public static final String PERSON_GOTO_FIELD = "personGoto";

	/** Name of the filed containing the person to go to */
	public static final String PERSON_MAILTO_FIELD = "personMailTo";

	/**
	 * Configuration options for {@link PersonAjaxForm}.
	 */
	public interface Config extends EditComponent.Config {

		@Override
		@NullDefault
		String getLockOperation();

		@BooleanDefault(false)
		@Override
		Boolean getUseChangeHandling();

	}

    /**
	 * Creates a {@link PersonAjaxForm}.
	 */
    public PersonAjaxForm(InstantiationContext context, Config aAtts) throws ConfigurationException {
        super(context, aAtts);
    }
    
    @Override
	public boolean validateModel(DisplayContext context) {
		if (getModel() == null) {
			setModel(TLContext.getContext().getCurrentPersonWrapper());
		}
		return super.validateModel(context);
    }

    /**
     * @see com.top_logic.layout.form.component.FormComponent#createFormContext()
     */
    @Override
	public FormContext createFormContext() {
		List<Person> allPerson = PersonManager.getManager().getAllAlivePersons();
		Person personModel = (Person) getModel();
        
		SelectField personGotoField = FormFactory.newSelectField(PERSON_GOTO_FIELD, allPerson);
		personGotoField.initializeField(Collections.singletonList(personModel));
		personGotoField.addConstraint(NotEmptyConstraint.INSTANCE);
        
		SelectField personMailField = FormFactory.newSelectField(PERSON_MAILTO_FIELD, allPerson);
		personMailField.initializeField(Collections.singletonList(personModel));
		personMailField.setImmutable(true);
		SelectFieldUtils.setOptionLabelProvider(personMailField, newMailToProvider());

		FormContext context = new FormContext("personAjaxForm", getResPrefix());
		context.addMember(personGotoField);
		context.addMember(personMailField);
		return context;
    }

	private PersonResourceProvider newMailToProvider() {
		PersonResourceProvider.Config config = PersonResourceProvider.createConfig();
		config.setImplementationClass(PersonMailToProvider.class);
		return TypedConfigUtil.createInstance(config);
	}

    public static class ApplyCommandHandler extends AbstractApplyCommandHandler {
        
        public static final String  COMMAND = "PersonAjaxForm_Apply";
        
        public ApplyCommandHandler(InstantiationContext context, Config config) {
			super(context, config);
        }
        
        @Override
		protected boolean storeChanges(LayoutComponent aComponent, FormContext aContext, Object aModel) {
            ((PersonAjaxForm)aComponent).setModel(
				CollectionUtil.getFirst((Collection) aContext.getField(PERSON_GOTO_FIELD).getValue()));
            return true;
        }

    }

	/**
	 * Executes an goto for the model of the component, to a given view.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class GotoPerson extends AbstractCommandHandler {

		/**
		 * Configuration for the {@link GotoPerson}.
		 * 
		 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
		 */
		public interface Config extends AbstractCommandHandler.Config {

			/** Configuration name for the value of {@link #getTargetComponentName()}. */
			String TARGET_COMPONENT = "targetComponent";

			/**
			 * Name of the component to go to.
			 */
			@Name(TARGET_COMPONENT)
			ComponentName getTargetComponentName();
		}

		/**
		 * Creates a new {@link GotoPerson}.
		 */
		public GotoPerson(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model,
				Map<String, Object> someArguments) {
			CommandHandlerFactory handlerFactory = CommandHandlerFactory.getInstance();
			GotoHandler gotoHandler = (GotoHandler) handlerFactory.getHandler(GotoHandler.COMMAND);
			return gotoHandler.executeGoto(aContext, aComponent, config().getTargetComponentName(),
				aComponent.getModel());
		}

		private Config config() {
			return (Config) getConfig();
		}


	}

	/**
	 * {@link PersonResourceProvider} creating Mail-to links.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class PersonMailToProvider extends PersonResourceProvider {

		/**
		 * Creates a new {@link PersonMailToProvider}.
		 */
		public PersonMailToProvider(InstantiationContext context, Config config) {
			super(context, config);
		}

		@Override
		public String getLink(DisplayContext context, Object anObject) {
			String mail = getMailAddress((Person) anObject);
			if (!StringServices.isEmpty(mail)) {
				return HTMLUtil.getMailToJS(mail);
			} else {
				return super.getLink(context, anObject);
			}
		}

		private String getMailAddress(Person p) {
			if (p == null) {
				return null;
			}
			UserInterface user = Person.userOrNull(p);
			return user.getEMail();
		}

	}
    
}

