package com.bike_assembly.services.usuario;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.InstanceSupplier;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link SuperAdminServiceImpl}.
 */
@Generated
public class SuperAdminServiceImpl__BeanDefinitions {
  /**
   * Get the bean definition for 'superAdminServiceImpl'.
   */
  public static BeanDefinition getSuperAdminServiceImplBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(SuperAdminServiceImpl.class);
    InstanceSupplier<SuperAdminServiceImpl> instanceSupplier = InstanceSupplier.using(SuperAdminServiceImpl::new);
    instanceSupplier = instanceSupplier.andThen(SuperAdminServiceImpl__Autowiring::apply);
    beanDefinition.setInstanceSupplier(instanceSupplier);
    return beanDefinition;
  }
}
