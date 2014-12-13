package nu.info.zeeshan.getthetext.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.apache.http.conn.util.InetAddressUtils;

import android.util.Log;

public class Utility {
	public static String TAG="nu.info.zeeshan.getthetext.util";
	 public static String getIpAddress() {
	      try {
	         for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
	            NetworkInterface intf = en.nextElement();
	            for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
	               InetAddress inetAddress = enumIpAddr.nextElement();
	               if (!inetAddress.isLoopbackAddress() && InetAddressUtils.isIPv4Address(inetAddress.getHostAddress())) {
	            	   return inetAddress.getHostAddress();
	               }
	            }
	         }
	      } catch (SocketException e) {
	         log(TAG, e.getMessage());
	      }
	      return null;
	   }
	 public static void log(String TAG,String msg){
		 Log.d(TAG,msg);
	 }
}
