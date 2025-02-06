package com.garret.dreammoa.config.oauth;

import java.util.Map;

public abstract class OAuth2UserInfo {
    protected Map<String, Object> attributes;

    public OAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public abstract String getId();     // 사용자 고유 ID
    public abstract String getName();   // 사용자 이름
    public abstract String getEmail();  // 사용자 이메일
    public abstract String getProfileImageUrl(); // 사용자 프로필 이미지 URL 추가
}
