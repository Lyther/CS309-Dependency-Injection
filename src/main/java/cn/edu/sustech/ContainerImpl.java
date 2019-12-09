package cn.edu.sustech;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

public class ContainerImpl implements Container {

    private Class container;
    private Class containerImpl;

    @Override
    public <T> void register(Class<T> serviceType) {
        if (serviceType == null ||
                Modifier.isAbstract(serviceType.getModifiers()) ||
                serviceType.isInterface() ||
                serviceType.getConstructors().length != 1)
            throw new IllegalArgumentException();
        this.container = serviceType;
    }

    @Override
    public <T> void register(Class<T> serviceType, Class<? extends T> implementationType) {
        if (serviceType == null ||
                implementationType == null ||
                Modifier.isAbstract(implementationType.getModifiers()) ||
                implementationType.isInterface())
            throw new IllegalArgumentException();
        this.container = serviceType;
        this.containerImpl = implementationType;
    }

    @Override
    public <T> T resolve(Class<T> serviceType) {
        if (serviceType == null) throw new IllegalArgumentException();
        try {
            return (T) this.containerImpl.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            return null;
        }
    }
}
