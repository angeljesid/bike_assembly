package com.bike_assembly.security;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.AutowiredFieldValueResolver;
import org.springframework.beans.factory.support.RegisteredBean;

/**
 * Autowiring for {@link SecurityConfig}.
 */
@Generated
public class SecurityConfig__Autowiring {
  /**
   * Apply the autowiring.
   */
  public static SecurityConfig apply(RegisteredBean registeredBean, SecurityConfig instance) {
    AutowiredFieldValueResolver.forRequiredField("successHandler").resolveAndSet(registeredBean, instance);
    return instance;
  }
}
