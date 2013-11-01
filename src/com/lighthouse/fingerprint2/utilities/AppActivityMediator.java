/*
 * For the full copyright and license information, please view the LICENSE file that was distributed
 * with this source code. (c) 2011
 */

package com.lighthouse.fingerprint2.utilities;

import android.app.Activity;
import android.os.Bundle;

import com.lighthouse.fingerprint2.activities.MainMenuActivity;
import com.lighthouse.fingerprint2.activities.MapListActivity;

/**
 * Application Activity Mediator
 * 
 * @author Kuban Dzhakipov <kuban.dzhakipov@sibers.com>
 * @version SVN: $Id$
 */
public class AppActivityMediator extends ActivityMediator {

	protected Bundle bundle = new Bundle();

	public enum mode {
		OPEN
	}

	public enum params {
		BUILDING_ID, MAP_NAME
	}

	public AppActivityMediator(Activity activity) {
		super(activity);
	}

	public void goHome() {
		startActivity(MainMenuActivity.class);
	}

	public void goFloors(String buildingId) {
		bundle.putInt(mode.OPEN.toString(), MapListActivity.GET_FLOORS);
		bundle.putString(params.BUILDING_ID.toString(), buildingId);
		startActivity(MainMenuActivity.class, bundle);
	}

}
