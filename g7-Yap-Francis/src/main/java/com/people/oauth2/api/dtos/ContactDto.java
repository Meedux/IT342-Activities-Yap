package com.people.oauth2.api.dtos;

import java.util.List;

public record ContactDto(
    String resourceName,
    String displayName,
    String givenName,
    String familyName,
    List<PhoneNumberDto> phoneNumbers,
    List<EmailDto> emailAddresses
) {}