package io.github.theangrydev.yatspeczohhakplugin;

import com.googlecode.zohhak.api.backend.ParameterCoercer;
import com.googlecode.zohhak.api.backend.ParameterCoercerFactory;

public class DefaultParameterCoercerFactory implements ParameterCoercerFactory {

    @Override
    public ParameterCoercer parameterCoercer(ParameterCoercer defaultParameterCoercer) {
        return defaultParameterCoercer;
    }
}
