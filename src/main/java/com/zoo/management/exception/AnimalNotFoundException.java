package com.zoo.management.exception;

public class AnimalNotFoundException extends RuntimeException {
    public AnimalNotFoundException(Long id) {
        super("חיה עם מזהה " + id + " לא נמצאה במערכת");
    }

    public AnimalNotFoundException(String message) {
        super(message);
    }
}
