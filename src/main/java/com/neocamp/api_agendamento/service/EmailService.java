package com.neocamp.api_agendamento.service;

import com.neocamp.api_agendamento.domain.entities.Schedule;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class EmailService {
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    private static final String BASE_URL = "http://localhost:8080";

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
        sendText(schedule.getClient().getEmail(), subject, body);
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
        sendText(schedule.getClient().getEmail(), subject, body);
    }

    public void sendScheduleNotification(Schedule schedule) {
        String html = buildHtml(schedule);
        sendHtml(schedule.getProvider().getEmail(), "Novo agendamento para você!", html);
    }

    public void sendReminderToClient(Schedule schedule) {
        String formatted = schedule.getDateTime()
                .format(DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'de' yyyy 'às' HH:mm", new Locale("pt", "BR")));
        String subject = "Lembrete de agendamento";
        String body = String.format("""
                Olá %s,

                Este é um lembrete do seu agendamento com o prestador %s para a data: %s
                Serviço: %s
                """,
                schedule.getClient().getName(),
                schedule.getProvider().getName(),
                formatted,
                schedule.getWork().getName());
        sendText(schedule.getClient().getEmail(), subject, body);
    }

    public void sendReminderToProvider(Schedule schedule) {
        String formatted = schedule.getDateTime()
                .format(DateTimeFormatter.ofPattern("EEEE, dd 'de' MMMM 'de' yyyy 'às' HH:mm", new Locale("pt", "BR")));
        String subject = "Lembrete de atendimento";
        String body = String.format("""
                Olá %s,

                Este é um lembrete do atendimento agendado com o cliente %s para a data: %s
                Serviço: %s
                """,
                schedule.getProvider().getName(),
                schedule.getClient().getName(),
                formatted,
                schedule.getWork().getName());
        sendText(schedule.getProvider().getEmail(), subject, body);
    }

    public void sendFollowUpToClientWithRating(Schedule schedule) {
        String subject = "Acompanhamento do serviço - Avalie o atendimento";
        String html = String.format("""
        <html>
        <body>
            <h2>Olá %s,</h2>
            <p>Seu serviço com o prestador <b>%s</b> foi concluído! Por favor, avalie:</p>
            %s
            <p>Serviço: %s</p>
        </body>
        </html>
        """,
                schedule.getClient().getName(),
                schedule.getProvider().getName(),
                ratingLinks(schedule.getId(), "provider"),
                schedule.getWork().getName()
        );
        sendHtml(schedule.getClient().getEmail(), subject, html);
    }

    public void sendFollowUpToProviderWithRating(Schedule schedule) {
        String subject = "Acompanhamento do atendimento - Avalie o cliente";
        String html = String.format("""
        <html>
        <body>
            <h2>Olá %s,</h2>
            <p>O serviço para o cliente <b>%s</b> foi concluído! Por favor, avalie:</p>
            %s
            <p>Serviço: %s</p>
        </body>
        </html>
        """,
                schedule.getProvider().getName(),
                schedule.getClient().getName(),
                ratingLinks(schedule.getId(), "client"),
                schedule.getWork().getName()
        );
        sendHtml(schedule.getProvider().getEmail(), subject, html);
    }


    private void sendText(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("matheusvic2016@gmail.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    private void sendHtml(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("matheusvic2016@gmail.com");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Erro ao enviar e-mail HTML", e);
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
                        <a class="button" href="%s/schedules/%d/confirm">Confirmar</a>
                        <a class="button cancel" href="%s/schedules/%d/cancel">Cancelar</a>
                    </div>
                </body>
                </html>
                """.formatted(
                schedule.getProvider().getName(),
                schedule.getClient().getName(),
                schedule.getAddress().toString(),
                formattedData,
                schedule.getWork().getName(),
                BASE_URL, schedule.getId(),
                BASE_URL, schedule.getId()
        );
    }

    private String ratingLinks(Long scheduleId, String type) {
        StringBuilder sb = new StringBuilder("<p>");
        for (int i = 5; i >= 1; i--) {
            sb.append(String.format(
                    "<a href=\"%s/schedules/rate?scheduleId=%d&type=%s&rating=%d\">⭐ %d</a> ",
                    BASE_URL, scheduleId, type, i, i
            ));
        }
        sb.append("</p>");
        return sb.toString();
    }

}
