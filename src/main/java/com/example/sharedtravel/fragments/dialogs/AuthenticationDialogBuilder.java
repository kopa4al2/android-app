package com.example.sharedtravel.fragments.dialogs;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;

import com.example.sharedtravel.MainActivity;
import com.example.sharedtravel.R;
import com.example.sharedtravel.callbacks.LoginCallback;
import com.example.sharedtravel.callbacks.RegisterCallback;
import com.example.sharedtravel.model.Validator;
import com.example.sharedtravel.utils.SecurePreferences;

public class AuthenticationDialogBuilder {

    private static AlertDialog shownDialog;


    /**
     * @param context           Context of the activity that called the AlertDialog
     * @param securePreferences SecurePreference instance to retrieve saved user credentials
     * @param callback          callback interface
     */
    public static void showLoginAlertDialog(Context context,
                                            SecurePreferences securePreferences,
                                            LoginCallback callback) {

        if (shownDialog != null && shownDialog.isShowing()) {
            shownDialog.dismiss();
            return;
        }

        LayoutInflater li = LayoutInflater.from(context);
        FrameLayout fl = new FrameLayout(context);
        fl.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        View view = li.inflate(R.layout.login_form, fl);
        shownDialog = new AlertDialog.Builder(context)
                .setView(view)
                .create();

        //FB LOGIN BUTTON
        view.findViewById(R.id.btn_fb).setOnClickListener((v) -> callback.loginWithFacebook(shownDialog));

        final TextInputEditText emailEditText = view.findViewById(R.id.et_login_email);
        final TextInputEditText passwordEditText = view.findViewById(R.id.et_login_password);
        final CheckBox rememberMeCheckBox = view.findViewById(R.id.cb_remember_me);

        String savedEmail = securePreferences.getString(MainActivity.PREF_EMAIL);
        String savedPassword = securePreferences.getString(MainActivity.PREF_PASSWORD);

        if (savedEmail != null || savedPassword != null) {
            emailEditText.setText(savedEmail);
            passwordEditText.setText(savedPassword);
            rememberMeCheckBox.setChecked(true);
        }
        shownDialog.setOnShowListener(dialogInterface -> {
            //Set transparent background
            shownDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            Button cancel = view.findViewById(R.id.btn_cancel);
            cancel.setOnClickListener((v) -> shownDialog.dismiss());

            //Check if email format is valid on focus change
            emailEditText.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    if (emailEditText.getText().toString().isEmpty()) {
                        showInvalidText(view.findViewById(R.id.text_input_login_email),
                                emailEditText,
                                shownDialog.getContext().getResources().getString(R.string.empty_field_error));
                    } else {
                        hideInvalidText(view.findViewById(R.id.text_input_login_email), emailEditText);
                    }
                }
            });

