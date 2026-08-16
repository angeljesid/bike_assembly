package com.bike_assembly.web;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.InstanceSupplier;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link RegistroController}.
 */
@Generated
public class RegistroController__BeanDefinitions {
  /**
   * Get the bean definition for 'registroController'.
   */
  public static BeanDefinition getRegistroControllerBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(RegistroController.class);
    InstanceSupplier<RegistroController> instanceSupplier = InstanceSupplier.using(RegistroController::new);
    instanceSupplier = instanceSupplier.andThen(RegistroController__Autowiring::apply);
    beanDefinition.setInstanceSupplier(instanceSupplier);
    return beanDefinition;
  }
}
