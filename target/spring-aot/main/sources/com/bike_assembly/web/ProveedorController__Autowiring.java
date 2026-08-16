package com.bike_assembly.web;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.AutowiredFieldValueResolver;
import org.springframework.beans.factory.support.RegisteredBean;

/**
 * Autowiring for {@link ProveedorController}.
 */
@Generated
public class ProveedorController__Autowiring {
  /**
   * Apply the autowiring.
   */
  public static ProveedorController apply(RegisteredBean registeredBean,
      ProveedorController instance) {
    AutowiredFieldValueResolver.forRequiredField("proveedorService").resolveAndSet(registeredBean, instance);
    return instance;
  }
}
