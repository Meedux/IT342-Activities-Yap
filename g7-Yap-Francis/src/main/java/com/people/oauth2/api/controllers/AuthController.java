package com.people.oauth2.api.controllers;

import java.io.IOException;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.RedirectView;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.people.oauth2.api.dtos.TokenDto;
import com.people.oauth2.api.dtos.UrlDto;

@Controller
public class AuthController {

    @Value("${spring.security.oauth2.resourceserver.opaque-token.client-id}")
    private String ClientId;

    @Value("${spring.security.oauth2.resourceserver.opaque-token.client-secret}")
    private String ClientSecret;

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/auth/url")
    @ResponseBody
    public ResponseEntity<UrlDto> auth(){
        String url = new GoogleAuthorizationCodeRequestUrl(
            ClientId, 
            "http://localhost:8080/auth/code", 
            Arrays.asList("email", "profile", "openid", "https://www.googleapis.com/auth/contacts")
        ).build();
        return ResponseEntity.ok(new UrlDto(url));
    }

    @GetMapping("/auth/code")
    public RedirectView handleAuthCode(@RequestParam("code") String code, Model model) {
        try {
            String token = new GoogleAuthorizationCodeTokenRequest(
                new NetHttpTransport(),
                new GsonFactory(),
                ClientId,
                ClientSecret,
                code,
                "http://localhost:8080/auth/code"
            ).execute().getAccessToken();
            
            // Redirect to display token
            return new RedirectView("/display-token?token=" + token);
        } catch (IOException e) {
            return new RedirectView("/login?error=authentication_failed");
        }
    }
    
    @GetMapping("/auth/callback")
    @ResponseBody
    public ResponseEntity<TokenDto> callback(@RequestParam("code") String code){
        String token;
        try{
            token = new GoogleAuthorizationCodeTokenRequest(
                new NetHttpTransport(),
                new GsonFactory(),
                ClientId,
                ClientSecret,
                code,
                "http://localhost:8080/auth/code"
            ).execute().getAccessToken();
        } catch(IOException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        return ResponseEntity.ok(new TokenDto(token));
    }
}