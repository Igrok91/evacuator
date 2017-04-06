package maps.evacuatorv20.auth;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;



import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import maps.evacuatorv20.Constants.Constants;
import maps.evacuatorv20.MapsActivity;
import maps.evacuatorv20.R;
import maps.evacuatorv20.auth.model.Message;
import maps.evacuatorv20.auth.model.User;
import maps.evacuatorv20.data.UserContract;
import maps.evacuatorv20.data.UserDBHelper;

/**
 * @author Igor Ryabtsev
 */
public class UserActivity extends AbstractAsyncActivity {

    protected static final String TAG = FirstActivity.class.getSimpleName();


    private TextView first_name_view;

    private TextView last_name_view;

    private TextView email_view;

    private Pattern pattern;

    private Matcher matcher;

    private UserDBHelper userDBHelper;

    private String first_name;

    private String last_name;

    private String email;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        first_name_view = (TextView)findViewById(R.id.first_name);
        last_name_view = (TextView)findViewById(R.id.last_name);
        email_view = (TextView)findViewById(R.id.email_user);
        final Button  button = (Button)findViewById(R.id.users_sign_in_button);
        userDBHelper = new UserDBHelper(this);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();

            }
        });

    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {


        // Reset errors.
        first_name_view.setError(null);
        last_name_view.setError(null);
        email_view.setError(null);



        first_name = first_name_view.getText().toString();
        last_name = last_name_view.getText().toString();
        email = email_view.getText().toString();


        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(first_name)) {
            first_name_view.setError(getString(R.string.fieldisempty));
            focusView = first_name_view;
            cancel = true;
        }

        if(TextUtils.isEmpty(last_name)) {
            last_name_view.setError(getString(R.string.fieldisempty));
            focusView = last_name_view;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            email_view.setError(getString(R.string.fieldisempty));
            focusView = email_view;
            cancel = true;
        } else if (!isEmailValid(email)) {
            email_view.setError(getString(R.string.error_invalid_email));
            focusView = email_view;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            new FetchResourceTask().execute();
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    // ***************************************
    // Private methods
    // ***************************************
    private void displayResponse(Message response) {
        Toast.makeText(this, response.getText(), Toast.LENGTH_LONG).show();
    }

    private void displayResponse(String response) {
        Toast.makeText(this, response, Toast.LENGTH_LONG).show();
    }

    // ***************************************
    // Private classes
    // ***************************************
    private class FetchResourceTask extends AsyncTask<Void, Void, Message> {


        private Message message;

        private SharedPreferences preferences;

        private String number;



        @Override
        protected void onPreExecute() {
            showLoadingProgressDialog();
            Intent intent = getIntent();
            number = intent.getStringExtra(Constants.APP_PREFERENCES_PHONE);

            message = new Message();
            User user = new User();
            user.setEmail(email);
            user.setFirstName(first_name);
            user.setLastName(last_name);
            message.setUser(user);
            // build the message object

        }

        @Override
        protected Message doInBackground(Void... params) {
            final String url = getString(R.string.base_uri) + "/setdatauser/" + number;

            // Populate the HTTP Basic Authentitcation header with the number_phone and password
            // Create a new RestTemplate instance
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
                 try {
                // Make the network request
                Log.d(TAG, url);
                      return restTemplate.postForObject(url, message, Message.class);
            } catch (HttpClientErrorException e) {
                Log.e(TAG, e.getLocalizedMessage(), e);
                return new Message(0, e.getStatusText(), e.getLocalizedMessage());
            } catch (ResourceAccessException e) {
                Log.e(TAG, e.getLocalizedMessage(), e);
                return new Message(0, e.getClass().getSimpleName(), e.getLocalizedMessage());
            }

        }

        @Override
        protected void onPostExecute(Message result) {
            dismissProgressDialog();
            saveDataUserOnDb();
            saveUserOnSetting();
            displayResponse(result);
          if(result != null) {
                Intent intent = new Intent(UserActivity.this, MapsActivity.class);
                intent.putExtra(Constants.APP_PREFERENCES_PHONE, result.getNumber());
                startActivity(intent);
            } else  displayResponse(Constants.USER_REGISTER_ERROR);


        }

        private boolean saveUserOnSetting() {
            preferences = getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(Constants.APP_PREFERENCES_PHONE, number);
            if (editor.commit()) {
                return true;
            }
            return false;
        }

        private boolean saveDataUserOnDb() {
            SQLiteDatabase database = userDBHelper.getWritableDatabase();
            ContentValues content = new ContentValues();
            content.put( UserContract.User.COLUMN_NAME, first_name);
            content.put( UserContract.User.COLUMN_LAST_NAME, last_name);
            content.put( UserContract.User.COLUMN_EMAIL, email);
            content.put( UserContract.User.COLUMN_TELEPHONE, number);
            long newRowId = database.insert(UserContract.User.TABLE_NAME, null, content);

            if (newRowId == -1) {
                // Если ID  -1, значит произошла ошибка
                displayResponse(Constants.USER_REGISTER_ERROR);
                return false;

            } else {
                displayResponse(Constants.USER_REGISTER_SUCCESS);
                return true;
            }
        }


    }


}
