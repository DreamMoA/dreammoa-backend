package com.garret.dreammoa.config.oauth;

import java.util.Map;

public class OAuth2UserInfoFactory {
    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if ("naver".equalsIgnoreCase(registrationId)) {
            return new NaverOAuth2UserInfo(attributes);
        } else {
            return new GoogleOAuth2UserInfo(attributes);
        }
    }
}