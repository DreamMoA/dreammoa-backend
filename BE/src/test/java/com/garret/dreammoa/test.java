//package com.garret.dreammoa;
//
//public class test {
//
//
//    @GetMapping("/share/{planId}")
//    public ResponseEntity<?> generateShareLink(@PathVariable String planId) {
//        try {
//            // Plan ID 암호화
//            String encryptedId = encryptionUtil.encrypt(planId);
//            // 암호화된 ID를 URL-safe하게 인코딩
//            String encodedId = Base64.getUrlEncoder().encodeToString(encryptedId.getBytes());
//            // Plan 정보 가져오기
//            PlanInviteResponseDto planInviteResponse = planService.getPlanById(planId);
//            if (planInviteResponse == null) {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Plan not found");
//            }
//
//            String planTitle = encryptionUtil.encode(planInviteResponse.getTitle());
//            String userName = encryptionUtil.encode(planInviteResponse.getUserName()); // 유저 이름
//            // URL 생성
//            // 초대 링크 생성
//            String shareLink = "http://localhost:5173/plan/invite?encryptedId=" + encodedId +
//                    "&planTitle=" + planTitle +
//                    "&userName=" + userName;
//
//            return ResponseEntity.ok(shareLink);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error generating share link");
//        }
//    }
//
//    // 수락 시 Plan 데이터 저장 및 리다이렉트
//    @PostMapping("/accept")
//    public ResponseEntity<?> acceptPlan(
//            @RequestBody Map<String, String> request) {
//        try {
//            // 복호화
//            String decodedId = new String(Base64.getUrlDecoder().decode(request.get("encryptedId")));
//            String planId = encryptionUtil.decrypt(decodedId);
//            // URL 디코딩
//            planService.savePlanForShare(planId, request.get("userId"));
//
//            // Plan 페이지로 리다이렉트
//            return ResponseEntity.ok()
//                    .build();
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing plan");
//        }
//    }
//
//package com.trip.util;
//
//import javax.crypto.Cipher;
//import javax.crypto.spec.SecretKeySpec;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Component;
//
//import java.net.URLDecoder;
//import java.net.URLEncoder;
//import java.util.Base64;
//
//    @Component
//    public class EncryptionUtil {
//
//        @Value("${share.link.key}")
//        private String shareSecretKey; // 16바이트 고정 키
//
//        public String encrypt(String data) throws Exception {
//            SecretKeySpec secretKey = new SecretKeySpec(shareSecretKey.getBytes(), "AES");
//            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
//            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
//            return Base64.getUrlEncoder().encodeToString(cipher.doFinal(data.getBytes("UTF-8")));
//        }
//
//        public String decrypt(String encryptedData) throws Exception {
//            SecretKeySpec secretKey = new SecretKeySpec(shareSecretKey.getBytes(), "AES");
//            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
//            cipher.init(Cipher.DECRYPT_MODE, secretKey);
//            return new String(cipher.doFinal(Base64.getUrlDecoder().decode(encryptedData)), "UTF-8");
//        }
//        public String decode(String data) throws Exception{
//            return URLDecoder.decode(data, "UTF-8");
//        }
//        public String encode(String data) throws Exception{
//            return URLEncoder.encode(data, "UTF-8");
//        }
//    }
//
//
//
//}
