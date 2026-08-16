package com.bike_assembly.services.usuario;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.AutowiredFieldValueResolver;
import org.springframework.beans.factory.support.RegisteredBean;

/**
 * Autowiring for {@link SuperAdminServiceImpl}.
 */
@Generated
public class SuperAdminServiceImpl__Autowiring {
  /**
   * Apply the autowiring.
   */
  public static SuperAdminServiceImpl apply(RegisteredBean registeredBean,
      SuperAdminServiceImpl instance) {
    AutowiredFieldValueResolver.forRequiredField("usuarioDao").resolveAndSet(registeredBean, instance);
    return instance;
  }
}
