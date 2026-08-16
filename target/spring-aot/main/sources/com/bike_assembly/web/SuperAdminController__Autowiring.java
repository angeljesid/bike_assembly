package com.bike_assembly.web;

import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.AutowiredFieldValueResolver;
import org.springframework.beans.factory.support.RegisteredBean;

/**
 * Autowiring for {@link SuperAdminController}.
 */
@Generated
public class SuperAdminController__Autowiring {
  /**
   * Apply the autowiring.
   */
  public static SuperAdminController apply(RegisteredBean registeredBean,
      SuperAdminController instance) {
    AutowiredFieldValueResolver.forRequiredField("superAdminService").resolveAndSet(registeredBean, instance);
    AutowiredFieldValueResolver.forRequiredField("organizacionDao").resolveAndSet(registeredBean, instance);
    return instance;
  }
}
