package com.bike_assembly.controller;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.InstanceSupplier;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link ClienteController}.
 */
@Generated
public class ClienteController__BeanDefinitions {
  /**
   * Get the bean definition for 'clienteController'.
   */
  public static BeanDefinition getClienteControllerBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(ClienteController.class);
    InstanceSupplier<ClienteController> instanceSupplier = InstanceSupplier.using(ClienteController::new);
    instanceSupplier = instanceSupplier.andThen(ClienteController__Autowiring::apply);
    beanDefinition.setInstanceSupplier(instanceSupplier);
    return beanDefinition;
  }
}
