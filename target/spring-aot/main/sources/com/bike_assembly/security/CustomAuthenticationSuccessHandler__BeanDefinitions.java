package com.bike_assembly.security;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link CustomAuthenticationSuccessHandler}.
 */
@Generated
public class CustomAuthenticationSuccessHandler__BeanDefinitions {
  /**
   * Get the bean definition for 'customAuthenticationSuccessHandler'.
   */
  public static BeanDefinition getCustomAuthenticationSuccessHandlerBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(CustomAuthenticationSuccessHandler.class);
    beanDefinition.setInstanceSupplier(CustomAuthenticationSuccessHandler::new);
    return beanDefinition;
  }
}
