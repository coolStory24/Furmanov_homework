package com.application.bookService.book.exceptions;

public class IsNotAuthorException extends Exception {
    public IsNotAuthorException(Long authorId) {
        super("Author: "+ authorId);
    }
}
