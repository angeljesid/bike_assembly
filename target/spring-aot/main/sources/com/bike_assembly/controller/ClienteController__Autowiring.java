package com.bike_assembly.controller;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.AutowiredFieldValueResolver;
import org.springframework.beans.factory.support.RegisteredBean;

/**
 * Autowiring for {@link ClienteController}.
 */
@Generated
public class ClienteController__Autowiring {
  /**
   * Apply the autowiring.
   */
  public static ClienteController apply(RegisteredBean registeredBean, ClienteController instance) {
    AutowiredFieldValueResolver.forRequiredField("usuarioDao").resolveAndSet(registeredBean, instance);
    return instance;
  }
}
