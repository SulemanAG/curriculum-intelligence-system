package org.curriculumai.common.exceptions;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, String id) {
        super(resourceName + " not found with ID: " + id);
    }
}

