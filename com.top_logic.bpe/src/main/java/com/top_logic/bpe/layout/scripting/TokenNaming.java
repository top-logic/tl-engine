/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.layout.scripting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.bpe.bpml.model.Node;
import com.top_logic.bpe.bpml.model.Participant;
import com.top_logic.bpe.bpml.model.Process;
import com.top_logic.bpe.execution.model.ProcessExecution;
import com.top_logic.bpe.execution.model.Token;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.layout.scripting.recorder.ref.AbstractModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.recorder.ref.ModelNamingScheme;
import com.top_logic.layout.scripting.recorder.ref.ui.BreadcrumbStrings;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;
import com.top_logic.model.TLClass;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link ModelNamingScheme} for {@link Token}s.
 * 
 * @author <a href="mailto:fma@top-logic.com">fma</a>
 */
public class TokenNaming extends AbstractModelNamingScheme<Token, TokenNaming.TokenName> {

	/**
	 * {@link ModelName} of the {@link BPMLObjectNaming}.
	 */
	public interface TokenName extends ModelName {

		/**
		 * The path to a token as "Breadcrumb String".
		 * 
		 * @see BreadcrumbStrings
		 */
		@Format(BreadcrumbStrings.class)
		List<String> getTokenPath();

		/** @see #getTokenPath() */
		void setTokenPath(List<String> value);

		/**
		 * Token ID as computed by the "dynamic-name" script in the {@link Participant}.
		 * 
		 * @see Participant#getDynamicName()
		 */
		@Nullable
		String getTokenId();

		/**
		 * @see #getTokenId()
		 */
		void setTokenId(String id);

	}

	@Override
	protected void initName(TokenName name, Token token) {
		name.setTokenId(getTokenId(token));

		List<String> tokenPath = new ArrayList<>();
		BPMLObjectNaming.buildPath(tokenPath, token.getNode());
		name.setTokenPath(tokenPath);
	}

	private String getTokenId(Token token) {
		Process process = token.getNode().getProcess();
		Participant participant = process.getParticipant();
		SearchExpression dynamicName = participant.getDynamicName();
		if (dynamicName == null) {
			return null;
		}
		QueryExecutor function = QueryExecutor.compile(dynamicName);
		return (String) function.execute(token);
	}

	@Override
	public Token locateModel(ActionContext context, TokenName name) {
		List<String> tokenPath = name.getTokenPath();
		Node node = (Node) BPMLObjectNaming.resolvePath(name, tokenPath, null);
		Process process = node.getProcess();
		Participant participant = process.getParticipant();
		SearchExpression dynamicName = participant.getDynamicName();
		String tokenId = name.getTokenId();

		Predicate<Token> matcher;
		if (tokenId == null || dynamicName == null) {
			matcher = t -> t.getNode() == node;
		} else {
			QueryExecutor function = QueryExecutor.compile(dynamicName);
			matcher = t -> t.getNode() == node && tokenId.equals(function.execute(t.getProcessExecution()));
		}

		TLClass modelType = (TLClass) node.getProcess().getParticipant().getModelType();
		Collection<? extends ProcessExecution> allExecutions =
			MetaElementUtil.getAllDirectInstancesOf(modelType, ProcessExecution.class);

		Token result = null;
		for (ProcessExecution execution : allExecutions) {
			Collection<? extends Token> activeTokens = execution.getActiveTokens();
			for (Token token : activeTokens) {
				if (matcher.test(token)) {
					if (result != null) {
						String message = "Ambiguous match for '" + name + "': " + result + " vs. " + token + ".";
						if (dynamicName == null) {
							message += " Consider setting the test ID property for the participant.";
						}

						throw ApplicationAssertions.fail(name, message);
					}
					result = token;
				}
			}
		}

		return result;
	}

	@Override
	public Class<? extends TokenName> getNameClass() {
		return TokenName.class;
	}

	@Override
	public Class<Token> getModelClass() {
		return Token.class;
	}

}
