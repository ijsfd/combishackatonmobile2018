package com.immutables.trycodecatch.trycodecatchtest.Activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.immutables.trycodecatch.trycodecatchtest.ApplicationContext;
import com.immutables.trycodecatch.trycodecatchtest.Models.BackendModels.LoginModel;
import com.immutables.trycodecatch.trycodecatchtest.Models.BackendModels.LoginResponse;
import com.immutables.trycodecatch.trycodecatchtest.Models.BackendModels.RegisterResponse;
import com.immutables.trycodecatch.trycodecatchtest.Models.BackendModels.UserRegister;
import com.immutables.trycodecatch.trycodecatchtest.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Response;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class RegisterActivity extends AppCompatActivity implements LoaderCallbacks<Cursor>
{

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[] { "foo@example.com:hello", "bar@example.com:world" };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserRegisterTask mAuthTask = null;

    // UI references.
    private View mProgressView;
    private View mLoginFormView;
    private EditText mFirstNameView;
    private EditText mLastNameView;
    private EditText mPhoneNumberView;
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private EditText mBirthYearView;
    private Spinner mGenderSpinner;
    private Spinner mBloodTypeSpinner;
    private EditText mPlaceOfResidenceView;
    private AlertDialog.Builder dialogBuilder;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        // Set up the login form.
        populateAutoComplete();
        mFirstNameView = (EditText) findViewById(R.id.firstName);
        mLastNameView = (EditText) findViewById(R.id.lastName);
        mPhoneNumberView = (EditText) findViewById(R.id.phone_number);
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mBirthYearView = (EditText) findViewById(R.id.birth_year);
        mGenderSpinner = (Spinner) findViewById(R.id.gender_spinner);
        List<String> spinnerArrayGender =  new ArrayList<String>();
        spinnerArrayGender.add("M");
        spinnerArrayGender.add("F");
        ArrayAdapter<String> adapterGender = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, spinnerArrayGender);

        adapterGender.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner sGenderItems = (Spinner) findViewById(R.id.gender_spinner);
        sGenderItems.setAdapter(adapterGender);
        mBloodTypeSpinner = (Spinner) findViewById(R.id.blood_type);
        List<String> spinnerArrayBlood =  new ArrayList<String>();
        spinnerArrayBlood.add("0-");
        spinnerArrayBlood.add("0+");
        spinnerArrayBlood.add("A-");
        spinnerArrayBlood.add("A+");
        spinnerArrayBlood.add("B-");
        spinnerArrayBlood.add("B+");
        spinnerArrayBlood.add("AB-");
        spinnerArrayBlood.add("AB+");
        ArrayAdapter<String> adapterBlood = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, spinnerArrayBlood);

        adapterBlood.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner sBloodItems = (Spinner) findViewById(R.id.blood_type);
        sBloodItems.setAdapter(adapterBlood);
        mPlaceOfResidenceView = (EditText) findViewById(R.id.place_of_residence);

        mPlaceOfResidenceView.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent)
            {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL)
                {
                    dialogBuilder.show();
                    return true;
                }
                return false;
            }
        });

        Button register = (Button) findViewById(R.id.register_button);
        register.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                dialogBuilder.show();
            }
        });

        Button login = (Button) findViewById(R.id.go_to_login_screen_button);
        login.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent loginActivityIntent = new Intent(RegisterActivity.this, LoginActivity.class);
                loginActivityIntent.setFlags(loginActivityIntent.getFlags() | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(loginActivityIntent);
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        attemptRegister();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                }
            }
        };

        dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage("By registering you agree that your personal data can be used and stored " +
                "on our platform.").setPositiveButton("Agree", dialogClickListener)
                .setNegativeButton("Do not agree", dialogClickListener);
    }

    private void populateAutoComplete()
    {
        if (!mayRequestContacts())
        {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts()
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
        {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED)
        {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS))
        {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE).setAction(android.R.string.ok, new View.OnClickListener()
            {
                @Override
                @TargetApi(Build.VERSION_CODES.M)
                public void onClick(View v)
                {
                    requestPermissions(new String[] { READ_CONTACTS }, REQUEST_READ_CONTACTS);
                }
            });
        }
        else
        {
            requestPermissions(new String[] { READ_CONTACTS }, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if (requestCode == REQUEST_READ_CONTACTS)
        {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptRegister()
    {
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String firstName = mFirstNameView.getText().toString();
        String lastName = mLastNameView.getText().toString();
        String phoneNumber = mPhoneNumberView.getText().toString();
        String email = mEmailView.getText().toString();
        String yearOfBirth = mBirthYearView.getText().toString();
        String gender = (String)(mGenderSpinner.getSelectedItem());
        String address = mPlaceOfResidenceView.getText().toString();
        String bloodType = (String)(mBloodTypeSpinner.getSelectedItem());
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password) || !isPasswordValid(password))
        {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(address))
        {
            mPlaceOfResidenceView.setError(getString(R.string.error_field_required));
            focusView = mPlaceOfResidenceView;
            cancel = true;
        }

        if (TextUtils.isEmpty(yearOfBirth))
        {
            mBirthYearView.setError(getString(R.string.error_field_required));
            focusView = mBirthYearView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email))
        {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }
        else if (!isEmailValid(email))
        {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (TextUtils.isEmpty(phoneNumber))
        {
            mPhoneNumberView.setError(getString(R.string.error_field_required));
            focusView = mPhoneNumberView;
            cancel = true;
        }


        if (TextUtils.isEmpty(lastName))
        {
            mLastNameView.setError(getString(R.string.error_field_required));
            focusView = mLastNameView;
            cancel = true;
        }

        if (TextUtils.isEmpty(firstName))
        {
            mFirstNameView.setError(getString(R.string.error_field_required));
            focusView = mFirstNameView;
            cancel = true;
        }

        if (cancel)
        {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        }
        else
        {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            UserRegister user = new UserRegister(firstName, lastName, phoneNumber, email, yearOfBirth, gender, address, bloodType, password);
            mAuthTask = new UserRegisterTask(user);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email)
    {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password)
    {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show)
    {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2)
        {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(show ? 0 : 1).setListener(new AnimatorListenerAdapter()
            {
                @Override
                public void onAnimationEnd(Animator animation)
                {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter()
            {
                @Override
                public void onAnimationEnd(Animator animation)
                {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        }
        else
        {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle)
    {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI, ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE + " = ?", new String[] { ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE },

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor)
    {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader)
    {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection)
    {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(RegisterActivity.this, android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery
    {
        String[] PROJECTION = { ContactsContract.CommonDataKinds.Email.ADDRESS, ContactsContract.CommonDataKinds.Email.IS_PRIMARY, };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserRegisterTask extends AsyncTask<Void, Void, Boolean>
    {

        private final UserRegister user;

        UserRegisterTask(UserRegister user)
        {
            this.user = user;
        }

        @Override
        protected Boolean doInBackground(Void... params)
        {
            Call<RegisterResponse> registerCall = ApplicationContext.backendService.registerUser(user);
            try
            {
                Response response = registerCall.execute();
                if (response.isSuccessful())
                {
                    RegisterResponse registerResponse = (RegisterResponse) response.body();
                    if (!registerResponse.success)
                    {
                        Snackbar.make(mEmailView, "Unable to create user.", Snackbar.LENGTH_LONG).show();
                        return false;
                    }
                    else
                    {
                        LoginModel loginModel = new LoginModel(mEmailView.getText().toString(), mPasswordView.getText().toString());
                        Call<LoginResponse> loginCall = ApplicationContext.backendService.loginUser(loginModel);
                        Response response1 = loginCall.execute();
                        if (response1.isSuccessful())
                        {
                            LoginResponse loginResponse = (LoginResponse) response1.body();
                            if (loginResponse.success)
                            {
                                ApplicationContext.token = loginResponse.data.token;
                                ApplicationContext.loggedInUser = loginResponse.data.user;
                                return true;
                            }
                        }
                    }
                }
                else
                {
                    Snackbar.make(mEmailView, "Problem communicating with the server.", Snackbar.LENGTH_LONG).show();
                    return false;
                }
            }
            catch (IOException e)
            {
                Log.d("Register error", e.getMessage());
                return false;
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success)
        {
            mAuthTask = null;
            showProgress(false);

            if (success)
            {
                Intent mainActivityIntent = new Intent(RegisterActivity.this, MainActivity.class);
                mainActivityIntent.setFlags(mainActivityIntent.getFlags() | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(mainActivityIntent);
            }
            else
            {

                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled()
        {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

