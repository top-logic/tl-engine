/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.search;

import java.util.Iterator;
import java.util.NoSuchElementException;

import com.top_logic.base.search.nodes.BinaryOper;
import com.top_logic.base.search.nodes.LiteralNode;
import com.top_logic.base.search.nodes.UnaryFilter;
import com.top_logic.base.search.parser.QueryInfix;
import com.top_logic.base.search.parser.QueryPrefix;
import com.top_logic.base.search.visitors.InfixVisitor;
import com.top_logic.base.search.visitors.PrefixVisitor;
import com.top_logic.util.error.TopLogicException;

/**
 * This class represents the semantics of the <i>TopLogic</i> Query Language.
 * 
 * <p>
 *  A query has to major usages:
 * </p>
 * <ul>
 *  <li>It is used to transform the String representation of the Query Language
 *      into a useable form (and back)
 *  </li>
 * </ul>
 * @author    <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
public final class Query {

    /**
	 * The {@link QueryTokenizer} breaks a {@link String} into tokens. Tokens are {@link String}s
	 * enclosed in &quot;, or separated by blanks, e.g.
	 * 
	 * <pre>
	 * My "Hello World!" string
	 * </pre>
	 * 
	 * is split into the tokens
	 * <ol>
	 * <li>My</li>
	 * <li>"Hello World!"</li>
	 * <li>string</li>
	 * </ol>
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	private static class QueryTokenizer implements Iterator<String> {

		private final String _in;

		private int _index = 0;

		private String _nextToken = null;

		/**
		 * Creates a new {@link QueryTokenizer}.
		 */
		private QueryTokenizer(String in) {
			_in = in;
		}

		@Override
		public String next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			String token = _nextToken;
			_nextToken = null;
			return token;
		}

		@Override
		public boolean hasNext() {
			if (_nextToken != null) {
				return true;
			}
			if (_index >= _in.length()) {
				return false;
			}
			return findNext();
		}

		private boolean findNext() {
			StringBuilder tmp = new StringBuilder(_in.length() - _index);
			do {
				char current = _in.charAt(_index++);
				if (tmp.length() == 0) {
					if (current == ' ') {
						// skip leading blanks.
					} else {
						tmp.append(current);
					}
				} else if (tmp.charAt(0) == '"') {
					// In phrase
					tmp.append(current);
					if (current == '"') {
						// phrase end
						_nextToken = tmp.toString();
						return true;
					}
				} else {
					switch (current) {
						case ' ': {
							_nextToken = tmp.toString();
							return true;
						}
						case '"': {
							// start of a phrase. Read " again
							_index--;
							_nextToken = tmp.toString();
							return true;
						}
						default: {
							tmp.append(current);
							break;
						}
					}

				}
			} while (_index < _in.length());

			if (tmp.length() == 0) {
				// Only white spaces
				return false;
			} else if (tmp.charAt(0) == '"') {
				throw new TopLogicException(
					I18NConstants.SEARCH_STRING_SYNTAX_ERROR_UNCLOSED_PHRASE__PHRASE_INPUT.fill(tmp, _in));
			} else {
				_nextToken = tmp.toString();
				return true;
			}
		}
	}

	/**
     * Constant for CTor indicating prefix parsing
     */
    public static final boolean INFIX = true;

    private static final String WILD_CARD = "*";

    /**
     * Root node of the parsed Expressions,
     */
    private QueryNode   root;
    
    /** 
     * Array of Attributes the result should be sorted by, may be null.
     */
    private String  orderBy[];

    /** 
     * Creates an empty Query, usefull for testing.
     */
    public Query() {
        // empty query
    }
    
    /** 
     * Creates an Query with given root and sort order.
     *<p>
     *  You may use this in case you want to constuct a Query in Place.
     *</p>
     */
    public Query(QueryNode aRoot, String[] anOrder) {
        root    = aRoot;
        orderBy = anOrder;
    }

    /**
     * The prefix notation of this Query (for internal usage).
     */
    @Override
	public String toString () {
        return new PrefixVisitor().visit(this);
    }

    /**
     * The infix notation of this Query for User interfaces.
     */
    public String toInfix () {
        return new InfixVisitor().visit(this);
    }
    
    // Accessor fucntions 
    
    /**
     *  the root node of the query.
     */
    public QueryNode getRoot() {
        return root;
    }
    
    /**
     *  Array of Attributes the resulkt should be sorted by, may be null.
     */
    public String[] getOrderBy() {
        return orderBy;
    }

    /** 
     * Construct a Query by parsing the given Expression as prefix
     */
    public static final Query parse(String expr) throws QueryException {
        return QueryPrefix.parse(expr);
    }

    /** 
     * Construct a Query by parsing the given Expression as infix.
     */
    public static final Query parseInfix(String expr) throws QueryException {
        return QueryInfix.parse(expr);
    }

    /**
     * Return a FullText query for the given parameters.
     * 
     * @param    someWords    A list of words (separated by spaces) to be used 
     *                        in the query.
     * @param    isAnd        Flag, if AND or OR should be used (
     *                        <code>true</code> for AND).
     * @return   The requested query, null in case now words where found
     */
    public static Query getFullTextQuery(String someWords, boolean isAnd, boolean exactMatch) {
        
        QueryNode       resultNode  = null;
		Iterator<String> theToken = new QueryTokenizer(someWords);
        int             kind        = isAnd ? QueryNode.AND : QueryNode.OR;
        
		while (theToken.hasNext()) {
			String term = escapeQuery(theToken.next());
            
			if (!exactMatch && !isSearchPhrase(term)) {
                if (! term.startsWith(WILD_CARD)) {
					term = WILD_CARD + term;
                }
                if (! term.endsWith(WILD_CARD)) {
					term = term + WILD_CARD;
                }
            }
            
            LiteralNode textNode = new LiteralNode(
                LiteralNode.STRING_LITERAL, (Object) term);
            UnaryFilter fullText = new UnaryFilter(QueryNode.TEXT, textNode);
            if (resultNode == null)
                resultNode = fullText;
            else
                resultNode = new BinaryOper(kind, resultNode, fullText);
        }
        if (resultNode == null)
            return null;
        return new Query(resultNode, null);
    }

	private static boolean isSearchPhrase(String term) {
		int length = term.length();
		return length > 1 && term.charAt(0) == '"' && term.charAt(length - 1) == '"';
	}

    // This is a TestString to be found by the FulltextSearch
    
	/**
	 * Escape the query according to the Lucene query standard. The following characters have to be
	 * escaped with the backslash + - && || ! ( ) { } [ ] ^ " ~ * ? : \
	 *
	 * @param aQuery
	 *        must not be null
	 * @return the escaped query
	 */
    public static String escapeQuery(String aQuery) {
        int len = aQuery.length();

        StringBuffer retval = new StringBuffer(len);
        for (int i = 0; i < len; i++) {
            char ch = aQuery.charAt(i);
            switch (ch) {
                
                case '&':
                     i++;
                     if (i < len) {
                         ch = aQuery.charAt(i);
                         if (ch == '&') {
                             retval.append("\\&&");
                         }
                         else {
                             retval.append('&');
                             retval.append(ch);
                         }
                     }
                     else {
                         retval.append(ch);
                     }
                     continue;
                
                case '|':
                     i++;
                     if (i < len) {
                         ch = aQuery.charAt(i);
                         if (ch == '|') {
                             retval.append("\\||");
                         }
                         else {
                             retval.append('|');
                             retval.append(ch);
                         }
                     }
                     else {
                         retval.append(ch);
                     }
                     continue;
                 
                case '\\':
                    retval.append("\\\\");
                    continue;

                // case '*': // allow using of '*' as Wildcard ...
                case '+':
                case '-':
                case '!':
                case '{':
                case '}':
                case '[':
                case ']':
                case '^':
                case '~':
                case '?':
                case ':':
                    retval.append("\\" + ch);
                    continue;
                  
                case '"':                  
				case '\u00c4': // Ä (not used in switch case for encoding problems)
				case '\u00e4': // ä (not used in switch case for encoding problems)
				case '\u00d6': // Ö (not used in switch case for encoding problems)
				case '\u00f6': // ö (not used in switch case for encoding problems)
				case '\u00dc': // Ü (not used in switch case for encoding problems)
				case '\u00fc': // ü (not used in switch case for encoding problems)
				case '\u00df': // ß (not used in switch case for encoding problems)
                    retval.append(ch);
                    continue;
                  
                default: // use Unicode escape if non-ASCII
                     if ((ch = aQuery.charAt(i)) < 0x20 || ch > 0x7e) {
                        String s = "0000" + Integer.toString(ch, 16);
                        retval.append("\\u" + s.substring(s.length() - 4, s.length()));
                     } else {
                        retval.append(ch);
                     }
           }
        }
        return retval.toString();
    }

}
