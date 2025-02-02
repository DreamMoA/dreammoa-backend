package com.garret.dreammoa.config.oauth;

import java.util.Map;

public class NaverOAuth2UserInfo extends OAuth2UserInfo {

    public NaverOAuth2UserInfo(Map<String, Object> attributes) {
        super((Map<String, Object>) attributes.get("response")); // 네이버는 'response' 안에 정보가 있음
    }

    @Override
    public String getId() {
        return (String) attributes.get("id"); // 네이버에서 ID는 "id" 필드
    }

    @Override
    public String getName() {
        return attributes.get("name") != null ? (String) attributes.get("name") : getEmail(); // 이름 없으면 이메일 사용
    }

    @Override
    public String getEmail() {
        return (String) attributes.get("email"); // 네이버에서 이메일은 "email" 필드
    }

    @Override
    public String getProfileImageUrl() {
        return (String) attributes.get("profile_image"); // 네이버에서 프로필 이미지는 "profile_image" 필드
    }
}
