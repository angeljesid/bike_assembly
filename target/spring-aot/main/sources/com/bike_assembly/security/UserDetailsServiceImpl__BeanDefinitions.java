package com.bike_assembly.security;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.InstanceSupplier;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link UserDetailsServiceImpl}.
 */
@Generated
public class UserDetailsServiceImpl__BeanDefinitions {
  /**
   * Get the bean definition for 'userDetailsServiceImpl'.
   */
  public static BeanDefinition getUserDetailsServiceImplBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(UserDetailsServiceImpl.class);
    InstanceSupplier<UserDetailsServiceImpl> instanceSupplier = InstanceSupplier.using(UserDetailsServiceImpl::new);
    instanceSupplier = instanceSupplier.andThen(UserDetailsServiceImpl__Autowiring::apply);
    beanDefinition.setInstanceSupplier(instanceSupplier);
    return beanDefinition;
  }
}
