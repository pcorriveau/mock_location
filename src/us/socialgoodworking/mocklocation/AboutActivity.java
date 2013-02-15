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
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.common.GooglePlayServicesUtil;
import us.socialgoodworking.mocklocation.R;

import android.os.Bundle;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.method.LinkMovementMethod;
//import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class AboutActivity extends SherlockFragmentActivity implements OnClickListener {

	Button btnLegalNotice;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        
        Drawable d = getResources().getDrawable(R.drawable.socialgoodworking_ab_back1);
        actionBar.setBackgroundDrawable(d);
        
        TextView tv = (TextView)findViewById(R.id.about_intro);
        tv.setMovementMethod (LinkMovementMethod.getInstance());
        tv.setText (Html.fromHtml (getString (R.string.way_about)));
        
        btnLegalNotice = (Button)findViewById(R.id.legal_notice);
        btnLegalNotice.setOnClickListener(this);
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
	        
	      default:
	            return super.onOptionsItemSelected(item);
	     }
	}

	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.legal_notice:
	  			String LicenseInfo = GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(getApplicationContext());
	  			AlertDialog.Builder LicenseDialog = new AlertDialog.Builder(this);
	  			LicenseDialog.setTitle("Legal Notices");
	  			LicenseDialog.setMessage(LicenseInfo);
	  			LicenseDialog.show();
				break;
		}
	}

}
