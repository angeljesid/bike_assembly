package com.bike_assembly.controller;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.InstanceSupplier;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link OperarioController}.
 */
@Generated
public class OperarioController__BeanDefinitions {
  /**
   * Get the bean definition for 'operarioController'.
   */
  public static BeanDefinition getOperarioControllerBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(OperarioController.class);
    InstanceSupplier<OperarioController> instanceSupplier = InstanceSupplier.using(OperarioController::new);
    instanceSupplier = instanceSupplier.andThen(OperarioController__Autowiring::apply);
    beanDefinition.setInstanceSupplier(instanceSupplier);
    return beanDefinition;
  }
}
