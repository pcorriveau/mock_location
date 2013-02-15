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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class SelectRouteDialog extends DialogFragment {

	private String [] routes;
	private String selectedRoute;

    public interface SelectRouteDialogListener {
        public void onSelectRouteDialogPositiveClick(DialogInterface dialog, String selectedRoute);
        public void onSelectRouteDialogNegativeClick(DialogInterface dialog);
    }

	boolean bSwitchButtonPlacement = !MockLocationActivity.SUPPORTS_ICE_CREAM_SANDWICH;
	SelectRouteDialogListener listener;

	public SelectRouteDialog() {
	}
	
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
    	Bundle b = this.getArguments();
    	routes = b.getStringArray("routes");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select a Route");
        
       	if ( bSwitchButtonPlacement ) {
       		// Really the positive, but reversed to be compatible with ICS and onward.
       		builder.setCancelable(false).setPositiveButton(android.R.string.cancel,	new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					listener.onSelectRouteDialogNegativeClick(dialog);
				}
			})
			.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					listener.onSelectRouteDialogPositiveClick(dialog, selectedRoute);
				}
			});
    	}
    	else {
    		builder.setCancelable(false).setPositiveButton(android.R.string.cancel,	new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					listener.onSelectRouteDialogNegativeClick(dialog);
				}
			})
			.setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					listener.onSelectRouteDialogPositiveClick(dialog, selectedRoute);
				}
			});
    	}
        
        builder.setSingleChoiceItems(routes, -1, new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface dialog, int which) {
        		selectedRoute = routes[which];
            }
        });
        
        return builder.create();
     }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            listener = (SelectRouteDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement SelectRouteDialogListener");
        }
    }    
}
