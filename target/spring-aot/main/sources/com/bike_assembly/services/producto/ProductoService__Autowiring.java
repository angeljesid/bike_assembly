package com.bike_assembly.services.producto;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.AutowiredFieldValueResolver;
import org.springframework.beans.factory.support.RegisteredBean;

/**
 * Autowiring for {@link ProductoService}.
 */
@Generated
public class ProductoService__Autowiring {
  /**
   * Apply the autowiring.
   */
  public static ProductoService apply(RegisteredBean registeredBean, ProductoService instance) {
    AutowiredFieldValueResolver.forRequiredField("productoDao").resolveAndSet(registeredBean, instance);
    return instance;
  }
}
