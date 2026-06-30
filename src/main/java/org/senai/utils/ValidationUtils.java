package org.senai.utils;

import java.util.regex.Pattern;

/**
 * Utility class for input validation and sanitization
 */
public class ValidationUtils {
    
    // Common patterns for validation
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    private static final Pattern CPF_PATTERN = Pattern.compile(
        "^\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}$|^\\d{11}$"
    );
    
    private static final Pattern MATRICULA_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9]{1,50}$"
    );
    
    // Dangerous characters that could be used for injection
    private static final Pattern DANGEROUS_CHARS = Pattern.compile(
        "[<>\"'%;()&+\\\\]"
    );
    
    /**
     * Validates if a string is not null, not empty and within length limits
     */
    public static boolean isValidString(String value, int maxLength) {
        return value != null && !value.trim().isEmpty() && value.length() <= maxLength;
    }
    
    /**
     * Validates email format
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
    
    /**
     * Validates CPF format (with or without formatting)
     */
    public static boolean isValidCpf(String cpf) {
        return cpf != null && CPF_PATTERN.matcher(cpf).matches();
    }
    
    /**
     * Validates matricula format (alphanumeric, max 50 chars)
     */
    public static boolean isValidMatricula(String matricula) {
        return matricula != null && MATRICULA_PATTERN.matcher(matricula).matches();
    }
    
    /**
     * Sanitizes input by removing dangerous characters
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        
        String sanitized = input.trim()
            .replaceAll(DANGEROUS_CHARS.pattern(), "") // Remove dangerous characters
            .replaceAll("\\s+", " "); // Normalize whitespace
        
        return sanitized.isEmpty() ? null : sanitized;
    }
    
    /**
     * Validates search parameter for common search
     */
    public static void validateSearchParameter(String paramName, String value, int maxLength) {
        if (value == null || value.trim().isEmpty()) {
            return; // Null/empty is allowed for optional parameters
        }
        
        if (value.length() > maxLength) {
            throw new IllegalArgumentException(
                paramName + " deve ter no máximo " + maxLength + " caracteres"
            );
        }
        
        if (DANGEROUS_CHARS.matcher(value).find()) {
            throw new IllegalArgumentException(
                paramName + " contém caracteres não permitidos"
            );
        }
    }
    
    /**
     * Validates email parameter specifically
     */
    public static void validateEmailParameter(String email) {
        if (email == null || email.trim().isEmpty()) {
            return; // Null/empty is allowed
        }
        
        validateSearchParameter("Email", email, 255);
        
    }
    
    /**
     * Validates CPF parameter specifically
     */
    public static void validateCpfParameter(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) {
            return; // Null/empty is allowed
        }
        
        validateSearchParameter("CPF", cpf, 14);
        
    }
    
    /**
     * Validates matricula parameter specifically
     */
    public static void validateMatriculaParameter(String matricula) {
        if (matricula == null || matricula.trim().isEmpty()) {
            return; // Null/empty is allowed
        }
        
        validateSearchParameter("Matrícula", matricula, 50);
        
        if (!matricula.matches("[a-zA-Z0-9]+")) {
            throw new IllegalArgumentException("Matrícula deve conter apenas letras e números");
        }
    }
    
    /**
     * Checks if at least one search parameter is provided
     */
    public static void validateAtLeastOneParameter(String... parameters) {
        boolean hasValidParameter = false;
        
        for (String param : parameters) {
            if (param != null && !param.trim().isEmpty()) {
                hasValidParameter = true;
                break;
            }
        }
        
        if (!hasValidParameter) {
            throw new IllegalArgumentException("Pelo menos um critério de busca deve ser fornecido");
        }
    }
}