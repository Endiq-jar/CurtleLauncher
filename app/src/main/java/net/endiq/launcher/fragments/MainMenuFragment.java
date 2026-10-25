package net.endiq.launcher.fragments;
import com.app.crutlelauncher.utils.anim.CrutleTransitions;

import static com.app.crutlelauncher.event.single.RefreshVersionsEvent.MODE.END;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.app.anim.AnimPlayer;
import com.app.anim.animations.Animations;
import com.app.crutlelauncher.InfoCenter;
import com.app.crutlelauncher.R;
import com.app.crutlelauncher.databinding.FragmentLauncherBinding;
import com.app.crutlelauncher.event.single.AccountUpdateEvent;
import com.app.crutlelauncher.event.single.LaunchGameEvent;
import com.app.crutlelauncher.event.single.RefreshVersionsEvent;
import com.app.crutlelauncher.event.value.InstallLocalModpackEvent;
import com.app.crutlelauncher.feature.mod.modpack.install.InstallExtra;
import com.app.crutlelauncher.feature.log.CrashAnalyzer;
import com.app.crutlelauncher.feature.crutle.DailyPlaytimeStats;
import com.app.crutlelauncher.feature.version.Version;
import com.app.crutlelauncher.feature.version.utils.VersionIconUtils;
import com.app.crutlelauncher.feature.version.VersionInfo;
import com.app.crutlelauncher.feature.version.VersionsManager;
import com.app.crutlelauncher.task.TaskExecutors;
import com.app.crutlelauncher.ui.fragment.AboutFragment;
import com.app.crutlelauncher.ui.fragment.ControlButtonFragment;
import com.app.crutlelauncher.ui.fragment.FilesFragment;
import com.app.crutlelauncher.ui.fragment.FragmentWithAnim;
import com.app.crutlelauncher.ui.fragment.LogViewerFragment;
import com.app.crutlelauncher.ui.fragment.AccountFragment;
import com.app.crutlelauncher.ui.fragment.DownloadFragment;
import com.app.crutlelauncher.ui.fragment.VersionsListFragment;
import com.app.crutlelauncher.ui.subassembly.version.VersionManagerDropdown;
import com.app.crutlelauncher.feature.accounts.AccountsManager;
import com.app.crutlelauncher.feature.log.Logging;
import com.app.crutlelauncher.utils.skin.SkinLoader;

import androidx.core.content.ContextCompat;

import net.endiq.launcher.value.MinecraftAccount;
import com.app.crutlelauncher.utils.file.FileTools;
import com.app.crutlelauncher.utils.path.PathManager;
import com.app.crutlelauncher.utils.ZHTools;
import com.app.crutlelauncher.utils.anim.ViewAnimUtils;

import net.endiq.launcher.Tools;
import net.endiq.launcher.contracts.OpenDocumentWithExtension;
import net.endiq.launcher.progresskeeper.ProgressKeeper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

public class MainMenuFragment extends FragmentWithAnim {
    public static final String TAG = "MainMenuFragment";
    private FragmentLauncherBinding binding;
    private ActivityResultLauncher<Object> modpackImportLauncher;
    private final net.endiq.launcher.progresskeeper.TaskCountListener tasksBadgeListener = this::updateTasksBadge;

