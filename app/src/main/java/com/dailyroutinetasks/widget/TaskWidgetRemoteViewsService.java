package com.dailyroutinetasks.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class TaskWidgetRemoteViewsService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new TaskWidgetRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}