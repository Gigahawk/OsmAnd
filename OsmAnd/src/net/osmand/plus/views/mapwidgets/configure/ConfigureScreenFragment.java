package net.osmand.plus.views.mapwidgets.configure;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import net.osmand.plus.OsmandApplication;
import net.osmand.plus.R;
import net.osmand.plus.base.BaseOsmAndFragment;
import net.osmand.plus.quickaction.QuickActionListFragment;
import net.osmand.plus.quickaction.QuickActionRegistry;
import net.osmand.plus.quickaction.QuickActionRegistry.QuickActionUpdatesListener;
import net.osmand.plus.settings.backend.ApplicationMode;
import net.osmand.plus.settings.backend.OsmandSettings;
import net.osmand.plus.utils.AndroidUtils;
import net.osmand.plus.utils.ColorUtilities;
import net.osmand.plus.utils.UiUtilities;
import net.osmand.plus.utils.UiUtilities.CompoundButtonType;
import net.osmand.plus.views.mapwidgets.WidgetsPanel;
import net.osmand.plus.views.mapwidgets.WidgetsRegister;
import net.osmand.plus.views.mapwidgets.configure.panel.ConfigureWidgetsFragment;
import net.osmand.plus.widgets.chips.ChipItem;
import net.osmand.plus.widgets.chips.HorizontalChipsView;
import net.osmand.util.Algorithms;

import java.util.ArrayList;
import java.util.List;

public class ConfigureScreenFragment extends BaseOsmAndFragment implements QuickActionUpdatesListener {

	public static final String TAG = ConfigureScreenFragment.class.getSimpleName();

	private static final String INFO_LINK = "https://docs.osmand.net/en/main@latest/osmand/widgets/configure-screen";

	private OsmandApplication app;
	private OsmandSettings settings;
	private ApplicationMode selectedAppMode;
	private UiUtilities iconsCache;
	private LayoutInflater inflater;

	private Toolbar toolbar;
	private HorizontalChipsView modesToggle;
	private LinearLayout widgetsCard;
	private LinearLayout buttonsCard;

	private boolean nightMode;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		app = requireMyApplication();
		settings = app.getSettings();
		selectedAppMode = settings.getApplicationMode();
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		iconsCache = app.getUIUtilities();
		nightMode = !settings.isLightContent();
		this.inflater = inflater = UiUtilities.getInflater(getContext(), nightMode);

		View view = inflater.inflate(R.layout.fragment_configure_screen, container, false);
		AndroidUtils.addStatusBarPadding21v(requireContext(), view);

		toolbar = view.findViewById(R.id.toolbar);
		modesToggle = view.findViewById(R.id.modes_toggle);
		widgetsCard = view.findViewById(R.id.widgets_card);
		buttonsCard = view.findViewById(R.id.buttons_card);

