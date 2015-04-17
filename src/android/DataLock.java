package com.triarc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.cordova.CallbackContext;

import android.content.Context;
import android.util.Log;

public class DataLock {
	private static final String TAG = "DataLock";
	private String lockName;
	private Context context;
	private String[] processSources = new String[] { "Plugin", "SyncDataStore" };
	private String ownName;

	public DataLock(String lockName, Context context, String sourceName) {
		this.lockName = lockName;
		this.context = context;
		this.ownName = sourceName;
	}

	//private ArrayList<FileInputStream> _readLockStreams = new ArrayList<FileInputStream>();
	private FileInputStream _writeLockStream;

	public void aquire(LockType lockType) throws IOException {
		if (lockType == LockType.read) {
			this.aquireRead(this.ownName);
		} else if (lockType == LockType.write) {
			this.aquireWrite();
		}
	}

	private void aquireWrite() throws IOException {
		if (_writeLockStream != null) {
			Log.d(TAG,
					"write lock already set for that process, skip. SourceName:"
							+ ownName + " lockName: " + lockName);
			return;
		}
		
		for (String sourceName : processSources) {
			this.aquireRead(sourceName);
		}
		File lockFile = getWriteLockFile();
		_writeLockStream = new FileInputStream(lockFile);
		_writeLockStream.getChannel().lock();
	}

	public static final String SYNC_LOCK = "synclock_";

	private File getWriteLockFile() throws IOException {
		String fileName = SYNC_LOCK + lockName + "_write";
		return this.getLockFile(fileName);
	}

	private File getLockFile(String fileName) throws IOException {
		File filePath = context.getFileStreamPath(fileName);
		if (!filePath.exists()) {
			filePath.createNewFile();
		}
		return filePath;
	}

	private File getReadLockFile(String source) throws IOException {
		String fileName = SYNC_LOCK + lockName + "_" + source;
		return this.getLockFile(fileName);
	}

	public void release(LockType lockType) {
		if (lockType == LockType.read) {
			this.releaseRead(this.ownName);
		} else if (lockType == LockType.write) {
			this.releaseWrite();
		}
	}

	private void releaseWrite() {
		for (String sourceName : processSources) {
			this.releaseRead(sourceName);
		}
		if (_writeLockStream != null){
			try {
				_writeLockStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e(TAG, "release write lock failed. from process => " + this.ownName);
			}
		} else {
			Log.w(TAG, "write lock has already been release. from process => " + this.ownName);
		}
	}

	private void releaseRead(String sourceName) {
		if (_readFileStreams.containsKey(sourceName)) {
			try {
				FileInputStream lockStream = _readFileStreams.get(sourceName);
				// this releases the lock
				lockStream.close();
				_readFileStreams.remove(sourceName);
				Log.i(TAG, "sync lock has been released. SourceName:"
						+ sourceName + " lockName:" + lockName);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			Log.w(TAG, "read lock already release for " + sourceName + ". process name=>" + this.ownName);
		}
	}

	private void aquireRead(String sourceName) throws IOException {
		// require lock for own process
		File lockFile = getReadLockFile(sourceName);
		lockFile(lockFile, sourceName);
	}

	private HashMap<String, FileInputStream> _readFileStreams = new HashMap<String, FileInputStream>();

	private void lockFile(File file, String sourceName) throws IOException {
		FileInputStream lockFileStream = new FileInputStream(file);
		FileChannel channel = lockFileStream.getChannel();
		channel.lock();
		_readFileStreams.put(sourceName, lockFileStream);
	}

	// private FileInputStream createLock(String postFix) {
	// File file = getLockFile(lockName, context);
	// FileInputStream lockFileStream = new FileInputStream(file);
	// FileChannel channel = lockFileStream.getChannel();
	// channel.lock();
	// _lockFileStreams.put(lockName, lockFileStream);
	// Log.i(TAG, "Sync Lock has been aquired: SourceName:" + sourceName);
	// }
}
