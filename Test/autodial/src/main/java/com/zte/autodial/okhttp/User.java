package com.zte.autodial.okhttp;

public class User {

	public String username ;
	public Integer password  ;
	
	public User() {
		// TODO Auto-generated constructor stub
	}
	
	public User(String username, Integer password) {
		this.username = username;
		this.password = password;
	}

	@Override
	public String toString()
	{
		return "User{" +
				"username='" + username + '\'' +
				", password='" + password + '\'' +
				'}';
	}
}
