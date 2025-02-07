package com.garret.dreammoa.domain.service.challenge;

import io.openvidu.java.client.OpenVidu;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;

public class OpenViduService {

    @Value("${OPENVIDU_URL}")
    private String OPENVIDU_URL;

    @Value("${OPENVIDU_SECRET}")
    private String OPENVIDU_SECRET;

    private OpenVidu openVidu;

    @PostConstruct
    public void init(){
        //OpenVidu 서버와의 연결 객체 생성
        this.openVidu = new OpenVidu(OPENVIDU_URL, OPENVIDU_SECRET);
        System.out.println(" openVidu init 완료");
    }
}
