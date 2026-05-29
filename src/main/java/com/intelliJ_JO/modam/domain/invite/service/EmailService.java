package com.intelliJ_JO.modam.domain.invite.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    public void sendInviteCode(String toEmail, String accountNumber, String inviteCode) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("[모담] 모임통장 초대 코드가 도착했습니다");
            helper.setText(buildEmailBody(accountNumber, inviteCode), true);

            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("이메일 발송에 실패했습니다.", e);
        }
    }

    private String buildEmailBody(String accountNumber, String inviteCode) {
        return """
                <div style="font-family: 'Apple SD Gothic Neo', sans-serif; max-width: 480px; margin: 0 auto; padding: 40px 24px; background: #fff;">
                  <h2 style="color: #FF7BAC; font-size: 22px; margin-bottom: 8px;">모담 모임통장 초대</h2>
                  <p style="color: #555; font-size: 15px; line-height: 1.6; margin-bottom: 24px;">
                    아래 초대 코드를 입력하면 <strong>%s</strong> 모임통장에 참여할 수 있습니다.
                  </p>
                  <div style="background: #FFF0F6; border-radius: 12px; padding: 24px; text-align: center; margin-bottom: 32px;">
                    <span style="font-size: 36px; font-weight: 700; letter-spacing: 10px; color: #FF7BAC;">%s</span>
                  </div>

                  <div style="border-top: 1px solid #f0f0f0; padding-top: 24px;">
                    <p style="color: #444; font-size: 14px; font-weight: 600; margin-bottom: 16px;">코드 입력 방법</p>
                    <ol style="color: #555; font-size: 14px; line-height: 2; padding-left: 20px; margin: 0 0 24px;">
                      <li>모담에 로그인하세요.</li>
                      <li>모임통장이 없다면 <a href="%s/account-setup" style="color: #FF7BAC; text-decoration: none; font-weight: 600;">모임통장 개설 페이지</a>에서<br>
                          "초대를 받아서 오셨나요?" 링크를 클릭하세요.</li>
                      <li>모임통장이 있다면 <a href="%s/dashboard" style="color: #FF7BAC; text-decoration: none; font-weight: 600;">대시보드</a>에서<br>
                          "초대 코드 입력" 버튼을 클릭하세요.</li>
                      <li>위 6자리 코드를 입력하면 파트너와 연결됩니다.</li>
                    </ol>
                    <p style="color: #aaa; font-size: 12px;">본인이 요청하지 않은 초대라면 이 메일을 무시하셔도 됩니다.</p>
                  </div>
                </div>
                """.formatted(accountNumber, inviteCode, baseUrl, baseUrl);
    }
}
