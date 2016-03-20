package io.github.theangrydev.yatspeczohhakplugin;

import com.googlecode.zohhak.api.backend.ParameterCoercer;
import com.googlecode.zohhak.api.backend.ParameterCoercerFactory;

class JsonCollectionsParameterCoercerFactory implements ParameterCoercerFactory {

    @Override
    public ParameterCoercer parameterCoercer(ParameterCoercer defaultParameterCoercer) {
        return new JsonCollectionsParameterCoercer(defaultParameterCoercer);
    }
}
