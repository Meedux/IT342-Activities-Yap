package com.yap.googlecontact.service;

import com.yap.googlecontact.model.Contact;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.core.ParameterizedTypeReference;

import java.util.Map;

@Service
public class GoogleContactsService {

    private final RestTemplate restTemplate;

    public GoogleContactsService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Map<String, Object> getContacts(OAuth2AccessToken accessToken) {
        String contactsUrl = "https://people.googleapis.com/v1/people/me/connections?personFields=names,emailAddresses";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken.getTokenValue());
        HttpEntity<Void> request = new HttpEntity<>(headers);

        return restTemplate.exchange(
            contactsUrl,
            HttpMethod.GET,
            request,
            new ParameterizedTypeReference<Map<String, Object>>() {}
        ).getBody();
    }

    public void addContact(OAuth2AccessToken accessToken, Contact contact) {
        String addContactUrl = "https://people.googleapis.com/v1/people:createContact";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken.getTokenValue());
        HttpEntity<Contact> request = new HttpEntity<>(contact, headers);

        restTemplate.postForObject(addContactUrl, request, Contact.class);
    }

    public void editContact(OAuth2AccessToken accessToken, String resourceName, Contact contact) {
        String editContactUrl = "https://people.googleapis.com/v1/" + resourceName + ":updateContact";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken.getTokenValue());
        HttpEntity<Contact> request = new HttpEntity<>(contact, headers);

        restTemplate.exchange(editContactUrl, HttpMethod.PATCH, request, Contact.class);
    }

    public void deleteContact(OAuth2AccessToken accessToken, String resourceName) {
        String deleteContactUrl = "https://people.googleapis.com/v1/" + resourceName + ":deleteContact";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken.getTokenValue());
        HttpEntity<Void> request = new HttpEntity<>(headers);

        restTemplate.exchange(deleteContactUrl, HttpMethod.DELETE, request, Void.class);
    }
}