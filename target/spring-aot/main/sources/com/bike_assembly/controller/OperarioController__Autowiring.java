package com.bike_assembly.controller;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.AutowiredFieldValueResolver;
import org.springframework.beans.factory.support.RegisteredBean;

/**
 * Autowiring for {@link OperarioController}.
 */
@Generated
public class OperarioController__Autowiring {
  /**
   * Apply the autowiring.
   */
  public static OperarioController apply(RegisteredBean registeredBean,
      OperarioController instance) {
    AutowiredFieldValueResolver.forRequiredField("usuarioDao").resolveAndSet(registeredBean, instance);
    return instance;
  }
}
