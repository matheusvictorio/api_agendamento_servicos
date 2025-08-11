package com.neocamp.api_agendamento.service;

import com.neocamp.api_agendamento.domain.entities.Schedule;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    public void sendConfirmationToClient(Schedule schedule) {
        String formatted = schedule.getDateTime()
                .format(DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'de' yyyy 'às' HH:mm", new Locale("pt", "BR")));

        String subject = "Seu agendamento foi confirmado!";
        String body = String.format("""
    Olá %s,

    Seu agendamento com o prestador %s foi confirmado para a data: %s

    Serviço: %s
    """,
                schedule.getClient().getName(),
                schedule.getProvider().getName(),
                formatted,
                schedule.getWork().getName()
        );
        send(schedule.getClient().getEmail(), subject, body);
    }

    public void sendCancellationToClient(Schedule schedule) {
        String formatted = schedule.getDateTime()
                .format(DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'de' yyyy 'às' HH:mm", new Locale("pt", "BR")));
        String subject = "Seu agendamento foi recusado";
        String body = String.format("""
        Olá %s,

        Infelizmente, o prestador %s recusou seu pedido de agendamento para a data: %s.

        Serviço: %s
        """,
                schedule.getClient().getName(),
                schedule.getProvider().getName(),
                formatted,
                schedule.getWork().getName()
        );
        send(schedule.getClient().getEmail(), subject, body);
    }

    private void send(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("matheusvic2016@gmail.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }


    public void sendScheduleNotification(Schedule schedule) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(schedule.getProvider().getEmail());
            helper.setSubject("Novo agendamento para você!");
            helper.setText(buildHtml(schedule), true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Erro ao enviar e-mail", e);
        }
    }

    private String buildHtml(Schedule schedule) {
        String formattedData = schedule.getDateTime()
                .format(DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'de' yyyy 'às' HH:mm", new Locale("pt", "BR")));

        return """
    <html>
    <head>
        <style>
            .button {
                display: inline-block;
                padding: 10px 20px;
                margin: 10px;
                font-size: 16px;
                color: #fff;
                background-color: #007BFF;
                border: none;
                border-radius: 5px;
                text-decoration: none;
            }
            .button.cancel {
                background-color: #dc3545;
            }
            .container {
                font-family: Arial, sans-serif;
                padding: 20px;
                background-color: #f9f9f9;
                border: 1px solid #ddd;
                border-radius: 10px;
                max-width: 600px;
                margin: auto;
            }
        </style>
    </head>
    <body>
        <div class="container">
            <h2>Olá, %s!</h2>
            <p>Você recebeu uma nova solicitação de serviço.</p>
            <p><b>Cliente:</b> %s</p>
            <p><b>Endereço:</b> %s</p>
            <p><b>Data e hora:</b> %s</p>
            <p><b>Serviço:</b> %s</p>
            <a class="button" href="http://localhost:8080/schedules/%d/confirm">Confirmar</a>
            <a class="button cancel" href="http://localhost:8080/schedules/%d/cancel">Cancelar</a>
        </div>
    </body>
    </html>
    """.formatted(
                schedule.getProvider().getName(),
                schedule.getClient().getName(),
                schedule.getAddress().toString(),
                formattedData,
                schedule.getWork().getName(),
                schedule.getId(),
                schedule.getId()
        );
    }

}
