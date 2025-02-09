//package com.skripsi.Fluency;
//
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.ServletInputStream;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.stereotype.Component;
//import org.springframework.web.filter.OncePerRequestFilter;
//import org.springframework.web.servlet.HandlerInterceptor;
//
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//import java.util.Enumeration;
//
//@Component
//public class RequestLoggingInterceptor extends OncePerRequestFilter {
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
//        System.out.println("=== MULTIPART REQUEST LOG ===");
//        System.out.println("Method: " + request.getMethod());
//        System.out.println("URL: " + request.getRequestURL());
//
//        // ✅ Log Headers
//        Enumeration<String> headers = request.getHeaderNames();
//        while (headers.hasMoreElements()) {
//            String header = headers.nextElement();
//            System.out.println(header + ": " + request.getHeader(header));
//        }
//
//        // ✅ Log Body (Multipart Data)
//        if ("POST".equalsIgnoreCase(request.getMethod()) && request.getContentType() != null
//                && request.getContentType().startsWith("multipart/form-data")) {
//
//            ServletInputStream inputStream = request.getInputStream();
//            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
//            byte[] data = new byte[1024];
//            int bytesRead;
//
//            while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
//                buffer.write(data, 0, bytesRead);
//            }
//
//            String body = buffer.toString(StandardCharsets.UTF_8);
//            System.out.println("Multipart Body: \n" + body);
//        }
//        filterChain.doFilter(request, response);
//    }
//}
