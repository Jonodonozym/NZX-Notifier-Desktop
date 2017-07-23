/**
 * CheckAnnouncementsTask.java
 *
 * Created by Jaiden Baker on Jul 9, 2017 3:47:44 PM
 * Copyright � 2017. All rights reserved.
 * 
 * Last modified on Jul 9, 2017 5:19:53 PM
 */

package jdz.NZXN.main;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import jdz.NZXN.Config.Config;
import jdz.NZXN.Config.ConfigWindow;
import jdz.NZXN.Notification.AnnouncementNotification;
import jdz.NZXN.Notification.Notification;
import jdz.NZXN.Notification.NotificationManager;
import jdz.NZXN.Notification.PriceNotification;
import jdz.NZXN.WebApi.NZXWebApi;
import jdz.NZXN.utils.Announcement;
import jdz.NZXN.utils.ComparePrice;

public class CheckAnnouncementsTask extends TimerTask{
	public static CheckAnnouncementsTask runningTask = null;
	private static Thread checkThread = null;
	private List<Runnable> runBeforeCheck = new ArrayList<Runnable>();
	private List<Runnable> runAfterCheck = new ArrayList<Runnable>();
	private List<Runnable> runEachSecond = new ArrayList<Runnable>();
	private int secondsSinceCheck = 0;
	private int intervalSeconds = 300;
	private LocalDateTime lastCheck = LocalDateTime.now();
	Timer timer;
	
	public CheckAnnouncementsTask(Timer timer){
		if (runningTask != null)
			throw new RuntimeException("Error: only 1 CheckAnnouncementsTask can exist at a time");
		
		runningTask = this;
		
		Config config = Config.loadConfig();
		intervalSeconds = config.getInterval()*60;
		lastCheck = config.getLastCheck();
		
		this.timer = timer;
		timer.schedule(this, 1000, 1000);
	}
	
	@Override
	public void run() {
		if (++secondsSinceCheck >= intervalSeconds)
			check();
		for (Runnable r: runEachSecond)
			r.run();
	}
	
	/**
	 * Runs the check on a separate thread and makes sure only 1 check can be done at a time
	 */
	public void check(){
		if (checkThread == null){
			checkThread = new Thread(){
				@Override
				public void run(){
					doCheck();
					checkThread = null;
				}
			};
			checkThread.run();
		}
	}
	
	private void doCheck(){
		for (Runnable r: runBeforeCheck)
			r.run();

		Config config = Config.loadConfig();
		secondsSinceCheck = 0;
		lastCheck = LocalDateTime.now();
		if (NZXWebApi.checkConnection())
		{
			lastCheck = NZXWebApi.getNZXTime();
			if (lastCheck == null)
				lastCheck = LocalDateTime.now();
			
			List<Notification> notifications = new ArrayList<Notification>();
			
			if (config.getAnnEnabled()){
				List<Announcement> a = NZXWebApi.getAnnouncements(config);
				if (!a.isEmpty()){
					if (config.getAnnSaveEnabled())
							NZXWebApi.downloadAttatchments(a);
					NZXWebApi.addToCSV(a);
					notifications.add(new AnnouncementNotification(a));
				}
			}
			
			if (config.getPriceEnabled()){
				List<String> prices = config.getPriceAlerts();
				List<String> toRemove = new ArrayList<String>();
				List<String> toAdd = new ArrayList<String>();
				for (String s: prices){
					List<String> args = Config.parseList(s, ":");
					if (args.get(0).length() != 3)
						continue;
					
					try {
						double value = NZXWebApi.getValue(args.get(0));
						double currentValue = Double.parseDouble(args.get(2));
						if (ComparePrice.checkPrice(currentValue, value, args.get(1))){
							toRemove.add(s);
							if (args.get(1).equals("Any change"))
								toAdd.add(args.get(0)+":"+args.get(1)+":"+value);
							notifications.add(new PriceNotification(args.get(0), value, currentValue));
						}
					} catch (IOException | NumberFormatException e) { }
					
				}
				prices.removeAll(toRemove);
				prices.addAll(toAdd);
				config.setPriceAlerts(prices);
			}
			NotificationManager.add(notifications);
			config.setLastCheck(lastCheck);
		}
		
		config.save();
		if (ConfigWindow.currentWindow != null)
			ConfigWindow.currentWindow.reloadConfig();
		for (Runnable r: runAfterCheck)
			r.run();
	}
	
	public void setIntervalMinutes(int minutes){ intervalSeconds = minutes*60; }
	public LocalDateTime getLastCheck(){ return lastCheck; }
	public void addTaskBeforeCheck(Runnable r){ runBeforeCheck.add(r); }
	public void addTaskAfterCheck(Runnable r){ runAfterCheck.add(r); }
	public void addTaskEachSecond(Runnable r){ runEachSecond.add(r); }

	public LocalDateTime getCurrentTime() {
		return lastCheck.plusSeconds(secondsSinceCheck);
	}

	public LocalDateTime getNextCheck() {
		return lastCheck.plusSeconds(intervalSeconds);
	}
}