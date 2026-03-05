package com.phumlanidev.cartservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class KeycloakTokenProvider {

  @Value("${keycloak.auth-server-url}")
  private String authServerUrl;

  @Value("${keycloak.realm}")
  private String realm;

  public String getAccessToken(String clientId, String clientSecret) {
    RestTemplate restTemplate = new RestTemplate();
    String tokenUrl = String.format("%s/realms/%s/protocol/openid-connect/token", authServerUrl, realm);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("grant_type", "client_credentials");
    params.add("client_id", clientId);
    params.add("client_secret", clientSecret);

    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

    ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
    return response.getBody().get("access_token").toString();
  }
}
