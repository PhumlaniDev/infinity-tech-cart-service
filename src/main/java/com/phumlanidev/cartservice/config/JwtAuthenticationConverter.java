package com.phumlanidev.cartservice.config;

import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

  @Value("${keycloak.principle-attribute}")
  private String principleAttribute;

  @Value("${keycloak.resource}")
  private String resourceId;

  @Override
  public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
    Set<GrantedAuthority> authorities = new HashSet<>();
    authorities.addAll(extractRealmRoles(jwt));
    authorities.addAll(extractResourceRoles(jwt));
    authorities.addAll(extractTopLevelRoles(jwt));

    System.out.println("🔐 Extracted authorities from token:");
    authorities.forEach(a -> System.out.println(" -> " + a.getAuthority()));

    return new JwtAuthenticationToken(jwt, authorities, getPrincipleClaimName(jwt));
  }

  private String getPrincipleClaimName(Jwt jwt) {
    return Optional.ofNullable(principleAttribute)
            .map(jwt::getClaim)
            .orElse(jwt.getClaim(JwtClaimNames.SUB)).toString();
  }

  private Collection<? extends GrantedAuthority> extractRealmRoles(Jwt jwt) {
    Map<String, Object> realmAccess = jwt.getClaim("realm_access");
    if (realmAccess == null || realmAccess.get("roles") == null) {
      return Set.of();
    }

    Collection<String> roles = (Collection<String>) realmAccess.get("roles");
    return roles.stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
            .collect(Collectors.toSet());
  }

  private Collection<? extends GrantedAuthority> extractResourceRoles(Jwt jwt) {
    Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
    if (resourceAccess == null) {
      return Set.of();
    }

    Map<String, Object> client = (Map<String, Object>) resourceAccess.get(resourceId);
    if (client == null || client.get("roles") == null) {
      return Set.of();
    }

    Collection<String> clientRoles = (Collection<String>) client.get("roles");
    return clientRoles.stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
            .collect(Collectors.toSet());
  }

  private Collection<? extends GrantedAuthority> extractTopLevelRoles(Jwt jwt) {
    Collection<String> topRoles = jwt.getClaim("roles");
    if (topRoles == null) {
      return Set.of();
    }

    return topRoles.stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
            .collect(Collectors.toSet());
  }

  public String extractUserEmail(Jwt jwt) {
    return jwt.getClaim("email");
  }

  public String extractUserId(Jwt jwt) {
    return jwt.getClaim("sub");
  }

  public Jwt getJwt() {
    // get token value from the current authentication context
    return (Jwt) org.springframework.security.core.context.SecurityContextHolder.getContext()
            .getAuthentication().getPrincipal();
  }
}