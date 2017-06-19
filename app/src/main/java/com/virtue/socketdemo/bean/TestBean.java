package com.virtue.socketdemo.bean;

import java.io.Serializable;

public class TestBean implements Serializable{
	

	public String protver;
	public int pkgtype;
	public int command;
	public int seq;
	public int terminaltype = 0;
	public String terminal = "";
	public int result = 0;
	public int bodylen = 200;
	public Inform body=new Inform() ;
	
	public class Inform implements Serializable {
		public String Phone ;
		public int cmd;
		public String getPhone() {
			return Phone;
		}
		public void setPhone(String phone) {
			Phone = phone;
		}
		public int getCmd() {
			return cmd;
		}
		public void setCmd(int cmd) {
			this.cmd = cmd;
		}
	
	}
	
}
