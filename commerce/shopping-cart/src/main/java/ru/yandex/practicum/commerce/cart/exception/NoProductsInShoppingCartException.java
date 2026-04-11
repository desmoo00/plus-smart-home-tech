package ru.yandex.practicum.commerce.cart.exception;

public class NoProductsInShoppingCartException extends RuntimeException {

    public NoProductsInShoppingCartException() {
        super("Requested products are not present in shopping cart");
    }
}
