package com.huaqin.market.ui;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.huaqin.market.R;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AppSecurityPermissions {

	private enum State {
		NO_PERMS,
		DANGEROUS_ONLY,
		NORMAL_ONLY,
		BOTH
	}
	
	private final static String TAG = "AppSecurityPermissions";
	private boolean localLOGV = false;
	
	private Context mContext;
	private PackageManager mPm;
	private LayoutInflater mInflater;
	private LinearLayout mPermsView;
	private HashMap<String, String> mDangerousMap;
	private HashMap<String, String> mNormalMap;
	private ArrayList<PermissionInfo> mPermsList;
	
	private String mDefaultGrpLabel;
	private String mDefaultGrpName = "DefaultGrp";
	private String mPermFormat;
	private Drawable mNormalIcon;
	private Drawable mDangerousIcon;
	
	private State mCurrentState;
	private LinearLayout mNonDangerousList;
	private LinearLayout mDangerousList;
	private HashMap<String, CharSequence> mGroupLabelCache;
	private View mNoPermsView;

	@SuppressWarnings("rawtypes")
	public AppSecurityPermissions(Context context, String[] pkgPermissions) {
		mContext = context;
		mPm = mContext.getPackageManager();
		mPermsList = new ArrayList<PermissionInfo>();
		HashSet<PermissionInfo> permSet = new HashSet<PermissionInfo>();
		
		// Extract all user permissions
		extractPerms(pkgPermissions, permSet);
		
		Iterator iterator = permSet.iterator();
		while (iterator.hasNext()) {
			PermissionInfo permInfo = (PermissionInfo) iterator.next();
			mPermsList.add(permInfo);
		}
	}

	private void extractPerms(String strList[], HashSet<PermissionInfo> permSet) {
		if ((strList == null) || (strList.length == 0)) {
			return;
		}
		
		for (String permName : strList) {
			try {
				PermissionInfo tmpPermInfo = mPm.getPermissionInfo(permName, 0);
				if (tmpPermInfo != null) {
					permSet.add(tmpPermInfo);
				}
			} catch (NameNotFoundException e) {
				Log.i(TAG, "Ignoring unknown permission:"+permName);
			}
		}
	}
	
	public int getPermissionCount() {
		return mPermsList.size();
	}
	
	public View getPermissionsView() {

		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mPermsView = (LinearLayout) mInflater.inflate(R.layout.app_perms_summary, null);
		
		mDangerousList = (LinearLayout) mPermsView.findViewById(R.id.dangerous_perms_list);
		mNonDangerousList = (LinearLayout) mPermsView.findViewById(R.id.non_dangerous_perms_list);
		mNoPermsView = mPermsView.findViewById(R.id.no_permissions);
		
		mDefaultGrpLabel = mContext.getString(R.string.default_permission_group);
		mPermFormat = mContext.getString(R.string.permissions_format);
		mNormalIcon = mContext.getResources().getDrawable(R.drawable.ic_appinfo_permission_not_warning);
		mDangerousIcon = mContext.getResources().getDrawable(R.drawable.ic_appinfo_permission_warning);
		
		// Set permissions view
		setPermissions(mPermsList);
		return mPermsView;
	}

    /**
     * Canonicalizes the group description before it is displayed to the user.
     *
     * TODO check for internationalization issues remove trailing '.' in str1
     */
    private String canonicalizeGroupDesc(String groupDesc) {
        if ((groupDesc == null) || (groupDesc.length() == 0)) {
            return null;
        }
        // Both str1 and str2 are non-null and are non-zero in size.
        int len = groupDesc.length();
        if (groupDesc.charAt(len - 1) == '.') {
            groupDesc = groupDesc.substring(0, len - 1);
        }
        return groupDesc;
    }

	/**
	 * Utility method that concatenates two strings defined by mPermFormat.
	 * a null value is returned if both str1 and str2 are null, if one of the strings
	 * is null the other non null value is returned without formatting
	 * this is to placate initial error checks
	 */
	private String formatPermissions(String groupDesc, CharSequence permDesc) {
		if (groupDesc == null) {
			if (permDesc == null) {
				return null;
			}
			return permDesc.toString();
		}
		
		groupDesc = canonicalizeGroupDesc(groupDesc);
		if (permDesc == null) {
			return groupDesc;
		}
		// groupDesc and permDesc are non null
		return String.format(mPermFormat, groupDesc, permDesc.toString());
	}

    private CharSequence getGroupLabel(String grpName) {
        if (grpName == null) {
            //return default label
            return mDefaultGrpLabel;
        }
        
        CharSequence cachedLabel = mGroupLabelCache.get(grpName);
        if (cachedLabel != null) {
            return cachedLabel;
        }
        
        PermissionGroupInfo permGrpInfo;
        try {
        	permGrpInfo = mPm.getPermissionGroupInfo(grpName, 0);
        } catch (NameNotFoundException e) {
            Log.i(TAG, "Invalid group name:" + grpName);
            return null;
        }
        CharSequence label = permGrpInfo.loadLabel(mPm).toString();
        mGroupLabelCache.put(grpName, label);
        return label;
    }

    /**
     * Utility method that displays permissions from a map containing group name and
     * list of permission descriptions.
     */
    private void displayPermissions(boolean dangerous) {
    	HashMap<String, String> permInfoMap = dangerous ? mDangerousMap : mNormalMap;
        LinearLayout permListView = dangerous ? mDangerousList : mNonDangerousList;
        
        permListView.removeAllViews();
        
        Set<String> permInfoStrSet = permInfoMap.keySet();
        for (String loopPermGrpInfoStr : permInfoStrSet) {
            CharSequence grpLabel = getGroupLabel(loopPermGrpInfoStr);
            //guaranteed that grpLabel wont be null since permissions without groups
            //will belong to the default group
            if (localLOGV) {
            	Log.i(TAG, "Adding view group:" + grpLabel + ", desc:" + permInfoMap.get(loopPermGrpInfoStr));
            }
            permListView.addView(getPermissionItemView(grpLabel,
                    permInfoMap.get(loopPermGrpInfoStr), dangerous));
        }
    }

    private void displayNoPermissions() {
        mNoPermsView.setVisibility(View.VISIBLE);
    }

    private View getPermissionItemView(CharSequence grpName, CharSequence permList,
            boolean dangerous) {

        return getPermissionItemView(mContext, mInflater, grpName, permList,
                dangerous, dangerous ? mDangerousIcon : mNormalIcon);
    }

	private static View getPermissionItemView(Context context, LayoutInflater inflater,
			CharSequence grpName, CharSequence permList, boolean dangerous, Drawable icon) {

		View permView = inflater.inflate(R.layout.app_permission_item, null);
		
		ImageView imgView = (ImageView) permView.findViewById(R.id.perm_icon);
		imgView.setImageDrawable(icon);
		
		TextView permGrpView = (TextView) permView.findViewById(R.id.permission_group);
		TextView permDescView = (TextView) permView.findViewById(R.id.permission_list);
		
		if (dangerous) {
			final Resources resources = context.getResources();
			
			permGrpView.setTextColor(resources.getColor(R.color.perms_dangerous_grp_color));
			permDescView.setTextColor(resources.getColor(R.color.perms_dangerous_perm_color));
		} else {
			permGrpView.setTextColor(Color.BLACK);
			permDescView.setTextColor(Color.BLACK);
		}
		
		if (grpName != null) {
			permGrpView.setText(grpName);
			permDescView.setText(permList);
		} else {
			permGrpView.setText(permList);
			permDescView.setVisibility(View.GONE);
		}
		return permView;
    }

	private void showPermissions() {

		switch (mCurrentState) {
		case NO_PERMS:
			displayNoPermissions();
			break;
			
		case DANGEROUS_ONLY:
			displayPermissions(true);
			mNonDangerousList.setVisibility(View.GONE);
			break;
			
		case NORMAL_ONLY:
			displayPermissions(false);
			mDangerousList.setVisibility(View.GONE);
			break;
			
		case BOTH:
			displayPermissions(true);
			mDangerousList.setVisibility(View.VISIBLE);
			
			displayPermissions(false);
			mNonDangerousList.setVisibility(View.VISIBLE);
			break;
		}
	}

	private boolean isDisplayablePermission(PermissionInfo pInfo) {
		if (pInfo.protectionLevel == PermissionInfo.PROTECTION_DANGEROUS ||
				pInfo.protectionLevel == PermissionInfo.PROTECTION_NORMAL) {
			return true;
		}
		return false;
	}

	/*
	 * Utility method that aggregates all permission descriptions categorized by group
	 * Say group1 has perm11, perm12, perm13, the group description will be
	 * perm11_Desc, perm12_Desc, perm13_Desc
	 */
	private void aggregateGroupDescs(HashMap<String, ArrayList<PermissionInfo> > map,
			HashMap<String, String> retMap) {

		if (map == null || retMap == null) {
			return;
		}
		
		Set<String> grpNames = map.keySet();
		Iterator<String> grpNamesIter = grpNames.iterator();
		while (grpNamesIter.hasNext()) {
			String grpDesc = null;
			String grpNameKey = grpNamesIter.next();
			ArrayList<PermissionInfo> grpPermsList = map.get(grpNameKey);
			
			if (grpPermsList == null) {
				continue;
			}
			
			for (PermissionInfo permInfo : grpPermsList) {
				CharSequence permDesc = permInfo.loadLabel(mPm);
				grpDesc = formatPermissions(grpDesc, permDesc);
			}
			// Insert grpDesc into map
			if (grpDesc != null) {
				if (localLOGV) {
					Log.i(TAG, "Group:" + grpNameKey + " description:" + grpDesc.toString());
				}
				retMap.put(grpNameKey, grpDesc.toString());
			}
		}
	}

	private void setPermissions(ArrayList<PermissionInfo> permList) {
		mGroupLabelCache = new HashMap<String, CharSequence>();
		//add the default label so that uncategorized permissions can go here
		mGroupLabelCache.put(mDefaultGrpName, mDefaultGrpLabel);
	    
		// Map containing group names and a list of permissions under that group
		// categorized as dangerous
		mDangerousMap = new HashMap<String, String>();
		// Map containing group names and a list of permissions under that group
		// categorized as normal
		mNormalMap = new HashMap<String, String>();
	    
		// Additional structures needed to ensure that permissions are unique under 
		// each group
		HashMap<String, ArrayList<PermissionInfo>> dangerousMap = 
			new HashMap<String, ArrayList<PermissionInfo>>();
		HashMap<String, ArrayList<PermissionInfo> > normalMap = 
			new HashMap<String, ArrayList<PermissionInfo>>();
		PermissionInfoComparator permComparator = new PermissionInfoComparator(mPm);
		
		if (permList != null) {
			// First pass to group permissions
			for (PermissionInfo pInfo : permList) {
				if (localLOGV) {
					Log.i(TAG, "Processing permission:" + pInfo.name);
				}
				if (!isDisplayablePermission(pInfo)) {
					if (localLOGV) {
						Log.i(TAG, "Permission:" + pInfo.name + " is not displayable");
					}
					continue;
				}
				
				HashMap<String, ArrayList<PermissionInfo> > permInfoMap =
					(pInfo.protectionLevel == PermissionInfo.PROTECTION_DANGEROUS) ?
							dangerousMap : normalMap;
				String grpName = (pInfo.group == null) ? mDefaultGrpName : pInfo.group;
				
				if (localLOGV) {
					Log.i(TAG, "Permission:" + pInfo.name + " belongs to group:" + grpName);
				}
				
				ArrayList<PermissionInfo> grpPermsList = permInfoMap.get(grpName);
				if (grpPermsList == null) {
					grpPermsList = new ArrayList<PermissionInfo>();
					grpPermsList.add(pInfo);
					permInfoMap.put(grpName, grpPermsList);
				} else {
					int idx = Collections.binarySearch(grpPermsList, pInfo, permComparator);
					
					if (localLOGV) {
						Log.i(TAG, "idx=" + idx + ", list.size=" + grpPermsList.size());
					}
					
					if (idx < 0) {
					    idx = -idx - 1;
					    grpPermsList.add(idx, pInfo);
					}
				}
			}
			// Second pass to actually form the descriptions
			// Look at dangerous permissions first
			aggregateGroupDescs(dangerousMap, mDangerousMap);
			aggregateGroupDescs(normalMap, mNormalMap);
		}
	    
		mCurrentState = State.NO_PERMS;
		
		if (mDangerousMap.size() > 0) {
			mCurrentState = (mNormalMap.size() > 0) ? State.BOTH : State.DANGEROUS_ONLY;
		} else if (mNormalMap.size() > 0) {
			mCurrentState = State.NORMAL_ONLY;
		}
		
		if (localLOGV) {
	    	Log.i(TAG, "mCurrentState=" + mCurrentState);
	    }
	    showPermissions();
	}

	private static class PermissionInfoComparator implements Comparator<PermissionInfo> {

		private PackageManager mPm;
		
		private final Collator sCollator = Collator.getInstance();

		PermissionInfoComparator(PackageManager pm) {
			mPm = pm;
		}
		
		@Override
		public final int compare(PermissionInfo a, PermissionInfo b) {
		    CharSequence sa = a.loadLabel(mPm);
		    CharSequence sb = b.loadLabel(mPm);
		    
		    return sCollator.compare(sa, sb);
		}
	}
}