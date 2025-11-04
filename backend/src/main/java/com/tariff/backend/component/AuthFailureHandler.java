// todo fix this so forbidden will return error

//package com.tariff.backend.component;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import java.io.IOException;
//import java.io.OutputStream;
//import javax.servlet.ServletException;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import org.springframework.http.HttpStatus;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.web.authentication.AuthenticationFailureHandler;
//import org.springframework.stereotype.Component;
//
//@Component
//public class AuthFailureHandler {
//
//  @Override
//  public void onAuthenticationFailure(
//    HttpServletRequest request,
//    HttpServletResponse httpServletResponse,
//    AuthenticationException ex
//  ) throws IOException, ServletException {
//    Map<String, Object> response = new HashMap<>();
//    response.put("status", "34");
//    response.put("message", "unauthorized access");
//
//    httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//    OutputStream out = httpServletResponse.getOutputStream();
//    ObjectMapper mapper = new ObjectMapper();
//    mapper.writerWithDefaultPrettyPrinter().writeValue(out, response);
//    out.flush();
//  }
//}
