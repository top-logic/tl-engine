/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.generate;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.tooling.ModuleLayoutConstants;

/**
 * Generator for an abstract proxy implementation that corresponds to a Java interface.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ProxyGenerator extends JavaGenerator {

	private static final String NAME_OPTION = "-name";
	private static final String INTERFACE_OPTION = "-interface";
	private static final String PACKAGE_OPTION = "-package";
	private static final String SRC_OPTION = "-src";
	private final String proxyClassName;
	private final Class<?> interfaceClass;

	private ProxyGenerator(String packageName, String proxyClassName, Class<?> interfaceClass) {
		super(packageName);
		this.proxyClassName = proxyClassName;
		this.interfaceClass = interfaceClass;
	}

	/**
	 * Application main method of {@link ProxyGenerator}.
	 */
	public static void main(String[] args) {
		Options options = new Options();
		for (int n = 0, cnt = args.length; n < cnt; ) {
			String option = args[n++];
			if (SRC_OPTION.equals(option)) {
				options.srcDirName = args[n++];
			}
			else if (PACKAGE_OPTION.equals(option)) {
				options.packageName = args[n++];
			}
			else if (INTERFACE_OPTION.equals(option)) {
				options.interfaceClassName = args[n++];
			}
			else if (NAME_OPTION.equals(option)) {
				options.proxyClassName = args[n++];
			}
			else {
				throw error("Unknown option '" + option + "'.");
			}
		}
		
		run(options);
	}

	static class Options {
		
		public String srcDirName;

		public String interfaceClassName;

		public String packageName;

		public String proxyClassName;

	}

	private static void run(Options options) {
		if (options.interfaceClassName == null) {
			throw error("Missing interface name.");
		}
		Class<?> interfaceClass = lookupClass(options);
		if (!interfaceClass.isInterface()) {
			throw error("Not an interface: " + interfaceClass.getName());
		}
		
		String proxyClassName = options.proxyClassName == null ? "Abstract" + interfaceClass.getSimpleName() + "Proxy" : options.proxyClassName;

		File srcDir;
		if (options.srcDirName == null) {
			srcDir = new File(ModuleLayoutConstants.SRC_MAIN_DIR);
		} else {
			srcDir = new File(options.srcDirName);
		}
		if (!srcDir.exists()) {
			throw error("Source dir does not exist: " + srcDir.getAbsolutePath());
		}
		
		String packageName = options.packageName == null ? "" : options.packageName;
		File packageDir = getPackageDir(srcDir, packageName);
		packageDir.mkdirs();
		
		try {
			new ProxyGenerator(packageName, proxyClassName, interfaceClass).generate(new File(packageDir, proxyClassName + ".java"));
		} catch (IOException ex) {
			error("Generation failed: " + ex.getMessage());
		}
	}

	private static Class<?> lookupClass(Options options) {
		try {
			return Class.forName(options.interfaceClassName);
		} catch (ClassNotFoundException ex) {
			throw error("The class '" + options.interfaceClassName + "' cannot be resolved in the class path of the tools JVM.");
		}
	}

	private static RuntimeException error(String message) {
		help();
		throw new RuntimeException(message);
	}

	private static void help() {
		System.out.println("Expected arguments:");
		System.out.println("   " + INTERFACE_OPTION + " <interface to generate a proxy for>");
		System.out.println("   " + NAME_OPTION + " <name of the generated proxy class>");
		System.out.println("   " + PACKAGE_OPTION + " <package of the generated proxy class>");
		System.out.println("   " + SRC_OPTION + " <source folder to generate the generated proxy class in>");
	}

	private static File getPackageDir(File srcDir, String fullPackageName) {
		int index = 0;
		File result = srcDir;
		if (fullPackageName.length() > 0) {
			while (true) {
				int separatorIndex = fullPackageName.indexOf('.', index);
				if (separatorIndex < 0) {
					result = new File(result, fullPackageName.substring(index));
					break;
				} else {
					result = new File(result, fullPackageName.substring(index, separatorIndex));
					index = separatorIndex + 1;
				}
			}
		}
		return result;
	}

	@Override
	protected void writeBody() {
		for (String importName : getReferencedClasses().getImportNames()) {
			line("import " + importName + ";");
		}
		nl();
		nl();
		
		javadocStart();
		commentLine("Abstract proxy for {@link " + interfaceClass.getSimpleName() + "}" + ".");
		commentLine("");
		commentLine("@see #impl()");
		commentLine("");
		commentLine("@author Automatically generated by <code>" + ProxyGenerator.class.getName() + "</code>");
		javadocStop();
		line("public abstract class " + className() + " implements " + interfaceClass.getSimpleName() + "{");
		nl();
		
		generateImplLookup();

		generateProxyMethods();
		
		line("}");
	}

	private void generateImplLookup() {
		javadocStart();
		commentLine("The underlying implementation.");
		javadocStop();
		line("protected abstract " + interfaceClass.getSimpleName() + " impl();");
		nl();
	}

	private void generateProxyMethods() {
		for (Method method : interfaceClass.getMethods()) {
			generateProxyMethod(method);
		}
	}

	private void generateProxyMethod(Method method) {
		String returnTypeName = TypeUtil.getTypeName(method.getGenericReturnType());
		StringBuilder paramDecl = getParameterDecl(method);
		StringBuilder throwsClause = getThrowsClause(method);
		StringBuilder argsExpr = getArgumentsExpr(method);
		String returnStm = getReturnStatement(method);
		
		if (isJava6()) {
			line("@Override");
		}
		if (method.getAnnotation(Deprecated.class) != null) {
			line("@Deprecated");
		}
		line("public " + returnTypeName + " " + method.getName() + "(" + paramDecl + ")" + throwsClause + " {");
		line(returnStm + "impl()." + method.getName() + "(" + argsExpr + ");");
		line("}");
		nl();
	}

	private boolean isJava6() {
		return true;
	}

	private StringBuilder getParameterDecl(Method method) {
		StringBuilder result = new StringBuilder();
		int n = 0;
		for (Type paramType : method.getGenericParameterTypes()) {
			if (result.length() > 0) {
				result.append(", ");
			}
			result.append(TypeUtil.getTypeName(paramType));
			result.append(' ');
			result.append(getParameterName(method, n));
			
			n++;
		}
		return result;
	}

	private StringBuilder getArgumentsExpr(Method method) {
		StringBuilder args = new StringBuilder();
		for (int n = 0, cnt = method.getGenericParameterTypes().length; n < cnt; n++) {
			if (args.length() > 0) {
				args.append(", ");
			}
			args.append(getParameterName(method, n));
		}
		return args;
	}

	/**
	 * Construct a name for the given method parameter.
	 * 
	 * @param n
	 *        The index of the method parameter in the
	 *        {@link Method#getParameterTypes()} array.
	 * @param method
	 *        The interface method.
	 */
	private String getParameterName(Method method, int n) {
		return "a" + (n + 1);
	}

	private String getReturnStatement(Method method) {
		String returnStm;
		if (method.getReturnType() == void.class) {
			returnStm = "";
		} else {
			returnStm = "return ";
		}
		return returnStm;
	}

	private StringBuilder getThrowsClause(Method method) {
		StringBuilder exceptions = new StringBuilder();
		for (Class<?> exception : method.getExceptionTypes()) {
			if (exceptions.length() == 0) {
				exceptions.append(" throws ");
			} else {
				exceptions.append(", ");
			}
			exceptions.append(exception.getSimpleName());
		}
		return exceptions;
	}

	private Imports getReferencedClasses() {
		Imports result = new Imports();
		result.add(interfaceClass);
		for (Method method : interfaceClass.getMethods()) {
			result.add(method.getReturnType());
			for (Class<?> paramType : method.getParameterTypes()) {
				result.add(paramType);
			}
			for (Class<?> exType : method.getExceptionTypes()) {
				result.add(exType);
			}
		}
		return result;
	}
	
	static class Imports {
		private Set<Class<?>> classes = new HashSet<>();
		
		public void add(Class<?> clazz) {
			if (clazz.isPrimitive()) {
				return;
			}
			if (clazz.isArray()) {
				add(clazz.getComponentType());
				return;
			}
			if (String.class.getPackage().equals(clazz.getPackage())) {
				return;
			}
			classes.add(clazz);
		}
		
		List<String> getImportNames() {
			ArrayList<String> result = new ArrayList<>(classes.size());
			for (Class<?> clazz : classes) {
				result.add(clazz.getName());
			}
			Collections.sort(result);
			return result;
		}
	}

	@Override
	public String className() {
		return proxyClassName;
	}
	
}
