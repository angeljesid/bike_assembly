package com.bike_assembly.security;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.AutowiredFieldValueResolver;
import org.springframework.beans.factory.support.RegisteredBean;

/**
 * Autowiring for {@link UserDetailsServiceImpl}.
 */
@Generated
public class UserDetailsServiceImpl__Autowiring {
  /**
   * Apply the autowiring.
   */
  public static UserDetailsServiceImpl apply(RegisteredBean registeredBean,
      UserDetailsServiceImpl instance) {
    AutowiredFieldValueResolver.forRequiredField("usuarioDao").resolveAndSet(registeredBean, instance);
    return instance;
  }
}
