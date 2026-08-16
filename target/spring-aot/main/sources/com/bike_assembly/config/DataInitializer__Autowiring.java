package com.bike_assembly.config;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.AutowiredFieldValueResolver;
import org.springframework.beans.factory.support.RegisteredBean;

/**
 * Autowiring for {@link DataInitializer}.
 */
@Generated
public class DataInitializer__Autowiring {
  /**
   * Apply the autowiring.
   */
  public static DataInitializer apply(RegisteredBean registeredBean, DataInitializer instance) {
    AutowiredFieldValueResolver.forRequiredField("usuarioDao").resolveAndSet(registeredBean, instance);
    AutowiredFieldValueResolver.forRequiredField("passwordEncoder").resolveAndSet(registeredBean, instance);
    return instance;
  }
}
