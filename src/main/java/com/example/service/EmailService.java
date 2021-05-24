package com.example.service;

import com.example.dto.Center;
import com.example.dto.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private SimpleDateFormat simpleDateFormat;

    @Value("${spring.mail.sender.email}")
    private String sender;

    @Value("${spring.mail.recipient.email}")
    private String recipient;

    String newLine = System.getProperty("line.separator");

    public void sendMail(Map<String, Map<String, List<Center>>> countryData) {
        String subject = getSubject(countryData);
        if (null != subject) {
            String body = getBody(countryData);
            if (null != body) {
                SimpleMailMessage mailMessage = new SimpleMailMessage();
                mailMessage.setFrom(sender);
                mailMessage.setTo(recipient);
                mailMessage.setSubject(subject);
                mailMessage.setText(body);
//                javaMailSender.send(mailMessage);
                System.out.println(mailMessage);
            }
        }
    }

    private String getBody(Map<String, Map<String, List<Center>>> countryData) {
        Map<String, List<Center>> stateData = countryData.values().stream().findFirst().orElse(null);
        if (null != stateData) {
            List<Center> centers = stateData.values().stream().findFirst().orElse(null);
            if (!CollectionUtils.isEmpty(centers)) {
                StringBuilder body = new StringBuilder();
                centers.forEach(center -> {
                    if (!CollectionUtils.isEmpty(center.getSessions())) {
                        body.append(newLine);
                        body.append(getCenterInfo(center))
                                .append(getSessionsInfo(center.getSessions()));
                    }
                });
                return body.toString();
            }
        }
        return null;
    }

    private String getSubject(Map<String, Map<String, List<Center>>> countryData) {
        String stateName = countryData.keySet().stream().findFirst().orElse(null);
        if (null != stateName) {
            Map<String, List<Center>> stateData = countryData.get(stateName);
            String districtName = stateData.keySet().stream().findFirst().orElse(null);
            if (null != districtName) {
                StringBuilder subject = new StringBuilder("New vaccine slots got available in ")
                        .append(districtName).append(", ").append(stateName).append(".");
                return subject.toString();
            }
        }
        return null;
    }

    private String getCenterInfo(Center center) {
        StringBuilder centerInfo = new StringBuilder();
        centerInfo.append(center.getBlockName()).append(", ")
                .append(center.getName()).append(newLine)
                .append(center.getAddress()).append(newLine)
                .append(center.getFeeType()).append(", ")
                .append(center.getFrom()).append(" - ").append(center.getTo()).append(newLine);
        return centerInfo.toString();
    }

    private String getSessionsInfo(List<Session> sessions) {
        StringBuilder sessionsInfo = new StringBuilder();
        sessionsInfo.append("[").append(newLine);
        sessions.forEach(session -> {
            sessionsInfo.append("\t")
                    .append(simpleDateFormat.format(session.getDate())).append(": ").append(session.getVaccine()).append(": ")
                    .append("(dose1=").append(session.getAvailableCapacityDose1()).append(", ")
                    .append("dose2=").append(session.getAvailableCapacityDose2()).append(")")
                    .append(newLine);
        });
        sessionsInfo.append("]").append(newLine);
        return sessionsInfo.toString();
    }
}
