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
import com.github._1c_syntax.mdclasses.mdo.MDObjectBase;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.eclipse.lsp4j.Diagnostic;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.github._1c_syntax.bsl.languageserver.util.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class MetadataObjectNameLengthDiagnosticTest extends AbstractDiagnosticTest<MetadataObjectNameLengthDiagnostic> {

  private static final String LONG_NAME = "ОченьДлинноеИмяОбъектаКотороеВызываетПроблемыВРаботеАТакжеОшибкиВыгрузкиКонфигурации";
  private static final String PATH_TO_METADATA = "src/test/resources/metadata";

  private MDObjectBase module;
  private DocumentContext documentContext;

  MetadataObjectNameLengthDiagnosticTest() {
    super(MetadataObjectNameLengthDiagnostic.class);
  }

  @Test
  void testConfigure() {

    Map<String, Object> configuration = diagnosticInstance.getInfo().getDefaultConfiguration();
    configuration.put("maxMetadataObjectNameLength", 10);
    diagnosticInstance.configure(configuration);

    getDocumentContextFromFile("CommonModules/ПервыйОбщийМодуль/Ext/Module.bsl", false);

    // when
    List<Diagnostic> diagnostics = diagnosticInstance.getDiagnostics(documentContext);

    //then
    assertThat(diagnostics).hasSize(1);
  }

  @Test
  void testConfigureNegative() {

    Map<String, Object> configuration = diagnosticInstance.getInfo().getDefaultConfiguration();
    configuration.put("maxMetadataObjectNameLength", 90);
    diagnosticInstance.configure(configuration);

    getDocumentContextFromFile("CommonModules/ПервыйОбщийМодуль/Ext/Module.bsl", false);

    // given
    when(module.getName()).thenReturn(LONG_NAME);

    // when
    List<Diagnostic> diagnostics = diagnosticInstance.getDiagnostics(documentContext);

    //then
    assertThat(diagnostics).hasSize(0);
  }

  @Test
  void testEmptyModule() {

    getDocumentContextFromFile("CommonModules/ПервыйОбщийМодуль/Ext/Module.bsl", true);

    // given
    when(module.getName()).thenReturn(LONG_NAME);
    when(documentContext.getMdObject()).thenReturn(Optional.of(module));

    // when
    List<Diagnostic> diagnostics = diagnosticInstance.getDiagnostics(documentContext);

    //then
    assertThat(diagnostics).hasSize(1);
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "Catalogs/Справочник1/Ext/ObjectModule.bsl",
    "Catalogs/Справочник1/Forms/ФормаВыбора/Ext/Form/Module.bsl",
    "CommonModules/ПервыйОбщийМодуль/Ext/Module.bsl"
  })
  void test(String modulePath) {

    getDocumentContextFromFile(modulePath, false);

    // given
    when(module.getName()).thenReturn(LONG_NAME);

    when(documentContext.getMdObject()).thenReturn(Optional.of(module));

    // when
    List<Diagnostic> diagnostics = diagnosticInstance.getDiagnostics(documentContext);

    //then
    assertThat(diagnostics).hasSize(1);
  }

  @Test
  void testNegative() {

    getDocumentContextFromFile("CommonModules/ПервыйОбщийМодуль/Ext/Module.bsl", false);

    // given
    when(module.getName()).thenReturn("Short");

    // when
    List<Diagnostic> diagnostics = diagnosticInstance.getDiagnostics(documentContext);

    //then
    assertThat(diagnostics).hasSize(0);
  }

  @SneakyThrows
  void getDocumentContextFromFile(String modulePath, boolean empty) {

    initServerContext(PATH_TO_METADATA);
    var testFile = new File(PATH_TO_METADATA, modulePath).getAbsoluteFile();

    documentContext = spy(new DocumentContext(
      testFile.toURI(),
      empty ? "" : FileUtils.readFileToString(testFile, StandardCharsets.UTF_8),
      context
    ));

    module = spy(Objects.requireNonNull(context).getConfiguration().getModulesByObject().get(documentContext.getUri()));
  }
}
