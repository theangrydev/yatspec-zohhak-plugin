package io.github.theangrydev.yatspeczohhakplugin;

import com.googlecode.zohhak.api.backend.ParameterCoercerFactory;

@SuppressWarnings("WeakerAccess")
public class ParameterCoercerFactoryFactory {
    public static final String PARAMETER_RESOLVER = "yatspec.zohhak.parameter.coercer.factory";

    public static void setParameterCoercerFactory(Class<? extends ParameterCoercerFactory> aClass) {
        System.setProperty(PARAMETER_RESOLVER, aClass.getName());
    }

    public static ParameterCoercerFactory parameterCoercerFactory() {
        try {
            Class<?> aClass = Class.forName(System.getProperty(PARAMETER_RESOLVER));
            return (ParameterCoercerFactory) aClass.newInstance();
        } catch (IllegalAccessException | InstantiationException | ClassNotFoundException e) {
            return new DefaultParameterCoercerFactory();
        }
    }
}