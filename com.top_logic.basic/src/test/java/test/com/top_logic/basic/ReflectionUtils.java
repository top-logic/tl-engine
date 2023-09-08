/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;

/**
 * The class {@link ReflectionUtils} provides methods to access methods and
 * fields of objects via reflection
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ReflectionUtils {

	/**
	 * {@link RuntimeException} thrown by methods in {@link ReflectionUtils}.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	public static class ReflectionException extends RuntimeException {

		ReflectionException() {
		}

		ReflectionException(String message) {
			super(message);
		}

		ReflectionException(Throwable cause) {
			super(cause);
		}

		ReflectionException(String message, Throwable cause) {
			super(message, cause);
		}

	}

	/**
	 * Returns the field with the given name declared in the given class. If
	 * there is no such field and <code>includeSuperClasses</code> is set, then
	 * the superclasses are checked.
	 * 
	 * @param definingClass
	 *        the class to get the field from. must not be <code>null</code>
	 * @param fieldName
	 *        the name of the field
	 * @param includeSuperclasses
	 *        whether also superclasses of the given class are checked
	 * 
	 * @return a {@link Field} for the field with the given name
	 * 
	 * @throws ReflectionException
	 *         if there is no such field, neither in the given class nor in any
	 *         superclass
	 */
	public static Field getField(Class<?> definingClass, String fieldName, boolean includeSuperclasses) throws ReflectionException {
		Class<?> declaringClass = definingClass;
		do {
			try {
				return declaringClass.getDeclaredField(fieldName);
			} catch (SecurityException ex) {
				// trying superclass
			} catch (NoSuchFieldException ex) {
				// trying superclass
			}
			declaringClass = declaringClass.getSuperclass();
		} while (includeSuperclasses && declaringClass != null);

		// no such field found
		StringBuilder message = new StringBuilder();
		message.append("There is no field named '");
		message.append(fieldName);
		message.append("' declared in class '");
		message.append(definingClass.getSimpleName());
		if (includeSuperclasses) {
			message.append("' or superclasses!");
		} else {
			message.append("'!");
		}
		throw new ReflectionException(message.toString());
	}

	/**
	 * Returns the method with the given name and given signature declared in
	 * the given class. If there is no such method (and
	 * <code>includeSuperclasses</code> is <code>true</code>) the superclasses
	 * are checked.
	 * 
	 * @param holderClass
	 *        the class to get the method from
	 * @param methodName
	 *        the name of the method
	 * @param signature
	 *        the signature of the desired method. <code>null</code> means the empty method
	 * @param includeSuperclasses
	 *        whether also superclasses are checked for the given method
	 * 
	 * @return a {@link Field} for the field with the given name
	 * 
	 * @throws ReflectionException
	 *         if there is no such method, neither in the given class nor in any
	 *         superclass
	 */
	public static Method getMethod(Class<?> holderClass, String methodName, Class<?>[] signature, boolean includeSuperclasses)
			throws ReflectionException {
		Class<?> declaringClass = holderClass;
		do {
			try {
				return declaringClass.getDeclaredMethod(methodName, signature);
			} catch (SecurityException ex) {
				// trying superclass
			} catch (NoSuchMethodException ex) {
				// trying superclass
			}
			declaringClass = declaringClass.getSuperclass();
		} while (includeSuperclasses && declaringClass != null);

		// no such method found
		StringBuilder message = new StringBuilder();
		message.append("There is no method named '");
		message.append(methodName);
		message.append("' declared in class '");
		message.append(holderClass.getSimpleName());
		if (includeSuperclasses) {
			message.append("' or superclasses!");
		} else {
			message.append("'!");
		}
		throw new ReflectionException(message.toString());
	}

	/**
	 * Executes {@link #executeMethod(Object, String, Class[], Object[], Class)} with expected type
	 * {@link Object}.
	 * 
	 * @see #executeMethod(Object, String, Class[], Object[], Class)
	 */
	public static Object executeMethod(Object methodHolder, String methodName, Class<?>[] signature, Object[] args) {
		return executeMethod(methodHolder, methodName, signature, args, Object.class);
	}

	/**
	 * Finds the method with the given name and the given signature defined in the runtime class of
	 * <code>methodHolder</code>. After getting the method it is invoked with the given arguments
	 * and the result is returned.
	 * 
	 * @param methodHolder
	 *        the object to get the method to call from
	 * @param methodName
	 *        the name of the method to execute
	 * @param signature
	 *        the signature of the method to call. <code>null</code> means the empty signature
	 * @param args
	 *        the arguments used to invoke the method. must match the signature. <code>null</code>
	 *        means the empty array
	 * @param expectedType
	 *        the expected type of the result.
	 * 
	 * @return the value returned by the method with the given name
	 * 
	 * @throws ReflectionException
	 *         if something fails
	 */
	public static <T> T executeMethod(Object methodHolder, String methodName, Class<?>[] signature, Object[] args,
			Class<T> expectedType) {
		Method method = getMethod(methodHolder.getClass(), methodName, signature, true);
		Object result;
		try {
			method.setAccessible(true);
			result = method.invoke(methodHolder, args);
		} catch (Exception ex) {
			StringBuilder message = new StringBuilder();
			message.append("Unable to execute method '");
			message.append(methodName);
			message.append("' with signature '");
			message.append(signature);
			message.append("' and arguments '");
			message.append(args);
			message.append("' in object '");
			message.append(methodHolder);
			message.append("'!");
			throw new ReflectionException(message.toString(), ex);
		}
		if (result == null) {
			return null;
		} else {
			if (expectedType.isInstance(result)) {
				return expectedType.cast(result);
			} else {
				throw new ReflectionException("Can not cast '" + result + "' to '" + expectedType.getName() + "'");
			}
		}
	}

	/**
	 * Calls {@link #executeStaticMethod(Class, String, Class[], Object[])} with
	 * the class with the given name.
	 * 
	 * @see #executeStaticMethod(Class, String, Class[], Object[])
	 */
	public static Object executeStaticMethod(String definingClass, String methodName, Class<?>[] signature, Object[] args) {
		try {
			final Class<?> clazz = Class.forName(definingClass);
			return executeStaticMethod(clazz, methodName, signature, args);
		} catch (ClassNotFoundException ex) {
			throw new ReflectionException(ex);
		}
	}

	/**
	 * Convenience variant of {@link #invoke(Method, Object, Object[])} for static methods, i.e.
	 * without the "receiver" argument.
	 */
	public static Object invokeStatic(Method method, Object... arguments) {
		return invoke(method, null, arguments);
	}

	/**
	 * Convenience variant of {@link Method#invoke(Object, Object[])} that wraps
	 * {@link ReflectiveOperationException}s in {@link RuntimeException}s.
	 * 
	 * @param method
	 *        The {@link Method} to invoke. Is not allowed to be null.
	 * @param receiver
	 *        The receiver on which the {@link Method} is called. For instance methods, it is not
	 *        allowed to be null. For static methods, it is ignored and is allowed to be null.
	 * @param arguments
	 *        The arguments to pass to the {@link Method}. Is not allowed to be null, but contain
	 *        null, where the method accepts null.
	 * @return The result of the method.
	 */
	public static Object invoke(Method method, Object receiver, Object... arguments) {
		try {
			return method.invoke(receiver, arguments);
		} catch (ReflectiveOperationException ex) {
			String message = "Failed to invoke the method " + method + " with reflection. Cause: " + ex.getMessage();
			throw new RuntimeException(message, ex);
		}
	}

	/**
	 * Executes a static method defined in the given class.
	 * 
	 * @param definingClass
	 *        the class in which the method is declared
	 * @param methodName
	 *        the name of the method to call
	 * @param signature
	 *        the signature of the desired method
	 * @param args
	 *        the arguments used to invoke the method
	 * 
	 * @return the value of the method
	 */
	public static Object executeStaticMethod(Class<?> definingClass, String methodName, Class<?>[] signature, Object[] args) {
		final Method method = getMethod(definingClass, methodName, signature, false);
		try {
			method.setAccessible(true);
			return method.invoke(definingClass, args);
		} catch (Exception ex) {
			throw new ReflectionException(ex);
		}
	}

	/**
	 * Sets <code>newValue</code> of the field with name <code>fieldName</code>
	 * declared in a superclass of the type of the object
	 * <code>valueHolder</code>
	 * 
	 * @param valueHolder
	 *        an object which has a field named <code>fieldName</code> whose
	 *        value is set
	 * @param fieldName
	 *        the name of the field whose value is set
	 * 
	 * @return the old value of the field
	 * 
	 * @throws ReflectionException
	 *         if it is not possible to set the value due to any reason
	 */
	public static Object setValue(Object valueHolder, String fieldName, Object newValue) throws ReflectionException {
		try {
			final Field declaredField = getField(valueHolder.getClass(), fieldName, true);
			declaredField.setAccessible(true);
			final Object oldValue = declaredField.get(valueHolder);
			declaredField.set(valueHolder, newValue);
			return oldValue;
		} catch (Exception ex) {
			StringBuilder message = new StringBuilder();
			message.append("Unable to set '");
			message.append(newValue);
			message.append("' to field '");
			message.append(fieldName);
			message.append("' in object '");
			message.append(valueHolder);
			message.append("'!");
			throw new ReflectionException(message.toString(), ex);
		}
	}

	/**
	 * Gets the value of the field with the given name in the given object and
	 * casts it to the given type.
	 * 
	 * @throws ReflectionException
	 *         iff {@link #getValue(Object, String)} throws some or the returned
	 *         value is not of the expected type.
	 * 
	 * @see #getValue(Object, String)
	 */
	public static <T> T getValue(Object valueHolder, String fieldName, Class<T> expectedType) throws ReflectionException {
		Object value = getValue(valueHolder, fieldName);
		return safeCast(value, expectedType);
	}

	/**
	 * Casts the given <code>obj</code> to the <code>expectedType</code>. If the
	 * cast is not possible it throws an {@link ReflectionException}.
	 * 
	 * @param <T>
	 *        the expected type of the given object
	 * @param obj
	 *        the object to cast
	 * @param expectedType
	 *        the {@link Class} representation of the expected type
	 * 
	 * @return the given object
	 * 
	 * @throws ReflectionException
	 *         iff the given object can not be cast to the expected type
	 */
	@SuppressWarnings("unchecked")
	private static <T> T safeCast(Object obj, Class<T> expectedType) throws ReflectionException {
		if (obj != null && !expectedType.isInstance(obj)) {
			throw new ReflectionException("The value '" + obj + "' can not be cast to '" + expectedType.getName()+ "'");
		}
		return (T) obj;
	}

	/**
	 * Returns the value of the field with name <code>fieldName</code> declared
	 * in a superclass of the type of the object <code>valueHolder</code>
	 * 
	 * @param valueHolder
	 *        an object which has a field named <code>fieldName</code> whose
	 *        value is needed
	 * @param fieldName
	 *        the name of the field whose value is needed
	 * 
	 * @throws ReflectionException
	 *         if it is not possible to get the value due to any reason
	 */
	public static Object getValue(Object valueHolder, String fieldName) throws ReflectionException {
		final Class<?> definingClass = valueHolder.getClass();
		final Field declaredField = getField(definingClass, fieldName, true);
		try {
			declaredField.setAccessible(true);
			return declaredField.get(valueHolder);
		} catch (Exception ex) {
			StringBuilder message = new StringBuilder();
			message.append("Unable to get value of field '");
			message.append(fieldName);
			message.append("' in object '");
			message.append(valueHolder);
			message.append("'!");
			throw new ReflectionException(message.toString(), ex);
		}
	}

	/**
	 * Retrieves the {@link Class class object} from the given name and calls
	 * {@link #getStaticValue(Class, String)} with that class.
	 * 
	 * @see #getStaticValue(Class, String)
	 * 
	 * @throws ReflectionException
	 *         when thrown by {@link #setStaticValue(Class, String, Object)} or
	 *         <code>definingClass</code> does not represent a class
	 */
	public static Object getStaticValue(String definingClass, String field) throws ReflectionException {
		final Class<?> clazz = resolveClass(definingClass);
		return getStaticValue(clazz, field);
	}

	public static Object getStaticValue(Class<?> definingClass, String field) throws ReflectionException {
		final Field declaredField = getField(definingClass, field, false);
		try {
			declaredField.setAccessible(true);
			return declaredField.get(definingClass);
		} catch (Exception cause) {
			throw new ReflectionException(cause);
		}
	}

	/**
	 * Retrieves the {@link Class class object} from the given name and calls
	 * {@link #setStaticValue(Class, String, Object)} with that class.
	 * 
	 * @see #setStaticValue(Class, String, Object)
	 * 
	 * @throws ReflectionException
	 *         when thrown by {@link #setStaticValue(Class, String, Object)} or
	 *         <code>definingClass</code> does not represent a class
	 */
	public static Object setStaticValue(String definingClass, String field, Object newValue) throws ReflectionException {
		final Class<?> clazz = resolveClass(definingClass);
		return setStaticValue(clazz, field, newValue);
	}

	/**
	 * Retrieves the {@link Class class object} from the given name and calls
	 * {@link #setStaticValue(Class, String, Class, Object)} with that class.
	 * 
	 * @see #setStaticValue(Class, String, Class, Object)
	 * 
	 * @throws ReflectionException
	 *         when thrown by
	 *         {@link #setStaticValue(Class, String, Class, Object)} or
	 *         <code>definingClass</code> does not represent a class
	 */
	public static <T> T setStaticValue(String definingClass, String field, Class<T> declaredValueType, T newValue)
			throws ReflectionException {
		Class<?> clazz = resolveClass(definingClass);
		return setStaticValue(clazz, field, declaredValueType, newValue);
	}

	private static Class<?> resolveClass(String definingClass) {
		final Class<?> clazz;
		try {
			clazz = ConfigUtil.lookupClassForName(Object.class, definingClass);
		} catch (ConfigurationException cause) {
			throw new ReflectionException(cause);
		}
		return clazz;
	}

	/**
	 * Sets the given <code>newValue</code> as new value to the static field
	 * with the given name in the given class.
	 * 
	 * @param definingClass
	 *        the class containing the static field
	 * @param field
	 *        the name of the static field to set
	 * @param newValue
	 *        the new value of the field
	 * 
	 * @return the old value of the field
	 * 
	 * @throws ReflectionException
	 *         if there is no such field or something other fails
	 */
	public static Object setStaticValue(Class<?> definingClass, String field, Object newValue) throws ReflectionException {
		final Field declaredField = getField(definingClass, field, false);
		try {
			declaredField.setAccessible(true);
			final Object currentValue = declaredField.get(definingClass);
			declaredField.set(definingClass, newValue);
			return currentValue;
		} catch (Exception ex) {
			throw new ReflectionException(ex);
		}
	}

	/**
	 * Sets the given <code>newValue</code> as new value to the static field
	 * with the given name in the given class as
	 * {@link #setStaticValue(Class, String, Object)}.
	 * 
	 * Moreover it is expected that the declared type of the field is
	 * <code>declaredValueType</code>.
	 * 
	 * @param declaredValueType
	 *        the type of the value the field is declared
	 * 
	 * @return the old value of the field
	 * 
	 * @throws ReflectionException
	 *         if {@link #setStaticValue(Class, String, Object)} throws some or
	 *         the the field is not of the declared type.
	 */
	public static <T> T setStaticValue(Class<?> definingClass, String field, Class<T> declaredValueType, T newValue)
			throws ReflectionException {
		final Object oldValue = setStaticValue(definingClass, field, newValue);
		return cast(definingClass, field, declaredValueType, oldValue);
	}

	private static <T> T cast(Class<?> definingClass, String field, Class<T> declaredValueType, final Object value) {
		if (value != null && !declaredValueType.isInstance(value)) {
			throw new ReflectionException("Old value of field '" + field + "' in type '" + definingClass.getName()
					+ "' has wrong type: expected '" + declaredValueType.getName() + "', actual '"
					+ value.getClass().getName() + "', value '" + value + "'");
		}
		return declaredValueType.cast(value);
	}

	/**
	 * Convenience variant of {@link Class#forName(String)} that wraps
	 * {@link ClassNotFoundException}s in {@link RuntimeException}s.
	 */
	public static Class<?> getClass(String qualifiedName) {
		try {
			return Class.forName(qualifiedName);
		} catch (ClassNotFoundException ex) {
			String message = "Failed to resolve the class '" + qualifiedName + "' with reflection."
				+ " Cause: " + ex.getMessage();
			throw new RuntimeException(message, ex);
		}
	}

}
