/*
 * Copyright 2016 Liam Williams <liam.williams@zoho.com>.
 *
 * This file is part of yatspec-zohhak-plugin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.theangrydev.yatspeczohhakplugin;

import com.googlecode.yatspec.junit.ParameterResolver;
import com.googlecode.yatspec.junit.Row;
import com.googlecode.yatspec.junit.VarargsParameterResolver;
import com.googlecode.zohhak.api.DefaultCoercer;
import com.googlecode.zohhak.api.backend.ConfigurationBuilder;
import com.googlecode.zohhak.api.backend.ConfigurationResolver;
import com.googlecode.zohhak.api.backend.ParameterCalculator;
import com.googlecode.zohhak.api.backend.executor.ParameterCalculatorProvider;
import io.github.theangrydev.yatspeczohhakplugin.json.JsonCollectionsParameterCoercerFactory;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;

public class ZohhakParameterResolver implements ParameterResolver {

    private static final String STRING_BOUNDARY = "'";
    private static final String PARAMETER_DELIMITER = "~";
    private static final String COLLECTION_DELIMITER = ",";
    private static final String COLLECTION_BEGIN = "[";
    private static final String COLLECTION_END = "]";

    private final VarargsParameterResolver varargsParameterResolver = new VarargsParameterResolver();
    private final ParameterCalculator parameterCalculator = new ParameterCalculatorProvider().getExecutor(new JsonCollectionsParameterCoercerFactory(), new CustomConfigurationResolver());

    @Override
    public Object[] resolveParameters(Row row, Class<?> testClass, Method testMethod) throws Exception {
        Object[] parameters = varargsParameterResolver.resolveParameters(row, testClass, testMethod);
        return parameterCalculator.calculateParameters(parametersLine(parameters), testMethod);
    }

    private String parametersLine(Object... rowParameters) {
        return stream(rowParameters).map(this::parameterToString).collect(joining(PARAMETER_DELIMITER));
    }

    private String parameterToString(Object object) {
        if (object.getClass().isArray()) {
            return streamArray(object).map(String::valueOf).collect(joining(COLLECTION_DELIMITER, COLLECTION_BEGIN, COLLECTION_END));
        } else {
            return object.toString();
        }
    }

    private Stream<Object> streamArray(Object array) {
        return IntStream.range(0, Array.getLength(array)).mapToObj(index -> Array.get(array, index));
    }

    private static class CustomConfigurationResolver implements ConfigurationResolver {

        @Override
        public ConfigurationBuilder calculateConfiguration(Method testMethod) {
            return new CustomConfiguration();
        }
    }

    private static class CustomConfiguration implements ConfigurationBuilder {

        @Override
        public List<Class<?>> getCoercers() {
            return singletonList(DefaultCoercer.class);
        }

        @Override
        public String getParameterSeparator() {
            return PARAMETER_DELIMITER;
        }

        @Override
        public String getStringBoundary() {
            return STRING_BOUNDARY;
        }
    }
}

