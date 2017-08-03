package com.duvitech.mytestserviceapp;
/*
  ******************************************************************************
  * @file    ${FILE_NAME}
  * @author  George Vigelette
  * @version v1.0.0
  * @date    8/2/2017
  * @brief   
  ******************************************************************************
  * @attention
  *
  * <h2><center>&copy; COPYRIGHT 2017 Duvitech</center></h2>
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  ******************************************************************************
*/

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.ConditionVariable;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

public class NotifyingService extends Service {
    private static final String TAG = "NotifyService";
    private static int MOOD_NOTIFICATIONS = R.layout.status_bar_notifications;
    private NotificationManager mNotificationManager;
    // variable which controls the notification thread
    private ConditionVariable mCondition;

    private Runnable mTask = new Runnable() {
        public void run() {
            Log.i(TAG,"Task Run");
            for (int i = 0; i < 4; ++i) {
                showNotification(R.drawable.stat_happy,
                        R.string.status_bar_notifications_happy_message);
                if (mCondition.block(5 * 1000))
                    break;
                showNotification(R.drawable.stat_neutral,
                        R.string.status_bar_notifications_ok_message);
                if (mCondition.block(5 * 1000))
                    break;
                showNotification(R.drawable.stat_sad,
                        R.string.status_bar_notifications_sad_message);
                if (mCondition.block(5 * 1000))
                    break;
            }
            // Done with our work...  stop the service!
            NotifyingService.this.stopSelf();
        }
    };

    private void showNotification(int moodId, int textId) {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        Log.i(TAG,"Show Notification");
        CharSequence text = getText(textId);

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);

        // Set the icon and timestamp.
        // Note that in this example, we do not set the tickerText.  We update the icon enough that
        // it is distracting to show the ticker text every time it changes.  We strongly suggest
        // that you do this as well.  (Think of of the "New hardware found" or "Network connection
        // changed" messages that always pop up)
        // Set the info for the views that show in the notification panel.
        Notification notification = new Notification.Builder(this)
                .setSmallIcon(moodId)
                .setWhen(System.currentTimeMillis())
                .setContentTitle(getText(R.string.status_bar_notifications_mood_title))
                .setContentText(text)  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .build();

        // Send the notification.
        // We use a layout id because it is a unique number.  We use it later to cancel.
        mNotificationManager.notify(MOOD_NOTIFICATIONS, notification);
    }

    @Override
    public void onCreate() {
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Thread notifyingThread = new Thread(null, mTask, "NotifyingService");
        mCondition = new ConditionVariable(false);
        notifyingThread.start();
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        mNotificationManager.cancel(MOOD_NOTIFICATIONS);
        // Stop the thread from generating further notifications
        mCondition.open();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new Binder() {
        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply,
                                     int flags) throws RemoteException {
            return super.onTransact(code, data, reply, flags);
        }
    };

}
