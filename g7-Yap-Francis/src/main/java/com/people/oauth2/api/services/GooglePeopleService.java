package com.people.oauth2.api.services;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.people.v1.PeopleService;
import com.google.api.services.people.v1.model.*;
import com.people.oauth2.api.dtos.ContactDto;
import com.people.oauth2.api.dtos.EmailDto;
import com.people.oauth2.api.dtos.PhoneNumberDto;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GooglePeopleService {

    private PeopleService createPeopleService(String accessToken) throws GeneralSecurityException, IOException {
        GoogleCredential credential = new GoogleCredential().setAccessToken(accessToken);
        return new PeopleService.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                credential)
                .setApplicationName("Google Contacts API Client")
                .build();
    }

    public List<ContactDto> getContacts(String accessToken) throws IOException, GeneralSecurityException {
        PeopleService peopleService = createPeopleService(accessToken);
        ListConnectionsResponse response = peopleService.people().connections()
                .list("people/me")
                .setPersonFields("names,phoneNumbers,emailAddresses")
                .execute();
        
        List<Person> connections = response.getConnections();
        if (connections == null || connections.isEmpty()) {
            return Collections.emptyList();
        }

        return connections.stream()
                .map(this::mapPersonToContactDto)
                .collect(Collectors.toList());
    }

    public ContactDto getContact(String accessToken, String resourceName) throws IOException, GeneralSecurityException {
        PeopleService peopleService = createPeopleService(accessToken);
        Person person = peopleService.people()
                .get(resourceName)
                .setPersonFields("names,phoneNumbers,emailAddresses")
                .execute();
        
        return mapPersonToContactDto(person);
    }

    public ContactDto createContact(String accessToken, ContactDto contactDto) throws IOException, GeneralSecurityException {
        PeopleService peopleService = createPeopleService(accessToken);
        
        Person contactToCreate = new Person();
        
        // Set names
        Name name = new Name()
                .setGivenName(contactDto.givenName())
                .setFamilyName(contactDto.familyName());
        contactToCreate.setNames(Collections.singletonList(name));
        
        // Set phone numbers
        if (contactDto.phoneNumbers() != null && !contactDto.phoneNumbers().isEmpty()) {
            List<PhoneNumber> phoneNumbers = contactDto.phoneNumbers().stream()
                    .map(phone -> new PhoneNumber()
                            .setValue(phone.value())
                            .setType(phone.type()))
                    .collect(Collectors.toList());
            contactToCreate.setPhoneNumbers(phoneNumbers);
        }
        
        // Set email addresses
        if (contactDto.emailAddresses() != null && !contactDto.emailAddresses().isEmpty()) {
            List<EmailAddress> emailAddresses = contactDto.emailAddresses().stream()
                    .map(email -> new EmailAddress()
                            .setValue(email.value())
                            .setType(email.type()))
                    .collect(Collectors.toList());
            contactToCreate.setEmailAddresses(emailAddresses);
        }
        
        Person createdPerson = peopleService.people()
                .createContact(contactToCreate)
                .execute();
                
        return mapPersonToContactDto(createdPerson);
    }
    
    public ContactDto updateContact(String accessToken, ContactDto contactDto) throws IOException, GeneralSecurityException {
        PeopleService peopleService = createPeopleService(accessToken);
        
        // First, get the existing contact to retrieve its etag
        Person existingContact = peopleService.people()
                .get(contactDto.resourceName())
                .setPersonFields("names,phoneNumbers,emailAddresses")
                .execute();
        
        // Create a Person object with updates
        Person contactToUpdate = new Person();
        
        // Set the etag - this is required for updates
        contactToUpdate.setEtag(existingContact.getEtag());
        
        // Set names
        Name name = new Name()
                .setGivenName(contactDto.givenName())
                .setFamilyName(contactDto.familyName());
        contactToUpdate.setNames(Collections.singletonList(name));
        
        // Set phone numbers
        if (contactDto.phoneNumbers() != null && !contactDto.phoneNumbers().isEmpty()) {
            List<PhoneNumber> phoneNumbers = contactDto.phoneNumbers().stream()
                    .map(phone -> new PhoneNumber()
                            .setValue(phone.value())
                            .setType(phone.type()))
                    .collect(Collectors.toList());
            contactToUpdate.setPhoneNumbers(phoneNumbers);
        } else {
            // Clear phone numbers if none provided
            contactToUpdate.setPhoneNumbers(Collections.emptyList());
        }
        
        // Set email addresses
        if (contactDto.emailAddresses() != null && !contactDto.emailAddresses().isEmpty()) {
            List<EmailAddress> emailAddresses = contactDto.emailAddresses().stream()
                    .map(email -> new EmailAddress()
                            .setValue(email.value())
                            .setType(email.type()))
                    .collect(Collectors.toList());
            contactToUpdate.setEmailAddresses(emailAddresses);
        } else {
            // Clear email addresses if none provided
            contactToUpdate.setEmailAddresses(Collections.emptyList());
        }
        
        String resourceName = contactDto.resourceName();
        Person updatedPerson = peopleService.people()
                .updateContact(resourceName, contactToUpdate)
                .setUpdatePersonFields("names,phoneNumbers,emailAddresses")
                .execute();
                
        return mapPersonToContactDto(updatedPerson);
    }
    
    public void deleteContact(String accessToken, String resourceName) throws IOException, GeneralSecurityException {
        PeopleService peopleService = createPeopleService(accessToken);
        peopleService.people().deleteContact(resourceName).execute();
    }

    private ContactDto mapPersonToContactDto(Person person) {
        String resourceName = person.getResourceName();
        
        String displayName = "";
        String givenName = "";
        String familyName = "";
        
        if (person.getNames() != null && !person.getNames().isEmpty()) {
            Name name = person.getNames().get(0);
            displayName = name.getDisplayName() != null ? name.getDisplayName() : "";
            givenName = name.getGivenName() != null ? name.getGivenName() : "";
            familyName = name.getFamilyName() != null ? name.getFamilyName() : "";
        }
        
        List<PhoneNumberDto> phoneNumbers = new ArrayList<>();
        if (person.getPhoneNumbers() != null) {
            phoneNumbers = person.getPhoneNumbers().stream()
                    .map(phone -> new PhoneNumberDto(
                            phone.getValue(), 
                            phone.getType()))
                    .collect(Collectors.toList());
        }
        
        List<EmailDto> emailAddresses = new ArrayList<>();
        if (person.getEmailAddresses() != null) {
            emailAddresses = person.getEmailAddresses().stream()
                    .map(email -> new EmailDto(
                            email.getValue(), 
                            email.getType()))
                    .collect(Collectors.toList());
        }
        
        return new ContactDto(resourceName, displayName, givenName, familyName, phoneNumbers, emailAddresses);
    }
}