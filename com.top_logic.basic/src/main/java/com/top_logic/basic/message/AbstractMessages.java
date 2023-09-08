/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.message;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import com.top_logic.basic.Logger;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.annotation.FrameworkInternal;

/**
 * Base class for definitions of internationalizable {@link Message} constants.
 * 
 * <p>
 * A messages class must have the following properties:
 * </p>
 * 
 * <ul>
 * <li>A subclass of {@link AbstractMessages}</li>
 * <li>Declared <code>public static</code></li>
 * <li>A top-level class</li>
 * <li>Contain only <code>public static</code> (non-<code>final</code>) constant
 * declarations (without initializers) of a concrete {@link Template} type.</li>
 * <li>Contain a static initializer as depicted below.</li>
 * </ul>
 * 
 * <pre>
 * public class Messages extends {@link AbstractMessages} {
 * 
 * 	public static {@link Template0} MSG_0;
 * 	public static {@link Template1} MSG_1;
 * 	public static {@link Template2} MSG_2;
 * 	public static {@link Template2} MSG_WIDTH_DEFAULT;
 * 
 * 	static {
 * 		// Create template instances.
 * 		init(Messages.class);
 * 
 * 		// Link with defaults.
 * 		MSG_WITH_DEFAULT.setDefaultTemplate(MSG_2);
 * 
 * 		// Finalize template defaults.
 * 		initDefaults(Messages.class);
 * 	}
 * }
 * 
 * </pre>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractMessages {

	/**
	 * {@link Template} for {@link Message}s without arguments.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static final class Template0 extends StaticTemplate<Template0> implements Message {
		/** Only called be reflection during Messages class initialization. */
		@FrameworkInternal
		public Template0(PackageProtected internal, Field constant) {
			super(internal, constant);
		}
		
		/**
		 * For symmetry only, sing {@link Template0} is a  {@link Message} itself.
		 */
		public Message fill() {
			return this;
		}

		@Override
		public int getParameterCount() {
			return 0;
		}

		@Override
		public Object[] getArguments() {
			return null;
		}

		@Override
		public Template getTemplate() {
			return this;
		}
	}
	
	/**
	 * {@link Template} for {@link Message}s with one argument.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static final class Template1 extends StaticTemplate<Template1> {
		/** Only called be reflection during Messages class initialization. */
		@FrameworkInternal
		public Template1(PackageProtected internal, Field constant) {
			super(internal, constant);
		}

		/**
		 * Constructs a {@link Message} from this {@link Template}
		 * 
		 * @param arg1
		 *        The first (and only) argument to the message.
		 */
		public Message fill(Object arg1) {
			return new MessageImpl(this, arg1);
		}

		@Override
		public int getParameterCount() {
			return 1;
		}
	}
	
	/**
	 * {@link Template} for {@link Message}s with two arguments.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static final class Template2 extends StaticTemplate<Template2> {
		/** Only called be reflection during Messages class initialization. */
		@FrameworkInternal
		public Template2(PackageProtected internal, Field constant) {
			super(internal, constant);
		}
		
		/**
		 * Constructs a {@link Message} from this {@link Template}
		 * 
		 * @param arg1
		 *        The first argument to the message.
		 * @param arg2
		 *        The second argument to the message.
		 */
		public Message fill(Object arg1, Object arg2) {
			return new MessageImpl(this, arg1, arg2);
		}
		
		@Override
		public int getParameterCount() {
			return 2;
		}
	}
	
	/**
	 * {@link Template} for {@link Message}s with three arguments.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static final class Template3 extends StaticTemplate<Template3> {
		/** Only called be reflection during Messages class initialization. */
		@FrameworkInternal
		public Template3(PackageProtected internal, Field constant) {
			super(internal, constant);
		}
		
		/**
		 * Constructs a {@link Message} from this {@link Template}
		 * 
		 * @param arg1
		 *        The first argument to the message.
		 * @param arg2
		 *        The second argument to the message.
		 * @param arg3
		 *        The third argument to the message.
		 */
		public Message fill(Object arg1, Object arg2, Object arg3) {
			return new MessageImpl(this, arg1, arg2, arg3);
		}
		
		@Override
		public int getParameterCount() {
			return 3;
		}
	}
	

	private static final Object MESSAGES_CLASS_NAME = "Messages";
	
	private static final int MAX_ARG_CNT = 3;

	private static final Constructor<?>[] IMPL_FACTORIES = new Constructor<?>[MAX_ARG_CNT + 1];
	static {
		String template0ClassName = Template0.class.getName();
		String templateClassPrefixName = template0ClassName.substring(0, template0ClassName.length() - 1);
		
		for (int n = 0; n <= MAX_ARG_CNT; n++) {
			String templateClassName = templateClassPrefixName + n;
			try {
				IMPL_FACTORIES[n] = Class.forName(templateClassName).getConstructor(PackageProtected.class, Field.class);
			} catch (SecurityException ex) {
				fail("No access to template class '" + templateClassName + "'.", ex);
			} catch (NoSuchMethodException ex) {
				fail("Missing constructor in template class '" + templateClassName + "'.", ex);
			} catch (ClassNotFoundException ex) {
				fail("Template class '" + templateClassName + "' does not exist.", ex);
			}
		}
	}

	protected static void init(Class<? extends AbstractMessages> messages) {
		String className = ((Class<? extends AbstractMessages>) messages).getName();
		int indexOfLastDot = className.lastIndexOf('.');

		if (! className.substring(indexOfLastDot + 1).equals(MESSAGES_CLASS_NAME)) {
			fail("Messages class '" + className + "' must be named '" + MESSAGES_CLASS_NAME + "'", null); 
		}
		
		if (! AbstractMessages.class.isAssignableFrom(messages)) {
			fail("Messages class '" + className + "' must extend '" + AbstractMessages.class.getName() + "'.", null);
		}
		
		Field[] allFields = messages.getDeclaredFields();
		
		boolean hasErrors = false;
		for (int cnt = allFields.length, n = 0; n < cnt; n++) {
			final Field constantField = allFields[n];
			boolean hasErrorsField = false;
			
			if (constantField.isSynthetic()) {
				// skip synthetic fields, e.g. fields included by coverage tools
				continue;
			}
			int constantModifiers = constantField.getModifiers();
			if (! Modifier.isPublic(constantModifiers)) {
				error("Fields in message constants '" + messages + "' must be public, '" + constantField + "' is not.", null);
				hasErrors = hasErrorsField = true;
			}
			
			if (! Modifier.isStatic(constantModifiers)) {
				error("Fields in message constants '" + messages + "' must be static, '" + constantField + "' is not.", null);
				hasErrors = hasErrorsField = true;
			}
			
			Class<?> constantType = constantField.getType();
			if (! Template.class.isAssignableFrom(constantType) || (constantType == Template.class)) {
				error("Templates must be of type TemplateX for X in 0,..," + MAX_ARG_CNT + ". '" + constantField + "' is not.", null);
				hasErrors = hasErrorsField = true;
			}
			
			if (hasErrorsField) {
				// Going any further would produce follow-up failures.
				continue;
			}
			
			final int argCnt = Integer.parseInt(constantType.getSimpleName().substring(Template.class.getSimpleName().length()));
			if (argCnt > MAX_ARG_CNT) {
				error("Templates in '" + messages + "' must be of type TemplateX for X in 0,..," + MAX_ARG_CNT + ". '" + constantField + "' is not.", null);
				hasErrors = true;
				
				// Going any further would produce follow-up failures.
				continue;
			}
			
			Constructor<?> implFac = IMPL_FACTORIES[argCnt];
			
			try {
				Object initialValue = constantField.get(null);
				if (initialValue != null) {
					Logger.error("Error initializing message class '" + messages + "': Already initialized: '" + constantField + "'.", AbstractMessages.class);
					continue;
				}
				
				Object templateInstance = implFac.newInstance(PackageProtected.INSTANCE, constantField);
				
				constantField.set(null, templateInstance);
			} catch (IllegalAccessException e) {
				throw new UnreachableAssertion(e);
			} catch (IllegalArgumentException ex) {
				error("Error initializing message class '" + messages + "'.", ex);
				hasErrors = true;
			} catch (InstantiationException ex) {
				error("Error initializing message class '" + messages + "'.", ex);
				hasErrors = true;
			} catch (InvocationTargetException ex) {
				error("Error initializing message class '" + messages + "'.", ex);
				hasErrors = true;
			}
		}
		
		if (hasErrors) {
			fail("Error initializing message class '" + messages + "', details have been logged.", null);
		}
	}
	
	private static void fail(String message, Throwable ex) {
		error(message, ex);
		throw (AssertionError) new AssertionError(message).initCause(ex);
	}
	
	private static void error(String message, Throwable ex) {
		Logger.error(message, ex, AbstractMessages.class);
	}

	protected static void initDefaults(Class<? extends AbstractMessages> messages) {
		Field[] allFields = messages.getDeclaredFields();
		for (int cnt = allFields.length, n = 0; n < cnt; n++) {
			final Field constantField = allFields[n];

			try {
				if (constantField.isSynthetic()) {
					// skip synthetic fields, e.g. fields included by coverage tools
					continue;
				}

				StaticTemplate<?> template = (StaticTemplate<?>) constantField.get(null);
				boolean success = template.finishInitialization();
				if (! success) {
					Logger.error("Error initializing message class '" + messages + "': Already initialized: '" + constantField + "'", AbstractMessages.class);
				}
			} catch (IllegalArgumentException ex) {
				Logger.error("Error initializing message class '" + messages + "'.", ex, AbstractMessages.class);
			} catch (IllegalAccessException ex) {
				Logger.error("Error initializing message class '" + messages + "'.", ex, AbstractMessages.class);
			}
		}
	}

	/**
	 * Type used to ensure that {@link Template}s must be instantiated through
	 * constant declarations only.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	/*package protected*/ 
	static final class PackageProtected {
		/**
		 * Singleton {@link AbstractMessages.PackageProtected} instance.
		 */
		/*package protected*/ static final PackageProtected INSTANCE = new PackageProtected();
		
		private PackageProtected() {
			// Singleton constructor.
		}
	}
	
}
