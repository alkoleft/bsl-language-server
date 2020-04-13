/*
 * This file is a part of BSL Language Server.
 *
 * Copyright © 2018-2020
 * Alexey Sosnoviy <labotamy@gmail.com>, Nikita Gryzlov <nixel2007@gmail.com> and contributors
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * BSL Language Server is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * BSL Language Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with BSL Language Server.
 */
package com.github._1c_syntax.bsl.languageserver.diagnostics;

import com.github._1c_syntax.bsl.languageserver.context.DocumentContext;
import com.github._1c_syntax.bsl.languageserver.diagnostics.metadata.DiagnosticInfo;
import com.github._1c_syntax.bsl.languageserver.diagnostics.metadata.DiagnosticMetadata;
import com.github._1c_syntax.bsl.languageserver.diagnostics.metadata.DiagnosticParameter;
import com.github._1c_syntax.bsl.languageserver.diagnostics.metadata.DiagnosticSeverity;
import com.github._1c_syntax.bsl.languageserver.diagnostics.metadata.DiagnosticTag;
import com.github._1c_syntax.bsl.languageserver.diagnostics.metadata.DiagnosticType;
import com.github._1c_syntax.bsl.languageserver.providers.CodeActionProvider;
import com.github._1c_syntax.bsl.parser.BSLLexer;
import com.github._1c_syntax.bsl.parser.BSLParser;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.Token;
import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.TextEdit;

import java.util.List;
import java.util.stream.Collectors;

@DiagnosticMetadata(
  type = DiagnosticType.CODE_SMELL,
  severity = DiagnosticSeverity.INFO,
  minutesToFix = 1,
  tags = {
    DiagnosticTag.BADPRACTICE
  }

)
public class ConsecutiveEmptyLinesDiagnostic extends AbstractDiagnostic implements QuickFixProvider {

  private static final int MAX_EMPTY_LINE_COUNT = 2;

  @DiagnosticParameter(
    type = Integer.class,
    defaultValue = "" + MAX_EMPTY_LINE_COUNT
  )
  private int maxEmptyLineCount = MAX_EMPTY_LINE_COUNT;
  private int emptyLineDelta;
  private int prevLineNumber = -1;


  public ConsecutiveEmptyLinesDiagnostic(DiagnosticInfo info) {
    super(info);
  }

  @Override
  protected void check(DocumentContext documentContext) {

    emptyLineDelta = maxEmptyLineCount + 1;

    documentContext.getTokens().stream()
      .filter(token -> token.getChannel() == Lexer.DEFAULT_TOKEN_CHANNEL
        || token.getType() == BSLLexer.LINE_COMMENT
        || token.getType() == -1) //EOF
      .map(Token::getLine)
      .distinct().forEachOrdered(this::addDiagnosticIfNeed);

  }

  private void addDiagnosticIfNeed(Integer lineNumber) {
    int realLineNumber = lineNumber - 1;
    int legalLineNumber = prevLineNumber + emptyLineDelta;
    if (legalLineNumber < realLineNumber) {
      int startLine = legalLineNumber;
      if (startLine == realLineNumber - 1) {
        startLine = legalLineNumber - 1;
      }
      int endLine = realLineNumber - 1;
      diagnosticStorage.addDiagnostic(startLine, 0, endLine, 0);
    }
    prevLineNumber = realLineNumber;
  }

  @Override
  public List<CodeAction> getQuickFixes(
    List<Diagnostic> diagnostics, CodeActionParams params, DocumentContext documentContext) {

    List<TextEdit> textEdits = diagnostics.stream()
      .map(Diagnostic::getRange)
      .map(range -> new TextEdit(
        new Range(
          range.getStart(),
          new Position(range.getEnd().getLine() + 1, range.getEnd().getCharacter())
        ),
        "")
      )
      .collect(Collectors.toList());

    return CodeActionProvider.createCodeActions(
      textEdits,
      info.getResourceString("quickFixMessage"),
      documentContext.getUri(),
      diagnostics
    );
  }
}