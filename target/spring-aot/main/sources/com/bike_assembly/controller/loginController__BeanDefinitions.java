package com.bike_assembly.controller;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link loginController}.
 */
@Generated
public class loginController__BeanDefinitions {
  /**
   * Get the bean definition for 'loginController'.
   */
  public static BeanDefinition getLoginControllerBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(loginController.class);
    beanDefinition.setInstanceSupplier(loginController::new);
    return beanDefinition;
  }
}
