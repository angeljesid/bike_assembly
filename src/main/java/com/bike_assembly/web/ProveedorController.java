package com.bike_assembly.web;

import com.bike_assembly.domain.Producto;
import com.bike_assembly.domain.Proveedor;
import com.bike_assembly.domain.Usuario;
import com.bike_assembly.repository.ProductoDao;
import com.bike_assembly.repository.UsuarioDao;
import com.bike_assembly.services.proveedor.IProveedorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/proveedor")
public class ProveedorController {

    @Autowired
    private IProveedorService proveedorService;

    @Autowired
    private UsuarioDao usuarioDao;

    @Autowired
    private ProductoDao productoDao;

    @GetMapping({"", "/"})
    public String listarProveedores(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario admin = usuarioDao.findByUsernameOrEmail(auth.getName(), auth.getName()).orElse(null);

        if (admin != null && admin.getOrganizacion() != null) {
            List<Proveedor> proveedores = proveedorService.listarProveedoresPorOrganizacion(admin.getOrganizacion());
            model.addAttribute("proveedores", proveedores);
        } else {
            model.addAttribute("proveedores", List.of());
        }
        return "admin/proveedor";
    }

    @GetMapping("/nuevo")
    public String nuevoProveedor(Model model) {
        model.addAttribute("proveedor", new Proveedor());
        return "admin/formulario-proveedor";
    }

    @GetMapping("/editar/{id}")
    public String editarProveedor(@PathVariable("id") Long id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario admin = usuarioDao.findByUsernameOrEmail(auth.getName(), auth.getName()).orElse(null);

        Proveedor proveedor = new Proveedor();
        proveedor.setIdProveedor(id);
        Proveedor proveedorExistente = proveedorService.encontrarProveedor(proveedor);

        if (admin == null || admin.getOrganizacion() == null || proveedorExistente == null || proveedorExistente.getOrganizacion() == null || !proveedorExistente.getOrganizacion().getIdOrganizacion().equals(admin.getOrganizacion().getIdOrganizacion())) {
            return "redirect:/admin/proveedor";
        }

        model.addAttribute("proveedor", proveedorExistente);

        List<Producto> productos = proveedorExistente.getProductos();
        if (productos != null && !productos.isEmpty()) {
            Map<LocalDate, List<Producto>> productosAgrupados = productos.stream()
                .collect(Collectors.groupingBy(
                    p -> p.getFechaIngreso() != null ? p.getFechaIngreso() : LocalDate.now(),
                    TreeMap::new,
                    Collectors.toList()
                ));
            
            Map<LocalDate, List<Producto>> agrupadosDesc = new TreeMap<>(Collections.reverseOrder());
            agrupadosDesc.putAll(productosAgrupados);
            model.addAttribute("historialSuministros", agrupadosDesc);
        }

        model.addAttribute("nuevoProducto", new Producto());

        return "admin/formulario-proveedor";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarProveedor(@PathVariable("id") Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario admin = usuarioDao.findByUsernameOrEmail(auth.getName(), auth.getName()).orElse(null);

        Proveedor proveedor = new Proveedor();
        proveedor.setIdProveedor(id);
        Proveedor proveedorExistente = proveedorService.encontrarProveedor(proveedor);

        if (admin != null && admin.getOrganizacion() != null && proveedorExistente != null && proveedorExistente.getOrganizacion() != null && proveedorExistente.getOrganizacion().getIdOrganizacion().equals(admin.getOrganizacion().getIdOrganizacion())) {
            proveedorService.eliminar(proveedorExistente);
        }

        return "redirect:/admin/proveedor";
    }

    @PostMapping("/guardar")
    public String guardarProveedor(Proveedor proveedor) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario admin = usuarioDao.findByUsernameOrEmail(auth.getName(), auth.getName()).orElse(null);

        if (admin == null || admin.getOrganizacion() == null) {
            return "redirect:/admin/proveedor";
        }

        if (proveedor.getIdProveedor() != null) {
            Proveedor proveedorExistente = proveedorService.encontrarProveedor(proveedor);
            if (proveedorExistente != null && proveedorExistente.getOrganizacion() != null &&
                proveedorExistente.getOrganizacion().getIdOrganizacion().equals(admin.getOrganizacion().getIdOrganizacion())) {
                
                proveedorExistente.setNombre(proveedor.getNombre());
                proveedorExistente.setTipoEstablecimiento(proveedor.getTipoEstablecimiento());
                proveedorExistente.setNumero(proveedor.getNumero());
                proveedorExistente.setCorreo(proveedor.getCorreo());
                proveedorExistente.setDireccion(proveedor.getDireccion());
                
                proveedorService.guardar(proveedorExistente);
                return "redirect:/admin/proveedor/editar/" + proveedor.getIdProveedor();
            }
        } else {
            proveedor.setOrganizacion(admin.getOrganizacion());
            proveedorService.guardar(proveedor);
        }

        return "redirect:/admin/proveedor";
    }

