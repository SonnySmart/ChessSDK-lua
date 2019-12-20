package org.cocos2dx.lua;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.alibaba.fastjson.JSON;
import com.tencent.mm.opensdk.modelmsg.SendAuth;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.cocos2dx.lib.Cocos2dxLuaJavaBridge;

import java.util.List;

public class ChessWeChat {

    public static String TAG = ChessWeChat.class.getSimpleName();

    public static int INVALID_LUAFUNC = -1;

    private static IWXAPI api; // 相应的包，请集成SDK后自行引入

    private static Context mContent;

    private static int mLuaFunc = INVALID_LUAFUNC;

    /**
     * 登录结果返回
     * */
    public static class LoginResult {
        public static int LOGIN_OK = 0;
        public static int LOGIN_CANCEL = 1;
        public static int LOGIN_FAILED = 2;

        private int mCode;
        private String mMessage;
        private String mData;

        public void setCode(int code) {
            mCode = code;
        }

        public void setMessage(String message) {
            mMessage = message;
        }

        public void setData(String data) {
            mData = data;
        }

        public String serialize() {
            return JSON.toJSONString(this);
        }
    }

    private static void getMetaData(Context context) {
        ApplicationInfo ai = null;
        try {
            ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return;
        }
        Bundle bundle = ai.metaData;
        ChessConfig.WECHAT_APPID = bundle.getString("WECHAT_APPID");
        ChessConfig.WECHAT_APPSECRET = bundle.getString("WECHAT_APPSECRET");
    }

    private static void callLuaFunc(LoginResult result) {
        if (result != null && mLuaFunc > INVALID_LUAFUNC) {
            Cocos2dxLuaJavaBridge.callLuaFunctionWithString(mLuaFunc, result.serialize());
        }
    }

    /**
     * 初始化获取api metadata
     * @param context 上下文
     * */
    public static IWXAPI getApi(Context context) {
        mContent = context;
        if (api == null && mContent != null) {
            api = WXAPIFactory.createWXAPI(mContent, ChessConfig.WECHAT_APPID);

            getMetaData(mContent);
        }
        return api;
    }

    /**
     * 判断微信客户端是否存在
     *
     * @return true安装, false未安装
     */
    public static boolean isWeChatAppInstalled(Context context) {
        if(getApi(context).isWXAppInstalled()) {
            return true;
        } else {
            final PackageManager packageManager = context.getPackageManager();// 获取packagemanager
            List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);// 获取所有已安装程序的包信息
            if (pinfo != null) {
                for (int i = 0; i < pinfo.size(); i++) {
                    String pn = pinfo.get(i).packageName;
                    if (pn.equalsIgnoreCase("com.tencent.mm")) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    /**
     *  微信登录
     * @param context 上下文
     * @param luaFunc lua回调
     * */
    public static void sendLogin(Context context, int luaFunc) {
        mLuaFunc = luaFunc;
        if (!isWeChatAppInstalled(context)) {
            LoginResult result = new LoginResult();
            result.setCode(LoginResult.LOGIN_FAILED);
            result.setMessage("未安装微信");
            callLuaFunc(result);
            return;
        }
        final SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = "diandi_wx_login";
        getApi(context).sendReq(req);
    }
}
