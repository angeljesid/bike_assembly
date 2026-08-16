package com.bike_assembly.web;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.AutowiredFieldValueResolver;
import org.springframework.beans.factory.support.RegisteredBean;

/**
 * Autowiring for {@link AdminController}.
 */
@Generated
public class AdminController__Autowiring {
  /**
   * Apply the autowiring.
   */
  public static AdminController apply(RegisteredBean registeredBean, AdminController instance) {
    AutowiredFieldValueResolver.forRequiredField("proveedorService").resolveAndSet(registeredBean, instance);
    AutowiredFieldValueResolver.forRequiredField("usuarioDao").resolveAndSet(registeredBean, instance);
    AutowiredFieldValueResolver.forRequiredField("productoService").resolveAndSet(registeredBean, instance);
    return instance;
  }
}
