/*
 * (C) Copyright 2018 Lukas Morawietz (https://github.com/F43nd1r)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.faendir.acra.gradle

import com.squareup.javapoet.FieldSpec
import com.squareup.javapoet.JavaFile
import com.squareup.javapoet.TypeSpec
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import org.gradle.plugins.ide.idea.IdeaPlugin

import java.util.regex.Matcher
import java.util.regex.Pattern

import static javax.lang.model.element.Modifier.*
/**
 * @author lukas
 * @since 05.06.18
 */
@CacheableTask
@SuppressWarnings("GroovyUnusedDeclaration")
class ThemeClassGenerator extends DefaultTask {
    @SkipWhenEmpty
    @InputDirectory
    File themesDirectory
    @OutputDirectory
    File outputDirectory

    ThemeClassGenerator() {
        project.afterEvaluate {
            project.sourceSets.main.java.srcDirs outputs.files
            project.plugins.withType(IdeaPlugin.class, { IdeaPlugin idea -> idea.model.module.generatedSourceDirs += outputs.files })
        }
    }

    @TaskAction
    void exec() {
        Pattern pattern = Pattern.compile("\\.(-?[_a-zA-Z]+[_a-zA-Z0-9-]*)")
        themesDirectory.eachDir {
            Set<String> cssClasses = new LinkedHashSet<>();
            project.fileTree(dir: it, include: "*.scss", exclude: "styles.scss").forEach {
                Matcher matcher = pattern.matcher(it.text.replaceAll("//.*\\n", "").replaceAll("/\\*.*\\*/", "").replaceAll("\".*\"", ""))
                while (matcher.find()) {
                    cssClasses.add(matcher.group(1))
                }
            }
            cssClasses.removeIf { it.startsWith("v-") }
            TypeSpec.Builder classBuilder = TypeSpec.classBuilder(it.name).addModifiers(PUBLIC, FINAL)
            classBuilder.addField(FieldSpec.builder(String, "THEME_NAME", PUBLIC, STATIC, FINAL).initializer('$S', it.name).build())
            for (String cssClass : cssClasses) {
                classBuilder.addField(FieldSpec.builder(String, cssClass.toUpperCase().replace("-", "_"), PUBLIC, STATIC, FINAL).initializer('$S', cssClass).build())
            }
            JavaFile.builder("com.vaadin.ui.themes", classBuilder.build())
                    .skipJavaLangImports(true)
                    .indent("    ")
                    .build()
                    .writeTo(outputDirectory)
        }
    }
}
