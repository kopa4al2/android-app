package com.example.sharedtravel.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.example.sharedtravel.R;
import com.example.sharedtravel.fragments.AllChatsFragment;
import com.example.sharedtravel.fragments.MyTravelsFragment;

public class EditUserTabsAdapter extends FragmentPagerAdapter {
    private Context ctx;

    public EditUserTabsAdapter(Context ctx, FragmentManager fm) {
        super(fm);
        this.ctx = ctx;
    }

    @Override
    public Fragment getItem(int i) {
        if (i == 0) {
            return new AllChatsFragment();
        } else if (i == 1) {
            return new MyTravelsFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        //Number of tabs
        return 2;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return ctx.getString(R.string.messages);
            case 1:
                return ctx.getString(R.string.view_travels);

            default:
                return null;
        }
    }


}
