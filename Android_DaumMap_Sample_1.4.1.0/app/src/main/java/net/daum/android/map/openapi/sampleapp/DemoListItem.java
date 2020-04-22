package net.daum.android.map.openapi.sampleapp;

import androidx.fragment.app.FragmentActivity;

import net.daum.android.map.openapi.sampleapp.demos.CameraDemoActivity;
import net.daum.android.map.openapi.sampleapp.demos.EventsDemoActivity;
import net.daum.android.map.openapi.sampleapp.demos.LocationDemoActivity;
import net.daum.android.map.openapi.sampleapp.demos.MapViewDemoActivity;
import net.daum.android.map.openapi.sampleapp.demos.MarkerDemoActivity;
import net.daum.android.map.openapi.sampleapp.demos.PolygonDemoActivity;


class DemoListItem {
    final int titleId;
    final int descriptionId;
    final Class<? extends FragmentActivity> activity;

    private DemoListItem(int titleId, int descriptionId,
                         Class<? extends FragmentActivity> activity) {
        this.titleId = titleId;
        this.descriptionId = descriptionId;
        this.activity = activity;
    }

    static final DemoListItem[] DEMO_LIST_ITEMS = {
            new DemoListItem(R.string.map_view_demo_title, R.string.map_view_demo_desc, MapViewDemoActivity.class),
            new DemoListItem(R.string.marker_demo_title, R.string.marker_demo_desc, MarkerDemoActivity.class),
            new DemoListItem(R.string.polygon_demo_title, R.string.polygon_demo_desc, PolygonDemoActivity.class),
            new DemoListItem(R.string.location_demo_title, R.string.location_demo_desc, LocationDemoActivity.class),
            new DemoListItem(R.string.camera_demo_title, R.string.camera_demo_desc, CameraDemoActivity.class),
            new DemoListItem(R.string.events_demo_title, R.string.events_demo_desc, EventsDemoActivity.class)
    };
}
