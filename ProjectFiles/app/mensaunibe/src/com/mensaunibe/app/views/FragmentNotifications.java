package com.mensaunibe.app.views;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.mensaunibe.R;
import com.mensaunibe.app.controller.Controller;
import com.mensaunibe.app.model.DataHandler;
import com.mensaunibe.app.model.User;
import com.mensaunibe.app.model.UserNotification;
import com.mensaunibe.util.gui.CustomListViewPullToRefresh;
import com.mensaunibe.util.gui.CustomListViewPullToRefresh.OnRefreshListener;

import android.graphics.PorterDuff.Mode;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Toast;

/**
 * Fragment that appears in the "content_frame", shows notifications
 */
public class FragmentNotifications extends Fragment {
	
	// for logging and debugging purposes
	@SuppressWarnings("unused")
	private static final String TAG = FragmentNotifications.class.getSimpleName();
	
	private Controller mController;
	private DataHandler mDataHandler;
	private User mUser;
	
	private SimpleAdapter mAdapter;
	private ProgressBar mProgressBar;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		this.mController = Controller.getController();
		this.mDataHandler = Controller.getDataHandler();
		this.mUser = mDataHandler.getUser();
		
		View rootView = inflater.inflate(R.layout.fragment_notifications, container, false);
		
		// set up the progress bar
        mProgressBar = (ProgressBar) rootView.findViewById (R.id.progressbar);
        // add nice color to the progress bar
        mProgressBar.getProgressDrawable().setColorFilter(0xffE3003D, Mode.SRC_IN);

		// get the list view from the layout into a variable, it's important
		// to fetch it from the rootView
		final CustomListViewPullToRefresh listview = (CustomListViewPullToRefresh) rootView.findViewById(R.id.notifications);
		
		// disable scrolling when list is refreshing
		listview.setLockScrollWhileRefreshing(false);

		// override the default strings
		listview.setTextPullToRefresh("Ziehen f�r Update");
		listview.setTextReleaseToRefresh("Loslassen f�r Update");
		listview.setTextRefreshing("Lade Daten...");

		// set the onRefreshListener for the pull down listview
		listview.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {
				// For demo purposes, the code will pause here to
				// force a delay when invoking the refresh
				listview.postDelayed(new Runnable() {
					@Override
					public void run() {
						
						listview.onRefreshComplete("Notifications neu geladen");
					}
				}, 2000);
			}
		});

		// Fetch the string array from resouce arrays.xml > mensalist
		// String[] notifications =
		// getResources().getStringArray(R.array.notificationlist);
		ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();

		List<UserNotification> notifications = mUser.getNotificationList().getNotifications();

		// Creating an array adapter to store the list of countries
		// ArrayAdapter<String> adapter = new
		// ArrayAdapter<String>(inflater.getContext(),
		// R.layout.list_item_1line, notifications);
		HashMap<String, String> item;
		for (UserNotification notification : notifications) {
			item = new HashMap<String, String>();
			item.put("line1", notification.getFrom());
			item.put("line2", notification.getMessageShortened());
			list.add(item);
		}

		mAdapter = new SimpleAdapter(inflater.getContext(), list,
				R.layout.list_notificationlist_item, new String[] {
						"line1", "line2" }, new int[] { R.id.line1,
						R.id.line2 });

		// setting the adapter for the ListView
		listview.setAdapter(mAdapter);

		Toast.makeText(mController, "Hier werden alle Notifications angezeigt", Toast.LENGTH_SHORT).show();
		
		return rootView;
	}
}
