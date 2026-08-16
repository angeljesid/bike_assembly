package com.bike_assembly.web;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.AutowiredFieldValueResolver;
import org.springframework.beans.factory.support.RegisteredBean;

/**
 * Autowiring for {@link RegistroController}.
 */
@Generated
public class RegistroController__Autowiring {
  /**
   * Apply the autowiring.
   */
  public static RegistroController apply(RegisteredBean registeredBean,
      RegistroController instance) {
    AutowiredFieldValueResolver.forRequiredField("usuarioDao").resolveAndSet(registeredBean, instance);
    AutowiredFieldValueResolver.forRequiredField("passwordEncoder").resolveAndSet(registeredBean, instance);
    return instance;
  }
}
