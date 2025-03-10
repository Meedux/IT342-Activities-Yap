package com.people.oauth2.api.controllers;

import com.people.oauth2.api.dtos.ContactDto;
import com.people.oauth2.api.dtos.EmailDto;
import com.people.oauth2.api.dtos.PhoneNumberDto;
import com.people.oauth2.api.services.GooglePeopleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/contacts")
public class ContactController {

    private final GooglePeopleService googlePeopleService;

    @Autowired
    public ContactController(GooglePeopleService googlePeopleService) {
        this.googlePeopleService = googlePeopleService;
    }

    @GetMapping
    public String listContacts(@RequestParam("token") String accessToken, Model model) {
        try {
            List<ContactDto> contacts = googlePeopleService.getContacts(accessToken);
            model.addAttribute("contacts", contacts);
            model.addAttribute("accessToken", accessToken);
            return "contacts/list";
        } catch (Exception e) {
            model.addAttribute("error", "Error fetching contacts: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/new")
    public String newContactForm(@RequestParam("token") String accessToken, Model model) {
        ContactDto newContact = new ContactDto(null, "", "", "", new ArrayList<>(), new ArrayList<>());
        model.addAttribute("contact", newContact);
        model.addAttribute("accessToken", accessToken);
        model.addAttribute("isNew", true);
        return "contacts/form";
    }

    @PostMapping
    public String createContact(
            @RequestParam("token") String accessToken,
            @RequestParam("givenName") String givenName,
            @RequestParam("familyName") String familyName,
            @RequestParam(value = "phoneNumber", required = false) String phoneNumber,
            @RequestParam(value = "phoneType", required = false) String phoneType,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "emailType", required = false) String emailType,
            Model model) {
        try {
            List<PhoneNumberDto> phones = new ArrayList<>();
            if (phoneNumber != null && !phoneNumber.isBlank()) {
                phones.add(new PhoneNumberDto(phoneNumber, phoneType != null ? phoneType : "mobile"));
            }
            
            List<EmailDto> emails = new ArrayList<>();
            if (email != null && !email.isBlank()) {
                emails.add(new EmailDto(email, emailType != null ? emailType : "home"));
            }
            
            ContactDto newContact = new ContactDto(
                null, 
                givenName + " " + familyName, 
                givenName, 
                familyName, 
                phones, 
                emails
            );
            
            googlePeopleService.createContact(accessToken, newContact);
            return "redirect:/contacts?token=" + accessToken;
        } catch (Exception e) {
            model.addAttribute("error", "Error creating contact: " + e.getMessage());
            return "error";
        }
    }

    // Modified to use path variables with regex to capture the full resource name
    @GetMapping("/{resourcePrefix}/{resourceId}/edit")
    public String editContactForm(
            @PathVariable("resourcePrefix") String resourcePrefix,
            @PathVariable("resourceId") String resourceId,
            @RequestParam("token") String accessToken,
            Model model) {
        try {
            String fullResourceName = resourcePrefix + "/" + resourceId;
            ContactDto contact = googlePeopleService.getContact(accessToken, fullResourceName);
            model.addAttribute("contact", contact);
            model.addAttribute("accessToken", accessToken);
            model.addAttribute("isNew", false);
            return "contacts/form";
        } catch (Exception e) {
            model.addAttribute("error", "Error fetching contact: " + e.getMessage());
            return "error";
        }
    }

    // Modified to use path variables with regex to capture the full resource name
    @PostMapping("/{resourcePrefix}/{resourceId}")
    public String updateContact(
            @PathVariable("resourcePrefix") String resourcePrefix,
            @PathVariable("resourceId") String resourceId,
            @RequestParam("token") String accessToken,
            @RequestParam("givenName") String givenName,
            @RequestParam("familyName") String familyName,
            @RequestParam(value = "phoneNumber", required = false) String phoneNumber,
            @RequestParam(value = "phoneType", required = false) String phoneType,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "emailType", required = false) String emailType,
            Model model) {
        try {
            String fullResourceName = resourcePrefix + "/" + resourceId;
            List<PhoneNumberDto> phones = new ArrayList<>();
            if (phoneNumber != null && !phoneNumber.isBlank()) {
                phones.add(new PhoneNumberDto(phoneNumber, phoneType != null ? phoneType : "mobile"));
            }
            
            List<EmailDto> emails = new ArrayList<>();
            if (email != null && !email.isBlank()) {
                emails.add(new EmailDto(email, emailType != null ? emailType : "home"));
            }
            
            ContactDto updatedContact = new ContactDto(
                fullResourceName, 
                givenName + " " + familyName, 
                givenName, 
                familyName, 
                phones, 
                emails
            );
            
            googlePeopleService.updateContact(accessToken, updatedContact);
            return "redirect:/contacts?token=" + accessToken;
        } catch (Exception e) {
            model.addAttribute("error", "Error updating contact: " + e.getMessage());
            return "error";
        }
    }

    // Modified to use path variables with regex to capture the full resource name
    @GetMapping("/{resourcePrefix}/{resourceId}/delete")
    public String deleteContact(
            @PathVariable("resourcePrefix") String resourcePrefix,
            @PathVariable("resourceId") String resourceId,
            @RequestParam("token") String accessToken,
            Model model) {
        try {
            String fullResourceName = resourcePrefix + "/" + resourceId;
            googlePeopleService.deleteContact(accessToken, fullResourceName);
            return "redirect:/contacts?token=" + accessToken;
        } catch (Exception e) {
            model.addAttribute("error", "Error deleting contact: " + e.getMessage());
            return "error";
        }
    }
}