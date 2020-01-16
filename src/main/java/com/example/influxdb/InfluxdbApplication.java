package com.example.influxdb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory; 
import org.influxdb.dto.Point; 
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;

@SpringBootApplication
public class InfluxdbApplication {
	static Thread _sendTask = new Thread(){
		public void run(){ 
			int i = 0;
			String dbName = "db0"; 
			InfluxDB influxDB =InfluxDBFactory.connect("http://172.18.0.2:8086","influx","influx"); 
			influxDB.setDatabase(dbName); 
			Random rand = new Random(); 
			while ( _stopTask == false ){
				influxDB.write(Point.measurement("cpu")
						.time(System.currentTimeMillis(),TimeUnit.MILLISECONDS)
						.addField("host", "serverA")
						.addField("value", rand.nextInt(100)/100.0)
						.build());
			try {
				Thread.sleep(5000); }catch(InterruptedException e){
					e.printStackTrace();
				}
			} 
			influxDB.close();
		}
	};
	
	static volatile boolean _stopTask = false;
	
	static ApplicationListener listener = new ApplicationListener<ContextClosedEvent>(){
		@Override  
		public void onApplicationEvent(ContextClosedEvent event){
				_stopTask = true; 
				try { 
					_sendTask.join();
				}catch (InterruptedException e) { 
					e.printStackTrace(); 
				}
		}
	 };

	public static void main(String[] args) {   
		_sendTask.start(); 
		ConfigurableApplicationContext cac = new SpringApplication(InfluxdbApplication.class)
			.run(args);
		cac.addApplicationListener(listener);
		}
}
