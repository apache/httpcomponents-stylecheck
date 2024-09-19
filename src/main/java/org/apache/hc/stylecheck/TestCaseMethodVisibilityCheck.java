/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.apache.hc.stylecheck;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.puppycrawl.tools.checkstyle.StatelessCheck;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.CommonUtil;
import com.puppycrawl.tools.checkstyle.utils.TokenUtil;

@StatelessCheck
public final class TestCaseMethodVisibilityCheck extends AbstractCheck {

    private static final Set<String> TARGET_ANNOTATIONS = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "BeforeAll",
            "BeforeEach",
            "Test",
            "AfterEach",
            "AfterAll")));

    public static final String MSG_KEY = "testcase.method.visibility";

    @Override
    public int[] getDefaultTokens() {
        return getAcceptableTokens();
    }

    @Override
    public int[] getRequiredTokens() {
        return CommonUtil.EMPTY_INT_ARRAY;
    }

    @Override
    public int[] getAcceptableTokens() {
        return new int[]{
                TokenTypes.METHOD_DEF
        };
    }

    @Override
    public void visitToken(final DetailAST ast) {
        if (ast.getType() == TokenTypes.METHOD_DEF && isAnnotatedAsTest(ast)) {
            final DetailAST modifiers = ast.findFirstToken(TokenTypes.MODIFIERS);
            final DetailAST publicModifier = modifiers.findFirstToken(TokenTypes.LITERAL_PUBLIC);
            if (publicModifier != null) {
                log(publicModifier, MSG_KEY, "public");
            }
            DetailAST privateModifier = modifiers.findFirstToken(TokenTypes.LITERAL_PRIVATE);
            if (privateModifier != null) {
                log(modifiers, MSG_KEY, "private");
            }
        }
    }

    private static boolean isAnnotatedAsTest(DetailAST methodDef) {
        final DetailAST modifiers = methodDef.findFirstToken(TokenTypes.MODIFIERS);
        final Optional<DetailAST> annotation = TokenUtil.findFirstTokenByPredicate(modifiers, currentToken ->
                currentToken.getType() == TokenTypes.ANNOTATION && TARGET_ANNOTATIONS.contains(getAnnotationName(currentToken)));
        return annotation.isPresent();
    }

    private static String getAnnotationName(DetailAST annotation) {
        final DetailAST dotAst = annotation.findFirstToken(TokenTypes.DOT);
        if (dotAst == null) {
            return annotation.findFirstToken(TokenTypes.IDENT).getText();
        } else {
            return dotAst.findFirstToken(TokenTypes.IDENT).getText();
        }
    }

}
