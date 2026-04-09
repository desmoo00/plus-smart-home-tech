package ru.yandex.practicum.commerce.api.warehouse;

public record AddressDto(
        String country,
        String city,
        String street,
        String house,
        String flat
) {
}
