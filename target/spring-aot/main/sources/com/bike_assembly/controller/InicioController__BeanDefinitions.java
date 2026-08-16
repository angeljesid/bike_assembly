package com.bike_assembly.controller;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link InicioController}.
 */
@Generated
public class InicioController__BeanDefinitions {
  /**
   * Get the bean definition for 'inicioController'.
   */
  public static BeanDefinition getInicioControllerBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(InicioController.class);
    beanDefinition.setInstanceSupplier(InicioController::new);
    return beanDefinition;
  }
}
