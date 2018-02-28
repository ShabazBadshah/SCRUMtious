package ca.mvp.scrumtious.scrumtious.view_impl;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import ca.mvp.scrumtious.scrumtious.R;
import ca.mvp.scrumtious.scrumtious.interfaces.view_int.LoginViewInt;
import ca.mvp.scrumtious.scrumtious.presenter_impl.LoginPresenter;
import ca.mvp.scrumtious.scrumtious.utils.SnackbarHelper;
import ca.mvp.scrumtious.scrumtious.utils.UserInputValidator;

public class LoginActivity extends AppCompatActivity implements LoginViewInt {

    private LoginPresenter loginPresenter;
    private LinearLayout loginLayout;
    private EditText emailField, passwordField;
    private TextInputLayout emailFieldLayout, passwordFieldLayout;
    private ProgressDialog signingInProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginPresenter = new LoginPresenter(this);
        loginLayout = (LinearLayout) findViewById(R.id.loginLayout);
        setupFormWatcher();
    }

    // Set up listeners for the text fields
    private void setupFormWatcher() {
        emailField = (EditText) findViewById(R.id.loginEmailField);
        passwordField = (EditText) findViewById(R.id.loginPasswordField);
        emailFieldLayout = (TextInputLayout) findViewById(R.id.loginEmailFieldLayout);
        passwordFieldLayout = (TextInputLayout) findViewById(R.id.loginPasswordFieldLayout);

        // By default, errors should not show until user clicks a field and types, but this
        // also prevents the user from trying to submit an empty form
        emailFieldLayout.setError(null);
        passwordFieldLayout.setError(null);
        emailFieldLayout.setErrorEnabled(true);
        passwordFieldLayout.setErrorEnabled(true);

        // Watch email address field for changes
        emailField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                int isValidEmail = UserInputValidator.isValidEmail(emailField.getText().toString().trim());

                if (isValidEmail < 0) {
                    emailFieldLayout.setErrorEnabled(true);

                    // Email field not set
                    if (isValidEmail == -1)
                        emailFieldLayout.setError("Please enter an e-mail address.");

                        // Email length is 0 or more than max email length
                    else if (isValidEmail == -2)
                        emailFieldLayout.setError("Please enter a valid length e-mail address.");

                        // Email does not match regex
                    else if (isValidEmail == -3)
                        emailFieldLayout.setError("Must provide a valid e-mail address.");

                } else {
                    emailFieldLayout.setError(null);
                    emailFieldLayout.setErrorEnabled(false);
                }
            }
        });
        passwordField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                // Cannot have an empty password
                if (TextUtils.isEmpty(passwordField.getText())) {
                    passwordFieldLayout.setErrorEnabled(true);
                    passwordFieldLayout.setError("Must enter a password.");

                } else {
                    passwordFieldLayout.setError(null);
                    passwordFieldLayout.setErrorEnabled(false);
                }
            }
        });

    }

    // When user clicks login button
    public void onSubmitLogin(View view) {

        String emailAddress = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        // If either field has an error, cannot submit the form
        if (passwordFieldLayout.isErrorEnabled() || emailFieldLayout.isErrorEnabled()) {
            showMessage("Cannot submit until the fields are filled out properly.", false);
            return;
        }

        // Creates a dialog that appears to tell the user that the sign in is occurring
        signingInProgressDialog = new ProgressDialog(this, R.style.AppCompatAlertDialogStyle);
        signingInProgressDialog.setTitle("Sign In");
        signingInProgressDialog.setCancelable(false);
        signingInProgressDialog.setMessage("Attempting to sign you in...");
        signingInProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        signingInProgressDialog.show();

        // Proceed to validate with backend authentication
        loginPresenter.attemptLogin(this, emailAddress, password);
    }

    // If user clicks sign up, take them to the sign up screen
    public void goToSignUpScreen(View view) {
        Intent intent = new Intent(this, SignupActivity.class);
        startActivity(intent);
    }

    // Display message to user
    @Override
    public void showMessage(String message, boolean showAsToast) {
        if (signingInProgressDialog != null && signingInProgressDialog.isShowing()){
        signingInProgressDialog.dismiss();
        }

        // Show message in toast so it persists across activity transitions
        if (showAsToast){
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }

        else {
            // Call the utils class method to handle making the snackbar
            SnackbarHelper.showSnackbar(this, message);
        }
    }

    // On successful login, go to the app's main activity
    @Override
    public void onSuccessfulLogin(String emailAddress, String password) {

        if (signingInProgressDialog != null && signingInProgressDialog.isShowing()) {
            signingInProgressDialog.dismiss();
        }

        SharedPreferences sharedPreferences = this.getSharedPreferences(
                getString(R.string.shared_preferences), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString("emailAddress", emailAddress);
        editor.putString("password", password);
        editor.commit();

        Intent intent = new Intent(LoginActivity.this, ProjectTabsActivity.class);
        startActivity(intent);
        finish();
    }

    // Leave the app if back button is pressed
    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.LoginAlertDialog));
        builder.setTitle("Leave the app?")
                .setMessage("Are you sure you want to exit the app?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // Remove text from fields
                        emailField.setText("");
                        passwordField.setText("");

                        emailFieldLayout.setError(null);
                        passwordFieldLayout.setError(null);

                        loginLayout.requestFocus();

                        // Exit the app
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        startActivity(intent);

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Remain in app
                    }
                })
                .create().show();
    }
}
