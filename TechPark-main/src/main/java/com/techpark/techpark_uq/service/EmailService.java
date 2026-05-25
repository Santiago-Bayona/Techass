package com.techpark.techpark_uq.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.url:http://localhost:8080}")
    private String appUrl;

    // =========================
    // CORREO DE BIENVENIDA
    // =========================
    public void enviarCorreoBienvenida(String nombre, String email) {
        try {
            Context context = new Context();
            context.setVariable("nombre", nombre);
            context.setVariable("appUrl", appUrl);
            
            String contenido = templateEngine.process("mail/bienvenida", context);
            
            enviarCorreo(email, "¡Bienvenido a TechPark UQ!", contenido);
            log.info("📧 Correo de bienvenida enviado a: {}", email);
        } catch (Exception e) {
            log.error("❌ Error al enviar correo de bienvenida a {}: {}", email, e.getMessage());
        }
    }

    // =========================
    // CORREO DE PROMOCIÓN
    // =========================
    public void enviarPromocion(String email, String nombre, String titulo, String descripcion, String codigoDescuento) {
        try {
            Context context = new Context();
            context.setVariable("nombre", nombre);
            context.setVariable("titulo", titulo);
            context.setVariable("descripcion", descripcion);
            context.setVariable("codigoDescuento", codigoDescuento);
            context.setVariable("appUrl", appUrl);
            
            String contenido = templateEngine.process("mail/promocion", context);
            
            enviarCorreo(email, "🎢 " + titulo + " - TechPark UQ", contenido);
            log.info("📧 Promoción enviada a: {}", email);
        } catch (Exception e) {
            log.error("❌ Error al enviar promoción a {}: {}", email, e.getMessage());
        }
    }

    // =========================
    // NOTIFICACIÓN DE COLA
    // =========================
    public void notificarPosicionCola(String email, String nombre, String atraccionNombre, int posicion, int tiempoEstimado) {
        try {
            Context context = new Context();
            context.setVariable("nombre", nombre);
            context.setVariable("atraccionNombre", atraccionNombre);
            context.setVariable("posicion", posicion);
            context.setVariable("tiempoEstimado", tiempoEstimado);
            
            String contenido = templateEngine.process("mail/notificacion-cola", context);
            
            enviarCorreo(email, "📢 Actualización de tu cola en " + atraccionNombre, contenido);
            log.info("📧 Notificación de cola enviada a: {}", email);
        } catch (Exception e) {
            log.error("❌ Error al enviar notificación de cola a {}: {}", email, e.getMessage());
        }
    }

    // =========================
    // NOTIFICACIÓN DE ATRACCIÓN REACTIVADA
    // =========================
    public void notificarAtraccionReactivada(String email, String nombre, String atraccionNombre) {
        try {
            Context context = new Context();
            context.setVariable("nombre", nombre);
            context.setVariable("atraccionNombre", atraccionNombre);
            context.setVariable("appUrl", appUrl);
            
            String contenido = templateEngine.process("mail/atraccion-reactivada", context);
            
            enviarCorreo(email, "🎉 ¡" + atraccionNombre + " está disponible nuevamente!", contenido);
            log.info("📧 Notificación de atracción reactivada enviada a: {}", email);
        } catch (Exception e) {
            log.error("❌ Error al enviar notificación de reactivación a {}: {}", email, e.getMessage());
        }
    }

    // =========================
    // CORREO DE RECUPERACIÓN DE CONTRASEÑA
    // =========================
    public void enviarCorreoRecuperacion(String email, String nombre, String resetUrl) {
        try {
            Context context = new Context();
            context.setVariable("nombre", nombre);
            context.setVariable("resetUrl", resetUrl);
            context.setVariable("appUrl", appUrl);
            
            String contenido = templateEngine.process("mail/recuperacion", context);
            
            enviarCorreo(email, "🔐 Recuperación de contraseña - TechPark UQ", contenido);
            log.info("📧 Correo de recuperación enviado a: {}", email);
        } catch (Exception e) {
            log.error("❌ Error al enviar correo de recuperación a {}: {}", email, e.getMessage());
        }
    }

    // =========================
    // CORREO DE CONFIRMACIÓN DE CONTRASEÑA ACTUALIZADA
    // =========================
    public void enviarCorreoPasswordActualizada(String email, String nombre) {
        try {
            Context context = new Context();
            context.setVariable("nombre", nombre);
            context.setVariable("appUrl", appUrl);
            context.setVariable("loginUrl", appUrl + "/login");
            
            String contenido = templateEngine.process("mail/password-actualizada", context);
            
            enviarCorreo(email, "✅ Tu contraseña ha sido actualizada - TechPark UQ", contenido);
            log.info("📧 Correo de confirmación de contraseña enviado a: {}", email);
        } catch (Exception e) {
            log.error("❌ Error al enviar correo de confirmación a {}: {}", email, e.getMessage());
        }
    }

    // =========================
    // CORREO MASIVO
    // =========================
    public void enviarCorreoMasivo(java.util.List<String> emails, String asunto, String titulo, String mensaje) {
        int enviados = 0;
        int errores = 0;
        
        for (String email : emails) {
            try {
                Context context = new Context();
                context.setVariable("titulo", titulo);
                context.setVariable("mensaje", mensaje);
                context.setVariable("appUrl", appUrl);
                
                String contenido = templateEngine.process("mail/comunicado-general", context);
                enviarCorreo(email, asunto, contenido);
                enviados++;
                
                // Pequeña pausa para no saturar el servidor de correo
                Thread.sleep(100);
            } catch (Exception e) {
                errores++;
                log.error("❌ Error al enviar correo masivo a {}: {}", email, e.getMessage());
            }
        }
        
        log.info("📧 Correo masivo completado. Enviados: {}, Errores: {}", enviados, errores);
    }

    // =========================
    // MÉTODO PRIVADO PARA ENVIAR CORREO
    // =========================
    private void enviarCorreo(String destinatario, String asunto, String contenidoHtml) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(contenidoHtml, true);
            
            mailSender.send(message);
            log.debug("✅ Correo enviado a: {}", destinatario);
        } catch (MessagingException e) {
            log.error("❌ Error al enviar correo a {}: {}", destinatario, e.getMessage());
            throw new RuntimeException("Error al enviar correo", e);
        }
    }
}