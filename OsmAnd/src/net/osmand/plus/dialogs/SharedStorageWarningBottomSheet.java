package net.osmand.plus.dialogs;

import android.os.Bundle;

import net.osmand.AndroidUtils;
import net.osmand.plus.R;
import net.osmand.plus.UiUtilities.DialogButtonType;
import net.osmand.plus.base.MenuBottomSheetDialogFragment;
import net.osmand.plus.base.bottomsheetmenu.BaseBottomSheetItem;
import net.osmand.plus.settings.datastorage.DataStorageFragment;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

import static net.osmand.aidlapi.OsmAndCustomizationConstants.DRAWER_SETTINGS_ID;

public class SharedStorageWarningBottomSheet extends MenuBottomSheetDialogFragment {

	private static final String TAG = SharedStorageWarningBottomSheet.class.getSimpleName();

	@Override
	public void createMenuItems(Bundle savedInstanceState) {
		items.add(new BaseBottomSheetItem.Builder()
			.setLayoutId(R.layout.bottom_sheet_shared_storage_warning)
			.create());
	}

	@Override
	protected boolean useVerticalButtons() {
		return true;
	}

	@Override
	protected void onRightBottomButtonClick() {
		FragmentManager fragmentManager = getFragmentManager();
		String tag = DataStorageFragment.class.getName();
		if (fragmentManager != null && AndroidUtils.isFragmentCanBeAdded(fragmentManager, tag)) {
			DataStorageFragment fragment = new DataStorageFragment();
			fragmentManager.beginTransaction()
					.replace(R.id.fragmentContainer, fragment, tag)
					.addToBackStack(DRAWER_SETTINGS_ID + ".new")
					.commitAllowingStateLoss();
		}
		dismiss();
	}

	@Override
	protected int getRightBottomButtonTextId() {
		return R.string.change_data_storage_folder;
	}

	@Override
	protected DialogButtonType getRightBottomButtonType() {
		return DialogButtonType.PRIMARY;
	}

	@Override
	protected void onDismissButtonClickAction() {
		dismiss();
	}

	public static void showInstance(@NonNull FragmentManager fragmentManager) {
		if (AndroidUtils.isFragmentCanBeAdded(fragmentManager, TAG)) {
			SharedStorageWarningBottomSheet fragment = new SharedStorageWarningBottomSheet();
			fragment.show(fragmentManager, TAG);
		}
	}
}