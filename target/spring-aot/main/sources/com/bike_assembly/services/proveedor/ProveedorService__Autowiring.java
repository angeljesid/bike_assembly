package com.bike_assembly.services.proveedor;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.AutowiredFieldValueResolver;
import org.springframework.beans.factory.support.RegisteredBean;

/**
 * Autowiring for {@link ProveedorService}.
 */
@Generated
public class ProveedorService__Autowiring {
  /**
   * Apply the autowiring.
   */
  public static ProveedorService apply(RegisteredBean registeredBean, ProveedorService instance) {
    AutowiredFieldValueResolver.forRequiredField("proveedorDao").resolveAndSet(registeredBean, instance);
    return instance;
  }
}
