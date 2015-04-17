package com.triarc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.HashMap;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

import android.content.Context;
import android.util.Log;

public class InterprocessLock extends CordovaPlugin {
	private static final String TAG = "InterprocessLock";

	@Override
	public boolean execute(String action, JSONArray args,
			CallbackContext callbackContext) throws JSONException {
		if ("lock".equals(action)) {
			try {
				lock(this.webView.getContext(), args.getString(0), "Plugin", LockType.write);
				callbackContext.success();
			} catch (Exception e) {
				callbackContext.error(e.getMessage());
			}
			return true;
		}
		if ("release".equals(action)) {
			try {
				release(this.webView.getContext(), args.getString(0), "Plugin", LockType.write);
				callbackContext.success();
			} catch (Exception e) {
				callbackContext.error(e.getMessage());
			}
			return true;
		}
		return false; 
	}	

	public static void release(Context context, String lockName, String sourceName, LockType lockType) {
		synchronized (lockObject) {
			DataLock orAdd = getOrAdd(lockName, context);
			orAdd.release(sourceName, lockType);
		}
	}

	private static Object lockObject = new Object();

	private static HashMap<String, DataLock> _dataLocks = new HashMap<String, DataLock>();

	private static DataLock getOrAdd(String lockName, Context context, String sourceName) {
		DataLock result = null;
		if (!_dataLocks.containsKey(lockName)) {
			result = new DataLock(lockName, context, sourceName);
		} else {
			result = _dataLocks.get(lockName);
		}
		return result;
	}

	public static void lock(Context context, String lockName,
			String sourceName, LockType lockType) throws IOException {
		synchronized (lockObject) {
			DataLock lockObj = getOrAdd(lockName, context);
			lockObj.aquire(lockType, sourceName);

		}
	}

//	public static boolean isLocked(String lockName, Context context)
//			throws IOException {
//		synchronized (lockObject) {
//			DataLock orAdd = getOrAdd(lockName, context);
//			
//			// if locked by this process
//			if (_lockFileStreams.containsKey(lockName))
//				return true;
//
//			File lockFile = getLockFile(lockName);
//			FileInputStream fileInputStream = new FileInputStream(lockFile);
//			try {
//				FileLock tryLock = fileInputStream.getChannel().tryLock();
//				if (tryLock == null)
//					return true;
//				tryLock.release();
//			} finally {
//				fileInputStream.close();
//			}
//			return false;
//		}
//	}

}
