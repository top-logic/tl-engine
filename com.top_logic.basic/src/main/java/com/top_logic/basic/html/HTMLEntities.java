/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.html;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility that lists all entities defined by HTML (excluding those also defined by XML).
 * 
 * @see #CHAR_BY_BY_NAME
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class HTMLEntities {

	/**
	 * The character replacement for the entity with the key name.
	 * 
	 * <p>
	 * E.g. for <code>nbsp</code>, the character with code U+00A0 is returned.
	 * </p>
	 * 
	 * @see "https://en.wikipedia.org/wiki/List_of_XML_and_HTML_character_entity_references"
	 */
	public static final Map<String, String> CHAR_BY_BY_NAME;

	static {
		HashMap<String, String> map = new HashMap<>();

		// Entities also defined by XML
		//
		// map.put("quot", "\u0022");
		// map.put("amp", "\u0026");
		// map.put("apos", "\u0027");
		// map.put("lt", "\u003C");
		// map.put("gt", "\u003E");

		map.put("nbsp", "\u00A0");
		map.put("iexcl", "\u00A1");
		map.put("cent", "\u00A2");
		map.put("pound", "\u00A3");
		map.put("curren", "\u00A4");
		map.put("yen", "\u00A5");
		map.put("brvbar", "\u00A6");
		map.put("sect", "\u00A7");
		map.put("uml", "\u00A8");
		map.put("copy", "\u00A9");
		map.put("ordf", "\u00AA");
		map.put("laquo", "\u00AB");
		map.put("not", "\u00AC");
		map.put("shy", "\u00AD");
		map.put("reg", "\u00AE");
		map.put("macr", "\u00AF");
		map.put("deg", "\u00B0");
		map.put("plusmn", "\u00B1");
		map.put("sup2", "\u00B2");
		map.put("sup3", "\u00B3");
		map.put("acute", "\u00B4");
		map.put("micro", "\u00B5");
		map.put("para", "\u00B6");
		map.put("middot", "\u00B7");
		map.put("cedil", "\u00B8");
		map.put("sup1", "\u00B9");
		map.put("ordm", "\u00BA");
		map.put("raquo", "\u00BB");
		map.put("frac14", "\u00BC");
		map.put("frac12", "\u00BD");
		map.put("frac34", "\u00BE");
		map.put("iquest", "\u00BF");
		map.put("Agrave", "\u00C0");
		map.put("Aacute", "\u00C1");
		map.put("Acirc", "\u00C2");
		map.put("Atilde", "\u00C3");
		map.put("Auml", "\u00C4");
		map.put("Aring", "\u00C5");
		map.put("AElig", "\u00C6");
		map.put("Ccedil", "\u00C7");
		map.put("Egrave", "\u00C8");
		map.put("Eacute", "\u00C9");
		map.put("Ecirc", "\u00CA");
		map.put("Euml", "\u00CB");
		map.put("Igrave", "\u00CC");
		map.put("Iacute", "\u00CD");
		map.put("Icirc", "\u00CE");
		map.put("Iuml", "\u00CF");
		map.put("ETH", "\u00D0");
		map.put("Ntilde", "\u00D1");
		map.put("Ograve", "\u00D2");
		map.put("Oacute", "\u00D3");
		map.put("Ocirc", "\u00D4");
		map.put("Otilde", "\u00D5");
		map.put("Ouml", "\u00D6");
		map.put("times", "\u00D7");
		map.put("Oslash", "\u00D8");
		map.put("Ugrave", "\u00D9");
		map.put("Uacute", "\u00DA");
		map.put("Ucirc", "\u00DB");
		map.put("Uuml", "\u00DC");
		map.put("Yacute", "\u00DD");
		map.put("THORN", "\u00DE");
		map.put("szlig", "\u00DF");
		map.put("agrave", "\u00E0");
		map.put("aacute", "\u00E1");
		map.put("acirc", "\u00E2");
		map.put("atilde", "\u00E3");
		map.put("auml", "\u00E4");
		map.put("aring", "\u00E5");
		map.put("aelig", "\u00E6");
		map.put("ccedil", "\u00E7");
		map.put("egrave", "\u00E8");
		map.put("eacute", "\u00E9");
		map.put("ecirc", "\u00EA");
		map.put("euml", "\u00EB");
		map.put("igrave", "\u00EC");
		map.put("iacute", "\u00ED");
		map.put("icirc", "\u00EE");
		map.put("iuml", "\u00EF");
		map.put("eth", "\u00F0");
		map.put("ntilde", "\u00F1");
		map.put("ograve", "\u00F2");
		map.put("oacute", "\u00F3");
		map.put("ocirc", "\u00F4");
		map.put("otilde", "\u00F5");
		map.put("ouml", "\u00F6");
		map.put("divide", "\u00F7");
		map.put("oslash", "\u00F8");
		map.put("ugrave", "\u00F9");
		map.put("uacute", "\u00FA");
		map.put("ucirc", "\u00FB");
		map.put("uuml", "\u00FC");
		map.put("yacute", "\u00FD");
		map.put("thorn", "\u00FE");
		map.put("yuml", "\u00FF");
		map.put("OElig", "\u0152");
		map.put("oelig", "\u0153");
		map.put("Scaron", "\u0160");
		map.put("scaron", "\u0161");
		map.put("Yuml", "\u0178");
		map.put("fnof", "\u0192");
		map.put("circ", "\u02C6");
		map.put("tilde", "\u02DC");
		map.put("Alpha", "\u0391");
		map.put("Beta", "\u0392");
		map.put("Gamma", "\u0393");
		map.put("Delta", "\u0394");
		map.put("Epsilon", "\u0395");
		map.put("Zeta", "\u0396");
		map.put("Eta", "\u0397");
		map.put("Theta", "\u0398");
		map.put("Iota", "\u0399");
		map.put("Kappa", "\u039A");
		map.put("Lambda", "\u039B");
		map.put("Mu", "\u039C");
		map.put("Nu", "\u039D");
		map.put("Xi", "\u039E");
		map.put("Omicron", "\u039F");
		map.put("Pi", "\u03A0");
		map.put("Rho", "\u03A1");
		map.put("Sigma", "\u03A3");
		map.put("Tau", "\u03A4");
		map.put("Upsilon", "\u03A5");
		map.put("Phi", "\u03A6");
		map.put("Chi", "\u03A7");
		map.put("Psi", "\u03A8");
		map.put("Omega", "\u03A9");
		map.put("alpha", "\u03B1");
		map.put("beta", "\u03B2");
		map.put("gamma", "\u03B3");
		map.put("delta", "\u03B4");
		map.put("epsilon", "\u03B5");
		map.put("zeta", "\u03B6");
		map.put("eta", "\u03B7");
		map.put("theta", "\u03B8");
		map.put("iota", "\u03B9");
		map.put("kappa", "\u03BA");
		map.put("lambda", "\u03BB");
		map.put("mu", "\u03BC");
		map.put("nu", "\u03BD");
		map.put("xi", "\u03BE");
		map.put("omicron", "\u03BF");
		map.put("pi", "\u03C0");
		map.put("rho", "\u03C1");
		map.put("sigmaf", "\u03C2");
		map.put("sigma", "\u03C3");
		map.put("tau", "\u03C4");
		map.put("upsilon", "\u03C5");
		map.put("phi", "\u03C6");
		map.put("chi", "\u03C7");
		map.put("psi", "\u03C8");
		map.put("omega", "\u03C9");
		map.put("thetasym", "\u03D1");
		map.put("upsih", "\u03D2");
		map.put("piv", "\u03D6");
		map.put("ensp", "\u2002");
		map.put("emsp", "\u2003");
		map.put("thinsp", "\u2009");
		map.put("zwnj", "\u200C");
		map.put("zwj", "\u200D");
		map.put("lrm", "\u200E");
		map.put("rlm", "\u200F");
		map.put("ndash", "\u2013");
		map.put("mdash", "\u2014");
		map.put("lsquo", "\u2018");
		map.put("rsquo", "\u2019");
		map.put("sbquo", "\u201A");
		map.put("ldquo", "\u201C");
		map.put("rdquo", "\u201D");
		map.put("bdquo", "\u201E");
		map.put("dagger", "\u2020");
		map.put("Dagger", "\u2021");
		map.put("bull", "\u2022");
		map.put("hellip", "\u2026");
		map.put("permil", "\u2030");
		map.put("prime", "\u2032");
		map.put("Prime", "\u2033");
		map.put("lsaquo", "\u2039");
		map.put("rsaquo", "\u203A");
		map.put("oline", "\u203E");
		map.put("frasl", "\u2044");
		map.put("euro", "\u20AC");
		map.put("image", "\u2111");
		map.put("weierp", "\u2118");
		map.put("real", "\u211C");
		map.put("trade", "\u2122");
		map.put("alefsym", "\u2135");
		map.put("larr", "\u2190");
		map.put("uarr", "\u2191");
		map.put("rarr", "\u2192");
		map.put("darr", "\u2193");
		map.put("harr", "\u2194");
		map.put("crarr", "\u21B5");
		map.put("lArr", "\u21D0");
		map.put("uArr", "\u21D1");
		map.put("rArr", "\u21D2");
		map.put("dArr", "\u21D3");
		map.put("hArr", "\u21D4");
		map.put("forall", "\u2200");
		map.put("part", "\u2202");
		map.put("exist", "\u2203");
		map.put("empty", "\u2205");
		map.put("nabla", "\u2207");
		map.put("isin", "\u2208");
		map.put("notin", "\u2209");
		map.put("ni", "\u220B");
		map.put("prod", "\u220F");
		map.put("sum", "\u2211");
		map.put("minus", "\u2212");
		map.put("lowast", "\u2217");
		map.put("radic", "\u221A");
		map.put("prop", "\u221D");
		map.put("infin", "\u221E");
		map.put("ang", "\u2220");
		map.put("and", "\u2227");
		map.put("or", "\u2228");
		map.put("cap", "\u2229");
		map.put("cup", "\u222A");
		map.put("int", "\u222B");
		map.put("there4", "\u2234");
		map.put("sim", "\u223C");
		map.put("cong", "\u2245");
		map.put("asymp", "\u2248");
		map.put("ne", "\u2260");
		map.put("equiv", "\u2261");
		map.put("le", "\u2264");
		map.put("ge", "\u2265");
		map.put("sub", "\u2282");
		map.put("sup", "\u2283");
		map.put("nsub", "\u2284");
		map.put("sube", "\u2286");
		map.put("supe", "\u2287");
		map.put("oplus", "\u2295");
		map.put("otimes", "\u2297");
		map.put("perp", "\u22A5");
		map.put("sdot", "\u22C5");
		map.put("lceil", "\u2308");
		map.put("rceil", "\u2309");
		map.put("lfloor", "\u230A");
		map.put("rfloor", "\u230B");
		map.put("lang", "\u2329");
		map.put("rang", "\u232A");
		map.put("loz", "\u25CA");
		map.put("spades", "\u2660");
		map.put("clubs", "\u2663");
		map.put("hearts", "\u2665");
		map.put("diams", "\u2666");

		CHAR_BY_BY_NAME = Collections.unmodifiableMap(map);
	}

}
