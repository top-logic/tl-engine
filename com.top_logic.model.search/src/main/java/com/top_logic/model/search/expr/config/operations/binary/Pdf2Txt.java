/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations.binary;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.top_logic.basic.Settings;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.element.meta.TypeSpec;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.WithFlatMapSemantics;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link GenericMethod} converting a PDF document to text.
 * 
 * <p>
 * The function uses the external tools `pdf2txt` for PDF with text contents and `pdftoppm` and
 * `tesseract` for OCR of scanned PDFs.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Pdf2Txt extends GenericMethod implements WithFlatMapSemantics<Object[]> {

	/**
	 * Application configuration options for executables used in PDF to text conversion.
	 */
	public interface GlobalConfig extends ConfigurationItem {

		/**
		 * The system-installed executable program to call for PDF to text conversion.
		 */
		@StringDefault("pdf2txt")
		String getPdf2TxtExecutable();

		/**
		 * The system-installed executable program to call for PDF to PNG conversion as preparation
		 * for OCR.
		 */
		@StringDefault("pdftoppm")
		String getPdf2PpmExecutable();

		/**
		 * The system-installed executable program for OCR.
		 */
		@StringDefault("tesseract")
		String getTesseractExecutable();

	}

	/** 
	 * Creates a {@link Pdf2Txt}.
	 */
	protected Pdf2Txt(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new Pdf2Txt(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return TLModelUtil.findType(TypeSpec.STRING_TYPE);
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		return evalPotentialFlatMap(definitions, null, arguments);
	}

	@Override
	public Object evalDirect(EvalContext definitions, Object self, Object[] arguments) {
		Object input = arguments[0];
		if (input == null) {
			return null;
		}

		if (!(input instanceof BinaryDataSource)) {
			throw new TopLogicException(
				I18NConstants.ERROR_BINARY_DATA_EXPECTED__ACTUAL.fill(input.getClass().getName()));
		}

		BinaryDataSource data = (BinaryDataSource) input;

		GlobalConfig config = ApplicationConfig.getInstance().getConfig(GlobalConfig.class);

		try {
			File tmpDir = Settings.getInstance().getTempDir();

			File tmp = File.createTempFile("data", ".pdf", tmpDir);

			try (FileOutputStream out = new FileOutputStream(tmp)) {
				data.deliverTo(out);
			}

			Process pdf2txt =
				new ProcessBuilder(config.getPdf2TxtExecutable(), "--layoutmode", "lr-tb", tmp.getAbsolutePath())
					.redirectOutput(Redirect.PIPE)
					.start();

			String result = StreamUtilities.readAllFromStream(pdf2txt.getInputStream(), StandardCharsets.UTF_8).trim();

			waitFor(pdf2txt, "pdf2txt");

			if (!result.isEmpty()) {
				tmp.delete();
				return result;
			}

			// Maybe a scanned PDF, try OCR.
			File tmpConversionDir = File.createTempFile("image-data", "", tmpDir);
			FileUtilities.deleteR(tmpConversionDir);
			tmpConversionDir.mkdir();

			waitFor(
				new ProcessBuilder(config.getPdf2PpmExecutable(), tmp.getAbsolutePath(),
					new File(tmpConversionDir, "img").getAbsolutePath(), "-png")
					.start(),
				"pdftoppm");

			File[] pngFiles = tmpConversionDir.listFiles();
			if (pngFiles == null) {
				FileUtilities.deleteR(tmpConversionDir);
				tmp.delete();
				return "";
			}

			StringBuilder buffer = new StringBuilder();
			for (File png : pngFiles) {
				Process tesseract = new ProcessBuilder(config.getTesseractExecutable(), png.getAbsolutePath(), "-")
					.redirectInput(Redirect.PIPE)
					.start();
				buffer.append(
					StreamUtilities.readAllFromStream(tesseract.getInputStream(), StandardCharsets.UTF_8).trim());
				waitFor(tesseract, "tesseract");
				buffer.append("\n\n");

				png.delete();
			}

			FileUtilities.deleteR(tmpConversionDir);
			tmp.delete();

			return buffer.toString();
		} catch (IOException | InterruptedException ex) {
			throw new TopLogicException(
				I18NConstants.ERROR_CONVERSION_FAILED__MSG.fill(ex.getMessage()));
		}
	}

	private void waitFor(Process p, String name) throws InterruptedException {
		int result = p.waitFor();
		if (result != 0) {
			throw new TopLogicException(I18NConstants.ERROR_CONVERSION_FAILED__MSG.fill(name + ": " + result));
		}
	}

	/**
	 * {@link MethodBuilder} for {@link Pdf2Txt}.
	 */
	public static class Builder extends AbstractSimpleMethodBuilder<Pdf2Txt> {

		private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("input")
			.build();

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public Pdf2Txt build(Expr expr, SearchExpression[] args) throws ConfigurationException {
			return new Pdf2Txt(getName(), args);
		}

		@Override
		public ArgumentDescriptor descriptor() {
			return DESCRIPTOR;
		}

	}
}
