package com.bike_assembly.services.SuperAdmin;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.InstanceSupplier;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link SuperAdminService}.
 */
@Generated
public class SuperAdminService__BeanDefinitions {
  /**
   * Get the bean definition for 'superAdminService'.
   */
  public static BeanDefinition getSuperAdminServiceBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(SuperAdminService.class);
    InstanceSupplier<SuperAdminService> instanceSupplier = InstanceSupplier.using(SuperAdminService::new);
    instanceSupplier = instanceSupplier.andThen(SuperAdminService__Autowiring::apply);
    beanDefinition.setInstanceSupplier(instanceSupplier);
    return beanDefinition;
  }
}
