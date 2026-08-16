package com.bike_assembly.web;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.InstanceSupplier;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link ProveedorController}.
 */
@Generated
public class ProveedorController__BeanDefinitions {
  /**
   * Get the bean definition for 'proveedorController'.
   */
  public static BeanDefinition getProveedorControllerBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(ProveedorController.class);
    InstanceSupplier<ProveedorController> instanceSupplier = InstanceSupplier.using(ProveedorController::new);
    instanceSupplier = instanceSupplier.andThen(ProveedorController__Autowiring::apply);
    beanDefinition.setInstanceSupplier(instanceSupplier);
    return beanDefinition;
  }
}
