package net.endiq.launcher;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static com.app.astralauncher.utils.ZHTools.getVersionCode;
import static com.app.astralauncher.utils.ZHTools.getVersionName;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import com.app.astralauncher.InfoDistributor;
import com.app.astralauncher.context.ContextExecutor;
import com.app.astralauncher.context.LocaleHelper;
import com.app.astralauncher.feature.log.Logging;
import com.app.astralauncher.setting.AllSettings;
import com.app.astralauncher.ui.activity.ErrorActivity;
import com.app.astralauncher.utils.path.PathManager;
import com.app.astralauncher.utils.ZHTools;
import net.endiq.launcher.utils.FileUtils;
import java.io.File;
import java.io.PrintStream;
import java.text.DateFormat;
import java.util.Date;


public class AstraApplication extends Application {
	public static final String CRASH_REPORT_TAG = "AstraCrashReport";

	@Override
	public void onCreate() {
		// Dark-only launcher. Set the night mode before any Activity, dialog or resource
		// lookup happens so every window starts from the dark theme and the night colour
		// resources - no light flash on a light-mode device, no partial theming.
		AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

		ContextExecutor.setApplication(this);

		Thread.setDefaultUncaughtExceptionHandler((thread, th) -> {
			File crashFile = new File(resolveCrashLogDir(), "latestlog.txt");
			try {
				FileUtils.ensureParentDirectory(crashFile);
				StringBuilder report = new StringBuilder();
				report.append(InfoDistributor.APP_NAME).append(" crash report\n");
				report.append(" - Time: ").append(DateFormat.getDateTimeInstance().format(new Date())).append("\n");
				report.append(" - Device: ").append(Build.PRODUCT).append(" ").append(Build.MODEL).append("\n");
				report.append(" - Android version: ").append(Build.VERSION.RELEASE).append("\n");
				report.append(" - Launcher version: ").append(getVersionName()).append(" (").append(String.valueOf(getVersionCode())).append(")").append("\n");
				report.append(" - Crash stack trace:\n");
				report.append(Log.getStackTraceString(th));

				String previous = null;
				if (crashFile.isFile()) {
					try {
						previous = new String(java.nio.file.Files.readAllBytes(crashFile.toPath()));
					} catch (Throwable ignored) { /* fall through with previous == null */ }
				}
				String combined = (previous == null || previous.trim().isEmpty())
					? report.toString()
					: report + "\n\n════════ earlier report(s) below ════════\n\n" + previous;
				final int maxChars = 256 * 1024;
				if (combined.length() > maxChars) combined = combined.substring(0, maxChars);

				PrintStream crashStream = new PrintStream(crashFile);
				crashStream.print(combined);
				crashStream.close();
			} catch (Throwable throwable) {
				Logging.e(CRASH_REPORT_TAG, " - Exception attempt saving crash stack trace:", throwable);
				Logging.e(CRASH_REPORT_TAG, " - The crash stack trace was:", th);
			}

			ErrorActivity.showLauncherCrash(AstraApplication.this, crashFile.getAbsolutePath(), th);
			ZHTools.killProcess();
		});

		try {
			super.onCreate();
			PathManager.DIR_DATA = getDir("files", MODE_PRIVATE).getParent();
			PathManager.DIR_CACHE = getCacheDir();
			PathManager.DIR_ACCOUNT_NEW = PathManager.DIR_DATA + "/accounts";
			Tools.DEVICE_ARCHITECTURE = Architecture.getDeviceArchitecture();
			//Force x86 lib directory for Asus x86 based zenfones
			if(Architecture.isx86Device() && Architecture.is32BitsDevice()){
				String originalJNIDirectory = getApplicationInfo().nativeLibraryDir;
				getApplicationInfo().nativeLibraryDir = originalJNIDirectory.substring(0,
												originalJNIDirectory.lastIndexOf("/"))
												.concat("/x86");
			}
		} catch (Throwable throwable) {
			Intent ferrorIntent = new Intent(this, ErrorActivity.class);
			ferrorIntent.putExtra("throwable", throwable);
			ferrorIntent.setFlags(FLAG_ACTIVITY_NEW_TASK);
			startActivity(ferrorIntent);
		}

		com.app.astralauncher.feature.shizuku.ShizukuManager.INSTANCE.init(this);
        com.app.astralauncher.feature.discord.DiscordPresenceLifecycle.register(this);

		androidx.startup.AppInitializer.getInstance(this)
			.initializeComponent(com.app.astralauncher.startup.AstraStartupInitializer.class);
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		ContextExecutor.clearApplication();
	}

	@Override
	public void onTrimMemory(int level) {
		super.onTrimMemory(level);
		try {
			com.bumptech.glide.Glide.get(this).trimMemory(level);
		} catch (Throwable t) {
			Logging.e(CRASH_REPORT_TAG, "Failed to trim Glide memory", t);
		}
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		try {
			com.bumptech.glide.Glide.get(this).clearMemory();
		} catch (Throwable t) {
			Logging.e(CRASH_REPORT_TAG, "Failed to clear Glide memory", t);
		}
	}

	@Override
    protected void attachBaseContext(Context base) {
		ContextExecutor.setApplication(this);
        super.attachBaseContext(LocaleHelper.Companion.setLocale(base));
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
		ContextExecutor.setApplication(this);
		LocaleHelper.Companion.setLocale(this);
    }

	private static File resolveCrashLogDir() {
		try {
			File logDir = new File(PathManager.DIR_LAUNCHER_LOG);
			if (logDir.isDirectory() || logDir.mkdirs()) return logDir;
		} catch (Throwable ignored) {
			// Uninitialized (crashed before the first Activity) or unusable - fall through.
		}
		return new File(PathManager.DIR_DATA);
	}
}
