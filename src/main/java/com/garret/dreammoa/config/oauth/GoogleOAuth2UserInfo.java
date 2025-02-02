package com.garret.dreammoa.config.oauth;

import java.util.Map;

public class GoogleOAuth2UserInfo extends OAuth2UserInfo {

    public GoogleOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getId() {
        return (String) attributes.get("sub"); // Google OAuth2에서 ID는 "sub" 필드
    }

    @Override
    public String getName() {
        return (String) attributes.get("name"); // Google OAuth2에서 이름은 "name" 필드
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email"); // Google OAuth2에서 이메일은 "email" 필드
    }

    @Override
    public String getProfileImageUrl() {
        return (String) attributes.get("picture"); // Google OAuth2에서 프로필 이미지는 "picture" 필드
    }
}
