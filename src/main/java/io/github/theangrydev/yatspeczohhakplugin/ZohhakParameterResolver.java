package io.github.theangrydev.yatspeczohhakplugin;

import com.googlecode.yatspec.junit.ParameterResolver;
import com.googlecode.yatspec.junit.Row;
import com.googlecode.yatspec.junit.VarargsParameterResolver;
import com.googlecode.zohhak.api.DefaultConfiguration;
import com.googlecode.zohhak.api.backend.ParameterCalculator;
import com.googlecode.zohhak.api.backend.ParameterCalculatorProvider;
import io.github.theangrydev.yatspeczohhakplugin.json.JsonCollectionsParameterCoercerFactory;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

@SuppressWarnings("WeakerAccess")
public class ZohhakParameterResolver implements ParameterResolver {

    private static final String PARAMETER_DELIMITER = "~";
    private static final String COLLECTION_DELIMITER = ",";
    private static final String COLLECTION_BEGIN = "[";
    private static final String COLLECTION_END = "]";

    private final VarargsParameterResolver varargsParameterResolver = new VarargsParameterResolver();
    private final ParameterCalculator parameterCalculator = new ParameterCalculatorProvider().getExecutor(new JsonCollectionsParameterCoercerFactory());
    private final CustomConfiguration customConfiguration = new CustomConfiguration();

    @Override
    public Object[] resolveParameters(Row row, Class<?> testClass, Method testMethod) throws Exception {
        Object[] parameters = varargsParameterResolver.resolveParameters(row, testClass, testMethod);
        return resolveParameters(testMethod, parametersLine(parameters));
    }

    private String parametersLine(Object[] rowParameters) {
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
        return IntStream.range(0, Array.getLength(array)).mapToObj(i -> Array.get(array, i));
    }

    private Object[] resolveParameters(Method testMethod, String parametersLine) throws NoSuchMethodException {
        return parameterCalculator.calculateParameters(parametersLine, testMethod, customConfiguration);
    }

    private static class CustomConfiguration extends DefaultConfiguration {
        @Override
        public String separator() {
            return PARAMETER_DELIMITER;
        }
    }
}
