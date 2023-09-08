/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.build.maven.sourceprocessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.github.javaparser.ParserConfiguration.LanguageLevel;
import com.github.javaparser.Range;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier.Keyword;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import com.github.javaparser.ast.nodeTypes.NodeWithModifiers;
import com.github.javaparser.ast.stmt.BlockStmt;

import com.top_logic.tool.stacktrace.internal.IntRanges;
import com.top_logic.tool.stacktrace.internal.LineNumberEncoding;

/**
 * Algorithm stripping private information from a Java source file.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SourceStripper {

	private static String _encoding = "Cp1252";
	private static String _newLine = "\r\n";

	public static void main(String[] args) throws IOException {
		process(() -> new FileInputStream(new File(args[0])), new FileOutputStream(args[1]));
	}

	public static StripInfo process(InputStreamSupplier inSupplier, OutputStream out)
			throws FileNotFoundException, IOException {
		StaticJavaParser.getConfiguration().setPreprocessUnicodeEscapes(true);
		CompilationUnit compilationUnit;
		try (InputStream in = inSupplier.get()) {
			StaticJavaParser.getParserConfiguration().setLanguageLevel(LanguageLevel.JAVA_11);
			compilationUnit = StaticJavaParser.parse(in);
		}
		
		IntRanges excludeRanges = new IntRanges();
		
		for (TypeDeclaration<?> type : list(compilationUnit.getTypes())) {
			processType(excludeRanges, type);
		}
		
		int lineCnt = 0;
		List<String> lines = new ArrayList<>();
		try (BufferedReader r = new BufferedReader(new InputStreamReader(inSupplier.get(), _encoding))) {
			boolean lastLineEmpty = false;
			int nr = 1;
			String line;
			while ((line = r.readLine()) != null) {
				lineCnt++;
				if (excludeRanges.contains(nr)) {
					// Skip line.
				} else {
					boolean emptyLine = line.trim().isEmpty();
					if (emptyLine) {
						if (!lastLineEmpty) {
							lines.add(line);
						} else {
							// Skip line.
							excludeRanges.addRange(nr, nr + 1);
						}
					} else {
						lines.add(line);
					}
					lastLineEmpty = emptyLine;
				}
				
				nr++;
			}
		}
		
		try (Writer w = new OutputStreamWriter(out, _encoding)) {
			LineNumberEncoding conversion = new LineNumberEncoding(lineCnt, excludeRanges);
			conversion.initDecoder();
			for (int n = 0, size = lines.size(); n < size; n++) {
				w.write(lines.get(n));
				w.write(_newLine);
			}
		}

		return new StripInfo(lineCnt, excludeRanges);
	}

	private static void processType(IntRanges excludeRanges, TypeDeclaration<?> type) {
		processType(false, excludeRanges, type);
	}

	private static void processType(boolean publicContainer, IntRanges excludeRanges, TypeDeclaration<?> type) {
		if (shouldRemove(publicContainer, type)) {
			addRange(excludeRanges, type);
		} else {
			boolean publicContext = isPublicContext(type);
			for (BodyDeclaration<?> member : list(type.getMembers())) {
				if (member instanceof FieldDeclaration) {
					FieldDeclaration field = (FieldDeclaration) member;
					if (shouldRemove(publicContext, field)) {
						if (field.getComment().isPresent()) {
							addRange(excludeRanges, field.getComment().get());
						}
					}
					// Keep field.
					continue;
				}
				if (member instanceof TypeDeclaration<?>) {
					processType(publicContext, excludeRanges, (TypeDeclaration<?>) member);
				} else if (!(member instanceof NodeWithModifiers)
					|| shouldRemove(publicContext, (NodeWithModifiers<?>) member)) {
					addRange(excludeRanges, member);
				} else if (member instanceof MethodDeclaration && shouldStrip((MethodDeclaration) member)) {
					Optional<BlockStmt> bodyOption = ((MethodDeclaration) member).getBody();
					if (bodyOption.isPresent()) {
						BlockStmt block = bodyOption.get();
						for (Node stmt : block.getChildNodes()) {
							addRange(excludeRanges, stmt);
						}
					}
				}
			}
		}
	}

	private static boolean isPublicContext(TypeDeclaration<?> type) {
		if (type instanceof ClassOrInterfaceDeclaration) {
			return ((ClassOrInterfaceDeclaration) type).isInterface();
		}
		return type.isAnnotationDeclaration();
	}

	private static void addRange(IntRanges ranges, Node node) {
		Optional<Comment> comment = node.getComment();
		if (comment.isPresent()) {
			addDirectRange(ranges, comment.get());
		}
		addDirectRange(ranges, node);
	}

	private static void addDirectRange(IntRanges ranges, Node node) {
		Optional<Range> rangeHandle = node.getRange();
		if (rangeHandle.isPresent()) {
			Range range = rangeHandle.get();
			ranges.addRange(range.begin.line, range.end.line + 1);
		}
	}

	private static boolean shouldRemove(boolean publicContext, NodeWithModifiers<?> node) {
		if (node instanceof NodeWithAnnotations) {
			Optional<AnnotationExpr> annotationHandle =
				((NodeWithAnnotations<?>) node).getAnnotationByName("FrameworkInternal");
			if (annotationHandle.isPresent()) {
				return true;
			}
		}
		return !publicContext && !node.hasModifier(Keyword.PUBLIC) && !node.hasModifier(Keyword.PROTECTED);
	}

	private static boolean shouldStrip(NodeWithModifiers<?> node) {
		return node.hasModifier(Keyword.PROTECTED);
	}

	private static <T extends Node> List<T> list(NodeList<T> list) {
		return new ArrayList<>(list);
	}
}
