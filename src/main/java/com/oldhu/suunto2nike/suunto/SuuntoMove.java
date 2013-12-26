package com.oldhu.suunto2nike.suunto;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SuuntoMove
{
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private String startTime;							// Format 2013-12-17 21:25:52
	private int duration;								// in milli-seconds
	private int calories;								// KCal
	private int distance;								// in meters
	private ArrayList<String> distanceSamples;

	public ArrayList<String> getDistanceSamples()
	{
		return distanceSamples;
	}

	public ArrayList<String> getHeartRateSamples()
	{
		return heartRateSamples;
	}

	private ArrayList<String> heartRateSamples;

	public SuuntoMove()
	{
		distanceSamples = new ArrayList<String>();
		heartRateSamples = new ArrayList<String>();
	}

	public void addDistanceSample(String distance)
	{
		distanceSamples.add(distance);
	}

	public void addHeartRateSample(String heartRate)
	{
		heartRateSamples.add(heartRate);
	}

	public String getStartTime()
	{
		return startTime;
	}

	public void setStartTime(String startTime)
	{
		this.startTime = startTime;
	}
	
	public void setStartTime(Date startTime)
	{
		this.startTime = dateFormat.format(startTime);
	}

	public int getDuration()
	{
		return duration;
	}

	public void setDuration(int duration)
	{
		this.duration = duration;
	}

	public int getCalories()
	{
		return calories;
	}

	public void setCalories(int calories)
	{
		this.calories = calories;
	}

	public int getDistance()
	{
		return distance;
	}

	public void setDistance(int distance)
	{
		this.distance = distance;
	}

	@Override
	public boolean equals(Object obj)
	{
		SuuntoMove move = (SuuntoMove) obj;
		if ((move.getDistance() == this.getDistance()) && (move.getCalories() == this.getCalories())
				&& (move.getDuration() == this.getDuration()) && (move.getStartTime().equals(this.getStartTime()))) {
			return true;
		}
		return false;
	}
}
