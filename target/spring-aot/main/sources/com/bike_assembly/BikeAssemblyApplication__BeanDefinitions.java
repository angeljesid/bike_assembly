package com.bike_assembly;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link BikeAssemblyApplication}.
 */
@Generated
public class BikeAssemblyApplication__BeanDefinitions {
  /**
   * Get the bean definition for 'bikeAssemblyApplication'.
   */
  public static BeanDefinition getBikeAssemblyApplicationBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(BikeAssemblyApplication.class);
    beanDefinition.setInstanceSupplier(BikeAssemblyApplication::new);
    return beanDefinition;
  }
}