            //Check if password format is valid on focus change
            passwordEditText.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    if (passwordEditText.getText().toString().isEmpty()) {
                        showInvalidText(view.findViewById(R.id.text_input_login_password),
                                passwordEditText,
                                shownDialog.getContext().getResources().getString(R.string.empty_field_error));
                    } else {
                        hideInvalidText(view.findViewById(R.id.text_input_login_password), passwordEditText);
                    }
                }
            });

            //BIND LOGIN BUTTON
            Button button = view.findViewById(R.id.btn_execute_login);


            button.setOnClickListener(v -> {
                final String email = emailEditText.getText().toString();
                final String password = passwordEditText.getText().toString();

                if (email.length() > 0 && password.length() > 0) {
                    boolean rememberCredentials = rememberMeCheckBox.isChecked();
                    callback.loginUser(email, password, shownDialog, rememberCredentials);
                } else {

                    //INVALID FORM
//                    MAYBE SHAKE MAYBE NO SHAKE
                    view.startAnimation(AnimationUtils.loadAnimation(context, R.anim.shake_animation));
                    if (email.length() == 0) {
                        showInvalidText(view.findViewById(R.id.text_input_login_email),
                                emailEditText,
                                shownDialog.getContext().getResources().getString(R.string.empty_field_error));
                    } else {
                        hideInvalidText(view.findViewById(R.id.text_input_login_email), emailEditText);
                    }

                    if (password.length() == 0) {
                        showInvalidText(view.findViewById(R.id.text_input_login_password),
                                passwordEditText,
                                shownDialog.getContext().getResources().getString(R.string.empty_field_error));
                    } else {
                        hideInvalidText(view.findViewById(R.id.text_input_login_password), passwordEditText);
                    }
                }

            });
        });
        shownDialog.show();

    }


    public static void showRegisterAlertDialog(Context context, RegisterCallback callback) {
        if (shownDialog != null && shownDialog.isShowing()) {
            shownDialog.dismiss();
            return;
        }
        LayoutInflater li = LayoutInflater.from(context);
        View view = li.inflate(R.layout.register_form, null);
        shownDialog = new AlertDialog.Builder (context)
                .setView(view)
                .create();


        final TextInputEditText emailEditText = view.findViewById(R.id.et_register_email);
        final TextInputEditText usernameEditText = view.findViewById(R.id.et_register_username);
        final TextInputEditText passwordEditText = view.findViewById(R.id.et_register_password);
        final TextInputLayout emailTextInputLayout = view.findViewById(R.id.text_input_register_email);
        final TextInputLayout usernameTextInputLayout = view.findViewById(R.id.text_input_register_username);
        final TextInputLayout passwordTextInputLayout = view.findViewById(R.id.text_input_register_password);
        final String invalidEmailMessage = context.getResources().getString(R.string.invalid_email);
        final String invalidUsernameMessage = context.getResources().getString(R.string.invalid_username);
        final String invalidPasswordMessage = context.getResources().getString(R.string.invalid_password);


        emailEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                if (!Validator.validateEmail(emailEditText.getText().toString())) {
                    showInvalidText(emailTextInputLayout, emailEditText, invalidEmailMessage);
                } else {
                    hideInvalidText(emailTextInputLayout, emailEditText);
                }
            }
        });
        usernameEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                if (!Validator.validateUsername(usernameEditText.getText().toString()))
                    showInvalidText(usernameTextInputLayout, usernameEditText, invalidUsernameMessage);
                else
                    hideInvalidText(usernameTextInputLayout, usernameEditText);
            }
        });
        passwordEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                if (!Validator.validatePassword(passwordEditText.getText().toString())) {
                    showInvalidText(passwordTextInputLayout, passwordEditText, invalidPasswordMessage);
                } else {
                    hideInvalidText(passwordTextInputLayout, passwordEditText);
                }
            }
        });
        shownDialog.setOnShowListener(dialogInterface -> {
            shownDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            Button button = view.findViewById(R.id.btn_execute_register);
            Button cancel = view.findViewById(R.id.btn_cancel);

            cancel.setOnClickListener((v) -> shownDialog.dismiss());

            button.setOnClickListener(btnView -> {
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                String username = usernameEditText.getText().toString();
                if (Validator.validateEmail(email)
                        && Validator.validatePassword(password)
                        && Validator.validateUsername(username)) {
                    //ALL IS GOOD
                    callback.registerUser(email, password, username, shownDialog);
                } else {

                    if (!Validator.validateUsername(username))
                        showInvalidText(usernameTextInputLayout, usernameEditText, invalidUsernameMessage);
                    if (!Validator.validateEmail(email)) {
                        showInvalidText(emailTextInputLayout, emailEditText, invalidEmailMessage);
                    }
                    if (!Validator.validatePassword(password)) {
                        showInvalidText(passwordTextInputLayout, passwordEditText, invalidPasswordMessage);
                    }

                    //shaky shaky
                    view.startAnimation(AnimationUtils.loadAnimation(context, R.anim.shake_animation));

                }
            });
        });


        shownDialog.show();
    }


    private static void showInvalidText(TextInputLayout textInputLayout,
                                        TextInputEditText textInputEditText,
                                        String invalidText) {
        Drawable errorIcon = textInputLayout.getContext().getResources().getDrawable(R.drawable.ic_error_outline);
        Drawable leftDrawable = textInputEditText.getCompoundDrawables()[0];
        textInputEditText.setCompoundDrawablesWithIntrinsicBounds(leftDrawable, null, errorIcon, null);

        textInputLayout.setErrorEnabled(true);
        textInputLayout.setError(invalidText);
    }

    private static void hideInvalidText(TextInputLayout textInputLayout, TextInputEditText textInputEditText) {
        Drawable leftDrawable = textInputEditText.getCompoundDrawables()[0];
        textInputLayout.setErrorEnabled(false);

        textInputEditText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

        if (leftDrawable != null) {
            textInputEditText.setCompoundDrawablesWithIntrinsicBounds(leftDrawable, null, null, null);
        }
    }
}

