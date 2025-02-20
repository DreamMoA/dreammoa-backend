package com.garret.dreammoa.config;
import com.garret.dreammoa.config.oauth.CustomOAuth2AuthorizationRequestResolver;
import com.garret.dreammoa.config.oauth.OAuth2AuthorizationRequestBasedOnCookieRepository;
import com.garret.dreammoa.config.oauth.OAuth2SuccessHandler;
import com.garret.dreammoa.config.oauth.OAuth2UserCustomService;
import com.garret.dreammoa.domain.repository.FileRepository;
import com.garret.dreammoa.domain.repository.UserRepository;
import com.garret.dreammoa.domain.service.user.CustomUserDetailsService;
import com.garret.dreammoa.filter.JwtFilter;
import com.garret.dreammoa.utils.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final OAuth2UserCustomService oAuth2UserCustomService; // 의존성 추가
    private final UserRepository userRepository;
    private final FileRepository fileRepository;
    private final FileProperties fileProperties;
    private final ClientRegistrationRepository clientRegistrationRepository;

    @Bean
    public OAuth2SuccessHandler oAuth2SuccessHandler() {
        return new OAuth2SuccessHandler(jwtUtil, userRepository, fileRepository, fileProperties);
    }


    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }


    @Bean
    public OAuth2AuthorizationRequestBasedOnCookieRepository oAuth2AuthorizationRequestBasedOnCookieRepository() {
        return new OAuth2AuthorizationRequestBasedOnCookieRepository();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, ClientRegistrationRepository clientRegistrationRepository) throws Exception {
        http
                // CORS 설정
                // CSRF 비활성화
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Form 로그인 비활성화
                .formLogin(AbstractHttpConfigurer::disable)
                // Basic 인증도 비활성화
                .httpBasic(AbstractHttpConfigurer::disable)
                // 예외처리(인증 실패 시 401 반환)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            log.warn("🔴 [401 Unauthorized] 인증되지 않은 사용자 접근 - 요청 경로: {}", request.getRequestURI());
                            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            log.warn("🟠 [403 Forbidden] 권한 부족 - 요청 경로: {}, 사용자: {}",
                                    request.getRequestURI(), request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "Anonymous");
                            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
                        })
                )
                // 인증/인가 설정
                .authorizeHttpRequests(auth -> auth
                                // Swagger UI 경로 인증 없이 허용
                                .requestMatchers(
                                        "/v3/api-docs/**",  // OpenAPI 문서 JSON
                                        "/swagger-ui/**",   // Swagger UI 리소스
                                        "/swagger-ui.html", // Swagger UI 접속 페이지
                                        "/webjars/**",      // Swagger가 사용하는 정적 리소스
                                        "/swagger-resources/**"
                                ).permitAll()
                                .requestMatchers("/stt-websocket").permitAll()
                                // 특정 GET 요청 허용 (글 목록 조회만)
                                .requestMatchers(HttpMethod.GET, "/boards").permitAll()
                                .requestMatchers(HttpMethod.GET, "api/likes/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/challenges/tag-challenges", "/challenges/search", "/challenges/all-challenges").permitAll()
                                //                        .requestMatchers("api/likes/**").authenticated()
                                .requestMatchers("/random-determinations","/ending-soon",
                                        "/total-screen-time", "/login","/", "/error", "/refresh", "/top-viewed", "/openvidu/**", "/join","/email-find","/pw-find","/openvidu/**",
                                        "/send-verification-code", "/verify-email-code", "/check-email", "/check-nickname",
                                        "/challenges/*/info", "/challenges/invite/**","/user-tag",
                                        "/tags", "/user-tags", "/stt/speech-to-text", "/gpt-summary")
                                .permitAll()
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                .requestMatchers("/files/**").permitAll()
//                                .requestMatchers("/admin/**").hasRole("ADMIN")
                                .anyRequest().authenticated()
                        //                        .anyRequest().permitAll()
                )
                // 구글로그인설정
                // 구글로그인설정
                // 네이버로그인설정
                // OAuth2 로그인 설정 (구글 + 네이버 + 카카오)
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(auth -> auth.authorizationRequestResolver(
                                new CustomOAuth2AuthorizationRequestResolver(clientRegistrationRepository) // 여기서 각 도메인 뿌려줌
                        ))
                        .userInfoEndpoint(userInfo -> userInfo.userService(oAuth2UserCustomService))
                        .successHandler(oAuth2SuccessHandler())
                )
                // JWT 필터
                .addFilterBefore(new JwtFilter(jwtUtil, userDetailsService),
                        UsernamePasswordAuthenticationFilter.class)
                // 세션 사용 안 함(STATELESS)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();


    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173", "http://dreammoa.duckdns.org", "https://dreammoa.duckdns.org", "http://3.38.214.23")); // React 개발 서버 도메인
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")); // 허용할 HTTP 메서드
        config.setAllowedHeaders(List.of("*")); // 모든 헤더 허용
        config.setAllowCredentials(true); // 인증 정보 허용

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // 모든 경로에 대해 적용
        return source;
    }



}

