package cn.glogs.activeauth.iamcore.service.impl;

import cn.glogs.activeauth.iamcore.api.payload.RestResultPacker;
import cn.glogs.activeauth.iamcore.config.properties.LocatorConfiguration;
import cn.glogs.activeauth.iamcore.domain.AuthenticationPrincipal;
import cn.glogs.activeauth.iamcore.exception.business.NotFoundException;
import cn.glogs.activeauth.iamcore.repository.AuthenticationPrincipalRepository;
import cn.glogs.activeauth.iamcore.service.AuthenticationMfaService;
import org.jboss.aerogear.security.otp.Totp;
import org.jboss.aerogear.security.otp.api.Base32;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class AuthenticationMfaServiceImpl implements AuthenticationMfaService {

    private final static String QR_PREFIX = "https://chart.googleapis.com/chart?chs=200x200&chld=M%%7C0&cht=qr&chl=";

    private final AuthenticationPrincipalRepository authenticationPrincipalRepository;
    private final LocatorConfiguration locatorConfiguration;

    public AuthenticationMfaServiceImpl(
            AuthenticationPrincipalRepository authenticationPrincipalRepository,
            LocatorConfiguration locatorConfiguration
    ) {
        this.authenticationPrincipalRepository = authenticationPrincipalRepository;
        this.locatorConfiguration = locatorConfiguration;
    }

    private boolean isValidLong(String code) {
        try {
            Long.parseLong(code);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    @Override
    @Transactional
    public String setMfa(Long principalId, boolean mfaEnable) throws NotFoundException {
        AuthenticationPrincipal principal = authenticationPrincipalRepository.findById(principalId).orElseThrow(() -> new NotFoundException(String.format("Principal %s not found.", principalId)));
        principal.setMfaEnable(mfaEnable);
        if (mfaEnable) {
            String secret = Base32.random();
            principal.setMfaSecret(secret);
            String url = QR_PREFIX + URLEncoder.encode(
                    String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s",
                            locatorConfiguration.getService(),
                            principal.getName(),
                            secret,
                            locatorConfiguration.getService()
                    ), StandardCharsets.UTF_8);
            authenticationPrincipalRepository.save(principal);
            return url;
        } else {
            principal.setMfaSecret(null);
            authenticationPrincipalRepository.save(principal);
            return "MFA Closed.";
        }
    }

    @Override
    public boolean verify(AuthenticationPrincipal principal, String verificationCode) {
        Totp totp = new Totp(principal.getMfaSecret());
        return isValidLong(verificationCode) && totp.verify(verificationCode);
    }
}
