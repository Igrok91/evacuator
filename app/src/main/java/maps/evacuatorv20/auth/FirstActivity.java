/*
 * Copyright 2010-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package maps.evacuatorv20.auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;



import org.springframework.http.HttpAuthentication;
import org.springframework.http.HttpBasicAuthentication;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import br.com.sapereaude.maskedEditText.MaskedEditText;
import maps.evacuatorv20.Constants.Constants;
import maps.evacuatorv20.R;
import maps.evacuatorv20.auth.model.Message;

/**
 * @author Igor Ryabtsev
 */
public class FirstActivity extends AbstractAsyncActivity {

	protected static final String TAG = FirstActivity.class.getSimpleName();



	// UI references.
	private MaskedEditText mEmailView;
	private TextView texterror;
	private View mLoginFormView;
	private SharedPreferences preferences;
	private Pattern pattern;
	private Matcher matcher;



	// ***************************************
	// Activity methods
	// ***************************************
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		preferences = getSharedPreferences(Constants.APP_PREFERENCES, Context.MODE_PRIVATE);
		if (preferences.contains(Constants.APP_PREFERENCES_PHONE)) {
				displayResponse("Пользователь авторизован уже блеать");
		} else {

			setContentView(R.layout.activity_login);
			pattern = Pattern.compile(Constants.APP_PATTERN_NUMBER);

			// Initiate the request to the protected service
			final Button submitButton = (Button) findViewById(R.id.email_sign_in_button);
			submitButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					mEmailView = (MaskedEditText) findViewById(R.id.number_phone);
					matcher = pattern.matcher(mEmailView.getRawText());


					if (matcher.matches()) {
						FetchSecuredResourceTask task =	new FetchSecuredResourceTask();
						task.execute();


					} else {
				     	texterror = (TextView)findViewById(R.id.textView2);
						texterror.clearComposingText();
						texterror.setText(R.string.error_text);
					}
				}
			});
		}
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
	private class FetchSecuredResourceTask extends AsyncTask<Void, Void, Message> {

		private String username;

		private String password = "pass";

		private Message message;

		@Override
		protected void onPreExecute() {
			showLoadingProgressDialog();

			// build the message object

			this.username = mEmailView.getRawText();
			Log.d("my", username);
		}

		@Override
		protected Message doInBackground(Void... params) {
			final String url = getString(R.string.base_uri) + "/register/" + username;

			// Populate the HTTP Basic Authentitcation header with the number_phone and password
			HttpAuthentication authHeader = new HttpBasicAuthentication(username, password);
			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.setAuthorization(authHeader);
			requestHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

			// Create a new RestTemplate instance
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());

			try {
				// Make the network request
			    Log.d(TAG, url);
			//	ResponseEntity<Message> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<Object>(requestHeaders), Message.class);
				return restTemplate.getForObject(url, Message.class);
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

			if (result != null) {
				Intent intent = new Intent(getBaseContext(), UserActivity.class);
				intent.putExtra(Constants.APP_PREFERENCES_PHONE, result.getNumber());
				startActivity(intent);
				displayResponse(result);
			} else displayResponse("Ошибка регистрации");



		}



	}
	
}
