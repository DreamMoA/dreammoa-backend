package com.garret.dreammoa.config.oauth;

import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * ✅ 최신 Spring Security 5.7+ 기준 OAuth2 요청 커스텀 Resolver
 * - OAuth2 요청 시 강제 로그인 파라미터 추가 (네이버, 카카오, 구글)
 * - OAuth2AuthorizationRequest에서 직접 registrationId를 가져오지 않고, ClientRegistration을 조회하는 방식 사용
 */
public class CustomOAuth2AuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final DefaultOAuth2AuthorizationRequestResolver defaultResolver;
    private final ClientRegistrationRepository clientRegistrationRepository;

    public CustomOAuth2AuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, "/oauth2/authorization");
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request);
        return customizeAuthorizationRequest(authorizationRequest);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest authorizationRequest = defaultResolver.resolve(request, clientRegistrationId);
        return customizeAuthorizationRequest(authorizationRequest);
    }

    private OAuth2AuthorizationRequest customizeAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest) {
        if (authorizationRequest == null) {
            return null;
        }

        Map<String, Object> additionalParameters = new HashMap<>(authorizationRequest.getAdditionalParameters());

        // ✅ clientId를 기반으로 ClientRegistration 조회하여 registrationId 가져오기
        String clientId = authorizationRequest.getClientId();
        String registrationId = getRegistrationIdByClientId(clientId);

        if ("naver".equals(registrationId)) {
            additionalParameters.put("auth_type", "reprompt");  // 네이버 강제 로그인
        }
        if ("kakao".equals(registrationId)) {
            additionalParameters.put("prompt", "login");  // 카카오 강제 로그인
        }
        if ("google".equals(registrationId)) {
            additionalParameters.put("prompt", "select_account");  // 구글 강제 로그인
        }

        return OAuth2AuthorizationRequest.from(authorizationRequest)
                .additionalParameters(additionalParameters)
                .build();
    }

    /**
     * 🔹 clientId를 기반으로 ClientRegistration을 찾아서 registrationId 반환
     */
    private String getRegistrationIdByClientId(String clientId) {
        if (clientRegistrationRepository == null) {
            return null;
        }

        Iterable<ClientRegistration> registrations = (Iterable<ClientRegistration>) clientRegistrationRepository;
        for (ClientRegistration registration : registrations) {
            if (registration.getClientId().equals(clientId)) {
                return registration.getRegistrationId();
            }
        }
        return null;
    }
}
