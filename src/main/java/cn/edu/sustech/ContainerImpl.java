package cn.edu.sustech;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContainerImpl implements Container {

    private List<Class> container = new ArrayList<>();
    private Map<Class, Class> containerImpl = new HashMap<>();

    @Override
    public <T> void register(Class<T> serviceType) {
        if (serviceType == null ||
                Modifier.isAbstract(serviceType.getModifiers()) ||
                serviceType.isInterface() ||
                serviceType.getConstructors().length != 1)
            throw new IllegalArgumentException();
        container.add(serviceType);
    }

    @Override
    public <T> void register(Class<T> serviceType, Class<? extends T> implementationType) {
        if (serviceType == null ||
                implementationType == null ||
                Modifier.isAbstract(implementationType.getModifiers()) ||
                implementationType.isInterface())
            throw new IllegalArgumentException();
        container.add(serviceType);
        containerImpl.put(serviceType, implementationType);
    }

    @Override
    public <T> T resolve(Class<T> serviceType) {
        if (serviceType == null) throw new IllegalArgumentException();
        if (!container.contains(serviceType)) throw new ServiceNotFoundException();

        try {
            Field[] fields = serviceType.getDeclaredFields();
            Constructor[] constructors = containerImpl.containsKey(serviceType) ? containerImpl.get(serviceType).getConstructors() : serviceType.getConstructors();

            // create instance directly from constructor
            Class[] parameterTypes = constructors[0].getParameterTypes();
            List<Object> parameters = new ArrayList<>();
            for (Class parameter : parameterTypes) {
                if (!container.contains(parameter)) throw new ServiceNotFoundException();
                for (Class instance : container)
                    if (instance.getTypeName().equals(parameter.getTypeName())) {
                        parameters.add(resolve(instance));  // Recursively resolve instance
                        break;
                    }
            }

            // create instance without field values
            T result = (T) constructors[0].newInstance(parameters.toArray());

            // set field value according to @Inject annotation
            for (Field field : fields) {
                Annotation annotation = field.getAnnotation(Inject.class);
                if (annotation != null) {
                    if (container.contains(field.getType())) {
                        for (Class instance : container)
                            if (instance == field.getType()) {
                                field.setAccessible(true);
                                field.set(result, resolve(instance));   // recursively resolve field instance
                                break;
                            }
                    } else
                        throw new ServiceNotFoundException();
                }
            }

            return result;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }
}
