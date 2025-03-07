package com.yap.googlecontact.controller;

import com.yap.googlecontact.model.Contact;
import com.yap.googlecontact.service.GoogleContactsService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
public class ContactsController {

    private final GoogleContactsService googleContactsService;
    private final OAuth2AuthorizedClientService authorizedClientService;

    public ContactsController(GoogleContactsService googleContactsService, OAuth2AuthorizedClientService authorizedClientService) {
        this.googleContactsService = googleContactsService;
        this.authorizedClientService = authorizedClientService;
    }

    @GetMapping("/contacts")
    public String getContacts(@AuthenticationPrincipal OAuth2AuthenticationToken authentication, Model model) {
        if (authentication == null) {
            return "redirect:/login";
        }

        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(),
                authentication.getName()
        );

        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
        Map<String, Object> contacts = googleContactsService.getContacts(accessToken);

        model.addAttribute("contacts", contacts);
        return "contacts"; // Thymeleaf template
    }

    @GetMapping("/contacts/add")
    public String addContactForm(Model model) {
        model.addAttribute("contact", new Contact());
        return "addContact"; // Thymeleaf template
    }

    @PostMapping("/contacts/add")
    public String addContact(@AuthenticationPrincipal OAuth2AuthenticationToken authentication, @ModelAttribute Contact contact) {
        if (authentication == null) {
            return "redirect:/login";
        }

        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(),
                authentication.getName()
        );

        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
        googleContactsService.addContact(accessToken, contact);

        return "redirect:/contacts";
    }

    @GetMapping("/contacts/edit/{resourceName}")
    public String editContactForm(@PathVariable String resourceName, Model model) {
        model.addAttribute("resourceName", resourceName);
        model.addAttribute("contact", new Contact());
        return "editContact"; // Thymeleaf template
    }

    @PostMapping("/contacts/edit/{resourceName}")
    public String editContact(@AuthenticationPrincipal OAuth2AuthenticationToken authentication, @PathVariable String resourceName, @ModelAttribute Contact contact) {
        if (authentication == null) {
            return "redirect:/login";
        }

        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(),
                authentication.getName()
        );

        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
        googleContactsService.editContact(accessToken, resourceName, contact);

        return "redirect:/contacts";
    }

    @GetMapping("/contacts/delete/{resourceName}")
    public String deleteContact(@AuthenticationPrincipal OAuth2AuthenticationToken authentication, @PathVariable String resourceName) {
        if (authentication == null) {
            return "redirect:/login";
        }

        OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(
                authentication.getAuthorizedClientRegistrationId(),
                authentication.getName()
        );

        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
        googleContactsService.deleteContact(accessToken, resourceName);

        return "redirect:/contacts";
    }
}