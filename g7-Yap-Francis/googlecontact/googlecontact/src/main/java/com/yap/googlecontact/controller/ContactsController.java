package com.yap.googlecontact.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.core.ParameterizedTypeReference;
import com.yap.googlecontact.model.*;

import java.util.Map;

@Controller
public class ContactsController {

    @GetMapping("/contacts")
    public String getContacts(@AuthenticationPrincipal OAuth2User user, Model model) {
        if (user == null) {
            return "redirect:/login";
        }

        String accessToken = user.getAttribute("access_token");

        String contactsUrl = "https://people.googleapis.com/v1/people/me/connections?personFields=names,emailAddresses";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        Map<String, Object> contacts = restTemplate.exchange(
            contactsUrl,
            HttpMethod.GET,
            request,
            new ParameterizedTypeReference<Map<String, Object>>() {}
        ).getBody();

        model.addAttribute("contacts", contacts);
        return "contacts"; // Thymeleaf template
    }

    @GetMapping("/contacts/add")
    public String addContactForm(Model model) {
        model.addAttribute("contact", new Contact());
        return "addContact"; // Thymeleaf template
    }

    @PostMapping("/contacts/add")
    public String addContact(@AuthenticationPrincipal OAuth2User user, @ModelAttribute Contact contact) {
        if (user == null) {
            return "redirect:/login";
        }

        String accessToken = user.getAttribute("access_token");

        String addContactUrl = "https://people.googleapis.com/v1/people:createContact";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Contact> request = new HttpEntity<>(contact, headers);

        restTemplate.postForObject(addContactUrl, request, Contact.class);

        return "redirect:/contacts";
    }

    @GetMapping("/contacts/edit/{resourceName}")
    public String editContactForm(@PathVariable String resourceName, Model model) {
        model.addAttribute("resourceName", resourceName);
        model.addAttribute("contact", new Contact());
        return "editContact"; // Thymeleaf template
    }

    @PostMapping("/contacts/edit/{resourceName}")
    public String editContact(@AuthenticationPrincipal OAuth2User user, @PathVariable String resourceName, @ModelAttribute Contact contact) {
        if (user == null) {
            return "redirect:/login";
        }

        String accessToken = user.getAttribute("access_token");

        String editContactUrl = "https://people.googleapis.com/v1/" + resourceName + ":updateContact";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Contact> request = new HttpEntity<>(contact, headers);

        restTemplate.exchange(editContactUrl, HttpMethod.PATCH, request, Contact.class);

        return "redirect:/contacts";
    }

    @GetMapping("/contacts/delete/{resourceName}")
    public String deleteContact(@AuthenticationPrincipal OAuth2User user, @PathVariable String resourceName) {
        if (user == null) {
            return "redirect:/login";
        }

        String accessToken = user.getAttribute("access_token");

        String deleteContactUrl = "https://people.googleapis.com/v1/" + resourceName + ":deleteContact";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        restTemplate.exchange(deleteContactUrl, HttpMethod.DELETE, request, Void.class);

        return "redirect:/contacts";
    }
}