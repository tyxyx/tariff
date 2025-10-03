package com.tariff.backend.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;

class SecurityConfigTest {

  @Test
  void corsConfigurationSourceUsesDefaultWhenFrontendHostMissing() {
    SecurityConfig config = new SecurityConfig();
    ReflectionTestUtils.setField(config, "frontendHost", " ");

    CorsConfiguration corsConfiguration = config
      .corsConfigurationSource()
      .getCorsConfiguration(new MockHttpServletRequest());

    assertThat(corsConfiguration).isNotNull();
    assertThat(corsConfiguration.getAllowedOrigins())
      .containsExactly("http://localhost:3000");
  }

  @Test
  void corsConfigurationSourceHonorsFullOrigin() {
    SecurityConfig config = new SecurityConfig();
    ReflectionTestUtils.setField(config, "frontendHost", "https://example.com");

    CorsConfiguration corsConfiguration = config
      .corsConfigurationSource()
      .getCorsConfiguration(new MockHttpServletRequest());

    assertThat(corsConfiguration).isNotNull();
    assertThat(corsConfiguration.getAllowedOrigins())
      .containsExactly("https://example.com");
  }

  @Test
  void corsConfigurationSourceBuildsOriginWhenSchemeMissing() {
    SecurityConfig config = new SecurityConfig();
    ReflectionTestUtils.setField(config, "frontendHost", "example.com");

    CorsConfiguration corsConfiguration = config
      .corsConfigurationSource()
      .getCorsConfiguration(new MockHttpServletRequest());

    assertThat(corsConfiguration).isNotNull();
    assertThat(corsConfiguration.getAllowedOrigins())
      .containsExactly("http://example.com:3000");
  }

  @Test
  void corsConfigurationSourcePreservesCustomPort() {
    SecurityConfig config = new SecurityConfig();
    ReflectionTestUtils.setField(config, "frontendHost", "example.com:8080");

    CorsConfiguration corsConfiguration = config
      .corsConfigurationSource()
      .getCorsConfiguration(new MockHttpServletRequest());

    assertThat(corsConfiguration).isNotNull();
    assertThat(corsConfiguration.getAllowedOrigins())
      .containsExactly("http://example.com:8080");
  }
}
