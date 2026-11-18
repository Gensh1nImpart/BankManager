package com.bankmanager.ui;

/** 页签刷新约定：切换到页签或点击刷新按钮时调用，内部必须异步取数。 */
public interface Refreshable {

    void refresh();
}
