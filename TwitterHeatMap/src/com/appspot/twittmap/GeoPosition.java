package com.appspot.twittmap;

public class GeoPosition{
	GeoPosition(double x,double y,String time){
		this.x=x;
		this.y=y;
		this.time=time;
	}
	double x;
	double y;
	String time;
	public String toString(){
		return "["+this.x+","+this.y+",\""+this.time+"\"]";
		
	}
}