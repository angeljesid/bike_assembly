package com.bike_assembly.services.producto;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.InstanceSupplier;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link ProductoService}.
 */
@Generated
public class ProductoService__BeanDefinitions {
  /**
   * Get the bean definition for 'productoService'.
   */
  public static BeanDefinition getProductoServiceBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(ProductoService.class);
    InstanceSupplier<ProductoService> instanceSupplier = InstanceSupplier.using(ProductoService::new);
    instanceSupplier = instanceSupplier.andThen(ProductoService__Autowiring::apply);
    beanDefinition.setInstanceSupplier(instanceSupplier);
    return beanDefinition;
  }
}
