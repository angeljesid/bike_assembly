package com.bike_assembly.services.SuperAdmin;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.AutowiredFieldValueResolver;
import org.springframework.beans.factory.support.RegisteredBean;

/**
 * Autowiring for {@link SuperAdminService}.
 */
@Generated
public class SuperAdminService__Autowiring {
  /**
   * Apply the autowiring.
   */
  public static SuperAdminService apply(RegisteredBean registeredBean, SuperAdminService instance) {
    AutowiredFieldValueResolver.forRequiredField("organizacionDao").resolveAndSet(registeredBean, instance);
    AutowiredFieldValueResolver.forRequiredField("usuarioDao").resolveAndSet(registeredBean, instance);
    AutowiredFieldValueResolver.forRequiredField("passwordEncoder").resolveAndSet(registeredBean, instance);
    return instance;
  }
}
