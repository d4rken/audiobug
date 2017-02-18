package eu.thedarken.audiobug;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

public class RecorderWidget extends AppWidgetProvider {
    public static final String ACTION_TOGGLE_RECORDING = "eu.thedarken.audiobug.ACTION_TOGGLE_RECORDING";
    private static final String TAG = "AB:ABWidget";

    @Override
    public void onEnabled(Context context) {
        Log.d(TAG, "Widget created");
        super.onEnabled(context);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d(TAG, "Update");
        Intent intent = new Intent(context, AudioBugService.class);
        intent.setAction(ACTION_TOGGLE_RECORDING);
        PendingIntent myPI = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        views.setOnClickPendingIntent(R.id.ib_widgetbutton, myPI);
        AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        mgr.updateAppWidget(appWidgetIds, views);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(ACTION_TOGGLE_RECORDING)) {
            Intent serviceIntent = new Intent(context, AudioBugService.class);
            serviceIntent.setAction(ACTION_TOGGLE_RECORDING);
            context.startService(serviceIntent);
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        Log.d(TAG, "Widget destroyed!");
        super.onDeleted(context, appWidgetIds);
    }
}
