package solstudios.app.spzmanagement;

import android.util.Log;

public class LogTask {
	public static final String TAG = "SPZ Management";

	public static final String LOG_D = "Log.d";
	public static final String LOG_I = "Log.i";
	public static final String LOG_E = "Log.e";
	public static final String LOG_V = "Log.v";

	private boolean isEnableLog = true;

	/**
	 * 
	 * @param logString
	 *            Log String data
	 * @param logTAB
	 *            Log Screen Name
	 * @param LogType
	 *            LogType: LOG_D,LOG_I,LOG_E,LOG_V
	 */
	public LogTask(String logString, String logTAB, String LogType) {
		if (isEnableLog)
			switch (LogType) {
				case LOG_D :
					Log.d(TAG, logTAB + ": " + logString);
					break;
				case LOG_I :
					Log.i(TAG, logTAB + ": " + logString);
					break;
				case LOG_E :
					Log.e(TAG, logTAB + ": " + logString);
					break;
				case LOG_V :
					Log.v(TAG, logTAB + ": " + logString);
					break;
			}
	}

}
