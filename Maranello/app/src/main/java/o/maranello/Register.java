package o.maranello;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import o.maranello.clients.RegisterClient;

/**
 * Created by kristianthornley on 27/01/17.
 * A login screen that offers login via user/password.
 */
public class Register extends AppCompatActivity {
    //Common prefs file used throughout the project
    private static final String PREFS_NAME = "MaranelloPrefsFile";
    private static final String TAG = "Register";
    //Keep track of the login task to ensure we can cancel it if requested.
    private UserLoginTask mAuthTask = null;

    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    //Error Message for the screen
    private String message;

    /**
     * Init the screen
     * @param savedInstanceState saved instance data
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG,"Entry: onCreate");
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        message = intent.getStringExtra("message");
        setContentView(R.layout.activity_register);
        // Set up the login form.
        mEmailView = (EditText) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    /**
     * Sets the message if present
     * @param savedInstanceState saved instance data
     */
    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        TextView title = (TextView) findViewById( R.id.message );
        title.setText(message);

    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            //Get Token
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
            mAuthTask = new UserLoginTask(email, password, settings.getString("gsmToken",""));
            mAuthTask.execute((Void) null);
        }
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
        private static final String TAG = "UserLoginTask";
        private final String mEmail;
        private final String mPassword;
        private final String mToken;

        UserLoginTask(String email, String password, String token) {
            mEmail = email;
            mPassword = password;
            mToken = token;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            RequestParams requestParams = new RequestParams();
            requestParams.put("userid",mEmail);
            requestParams.put("password",mPassword);
            requestParams.put("pushKey",mToken);
            Log.d(TAG, "Login from: " + mEmail + " Token: "  + mToken);
            RegisterClient.get(requestParams, new JsonHttpResponseHandler() {
                private static final String TAG = "JsonHttpResponseHandler";
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    Boolean success = false;
                    try {
                        Log.d(TAG, "iotKey received: " + response.get("iotKey"));

                        SharedPreferences sharedAppPreferences = Register.this.getSharedPreferences(PREFS_NAME,0);

                        if(!TextUtils.isEmpty(response.getString("iotKey"))){
                            SharedPreferences.Editor editor = sharedAppPreferences.edit();
                            editor.putString("iotKey", response.getString("iotKey"));
                            editor.putString("deviceId", response.getString("deviceId"));
                            editor.apply();
                            success = true;
                        }
                    }catch (JSONException e){
                        Log.e(TAG, "Exception in JSON");
                    }
                    mAuthTask = null;

                    if (success) {
                        Intent intent = new Intent(Register.this, WifiSettings.class);
                        startActivity(intent);
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showProgress(false);
                                mPasswordView.setError(getString(R.string.error_incorrect_password));
                                mPasswordView.requestFocus();
                            }
                        });
                    }
                }
                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable exception, JSONObject response){
                    Log.e(TAG, "Register failed: " + exception);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mAuthTask = null;
                            showProgress(false);
                            mPasswordView.setError(getString(R.string.error_incorrect_password));
                            mPasswordView.requestFocus();
                        }
                    });


                }
            });
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {



        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

