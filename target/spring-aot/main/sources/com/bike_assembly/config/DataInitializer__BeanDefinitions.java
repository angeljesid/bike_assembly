package com.bike_assembly.config;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.InstanceSupplier;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link DataInitializer}.
 */
@Generated
public class DataInitializer__BeanDefinitions {
  /**
   * Get the bean definition for 'dataInitializer'.
   */
  public static BeanDefinition getDataInitializerBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(DataInitializer.class);
    InstanceSupplier<DataInitializer> instanceSupplier = InstanceSupplier.using(DataInitializer::new);
    instanceSupplier = instanceSupplier.andThen(DataInitializer__Autowiring::apply);
    beanDefinition.setInstanceSupplier(instanceSupplier);
    return beanDefinition;
  }
}