    public MainMenuFragment() {
        super(R.layout.fragment_launcher);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        modpackImportLauncher = registerForActivityResult(new OpenDocumentWithExtension(null), uris -> {
            if (uris == null || uris.isEmpty()) return;
            importModpackFromUri(uris.get(0));
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLauncherBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.aboutText.setText(InfoCenter.replaceName(requireActivity(), R.string.about_tab));
        setupBottomNav();
        binding.aboutButton.setOnClickListener(v -> ZHTools.swapFragmentWithAnim(this, AboutFragment.class, AboutFragment.TAG, null));
        binding.customControlButton.setOnClickListener(v -> ZHTools.swapFragmentWithAnim(this, ControlButtonFragment.class, ControlButtonFragment.TAG, null));
        binding.installJarButton.setOnClickListener(v -> runInstallerWithConfirmation(false));
        binding.installJarButton.setOnLongClickListener(v -> {
            runInstallerWithConfirmation(true);
            return true;
        });
        binding.terracottaButton.setOnClickListener(v -> ZHTools.swapFragmentWithAnim(this,
            com.app.crutlelauncher.ui.fragment.TerracottaFragment.class,
            com.app.crutlelauncher.ui.fragment.TerracottaFragment.TAG, null));
        binding.shareLogsButton.setOnClickListener(v -> ZHTools.swapFragmentWithAnim(this, com.app.crutlelauncher.ui.fragment.ShareLogsFragment.class, com.app.crutlelauncher.ui.fragment.ShareLogsFragment.TAG, null));
        binding.modpackImportButton.setOnClickListener(v -> {
            if (ProgressKeeper.getTaskCount() == 0) {
                modpackImportLauncher.launch(null);
            } else {
                Toast.makeText(requireContext(), R.string.tasks_ongoing, Toast.LENGTH_LONG).show();
            }
        });
        // Footer: launcher name, version string, and GitHub link
        binding.footerAppName.setText(com.app.crutlelauncher.InfoDistributor.LAUNCHER_NAME);
        binding.footerVersionText.setText("v" + com.app.crutlelauncher.BuildConfig.VERSION_NAME);
        binding.footerGithubButton.setOnClickListener(v ->
            ZHTools.openLink(requireActivity(), com.app.crutlelauncher.utils.path.UrlManager.URL_HOME));

        binding.version.setOnClickListener(v -> {
            if (!isTaskRunning()) {
                ZHTools.swapFragmentWithAnim(this, VersionsListFragment.class, VersionsListFragment.TAG, null);
            } else {
                ViewAnimUtils.setViewAnim(binding.version, Animations.Shake);
                { android.content.Context toastContext = getContext(); if (toastContext != null) TaskExecutors.runInUIThread(() -> Toast.makeText(toastContext, R.string.version_manager_task_in_progress, Toast.LENGTH_SHORT).show()); }
            }
        });
        binding.managerProfileButton.setOnClickListener(v -> {
            if (!isTaskRunning()) {
                ViewAnimUtils.setViewAnim(binding.managerProfileButton, Animations.Pulse);
                new VersionManagerDropdown(this).show(binding.managerProfileButton);
            } else {
                ViewAnimUtils.setViewAnim(binding.managerProfileButton, Animations.Shake);
                { android.content.Context toastContext = getContext(); if (toastContext != null) TaskExecutors.runInUIThread(() -> Toast.makeText(toastContext, R.string.version_manager_task_in_progress, Toast.LENGTH_SHORT).show()); }
            }
        });

        binding.playButton.setOnClickListener(v -> EventBus.getDefault().post(new LaunchGameEvent()));

        // Community links
        binding.linkDiscordButton.setOnClickListener(v -> openUrl("https://discord.gg/8TfuMhM8tD"));
        binding.linkWebsiteButton.setOnClickListener(v -> openUrl("https://app-jar.github.io/app-shop/"));
        binding.linkYoutubeButton.setOnClickListener(v -> openUrl("https://youtube.com/@app-jar?si=9sb9OnKDJG2kUnO1"));

        binding.versionName.setSelected(true);
        binding.versionInfo.setSelected(true);

        // Top app bar: title/subtitle + quick-action icon row
        binding.homeTopBarTitle.setText(com.app.crutlelauncher.InfoDistributor.LAUNCHER_NAME);
        binding.topBarLogoButton.setOnClickListener(v -> ZHTools.swapFragmentWithAnim(this,
            com.app.crutlelauncher.ui.fragment.LogoPickerFragment.class,
            com.app.crutlelauncher.ui.fragment.LogoPickerFragment.TAG, null));
        binding.topBarAccountButton.setOnClickListener(v ->
            ZHTools.swapFragmentWithAnim(this, AccountFragment.class, AccountFragment.TAG, null));
        refreshAccountButton();
        binding.topBarStorageButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putString(FilesFragment.BUNDLE_LIST_PATH, PathManager.DIR_GAME_HOME);
            ZHTools.swapFragmentWithAnim(this, FilesFragment.class, FilesFragment.TAG, bundle);
        });
        binding.topBarDownloadButton.setOnClickListener(v -> ZHTools.swapFragmentWithAnim(this,
            com.app.crutlelauncher.ui.fragment.DownloadFragment.class,
            com.app.crutlelauncher.ui.fragment.DownloadFragment.TAG, null));
        binding.topBarCursorButton.setOnClickListener(v -> ZHTools.swapFragmentWithAnim(this,
            com.app.crutlelauncher.ui.fragment.CustomMouseFragment.class,
            com.app.crutlelauncher.ui.fragment.CustomMouseFragment.TAG, null));
        binding.topBarMusicButton.setOnClickListener(v -> ZHTools.swapFragmentWithAnim(this,
            com.app.crutlelauncher.ui.fragment.MusicFragment.class,
            com.app.crutlelauncher.ui.fragment.MusicFragment.TAG, null));
        binding.topBarSettingsButton.setOnClickListener(v -> ZHTools.swapFragmentWithAnim(this,
            com.app.crutlelauncher.ui.fragment.settings.SettingsFragment.class,
            com.app.crutlelauncher.ui.fragment.settings.SettingsFragment.TAG, null));
        binding.topBarAiButton.setVisibility(
            com.app.crutlelauncher.setting.AllSettings.getAiAssistantEnabled().getValue()
                ? View.VISIBLE : View.GONE);
        binding.topBarAiButton.setOnClickListener(v -> ZHTools.swapFragmentWithAnim(this,
            com.app.crutlelauncher.ui.fragment.AiChatFragment.class,
            com.app.crutlelauncher.ui.fragment.AiChatFragment.TAG, null));
        binding.topBarTasksButton.setOnClickListener(v -> showRunningTasksDialog());
        updateTasksBadge(ProgressKeeper.getTaskCount());

        // Today's Statistics dashboard
        refreshStatistics();
        refreshLastGameLog();

        populateFeaturePlugins();
        refreshCurrentVersion();
    }

