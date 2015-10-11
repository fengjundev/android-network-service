package com.feng.android.network.cache;

public class CacheInfo <T>{
	private T cache;
	private boolean isDue;
	
	public CacheInfo(){
		isDue = false;
	}
	
	public T getCache() {
		return cache;
	}
	public void setCache(T cache) {
		this.cache = cache;
	}
	public boolean isDue() {
		return isDue;
	}
	public void setDue(boolean isDue) {
		this.isDue = isDue;
	}
	
	
}
