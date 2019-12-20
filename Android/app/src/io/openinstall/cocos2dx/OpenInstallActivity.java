/****************************************************************************
 Copyright (c) 2015 Chukong Technologies Inc.

 http://www.cocos2d-x.org

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 ****************************************************************************/
package io.openinstall.cocos2dx;

import android.content.Intent;
import android.os.Bundle;

import com.fm.openinstall.OpenInstall;

import org.cocos2dx.lib.Cocos2dxActivity;

public class OpenInstallActivity extends Cocos2dxActivity {

    private static Cocos2dxActivity cocos2dxActivity = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cocos2dxActivity = this;
        OpenInstall.init(this);
        OpenInstallHelper.getWakeup(getIntent(), cocos2dxActivity);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        OpenInstallHelper.getWakeup(intent, cocos2dxActivity);
    }

    public static void getInstall(int s, int luaFunc) {
        OpenInstallHelper.getInstall(s, luaFunc, cocos2dxActivity);
    }

    public static void registerWakeupCallback(int luaFunc){
        OpenInstallHelper.registerWakeupCallback(luaFunc, cocos2dxActivity);
    }

    public static void reportEffectPoint(String pointId, int pointValue){
        OpenInstall.reportEffectPoint(pointId, pointValue);
    }

}
