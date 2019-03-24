package com.example.sharedtravel.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validator {

    //REGEX IS FROM STACK OVERFLOW
    private static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    private static Pattern VALIDATE_PHONE_NUMBER_REGEX =
            Pattern.compile("^0[0-9]{9}$");

    public static boolean validateEmail(String emailStr) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return matcher.find();
    }

    public static boolean validatePassword(String password) {
        return password.length() >= 6;
    }

    public static boolean validateUsername(String textToValidate) {
        return textToValidate != null
                && !textToValidate.isEmpty()
                && textToValidate.length() < 16;
    }

    public static boolean validatePhone(String textToValidate) {
        return textToValidate != null
                && !textToValidate.isEmpty()
                && textToValidate.length() > 0
                && VALIDATE_PHONE_NUMBER_REGEX.matcher(textToValidate).find();
    }

    public static boolean validateMoreInfo(String textToValidate) {
        return textToValidate.length() < 256;
    }
}