    @PostMapping("/api/suministro")
    @ResponseBody
    public ResponseEntity<?> registrarSuministroStock(@RequestBody SuministroRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario admin = usuarioDao.findByUsernameOrEmail(auth.getName(), auth.getName()).orElse(null);
        
        if (admin == null || admin.getOrganizacion() == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "msg", "Sin autorización"));
        }

        Producto productoBase = productoDao.findById(request.getIdProducto()).orElse(null);
        Proveedor proveedor = proveedorService.encontrarProveedor(new Proveedor() {{ setIdProveedor(request.getIdProveedor()); }});

        if (productoBase == null || proveedor == null) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "msg", "Datos no encontrados"));
        }

        if (productoBase.getCondicion().equalsIgnoreCase(request.getCondicion()) && 
            productoBase.getProveedor() != null && 
            productoBase.getProveedor().getIdProveedor().equals(proveedor.getIdProveedor())) {
            
            productoBase.setStock((productoBase.getStock() != null ? productoBase.getStock() : 0) + request.getCantidad());
            productoBase.setFechaIngreso(LocalDate.now());
            productoDao.save(productoBase);
            return ResponseEntity.ok(Map.of("success", true));
        }

        List<Producto> variantes = productoDao.findByOrganizacion(admin.getOrganizacion()).stream()
            .filter(p -> p.getNombre().equalsIgnoreCase(productoBase.getNombre()) &&
                         p.getCondicion().equalsIgnoreCase(request.getCondicion()) &&
                         p.getProveedor() != null && 
                         p.getProveedor().getIdProveedor().equals(proveedor.getIdProveedor()))
            .collect(Collectors.toList());

        if (!variantes.isEmpty()) {
            Producto variante = variantes.get(0);
            variante.setStock((variante.getStock() != null ? variante.getStock() : 0) + request.getCantidad());
            variante.setFechaIngreso(LocalDate.now());
            productoDao.save(variante);
        } else {
            Producto nuevaFicha = new Producto();
            nuevaFicha.setNombre(productoBase.getNombre());
            nuevaFicha.setPrecio(productoBase.getPrecio());
            nuevaFicha.setPrecioInicial(productoBase.getPrecioInicial());
            nuevaFicha.setIva(productoBase.getIva());
            nuevaFicha.setPrecioTotal(productoBase.getPrecioTotal());
            nuevaFicha.setTipo(productoBase.getTipo());
            nuevaFicha.setStock(request.getCantidad());
            nuevaFicha.setMarca(productoBase.getMarca());
            nuevaFicha.setCategoria(productoBase.getCategoria());
            nuevaFicha.setCondicion(request.getCondicion());
            nuevaFicha.setImagen(productoBase.getImagen());
            nuevaFicha.setCodigoBarras(productoBase.getCodigoBarras());
            nuevaFicha.setProveedor(proveedor);
            nuevaFicha.setOrganizacion(admin.getOrganizacion());
            nuevaFicha.setPublicado(productoBase.getPublicado());
            nuevaFicha.setFechaIngreso(LocalDate.now());
            productoDao.save(nuevaFicha);
        }

        return ResponseEntity.ok(Map.of("success", true));
    }

    public static class SuministroRequest {
        private Long idProducto;
        private Long idProveedor;
        private Integer cantidad;
        private String condicion;

        public Long getIdProducto() { return idProducto; }
        public void setIdProducto(Long idProducto) { this.idProducto = idProducto; }
        public Long getIdProveedor() { return idProveedor; }
        public void setIdProveedor(Long idProveedor) { this.idProveedor = idProveedor; }
        public Integer getCantidad() { return cantidad; }
        public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
        public String getCondicion() { return condicion; }
        public void setCondicion(String condicion) { this.condicion = condicion; }
    }
}