/*  public static void showLoginAlertDialog(Context context,
                                            SecurePreferences securePreferences,
                                            LoginCallback callback) {

        if (shownDialog != null && shownDialog.isShowing()) {
            shownDialog.dismiss();
            return;
        }

        LayoutInflater li = LayoutInflater.from(context);
        FrameLayout fl = new FrameLayout(context);
        fl.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        View view = li.inflate(R.layout.login_form, fl);
        shownDialog = new AlertDialog.Builder(context)
                .setView(view)
                .create();

        //FB LOGIN BUTTON
        view.findViewById(R.id.btn_fb).setOnClickListener((v) -> callback.loginWithFacebook(shownDialog));

        final TextInputEditText emailEditText = view.findViewById(R.id.et_login_email);
        final TextInputEditText passwordEditText = view.findViewById(R.id.et_login_password);
        final CheckBox rememberMeCheckBox = view.findViewById(R.id.cb_remember_me);

        String savedEmail = securePreferences.getString(MainActivity.PREF_EMAIL);
        String savedPassword = securePreferences.getString(MainActivity.PREF_PASSWORD);

        if (savedEmail != null || savedPassword != null) {
            emailEditText.setText(savedEmail);
            passwordEditText.setText(savedPassword);
            rememberMeCheckBox.setChecked(true);
        }
        shownDialog.setOnShowListener(dialogInterface -> {
            //Set transparent background
            Objects.requireNonNull(
                    shownDialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

            Button cancel = view.findViewById(R.id.btn_cancel);
            cancel.setOnClickListener((v) -> shownDialog.dismiss());

            //Check if email format is valid on focus change
            emailEditText.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    if (emailEditText.getText().toString().isEmpty()) {
                        showInvalidText(view.findViewById(R.id.text_input_login_email),
                                emailEditText,
                                shownDialog.getContext().getResources().getString(R.string.empty_field_error));
                    } else {
                        hideInvalidText(view.findViewById(R.id.text_input_login_email), emailEditText);
                    }
                }
            });

            //Check if password format is valid on focus change
            passwordEditText.setOnFocusChangeListener((v, hasFocus) -> {
                if (!hasFocus) {
                    if (passwordEditText.getText().toString().isEmpty()) {
                        showInvalidText(view.findViewById(R.id.text_input_login_password),
                                passwordEditText,
                                shownDialog.getContext().getResources().getString(R.string.empty_field_error));
                    } else {
                        hideInvalidText(view.findViewById(R.id.text_input_login_password), passwordEditText);
                    }
                }
            });

            //BIND LOGIN BUTTON
            Button button = view.findViewById(R.id.btn_execute_login);


            button.setOnClickListener(v -> {
                final String email = emailEditText.getText().toString();
                final String password = passwordEditText.getText().toString();

                if (email.length() > 0 && password.length() > 0) {
                    boolean rememberCredentials = rememberMeCheckBox.isChecked();
                    callback.loginUser(email, password, shownDialog, rememberCredentials);
                } else {

                    //INVALID FORM
//                    MAYBE SHAKE MAYBE NO SHAKE
                    view.startAnimation(AnimationUtils.loadAnimation(context, R.anim.shake_animation));
                    if (email.length() == 0) {
                        showInvalidText(view.findViewById(R.id.text_input_login_email),
                                emailEditText,
                                shownDialog.getContext().getResources().getString(R.string.empty_field_error));
                    } else {
                        hideInvalidText(view.findViewById(R.id.text_input_login_email), emailEditText);
                    }

                    if (password.length() == 0) {
                        showInvalidText(view.findViewById(R.id.text_input_login_password),
                                passwordEditText,
                                shownDialog.getContext().getResources().getString(R.string.empty_field_error));
                    } else {
                        hideInvalidText(view.findViewById(R.id.text_input_login_password), passwordEditText);
                    }
                }

            });
        });
        shownDialog.show();

    }
    */
