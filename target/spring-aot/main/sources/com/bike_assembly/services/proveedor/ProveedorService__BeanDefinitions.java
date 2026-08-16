package com.bike_assembly.services.proveedor;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.InstanceSupplier;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link ProveedorService}.
 */
@Generated
public class ProveedorService__BeanDefinitions {
  /**
   * Get the bean definition for 'proveedorService'.
   */
  public static BeanDefinition getProveedorServiceBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(ProveedorService.class);
    InstanceSupplier<ProveedorService> instanceSupplier = InstanceSupplier.using(ProveedorService::new);
    instanceSupplier = instanceSupplier.andThen(ProveedorService__Autowiring::apply);
    beanDefinition.setInstanceSupplier(instanceSupplier);
    return beanDefinition;
  }
}