    /** Cubiq-style bottom nav: News / Play / Mods / Profile. Additive only — doesn't
     *  remove or replace any existing navigation path, just offers another one.
     *  This screen (fragment_launcher) is itself the "Play" destination, so Play
     *  is always shown active here; the other three tabs swap to their screen. */
    private void setupBottomNav() {
        markOnlyPlayTabActive();

        binding.bottomNavBar.navMods.setOnClickListener(v ->
                ZHTools.swapFragmentWithAnim(this, DownloadFragment.class, DownloadFragment.TAG, null));

        binding.bottomNavBar.navProfile.setOnClickListener(v ->
                ZHTools.swapFragmentWithAnim(this, AccountFragment.class, AccountFragment.TAG, null));

        binding.bottomNavBar.navNews.setOnClickListener(v -> {
            android.content.Context toastContext = getContext();
            if (toastContext != null) {
                Toast.makeText(toastContext, R.string.main_nav_news_coming_soon, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** Resets all four bottom-nav tabs to inactive, then marks Play active
     *  (green label + tint + underline), matching the reference UI. */
    private void markOnlyPlayTabActive() {
        if (binding == null) return;
        int accent = ContextCompat.getColor(requireContext(), R.color.accent_primary);
        int inactive = ContextCompat.getColor(requireContext(), R.color.crutle_text_secondary);

        android.widget.ImageView[] tintableIcons = {binding.bottomNavBar.navNewsIcon,
                binding.bottomNavBar.navModsIcon, binding.bottomNavBar.navProfileIcon};
        TextView[] labels = {binding.bottomNavBar.navNewsLabel, binding.bottomNavBar.navModsLabel,
                binding.bottomNavBar.navProfileLabel};
        View[] indicators = {binding.bottomNavBar.navNewsIndicator, binding.bottomNavBar.navModsIndicator,
                binding.bottomNavBar.navProfileIndicator};

        for (android.widget.ImageView icon : tintableIcons) icon.setColorFilter(inactive);
        for (TextView label : labels) label.setTextColor(inactive);
        for (View indicator : indicators) indicator.setVisibility(View.INVISIBLE);

        binding.bottomNavBar.navPlayLabel.setTextColor(accent);
        binding.bottomNavBar.navPlayIndicator.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding == null) return;
        refreshLastGameLog();
        populateFeaturePlugins();
    }

    /** Populates the weekly playtime chart and today's total from {@link DailyPlaytimeStats}. */
    private void refreshStatistics() {
        long[] weekMs = DailyPlaytimeStats.getThisWeekMs();
        float[] weekHours = new float[weekMs.length];
        for (int i = 0; i < weekMs.length; i++) weekHours[i] = weekMs[i] / 3600000f;
        binding.statsChart.setData(weekHours);
    }

    private void refreshLastGameLog() {
        String lastLogText = CrashAnalyzer.INSTANCE.getLastLogText();
        File logFile = com.app.crutlelauncher.feature.log.LatestLogResolver.resolveLastGameLogFile();

        if ((lastLogText == null || lastLogText.isEmpty()) && logFile == null) {
            binding.lastLogPreview.setText(R.string.main_last_log_none);
            binding.lastLogCard.setOnClickListener(null);
            return;
        }

        String previewSource = (lastLogText != null && !lastLogText.isEmpty())
                ? lastLogText
                : CrashAnalyzer.tailOf(logFile, 8 * 1024);
        String preview = lastNonEmptyLine(previewSource);
        if (preview.isEmpty()) {
            binding.lastLogPreview.setText(R.string.main_last_log_none);
        } else {
            binding.lastLogPreview.setText(preview);
        }

        final File targetLogFile = logFile;
        binding.lastLogCard.setOnClickListener(v -> {
            File toOpen = targetLogFile != null
                    ? targetLogFile
                    : com.app.crutlelauncher.feature.log.LatestLogResolver.resolveLatestLogFile();
            if (toOpen != null && toOpen.isFile()) {
                ZHTools.swapFragmentWithAnim(this, LogViewerFragment.class, LogViewerFragment.TAG,
                    LogViewerFragment.Companion.createArgs(toOpen));
            }
        });
    }

    private static String lastNonEmptyLine(String text) {
        if (text == null) return "";
        String[] lines = text.split("\n");
        for (int i = lines.length - 1; i >= 0; i--) {
            String trimmed = lines[i].trim();
            if (trimmed.isEmpty()) continue;
            if (trimmed.startsWith("Java Exit code")) continue;
            return trimmed.length() > 200 ? trimmed.substring(trimmed.length() - 200) : trimmed;
        }
        return "";
    }

    private void populateFeaturePlugins() {
        java.util.List<com.app.crutlelauncher.plugins.feature.FeaturePlugin> plugins =
                com.app.crutlelauncher.plugins.feature.FeaturePluginManager.getFeaturePluginList();
        binding.featurePluginsContainer.removeAllViews();
        if (plugins.isEmpty()) {
            binding.featurePluginsContainer.setVisibility(View.GONE);
            return;
        }
        binding.featurePluginsContainer.setVisibility(View.VISIBLE);
        android.content.pm.PackageManager packageManager = requireContext().getPackageManager();
        for (com.app.crutlelauncher.plugins.feature.FeaturePlugin plugin : plugins) {
            View row = getLayoutInflater().inflate(R.layout.item_feature_plugin, binding.featurePluginsContainer, false);
            android.widget.ImageView icon = row.findViewById(R.id.feature_plugin_icon);
            android.widget.TextView title = row.findViewById(R.id.feature_plugin_title);
            android.widget.TextView desc = row.findViewById(R.id.feature_plugin_desc);

            title.setText(plugin.getDisplayName());
            if (plugin.getDescription().isEmpty()) {
                desc.setVisibility(View.GONE);
            } else {
                desc.setText(plugin.getDescription());
            }
            try {
                icon.setImageDrawable(packageManager.getApplicationIcon(plugin.getApplicationInfo()));
            } catch (Exception ignored) {
                icon.setImageResource(R.drawable.ic_puzzle_piece);
            }

            row.setOnClickListener(v -> {
                Intent launchIntent = packageManager.getLaunchIntentForPackage(plugin.getPackageName());
                if (launchIntent != null) {
                    try {
                        startActivity(launchIntent);
                    } catch (Exception e) {
                        Toast.makeText(requireContext(), plugin.getPackageName(), Toast.LENGTH_LONG).show();
                    }
                }
            });

            binding.featurePluginsContainer.addView(row);
        }
    }

    private void refreshCurrentVersion() {
        Version version = VersionsManager.INSTANCE.getCurrentVersion();

        int versionInfoVisibility;
        if (version != null) {
            binding.versionName.setText(version.getVersionName());
            VersionInfo versionInfo = version.getVersionInfo();
            if (versionInfo != null) {
                binding.versionInfo.setText(versionInfo.getInfoString());
                versionInfoVisibility = View.VISIBLE;
            } else versionInfoVisibility = View.GONE;

            new VersionIconUtils(version).start(binding.versionIcon);
            binding.managerProfileButton.setVisibility(View.VISIBLE);
        } else {
            binding.versionName.setText(R.string.version_no_versions);
            binding.managerProfileButton.setVisibility(View.GONE);
            versionInfoVisibility = View.GONE;
        }
        binding.versionInfo.setVisibility(versionInfoVisibility);
    }

    @Subscribe()
    public void event(RefreshVersionsEvent event) {
        if (event.getMode() == END) {
            TaskExecutors.runInUIThread(this::refreshCurrentVersion);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void event(AccountUpdateEvent event) {
        refreshAccountButton();
    }

    /**
     * Loads the current account's avatar into the top bar's account button, same source
     * (SkinLoader) and fallback (ic_add when no account is set yet) as the old view_account
     * card's AccountViewWrapper used, just without the name/type text an icon-only top-bar
     * button has no room for.
     */
    private void refreshAccountButton() {
        if (binding == null) return;
        MinecraftAccount account = AccountsManager.INSTANCE.getCurrentAccount();
        if (account == null) {
            binding.topBarAccountButton.setImageTintList(
                ContextCompat.getColorStateList(requireContext(), R.color.crutle_text_secondary));
            binding.topBarAccountButton.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_add));
            return;
        }
        try {
            binding.topBarAccountButton.setImageTintList(null);
            binding.topBarAccountButton.setImageDrawable(
                SkinLoader.getAvatarDrawable(requireContext(), account, (int) Tools.dpToPx(22f))
            );
        } catch (Exception e) {
            Logging.e("MainMenuFragment", "Failed to load avatar.", e);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        ProgressKeeper.addTaskCountListener(tasksBadgeListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        ProgressKeeper.removeTaskCountListener(tasksBadgeListener);
    }

    private void updateTasksBadge(int taskCount) {
        TaskExecutors.runInUIThread(() -> {
            if (binding == null) return;
            android.widget.TextView badge = binding.topBarTasksBadge;
            if (taskCount > 0) {
                badge.setText(String.valueOf(taskCount));
                badge.setVisibility(View.VISIBLE);
            } else {
                badge.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Shows every task ProgressKeeper currently knows about (downloads, login, mod
     * checking, JRE/runtime unpacking, etc - anything routed through ProgressKeeper.submitProgress,
     * regardless of which fixed key it uses) in a simple on-demand dialog, replacing the
     * old always-visible bottom ProgressLayout bar.
     */
    private void showRunningTasksDialog() {
        com.app.crutlelauncher.databinding.DialogRunningTasksBinding dialogBinding =
                com.app.crutlelauncher.databinding.DialogRunningTasksBinding.inflate(getLayoutInflater());

        java.util.Map<String, com.app.mcgui.TextProgressBar> rowsByKey = new java.util.HashMap<>();
        android.os.Handler refreshHandler = TaskExecutors.getUIHandler();
        Runnable[] refreshRunnableHolder = new Runnable[1];

        refreshRunnableHolder[0] = () -> {
            if (!isAdded()) return; // fragment detached - stop polling, dialog will be dismissed with it

            java.util.List<ProgressKeeper.Snapshot> snapshots = ProgressKeeper.getSnapshots();
            dialogBinding.tasksDialogEmpty.setVisibility(snapshots.isEmpty() ? View.VISIBLE : View.GONE);

            java.util.Set<String> stillRunning = new java.util.HashSet<>();
            for (ProgressKeeper.Snapshot snapshot : snapshots) {
                stillRunning.add(snapshot.progressKey);
                com.app.mcgui.TextProgressBar row = rowsByKey.get(snapshot.progressKey);
                if (row == null) {
                    row = new com.app.mcgui.TextProgressBar(requireContext());
                    row.setTextPadding(getResources().getDimensionPixelOffset(R.dimen._6sdp));
                    android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                            android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                            getResources().getDimensionPixelOffset(R.dimen._24sdp));
                    params.bottomMargin = getResources().getDimensionPixelOffset(R.dimen._6sdp);
                    dialogBinding.tasksDialogList.addView(row, params);
                    rowsByKey.put(snapshot.progressKey, row);
                }
                row.setProgress(Math.max(snapshot.progress, 0));
                row.setText(describeSnapshot(snapshot));
            }

            java.util.Iterator<java.util.Map.Entry<String, com.app.mcgui.TextProgressBar>> it = rowsByKey.entrySet().iterator();
            while (it.hasNext()) {
                java.util.Map.Entry<String, com.app.mcgui.TextProgressBar> entry = it.next();
                if (!stillRunning.contains(entry.getKey())) {
                    dialogBinding.tasksDialogList.removeView(entry.getValue());
                    it.remove();
                }
            }

            refreshHandler.postDelayed(refreshRunnableHolder[0], 400);
        };
        refreshRunnableHolder[0].run();

        new AlertDialog.Builder(requireContext(), R.style.CustomAlertDialogTheme)
                .setView(dialogBinding.getRoot())
                .setPositiveButton(android.R.string.ok, null)
                .setOnDismissListener(d -> refreshHandler.removeCallbacks(refreshRunnableHolder[0]))
                .show();
    }

    private String describeSnapshot(ProgressKeeper.Snapshot snapshot) {
        try {
            if (snapshot.resid != -1) return getString(snapshot.resid, snapshot.varArg);
            if (snapshot.varArg.length > 0 && snapshot.varArg[0] != null) return (String) snapshot.varArg[0];
        } catch (Throwable ignored) {
        }
        return "";
    }

    private void openUrl(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            Toast.makeText(requireContext(), url, Toast.LENGTH_LONG).show();
        }
    }

    private void runInstallerWithConfirmation(boolean isCustomArgs) {
        if (ProgressKeeper.getTaskCount() == 0)
            Tools.installMod(requireActivity(), isCustomArgs);
        else
            Toast.makeText(requireContext(), R.string.tasks_ongoing, Toast.LENGTH_LONG).show();
    }

    private void importModpackFromUri(Uri uri) {
        // Snapshot the context up front: requireContext() from the worker thread -
        // or from the error toast posted after it - crashes if the user navigated
        // away mid-copy ("not attached to a context").
        android.content.Context appContext = requireContext().getApplicationContext();
        TaskExecutors.getDefault().execute(() -> {
            try {
                java.io.File copiedFile = FileTools.copyFileInBackground(appContext, uri, PathManager.DIR_CACHE_STRING);
                EventBus.getDefault().post(new InstallLocalModpackEvent(new InstallExtra(true, copiedFile.getAbsolutePath())));
            } catch (Exception e) {
                TaskExecutors.runInUIThread(() ->
                    Toast.makeText(appContext, R.string.modpack_install_download_failed, Toast.LENGTH_LONG).show());
            }
        });
    }

    @Override
    public void slideIn(AnimPlayer animPlayer) {
        // The two big content panels take the configured screen transition, one beat
        // apart. Chrome (app bar, rail, footer) fades instead of sliding the full
        // screen height, and still arrives in sequence so the screen assembles itself.
        com.app.crutlelauncher.utils.anim.CrutleTransitions.stagger(
                animPlayer,
                java.util.Arrays.asList(binding.launcherMenu, binding.playLayout),
                55L);
        addDelayed(animPlayer, binding.homeTopBar, Animations.FadeIn, 0L);
        addDelayed(animPlayer, binding.quickActionsRail, Animations.FadeIn, 55L);
        addDelayed(animPlayer, binding.launcherFooter, Animations.FadeIn, 165L);
        animPlayer.apply(new AnimPlayer.Entry(binding.playButtonsLayout, Animations.BounceEnlarge));
    }

    @Override
    public void slideOut(AnimPlayer animPlayer) {
        animPlayer.apply(new AnimPlayer.Entry(binding.launcherMenu, CrutleTransitions.exit()))
                .apply(new AnimPlayer.Entry(binding.playLayout, CrutleTransitions.exit()))
                .apply(new AnimPlayer.Entry(binding.playButtonsLayout, Animations.BounceShrink));
        addDelayed(animPlayer, binding.homeTopBar, Animations.FadeOut, 0L);
        addDelayed(animPlayer, binding.quickActionsRail, Animations.FadeOut, 20L);
        addDelayed(animPlayer, binding.launcherFooter, Animations.FadeOut, 40L);
    }

    /** Adds [animations] for [view], delayed so several entries form one sequence. */
    private static void addDelayed(AnimPlayer player, android.view.View view,
                                   Animations animations, long delayMs) {
        android.animation.Animator[] animators =
                animations.getAnimator().getAnimators(view);
        for (android.animation.Animator animator : animators) {
            animator.setStartDelay(delayMs);
        }
        player.addAll(animators);
    }
}
