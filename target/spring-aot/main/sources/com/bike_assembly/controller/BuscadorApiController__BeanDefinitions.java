package com.bike_assembly.controller;

import com.bike_assembly.repository.ProductoDao;
import com.bike_assembly.repository.ProveedorDao;
import org.springframework.aot.generate.Generated;
import org.springframework.beans.factory.aot.BeanInstanceSupplier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;

/**
 * Bean definitions for {@link BuscadorApiController}.
 */
@Generated
public class BuscadorApiController__BeanDefinitions {
  /**
   * Get the bean instance supplier for 'buscadorApiController'.
   */
  private static BeanInstanceSupplier<BuscadorApiController> getBuscadorApiControllerInstanceSupplier(
      ) {
    return BeanInstanceSupplier.<BuscadorApiController>forConstructor(ProductoDao.class, ProveedorDao.class)
            .withGenerator((registeredBean, args) -> new BuscadorApiController(args.get(0), args.get(1)));
  }

  /**
   * Get the bean definition for 'buscadorApiController'.
   */
  public static BeanDefinition getBuscadorApiControllerBeanDefinition() {
    RootBeanDefinition beanDefinition = new RootBeanDefinition(BuscadorApiController.class);
    beanDefinition.setInstanceSupplier(getBuscadorApiControllerInstanceSupplier());
    return beanDefinition;
  }
}
