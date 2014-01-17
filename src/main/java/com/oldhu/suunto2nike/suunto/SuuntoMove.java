package com.oldhu.suunto2nike.suunto;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SuuntoMove
{
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private String startTime; // Format 2013-12-17 21:25:52
	private int duration; // in milli-seconds
	private int calories; // KCal
	private int distance; // in meters
	private ArrayList<Integer> distanceSamples; // in meters
	private ArrayList<Integer> heartRateSamples;
	private ArrayList<TrackPoint> trackPoints;

	public ArrayList<Integer> getDistanceSamples()
	{
		return distanceSamples;
	}

	public ArrayList<Integer> getHeartRateSamples()
	{
		return heartRateSamples;
	}

	public SuuntoMove()
	{
		distanceSamples = new ArrayList<Integer>();
		heartRateSamples = new ArrayList<Integer>();
		trackPoints = new ArrayList<TrackPoint>();
	}

	public void addDistanceSample(Integer distance)
	{
		distanceSamples.add(distance);
	}

	public void addHeartRateSample(Integer heartRate)
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

	public void addTrackPoint(double lat, double lon, int ele, String time)
	{
		trackPoints.add(new TrackPoint(lat, lon, ele, time));
	}

	public Iterable<TrackPoint> getTrackPoints()
	{
		return trackPoints;
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

	public class TrackPoint
	{
		private double latitude;
		private double longitude;
		private int elevation;
		private String time;

		public String getLatitude()
		{
			return Double.toString(latitude);
		}

		public String getLongitude()
		{
			return Double.toString(longitude);
		}

		public int getElevation()
		{
			return elevation;
		}

		public String getTime()
		{
			return time;
		}

		public TrackPoint(double lat, double lon, int ele, String time)
		{
			this.latitude = lat;
			this.longitude = lon;
			this.elevation = ele;
			this.time = time;
		}

	}
}
