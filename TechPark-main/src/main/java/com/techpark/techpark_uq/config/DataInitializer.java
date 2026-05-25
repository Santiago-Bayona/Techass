package com.techpark.techpark_uq.config;

import com.techpark.techpark_uq.model.entity.*;
import com.techpark.techpark_uq.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ZonaRepository zonaRepository;
    private final AtraccionRepository atraccionRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("🚀 Iniciando carga de datos iniciales...");

        // Verificar si ya hay datos para no duplicar
        if (zonaRepository.count() > 0) {
            log.info("⚠️ Los datos ya fueron cargados anteriormente. Saltando inicialización.");
            return;
        }

        // 1. CREAR ZONAS
        log.info("📌 Creando zonas...");
        Zona zonaExtrema = crearZona("Zona Extrema", "Montañas rusas y emociones fuertes para adultos y jóvenes", 800, "#FF4444");
        Zona zonaAcuatica = crearZona("Zona Acuática", "Atracciones con agua para refrescarse", 600, "#4444FF");
        Zona zonaInfantil = crearZona("Zona Infantil", "Atracciones seguras para los más pequeños", 500, "#44FF44");
        Zona zonaShows = crearZona("Zona Shows", "Espectáculos y presentaciones en vivo", 1000, "#FFAA00");
        Zona zonaGastronomica = crearZona("Zona Gastronómica", "Restaurantes y puestos de comida", 400, "#AA44FF");

        // 2. CREAR ATRACCIONES
        log.info("🎢 Creando atracciones...");
        
        // Zona Extrema
        crearAtraccion("Montaña Rusa Xtreme", TipoAtraccion.MECANICA, 30, 1.40, 14, 5.0,
                0, 25, EstadoAtraccion.ACTIVA, zonaExtrema, 100, 50);
        crearAtraccion("Torre de Caída Libre", TipoAtraccion.MECANICA, 20, 1.50, 14, 4.0,
                0, 20, EstadoAtraccion.ACTIVA, zonaExtrema, 150, 80);
        crearAtraccion("Sillas Voladoras", TipoAtraccion.MECANICA, 25, 1.20, 8, 2.0,
                0, 15, EstadoAtraccion.ACTIVA, zonaExtrema, 80, 30);
        crearAtraccion("Casa del Terror", TipoAtraccion.MECANICA, 15, 1.30, 12, 3.0,
                0, 30, EstadoAtraccion.ACTIVA, zonaExtrema, 120, 100);

        // Zona Acuática
        crearAtraccion("Río Salvaje", TipoAtraccion.ACUATICA, 20, 1.20, 8, 3.0,
                0, 25, EstadoAtraccion.ACTIVA, zonaAcuatica, 200, 100);
        crearAtraccion("Tobogán Gigante", TipoAtraccion.ACUATICA, 15, 1.10, 6, 2.0,
                0, 15, EstadoAtraccion.ACTIVA, zonaAcuatica, 250, 120);
        crearAtraccion("Piscina de Olas", TipoAtraccion.ACUATICA, 50, 0.90, 4, 1.0,
                0, 30, EstadoAtraccion.ACTIVA, zonaAcuatica, 180, 150);
        crearAtraccion("Rápido del Amazonas", TipoAtraccion.ACUATICA, 25, 1.15, 10, 4.0,
                0, 20, EstadoAtraccion.ACTIVA, zonaAcuatica, 220, 80);

        // Zona Infantil
        crearAtraccion("Carritos Chocones", TipoAtraccion.INFANTIL, 15, 0.90, 4, 0.0,
                0, 10, EstadoAtraccion.ACTIVA, zonaInfantil, 50, 150);
        crearAtraccion("Carrusel Mágico", TipoAtraccion.INFANTIL, 20, 0.80, 3, 0.0,
                0, 8, EstadoAtraccion.ACTIVA, zonaInfantil, 80, 180);
        crearAtraccion("Avioncitos", TipoAtraccion.INFANTIL, 12, 0.85, 3, 0.0,
                0, 8, EstadoAtraccion.ACTIVA, zonaInfantil, 30, 120);
        crearAtraccion("Tren de la Amistad", TipoAtraccion.INFANTIL, 30, 0.75, 2, 0.0,
                0, 5, EstadoAtraccion.ACTIVA, zonaInfantil, 60, 160);

        // Zona Shows
        crearAtraccion("Show de Magia", TipoAtraccion.SHOW, 100, 0.0, 0, 5.0,
                0, 30, EstadoAtraccion.ACTIVA, zonaShows, 150, 200);
        crearAtraccion("Circo Acrobático", TipoAtraccion.SHOW, 80, 0.0, 0, 5.0,
                0, 45, EstadoAtraccion.ACTIVA, zonaShows, 200, 250);
        crearAtraccion("Cine 4D", TipoAtraccion.SHOW, 60, 0.0, 0, 3.0,
                0, 20, EstadoAtraccion.ACTIVA, zonaShows, 100, 220);
        crearAtraccion("Concierto en Vivo", TipoAtraccion.SHOW, 150, 0.0, 0, 8.0,
                0, 40, EstadoAtraccion.ACTIVA, zonaShows, 250, 200);

        // Zona Gastronómica
        crearAtraccion("Restaurante Principal", TipoAtraccion.SHOW, 80, 0.0, 0, 0.0,
                0, 15, EstadoAtraccion.ACTIVA, zonaGastronomica, 300, 300);
        crearAtraccion("Cafetería Central", TipoAtraccion.SHOW, 50, 0.0, 0, 0.0,
                0, 10, EstadoAtraccion.ACTIVA, zonaGastronomica, 280, 320);
        crearAtraccion("Heladería Polar", TipoAtraccion.SHOW, 30, 0.0, 0, 0.0,
                0, 8, EstadoAtraccion.ACTIVA, zonaGastronomica, 320, 280);

        // 3. CREAR USUARIOS
        log.info("👤 Creando usuarios...");
        
        // Administrador
        crearAdministrador("Admin TechPark", "99999999", "admin@techpark.com", "admin123", 35, 1.75);
        
        // Operadores
        crearOperador("Juan Operador", "88888888", "juan@techpark.com", "operador123", 28, 1.78, "EMP-001", "MAÑANA", zonaExtrema);
        crearOperador("Maria Operadora", "77777777", "maria@techpark.com", "operador123", 32, 1.65, "EMP-002", "TARDE", zonaAcuatica);
        crearOperador("Carlos Operador", "66666666", "carlos@techpark.com", "operador123", 30, 1.70, "EMP-003", "NOCHE", zonaInfantil);
        crearOperador("Laura Operadora", "55555555", "laura@techpark.com", "operador123", 27, 1.62, "EMP-004", "MAÑANA", zonaShows);
        
        // Visitantes
        crearVisitante("Carlos Visitante", "11111111", "carlos@email.com", "visitante123", 25, 1.75, "GENERAL", 100.0);
        crearVisitante("Ana Visitante", "22222222", "ana@email.com", "visitante123", 22, 1.65, "FAST_PASS", 150.0);
        crearVisitante("Pedro Visitante", "33333333", "pedro@email.com", "visitante123", 35, 1.80, "FAMILIAR", 80.0);
        crearVisitante("Sofia Visitante", "44444444", "sofia@email.com", "visitante123", 19, 1.68, "GENERAL", 120.0);
        crearVisitante("Luis Visitante", "12345678", "luis@email.com", "visitante123", 29, 1.72, "FAST_PASS", 200.0);
        crearVisitante("Elena Visitante", "87654321", "elena@email.com", "visitante123", 26, 1.60, "GENERAL", 90.0);
        crearVisitante("Diego Visitante", "11223344", "diego@email.com", "visitante123", 31, 1.85, "GENERAL", 110.0);
        crearVisitante("Valentina Visitante", "44332211", "valentina@email.com", "visitante123", 24, 1.70, "FAST_PASS", 130.0);

        // 4. SIMULAR ALGUNAS VISITAS HISTÓRICAS (opcional)
        log.info("📊 Datos iniciales cargados exitosamente!");
        mostrarResumen();
    }

    // ============= MÉTODOS AUXILIARES =============

    private Zona crearZona(String nombre, String descripcion, int capacidadMaxima, String color) {
        Zona zona = new Zona();
        zona.setNombre(nombre);
        zona.setDescripcion(descripcion);
        zona.setCapacidadMaxima(capacidadMaxima);
        zona.setColorRepresentativo(color);
        zona.setAforoActual(0);
        log.debug("   Zona creada: {}", nombre);
        return zonaRepository.save(zona);
    }

    private Atraccion crearAtraccion(String nombre, TipoAtraccion tipo, int capacidadMaxima,
                                      double alturaMinima, int edadMinima, double costoAdicional,
                                      int contadorVisitantes, int tiempoEspera, EstadoAtraccion estado,
                                      Zona zona, double x, double y) {
        Atraccion atraccion = new Atraccion();
        atraccion.setNombre(nombre);
        atraccion.setTipo(tipo);
        atraccion.setCapacidadMaxima(capacidadMaxima);
        atraccion.setAlturaMinima(alturaMinima);
        atraccion.setEdadMinima(edadMinima);
        atraccion.setCostoAdicional(costoAdicional);
        atraccion.setContadorVisitantes(contadorVisitantes);
        atraccion.setTiempoEsperaEstimado(tiempoEspera);
        atraccion.setEstado(estado);
        atraccion.setZona(zona);
        atraccion.setPosicionX(x);
        atraccion.setPosicionY(y);
        log.debug("   Atracción creada: {} en {}", nombre, zona.getNombre());
        return atraccionRepository.save(atraccion);
    }

    private void crearAdministrador(String nombre, String documento, String email, String rawPassword,
                                     int edad, double estatura) {
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        Usuario admin = new Usuario();
        admin.setNombre(nombre);
        admin.setDocumento(documento);
        admin.setEmail(email);
        admin.setPassword(encodedPassword);
        admin.setEdad(edad);
        admin.setEstatura(estatura);
        admin.setRol(RolUsuario.ADMINISTRADOR);
        admin.setActivo(true);
        
        usuarioRepository.save(admin);
        log.debug("   Administrador creado: {} ({})", nombre, email);
    }

    private void crearOperador(String nombre, String documento, String email, String rawPassword,
                                int edad, double estatura, String numeroEmpleado, String turno, Zona zona) {
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        Operador operador = new Operador();
        operador.setNombre(nombre);
        operador.setDocumento(documento);
        operador.setEmail(email);
        operador.setPassword(encodedPassword);
        operador.setEdad(edad);
        operador.setEstatura(estatura);
        operador.setRol(RolUsuario.OPERADOR);
        operador.setActivo(true);
        operador.setNumeroEmpleado(numeroEmpleado);
        operador.setTurno(turno);
        operador.setZonaAsignada(zona);
        
        usuarioRepository.save(operador);
        log.debug("   Operador creado: {} ({}) - Zona: {}", nombre, email, zona.getNombre());
    }

    private void crearVisitante(String nombre, String documento, String email, String rawPassword,
                                 int edad, double estatura, String tipoTicket, Double saldoInicial) {
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        Visitante visitante = new Visitante();
        visitante.setNombre(nombre);
        visitante.setDocumento(documento);
        visitante.setEmail(email);
        visitante.setPassword(encodedPassword);
        visitante.setEdad(edad);
        visitante.setEstatura(estatura);
        visitante.setRol(RolUsuario.VISITANTE);
        visitante.setActivo(true);
        visitante.setTicketActivo(tipoTicket);
        visitante.setSaldoVirtual(saldoInicial);
        visitante.setUbicacionActual("Entrada Principal");
        
        usuarioRepository.save(visitante);
        log.debug("   Visitante creado: {} ({}) - Ticket: {}", nombre, email, tipoTicket);
    }

    private void mostrarResumen() {
        log.info("📊 ========== RESUMEN DE CARGA ==========");
        log.info("   🗺️ Zonas: {}", zonaRepository.count());
        log.info("   🎢 Atracciones: {}", atraccionRepository.count());
        log.info("   👥 Usuarios: {}", usuarioRepository.count());
        log.info("      - Administradores: {}", usuarioRepository.findAll().stream().filter(u -> u.getRol() == RolUsuario.ADMINISTRADOR).count());
        log.info("      - Operadores: {}", usuarioRepository.findAll().stream().filter(u -> u.getRol() == RolUsuario.OPERADOR).count());
        log.info("      - Visitantes: {}", usuarioRepository.findAll().stream().filter(u -> u.getRol() == RolUsuario.VISITANTE).count());
        log.info("=========================================");
        log.info("");
        log.info("🔑 CREDENCIALES DE ACCESO:");
        log.info("   📧 admin@techpark.com / admin123 (Administrador)");
        log.info("   📧 juan@techpark.com / operador123 (Operador)");
        log.info("   📧 carlos@email.com / visitante123 (Visitante)");
        log.info("=========================================");
    }
}