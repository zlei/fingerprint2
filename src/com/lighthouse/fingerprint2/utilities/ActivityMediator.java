/*
 * For the full copyright and license information, please view the LICENSE file that was distributed
 * with this source code. (c) 2011
 */

package com.lighthouse.fingerprint2.utilities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;


/**
 * Activity Mediator to run different parts of activities
 * 
 * @author Kuban Dzhakipov <kuban.dzhakipov@sibers.com>
 * @version SVN: $Id$
 */
public class ActivityMediator {
  
  private Activity mActivity;
  
  public ActivityMediator(Activity activity){
      mActivity = activity;
  }
  
  protected void startActivity(Class<?> cls){
      Intent intent = new Intent(mActivity, cls);
      mActivity.startActivity(intent);
  }
  
  protected void startActivity(Class<?> cls, Bundle extras){
      Intent intent = new Intent(mActivity, cls);
      intent.replaceExtras(extras);
      mActivity.startActivity(intent);
  }

}