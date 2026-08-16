package com.bike_assembly.web;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.InstanceSupplier;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link SuperAdminController}.
 */
@Generated
public class SuperAdminController__BeanDefinitions {
  /**
   * Get the bean definition for 'superAdminController'.
   */
  public static BeanDefinition getSuperAdminControllerBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(SuperAdminController.class);
    InstanceSupplier<SuperAdminController> instanceSupplier = InstanceSupplier.using(SuperAdminController::new);
    instanceSupplier = instanceSupplier.andThen(SuperAdminController__Autowiring::apply);
    beanDefinition.setInstanceSupplier(instanceSupplier);
    return beanDefinition;
  }
}
