/*
 * Copyright (C) 2012 Paul Corriveau
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

package us.socialgoodworking.mocklocation;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import us.socialgoodworking.mocklocation.R;

import android.os.Bundle;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class PolicyActivity extends SherlockFragmentActivity implements OnClickListener  {

	static final String TAG = "PolicyActivity";
	
	//private MenuItem menuFullText;
	
	Button btnNetwork, btnWifi, btnStorage, btnMock, btnEmail;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_policy);
		
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        
        Drawable d = getResources().getDrawable(R.drawable.socialgoodworking_ab_back1);
        actionBar.setBackgroundDrawable(d);

        btnNetwork = (Button)findViewById(R.id.network);
        btnWifi = (Button)findViewById(R.id.wifi);
        btnStorage = (Button)findViewById(R.id.storage);
        btnMock = (Button)findViewById(R.id.mock_location);
        btnEmail = (Button)findViewById(R.id.email);
        
        btnNetwork.setOnClickListener(this);
        btnWifi.setOnClickListener(this);
        btnStorage.setOnClickListener(this);
        btnMock.setOnClickListener(this);
        btnEmail.setOnClickListener(this);
        
        TextView tv = (TextView)findViewById(R.id.privacy_policy);
        tv.setMovementMethod (LinkMovementMethod.getInstance());
        tv.setText (Html.fromHtml (getString (R.string.policy_text)));
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	getSupportMenuInflater().inflate(R.menu.menu_activity_policy, menu);
    	//menuFullText = menu.findItem(R.id.full_text);
        return true;
    }
	
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		
	     switch(item.getItemId()){
	      
	      case android.R.id.home:
	    	  intent = new Intent(this, MockLocationActivity.class);
	          intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	          startActivity(intent);
	          return true;
	          
	      case R.id.full_text:
	    	  Spanned policy = Html.fromHtml(getString(R.string.privacy_policy));
	    	  AlertDialog.Builder dlg = new AlertDialog.Builder(this);
	    	  dlg.setTitle(getString(R.string.menu_policy));
	    	  dlg.setMessage(policy);
	    	  dlg.setCancelable(true).setPositiveButton(android.R.string.ok, null);
	    	  dlg.show();
	    			  
	    	  //showDialog(getString(R.string.menu_policy), getString(R.string.privacy_policy));
	    	  return true;
	        
	      default:
	            return super.onOptionsItemSelected(item);
	     }
	}

	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.network:
				showDialog(getString(R.string.title_network), getString(R.string.access_network));
				break;
				
			case R.id.wifi:
				showDialog(getString(R.string.title_wifi), getString(R.string.access_wifi));
				break;

			case R.id.storage:
				showDialog(getString(R.string.title_storage), getString(R.string.access_storage));
				break;
			
			case R.id.mock_location:
				showDialog(getString(R.string.title_mock), getString(R.string.access_mock));
				break;
				
			case R.id.email:
				showDialog(getString(R.string.title_email), getString(R.string.access_email));
				break;
		}
	}
	
	private void showDialog(String button, String message) {
		AlertDialog.Builder dlg = new AlertDialog.Builder(this);
		dlg.setTitle(button);
		dlg.setMessage(message);
		dlg.setCancelable(true).setPositiveButton(android.R.string.ok, null);
		dlg.show();
	}

}
