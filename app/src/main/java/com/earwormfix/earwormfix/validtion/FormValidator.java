package com.earwormfix.earwormfix.validtion;

import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.EditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormValidator {
    private static final FormValidator _instance;

    static {
        _instance = new FormValidator();
    }
    private FormValidator() {
    }
    public static FormValidator getInstance() {
        return _instance;
    }
    public boolean isEmpty(EditText editText) {
        String input = editText.getText().toString().trim();
        return input.length() == 0;

    }
    public boolean isEmail(EditText text) {
        String email = text.getText().toString().trim();
        return (!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches());
    }
    public boolean isPassword(EditText text){
        String password = text.getText().toString().trim();
        return (!TextUtils.isEmpty(password) && password.length()>4 && password.length()<=11);

    }
    public boolean isValidPassword(final String password) {

        Pattern pattern;
        Matcher matcher;
        // .{4,} = anything, at least six places though
        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{4,}$";

        pattern = Pattern.compile(PASSWORD_PATTERN);
        matcher = pattern.matcher(password);

        return matcher.matches();

    }
    public boolean validateName(EditText content) {
        String name= content.getText().toString().trim();
        return name.matches("^[a-zA-Z0-9_.]+");
    }
    public boolean isValidPhone(String content){
        return PhoneNumberUtils.isGlobalPhoneNumber(content) && content.length()>=8;
    }
}
