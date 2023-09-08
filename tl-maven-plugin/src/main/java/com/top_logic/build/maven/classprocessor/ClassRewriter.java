/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.build.maven.classprocessor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.top_logic.tool.stacktrace.internal.LineNumberEncoding;

/**
 * Algorithm applying a {@link LineNumberEncoding} to a given Java class file.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ClassRewriter {

	Properties _mapping;

	/** 
	 * Creates a {@link ClassRewriter}.
	 */
	public ClassRewriter() {
		super();
	}

	public void loadLineNumberMapping(File file) throws IOException, FileNotFoundException {
		_mapping = new Properties();
		_mapping.load(new FileInputStream(file));
	}

	public void rewrite(InputStream in, OutputStream out) throws IOException {
		ClassReader reader = new ClassReader(in);
		ClassWriter writer = new ClassWriter(0);

		ClassVisitor rewrite = new ClassVisitor(Opcodes.ASM9, writer) {
			LineNumberEncoding _encoding;

			private String _name;

			@Override
			public void visit(int version, int access, String name, String signature, String superName,
					String[] interfaces) {
				_name = name;
				super.visit(version, access, name, signature, superName, interfaces);
			}

			@Override
			public void visitSource(String source, String debug) {
				_encoding = null;

				if (source != null) {
					int pkgSepIndex = _name.lastIndexOf('/');
					String fileName;
					if (pkgSepIndex >= 0) {
						fileName = _name.substring(0, pkgSepIndex) + '/' + source;
					} else {
						fileName = source;
					}
					String value = _mapping.getProperty(stripJavaExtension(fileName));
					if (value != null) {
						LineNumberEncoding encoder = LineNumberEncoding.fromString(value);
						_encoding = encoder;
						_encoding.initEncoder();
					}
				}

				super.visitSource(source, debug);
			}

			private String stripJavaExtension(String source) {
				if (source == null) {
					return null;
				}
				if (source.endsWith(".java")) {
					return source.substring(0, source.length() - ".java".length());
				}
				return source;
			}


			@Override
			public MethodVisitor visitMethod(int access, String name, String desc, String signature,
					String[] exceptions) {
				MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
				if (_encoding == null) {
					return mv;
				}
				return new MethodVisitor(Opcodes.ASM5, mv) {
					@Override
					public void visitLabel(Label label) {
						super.visitLabel(label);
					}

					@Override
					public void visitLineNumber(int line, Label start) {
						line = _encoding.encode(line);
						super.visitLineNumber(line, start);
					}
				};
			}
		};

		reader.accept(rewrite, 0);

		out.write(writer.toByteArray());
	}

	public static void main(String[] args) throws IOException {
		ClassRewriter rewriter = new ClassRewriter();
		rewriter.loadLineNumberMapping(new File(args[0]));
		try (FileInputStream in = new FileInputStream(new File(args[1]))) {
			try (FileOutputStream out = new FileOutputStream(new File(args[2]))) {
				rewriter.rewrite(in, out);
			}
		}
	}

}
