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

import us.socialgoodworking.mocklocation.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class RecordRouteDialog  extends DialogFragment {

    public interface RecordRouteDialogListener {
        public void onRecordRouteDialogPositiveClick(DialogInterface dialog, String routeName, int interval);
        public void onRecordRouteDialogNegativeClick(DialogInterface dialog);
    }
    
	boolean bSwitchButtonPlacement = !MockLocationActivity.SUPPORTS_ICE_CREAM_SANDWICH;
	RecordRouteDialogListener listener;
	EditText routeName;
	EditText interval;

	
	public RecordRouteDialog() {
	}
	
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        
        View v = inflater.inflate(R.layout.activity_route, null);
        
		routeName = (EditText) v.findViewById(R.id.route_name);
		interval = (EditText) v.findViewById(R.id.update_interval);

        builder.setView(v);
        builder.setTitle("Record a Route");
        
       	if ( bSwitchButtonPlacement ) {
       		// Really the positive, but reversed to be compatible with ICS and onward.
       		builder.setCancelable(false).setPositiveButton(android.R.string.cancel,	new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					listener.onRecordRouteDialogNegativeClick(dialog);
				}
			})
			.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					int i = 5;
					if (!interval.getText().toString().isEmpty())
						i = Integer.parseInt(interval.getText().toString());
					listener.onRecordRouteDialogPositiveClick(dialog, routeName.getText().toString(), i);
				}
			});
    	}
    	else {
    		builder.setCancelable(false).setPositiveButton(android.R.string.cancel,	new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					listener.onRecordRouteDialogNegativeClick(dialog);
				}
			})
			.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					int i = 5;
					if (!interval.getText().toString().isEmpty())
						i = Integer.parseInt(interval.getText().toString());
					listener.onRecordRouteDialogPositiveClick(dialog, routeName.getText().toString(), i);
				}
			});
    	}
        
        return builder.create();
     }
    
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            listener = (RecordRouteDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement RecordRouteDialogListener");
        }
    }    
}
