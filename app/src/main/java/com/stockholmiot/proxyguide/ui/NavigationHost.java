package com.stockholmiot.proxyguide.ui;

import androidx.fragment.app.Fragment;

public interface NavigationHost {
    void navigateTo(Fragment fragment, boolean addToBackstack);
    void navigateToCustom(Fragment fragment, boolean addToBackstack);
    void navigateToMap(Fragment fragment, boolean addToBackstack);
}