		setupToolbar();
		setupModesToggle();
		fullUpdate();

		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
		app.getQuickActionRegistry().addUpdatesListener(this);
	}

	@Override
	public void onStop() {
		super.onStop();
		app.getQuickActionRegistry().removeUpdatesListener(this);
	}

	private void setupToolbar() {
		View backBtn = toolbar.findViewById(R.id.back_button);
		backBtn.setOnClickListener(view -> {
			FragmentActivity activity = getActivity();
			if (activity != null) {
				activity.onBackPressed();
			}
		});
		View infoBtn = toolbar.findViewById(R.id.info_button);
		infoBtn.setOnClickListener(v -> {
			FragmentActivity activity = getActivity();
			if (activity != null) {
				AndroidUtils.openUrl(activity, Uri.parse(INFO_LINK), nightMode);
			}
		});
	}

	private void setupModesToggle() {
		List<ChipItem> items = new ArrayList<>();
		ChipItem selectedItem = null;
		for (ApplicationMode am : ApplicationMode.values(app)) {
			ChipItem item = new ChipItem(am.getStringKey());
			int colorId = am.getIconColorInfo().getColor(nightMode);
			int profileColor = ContextCompat.getColor(app, colorId);
			int bgSelectedColor = ColorUtilities.getColorWithAlpha(profileColor, 0.25f);
			item.icon = iconsCache.getIcon(am.getIconRes());
			item.iconColor = profileColor;
			item.iconSelectedColor = profileColor;
			item.strokeSelectedColor = profileColor;
			item.rippleColor = profileColor;
			item.bgSelectedColor = bgSelectedColor;
			item.tag = am;
			if (Algorithms.objectEquals(selectedAppMode, am)) {
				selectedItem = item;
			}
			items.add(item);
		}
		modesToggle.setItems(items);
		modesToggle.setSelected(selectedItem);
		modesToggle.setOnSelectChipListener(chip -> {
			if (chip.tag instanceof ApplicationMode) {
				ApplicationMode am = (ApplicationMode) chip.tag;
				settings.setApplicationMode(am);
				selectedAppMode = am;
				modesToggle.smoothScrollTo(chip);
			}
			return true;
		});
		if (selectedItem != null) {
			modesToggle.scrollTo(selectedItem);
		}
		settings.APPLICATION_MODE.addListener(change -> {
			if (getContext() != null) {
				fullUpdate();
			}
		});
	}

	private void fullUpdate() {
		updateWidgetsCard();
		updateButtonsCard();
	}

	private void updateWidgetsCard() {
		widgetsCard.removeAllViews();
		widgetsCard.addView(createWidgetGroupView(WidgetsPanel.LEFT, false, false));
		widgetsCard.addView(createWidgetGroupView(WidgetsPanel.RIGHT, true, false));
		widgetsCard.addView(createWidgetGroupView(WidgetsPanel.TOP, false, false));
		widgetsCard.addView(createWidgetGroupView(WidgetsPanel.BOTTOM, false, true));
		widgetsCard.addView(createButtonWithSwitch(
				R.drawable.ic_action_appearance,
				getString(R.string.map_widget_transparent),
				false,
				false,
				false,
				null
		));
	}

	private void updateButtonsCard() {
		buttonsCard.removeAllViews();
		buttonsCard.addView(createButtonWithSwitch(
				R.drawable.ic_action_compass,
				getString(R.string.map_widget_compass),
				false,
				false,
				false,
				null
		));
		buttonsCard.addView(createButtonWithSwitch(
				R.drawable.ic_action_ruler_line,
				getString(R.string.map_widget_distance_by_tap),
				false,
				true,
				false,
				null
		));

		QuickActionRegistry qaRegistry = app.getQuickActionRegistry();
		int actionsCount = qaRegistry.getQuickActions().size();
		String actions = getString(R.string.shared_string_actions);
		String desc = getString(R.string.ltr_or_rtl_combine_via_colon, actions, String.valueOf(actionsCount));
		buttonsCard.addView(createButtonWithDesc(
				R.drawable.ic_quick_action,
				getString(R.string.configure_screen_quick_action),
				desc,
				qaRegistry.isQuickActionOn(),
				v -> QuickActionListFragment.showInstance(requireActivity(), false)
		));
	}

	private void showConfigureWidgetsScreen(WidgetsPanel panel) {
		FragmentActivity activity = getActivity();
		if (activity != null) {
			ConfigureWidgetsFragment.showInstance(activity, panel, selectedAppMode);
		}
	}

	@Override
	public void onActionsUpdated() {
		updateButtonsCard();
	}

	private View createWidgetGroupView(WidgetsPanel panel,
	                                   boolean showShortDivider,
	                                   boolean showLongDivider) {
		int activeColor = ColorUtilities.getActiveColor(app, nightMode);
		int defColor = ColorUtilities.getDefaultIconColor(app, nightMode);

		View view = inflater.inflate(R.layout.configure_screen_list_item, null);
		ImageView ivIcon = view.findViewById(R.id.icon);
		TextView tvTitle = view.findViewById(R.id.title);
		TextView tvDesc = view.findViewById(R.id.items_count_descr);

		int count = WidgetsRegister.getSortedWidgets(selectedAppMode, panel).size();
		int iconColor = count > 0 ? activeColor : defColor;
		Drawable icon = getPaintedContentIcon(panel.getIconId(), iconColor);
		ivIcon.setImageDrawable(icon);

		String title = getString(panel.getTitleId());
		tvTitle.setText(title);

		tvDesc.setVisibility(View.VISIBLE);
		tvDesc.setText(String.valueOf(count));

		if (showShortDivider) {
			view.findViewById(R.id.short_divider).setVisibility(View.VISIBLE);
		}

		if (showLongDivider) {
			view.findViewById(R.id.long_divider).setVisibility(View.VISIBLE);
		}

		setupClickListener(view, v -> showConfigureWidgetsScreen(panel));
		setupListItemBackground(view);
		return view;
	}

	private View createButtonWithSwitch(int iconId,
	                                    @NonNull String title,
	                                    boolean enabled,
	                                    boolean showShortDivider,
	                                    boolean showLongDivider,
	                                    @Nullable OnClickListener listener) {
		int activeColor = ColorUtilities.getActiveColor(app, nightMode);
		int defColor = ColorUtilities.getDefaultIconColor(app, nightMode);
		int iconColor = enabled ? activeColor : defColor;
		View view = inflater.inflate(R.layout.configure_screen_list_item, null);

		Drawable icon = getPaintedContentIcon(iconId, iconColor);
		ImageView ivIcon = view.findViewById(R.id.icon);
		ivIcon.setImageDrawable(icon);

		TextView tvTitle = view.findViewById(R.id.title);
		tvTitle.setText(title);

		CompoundButton cb = view.findViewById(R.id.compound_button);
		cb.setChecked(enabled);
		cb.setVisibility(View.VISIBLE);
		UiUtilities.setupCompoundButton(cb, nightMode, CompoundButtonType.GLOBAL);

		if (showShortDivider) {
			view.findViewById(R.id.short_divider).setVisibility(View.VISIBLE);
		}

		if (showLongDivider) {
			view.findViewById(R.id.long_divider).setVisibility(View.VISIBLE);
		}

		cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
			ivIcon.setColorFilter(isChecked ? activeColor : defColor);
			if (listener != null) {
				listener.onClick(buttonView);
			}
		});
		setupClickListener(view, v -> {
			boolean newState = !cb.isChecked();
			cb.setChecked(newState);
			ivIcon.setColorFilter(newState ? activeColor : defColor);
			if (listener != null) {
				listener.onClick(v);
			}
		});
		setupListItemBackground(view);

		return view;
	}

	private View createButtonWithDesc(int iconId,
	                                  @NonNull String title,
	                                  @NonNull String desc,
	                                  boolean enabled,
	                                  OnClickListener listener) {
		int activeColor = ColorUtilities.getActiveColor(app, nightMode);
		int defColor = ColorUtilities.getDefaultIconColor(app, nightMode);
		int iconColor = enabled ? activeColor : defColor;
		View view = inflater.inflate(R.layout.configure_screen_list_item, null);

		Drawable icon = getPaintedContentIcon(iconId, iconColor);
		ImageView ivIcon = view.findViewById(R.id.icon);
		ivIcon.setImageDrawable(icon);

		TextView tvTitle = view.findViewById(R.id.title);
		tvTitle.setText(title);

		TextView tvDesc = view.findViewById(R.id.description);
		tvDesc.setVisibility(View.VISIBLE);
		tvDesc.setText(desc);

		setupClickListener(view, listener);
		setupListItemBackground(view);
		return view;
	}

	private void setupListItemBackground(@NonNull View view) {
		View button = view.findViewById(R.id.button_container);
		int activeColor = ColorUtilities.getActiveColor(app, nightMode);
		Drawable background = UiUtilities.getColoredSelectableDrawable(app, activeColor, 0.3f);
		AndroidUtils.setBackground(button, background);
	}

	private void setupClickListener(@NonNull View view,
	                                @Nullable OnClickListener listener) {
		View button = view.findViewById(R.id.button_container);
		button.setOnClickListener(listener);
	}

	@Override
	public int getStatusBarColorId() {
		View view = getView();
		if (view != null && Build.VERSION.SDK_INT >= 23 && !nightMode) {
				view.setSystemUiVisibility(view.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
		}
		return ColorUtilities.getListBgColorId(nightMode);
	}

	public static void showInstance(@NonNull FragmentActivity activity) {
		FragmentManager fragmentManager = activity.getSupportFragmentManager();
		if (AndroidUtils.isFragmentCanBeAdded(fragmentManager, TAG)) {
			ConfigureScreenFragment fragment = new ConfigureScreenFragment();
			fragmentManager.beginTransaction()
					.add(R.id.fragmentContainer, fragment, TAG)
					.addToBackStack(TAG)
					.commitAllowingStateLoss();
		}
	}

}
